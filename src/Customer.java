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
	private static String unblindedMoneyOrderDirectory = "unblindedMoneyOrders/";
	private static String signedMoneyOrderDirectory = "signedMoneyOrder/";
	private static String unblindedSignedMoneyOrderDirectory = "unblindedSignedMoneyOrder/";
	private static String randomNumberDirectory = "savedRandomNumbers/";
	private static Random rand = new Random();
	private static long lastSecret = 0;

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
			fileContents += generateIdentityStrings(ordersToCreate, identity, i + "MO.txt");

			Path filePath = Paths.get(moneyOrderDirectory + i + "MO.txt");
			Files.write(filePath, fileContents.getBytes());
		}
	}

	private static String generateIdentityStrings(int ordersToCreate, long identity, String randomNumberFileName) throws IOException {
		String result = "";
		String randomNumberFileResult = "";
		
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
			randomNumberFileResult += leftHash + " " + leftSecret + " " + randomLeft1 + " " + randomLeft2 + "\r\n";
			randomNumberFileResult += rightHash + " " + rightSecret + " " + randomRight1 + " " + randomRight2 + "\r\n";
		}
		
		Files.write(Paths.get(randomNumberDirectory + randomNumberFileName), randomNumberFileResult.getBytes());
		return result;
	}

	public static void blindMoneyOrders() throws IOException {
		File[] moneyOrders = new File(moneyOrderDirectory).listFiles();
		lastSecret = rand.nextLong();
		long multiplier = Common.powermod(lastSecret, Bank.publicKey, Bank.modulus);
		
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
	
	public static void unblindAllButOneMoneyOrder(long moneyOrderToNotUnblind) throws IOException {
		File[] moneyOrders = new File(blindedMoneyOrderDirectory).listFiles();
		long multiplier = Common.powermod(lastSecret, -1, Bank.modulus);
		String fileNameToNotUnblind = moneyOrderToNotUnblind + "MO.txt";
		
		for (File moneyOrder : moneyOrders) {
			//Skip over the file that we don't want to unblind.
			if (moneyOrder.getName().equals(fileNameToNotUnblind)) {
				continue;
			}
			
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
			
			Files.write(Paths.get(unblindedMoneyOrderDirectory + moneyOrder.getName()), output.getBytes());
		}
	}

	public static void unblindMoneyOrder() throws IOException {
		File[] moneyOrders = new File(signedMoneyOrderDirectory).listFiles(); //should only ever be one file
		long multiplier = Common.powermod(lastSecret, -1, Bank.modulus);
		
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
			
			Files.write(Paths.get(unblindedSignedMoneyOrderDirectory + moneyOrder.getName()), output.getBytes());
		}
	}

	//0 reveals left half, 1 reveals right half
	public static List<RevealedIdentityStrings> revealIdentityStringHalves(String halvesToReveal) throws IOException {
		int length = halvesToReveal.length();
		List<RevealedIdentityStrings> list = new ArrayList<RevealedIdentityStrings>();
		
		File[] moneyOrders = new File(unblindedSignedMoneyOrderDirectory).listFiles(); //should only ever be one file
		String moneyOrderName = moneyOrders[0].getName();
		
		List<String> randomNumberLines = Files.readAllLines(Paths.get(randomNumberDirectory + moneyOrderName));
		
		for (int i = 0; i < length; i++) {
			switch (halvesToReveal.charAt(i)) {
				case '0':
				{
					String[] individualIdentityStrings = randomNumberLines.get(2 * i).split(" ");
					RevealedIdentityStrings identityStrings = createIdentityStringObject(individualIdentityStrings);
					list.add(identityStrings);
				}
				case '1':
				{
					String[] individualIdentityStrings = randomNumberLines.get(2 * i + 1).split(" ");
					RevealedIdentityStrings identityStrings = createIdentityStringObject(individualIdentityStrings);
					list.add(identityStrings);
				}
				default:
				{
					throw new IllegalArgumentException("halvesToReveal must contain only ones and zeroes.");
				}
			}
		}
		
		return list;
	}

	private static RevealedIdentityStrings createIdentityStringObject(String[] individualIdentityStrings) {
		RevealedIdentityStrings identityStrings = new RevealedIdentityStrings();
		
		identityStrings.hashResult = Long.parseLong(individualIdentityStrings[0]);
		identityStrings.secret = Long.parseLong(individualIdentityStrings[1]);
		identityStrings.randomValue1 = Long.parseLong(individualIdentityStrings[2]);
		identityStrings.randomValue2 = Long.parseLong(individualIdentityStrings[3]);
		
		return identityStrings;
	}
}
