package org.pamguard.x3.sud;


import java.io.DataInput;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * Parse wav files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class WavFileHandler implements ISudarDataHandler {

	private int[] chunkIds;

	public WavFileHandler(String filePath) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processChunk(ChunkHeader ch, byte[] buf) {
		System.out.println("Process wav chunk");
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(DataInput inputStream, String innerXml, int id) {
		this.chunkIds = new int[]{id};

	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}

}
