package org.pamguard.x3.sud;


import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.io.FilenameUtils;
import org.pamguard.x3.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * Write wav files from the X3 data (or umcompressed data blocks in the SUD files). 
 * 
 * @author Jamie Macaulay
 *
 */
public class WavFileHandler implements ISudarDataHandler {

	private int[] chunkIds;

	private File sudFile;

	private String fileName;

	private File audioFile;

	private PipedOutputStream pipedOutputStream;

	private Integer fs;

	private String fileSuffix;

	private Integer timeCheck = 1;

	private Integer channel;

	private Integer nchan = 1;

	private AudioInputStream audioInputStream;

	public WavFileHandler(String filePath) {
		this.sudFile = new File(filePath);

		this.fileName = FilenameUtils.removeExtension(sudFile.getName());
	}
	
	long totalBytes = 0;

	private Object lastChunk;

	private int chunkCount;

	private int cumulativeSamples;

	private int cumulativeTimeErrorUs;

	private Thread writeThread;


	@Override
	public void processChunk(Chunk sudChunk) {
		
		//System.out.println("Process wav file: " + sudChunk.buffer.length + "  " + sudChunk.buffer[0]);
		
		//create the audio file. 
		if (audioFile==null) {
			audioFile = new File(fileName + fileSuffix + ".wav");
			if (audioFile.exists()) {
				audioFile.delete();
			}
			writeThread = new Thread(new WriteThread());
			writeThread.start();
		}
		
		try
		{
			
			
			pipedOutputStream.write(sudChunk.buffer);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public void close() {
		//System.out.println("Close the file:"); 
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
	}

	@Override
	public void init(DataInput inputStream, String innerXml, int id) {
		this.chunkIds = new int[]{id};
		
		
		Document doc = XMLFileHandler.convertStringToXMLDocument(innerXml.trim());

		NodeList nodeList = doc.getElementsByTagName("CFG");
		
		HashMap<String, String> nodeContent = XMLUtils.getInnerNodeContent(new String[] {"FS", "SUFFIX", "TIMECHK", "CHANNEL", "NCHS"},  nodeList);
		
		
//		for (int i=0; i<nodeContent.size(); i++) {
//			System.out.println(nodeContent[i]);
//		}
		
		channel = -1;
		nchan = 1;
		cumulativeTimeErrorUs = 0;
		cumulativeSamples = 0;
		chunkCount = 0;
		lastChunk = null;
		
		fs = Integer.valueOf(nodeContent.get("FS"));
		fileSuffix = nodeContent.get("SUFFIX");
		if (nodeContent.get("TIMECHK")!=null) timeCheck = Integer.valueOf(nodeContent.get("TIMECHK"));
		if (nodeContent.get("CHANNEL")!=null) channel = Integer.valueOf(nodeContent.get("CHANNEL"));
		nchan = Integer.valueOf(nodeContent.get("NCHS"));

		//create the audio format. 
		AudioFormat audioFormat = new AudioFormat(fs, 16,nchan, true, false);
		
		///create the wav writer
		PipedInputStream pipedInputStream;
		pipedOutputStream = null;
		try {
			pipedInputStream = new PipedInputStream();
			pipedOutputStream = new PipedOutputStream(pipedInputStream);
			audioInputStream = new AudioInputStream(pipedInputStream, audioFormat, AudioSystem.NOT_SPECIFIED);

		}
		catch (IOException Ex) {
			Ex.printStackTrace();
		}
	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
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
		//System.out.println("Enter write Data");
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
