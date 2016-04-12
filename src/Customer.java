import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/*
 * Customer.java
 * Author(s): 
 * 
 * A file that does stuff.
 */
public class Customer {
	private static String moneyOrderDirectory = "/moneyOrders/";
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
			fileContents += rand.nextLong() + "\n";
			//amount
			fileContents += amount + "\n";
			//identity strings
			fileContents += generateIdentityStrings();
			
			Path filePath = Paths.get(i + "MO.txt");
			Files.write(filePath, fileContents.getBytes());
		}
	}

	private static String generateIdentityStrings() {
		return null;
	}

	public static void blindMoneyOrders() {

	}

	public static void unblindMoneyOrder() {

	}

	public static List<Long> revealIdentityStringHalves(String halvesToReveal) {
		return new LinkedList<Long>();
	}
}
