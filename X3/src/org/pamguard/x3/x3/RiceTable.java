package org.pamguard.x3.x3;

/**
 * Rice table functions and lookup tables for both reading and writing 
 * compressed rice data. 
 * @author Doug
 *
 */
public class RiceTable {


	public int riceOrder;
	public int riceRange;
	public int[] riceCodes;
	public int midPoint;
	public int[] riceBits;

	/**
	 * Create a rice table with given order and range of values
	 * @param riceOrder Order of the Rice table
	 * @param riceRange maximum / minimum number to include in the table. 
	 */
	public RiceTable(int riceOrder, int riceRange) {
		super();
		this.riceOrder = riceOrder;
		this.riceRange = riceRange;
		int nCodes = riceRange * 2 + 1;
		midPoint = riceRange;
		riceCodes = new int[nCodes];
		riceBits = new int[nCodes];
		int riceMask = 1<<riceOrder;
		int endCode = 1;
		int nBits = riceOrder+1;
		riceCodes[midPoint] = (short) riceMask;
		riceBits[midPoint] = nBits;
		for (int i = 1; i <= riceRange; i++) {
			if (endCode == riceMask) {
				endCode = 0;
				nBits++;
			}
			riceCodes[midPoint-i] = (short) (riceMask | endCode++);
			riceBits[midPoint-i] = (short) nBits;
			if (endCode == riceMask) {
				endCode = 0;
				nBits++;
			}
			riceCodes[midPoint+i] = (short) (riceMask | endCode++);
			riceBits[midPoint+i] = (short) nBits;
		}
		int x = riceCodes[0];
	}

	/**
	 * Create an inverse RICE lookup table up to a certain number. The table 
	 * length will be 2*whereTo + 1 in the format 0,-1,1,-2,2,-3,3, etc...<br>
	 * A single table can be used with all rice codes. 
	 * @param whereTo highest number to include in the table
	 * @return Inverse RICE lookup table. 
	 */
	public static short[] makeInverseRice(int whereTo) {
		short[] irt = new short[whereTo*2+1];
		int ind = 0;
		for (int i = 1; i <= whereTo; i++) {
			irt[++ind] = (short) -i;
			irt[++ind] = (short) i;
		}
		return irt;
	}

}
