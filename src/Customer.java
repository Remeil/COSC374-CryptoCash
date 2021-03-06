import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

/*
 * Customer.java
 * Author(s): Johnathan Stiles
 * 
 * This class provides all the money order operations from the customer.
 */
public class Customer {
	public final static String MONEY_ORDER_DIRECTORY = "moneyOrders/";
	public final static String BLINDED_MONEY_ORDER_DIRECTORY = "blindedMoneyOrders/";
	public final static String UNBLINDED_MONEY_ORDER_DIRECTORY = "unblindedMoneyOrders/";
	public final static String SIGNED_MONEY_ORDER_DIRECTORY = "signedMoneyOrder/";
	public final static String UNBLINDED_SIGNED_MONEY_ORDER_DIRECTORY = "unblindedSignedMoneyOrder/";
	public final static String RANDOM_NUMBER_DIRECTORY = "savedRandomNumbers/";
	private static Random rand = new Random();
	private static long lastSecret = 0;

	private Customer() {

	}

	//Set the seed
	public static void seedPrng(long seed) {
		rand.setSeed(seed);
	}

	
	//Create ordersToCreate money orders, for the given amount using the given identity.
	public static void createMoneyOrders(double amount, int ordersToCreate, long identity) throws IOException {
		clearAllFiles();
		
		for (int i = 1; i <= ordersToCreate; i++) {
			String fileContents = "";

			//unique identifier
			fileContents += (rand.nextLong() % Bank.modulus) + "\r\n";
			//amount
			fileContents += amount + "\r\n";
			//identity strings
			fileContents += generateIdentityStrings(ordersToCreate, identity, i + "MO.txt");

			Path filePath = Paths.get(MONEY_ORDER_DIRECTORY + i + "MO.txt");
			Files.write(filePath, fileContents.getBytes());
		}
	}

	//Empty working directories
	private static void clearAllFiles() throws IOException {
		File moneyOrderDirectory = new File(MONEY_ORDER_DIRECTORY);
		File blindedMoneyOrderDirectory = new File(BLINDED_MONEY_ORDER_DIRECTORY);
		File unblindedMoneyOrderDirectory = new File(UNBLINDED_MONEY_ORDER_DIRECTORY);
		File signedMoneyOrderDirectory = new File(SIGNED_MONEY_ORDER_DIRECTORY);
		File unblindedSignedMoneyOrderDirectory = new File(UNBLINDED_SIGNED_MONEY_ORDER_DIRECTORY);
		File uniquenessStringsDirectory = new File(Bank.UNIQUENESS_STRINGS_DIRECTORY);
		
		clearFiles(moneyOrderDirectory);
		clearFiles(blindedMoneyOrderDirectory);
		clearFiles(unblindedMoneyOrderDirectory);
		clearFiles(signedMoneyOrderDirectory);
		clearFiles(unblindedSignedMoneyOrderDirectory);
		clearFiles(uniquenessStringsDirectory);
	}

	//Empty a single directory
	private static void clearFiles(File directory) throws IOException {
		if (directory.exists()) {
			for (File file : directory.listFiles()) {
				file.delete();
			}
		}
		else {
			directory.mkdir();
		}
	}

	//Create and generate ordersToCreate identity strings, based of the given identity, and save it in the given randomNumberFileName
	private static String generateIdentityStrings(int ordersToCreate, long identity, String randomNumberFileName) throws IOException {
		String result = "";
		String randomNumberFileResult = "";
		
		for (int i = 0; i < ordersToCreate; i++) {
			//generate random string same length as message 
			long rightSecret = (rand.nextLong() % Bank.modulus);
			//generate secret string based off identity and message
			long leftSecret = rightSecret ^ identity;

			//generate two random numbers for left half
			long randomLeft1 = (rand.nextLong() % Bank.modulus);
			long randomLeft2 = (rand.nextLong() % Bank.modulus);

			long leftHash = Common.hash(randomLeft1, randomLeft2, leftSecret);

			//generate two random numbers for right half
			long randomRight1 = (rand.nextLong() % Bank.modulus);
			long randomRight2 = (rand.nextLong() % Bank.modulus);

			long rightHash = Common.hash(randomRight1, randomRight2, rightSecret);

			result += leftHash + " " + randomLeft2 + " " + rightHash + " " + randomRight2 + "\r\n";
			randomNumberFileResult += leftHash + " " + leftSecret + " " + randomLeft1 + " " + randomLeft2 + "\r\n";
			randomNumberFileResult += rightHash + " " + rightSecret + " " + randomRight1 + " " + randomRight2 + "\r\n";
		}
		
		Files.write(Paths.get(RANDOM_NUMBER_DIRECTORY + randomNumberFileName), randomNumberFileResult.getBytes());
		return result;
	}

	//Blind all money orders, and place them in a new folder for processing.
	public static long blindMoneyOrders() throws IOException {
		File[] moneyOrders = new File(MONEY_ORDER_DIRECTORY).listFiles();
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
			
			Files.write(Paths.get(BLINDED_MONEY_ORDER_DIRECTORY + moneyOrder.getName()), output.getBytes());
		}
		
		return lastSecret;
	}
	
	//Unblind all but one money order for the bank to inspect
	public static void unblindAllButOneMoneyOrder(long moneyOrderToNotUnblind) throws IOException {
		File[] moneyOrders = new File(BLINDED_MONEY_ORDER_DIRECTORY).listFiles();
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
			
			Files.write(Paths.get(UNBLINDED_MONEY_ORDER_DIRECTORY + moneyOrder.getName()), output.getBytes());
		}
	}

	//Unblind a signed money order so we can use it.
	public static void unblindMoneyOrder() throws IOException {
		File[] moneyOrders = new File(SIGNED_MONEY_ORDER_DIRECTORY).listFiles(); //should only ever be one file
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
			
			Files.write(Paths.get(UNBLINDED_SIGNED_MONEY_ORDER_DIRECTORY + moneyOrder.getName()), output.getBytes());
		}
	}

	//Reveal identity string halves of our money order, based on a inputted bit string.
	public static List<RevealedIdentityStrings> revealIdentityStringHalves(String halvesToReveal) throws IOException {
		int length = halvesToReveal.length();
		List<RevealedIdentityStrings> list = new ArrayList<RevealedIdentityStrings>();
		
		File[] moneyOrders = new File(UNBLINDED_SIGNED_MONEY_ORDER_DIRECTORY).listFiles(); //should only ever be one file
		String moneyOrderName = moneyOrders[0].getName();
		
		List<String> randomNumberLines = Files.readAllLines(Paths.get(RANDOM_NUMBER_DIRECTORY + moneyOrderName));
		
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

	//Generate an identity string object from the inputted string.
	private static RevealedIdentityStrings createIdentityStringObject(String[] individualIdentityStrings) {
		RevealedIdentityStrings identityStrings = new RevealedIdentityStrings();
		
		identityStrings.hashResult = Long.parseLong(individualIdentityStrings[0]);
		identityStrings.secret = Long.parseLong(individualIdentityStrings[1]);
		identityStrings.randomValue1 = Long.parseLong(individualIdentityStrings[2]);
		identityStrings.randomValue2 = Long.parseLong(individualIdentityStrings[3]);
		
		return identityStrings;
	}
}
