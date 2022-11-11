package org.pamguard.x3.sud;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A map of a single .sud file. This allows the file to be processed without
 * first iterating through the file to map chunks.
 * 
 * @author Jamie Macaulay
 *
 */
public class SudFileMap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4L;

	/**
	 * A list of all chunk headers in the file.
	 */
	public ArrayList<ChunkHeader> chunkHeaderMap;

	/**
	 * The total number of SAMPLES (not bytes).
	 */
	public long totalSamples;

	/**
	 * The sample rate of the file.
	 */
	public float sampleRate;

	/**
	 * Was the data zero padded - if the file is opened with a different setting the
	 * map mus be recreated.
	 */
	public boolean zeroPad;

	/**
	 * The number of bits per sample.
	 */
	public int bitsPerSample = 16;

	/**
	 * The number of channels
	 */
	public int nChannels = 1;
	
	/**
	 * The sample rate of the click detector. NaN if there is no click detector
	 */
	public float clickDetSampleRate = Float.NaN; 
	
	/**
	 * The start time of the file in millisecond time. 
	 */
	public long headerTimeMillis = 0; 
	
	/**
	 * The ID of the hardware used to record the SudFile. 
	 */
	public String hardwareID = null;
	
	/**
	 * The temperature of the device when the file was created. 
	 */
	public double temperature = 0; 
	
	/**
	 * All metadata associated with the sud file. 
	 */
	public String xmlMetaData = null;

	/**
	 * time of the first chunk. 
	 */
	public long firstChunkTimeMicrosecs; 

	/**
	 * Get the number of channels. 
	 * @return the number of channels. 
	 */
	public int getNChannels() {
		return nChannels;
	}

	/**
	 * Get the number of bits per sample. 
	 * @return the number of bits per sample. 
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * Get the sample rate of the file in samples per second. 
	 * @return the sample rate in samples per second. 
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * @return the firstChunkTime in microseconds
	 */
	public long getFirstChunkTimeMicros() {
		return firstChunkTimeMicrosecs;
	}

	/**
	 * @param firstChunkTime the firstChunkTime to set
	 */
	public void setFirstChunkTimeMicros(long firstChunkTime) {
		this.firstChunkTimeMicrosecs = firstChunkTime;
	}

	/**
	 * @return the firstChunkTime in milliseconds
	 */
	public long getFirstChunkTimeMillis() {
		return firstChunkTimeMicrosecs/1000;
	}

}
