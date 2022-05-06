package org.pamguard.x3.sud;


import com.google.common.io.LittleEndianDataInputStream;

/**
 * CSV data writer. 
 * @author Jamie Macaulay
 *
 */
public class CsvFileHandler implements ISudarDataHandler {
	
	/**
	 * Chunk IDs. 
	 */
	private int[] chunkIds;  

	public CsvFileHandler(String filePath) {
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
