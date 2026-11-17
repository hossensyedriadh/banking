
package io.github.hossensyedriadh.atm_terminal.cli;

import io.github.hossensyedriadh.atm_terminal.configuration.properties.AtmTerminalSessionProperties;
import io.github.hossensyedriadh.atm_terminal.model.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.model.BalanceInquiryResult;
import io.github.hossensyedriadh.atm_terminal.model.WithdrawalResult;
import io.github.hossensyedriadh.atm_terminal.service.AtmAuthenticationService;
import io.github.hossensyedriadh.atm_terminal.service.AtmSessionTerminationService;
import io.github.hossensyedriadh.atm_terminal.service.BalanceInquiryService;
import io.github.hossensyedriadh.atm_terminal.service.WithdrawalService;
import lombok.extern.log4j.Log4j2;
import org.jdom2.output.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;

@Log4j2
@Component
public class AtmTerminalCli {
    private final AtmAuthenticationService atmAuthenticationService;
    private final BalanceInquiryService balanceInquiryService;
    private final WithdrawalService withdrawalService;
    private final AtmSessionTerminationService atmSessionTerminationService;
    private final AtmTerminalSessionProperties sessionProperties;

    @Autowired
    public AtmTerminalCli(AtmAuthenticationService atmAuthenticationService, BalanceInquiryService balanceInquiryService, WithdrawalService withdrawalService, AtmSessionTerminationService atmSessionTerminationService, AtmTerminalSessionProperties sessionProperties) {
        this.atmAuthenticationService = atmAuthenticationService;
        this.balanceInquiryService = balanceInquiryService;
        this.withdrawalService = withdrawalService;
        this.atmSessionTerminationService = atmSessionTerminationService;
        this.sessionProperties = sessionProperties;
    }

    public void run(Socket socket) {
        try (ConsoleInput consoleInput = new ConsoleInput(socket, this.sessionProperties)) {
            boolean terminalRunning = true;

            while (terminalRunning) {
                consoleInput.clearScreen();
                terminalRunning = this.runCardSession(consoleInput);
            }

        } catch (ConsoleInput.SessionTimeoutException e) {
            log.info("ATM terminal session terminated: {}", e.getMessage());
        } catch (EOFException e) {
            log.info("ATM terminal client disconnected");
        } catch (Exception e) {
            log.error("ATM terminal session failed", e);
        }
    }

    private boolean runCardSession(ConsoleInput input) throws IOException {
        PrintWriter output = input.writer();
        this.writeLine(output, "Welcome to ATM Terminal");
        this.writeLine(output, "");

        String pan = input.readPan();
        String pin = input.readPin();

        try {
            AuthenticationResult result = this.atmAuthenticationService.authenticate(pan, pin);

            switch (result.status()) {
                case SUCCESS -> this.displayMenu(input, pan, result.sessionToken());
                case INVALID_PIN -> this.writeLine(output, "Incorrect PIN");
                case PIN_BLOCKED -> this.writeLine(output, "PIN blocked");
                case SYSTEM_ERROR -> this.writeLine(output, "Unable to authenticate card");
            }

        } catch (EOFException e) {
            throw e;
        } catch (Exception e) {
            this.writeLine(output, "Unable to connect to ATM Switch");
            log.error("ATM authentication failed", e);
        }

        this.writeLine(output, "");
        input.readLine("Press Enter to continue...");

        return true;
    }

    private void displayMenu(ConsoleInput input, String pan, String sessionToken) throws IOException {
        PrintWriter output = input.writer();

        try {
            while (true) {
                this.writeLine(output, "");
                this.writeLine(output, "=== ATM Menu ===");
                this.writeLine(output, "1. Balance Inquiry");
                this.writeLine(output, "2. Withdraw");
                this.writeLine(output, "3. Exit");
                this.writeLine(output, "");

                String selection = input.readLine("Select: ");

                switch (selection) {
                    case "1" -> this.handleBalanceInquiry(pan, sessionToken, output);

                    case "2" -> this.handleWithdrawal(input, pan, sessionToken, output);

                    case "3" -> {
                        this.writeLine(output, "Card session ended");
                        return;
                    }

                    default -> this.writeLine(output, "Invalid selection");
                }

                output.flush();
            }

        } finally {
            this.terminateSession(pan, sessionToken, output);
        }
    }

    private void handleBalanceInquiry(String pan, String sessionToken, PrintWriter output) {
        try {
            BalanceInquiryResult result = this.balanceInquiryService.inquire(pan, sessionToken);

            if (result.successful()) {
                this.writeLine(output, "");
                this.writeLine(output, "Balance inquiry successful");
                output.printf("Available balance: %s %s\r\n", result.currency(), result.availableBalance());
            } else {
                this.writeLine(output, result.message() != null ? "Balance inquiry failed. Reason: " + result.message() : "Balance inquiry declined");
            }

            this.writeLine(output, "");
        } catch (Exception e) {
            log.error("Balance inquiry failed", e);
            this.writeLine(output, "Balance inquiry failed");
        }
    }

    private void handleWithdrawal(ConsoleInput input, String pan, String sessionToken, PrintWriter output) throws IOException {
        try {
            String amountInput = input.readAmount("Enter withdrawal amount (BDT): ").trim();

            BigDecimal amount = new BigDecimal(amountInput);

            if (amount.signum() <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
            }

            if (amount.remainder(new BigDecimal("500")).compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Withdrawal amount must be in multiples of 500");
            }

            WithdrawalResult result = this.withdrawalService.withdraw(pan, amount, sessionToken);

            if (result.successful()) {
                this.writeLine(output, "");
                this.writeLine(output, "Withdrawal successful");
                this.writeLine(output, "Amount: " + result.currency() + " " + result.amount());
                this.writeLine(output, result.message());
            } else {
                this.writeLine(output, result.message() != null ? "Withdrawal failed. Reason: " + result.message() : "Withdrawal declined");
            }

            this.writeLine(output, "");
        } catch (ConsoleInput.SessionTimeoutException | EOFException e) {
            throw e;
        } catch (Exception e) {
            log.error("Withdrawal failed", e);
            this.writeLine(output, "Unable to confirm the withdrawal. Please check the transaction status.");
        }
    }

    private void terminateSession(String pan, String sessionToken, PrintWriter output) {
        try {
            boolean terminated = this.atmSessionTerminationService.terminate(pan, sessionToken);

            if (terminated) {
                this.writeLine(output, "ATM session terminated");
            } else {
                this.writeLine(output, "Could not confirm session termination. The session may expire automatically");
            }
        } catch (Exception e) {
            log.error("ATM session termination failed", e);
            this.writeLine(output, "Unable to confirm ATM session termination.");
        }

        output.flush();
    }

    private void writeLine(PrintWriter output, String text) {
        output.print(text);
        output.print(LineSeparator.CRNL.value());
        output.flush();
    }
}
