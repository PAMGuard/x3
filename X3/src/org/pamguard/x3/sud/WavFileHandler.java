package org.pamguard.x3.sud;


import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

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
	
	/***Info on the wav file****/
	private Integer fs;

	private String fileSuffix;

	private Integer timeCheck = 1;

	private Integer channel;

	private Integer nchan = 1;
	
	/***The chunk IDs associated with wav file writing***/

	private int[] chunkIds;

	private File sudFile;
	
	/***Wav file writing using Java's in built Audio API****/

	private String fileName;

	private File audioFile;

	private PipedOutputStream pipedOutputStream;


	private AudioInputStream audioInputStream;
	
	private Thread writeThread;

	/***Xero drop out info****/
	
	long totalBytes = 0;

	// the previous chunk to be written to the wav file. 
	private Chunk lastChunk;
	
	//the value of the last error. 
	private int lastError;

	private int chunkCount;

	private int cumulativeSamples;

	private int cumulativeTimeErrorUs;

	private boolean prevChunkWasNeg;

	private Chunk firstChunk;

	private boolean zeroFill;

	private LogFileStream logFile;

	
	//the difference between the sample count and the device's on board clock before a correction is made
	public static double timeErrorWarningThreshold = 0.04; //%



	public WavFileHandler(String filePath) {
		this.sudFile = new File(filePath);

		this.fileName = FilenameUtils.removeExtension(sudFile.getName());
	}
	

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
			//Test to see whether we need to zero fill drop outs. 
			if (lastChunk==null) {
				//the first chunk - nothing we can do here. 
				this.firstChunk = sudChunk; 
				
			}
			else {
				int elapsedTimeS = (sudChunk.chunkHeader.TimeS - lastChunk.chunkHeader.TimeS);
				
				//the time between chunks in micoseconds
				long elapsedTimeUs = (long)((elapsedTimeS * 1000000) + (sudChunk.chunkHeader.TimeOffsetUs - lastChunk.chunkHeader.TimeOffsetUs));
				
				//the time between chunks calculated using samples. 
				long calculatedTime = (long)((long)lastChunk.chunkHeader.SampleCount * 1000000 / fs);
				
				//these two times should be the same, 
				int error = (int) (elapsedTimeUs - calculatedTime);
				cumulativeTimeErrorUs += error;
				lastChunk = sudChunk;
				
				if ((timeCheck > 0) && (Math.abs(error) > (calculatedTime * timeErrorWarningThreshold))) {
					//log warning
					double t = (double)(sudChunk.chunkHeader.TimeS - firstChunk.chunkHeader.TimeS) + 
							((double)((long)sudChunk.chunkHeader.TimeOffsetUs -  firstChunk.chunkHeader.TimeOffsetUs) / 1000000);
					if (error > 0) {
						if (!prevChunkWasNeg) {
							//String.format("Sampling Gap {0} us at sample {1} ({2} s), chunk {3}", error, cumulativeSamples, t, chunkCount);
							logFile.writeXML(this.chunkIds[0], "WavFileHandler", "Info", String.format("Sampling Gap {0} us at sample {1} ({2} s), chunk {3}", error, cumulativeSamples, t, chunkCount));
							if (zeroFill) {
								
								int samplesToAdd = (int)(error * fs / 1000000);
								byte[] fill = new byte[samplesToAdd * 2 * nchan];
								pipedOutputStream.write(fill);

								//fsWavOut.Write(fill, 0, fill.Length);
								error = 0;
								cumulativeSamples += samplesToAdd;
								logFile.writeXML(this.chunkIds[0], "WavFileHandler", "Info", String.format("added {0} zeros", samplesToAdd));
							}
						}
					} 
					else {
						prevChunkWasNeg = true;

					}					
				}
				else {
					prevChunkWasNeg = false;
				}
				lastError = error; 

			}
						
			pipedOutputStream.write(sudChunk.buffer);
			lastChunk = sudChunk;


		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public void close() {
		
		if (this.lastChunk!=null) {

			// the format of your date
			SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); 
			// give a timezone reference for formatting (see comment at the bottom)
			SimpleDateFormat sdfLocal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); 
			sdf.setTimeZone(TimeZone.getDefault()); 
			
			long recordPeriod = lastChunk.chunkHeader.TimeS - firstChunk.chunkHeader.TimeS;
			recordPeriod *= 1000000;
			recordPeriod += (long)lastChunk.chunkHeader.TimeOffsetUs - firstChunk.chunkHeader.TimeOffsetUs;
			logFile.writeXML(chunkIds[0], "WavFileHandler", "OffloaderTimeZone", TimeZone.getDefault().getDisplayName());
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SamplingStartTimeLocal", String.format("%s", sdfLocal.format(new Date(firstChunk.chunkHeader.TimeS*1000L))));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SamplingStopTimeLocal", String.format("%s", sdfLocal.format(new Date(lastChunk.chunkHeader.TimeS*1000L))));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SamplingStartTimeUTC", String.format("%s", sdf.format(new Date(firstChunk.chunkHeader.TimeS*1000L))));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SamplingStopTimeUTC", String.format("%s", sdf.format(new Date(lastChunk.chunkHeader.TimeS*1000L))));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SamplingStartTimeSubS", String.format("%d us", firstChunk.chunkHeader.TimeOffsetUs));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SamplingTimePeriod", String.format("%d us", recordPeriod));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "CumulativeSamplingGap", String.format("%d us", cumulativeTimeErrorUs));
			logFile.writeXML(chunkIds[0], "WavFileHandler", "SampleCount", String.format("%d", cumulativeSamples));
		}
		
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
	public void init(LogFileStream inputStream, String innerXml, int id) {
		this.logFile = inputStream;
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
