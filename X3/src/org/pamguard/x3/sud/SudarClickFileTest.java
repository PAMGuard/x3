package org.pamguard.x3.sud;

import java.io.File;

public class SudarClickFileTest {
	
	/**
	 * Test decompression on a file using a SudAudioInputStream. 
	 * @param args - input args are null. 
	 */
	public static void main(String[] args) {

		long time0 = System.currentTimeMillis();
		String filePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/sud_decompression/clickdet_example/7140.221020162018.sud";
		SudAudioInputStream sudAudioInputStream = null;
		
		SudParams sudParams = new SudParams(); 
		sudParams.saveFolder = null;
		sudParams.saveWav = true; 

		boolean verbose = false; // true to print more stuff.
		
		try {
			
			sudAudioInputStream = SudAudioInputStream.openInputStream(new File(filePath), sudParams, verbose);
			
			long time1 = System.currentTimeMillis();
			
			System.out.println("Time to create file map: " + (time1 - time0));

			System.out.println("sudAudioInputStream.available() 1: " + sudAudioInputStream.available());


			sudAudioInputStream.skip(500000 * 0);
			
			long time2 = System.currentTimeMillis();
			
			System.out.println("Time to skip 1: " + (time2 - time1));

			
			time1 = System.currentTimeMillis();
			
			sudAudioInputStream.skip(500000 * 0);

			time2 = System.currentTimeMillis();
			
			System.out.println("Time to skip 2: " + (time2 - time1));

			System.out.println("sudAudioInputStream.available() 2: " + sudAudioInputStream.available());

			while (sudAudioInputStream.available() > 0) {
				sudAudioInputStream.read();
			}
			 
			sudAudioInputStream.close();

		} catch (Exception e) {

			e.printStackTrace();
		}
		long time3 = System.currentTimeMillis();

		System.out.println("Total processing time: " + (time3 - time0));
	}

}
