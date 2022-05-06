package org.pamguard.x3.sud;

import com.google.common.io.LittleEndianDataInputStream;


/**
 * Interface for decoding a single chunk of a .sud file. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface ISudarDataHandler {
	
	void processChunk(ChunkHeader ch, byte[] buf) throws Exception;

	
	void close(); 
	
	
	void init(LittleEndianDataInputStream inputStream, String innerXml, int id);
	
	
	static public ISudarDataHandler createHandler(String ftype, String filePath) throws FileFormatNotSupportedException {
		switch(ftype.toLowerCase()) {
				case "x3v2": return new X3Handler(filePath);
				case "wav": return new WavFileHandler(filePath);
				case "txt": return new TxtFileHandler(filePath);
				case "csv": return new CsvFileHandler(filePath);
				default : throw new FileFormatNotSupportedException(String.format("FType %s not supported", ftype));
		}
	}


	int[] getChunkID();

}
