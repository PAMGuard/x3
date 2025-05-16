package org.pamguard.x3.sud.test;

import java.io.File;
import org.pamguard.x3.sud.SudAudioInputStream;
import org.pamguard.x3.sud.SudParams;
import org.pamguard.x3.utils.WavFile;

public class SudStreamTest {
	
	public static void main(String[] args) {
		
	long time0 = System.currentTimeMillis();
	
	//4 channel sud files

	String sudfilePath = "/Users/jdjm/Desktop/sud_test_pamguard/sud/5150.250415052938.sud";

	
	//String sudfilePath = "/Users/jdjm/Desktop/sud_test/sud/738742278.180708083005.sud";

	
	WavFile readWavFile;
	try {
		SudParams sudParams = new SudParams();
		sudParams.setVerbose(false);
		sudParams.setFileSave(false, false, false, false);
		sudParams.setSudEnable(true, true, true);
		
		SudAudioInputStream sudAudioInputStream = SudAudioInputStream.openInputStream(new File(sudfilePath), sudParams, false); 
		
		byte[] audiochnk = new byte[307200]; 
		int count = 0;
		int read = 0;
		while (sudAudioInputStream.available()>10000) {
			read += sudAudioInputStream.read(audiochnk);
			if (count%10==0) {
				System.out.println("Bytes available: compressed: " + sudAudioInputStream.getSudFileExpander().getSudInputStream().available() + " uncompressed " + (sudAudioInputStream.getTotalBytes() - sudAudioInputStream.getBytesRead()) +  " " + read); 
			}
			
			if (sudAudioInputStream.getSudFileExpander().getSudInputStream().available()<10) {
				System.out.println("Bytes available: compressed: " + sudAudioInputStream.getSudFileExpander().getSudInputStream().available() + " uncompressed " + (sudAudioInputStream.getTotalBytes() - sudAudioInputStream.getBytesRead())); 
				break;
			}
			count++;
		}
		
		System.out.println("Bytes read: uncompressed: " + read);

		sudAudioInputStream.close();
	}
	catch (Exception e) {
		e.printStackTrace();
		}
	}

}
