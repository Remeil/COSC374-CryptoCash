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
			long rsi = rand.nextLong();
			//generate secret string based off identity and message
			long ssi = rsi ^ identity;

			//generate two random numbers for left half
			long rc1l = rand.nextLong();
			long rc2l = rand.nextLong();

			long leftHash = Common.hash(rc1l, rc2l, ssi);

			//generate two random numbers for right half
			long rc1r = rand.nextLong();
			long rc2r = rand.nextLong();

			long rightHash = Common.hash(rc1r, rc2r, rsi);

			result += leftHash + " " + rc2l + " " + rightHash + " " + rc2r + "\r\n";
		}
		
		return result;
	}

	public static void blindMoneyOrders() {

	}

	public static void unblindMoneyOrder() {

	}

	public static List<Long> revealIdentityStringHalves(String halvesToReveal) {
		return new LinkedList<Long>();
	}
}
