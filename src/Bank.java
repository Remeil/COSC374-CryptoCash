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

	private Bank() {

	}

	public static void seedPrng(long seed) {

	}

	public static long selectUnopenedMoneyOrder(int maximum) {
		return 1;
	}

	public static boolean verifyAndSignMoneyOrders(int unopenedOrder) {
		return true;
	}

	//probably need to return more than a boolean here, to tell between
	//merchant cheating and customer cheating
	public static boolean verifyMerchantMoneyOrder() {
		return true;
	}
}
