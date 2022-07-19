package org.pamguard.x3.sud;

import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;


/**
 * 
 * Expands .sud files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudFileExpander {

	private File file; 

	/**
	 * The folder to save suf files to. 
	 */
	private File outFolder;

	/**
	 * The name of the output file. If null then the sud file name is used. 
	 */
	private String outName = null; 

	/**
	 * Progress listeners. 
	 */
	private ArrayList<SudProgressListener> progressListeners = new ArrayList<SudProgressListener>(); 

	/*
	 * Data handlers for processing individual chunks of data.
	 */
	private  HashMap<Integer, IDSudar>  dataHandlers = new   HashMap<Integer, IDSudar>();


	public SudFileExpander(File file) {
		this.file = file; 
	}

	/**
	 * Process a single .sud file.
	 * @param file - the file to processed 
	 * @throws IOException 
	 */
	public void processFile(File file) throws IOException {


		//create input stream to read the binary data.
		DataInput bufinput;
				bufinput = new SudDataInputStream(new FileInputStream(file));
//		bufinput = new DataInputStream(new FileInputStream(file));



		//		bufinput.
		//		int nbytes = bufinput.available();


		SudHeader sudHeader = SudHeader.deSerialise(bufinput);
		System.out.println(sudHeader.toHeaderString());
		//		System.out.println("Bytes read: " + (nbytes-bufinput.available()));

		dataHandlers.clear();

		/**
		 * Initially only one data handler is added - the xml datya handler
		 * writes xml metadata to a file but also adds other data handlers to dataHandlers 
		 * depending on the metadata in the file. The main reason this needs to be done is that
		 * the file defines which chunkID corresponds to which data handler. 
		 */
		XMLFileHandler xmlHandler = new XMLFileHandler(file, outFolder, outName, dataHandlers); 
		xmlHandler.init(bufinput, "", 0);

		dataHandlers.put(0, new IDSudar(xmlHandler)); 


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
		Iterator<Integer> keySet = dataHandlers.keySet().iterator();
		while (keySet.hasNext()) {
			dataHandlers.get(keySet.next()).dataHandler.close();
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

		if (aHandler==null) return; 

		if (aHandler.srcID > 0 ) {
			//if the srcID > 0 there may be another data handler that needs to be used first. This will 
			//recursively process data through all data handlers up until the srcID is zero. 
			processChunk(aHandler.srcID,  sudChunk); //recursive
		}
		try {
			aHandler.dataHandler.processChunk(sudChunk);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	

}

