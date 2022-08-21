package hitonoriol.madsand.util;

public class BooleanTally {
	private long counter = 0;
	
	public boolean action() {
		++counter;
		return counter == 0;
	}
	
	public boolean reverseAction() {
		--counter;
		return counter == 0;
	}
	
	public void reset() {
		counter = 0;
	}
	
	public boolean isNeutral() {
		return counter == 0;
	}
}
