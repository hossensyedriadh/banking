package io.github.hossensyedriadh.atm_terminal.cli;

import io.github.hossensyedriadh.atm_terminal.enumerator.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.model.BalanceInquiryResult;
import io.github.hossensyedriadh.atm_terminal.service.AtmAuthenticationService;
import io.github.hossensyedriadh.atm_terminal.service.BalanceInquiryService;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AtmTerminalCli implements CommandLineRunner {
    private final AtmAuthenticationService atmAuthenticationService;
    private final BalanceInquiryService balanceInquiryService;

    public AtmTerminalCli(AtmAuthenticationService atmAuthenticationService, BalanceInquiryService balanceInquiryService) {
        this.atmAuthenticationService = atmAuthenticationService;
        this.balanceInquiryService = balanceInquiryService;
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

            switch (result) {
                case SUCCESS -> this.displayMenu(input, pan);
                case INVALID_PIN -> System.out.println("Incorrect PIN");
                case PIN_BLOCKED -> System.out.println("PIN blocked");
                case SYSTEM_ERROR -> System.out.println("Unable to authenticate card");
            }
        } catch (Exception e) {
            System.out.println("Unable to connect to ATM Switch");
            log.error("ATM authentication failed");
        }

        input.readLine("Press Enter to continue...");
        return true;
    }

    private void displayMenu(ConsoleInput input, String pan) {
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
                        BalanceInquiryResult result = this.balanceInquiryService.inquire(pan);

                        if (result.successful()) {
                            System.out.println("Balance inquiry successful");
                            System.out.printf("Available balance: %s %s%n", result.currency(), result.availableBalance());
                        } else {
                            System.out.println(result.message() != null ? "Balance inquiry failed. Reason: " + result.message()
                                    : "Balance inquiry declined");
                        }
                    } catch (Exception e) {
                        System.out.println("Balance inquiry failed");
                        log.error("Balance inquiry failed");
                    }
                }
                case "2" -> System.out.println("Withdrawal in development");
                case "3" -> {
                    System.out.println("Card session ended");
                    return;
                }
                default -> System.out.println("Invalid selection");
            }
        }
    }
}
