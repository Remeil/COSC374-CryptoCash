import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/*
 * Customer.java
 * Author(s): 
 * 
 * A file that does stuff.
 */
public class Customer {
	private static String moneyOrderDirectory = "moneyOrders/";
	private static String blindedMoneyOrderDirectory = "blindedMoneyOrders/";
	private static String signedMoneyOrderDirectory = "signedMoneyOrder/";
	private static Random rand = new Random();

	private Customer() {

	}

	public static void seedPrng(long seed) {
		rand.setSeed(seed);
	}

	public static void createMoneyOrders(double amount, int ordersToCreate, long identity) throws IOException {
		for (int i = 1; i <= ordersToCreate; i++) {
			String fileContents = "";

			//unique identifier
			fileContents += rand.nextLong() + "\r\n";
			//amount
			fileContents += amount + "\r\n";
			//identity strings
			fileContents += generateIdentityStrings(ordersToCreate, identity);

			Path filePath = Paths.get(moneyOrderDirectory + i + "MO.txt");
			Files.write(filePath, fileContents.getBytes());
		}
	}

	private static String generateIdentityStrings(int ordersToCreate, long identity) {
		String result = "";
		
		for (int i = 0; i < ordersToCreate; i++) {
			//generate random string same length as message 
			long rightSecret = rand.nextLong();
			//generate secret string based off identity and message
			long leftSecret = rightSecret ^ identity;

			//generate two random numbers for left half
			long randomLeft1 = rand.nextLong();
			long randomLeft2 = rand.nextLong();

			long leftHash = Common.hash(randomLeft1, randomLeft2, leftSecret);

			//generate two random numbers for right half
			long randomRight1 = rand.nextLong();
			long randomRight2 = rand.nextLong();

			long rightHash = Common.hash(randomRight1, randomRight2, rightSecret);

			result += leftHash + " " + randomLeft2 + " " + rightHash + " " + randomRight2 + "\r\n";
		}
		
		return result;
	}

	public static void blindMoneyOrders() throws IOException {
		File[] moneyOrders = new File(moneyOrderDirectory).listFiles();
		long multiplier = Common.powermod(rand.nextLong(), Bank.publicKey, Bank.modulus);
		
		for (File moneyOrder : moneyOrders) {
			List<String> moneyOrderLines = Files.readAllLines(Paths.get(moneyOrder.getAbsolutePath()));
			String output = "";
			
			//Line 1 is the uniqueness string
			long uniquenessString = Long.parseLong(moneyOrderLines.get(0));
			output += ((uniquenessString * multiplier) % Bank.modulus) + "\r\n";
			
			//Line 2 is the amount
			long amount = (long) (Double.parseDouble(moneyOrderLines.get(1)) * 100);
			output += ((amount * multiplier) % Bank.modulus) / 100.0 + "\r\n";
			
			//Line 3+ are the identity strings
			for (int i = 2; i < moneyOrderLines.size(); i++) {
				String[] identityStrings = moneyOrderLines.get(i).split(" ");
				
				for (String identityString : identityStrings) {
					long idString = Long.parseLong(identityString);
					output += ((idString * multiplier) % Bank.modulus) + " ";
				}
				
				output += "\r\n";
			}
			
			Files.write(Paths.get(blindedMoneyOrderDirectory + moneyOrder.getName()), output.getBytes());
		}
	}

	public static void unblindMoneyOrder() {

	}

	public static List<Long> revealIdentityStringHalves(String halvesToReveal) {
		return new LinkedList<Long>();
	}
}
