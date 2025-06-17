package org.pamguard.x3.sud;


/**
 * Interface for decoding a single chunk of a .sud file. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface ISudarDataHandler {
	
	
	public static final String X3_FTYPE 	= "x3v2"; 
	public static final String WAV_FTYPE 	= "wav"; 
	public static final String TXT_FTYPE 	= "txt"; 
	public static final String CSV_FTYPE 	= "csv"; 
	public static final String XML_FTYPE 	= "xml"; 

	
	/**
	 * Process a chunk for the specific handler.
	 * @param sudChunk - class which contains the chunk header and the data.
	 * @throws Exception - might be triggered for example if writing a wav file
	 */
	public void processChunk(Chunk sudChunk) throws Exception;

	/**
	 * Called whenever it is time to close the file
	 */
	public void close(); 
	
	/**
	 * Initialise the file handler. The start of the sud files contains xml chunks that define which handlers are
	 * needed by the file. init(...) is called with the xml info from the initial chunk which contains metadata such as 
	 * sample rate, number of channels etc. 
	 * 
	 * @param logFile - the log file. 
	 * @param innerXml - the xml data for the chunk type. 
	 * @param id - the id fo the chunks for this handler. 
	 */
	public void init(LogFileStream logFile, String innerXml, int id);
	

	/**
	 * Get the handler type. 
	 * @return the handler type. 
	 */
	public String getHandlerType();
	
	/**
	 * Get the type of output file. Can be null. 
	 * @return the handler type. 
	 */
	public String getFileType();
	

	
	static public ISudarDataHandler createHandler(String ftype, SudParams sudFileData) throws FileFormatNotSupportedException {
		//System.out.println("Create handler: " + ftype);
		switch(ftype.toLowerCase()) {
				case X3_FTYPE: return new X3Handler(sudFileData,  ftype); 
				case WAV_FTYPE: return new WavFileHandler(sudFileData, ftype);
				case TXT_FTYPE: return new TxtFileHandler(sudFileData, ftype);
				case CSV_FTYPE: return new CsvFileHandler(sudFileData, ftype);
				default : throw new FileFormatNotSupportedException(String.format("FType %s not supported", ftype));
		}
	}
	


	/**
	 * Get the chunk IDs.
	 * @return the chunk IDs associated with this handler. 
	 */
	int[] getChunkID();

}
