
package org.pamguard.x3.sud.test;

import java.io.File;
import java.io.IOException;

import org.pamguard.x3.sud.Chunk;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudFileExpander;
import org.pamguard.x3.sud.SudFileMap;
import org.pamguard.x3.sud.SudParams;
import org.pamguard.x3.utils.WavFile;
import org.pamguard.x3.utils.WavFileException;

/**
 * Basic test for opening a .sud file
 * @author Jamie Macaulay
 *
 */
public class SudarFileTest {
	
	
	public static void main(String[] args) {
		System.out.println("Test .sud file decompression");
				
		long time0 = System.currentTimeMillis();
		
		//4 channel sud files
		String wavFilePath = "/Users/jdjm/Desktop/sud_test/sud/738742278.180708083005.wav";

		String sudfilePath = "/Users/jdjm/Desktop/sud_test/sud/738742278.180708083005.sud";
		
		String sudWavPath = sudfilePath.replace(".sud", ".wav");

		SudParams sudParams = new SudParams();
		sudParams.setVerbose(false);
		sudParams.setFileSave(true, false, false, false);
		sudParams.setSudEnable(true, true, true);
		
		sudParams.zeroPad = true;
	

		SudFileExpander sudFileExpander = new SudFileExpander(new File(sudfilePath), sudParams); 

		try {
			sudFileExpander.processFile();
			sudFileExpander.closeFileExpander();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		WavFile readWavFile;
		try {
			
			
			//map the sud file - how many samples are available?
			SudFileMap sudMap = SudAudioInputStream.mapSudFile(sudFileExpander, null,  false);

			readWavFile = WavFile.openWavFile(new File(sudWavPath));
			//readWavFile.display();

			long numFrames = readWavFile.getNumFrames();
			int numChannels = readWavFile.getNumChannels();
			int validBits = readWavFile.getValidBits();
			long sampleRate = readWavFile.getSampleRate();
						
			System.out.println("The number of samples in the file from PAMGuard's own sud decompression is: " + numFrames + " and from sud map: " + sudMap.totalSamples + " first chnk microseconds. " + sudMap.firstChunkTimeMicrosecs); 
			
			sudParams.setSaveWav(false);
			
			SudAudioInputStream sudAudioInputStream = SudAudioInputStream.openInputStream(new File(sudfilePath), sudParams, false); 

			System.out.println("The number of samples in the file from PAMGuard's sud audio stream is: " + sudAudioInputStream.getFrameLength() +  " based on available bytes: " + sudAudioInputStream.available()/numChannels/2);
			sudAudioInputStream.close();

			//Read the file decompressed from SoundTrapHist.exe
			readWavFile = WavFile.openWavFile(new File(wavFilePath));
			numFrames = readWavFile.getNumFrames();

			System.out.println("The number of samples in the file from SoundTrap Host sud decompression is: " + numFrames); 

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WavFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
//		sudFileExpander.addSudFileListener((chunkID, sudChunk)->{
//			
//
//			System.out.println(sudFileExpander.getChunkFileType(chunkID));
//			System.out.println(sudChunk.chunkHeader.toHeaderString()); 
//
//			if (sudFileExpander.getChunkFileType(chunkID).equals("X3V2")) {
//				System.out.print(sudFileExpander.getChunkFileType(chunkID) +" x3 DATA:");
//				sudChunk.chunkHeader.toHeaderString(); 
//				
//				System.out.println("X3 nBytes: " + sudChunk.buffer.length);
//				System.out.println(chunkData2String(sudChunk));
//			}
//			
//			if (sudFileExpander.getChunkFileType(chunkID).equals("wav")) {
//				System.out.print(sudFileExpander.getChunkFileType(chunkID) +" WAV DATA:");
//				sudChunk.chunkHeader.toHeaderString(); 
//								
//				System.out.println("WAV nBytes: " + sudChunk.buffer.length);
//				System.out.println(chunkData2String(sudChunk));
//			}
//		});

		long time1 = System.currentTimeMillis();
		
		System.out.println("Processing time: " +  (time1-time0));
		
		

	}
	

	
	public static String chunkData2String(Chunk sudChunk) {
		String arr = ""; 
//		for (int i=0; i<sudChunk.buffer.length; i++) {
//			arr += (sudChunk.buffer[i] + ","); 
//		}
		for (int i=0; i<Math.min(sudChunk.buffer.length, 50); i++) {
			arr += (sudChunk.buffer[i] + ","); 
		}
		return arr; 
	}

}
