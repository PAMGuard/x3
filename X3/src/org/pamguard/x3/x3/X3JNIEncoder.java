package org.pamguard.x3.x3;

/**
 * Encoder which makes calls through to C code JNI functions. 
 * @author Doug Gillespie
 *
 */
public class X3JNIEncoder implements X3Encoder {

	private static final String libName = "libX3InflateJNI";
	private boolean libOk;


	public X3JNIEncoder() {
		super();
		libOk = loadLibrary();
	}

	private boolean loadLibrary() {		
		try {
			System.loadLibrary(libName);
			return true;
		}
		catch (UnsatisfiedLinkError e) {
			System.out.println(e.getMessage());
			return false; 
		}
	}

	public native int jniWavToX3(String sourceFile, String destFile);

	public native int jniX3ToWav(String sourceFile, String destFile);
	
	/**
	 * Open a file for x3 recording
	 * @param fileName Full path to file
	 * @param nBits number of bits in the data
	 * @param sampleRate sample rate
	 * @param channelMap bitmap of used channels. 
	 * @param timeMillis time in milliseconds (written into file header)
	 * @return 1 if file opened ok. 
	 */
	private native int jniOpenFile(String fileName, int nBits, int sampleRate, int channelMap, long timeMillis);
	
	/**
	 * Close the current x3 file. 
	 * @return
	 */
	private native int jniCloseFile();
	
	/**
	 * Write new data to the file.<p>
	 * Data will be for all channels in the channel map given to
	 * the openNewFile function and are assumed to be interleaved.
	 * <p>First copy the data into the rawDatabuffer until the buffer is full.
	 * then send that buffer off to be compressed, written to output file and
	 * reset.
	 * dataSamples is samples per channel so the length of data should be
	 * dataSamples * the number of channels. Data should be interleaved.
	 */
	private native int jniRecordData(short[] data, int dataSamples);

	@Override
	public int wavToX3(String sourceFile, String destFile) {
		if (libOk == false) {
			return ERROR_FAIL;
		}
		return jniWavToX3(sourceFile, destFile);
	}

	@Override
	public int x3ToWav(String sourceFile, String destFile) {
		if (libOk == false) {
			return ERROR_FAIL;
		}
		return jniX3ToWav(sourceFile, destFile);
	}

	/**
	 * Open a file for x3 recording
	 * @param fileName Full path to file
	 * @param nBits number of bits in the data
	 * @param sampleRate sample rate
	 * @param channelMap bitmap of used channels. 
	 * @param timeMillis time in milliseconds (written into file header)
	 * @return 1 if file opened ok. 
	 */
	public int openFile(String fileName, int nBits, int sampleRate, int channelMap, long timeMillis) {
		if (libOk == false) {
			return ERROR_FAIL;
		}
		return jniOpenFile(fileName, nBits, sampleRate, channelMap, timeMillis);
	}
	
	/**
	 * Close the current x3 file. 
	 * @return
	 */
	public int CloseFile() {
		if (libOk == false) {
			return ERROR_FAIL;
		}
		return jniCloseFile();
	}
	
	/**
	 * Write new data to the file.<p>
	 * Data will be for all channels in the channel map given to
	 * the openNewFile function and are assumed to be interleaved.
	 * <p>First copy the data into the rawDatabuffer until the buffer is full.
	 * then send that buffer off to be compressed, written to output file and
	 * reset.
	 * dataSamples is samples per channel so the length of data should be
	 * dataSamples * the number of channels. Data should be interleaved.
	 */
	public int recordData(short[] data, int dataSamples) {
		if (libOk == false) {
			return ERROR_FAIL;
		}
		return jniRecordData(data, dataSamples);
	}
}
