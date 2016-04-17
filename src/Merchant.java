import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/*
 * Merchant.java
 * Author(s): 
 * 
 * A file that does stuff.
 */
public class Merchant {
	
	private static Random rand = new Random();
	
	private Merchant() {

	}

	public static void seedPrng(long seed) {
		rand.setSeed(seed);
	}

	public static boolean verifyBankSignature(long givenKey) throws IOException {
		File[] moneyOrders = new File(Customer.SIGNED_MONEY_ORDER_DIRECTORY).listFiles();
		long multiplier = Common.powermod(givenKey, Bank.publicKey, Bank.modulus);
		
		for (File moneyOrder : moneyOrders) {
			List<String> moneyOrderLines = Files.readAllLines(Paths.get(moneyOrder.getAbsolutePath()));
			String output = "";
			
			//Line 1 is the uniqueness string
			long uniquenessString = Long.parseLong(moneyOrderLines.get(0));
			output += (Common.powermod(multiplier, -1, Bank.modulus) * uniquenessString) % Bank.modulus + "\r\n";
			
			//Line 2 is the amount
			long amount = (long) (Double.parseDouble(moneyOrderLines.get(1)) * 100);
			output += ((Common.powermod(multiplier, -1, Bank.modulus) * amount) % Bank.modulus) / 100.0 + "\r\n";
			
			//Line 3+ are the identity strings
			for (int i = 2; i < moneyOrderLines.size(); i++) {
				String[] identityStrings = moneyOrderLines.get(i).split(" ");
				
				for (String identityString : identityStrings) {
					long idString = Long.parseLong(identityString);
					output += (Common.powermod(multiplier, -1, Bank.modulus) * idString) % Bank.modulus + " ";
				}
				
				output += "\r\n";
			}
			
			//Files.write(Paths.get(blindedMoneyOrderDirectory + moneyOrder.getName()), output.getBytes());
		}
		return true;
	}

	public static String generateRevealIdentityString(long stringLength) {
		String randReveal = "";
		for(int i = 0; i < stringLength; i++) {
			if(rand.nextBoolean())
				randReveal += "1";
			else
				randReveal += "0";
		}
		return randReveal;
	}

	public static void storeRevealedIdentityStrings(List<Long> identityStrings) {

	}
}
