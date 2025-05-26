package org.pamguard.x3.sud.test;

import java.io.File;
import java.io.IOException;

import org.pamguard.x3.sud.ISudarDataHandler;
import org.pamguard.x3.sud.ISudarKey;
import org.pamguard.x3.sud.SudFileExpander;
import org.pamguard.x3.sud.SudParams;

/**
 * Test that the sud files decompresses mnagetometers correctly.
 */
public class SudMagenetomterTest {
	
	public static void main(String[] args) {
		System.out.println("Test .sud file decompression for magnetometers");

		String sudFilePath = "/Volumes/SMRU_PAM/2024-26_SAMBAH/lander_acoustic_check/Test_Lander1_deployment_6_nov_2024/8614.241106080004.sud";
		
		
		SudParams sudParams = new SudParams();
		sudParams.setVerbose(false);
		sudParams.setFileSave(false, false, false, false, true);
		sudParams.setSudEnable(true, true, true, true);
		sudParams.setVerbose(false);
		
		
		//should or should we not save the wav file?
		boolean saveWav = sudParams.isFileSave(new ISudarKey(ISudarDataHandler.WAV_FTYPE, "wav")); 
		

		System.out.println("SAVE WAV FILES: " + saveWav); 

		SudFileExpander sudFileExpander = new SudFileExpander(new File(sudFilePath), sudParams);

		try {
			sudFileExpander.processFile();
			sudFileExpander.closeFileExpander();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		
		System.out.println("Magnetometer data processed successfully.");
	}
	
	

}
