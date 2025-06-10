package org.pamguard.x3.sud.test;

import java.io.File;
import java.io.IOException;

import org.pamguard.x3.sud.SudFileExpander;
import org.pamguard.x3.sud.SudParams;

/**
 * Simple test that just expands a sud file
 * @author Jamie Macaulay
 *
 */
public class ExpandSudFileTest  {
	
	public static void main(String[] args) {
		String sudfilePath = "F:/Zeropad_test/134250533.241227185742.sud";
		
		SudParams sudParams = new SudParams();
		sudParams.setVerbose(false);
		sudParams.setFileSave(true, true, true, true, true);
		sudParams.setSudEnable(true, true, true, true);
		
		sudParams.zeroPad = true;
	

		System.out.println("Start expanding file: "); 
		SudFileExpander sudFileExpander = new SudFileExpander(new File(sudfilePath), sudParams); 

		try {
			sudFileExpander.processFile();
			sudFileExpander.closeFileExpander();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Finish expanding file: "); 

		
	}

}
