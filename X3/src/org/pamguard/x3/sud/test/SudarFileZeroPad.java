package org.pamguard.x3.sud.test;

import java.io.File;

import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileExpander;
import org.pamguard.x3.sud.SudFileMap;
import org.pamguard.x3.sud.SudParams;

public class SudarFileZeroPad {

	public static void main(String[] args) {


		//check if zero padding creates a different number of samples.
		String sudfilePath = "/Users/jdjm/Desktop/sud_test_pamguard/sud/5150.250415052938.sud";


		SudParams sudParams = new SudParams();
		sudParams.setVerbose(false);
		sudParams.setFileSave(true, false, false, false);
		sudParams.setSudEnable(true, true, true);

		sudParams.zeroPad = true;


		SudFileExpander sudFileExpander = new SudFileExpander(new File(sudfilePath), sudParams); 

		try {
			SudFileMap map;
			map = SudAudioInputStream.mapSudFile(sudFileExpander, null, false);
			System.out.println("Zero pad samples: " + map.totalSamples);
			
			sudParams.zeroPad= false;
			 sudFileExpander = new SudFileExpander(new File(sudfilePath), sudParams); 
			map = SudAudioInputStream.mapSudFile(sudFileExpander, null, false);
			System.out.println("No zero pad samples: " + map.totalSamples);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


}
