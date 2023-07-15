// Note to self; Main purpose - Personal Use
// Be mindful of file paths(Both here and in PDFToText.java),
// make sure to have pdfBox apache 2.0.28 jar set in library, with compatible jdk.
// Project incomplete: Need to update to scraper that updated automatically, from text notifications.
// Using bot to sign into Icloud and download desired number pdf, updated everyday. Final Obj; Instant.
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Scanner;

public class TotalSpendingsCalculator {
    private static double totalSpendings = 0.0;
    private static double bankAccount = 0.0;
    private static double consoleAdditions = 0.0;
    private static double consoleSubtractions = 0.0;

    public static void main(String[] args) {
        String fileName = "report.txt";
        NumberFormat numberFormat = NumberFormat.getInstance();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("incoming wire transfer") || line.contains("Zelle(R):")) {
                    String[] parts = line.split("\\$");
                    if (parts.length >= 2) {
                        String amountStr = parts[1].split(" ")[0].replace(",", "");
                        try {
                            double amount = numberFormat.parse(amountStr).doubleValue();
                            bankAccount += amount;
                        } catch (ParseException e) {
                            System.err.println("Invalid amount format: " + amountStr);
                        }
                    }
                } else if (line.contains("debit card")) {
                    String[] parts = line.split("\\$");
                    if (parts.length >= 2) {
                        String amountStr = parts[1].split(" ")[0].replace(",", "");
                        try {
                            double amount = numberFormat.parse(amountStr).doubleValue();
                            totalSpendings += amount;
                        } catch (ParseException e) {
                            System.err.println("Invalid amount format: " + amountStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculates Difference and prints out final
        double whatYouHave = bankAccount - totalSpendings;

        System.out.println("Total spendings: $" + totalSpendings);
        System.out.println("Bank account balance: $" + bankAccount);
        System.out.println("What you have: $" + whatYouHave);

        // add & subtract operations
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter 'add' or 'subtract' followed by the amount (e.g., 'add 100.0'): ");
            System.out.println("    Type 'Done' for summary");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("done")) {
                whatYouHave += consoleAdditions - consoleSubtractions;

                System.out.println("Final Total spendings: $" + totalSpendings);
                System.out.println("Final Bank account balance: $" + bankAccount);
                System.out.println("Adjustment Additions: $" + consoleAdditions);
                System.out.println("Adjustment Subtractions: $" + consoleSubtractions);
                System.out.println("Final What you have: $" + whatYouHave);
                break;
            }
            String[] parts = input.split(" ");
            if (parts.length == 2) {
                String operation = parts[0];
                double amount = Double.parseDouble(parts[1]);

                // Perform add/subtract based on user input
                if (operation.equalsIgnoreCase("add")) {
                    consoleAdditions += amount;
                } else if (operation.equalsIgnoreCase("subtract")) {
                    consoleSubtractions += amount;
                } else {
                    System.out.println("Invalid operation!");
                    continue;
                }

                System.out.println("Updated What you have: $" + (whatYouHave + consoleAdditions - consoleSubtractions));
            } else {
                System.out.println("Invalid input!");
            }
        }
    }
}
