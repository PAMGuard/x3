package org.pamguard.x3.sud;


import java.io.DataInput;
import java.io.File;

public class TxtFileHandler implements ISudarDataHandler {

	private int[] chunkIds;
	
	/**
	 * The current sud file. 
	 */
	private File sudFile;

	public TxtFileHandler(String filePath) {
		this.sudFile = new File(filePath);
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

}
