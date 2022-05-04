package org.pamguard.x3.x3;

import java.io.DataInputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import com.sun.org.apache.xerces.internal.dom.DocumentImpl;

/**
 * X3 file functions using the standards used for the Decimus system which is 
 * slightly different to the d3 standard. 
 * @author Doug Gillespie
 *
 */
public class X3PBFileSystem extends X3FileSystem {

	public X3PBFileSystem() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String readFileHeader(DataInputStream dis) throws IOException {
		int xmlLength = dis.readInt();
		xmlLength = Integer.reverseBytes(xmlLength);
		byte[] xmlData = new byte[xmlLength];
		dis.read(xmlData);
		String xmlString = new String(xmlData, "US-ASCII");
		System.out.println(xmlString);
		return xmlString;
	}

	@Override
	public X3FileHeader decodeFileHeader(String xmlString) {
	    Document doc = convertStringToDocument(xmlString);
	    // decend into the info node
	    Element info = (Element) doc.getFirstChild();
	    X3FileHeader fileHeader = new X3FileHeader();
	    String el;
	    try {
	    	el = info.getAttribute("NCHAN");
	    	fileHeader.nChannels = Integer.valueOf(el);
	    	el = info.getAttribute("BLKLEN");
	    	fileHeader.blockLen = Integer.valueOf(el);
	    	el = info.getAttribute("SAMPLERATE");
	    	fileHeader.sampleRate = Integer.valueOf(el);
	    	el = info.getAttribute("TIMESTAMP");
	    	//	    fileHeader.startTimeMillis = 
	    }
	    catch (Exception e) {
	    	System.err.println("Error unpacking x3 file header: " + e.getMessage());
	    }
		
		return fileHeader;
	}
	
	@Override
	public  Document createX3HeaderXML(int blockLength, int sampleRate, int nChannels) {
//		Document doc = new DocumentImpl();
		Document doc = X3Common.createBlankDoc();
		Element root = doc.createElement("X3FILE");
		doc.appendChild(root);

		Element vInfo = doc.createElement("VERSION");
		root.appendChild(vInfo);
		vInfo.setAttribute("Version", "1");

		Element cfg = doc.createElement("CFG");
		root.appendChild(cfg);
		Element el = doc.createElement("SAMPLERATE");
		el.setTextContent(String.format("%d", sampleRate));
		cfg.appendChild(el);
		el = doc.createElement("NCHAN");
		el.setTextContent(String.format("%d", nChannels));
		cfg.appendChild(el);
		el = doc.createElement("BLOCKLEN");
		el.setTextContent(String.format("%d", X3FrameEncode.blockSamples));
		cfg.appendChild(el);
		
		return doc;
	}
	@Override
	public X3FrameHeader readFrameHeader(DataInputStream dis, X3FileHeader x3FileHeader, X3FrameHeader exHeader) throws IOException {
		if (exHeader == null) {
			exHeader = new X3FrameHeader();
		}
		byte[] bb = new byte[16];
//		dis.read(bb);
		//has 16 bytes of data - different to the d3 x3 header. 
		exHeader.setX3_key(dis.readShort());
		exHeader.setnBytes(dis.readShort());
		exHeader.setnSamples(dis.readShort());
		exHeader.setCrcData((short) dis.readInt());
		exHeader.setCrcHead(dis.readShort());
		exHeader.setId((byte) 1);
		exHeader.setnChan(x3FileHeader.nChannels);
		dis.skip(4);
		return exHeader;
	}


}
