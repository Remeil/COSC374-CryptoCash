import java.math.BigInteger;

/*
 * Common.java
 * Author(s): Johnathan Stiles, Edward Snyr
 * 
 * Provides common methods used in several classes.
 */

public class Common {
	private Common() {
		
	}
	
	//XOR each value together.
	public static long hash(long randomValue1, long randomValue2, long identity) {
		return randomValue1 ^ randomValue2 ^ identity;
	}
	
	public static long powermod(long base, long exponent, long modulus) {
		BigInteger bigBase, bigExponent, bigModulus;
		bigBase = new BigInteger( (new Long(base)).toString() );
		bigExponent = new BigInteger( (new Long(exponent)).toString() );
		bigModulus = new BigInteger( (new Long(modulus)).toString() );
		
		BigInteger result = bigBase.modPow(bigExponent, bigModulus);
		
		return result.longValue();
	}
}
