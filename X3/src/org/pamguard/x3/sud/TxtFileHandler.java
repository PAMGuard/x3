package org.pamguard.x3.sud;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.pamguard.x3.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * Processes a text file chunk form a sud file. 
 * <p>
 * This is used to create the bcl files which complement the .dwv files used in click detection. 
 * @author Jamie Macaulay
 *
 */
public class TxtFileHandler implements ISudarDataHandler {

	private int[] chunkIds;
	
	/**
	 * The current sud file. 
	 */
	private File sudFile;

	private String ftype;

	/**
	 * The file suffix e.g. txt, bcl etc. 
	 */
	private String fileSuffix;

	/**
	 * The filename to use. 
	 */
	private String fileName;

	/**
	 * True to save the text file. 
	 */
	private boolean saveMeta;

	private FileWriter sw;
	
	/**
	 * Reference to the sud params. 
	 */
	private SudParams sudParams;

	public TxtFileHandler(String filePath) {
		this.sudFile = new File(filePath);
	}

	public TxtFileHandler(SudParams filePath, String ftype) {
		this.ftype= ftype; 
		this.sudFile = new File(filePath.getSudFilePath());
		this.fileName =filePath.getOutFilePath();
		this.ftype=ftype; 
		this.sudParams= filePath; 
	}
	


	@Override
	public void processChunk(Chunk sudChunk) {
		
		if (saveMeta) {
			if(sw == null) {
				try {
					sw = new FileWriter(fileName + "." + fileSuffix, false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			//System.out.println("Read Txt: n bytes" + sudChunk.buffer.length);
			
			//important that we use a sud input stream here. 
			SudDataInputStream byteStream = new SudDataInputStream(new ByteArrayInputStream(sudChunk.buffer)); 
						
			try {
				while(byteStream.available()>0) {
					//System.out.println("Read Txt: " + byteStream.available());
					
					long rtime = (byteStream.readInt()) & 0xffffffffL;
					long mticks = (byteStream.readInt()) & 0xffffffffL;
					int n = byteStream.readUnsignedShort();
					
					byte[] b = new byte[(n % 2 == 1) ? n+1 : n]; 
					byteStream.read(b); 
					
					SwapEndian(b);
					
					String s = new String(b, "UTF-8");

					//System.out.println("rtime: " + rtime + " mticks: " + mticks + " string len: " + s.length() + " n: " + n);

					if((rtime ==0) && (mticks == 0)) {
						sw.write( String.format("%s\n", s));
					}
					else {
						sw.write( String.format("%d,%d,%s\n", rtime, mticks, s));
					}
				}
				sw.flush();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
//			//string s = Encoding.Unicode.GetString(buf)
//			String s = new String(sudChunk.buffer, StandardCharsets.UTF_8);
//			sw.write( s );
//			sw.flush();
		}

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
	
	int swapWords( int l ){
		return ((l & 0x0000FFFF) << 16) | ((l & 0xFFFF0000) >> 16);
	}
	
	public void SwapEndian(byte[] data) {
		 XMLFileHandler.swapEndian(data);
	}


	@Override
	public void init(LogFileStream inputStream, String innerXml, int id) {
		this.chunkIds = new int[]{id};
		
		Document doc = XMLFileHandler.convertStringToXMLDocument(innerXml.trim());

		NodeList nodeList = doc.getElementsByTagName("CFG");

		HashMap<String, String> nodeContent = XMLUtils.getInnerNodeContent(new String[] {"SUFFIX"},  nodeList);

		fileSuffix = nodeContent.get("SUFFIX");
		
		this.saveMeta = sudParams.isFileSave(new ISudarKey(ISudarDataHandler.TXT_FTYPE, fileSuffix));
		
		//System.out.println("Hello text file: " + fileSuffix + " " + saveMeta); 
	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}

	@Override
	public String getHandlerType() {
		return ftype;
	}

	@Override
	public String getFileType() {
		return fileSuffix;
	}



}
