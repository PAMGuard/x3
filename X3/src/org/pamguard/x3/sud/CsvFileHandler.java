package org.pamguard.x3.sud;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.apache.commons.io.FilenameUtils;
import org.pamguard.x3.utils.XMLUtils;

/**
 * Extracts data from a .sud file and writes it to a .csv file. 
 * @author Jamie Macaulay
 *
 */
public class CsvFileHandler implements ISudarDataHandler {

	private static final String CSV_FILE_SUFFIX = "csv";

	/**
	 * Chunk IDs. 
	 */
	private int[] chunkIds;  

	FileWriter sw;

	String fileSuffix = "csv";

	int id;

	String header;

	private File sudFile;

	private String fileName;

	/**
	 * A string enum to define the handler
	 */
	private String ftype;

	/**
	 * True to save the csv. 
	 */
	private boolean saveMeta; 


	public CsvFileHandler(SudParams filePath, String ftype) {
		this.sudFile = new File(filePath.getSudFilePath());
		this.fileName =filePath.getOutFilePath();
		this.ftype=ftype; 
		this.saveMeta=filePath.isFileSave(ISudarDataHandler.CSV_FTYPE, CSV_FILE_SUFFIX); 

	}

	@Override
	public void processChunk(Chunk sudChunk) throws IOException {

		if (saveMeta) {
			//System.out.println("Read CSV: " + (fileName + fileSuffix + ".csv"));
			if(sw == null) {
				sw = new FileWriter(fileName + fileSuffix + ".csv", false);
				sw.write(header);
				sw.write("\n");

			}
			//string s = Encoding.Unicode.GetString(buf);

			String s = new String(sudChunk.buffer, StandardCharsets.UTF_8);
			sw.write( s );
			sw.flush();
		}

	}

	@Override
	public String getFileType() {
		return fileSuffix;
	}


	@Override
	public void close() {
		if(sw != null) {
			try {
				sw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void init(LogFileStream inputStream, String innerXml, int id) {
		this.chunkIds = new int[]{id};

		Document doc = XMLFileHandler.convertStringToXMLDocument(innerXml.trim());

		NodeList nodeList = doc.getElementsByTagName("CFG");

		HashMap<String, String> nodeContent = XMLUtils.getInnerNodeContent(new String[] {"SUFFIX", "HEADER"},  nodeList);

		header = nodeContent.get("SUFFIX");
		fileSuffix = nodeContent.get("HEADER");

	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}

	@Override
	public String getHandlerType() {
		return ftype;
	}
	


}
