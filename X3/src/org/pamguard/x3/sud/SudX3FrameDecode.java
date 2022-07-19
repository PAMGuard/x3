package org.pamguard.x3.sud;

import java.io.DataInput;

/**
 * 	Decompresses the X3 data in a .sud file. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudX3FrameDecode {
	
	

	short[] RSUFFS = {0,1,3} ;
	
	short[]   IRT = {0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,
		-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,
		-19,19,-20,20,-21,21,-22,22,-23,23,-24,24,-25,25,-26,26};

	
	int BlockDecode(DataInput bufIn,  short[] buf, short last, int count) {
		System.out.println("Ready for block decode:");
		return 0; 
	}
	
	
	
	
}
