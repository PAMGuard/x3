package org.pamguard.x3.sud;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.pamguard.x3.x3.RiceTable;


/**
 * Prints out the first X3 and corresponding wav chunk
 * 
 * @author Jamie Macaulay 
 *
 */
public class SudFileX3ChunkTest {

	final int MAX_PRINT = 1; //the maximum number of lines to print starting form the first chunk

	private byte[] x3;

	private byte[] wav;

	public void testSudFile(File sudFileIn, File wavFileOut) {

		SudParams sudParams = new SudParams(); 
		SudFileExpander sudFileExpander = new SudFileExpander(sudFileIn); 

		AtomicInteger count = new AtomicInteger(0); 

		sudFileExpander.addSudFileListener((chunkID, sudChunk)->{

			if (sudFileExpander.getChunkFileType(chunkID).equals("X3V2") && count.get()<MAX_PRINT) {
				System.out.print(sudFileExpander.getChunkFileType(chunkID) +" x3 DATA:");
				System.out.print(sudChunk.chunkHeader.toHeaderString()); 
				
				System.out.println("X3 nBytes: " + sudChunk.buffer.length);
				System.out.println(chunkData2String(sudChunk));
				x3=sudChunk.buffer;
			}

			if (sudFileExpander.getChunkFileType(chunkID).equals("wav") && count.get()<MAX_PRINT) {
				System.out.print(sudFileExpander.getChunkFileType(chunkID) +" WAV DATA:");
				System.out.print(sudChunk.chunkHeader.toHeaderString()); 
				
				System.out.println("WAV nBytes: " + sudChunk.buffer.length);
				System.out.println(chunkData2String(sudChunk));
				count.set(count.get()+1); 
				wav=sudChunk.buffer;
			}
		});

		//run the file
		try {
			sudFileExpander.processFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//convert to wav file data. 
		short[] wavData = new short[(int) wav.length/2]; 
		for (int i=0; i<wav.length; i=i+2) {
			wavData[(int) i/2]  = (short) (((wav[i] & 0xFF))
							| (short)((wav[i+1] & 0xFF) << 8 )
							);
		};

		//Now save the wav file data to a text file
		try {
			FileWriter myWriter = new FileWriter(wavFileOut);
			myWriter.write(shortArr2String(wavData));
			myWriter.close();
			System.out.println("Successfully wrote "  + wav.length + " bytes to the file from: " + x3.length + " compressed bytes.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}


	}

	public static String chunkData2String(Chunk sudChunk) {
		String arr = ""; 
//		for (int i=0; i<sudChunk.buffer.length; i++) {
//			arr += (sudChunk.buffer[i] + ","); 
//		}
		for (int i=0; i<sudChunk.buffer.length; i++) {
			arr += (sudChunk.buffer[i] + ","); 
		}
		return arr; 
	}

	public static String shortArr2String(short[] sudChunk) {
		String arr = ""; 
		for (int i=0; i<sudChunk.length; i++) {
			arr += (sudChunk[i] + "\n"); 
		}
		return arr; 
	}


	public static void main(String[] args) {
		System.out.println("Hello .sud file decompression");
		//		String filePath = "/Users/au671271/MATLAB-Drive/MATLAB/PAMGUARD/x3/335564854.180411000003.sud";
		//		String filePath = "/Volumes/GoogleDrive-108005893101854397430/My Drive/PAMGuard_dev/sud_decompression/singlechan_exmple/67411977.171215195605.sud";
		// 		String sudFileInPath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/sud_decompression/singlechan_exmple/67411977.171215195605.sud";
		String sudFileInPath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/sud_decompression/singlechan_exmple/67411977.171215195605.sud";

		
		//save to a file inside s a folder containing some C code to write .sud files. 
		String wavFileOutPath = "/Users/au671271/Documents/testWav.txt";

		SudFileX3ChunkTest sudFileX3ChunkTest = new SudFileX3ChunkTest(); 

		sudFileX3ChunkTest.testSudFile(new File(sudFileInPath), new File(wavFileOutPath)); 
		
		 short[] irt = RiceTable.makeInverseRice(20);
		 String arr = ""; 
			for (int i=0; i<irt.length; i++) {
				arr += (irt[i] + ","); 
			}
	System.out.println(arr);


	}

}
