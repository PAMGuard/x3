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
	private static final long serialVersionUID = 1L;

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
	 * Get the number of channels. 
	 * @return the number of channels. 
	 */
	public int getNChannels() {
		return nChannels;
	}

	/**
	 * Gte the number of bits per sample. 
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

}
