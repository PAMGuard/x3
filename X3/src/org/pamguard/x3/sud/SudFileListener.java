package org.pamguard.x3.sud;

/**
 * Listener for sud files. 
 * @author Jamie Macaulay
 *
 */
public interface SudFileListener {
	
	/**
	 * Called whenever a chunk is processed. 
	 * @param chunkID - the chunkID used buy the file expander. 
	 * @param sudChunk - the sudChunk containing the data before it is processed
	 */
	void chunkProcessed(int chunkId, Chunk sudChunk);

}
