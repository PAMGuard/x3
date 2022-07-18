package org.pamguard.x3.sud;

/**
 * Holds a chunk header and a data buffer for a single 
 * sud file chunk. 
 * @author Jamie Macaulay 
 *
 */
public class Chunk {
	
	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public ChunkHeader getChunkHeader() {
		return chunkHeader;
	}

	public void setChunkHeader(ChunkHeader chunkHeader) {
		this.chunkHeader = chunkHeader;
	}

	public Chunk(byte[] buffer, ChunkHeader chunkHeader) {
		super();
		this.buffer = buffer;
		this.chunkHeader = chunkHeader;
	}

	public byte[] buffer;
	
	public ChunkHeader chunkHeader; 

}
