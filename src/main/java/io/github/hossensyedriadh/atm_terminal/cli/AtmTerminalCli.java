package io.github.hossensyedriadh.atm_terminal.cli;

import io.github.hossensyedriadh.atm_terminal.enumerator.AuthenticationResult;
import io.github.hossensyedriadh.atm_terminal.model.BalanceInquiryResult;
import io.github.hossensyedriadh.atm_terminal.service.AtmAuthenticationService;
import io.github.hossensyedriadh.atm_terminal.service.BalanceInquiryService;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Arrays;
import java.util.Scanner;

@Log4j2
@Component
public class AtmTerminalCli implements CommandLineRunner {
    private final AtmAuthenticationService atmAuthenticationService;
    private final BalanceInquiryService balanceInquiryService;

    @Autowired
    public AtmTerminalCli(AtmAuthenticationService atmAuthenticationService, BalanceInquiryService balanceInquiryService) {
        this.atmAuthenticationService = atmAuthenticationService;
        this.balanceInquiryService = balanceInquiryService;
    }

    @Override
    public void run(String @NonNull ... args) {
        Console console = System.console();

        if (console == null) {
            System.err.println("Run the application from an interactive console");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        console.printf("Welcome to ATM Terminal\n");
        console.printf("Enter PAN: ");
        String pan = scanner.nextLine().trim();

        char[] pinChars = console.readPassword("Enter PIN: ");

        if (pinChars == null) {
            return;
        }

        String pin = new String(pinChars);

        Arrays.fill(pinChars, '\0');

        try {
            AuthenticationResult result = this.atmAuthenticationService.authenticate(pan, pin);

            switch (result) {
                case SUCCESS -> this.displayMenu(scanner, console, pan);
                case INVALID_PIN -> console.printf("Incorrect PIN\n");
                case PIN_BLOCKED -> console.printf("PIN Blocked\n");
                case SYSTEM_ERROR -> console.printf("Unable to authenticate card\n");
            }
        } catch (Exception e) {
            console.printf("Unable to connect to ATM Switch\n");
            log.error("Unable to connect to ATM Switch", e);
        }
    }

    private void displayMenu(Scanner scanner, Console console, String pan) {
        while (true) {
            console.printf("\n");
            console.printf("=== ATM Menu ===\n");
            console.printf("1. Balance Inquiry\n");
            console.printf("2. Withdraw\n");
            console.printf("3. Exit\n");
            console.printf("\n");
            console.printf("Select: ");

            String selection = scanner.nextLine().trim();

            switch (selection) {
                case "1" -> {
                    try {
                        BalanceInquiryResult result = this.balanceInquiryService.inquire(pan);

                        if (result.successful()) {
                            console.printf("Balance inquiry successful\n");
                            console.printf("Available balance: %s %s\n", result.currency(), result.availableBalance());
                        } else {
                            if (result.message() != null) {
                                console.printf("Balance inquiry failed. Reason: %s\n", result.message());
                            } else {
                                console.printf("Balance inquiry declined\n");
                            }
                        }
                    } catch (Exception e) {
                        console.printf("Balance inquiry failed\n");
                        log.error("Balance inquiry failed", e);
                    }
                }
                case "2" -> console.printf("Withdrawal in development\n");
                case "3" -> {
                    console.printf("Card session ended\n");
                    return;
                }
                default -> console.printf("Invalid selection\n");
            }
        }
    }
}
