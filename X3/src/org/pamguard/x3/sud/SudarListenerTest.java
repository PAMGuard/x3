package org.pamguard.x3.sud;

import java.io.File;


/**
 * Example main class which deomstrates how to listen dfor sud chunks. 
 * @author au671271
 *
 */
public class SudarListenerTest {
	
	/**
	 * Test decompression on a file using a SudAudioInputStream. 
	 * @param args - input args are null. 
	 */
	public static void main(String[] args) {

		long time0 = System.currentTimeMillis();
		String filePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/sud_decompression/clickdet_example/7140.221020162018.sud";
		
		SudParams sudParams = new SudParams(); 
		sudParams.saveFolder = null;
		sudParams.saveWav = true; 

		boolean verbose = false; // true to print more stuff.
		
		try {
			
			final SudAudioInputStream sudAudioInputStream = SudAudioInputStream.openInputStream(new File(filePath), sudParams, verbose);
			

			sudAudioInputStream.addSudFileListener((chunkID, sudChunk)->{
				
				if (sudAudioInputStream.getChunkIDString(chunkID).equals("X3V2")) {
					//System.out.println("X3 compressed data either from continuous recordings or clicks");
					sudChunk.chunkHeader.toHeaderString(); 
	
				}

				if (sudAudioInputStream.getChunkIDString(chunkID).equals("wav")) {
					//System.out.println("Uncompressed .wav data either from continuous recordings or clicks");
					//WavFileHandler wavHandler = (WavFileHandler) sudAudioInputStream.getChunkDataHandler(chunkID).dataHandler; 
					
					if (sudAudioInputStream.isChunkIDWav(chunkID)) {
						System.out.println("ID: " + chunkID + " This is raw data from detected CLICKS: " + sudAudioInputStream.getChunkIDString(chunkID));
					}
					else {
						System.out.println("ID: " + chunkID + " This is raw data from continous RECORDINGS: " + sudAudioInputStream.getChunkIDString(chunkID));
					}
					
				}
				
				if (sudAudioInputStream.getChunkIDString(chunkID).equals("csv")) {
					System.out.println("CSV data the bytes convert directly to comma delimted data");
				}
				
				if (sudAudioInputStream.getChunkIDString(chunkID).equals("txt")) {
					//see the Text file handler for how to convert bytes - it's a little involved unfortunately. 
					System.out.println("Text data - almsot always used for bcl files");
				}
			});
			
			//run through the file. 
			while (sudAudioInputStream.available() > 0) {
				
				//note this is reading bytes of uncompressed continuous recordings only. 
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
