package org.pamguard.x3.x3;

/**
 * 
 * File header information for an X3 File. 
 * Contains essential information such as sample rate
 * and number of channels as well as stuff like the 
 * X3 packed block size and default frame length. 
 * @author Doug Gillespie
 *
 */
public class X3FileHeader {

	// essential sound information
	public int sampleRate;
	public int nChannels;
	public long startTimeMillis;
	
	// essential X3 information
	int blockLen;
	
	public X3FileHeader() {
	}

}
