package org.pamguard.x3.sud;

import java.io.DataInput;
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

import com.google.common.io.LittleEndianDataInputStream;


/**
 * 	Handles XML chunks in the .sud file. The XML chunks define the ID of the other chunks i.e. what the chunk IDs mean and 
 *  how to parse them. The XML
 * @author Jamie Macaulay
 *
 */
public class XMLFileHandler implements ISudarDataHandler  {
	
	private int[] chunkIds;
	
	/**
	 * The file name. 
	 */
	private File fileName;

	/**
	 * The buffer input stream.
	 */
	private DataInput bufInput;

	private HashMap<Integer, IDSudar>  dataHandlers;
	

	public XMLFileHandler(File sourceFile, File outFolder, String outName, HashMap<Integer, IDSudar> dataHandlers) {
		this.fileName = sourceFile;
		this.dataHandlers = dataHandlers;
	}

	@Override
	public void processChunk(ChunkHeader ch, byte[] buf) throws UnsupportedEncodingException {
		swapEndian(buf); 

		String xml = new String(buf, "UTF-8");
		
		System.out.println(xml);

		//very important t o use trim or else throws an error
		Document doc = convertStringToXMLDocument(xml.trim());

//		System.out.println(doc.getDocumentElement().toString());
//		System.out.println(doc.getDocumentElement().getChildNodes().item(1).getNodeName());
//		System.out.println(doc.getDocumentElement().getChildNodes().item(1).getAttributes().item(0));


		NodeList nodeList = doc.getElementsByTagName("CFG"); 
		
		System.out.println("node len XML: " + nodeList.getLength()); 

		
		if(nodeList!=null && nodeList.getLength() > 0) {
			for (int i=0; i<nodeList.getLength(); i++) {
				
				System.out.println("node len XML: " + nodeList.item(i).getAttributes().getLength()); 

				

//				System.out.println("CODEC: " + nodeList.item(i).getAttributes().getNamedItem("CODEC").getNodeValue()); 
//				System.out.println("SUFFIX: " + nodeList.item(i).getAttributes().getNamedItem("SUFFIX").getNodeValue()); 
				
				Node ftype = nodeList.item(i).getAttributes().getNamedItem("FTYPE");
				Node id = nodeList.item(i).getAttributes().getNamedItem("ID");
				Node srcid = nodeList.item(i).getAttributes().getNamedItem("CODEC");
				Node suffix = nodeList.item(i).getAttributes().getNamedItem("SUFFIX");
				
				if(ftype != null && id != null && Integer.valueOf(id.getNodeValue()) != 0) {
					try {
						ISudarDataHandler handler = ISudarDataHandler.createHandler(ftype.getNodeValue(), fileName.getAbsolutePath());
						handler.init(bufInput, xml, i);
						
						IDSudar idSudar = new IDSudar(); 
						idSudar.iD = Integer.valueOf(id.getNodeValue().trim()); 
						idSudar.dataHandler = handler; 
						if (srcid!=null) {
							idSudar.srdID = Integer.valueOf(srcid.getNodeValue().trim()); 
						}
						System.out.println("IDSudar: ID" + idSudar.iD + " " + idSudar.srdID); 
						
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
			data[i] = data[i + 1];
			data[i + 1] = b;
		}
	}


	/**
	 * Parse an xml string. 
	 * @param xmlString - the xml string to parse
	 * @return 
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
			e.printStackTrace();
		}
		return null;
	}



	@Override
	public void init(DataInput bufinput2, String innerXml, int id) {
		this.bufInput = bufinput2;
		this.chunkIds = new int[]{id};
	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}


}
