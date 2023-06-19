package org.pamguard.x3.sud;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * 	Handles XML chunks in the .sud file. The XML chunks define the ID of the other chunks i.e. what the chunk IDs mean and 
 *  how to parse them. 
 *  
 *  
 * @author Jamie Macaulay
 *
 */
public class XMLFileHandler implements ISudarDataHandler  {
	
	public static String XML_FILE_SUFFIX = "xml"; 
	
	private int[] chunkIds;
	
	/**
	 * The file name. 
	 */
	private File fileName;

	/**
	 * The buffer input stream.
	 */
	private LogFileStream logStream;

	/**
	 * The data handlers. 
	 */
	private HashMap<Integer, IDSudar>  dataHandlers;
	
	/**
	 * A string enum to define the handler
	 */
	private String ftype = "xml";
	
	/**
	 * True to save the xml file whenever a sud is read. 
	 */
	private boolean saveMeta = true; 
	
	private SudParams sudarParams; 

	public XMLFileHandler(SudParams sudParams, HashMap<Integer, IDSudar> dataHandlers) {
		this.dataHandlers = dataHandlers;
		this.sudarParams=sudParams;
		this.saveMeta=sudParams.isFileSave(new ISudarKey(ISudarDataHandler.XML_FTYPE, XML_FILE_SUFFIX)); 
	}

	@Override
	public void processChunk(Chunk subChunk) throws UnsupportedEncodingException {
		swapEndian(subChunk.buffer); 

		String xml = new String(subChunk.buffer, "UTF-8");
		
		//System.out.println(subChunk.chunkHeader.toHeaderString() + xml.length());
		
		
//		for (int i=0; i<xml.length(); i++) {
//			System.out.println(i + "  " +xml.charAt(i)); 
//		}
//		System.out.println("XML Arrive length " + subChunk.buffer.length);
//		if (subChunk.buffer.length > 1000) {
//
//			System.out.println("XML Arrive length " + subChunk.buffer.length);
//		}
		//System.out.println(xml);
		
		//save to the log file. 
		if (this.saveMeta) {
			logStream.print(xml);
		}

		//very important t o use trim or else throws an error
		if (xml == null) {
			return;
		}
		Document doc = convertStringToXMLDocument(xml.trim());
		if (doc == null) {
			/*
			 *  sometimes the doc is coming out as null which seems to happen when 
			 *  the XML isn't actually XML data but seems to be numeric data.   
			 */
			return;
		}

//		System.out.println(doc.getDocumentElement().toString());
//		System.out.println(doc.getDocumentElement().getChildNodes().item(1).getNodeName());
//		System.out.println(doc.getDocumentElement().getChildNodes().item(1).getAttributes().item(0));
		
		int srcID = 0; 
		NodeList nodeListSrc = doc.getElementsByTagName("SRC"); 
		if(nodeListSrc!=null && nodeListSrc.getLength() > 0) {
			for (int i=0; i<nodeListSrc.getLength(); i++) {
				Node id = nodeListSrc.item(i).getAttributes().getNamedItem("ID");
				srcID = Integer.valueOf(id.getNodeValue().trim()); 
			}
		}
		//System.out.println("nodeListSrc: " + nodeListSrc.getLength() + " ID: "+	nodeListSrc.item(0).getAttributes().getNamedItem("ID")); 

		//System.out.println("node len XML: " + nodeList.getLength()); 	
		
		NodeList nodeList = doc.getElementsByTagName("CFG"); 
		if(nodeList!=null && nodeList.getLength() > 0) {
			for (int i=0; i<nodeList.getLength(); i++) {
				
				
				//System.out.println("node len XML: " + nodeList.item(i).getAttributes().getLength()); 
//				System.out.println("CODEC: " + nodeList.item(i).getAttributes().getNamedItem("CODEC").getNodeValue()); 
//				System.out.println("SUFFIX: " + nodeList.item(i).getAttributes().getNamedItem("SUFFIX").getNodeValue()); 
				
				Node ftype = nodeList.item(i).getAttributes().getNamedItem("FTYPE");
				Node id = nodeList.item(i).getAttributes().getNamedItem("ID");
				//Node srcid = nodeList.item(i).getAttributes().getNamedItem("CODEC");
				//Node suffix = nodeList.item(i).getAttributes().getNamedItem("SUFFIX");
				
				if(ftype != null && id != null && Integer.valueOf(id.getNodeValue()) != 0) {
					try {
						ISudarDataHandler handler = ISudarDataHandler.createHandler(ftype.getNodeValue(), sudarParams);
						handler.init(logStream, xml, i);
						
						IDSudar idSudar = new IDSudar(); 
						idSudar.iD = Integer.valueOf(id.getNodeValue().trim()); 
						idSudar.dataHandler = handler; 
						idSudar.srcID = srcID;
						
						//System.out.println("IDSudar: ID" + idSudar.iD + " " + idSudar.srcID); 
						
						dataHandlers.put(idSudar.iD , idSudar);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					
				}

//				XmlAttribute ftype = n.Attributes["FTYPE"];
//				XmlAttribute id = n.Attributes["ID"];
//				XmlAttribute srcid = n.Attributes["CODEC"];
//				XmlAttribute suffix = n.Attributes["SUFFIX"];
//				if(ftype != null && id != null && Convert.ToInt32(id.Value) != 0) {
//					DataConfig d = new DataConfig();
//					d.id = Convert.ToInt32(id.Value);
//					d.ftype = ftype.Value;
//					if(srcid != null)
//						d.srcId = Convert.ToInt32(srcid.Value);
				
//					d.handler = SudarDataHandlerHelper.CreateHandler(d.ftype, fileName);
//					d.handler.Init(log, n.OuterXml, d.id);
//					configs.Add(d.id, d);
//				}
			}
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}



	/**
	 * Pairwise byte swap, i.e. change endianness of int16's. Won't work for 
	 * anything else.
	 * @param data
	 */
	public static void swapEndian(byte[] data)
	{
		for (int i = 0; i < data.length; i += 2)
		{
			byte b = data[i];
			if (i+1<data.length) {
				data[i] = data[i + 1];
				data[i + 1] = b;
			}
		}
	}


	/**
	 * Parse an xml string. 
	 * @param xmlString - the xml string to parse
	 * @return the xml document. 
	 */
	public static Document convertStringToXMLDocument(String xmlString) {
		//Parser that produces DOM object trees from XML content
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		//API to obtain DOM Document instance
		DocumentBuilder builder = null;
		try
		{
			//Create DocumentBuilder with default configuration
			builder = factory.newDocumentBuilder();

			//Parse the content to Document object
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
			return doc;
		} 
		catch (Exception e) 
		{
//			e.printStackTrace();
			System.err.println("SUD XMLFileHAndler invalid XML string extracted from file: String start is  \""
					+ xmlString.substring(0, 30) + "\" ...");
		}
		return null;
	}



	@Override
	public void init(LogFileStream bufinput2, String innerXml, int id) {
		this.logStream = bufinput2;
		
		this.chunkIds = new int[]{id};
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
		return ftype;
	}





}
