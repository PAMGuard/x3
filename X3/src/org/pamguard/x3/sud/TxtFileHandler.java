package org.pamguard.x3.sud;


import java.io.File;


/**
 * Processes a text file chunk form a sud file. 
 * <p>
 * TODO - this file handler may never be used in standard .sud files and so has not been implemented. 
 * @author Jamie Macaulay
 *
 */
public class TxtFileHandler implements ISudarDataHandler {

	private int[] chunkIds;
	
	/**
	 * The current sud file. 
	 */
	private File sudFile;

	private String ftype;

	public TxtFileHandler(String filePath) {
		this.sudFile = new File(filePath);
	}

	public TxtFileHandler(String filePath, String ftype) {
		this.ftype= ftype; 
	}

	@Override
	public void processChunk(Chunk sudChunk) {

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
	
	int swapWords( int x ){
		return ((x & 0x0000FFFF) << 16) | ((x & 0xFFFF0000) >> 16);
	}
	
	public void SwapEndian(byte[] data) {
		 XMLFileHandler.swapEndian(data);
	}


	@Override
	public void init(LogFileStream inputStream, String innerXml, int id) {
		this.chunkIds = new int[]{id};

	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}

	@Override
	public String getHandlerType() {
		return ftype;
	}

}
