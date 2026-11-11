package io.github.hossensyedriadh.atm_terminal.cli;

import io.github.hossensyedriadh.atm_terminal.model.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.model.BalanceInquiryResult;
import io.github.hossensyedriadh.atm_terminal.model.WithdrawalResult;
import io.github.hossensyedriadh.atm_terminal.service.AtmAuthenticationService;
import io.github.hossensyedriadh.atm_terminal.service.AtmSessionTerminationService;
import io.github.hossensyedriadh.atm_terminal.service.BalanceInquiryService;
import io.github.hossensyedriadh.atm_terminal.service.WithdrawalService;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Log4j2
@Component
public class AtmTerminalCli implements CommandLineRunner {
    private final AtmAuthenticationService atmAuthenticationService;
    private final BalanceInquiryService balanceInquiryService;
    private final WithdrawalService withdrawalService;
    private final AtmSessionTerminationService atmSessionTerminationService;

    public AtmTerminalCli(AtmAuthenticationService atmAuthenticationService, BalanceInquiryService balanceInquiryService,
                          WithdrawalService withdrawalService, AtmSessionTerminationService atmSessionTerminationService) {
        this.atmAuthenticationService = atmAuthenticationService;
        this.balanceInquiryService = balanceInquiryService;
        this.withdrawalService = withdrawalService;
        this.atmSessionTerminationService = atmSessionTerminationService;
    }

    @Override
    public void run(String @NonNull ... args) {
        try (ConsoleInput input = new ConsoleInput()) {
            boolean terminalRunning = true;

            while (terminalRunning) {
                input.clearScreen();

                terminalRunning = runCardSession(input);
            }
        } catch (Exception e) {
            log.error("ATM terminal input failed", e);
            System.err.println("Unable to start interactive ATM terminal");
        }
    }

    private boolean runCardSession(ConsoleInput input) {
        System.out.println("Welcome to ATM Terminal");
        System.out.println();

        String pan = input.readPan();
        String pin = input.readPin();

        try {
            AuthenticationResult result = this.atmAuthenticationService.authenticate(pan, pin);

            switch (result.status()) {
                case SUCCESS -> this.displayMenu(input, pan, result.sessionToken());
                case INVALID_PIN -> System.out.println("Incorrect PIN");
                case PIN_BLOCKED -> System.out.println("PIN blocked");
                case SYSTEM_ERROR -> System.err.println("Unable to authenticate card");
            }
        } catch (Exception e) {
            System.err.println("Unable to connect to ATM Switch");
            log.error("ATM authentication failed", e);
        }

        input.readLine("Press Enter to continue...");
        return true;
    }

    private void displayMenu(ConsoleInput input, String pan, String sessionToken) {
        while (true) {
            System.out.println();
            System.out.println("=== ATM Menu ===");
            System.out.println("1. Balance Inquiry");
            System.out.println("2. Withdraw");
            System.out.println("3. Exit");
            System.out.println();

            String selection = input.readLine("Select: ");

            switch (selection) {
                case "1" -> {
                    try {
                        BalanceInquiryResult result = this.balanceInquiryService.inquire(pan, sessionToken);

                        if (result.successful()) {
                            System.out.println();
                            System.out.println("Balance inquiry successful");
                            System.out.printf("Available balance: %s %s%n", result.currency(), result.availableBalance());
                        } else {
                            System.err.println(result.message() != null ? "Balance inquiry failed. Reason: " + result.message()
                                    : "Balance inquiry declined");
                        }
                        System.out.println();
                    } catch (Exception e) {
                        System.err.println("Balance inquiry failed");
                        log.error("Balance inquiry failed", e);
                    }
                }
                case "2" -> {
                    try {
                        String amountInput = input.readAmount("Enter withdrawal amount (BDT): ").trim();

                        BigDecimal amount = new BigDecimal(amountInput);

                        if (amount.signum() <= 0) {
                            log.error("Withdrawal amount must be greater than zero");
                            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
                        }

                        if (amount.remainder(new BigDecimal(500)).compareTo(BigDecimal.ZERO) != 0) {
                            throw new IllegalArgumentException("Withdrawal amount must be in multiples of 500");
                        }

                        WithdrawalResult result = this.withdrawalService.withdraw(pan, amount, sessionToken);

                        if (result.successful()) {
                            System.out.println();
                            System.out.println("Withdrawal successful");
                            System.out.println("Amount: " + result.currency() + " " + result.amount());
                            System.out.println(result.message());
                        } else {
                            System.err.println(result.message() != null ? "Withdrawal failed. Reason: " + result.message() : "Withdrawal declined");
                        }
                        System.out.println();
                    } catch (Exception e) {
                        log.error("Withdrawal failed", e);
                        System.err.println("Unable to confirm the withdrawal. Please check the transaction status.");
                    }
                }
                case "3" -> {
                    try {
                        boolean terminated = this.atmSessionTerminationService.terminate(pan, sessionToken);

                        if (terminated) {
                            System.out.println("ATM session terminated");
                        } else {
                            System.err.println("Could not confirm session termination. The session may expire automatically");
                        }
                    } finally {
                        System.out.println("Car session ended");
                    }

                    return;
                }
                default -> System.err.println("Invalid selection");
            }
        }
    }
}
