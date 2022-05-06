package org.pamguard.x3.sud;

import java.io.BufferedInputStream;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * Opens an X3 block
 * @author Jamie Macaulay
 *
 */
public class X3Handler implements ISudarDataHandler {

	private int[] chunkIds;

	public X3Handler(String filePath) {
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
	public void init(LittleEndianDataInputStream inputStream, String innerXml, int id) {
		this.chunkIds = new int[]{id};

	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}


}
