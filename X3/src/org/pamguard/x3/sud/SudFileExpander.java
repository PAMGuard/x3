package org.pamguard.x3.sud;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.pamguard.x3.x3.CRC16;


/**
 * 
 * Expands .sud files and saves the raw .wav files, .csv files .xml and other files to a folder. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudFileExpander {

	/**
	 * The chunk ID for xml files. 
	 */
	private static final int XML_CHUNK_ID = 0;

	/**
	 * Progress listeners. 
	 */
	private ArrayList<SudFileListener> sudFileListener = new ArrayList<SudFileListener>(); 

	/*
	 * Data handlers for processing individual chunks of data.
	 */
	private  HashMap<Integer, IDSudar>  dataHandlers = new   HashMap<Integer, IDSudar>();

	/**
	 * The data input stream
	 */
	private SudDataInputStream bufinput;

	/**
	 * The log file stream. 
	 */
	private LogFileStream logFile;
	
	/**
	 * The current sud parameters. 
	 */
	private SudParams sudParams = new SudParams();


	public SudFileExpander(File file, SudParams sudParams) {
		this.sudParams = sudParams.clone(); 
		this.sudParams.setSudFilePath(file.getAbsolutePath());
	}
	
	public SudFileExpander(File sudFileIn) {
		this(sudFileIn, new SudParams()); 
	}

	public SudHeader openSudFile(SudDataInputStream bufinput) throws IOException {
		//		int nbytes = bufinput.available();

		SudHeader sudHeader = SudHeader.deSerialise(bufinput);
		//System.out.println(sudHeader.toHeaderString());
		//		System.out.println("Bytes read: " + (nbytes-bufinput.available()));

		dataHandlers.clear();

		/**
		 * Initially only one data handler is added - the xml data handler
		 * writes xml metadata to a file but also adds other data handlers to dataHandlers 
		 * depending on the metadata in the file. The main reason this needs to be done is that
		 * the file defines which chunkID corresponds to which data handler. 
		 */
		XMLFileHandler xmlHandler = new XMLFileHandler(sudParams, dataHandlers); 
		
		//TODO - add out folder. 
		String logFileName = (sudParams.getOutFilePath() + ".log.xml");

		if (sudParams.isFileSave(XMLFileHandler.XML_FILE_SUFFIX)) {
			logFile = new LogFileStream(logFileName);
		}

		xmlHandler.init(logFile, "", XML_CHUNK_ID);
		

		dataHandlers.put(0, new IDSudar(xmlHandler)); 
		
		return sudHeader; 
	}


	public SudHeader openSudFile(File file) throws IOException {
		
		//create input stream to read the binary data.
		bufinput = new SudDataInputStream(new BufferedInputStream(new FileInputStream(file)));

		return openSudFile(bufinput); 
	}

	/**
	 * Process a single .sud file. This will process the entire file. 
	 * @param file - the file to processed . 
	 * @throws IOException 
	 */
	public void processFile(File file) throws IOException {


		openSudFile(file);


		ChunkHeader chunkHeader;
		int count = 0; 
		while(true){
			try {
				chunkHeader = ChunkHeader.deSerialise(bufinput);

				if (chunkHeader.checkId()) {
					//				System.out.println("--------------");
					//				System.out.println(chunkHeader.toHeaderString());
					count++;
					if (sudParams.isVerbose()) {
						System.out.println(count + ": Read chunk data: " + chunkHeader.ChunkId + " n bytes: " + chunkHeader.DataLength);
					}

					byte[] data = new byte[chunkHeader.DataLength];
					bufinput.readFully(data);
//					byte[] data = bufinput.readNBytes(chunkHeader.DataLength); 
					int crc = CRC16.calcSUD(data, chunkHeader.DataLength);
					if (crc != chunkHeader.DataCrc) {
						System.out.println("Bad data CRC");
						continue;
					}


//					//process the chunk
//					if (chunkHeader.ChunkId == 0) {
//						System.out.printf("Chunk id 0 ");
//					}
					processChunk(chunkHeader.ChunkId, new Chunk(data, chunkHeader));
					
					
//					//TEMP TEMP TEMP to just grab the first x3 file
					//if (chunkHeader.ChunkId==3 && count>22) return;
				}
				else {
					bufinput.skipBytes(chunkHeader.DataLength);
				}

			}
			catch (EOFException eof) {
				System.out.println("End of .sud file EOF");
				break;
			}
		}
		
		//close everything. 
		closeFileExpander();
	}
	
	
	/**
	 * Close the file expander, saving any current log, data and/or audio files. 
	 */
	public void closeFileExpander() {
		//close everything. 
		Iterator<Integer> keySet = dataHandlers.keySet().iterator();
		while (keySet.hasNext()) {
			dataHandlers.get(keySet.next()).dataHandler.close();
		}
		
		if (logFile != null) {
			logFile.close();
		}
	}


	/**
	 * Process a chunk of data
	 * @param chunkId - the chunk ID
	 * @param ch - the chunk header
	 * @param buf - the data. 
	 */
	void processChunk(int chunkId, Chunk sudChunk) {
		//if (chunkId!=0) {
		//does the data handler contain the chunkID?
		//boolean contains = IntStream.of(dataHandler.getChunkID()).anyMatch(x -> x == chunkId);
		IDSudar aHandler = dataHandlers.get(chunkId); 
		
		//System.out.println("Process data handler: "  + aHandler.iD + " " + aHandler.srcID + (aHandler==null)); 

		if (aHandler==null) {
			return; 
		}

		if (aHandler.srcID > 0 ) {

			//if the srcID > 0 there may be another data handler that needs to be used first. This will 
			//recursively process data through all data handlers up until the srcID is zero. 
			processChunk(aHandler.srcID,  sudChunk); //recursive
			

		}
		try {
			for (int i=0; i<this.sudFileListener.size(); i++) {
				sudFileListener.get(i).chunkProcessed(chunkId,  sudChunk); 
			}
			
			aHandler.dataHandler.processChunk(sudChunk);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Check whether a chunk ID is an uncompressed chunk of wav data from CONTINUOUS
	 * recordings (there is also uncompressed wav data from click detections but this will return false)
	 * 
	 * @return true if the chunkID contains uncompressed wav data from continuous or
	 *         duty samples recordings.
	 */
	public boolean isChunkIDWav(int chunkID) {
		return isChunkIDWavint(chunkID,  "wav"); 
	}
	
	/**
	 * Check whether a chunk ID is an uncompressed chunk of click data.
	 * 
	 * @return true if the chunkID contains uncompressed wav data from click detections. 
	 */
	public boolean isChunkIDDwv(int chunkID) {
		return isChunkIDWavint(chunkID,  "dwv");
	}
	
	/**
	 * Check whether a chunk is uncompressed wav data. 
	 * @param chunkID - the chunk to check.
	 * @param fileSuffix - the type of wav data ("wav" for continuous recordings, "dwv" for click detection waveforms)
	 * @return true if the chunkID is wav data of the right type. 
	 */
	private boolean isChunkIDWavint(int chunkID, String fileSuffix){
		String strId = getChunkFileType(chunkID);
		if (strId == null) {
			System.out.println("Unknown SUD chunk id in getChunkFileType(): " + chunkID);
			return false;
		}
		if (strId.equals("wav")) {
			WavFileHandler wavHandler = (WavFileHandler) getChunkDataHandler(chunkID).dataHandler;
			if (wavHandler.getFileSuffix().equals(fileSuffix))
				return true;
			else
				return false; // probably dwv file for click detections.
		} else
			return false;
	}
	
	/**
	 * Get the string name for the chunk ID. Note that a .sud file can have
	 * different numbers and versions of data handlers and so the chunkID is not
	 * unique between files. The chunk string is unique.
	 * <p>
	 * Note: this function call only be called after processChunk has been called.
	 * 
	 * @param chunkID - the ID of the chunk.
	 * @return the string file type name of the handler associated with the chunkID or null if
	 *         there is no handler associated with the chunkID. 
	 */
	public String getChunkFileType(int chunkID) {
		IDSudar aHandler = dataHandlers.get(chunkID); 

		if (aHandler!=null) {
			return aHandler.dataHandler.getHandlerType(); 
		}
		else {
			return null; 
		} 
	}
	
	/**
	 * Get the data handler instance for a chunkID 
	 * @param chunkID - the ID of the chunk. 
	 * @return the data handler instance to process the chunk. 
	 */
	public  IDSudar getChunkDataHandler(int chunkID) {
		return dataHandlers.get(chunkID); 
	}

	/**
	 * Process a single .sud file.
	 * @param file - the file to processed 
	 * @throws IOException 
	 */
	public void processFile() throws IOException {
		processFile(new File(sudParams.getSudFilePath())); 
	}
	
	/**
	 * Add a file listener to the sud file expander. 
	 * @param sudFileListener - the file listener to add. 
	 */
	public void addSudFileListener(SudFileListener sudFileListener) {
		this.sudFileListener.add(sudFileListener);
	}
	
	/**
	 * Remove a sud file listener. 
	 * @param sudFileListener - the sudFileListener to remove.
	 * @return true if the SudFileListener was removed. 
	 */
	public boolean removeSudFileListener(SudFileListener sudFileListener) {
		return this.sudFileListener.remove(sudFileListener);
	}
	
	/**
	 * Get the current .sud file. 
	 * @return the current sud file
	 */
	public File getSudFile() {
		return new File(this.sudParams.getSudFilePath());
	}

	
	/**
	 * Set the current .sud file
	 * @param file - the .sud file. 
	 */
	public void setSudFile(File file) {
		sudParams.setSudFilePath(file.getAbsolutePath());
	}

	/**
	 * The sud data input stream. 
	 * @return the input stream. 
	 */
	public SudDataInputStream getSudInputStream() {
		return this.bufinput;
	}

	/**
	 * Get the data handlers for the current sud file. The data handlers are classes
	 * that process different chunks. A .sud file may have one or more data
	 * handlers.
	 * 
	 * @return the data handler map.
	 */
	public HashMap<Integer, IDSudar> getDataHandlers() {
		return this.dataHandlers;
	}

	/**
	 * Get the parameters for extracting the .sud file. This contains options such
	 * as whether to zeroPad, where and if to save files etc.
	 * 
	 * @return the parameters class that holds settings.
	 */
	public SudParams getSudParams() {
		return this.sudParams;
	}
	
	/**
	 * Set the parameters for extracting the .sud file. This contains options such
	 * as whether to zeroPad, where and if to save files etc.
	 * 
	 * @param the parameters class that holds settings.
	 */
	public void setSudParams(SudParams sudParams) {
		this.sudParams = sudParams;
	}

	public void resetInputStream() throws IOException {
		bufinput = new SudDataInputStream(new FileInputStream(sudParams.getSudFilePath()));
	}


}

