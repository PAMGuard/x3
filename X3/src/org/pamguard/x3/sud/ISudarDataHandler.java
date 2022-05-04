package org.pamguard.x3.sud;

import java.io.BufferedInputStream;

public interface ISudarDataHandler {
	
	void processChunk(ChunkHeader ch, byte[] buf);

	
	void close(); 
	
	
	void init(BufferedInputStream inputStream, String innerXml, int id);
	
	
	static public ISudarDataHandler createHandler(String ftype, String filePath) throws FileFormatNotSupportedException {
		switch(ftype.toLowerCase()) {
				case "x3v2": return new X3Handler(filePath);
				case "wav": return new WavFileHandler(filePath);
				case "txt": return new TxtFileHandler(filePath);
				case "csv": return new CsvFileHandler(filePath);
				default : throw new FileFormatNotSupportedException(String.format("FType %s not supported", ftype));
		}
	}

}
