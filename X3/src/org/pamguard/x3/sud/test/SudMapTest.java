package org.pamguard.x3.sud.test;

import java.io.File;
import java.io.IOException;

import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileMap;

/**
 * Test loading a sudx map file. 
 */
public class SudMapTest {
	
	
	public static void main(String[] args) {
		
//		String file= "/Volumes/SMRU_PAM/2018_28_Gill_nets/4chan/20240628_AK660 H2/5434/5434.240629224734.sudx";
		String file = "/Volumes/SMRU_PAM/2018_28_Gill_nets/4chan/20240628_AK660 H2/5434/5434.240628133610.sudx";
		
		try {
			SudFileMap sudFileMap = SudAudioInputStream.loadSudMap(new File(file));
			
			System.out.println("---Opened sud file map----");
			System.out.println("Total samples: "  + sudFileMap.totalSamples);


		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}

}
