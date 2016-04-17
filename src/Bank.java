import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/*
 * Bank.java
 * Author(s): 
 * 
 * A file that does stuff.
 */
public class Bank {
	public static long publicKey = 1;
	private static long privateKey = 1;
	public static long modulus = 1;
	private static Random rand = new Random();

	private Bank() {

	}

	public static void seedPrng(long seed) {
		rand.setSeed(seed);
	}

	public static long selectUnopenedMoneyOrder(int maximum) {
		return (long)rand.nextInt(maximum) + 1;
	}

	public static boolean verifyAndSignMoneyOrders(int unopenedOrder, long givenKey) throws IOException {
		File[] moneyOrders = new File(Customer.BLINDED_MONEY_ORDER_DIRECTORY).listFiles();
		long base = -1;
		long multiplier = Common.powermod(givenKey, Bank.publicKey, Bank.modulus);
		
		for (File moneyOrder : moneyOrders) {
			if(moneyOrder.getName() == (unopenedOrder + "MO.txt"))
				continue;
			
			List<String> moneyOrderLines = Files.readAllLines(Paths.get(moneyOrder.getAbsolutePath()));
			
			//Line 2 is the amount
			long amount = (long) (Double.parseDouble(moneyOrderLines.get(1)) * 100);
			//Decrypt amount
			amount = (Common.powermod(multiplier, -1, Bank.modulus) * amount) % Bank.modulus;
			
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

	//probably need to return more than a boolean here, to tell between
	//merchant cheating and customer cheating
	public static boolean verifyMerchantMoneyOrder() {
		return true;
	}
}
