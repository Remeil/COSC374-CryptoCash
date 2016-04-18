import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/*
 * Bank.java
 * Author(s): Edward Snyr
 * 
 * The Bank class stores the keys used for the RSA encryption, verifies
 * money orders, signs money orders, and verifies merchant orders.
 */
public class Bank {
	public static long publicKey = 25486319;
	private static long privateKey = 53731 * 487589;
	public static long modulus = 1075897 * 24781;
	public final static String UNIQUENESS_STRINGS_DIRECTORY = "uniquenessStrings/";
	private static Random rand = new Random();

	private Bank() {

	}

	/*
	 * Seeds the random number generator.
	 */
	public static void seedPrng(long seed) {
		rand.setSeed(seed);
	}

	/*
	 * Randomly selects a money order not to open.
	 */
	public static long selectUnopenedMoneyOrder(int maximum) {
		return (long)rand.nextInt(maximum) + 1;
	}

	/*
	 * First unblinds all but one money order, then checks if the value for each
	 * is the same. Returns true if there are no issues, false otherwise. Creates
	 * a signed money order file if it is ruled to be valid.
	 */
	public static boolean verifyAndSignMoneyOrders(int unopenedOrder, long givenKey) throws IOException {
		
		Customer.unblindAllButOneMoneyOrder(unopenedOrder);
		
		File[] moneyOrders = new File(Customer.UNBLINDED_MONEY_ORDER_DIRECTORY).listFiles();
		long base = -1;
		
		for (File moneyOrder : moneyOrders) {
			if(moneyOrder.getName() == (unopenedOrder + "MO.txt"))
				continue;
			
			List<String> moneyOrderLines = Files.readAllLines(Paths.get(moneyOrder.getAbsolutePath()));
			
			//Line 2 is the amount
			long amount = (long) (Double.parseDouble(moneyOrderLines.get(1)) * 100);
			
			if(base == -1)
				base = amount;
			if(base != amount)
				return false;
		
		}
		
		List<String> signingLines = Files.readAllLines(Paths.get(moneyOrders[unopenedOrder].getAbsolutePath()));
		String outputSigned = "";
		
		//Line 1 is the uniqueness string
		long uniquenessString = Long.parseLong(signingLines.get(0));
		outputSigned += ((uniquenessString ^ Bank.privateKey) % Bank.modulus) + "\r\n";
		
		//Line 2 is the amount
		long amount = (long) (Double.parseDouble(signingLines.get(1)) * 100);
		outputSigned += ((amount ^ Bank.privateKey) % Bank.modulus) / 100.0 + "\r\n";
		
		//Line 3+ are the identity strings
		for (int i = 2; i < signingLines.size(); i++) {
			String[] identityStrings = signingLines.get(i).split(" ");
			
			for (String identityString : identityStrings) {
				long idString = Long.parseLong(identityString);
				outputSigned += ((idString ^ Bank.privateKey) % Bank.modulus) + " ";
			}
			
			outputSigned += "\r\n";
		}
		
		Files.write(Paths.get(Customer.SIGNED_MONEY_ORDER_DIRECTORY + moneyOrders[unopenedOrder].getName()), outputSigned.getBytes());
		
		return true;
	}

	/*
	 * Verifies the money order by making sure that the uniqueness string for the
	 * order has not been used yet.
	 */
	public static boolean verifyMerchantMoneyOrder() throws IOException{
		File[] uniquenessStrings = new File(UNIQUENESS_STRINGS_DIRECTORY).listFiles();
		File[] merchantMoneyOrders = new File(Customer.UNBLINDED_SIGNED_MONEY_ORDER_DIRECTORY).listFiles();
		String output = "";
		
		for (File uStrings : uniquenessStrings) {
			List<String> uniquenessStringLines = Files.readAllLines(Paths.get(uStrings.getAbsolutePath()));
			for(String line : uniquenessStringLines) {
				output += line;
			}
			for(File mOrders : merchantMoneyOrders) {
				List<String> merchantOrder = Files.readAllLines(Paths.get(mOrders.getAbsolutePath()));
				output += merchantOrder.get(1);
				for(String line : uniquenessStringLines) {
					if((line != merchantOrder.get(1)))
						return false;
				}
				
			}
			
		}
		
		Files.write(Paths.get(UNIQUENESS_STRINGS_DIRECTORY + "UniquenessStrings.txt"), output.getBytes());
		
		return true;
	}
}
