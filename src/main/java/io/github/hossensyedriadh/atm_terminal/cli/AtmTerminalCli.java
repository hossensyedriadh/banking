
package io.github.hossensyedriadh.atm_terminal.cli;

import io.github.hossensyedriadh.atm_terminal.model.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.model.BalanceInquiryResult;
import io.github.hossensyedriadh.atm_terminal.model.WithdrawalResult;
import io.github.hossensyedriadh.atm_terminal.service.AtmAuthenticationService;
import io.github.hossensyedriadh.atm_terminal.service.AtmSessionTerminationService;
import io.github.hossensyedriadh.atm_terminal.service.BalanceInquiryService;
import io.github.hossensyedriadh.atm_terminal.service.WithdrawalService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;

@Log4j2
@Component
public class AtmTerminalCli {
    private final AtmAuthenticationService atmAuthenticationService;
    private final BalanceInquiryService balanceInquiryService;
    private final WithdrawalService withdrawalService;
    private final AtmSessionTerminationService atmSessionTerminationService;

    public AtmTerminalCli(AtmAuthenticationService atmAuthenticationService, BalanceInquiryService balanceInquiryService, WithdrawalService withdrawalService, AtmSessionTerminationService atmSessionTerminationService) {
        this.atmAuthenticationService = atmAuthenticationService;
        this.balanceInquiryService = balanceInquiryService;
        this.withdrawalService = withdrawalService;
        this.atmSessionTerminationService = atmSessionTerminationService;
    }

    public void run(InputStream input, OutputStream output) {
        try (ConsoleInput consoleInput = new ConsoleInput(input, output)) {
            boolean terminalRunning = true;

            while (terminalRunning) {
                consoleInput.clearScreen();
                terminalRunning = this.runCardSession(consoleInput);
            }

        } catch (EOFException e) {
            log.info("ATM terminal client disconnected");
        } catch (Exception e) {
            log.error("ATM terminal session failed", e);
        }
    }

    private boolean runCardSession(ConsoleInput input) throws IOException {
        PrintWriter output = input.writer();
        output.println("Welcome to ATM Terminal");
        output.println();

        String pan = input.readPan();
        String pin = input.readPin();

        try {
            AuthenticationResult result = this.atmAuthenticationService.authenticate(pan, pin);

            switch (result.status()) {
                case SUCCESS -> this.displayMenu(input, pan, result.sessionToken());
                case INVALID_PIN -> output.println("Incorrect PIN");
                case PIN_BLOCKED -> output.println("PIN blocked");
                case SYSTEM_ERROR -> output.println("Unable to authenticate card");
            }

        } catch (EOFException e) {
            throw e;
        } catch (Exception e) {
            output.println("Unable to connect to ATM Switch");
            log.error("ATM authentication failed", e);
        }

        output.println();
        input.readLine("Press Enter to continue...");

        return true;
    }

    private void displayMenu(ConsoleInput input, String pan, String sessionToken) throws IOException {
        PrintWriter output = input.writer();

        try {
            while (true) {
                output.println();
                output.println("=== ATM Menu ===");
                output.println("1. Balance Inquiry");
                output.println("2. Withdraw");
                output.println("3. Exit");
                output.println();

                String selection = input.readLine("Select: ");

                switch (selection) {
                    case "1" -> this.handleBalanceInquiry(pan, sessionToken, output);

                    case "2" -> this.handleWithdrawal(input, pan, sessionToken, output);

                    case "3" -> {
                        output.println("Car session ended");
                        return;
                    }

                    default -> output.println("Invalid selection");
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
                output.println();
                output.println("Balance inquiry successful");
                output.printf("Available balance: %s %s%n", result.currency(), result.availableBalance());
            } else {
                output.println(result.message() != null ? "Balance inquiry failed. Reason: " + result.message() : "Balance inquiry declined");
            }

            output.println();
        } catch (Exception e) {
            log.error("Balance inquiry failed", e);
            output.println("Balance inquiry failed");
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
                output.println();
                output.println("Withdrawal successful");
                output.println("Amount: " + result.currency() + " " + result.amount());
                output.println(result.message());
            } else {
                output.println(result.message() != null ? "Withdrawal failed. Reason: " + result.message() : "Withdrawal declined");
            }

            output.println();
        } catch (EOFException e) {
            throw e;

        } catch (Exception e) {
            log.error("Withdrawal failed", e);
            output.println("Unable to confirm the withdrawal. Please check the transaction status.");
        }
    }

    private void terminateSession(String pan, String sessionToken, PrintWriter output) {
        try {
            boolean terminated = this.atmSessionTerminationService.terminate(pan, sessionToken);

            if (terminated) {
                output.println("ATM session terminated");
            } else {
                output.println("Could not confirm session termination. The session may expire automatically");
            }
        } catch (Exception e) {
            log.error("ATM session termination failed", e);
            output.println("Unable to confirm ATM session termination.");
        }

        output.flush();
    }
}
