package org.pamguard.x3.sud;

import java.io.File;
import java.io.IOException;

/**
 * Test opening a .sud file
 * @author Jamie Macaulay
 *
 */
public class SudarFileTest {
	
	public static void main(String[] args) {
		System.out.println("Hello .sud file decompression");
		
//		String filePath = "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/x3/335564854.180411000003.sud";
		String filePath = "C:\\PAMGuardTest\\SUDFile\\singlechan_exmple\\67411977.171215195605.sud";
		
		SudFileExpander sudFileExpander = new SudFileExpander(new File(filePath)); 
		try {
			sudFileExpander.processFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
