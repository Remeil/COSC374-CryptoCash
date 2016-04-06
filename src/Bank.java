/*
 * Bank.java
 * Author(s): 
 * 
 * A file that does stuff.
 */
public class Bank {
	public long publicKey = 1;
	private long privateKey = 1;
	public long modulus = 1;

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

	public static boolean verifyMerchantMoneyOrder() {
		return true;
	}
}
