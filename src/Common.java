
public class Common {
	private Common() {
		
	}
	
	//XOR each value together.
	public static long hash(long randomValue1, long randomValue2, long identity) {
		return randomValue1 ^ randomValue2 ^ identity;
	}
}
