package org.pamguard.x3.x3;

/**
 * General X3 encoder / decoder. 
 * @author Doug Gillespie
 *
 */
public interface X3Encoder {

	static public final int ERROR_OK = 0;
	static public final int ERROR_FAIL = 1;
	
	/**
	 * Convert a wav file into an X3 file. 
	 * @param sourceFile Wav file name
	 * @param destFile X3 file name
	 * @return 0 if sucessful
	 */
	public int wavToX3(String sourceFile, String destFile);
	
	/**
	 * Convert an x3 file into a wav file
	 * @param sourceFile X3 file name
	 * @param destFile Wav file name
	 * @return 0 if sucessful. 
	 */
	public int x3ToWav(String sourceFile, String destFile);
	
}
