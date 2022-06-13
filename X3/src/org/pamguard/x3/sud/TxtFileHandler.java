package org.pamguard.x3.sud;


import java.io.DataInput;

import com.google.common.io.LittleEndianDataInputStream;

public class TxtFileHandler implements ISudarDataHandler {

	private int[] chunkIds;

	public TxtFileHandler(String filePath) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processChunk(ChunkHeader ch, byte[] buf) {
		// TODO Auto-generated method stub

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
