package org.pamguard.x3.x3;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.w3c.dom.Document;

/**
 * Test program
 * @author Doug Gillespie
 *
 */
public class X3D3Test {

	public static X3FrameDecode frameDecoder = new X3FrameDecode();

	public X3D3Test() {
		// TODO Auto-generated constructor stub
	}


	public static void main(String[] args) {

		testEncode();
		//		testDecode();
	}

	private static void testEncode() {

		X3FileSystem x3FileSystem = new X3D3FileSystem();
		String sourceName = "C:\\PamguardTest\\PLATest\\MF_20131004_000037_500.wav"; // buoy data
		X3FrameEncode x3FrameEncode = new X3FrameEncode();
		//open the wav file and start reading chunks of data. 
		File audioFile = new File(sourceName);
		if (audioFile.exists() == false) {
			System.err.println("File does not exist : " + sourceName);
		}
		int totalSamples = 0;
		try {
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
			AudioFormat audioFormat = audioStream.getFormat();
			Document doc = x3FileSystem.createX3HeaderXML(X3FrameEncode.blockSamples,
					(int) audioFormat.getSampleRate(), audioFormat.getChannels());
			System.out.println(x3FileSystem.getXMLDataText(doc, 0));
//			if (2>1) return;
			int chunkBytes = audioFormat.getFrameSize() * X3FrameEncode.frameSamples;
			byte[] wavBytes = new byte[chunkBytes];
			short[] wavData = new short[chunkBytes/2];
			int available;
			int newSamples;
			int w1, w2, w;
			while((available = audioStream.available()) > 0) {
				chunkBytes = Math.min(chunkBytes, available);
				audioStream.read(wavBytes, 0, chunkBytes);
				newSamples = chunkBytes / audioFormat.getFrameSize();
				totalSamples += newSamples;
				int bPos = 0;
				for (int i = 0; i < newSamples; i++, bPos+=2)  {
					wavData[i] = (short) ((wavBytes[bPos+1]&0xFF)<<8 | (wavBytes[bPos]&0xFF));
//					wavData[i] /= 2;
				}
//				x3FrameEncode.encodeFrame(wavData, audioFormat.getChannels(), newSamples);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		System.out.println("Total samples read from file = " + totalSamples);
		int[] codeCount = x3FrameEncode.codeCount;
		for (int i = 0; i <codeCount.length; i++) {
			System.out.println("Compression code usage " + i + " = " + codeCount[i]);
		}
	}


	private static void testDecode() {
		//		Try unpacking an  x3a file. 
		//		String x3Name = "C:\\PamguardTest\\PLATest\\20150323_10\\PLA_20150323_120355_865691.x3a"; //0's
		//		String x3Name = "C:\\PamguardTest\\PLATest\\20150323_10\\PLA_20150323_221823_416395.x3a"; // 2*8 step
		//		String x3Name = "C:\\PamguardTest\\PLATest\\20150323_10\\PLA_20150323_220519_951583.x3a"; // 13*8 step
		String sourceName = "C:\\PamguardTest\\PLATest\\MF_20131004_000037_500.x3"; // buoy data


		String destName = sourceName.replace(".x3", ".wav");
		destName = destName.replace(".x3a", ".wav");
		File destFile = new File(destName);
		if (destFile.exists()) destFile.delete();
		byte[] tstData = new byte[2000];
		for (int i = 0; i < tstData.length; i+=2) {
			//			tstData[i]=i;
		}
		AudioFormat frmt = new AudioFormat(44100, 16, 1, true, false);
		//		ByteOutputStream aos = new ByteOutputStream();
		//		AudioInputStream ais = new AudioInputStream(aos, frmt,
		//				tstData.length / frmt.getFrameSize()
		//				);
		//		AudioInputStream ais = new AudioInputStream()
		//		for (int i = 0; i < 5; i++) {
		//		try {
		//			
		//			int wrote = AudioSystem.write(ais, AudioFileFormat.Type.WAVE, destFile);
		//			System.out.println("Bytes wrote = " + wrote);
		//		}
		//		catch(Exception e) {
		//			e.printStackTrace();
		//		}
		//		}
		////		AudioSystem.
		//		if (2 >1) return ;



		File x3File = new File(sourceName);
		X3FrameHeader x3Head = new X3FrameHeader();
		byte[] x3Data;
		FileInputStream fis = null;
		int x3FileType = 0;
		X3FileSystem x3FileSystem;
		try {
			fis = new FileInputStream(x3File);
			DataInputStream dis = new DataInputStream(fis);
			x3FileType = X3FileSystem.getX3Type(dis);
			x3FileSystem = X3FileSystem.getFileSystem(x3FileType);
			if (x3FileSystem == null) {
				System.out.println("Unknown X3 file system");
				return;
			}
			String xmlString = x3FileSystem.readFileHeader(dis);
			X3FileHeader x3FileHeader = x3FileSystem.decodeFileHeader(xmlString);
			if (x3FileHeader == null) {
				System.out.println("Failed to read file header from x3 file.");
				return;
			}
			String str;
			int blocksRead = 0;
			while (true) {
				x3Head = x3FileSystem.readFrameHeader(dis, x3FileHeader, x3Head);
				x3Data = new byte[x3Head.getnBytes()];
				dis.read(x3Data);
				if (x3Head.getId() == 1){
					frameDecoder.unpackX3Frame(x3Head, x3Data, 0, null, x3FileHeader.blockLen);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch(EOFException e) {
			System.out.println("End of file");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
