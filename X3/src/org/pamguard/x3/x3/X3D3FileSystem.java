package org.pamguard.x3.x3;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.pamguard.x3.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

//import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

/**
 * X3 file reading and writing functions which follow Marks D3 standard X3 files. 
 * @author Doug Gillespie
 *
 */
public class X3D3FileSystem extends X3FileSystem {

	public X3D3FileSystem() {
		// TODO Auto-generated constructor stub
	}
	
    public String readFileHeader(DataInputStream dis) throws IOException {
		X3FrameHeader frameHeader = new X3FrameHeader();
		String xmlString = null;
		if (!frameHeader.readHeader(dis)) {
			return null;
		};

		byte[] x3Data = new byte[frameHeader.getnBytes()];
		dis.read(x3Data);
		xmlString = new String(x3Data, "US-ASCII");
		
		return xmlString;
    }

	@Override
	public X3FileHeader decodeFileHeader(String xmlString) {

//		System.out.println(xmlString);
//		// now convert to an xml document and decode ...
//		Document doc = convertStringToDocument(xmlString.trim());
//		Node mainNode = doc.getFirstChild();
//		NodeList kids = mainNode.getChildNodes();
//		int n = kids.getLength();
//		for (int i  = 0;i < n; i++) {
//			System.out.println(kids.item(i).getNodeName());
//		}
//		doc.
		// for now just cheat and grab the nodes we want out of the xml string
		X3FileHeader fileHeader = new X3FileHeader();
		fileHeader.blockLen = findXMLInteger(xmlString, "BLKLEN");
		fileHeader.sampleRate = findXMLInteger(xmlString, "FS");
		fileHeader.nChannels = findXMLInteger(xmlString, "NCHS");
//		fileHeader.startTimeMillis = findXMLInteget(xmlString, "TIME");
		
		Document doc = convertStringToDocument(xmlString.trim());
		Node mainNode = doc.getFirstChild(); //<CONFIG>
		
		HashMap<String, String> childrenNeeded = new HashMap<String, String>();
		childrenNeeded.put("BLKLEN", null);
		childrenNeeded.put("FS", null);
		childrenNeeded.put("NCHS", null);
		
		ArrayList<Node> possibleNodesToGetAllFields = XMLUtils.getNodesWithChildren(XMLUtils.getNodeArrayList(XMLUtils.getNodeArray(mainNode.getChildNodes())), childrenNeeded);
		
		if (possibleNodesToGetAllFields.size()==1){
			ArrayList<Node> correctCfgNodeChildren = XMLUtils.getNodeArrayList(XMLUtils.getNodeArray(possibleNodesToGetAllFields.get(0).getChildNodes()));
//			ArrayList<Node> nodes = getNodesOfName(correctCfgNodeChildren, "BLKLEN");
//			Node blkNode = nodes.get(0);
//			String val = blkNode.getTextContent();//getNodeValue();
//			Integer iVal = Integer.valueOf(val);
			
			fileHeader.blockLen = Integer.valueOf(XMLUtils.getNodesOfName(correctCfgNodeChildren, "BLKLEN").get(0).getTextContent());
			fileHeader.sampleRate = Integer.valueOf(XMLUtils.getNodesOfName(correctCfgNodeChildren, "FS").get(0).getTextContent());
			fileHeader.nChannels = Integer.valueOf(XMLUtils.getNodesOfName(correctCfgNodeChildren, "NCHS").get(0).getTextContent());
		}
		return fileHeader;
	}
	
