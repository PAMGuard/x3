package org.pamguard.x3.sud;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Functions for sorting out and getting data from SUD file xml, either in raw
 * form read from the file, or from an extracted XML file.  
 * @author dg50
 *
 */
public class SUDXMLUtils {

	public Document createDocument(String xml) {
		while (xml.charAt(xml.length()-1) == 0) {
			xml = xml.substring(0, xml.length()-1);
		};
		/*
		 *  check for zeros and replace with a space. Now that I've removed the 
		 *  NTS 0's on the ends of some of the XML parts as they arrive, this no longer
		 *  seems to be an issue, so could probably remove lines up to the point we
		 *  wrap with <ST> </ST>
		 */
		char[] chars = xml.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == 0) {
				chars[i] = ' ';
			}
		}
		xml = new String(chars);
		//		xml.replace((char) 0, ' ');
		xml = "<ST>\n" + xml +  "</ST>";
		//		System.out.println(xml);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			doc = dBuilder.parse(bis);
		} catch (ParserConfigurationException e) {
			System.out.println(String.format("Parser Error in XML file %s: %s", xml, e.getMessage()));
			return null;
		} catch (SAXException e) {
			System.out.println(String.format("SAX Error in XML file %s: %s", xml, e.getMessage()));
			return null;
		} catch (IOException e) {
			System.out.println(String.format("IO Error in XML file %s: %s", xml, e.getMessage()));
			return null;
		}
		doc.getDocumentElement().normalize();
		return doc;
	}

	/**
	 * Find a CFG element with the given procName , e.g. <PROC> CDET </PROC>
	 * @param topElement
	 * @param procName
	 * @return
	 */
	private Element findConfig(Element topElement, String procName) {
		NodeList procs = topElement.getElementsByTagName("PROC");
		for (int i = 0; i < procs.getLength(); i++) {
			Node aNode = procs.item(i);
			if (aNode instanceof Element == false) {
				continue;
			}
			String txt = aNode.getTextContent().trim();
			if (txt.equals(procName)) {
				Node parent = aNode.getParentNode();
				if (parent instanceof Element) {
					return (Element) parent;
				}
			}
		}
		return null;
	}
	/**
	 * Get SoundTrap click detector information. 
	 * @param doc
	 */
	public SUDClickDetectorInfo extractDetectorInfo(Document doc) {

		SUDClickDetectorInfo detInfo = new SUDClickDetectorInfo();
		Element docElement = doc.getDocumentElement();
		
		Element detElement = findConfig(docElement, "CDET");
		if (detElement == null) {
			detElement = docElement;
		}
		Double th = findDoubleNodeData(detElement, "DETTHR");
		if (th != null) {
			detInfo.detThr = th;
		}
		Integer preDet = findIntegerNodeData(detElement, "PREDET");
		if (preDet != null) {
			detInfo.preSamples = preDet;
		}
		Integer postDet = findIntegerNodeData(detElement, "POSTDET");
		if (postDet != null) {
			detInfo.postSamples = postDet;
		}
		Integer len = findIntegerNodeData(detElement, "LEN");
		if (len != null) {
			detInfo.lenSamples = len;
		}
		Integer bs = findIntegerNodeData(detElement, "BLANKING");
		if (bs != null) {
			detInfo.blankingSamples = bs;
		}
		
		Element snipElement;
//		= findConfig(docElement, "snip");
//		if (snipElement == null) {
			snipElement = findConfig(docElement, "AUDIO"); // take from main audio Element.
			if (snipElement == null) {
				snipElement = docElement;
			}
//		}
		Integer fs = findIntegerNodeData(snipElement, "FS");
		if (fs != null) {
			detInfo.sampleRate = fs;
		}
		Integer nCh = findIntegerNodeData(snipElement, "NCHS");
		if (nCh != null) {
			detInfo.nChan = nCh;
		}
		
		return detInfo;

	}

	/**
	 * Get data from within an element as a Double value
	 * @param docElement document element
	 * @param nodeName node name
	 * @return double, or null if non existant of wrong format
	 */
	public Double findDoubleNodeData(Element docElement, String nodeName) {
		String str = findFirstNodeData(docElement, nodeName);
		if (str == null) {
			return null;
		}
		try {
			Double val = Double.valueOf(str);
			return val;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get data from within an element as a Integer value
	 * @param docElement document element
	 * @param nodeName node name
	 * @return integer, or null if non existant of wrong format
	 */
	public Integer findIntegerNodeData(Element doc, String nodeName) {
		String str = findFirstNodeData(doc, nodeName);
		if (str == null) {
			return null;
		}
		try {
			Integer val = Integer.valueOf(str);
			return val;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get the content of the first node in the document for the given name. 
	 * @param doc
	 * @param nodeName
	 * @return
	 */
	public String findFirstNodeData(Element doc, String nodeName) {
		Node node = findFirstNode(doc, nodeName);
		if (node == null) {
			return null;
		}
		String txt = node.getTextContent();
		if (txt == null) {
			return null;
		}
		return txt.trim();
	}

	/**
	 * Find the first node in the document for the given name. 
	 * @param doc
	 * @param nodeName
	 * @return
	 */
	private Node findFirstNode(Element doc, String nodeName) {
		NodeList els = doc.getElementsByTagName(nodeName);
		int n = els.getLength();
		if (n == 0) {
			return null;
		}
		return els.item(0);
	}
}
