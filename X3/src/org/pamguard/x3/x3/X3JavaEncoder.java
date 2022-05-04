package org.pamguard.x3.x3;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


/**
 * Pure Java X3 - Wav file encoder / decoder. 
 * @author Doug Gillespie
 *
 */
public class X3JavaEncoder implements X3Encoder {

	public static X3FrameDecode frameDecoder = new X3FrameDecode();

	private volatile AudioInputStream audioInputStream;

	private File audioFile;

	private Thread writeThread;

	public X3JavaEncoder() {
	}

	@Override
	public int wavToX3(String sourceFile, String destFile) {
		X3FrameEncode x3FrameEncode = new X3FrameEncode();
		X3FileSystem x3FileSystem = new X3D3FileSystem();
		// start by opening the wav file and getting the audio format information
		File audioFile = new File(sourceFile);
		if (audioFile.exists() == false) {
			System.err.println("File does not exist : " + sourceFile);
			return 1;
		}
		int totalSamples = 0;
		AudioInputStream audioStream = null;
		AudioFormat audioFormat = null;
		try {
			audioStream = AudioSystem.getAudioInputStream(audioFile);
			audioFormat = audioStream.getFormat();
		}
		catch (IOException e) {
			e.printStackTrace();
			return 1;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return 1;
		}
		// now get some xml information to write into the header of the destination file ...
		Document doc = x3FileSystem.createX3HeaderXML(X3FrameEncode.blockSamples,
				(int) audioFormat.getSampleRate(), audioFormat.getChannels());
		String xmlString = x3FileSystem.getXMLDataText(doc, 0);
//		System.out.println(xmlString);
		X3FrameHeader x3FrameHeader = new X3FrameHeader();
		// now open file file output stream.
		FileOutputStream fos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream(new File(destFile));
			dos = new DataOutputStream(new BufferedOutputStream(fos));
			dos.write(X3FileSystem.X3A_FILE_KEY.getBytes());
			x3FrameHeader.setId((byte) 0);
			x3FrameHeader.setnChan(0);
			x3FrameHeader.setnSamples((short) 0);
			x3FrameHeader.setnBytes((short) xmlString.length());
			x3FrameHeader.setTimeCode(0);
			x3FrameHeader.setTimeMicros(0);
			x3FrameHeader.setCrcHead(CRC16.getCRC16(x3FrameHeader.getHeadData(), 16));
			byte[] xmlBytes = xmlString.getBytes();
			x3FrameHeader.setCrcData(CRC16.getCRC16(xmlBytes, xmlBytes.length));
			dos.write(x3FrameHeader.getHeadData());
			dos.write(xmlBytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// file is now ready and has it's header - so start working through the wav file. 
		int chunkBytes = audioFormat.getFrameSize() * X3FrameEncode.frameSamples;
		byte[] wavBytes = new byte[chunkBytes];
		byte[] packedData = new byte[chunkBytes * 2];
		short[] wavData = new short[chunkBytes/2];
		int available;
		int newSamples;
		int w1, w2, w;
		X3FrameHeader frameHeader = new X3FrameHeader();
		frameHeader.setId((byte) 2);
		frameHeader.setX3_key(X3FrameHeader.X3_KEY);
		frameHeader.setnChan(audioFormat.getChannels());
		try {
			while((available = audioStream.available()) > 0) {
				chunkBytes = Math.min(chunkBytes, available);
				audioStream.read(wavBytes, 0, chunkBytes);
				newSamples = chunkBytes / audioFormat.getFrameSize();
				totalSamples += newSamples;
				int bPos = 0;
				for (int i = 0; i < chunkBytes/2; i++, bPos+=2)  {
					wavData[i] = (short) ((wavBytes[bPos+1]&0xFF)<<8 | (wavBytes[bPos]&0xFF));
					//				wavData[i] /= 2;
				}
				int packedBytes = x3FrameEncode.encodeFrame(wavData, packedData, audioFormat.getChannels(), newSamples);
				// round to an even number
				packedBytes = (packedBytes + 1) / 2;
				packedBytes *= 2;
				// make a header for the block. 
				frameHeader.setnSamples((short) newSamples);
				frameHeader.setnBytes((short) packedBytes);
				// now get the CRC's. 
				short crc = CRC16.getCRC16(frameHeader.getHeadData(), 16);
				frameHeader.setCrcHead(crc);
				crc = CRC16.getCRC16(packedData, packedBytes);
				frameHeader.setCrcData(crc);
				// now write the x3 data
				dos.write(frameHeader.getHeadData());
				dos.write(packedData, 0, packedBytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return 1;
		}

		try {
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
//		System.out.println("Total samples converetd from wav to x3 = " + totalSamples);
		return 0;
	}

	@Override
	public int x3ToWav(String sourceFile, String destFile) {
		// first sort out the wav file endof things ...

		PipedInputStream pipedInputStream;
		PipedOutputStream pipedOutputStream = null;
		try {
			pipedInputStream = new PipedInputStream();
			pipedOutputStream = new PipedOutputStream(pipedInputStream);
		}
		catch (IOException Ex) {
			Ex.printStackTrace();
			return 1;
		}
		audioFile = new File(destFile);
		if (audioFile.exists()) {
			audioFile.delete();
		}


		File x3File = new File(sourceFile);
		X3FrameHeader x3Head = new X3FrameHeader();
		byte[] x3Data;
		FileInputStream fis = null;
		int x3FileType = 0;
		X3FileSystem x3FileSystem;
		long totalSamples = 0;
		try {
			fis = new FileInputStream(x3File);
			DataInputStream dis = new DataInputStream(fis);
			x3FileType = X3FileSystem.getX3Type(dis);
			x3FileSystem = X3FileSystem.getFileSystem(x3FileType);
			if (x3FileSystem == null) {
				System.out.println("Unknown X3 file system");
				return 1;
			}
			String xmlHeadString = x3FileSystem.readFileHeader(dis);
			Document doc = x3FileSystem.convertStringToDocument(xmlHeadString);
			writeXmlFile(doc, X3FileSystem.getfileTypeString(x3FileType), destFile);
			X3FileHeader x3FileHeader = x3FileSystem.decodeFileHeader(xmlHeadString);
			if (x3FileHeader == null) {
				System.out.println("Failed to read file header from x3 file.");
				return 1;
			}
			//			// get this far so can open the stream for the wav file
			AudioFormat audioFormat = new AudioFormat(x3FileHeader.sampleRate, 16, x3FileHeader.nChannels, true, false);
			audioInputStream = new AudioInputStream(pipedInputStream, audioFormat, AudioSystem.NOT_SPECIFIED);

			writeThread = new Thread(new WriteThread());
			writeThread.start();


			String str;
			int blocksRead = 0;
			short[] data = null;
			byte[] byteData = null;
			while (true) {
				x3Head = x3FileSystem.readFrameHeader(dis, x3FileHeader, x3Head);
				if (x3Head == null) {
					break;
				}
				x3Data = new byte[x3Head.getnBytes()];
				dis.read(x3Data);
				if (x3Head.getId() > 0){
					data = frameDecoder.unpackX3Frame(x3Head, x3Data, 0, data, x3FileHeader.blockLen);
					if (byteData == null || byteData.length != data.length*2) {
						byteData = new byte[data.length*2];
					}
					int ib = 0;
					for (int i = 0; i < data.length; i++, ib+=2) {
						byteData[ib] = (byte) data[i];
						byteData[ib+1] = (byte) (data[i]>>>8);
					}
					pipedOutputStream.write(byteData);
					totalSamples += x3Head.getnSamples();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch(EOFException e) {
			System.out.println("End of file " + sourceFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception exc) {
			exc.printStackTrace();
			
		}

		try {
			pipedOutputStream.flush();
			pipedOutputStream.close();
			// don't close audioInputStream - the actual bit that writes. It will 
			// close itself when it's done in it's own thread. But we DO need to wait for 
			// that to happen. 
//			audioInputStream.
//			audioInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 *  Wait for the write thread to complete (but don't wait forever !
		 * 
		 */
		int nKips = 0;
		for (int i = 0; i < 50; i++) {
			if (audioInputStream == null) {
				break;
			}
			try {
				Thread.sleep(100);
				nKips++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (nKips >= 49) {
			System.out.println(String.format("Warning !!! Waited %d milliseconds for file write to complete.", nKips*100));
		}
//		System.out.println("Total samples convereted from x3 to wav = " + totalSamples);

		return 0;
	}

	/**
	 * Write the xml data as a new xml document in the same folder as the output file
	 * @param doc Xml data read from x3 file. 
	 * @param destFile dest wav file name - change to .xml. 
	 */
	private boolean writeXmlFile(Document doc, String type, String destFile) {
		int lw = destFile.lastIndexOf(".wav");
		if (lw < 0) {
			lw = destFile.lastIndexOf(".WAV");
		}
		if (lw < 0) { 
			System.out.println("unable to find correct end to turn to .xml of file " + destFile);
			return false;
		}
		String xmlName = destFile.substring(0, lw) + ".xml";
		
//		FileOutputStream fos = null;
//		try {
//			fos = new FileOutputStream(xmlName);
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		}
//		OutputFormat of = new OutputFormat("XML","ISO-8859-1",true);
//		of.setIndent(1);
//		of.setLineSeparator("\r\n");
//		of.setIndenting(true);
//		of.setDoctype(null, type);
//		XMLSerializer serializer = new XMLSerializer(fos,of);
//		// As a DOM Serializer
//		try {
//			serializer.asDOMSerializer();
//			serializer.serialize( doc.getDocumentElement() );
//			fos.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, null);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, type);
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(domSource, result);
			String asString = writer.toString();
			if (asString!=null) {
				BufferedWriter out = new BufferedWriter(new FileWriter(xmlName, false));
				out.write(asString);
				out.close();
			}
		} catch (TransformerException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}			
		
		return true;
	}

	class WriteThread implements Runnable {

		@Override
		public void run() {
			writeData();
		}
	}

	/**
	 * Called within the write thread, this does not return
	 * until the pipes get closed. 
	 */
	private void writeData() {
		//		System.out.println("Enter write Data");
		long totalBytes = 0;
		try
		{
			long bytesWritten = AudioSystem.write(audioInputStream,	AudioFileFormat.Type.WAVE, audioFile);
			totalBytes += bytesWritten;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		try {
			audioInputStream.close();
			audioInputStream = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		//		System.out.println("Leave write Data");
//		System.out.println("Leave Wav write thread after n Bytes = " + totalBytes);
	}

}