	private Integer findXMLInteger(String xmlDoc, String tagName) {
		String str = findTag(xmlDoc, tagName);
		if (str == null) {
			return null;
		}
		try {
			return Integer.valueOf(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * find the first instance of a tag in an xml document 
	 * and extract the tag's string. 
	 * @param xmlDoc
	 * @param tagName
	 * @return Tag string or null
	 */
	String findTag(String xmlDoc, String tagName) {
		int tagStart = xmlDoc.indexOf(tagName);
		if (tagStart == -1) return null;
		tagStart = xmlDoc.indexOf('>', tagStart);
		if (tagStart == -1) return null;
		int tagEnd = xmlDoc.indexOf('<', tagStart);
		if (tagEnd == -1) return null;
		String elStr = xmlDoc.substring(tagStart+1, tagEnd);
		return elStr;
	}

	@Override
	public X3FrameHeader readFrameHeader(DataInputStream dis, X3FileHeader x3FileHeader, X3FrameHeader exHeader) throws IOException {
		if (exHeader == null) {
			exHeader = new X3FrameHeader();
		}
		if (!exHeader.readHeader(dis)) {
			return null;
		}
		else {
			return exHeader;
		}
	}

	/* (non-Javadoc)
	 * @see x3.X3FileSystem#createX3HeaderXML(int, int, int)
	 */
	@Override
	public Document createX3HeaderXML(int blockLength, int sampleRate,
			int nChannels) {
//		Document doc = new DocumentImpl();
		Document doc = X3Common.createBlankDoc();
		Element root = doc.createElement("CONFIG");
		doc.appendChild(root);
		Element el;

		Element cfg;
		cfg = makeCfgElement(doc, 1, null, sampleRate, nChannels, "AUDIO", null, null);
		root.appendChild(cfg);
		
		cfg = makeCfgElement(doc, 2, 1, sampleRate, nChannels, "X3V2", "X3V2", null);
		root.appendChild(cfg);
		addNode(doc, cfg, "BLKLEN", String.format("%d", X3FrameEncode.blockSamples));
		addNode(doc, cfg, "FILTER", "diff");
		int[] th = X3FrameEncode.riceThresholds;
		for (int i = 0; i < th.length; i++) {
			el = addNode(doc, cfg, "CODE", String.format("RICE%d", X3FrameEncode.riceOrders[i]));
			el.setAttribute("THRESH", String.format("%d", th[i]));
		}
		addNode(doc, cfg, "CODE", "BFP");
		
		cfg = makeCfgElement(doc, 3, 2, sampleRate, nChannels, null, "wav", 2);
		root.appendChild(cfg);
		addNode(doc, cfg, "SUFFIX", "wav");
		
		
		
		
		return doc;
	}
	
	/**
	 * Make a standard elementn which can then be modified to include
	 * module specific information
	 * @param doc Document
	 * @param iD module id
	 * @param src modules source id (can be null)
	 * @param sampleRate sample rate
	 * @param nChannels number of channels
	 * @param proc Process name (can be null) 
	 * @param fType Module file type (can be null)
	 * @param codec Module CODEC (can be null)
	 * @return A standard xml element with module information. 
	 */
	private Element makeCfgElement(Document doc, int iD, Integer src, int sampleRate, int nChannels, String proc, String fType, Integer codec) {
		Element cfg = doc.createElement("CFG");
		cfg.setAttribute("ID", String.format("%d", iD));
		if (proc != null) {
			cfg.setAttribute("PROC", proc);
		}
		if (fType != null) {
			cfg.setAttribute("FTYPE", fType);
		}
		if (codec != null) {
			cfg.setAttribute("CODEC", codec.toString());
		}
		Element el;
		if (src != null) {
			el = addNode(doc, cfg, "SRC", null);
			el.setAttribute("ID", String.format("%d", src));
		}
		addNode(doc, cfg, "PROC", "AUDIO");
		int chanMap = 0;
		for (int i = 0; i < nChannels; i++) {
			chanMap |= 1<<i;
		}
		addNode(doc, cfg, "CHANBITMAP", chanMap);
		addNode(doc, cfg, "NCHS", nChannels);
		el = addNode(doc, cfg, "FS", sampleRate);
		el.setAttribute("UNIT", "Hz");
		addNode(doc, cfg, "NBITS", 16);
		return cfg;
	}

	/**
	 * Add an integer valued node to an XML Element
	 * @param doc Document
	 * @param parent Parent element
	 * @param name Name of element to add
	 * @param value Value of element to add
	 * @return New Element. 
	 */
	public Element addNode(Document doc, Element parent, String name, int value) {
		return addNode(doc, parent, name, String.format("%d", value));
	}
	
	/**
	 * 
	 * Add a String valued node to an XML Element
	 * @param doc Document
	 * @param parent Parent element
	 * @param name Name of element to add
	 * @param value String value of element to add
	 * @return New Element. 
	 */
	public Element addNode(Document doc, Element parent, String name, String value) {
		Element el = doc.createElement(name);
		el.setTextContent(value);
		parent.appendChild(el);
		return el;
	}

}
