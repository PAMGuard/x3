package org.pamguard.x3.sud;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.io.LittleEndianDataInputStream;



public class XMLFileHandler implements ISudarDataHandler  {
	
	private int[] chunkIds;
	

	public XMLFileHandler(File outFolder, String outName, ArrayList<ISudarDataHandler> dataHandlers) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processChunk(ChunkHeader ch, byte[] buf) throws UnsupportedEncodingException {
		swapEndian(buf); 

		String xml = new String(buf, "UTF-8");

		Document doc = convertStringToXMLDocument(xml);

		System.out.println(doc.getChildNodes().toString());
		
		NodeList nodeList = doc.getElementsByTagName("CFG"); 
		
		if(nodeList!=null && nodeList.getLength() > 0) {
			for (int i=0; i<nodeList.getLength(); i++) {
				
				System.out.println(nodeList.item(i).getAttributes()); 
				
				
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
	private static Document convertStringToXMLDocument(String xmlString) {
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
	public void init(LittleEndianDataInputStream bufinput, String innerXml, int id) {
		this.chunkIds = new int[]{id};

	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}


}
