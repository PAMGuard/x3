package org.pamguard.x3.sud;

/**
 * Listener for creating sudx map files
 * @author Jamie Macaulay
 *
 */
public interface SudMapListener {
	
	
	/**
	 * Called whenever a chunk is processed. 
	 * @param chunkHeader - the current chunk header processed for the map - can be null
	 * @param count - count - the count of the chunk, 0 indicates the map has started. -1 Indicates the map has finished. All other numbers iniciate the total number of chunks processed
	 */
	void chunkProcessed(ChunkHeader chunkHeader, int count);

	
	

}
