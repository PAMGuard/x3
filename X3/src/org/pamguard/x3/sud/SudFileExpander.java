package org.pamguard.x3.sud;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;


/**
 * 
 * Expands .sud files and saves the raw .wav files, .csv files .xml and other files to a folder. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudFileExpander {

	/**
	 * The current .sud file. 
	 */
	private File file; 

	/**
	 * The name of the output file. If null then the .sud file name is used. 
	 */
	private String outName = null; 

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



	public SudFileExpander(File file) {
		this.file = file; 
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
		XMLFileHandler xmlHandler = new XMLFileHandler(file, sudParams.saveFolder == null ? null : new File(sudParams.saveFolder), outName, dataHandlers); 
		
		//TODO - add out folder. 
		String logFileName = FilenameUtils.removeExtension(file.getName()) + ".log.xml";

		logFile = new LogFileStream(logFileName);
		
		xmlHandler.init(logFile, "", 0);

		dataHandlers.put(0, new IDSudar(xmlHandler)); 
		
		return sudHeader; 
	}


	public SudHeader openSudFile(File file) throws IOException {
		
		//create input stream to read the binary data.
		bufinput = new SudDataInputStream(new FileInputStream(file));

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

					//System.out.println(count + ": Read chunk data: " + chunkHeader.ChunkId + " n bytes: " + chunkHeader.DataLength);

					byte[] data = new byte[chunkHeader.DataLength];
					bufinput.readFully(data);
//					byte[] data = bufinput.readNBytes(chunkHeader.DataLength); 

					//process the chunk
					processChunk(chunkHeader.ChunkId, new Chunk(data, chunkHeader));
					
//					//TEMP TEMP TEMP to just grab the first x3 file
					//if (chunkHeader.ChunkId==3 && count>22) return;
				}

			}
			catch (EOFException eof) {
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
		
		logFile.close();
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

		if (aHandler==null) return; 

		if (aHandler.srcID > 0 ) {

			//if the srcID > 0 there may be another data handler that needs to be used first. This will 
			//recursively process data through all data handlers up until the srcID is zero. 
			processChunk(aHandler.srcID,  sudChunk); //recursive
			

		}
		try {
			for (int i=0; i<this.sudFileListener.size(); i++) {
				sudFileListener.get(i).chunkProcessed(chunkId, sudChunk); 
			}
			
			aHandler.dataHandler.processChunk(sudChunk);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the string name for the chunk ID. Note that a .sud file can have
	 * different numbers and versions of data handlers and so the chunkID is not
	 * unique between files. The chunk string is unique.
	 * <p>
	 * Note: this function call only be called after processChunk has been called.
	 * 
	 * @param chunkID
	 * @return the string name of the handler associated with the chunkID or null if
	 *         there is no handler associated with the chunkID. 
	 */
	public String getChunkIDString(int chunkID) {
		IDSudar aHandler = dataHandlers.get(chunkID); 

		if (aHandler!=null) {
			return aHandler.dataHandler.getHandlerType(); 
		}
		else {
			return null; 
		} 

	}

	/**
	 * Process a single .sud file.
	 * @param file - the file to processed 
	 * @throws IOException 
	 */
	public void processFile() throws IOException {
		processFile(file); 
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
		return file;
	}

	
	/**
	 * Set the current .sud file
	 * @param file - the .sud file. 
	 */
	public void setSudFile(File file) {
		this.file = file;
	}

	/**
	 * The sud data input stream. 
	 * @return the input stream. 
	 */
	public SudDataInputStream getSudInputStream() {
		return this.bufinput;
	}

	/**
	 * Get the data handlers for the current sud file. The data handlers are classes that
	 * process different chunks. A .sud file may have one or more data handlers. 
	 * @return the data handler map. 
	 */
	public HashMap<Integer, IDSudar> getDataHandlers() {
		return this.dataHandlers;
	}

	/**
	 * Get the parameters for extracting the .sud file. This contains options such as whether to zeroPad, where and if to save files etc.
	 * @return the parameters class that holds settings.
	 */
	public SudParams getSudParams() {
		return this.sudParams;
	}

	public void resetInputStream() throws IOException {
		bufinput = new SudDataInputStream(new FileInputStream(file));
	}


}

