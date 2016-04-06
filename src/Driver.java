import java.util.*;

/*
 * Driver.java
 * Author(s): 
 * 
 * A file that does stuff.
 */
public class Driver {
	public static void main(String[] args) {
		boolean done = false;
		Scanner scan = new Scanner(System.in);
		int lastNumberOfOrders = -1;
		String lastIdentityString = "";

		do {
			//Menu stuff
			System.out.println("*** Main Menu ***");
			System.out.println("1) Customer: Create money orders");
			System.out.println("2) Customer: Blind money orders");
			System.out.println("3) Bank: Verify+Sign money order");
			System.out.println("4) Customer: Unblind money order");
			System.out.println("5) Merchant: Verify bank signature");
			System.out.println("6) Merchant: Request identity halves");
			System.out.println("7) Customer: Reveal identity halves");
			System.out.println("8) Bank: Verify valid payment");
			System.out.println("9) Automatic: Go through the entire process automatically.");
			System.out.println("10) Seed PRNGs");
			System.out.println("11) Exit");

			int input = -1;
			
			do {
				try {
					System.out.print("Enter Selection: ");
					input = scan.nextInt();
				}
				catch (InputMismatchException e) {}
			} while (input < 1 || input > 11);

			switch (input) {
				//Create money orders
				case 1: {
					double amount = -1;
					int ordersToCreate = -1;
					long identity = -1;

					do {
						try {
							System.out.print("Enter the amount of cash for the money order: ");
							amount = scan.nextDouble();
						}
						catch (InputMismatchException e) {}
					} while (amount < 0);

					do {
						try {
							System.out.print("Enter the number of orders to create: ");
							ordersToCreate = scan.nextInt();
						}
						catch (InputMismatchException e) {}
					} while (ordersToCreate < 0);

					do {
						try {
							System.out.print("Enter your identifying number: ");
							identity = scan.nextLong();
						}
						catch (InputMismatchException e) {}
					} while (identity < 0);

					System.out.println("Creating money orders...");
					Customer.createMoneyOrders(amount, ordersToCreate, identity);
					System.out.println("Done creating money orders.");
					lastNumberOfOrders = ordersToCreate;
					break;
				}
				//Blind money orders
				case 2: {
					System.out.println("Blinding money orders...");
					Customer.blindMoneyOrders();
					System.out.println("Done blinding money orders.");
					break;
				}
				//Verify and sign money orders
				case 3: {
					int orderToSign = -2;
					boolean confirmed = true;;
					do {
						try {
							confirmed = true;
							System.out.print("Enter the number of the money order to sign (-1 for random): ");
							orderToSign = scan.nextInt();
							
							if (orderToSign > lastNumberOfOrders) {
								System.out.print("The order to sign appears to fall out of the range of possible orders. Are you sure you want to choose this order (Y/N): ");
								char response = scan.next().toUpperCase().charAt(0);
								confirmed = response == 'Y';
							}
						}
						catch (InputMismatchException e) {}
					} while (orderToSign < -1 && confirmed);
					
					System.out.println("Verifying and signing money order " + orderToSign + "...");
					if (Bank.verifyAndSignMoneyOrders(orderToSign)) {
						System.out.println("Money order signed.");
					} else {
						System.out.println("ERROR: Invalid money order.");
					}
					break;
				}
				//Unblind money orders
				case 4: {
					System.out.println("Unblinding money order...");
					Customer.unblindMoneyOrder();
					System.out.println("Money order unblinded.");
					break;
				}
				//Verify bank signature
				case 5: {
					System.out.println("Verifying bank signature...");
					if (Merchant.verifyBankSignature(Bank.publicKey, Bank.modulus)) {
						System.out.println("Bank signature appears to be valid.");
					}
					else {
						System.out.println("ERROR: Bank signature appears to be invalid");
					}
					break;
				}
				//Request identity halves
				case 6: {
					System.out.println("Generating identity reveal string...");
					lastIdentityString = Merchant.generateRevealIdentityString(lastNumberOfOrders);
					System.out.println("Generated string: " + lastIdentityString);
					break;
				}
				//Reveal identity halves
				case 7: {
					System.out.println("Revealing identity halves...");
					Customer.revealIdentityStringHalves(lastIdentityString);
					System.out.println("Identity halves revealed.");
					break;
				}
				//Verify payment
				case 8: {
					System.out.println("Verifying payment...");
					if (Bank.verifyMerchantMoneyOrder()) {
						System.out.println("Payment verified.");
					}
					else {
						System.out.println("ERROR: Payment is invalid");
					}
					break;
				}
				//Automatic
				case 9: {
					break;
				}
				//Seed PRNGs
				case 10: {

					break;
				}
				//Exit
				case 11: {
					done = true;
					break;
				}
				default: {
					throw new AssertionError("Impossible state");
				}
			}
			
			System.out.println();
		} while (!done);
	}
}
