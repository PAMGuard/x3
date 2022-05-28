package org.pamguard.x3.x3;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

//import com.sun.org.apache.xml.internal.serialize.OutputFormat;
//import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
//import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
 * 
 * Interface for dealing with the two slightly different X3 file systems
 * (even though we're hoping to fade one out asap). 
 * @author Doug Gillespie
 *
 */
public abstract class X3FileSystem {
	
	static final String X3A_FILE_KEY  = "X3ARCHIV";
	static final String X3_FILE_KEY = "x3";
	static public final int X3_UNKNOWN = 0;
	static public final int X3_PAMBUOY = 1;
	static public final int X3_D3X3A = 2;

	static String getfileTypeString(int type) {
		switch (type) {
		case X3_UNKNOWN:
			return "Unknown X3 format";
		case X3_PAMBUOY:
			return X3_FILE_KEY;
		case X3_D3X3A:
			return X3A_FILE_KEY;
		}
		return "Unknown X3 format";
	}

	/**
	 * Work out which type of file it is. 
	 * @param x3File x3File
	 * @return file type 0 = unknown, 1 = Decimus format, 2 = Marks D3 format. 
	 */
	static public int getX3Type(File x3File) {
		int x3Type = X3_UNKNOWN;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(x3File));
			x3Type = getX3Type(dis);
			dis.close();
		}
		catch (Exception e) {
			return X3_UNKNOWN;
		}
		
		return x3Type;
	}
	
	/**
	 * Work out which type of file it is. 
	 * @param dataInputStream
	 * @return file type 0 = unknown, 1 = Decimus format, 2 = Marks D3 format. 
	 */
	static public int getX3Type(DataInputStream dataInputStream) {
		String str;
		byte[] data = new byte[X3A_FILE_KEY.length()];
		try {
			dataInputStream.read(data, 0, X3_FILE_KEY.length());
			str = new String(data, "US-ASCII");
			if (str.trim().equals(X3_FILE_KEY)) {
				return X3_PAMBUOY;
			}
			dataInputStream.read(data, X3_FILE_KEY.length(), X3A_FILE_KEY.length()-X3_FILE_KEY.length());
			str = new String(data, "US-ASCII");
			if (str.equals(X3A_FILE_KEY)) {
				return X3_D3X3A;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return X3_UNKNOWN;
		}
		return X3_UNKNOWN;
	}
	
	/**
	 * Create a new file system of the appropriate tyep. 
	 * @param fileType 1 or 2. 
	 * @return a class which knows how to unpack data from each file type. 
	 */
	static X3FileSystem getFileSystem(int fileType) {
		switch (fileType) {
		case X3_UNKNOWN:
			return null;
		case X3_PAMBUOY:
			return new X3PBFileSystem();
		case X3_D3X3A:
			return new X3D3FileSystem();
		}
		return null;
	}

	/**
	 * Generate an xml documnet from a string. 
	 * @param xmlStr xml string. 
	 * @return xml document
	 */
    public Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
        DocumentBuilder builder; 
        try 
        { 
            builder = factory.newDocumentBuilder(); 
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr.trim() ) ) );
            return doc;
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return null;
    }
    
    public abstract String readFileHeader(DataInputStream dis) throws IOException;

	/**
	 * Called just after the first few bytes of the file have been read in 
	 * order to identify the file type. All files should start with a valid
	 * header, so read it now. 
	 * @param dis data input stream
	 * @return a valid file header structure, or null in the event of failure. 
	 */
	public abstract X3FileHeader decodeFileHeader(String xmlData);
	
	/**
	 * Read a frame header from the input stream. 
	 * @param dis data input stream
	 * @param x3FileHeader file header so data can be extracted if format data is spread incorrectly between file and block headers .
	 * @param exHeader Existing frame header which can be reused or will be created if null. 
	 * @return a frame header structure. 
	 */
	public abstract X3FrameHeader readFrameHeader(DataInputStream dis, X3FileHeader x3FileHeader, 
			X3FrameHeader exHeader) throws IOException;

	/**
	 * Create an XML document with header information. 
	 * @param blockLength block length used throughout file
	 * @param sampleRate sample rate
	 * @param nChannels number o channels
	 * @return XML document to go at front of file. 
	 */
	public abstract Document createX3HeaderXML(int blockLength, int sampleRate, int nChannels);
	
	/**
	 * Convert an XML Document into a string
	 * @param doc XML Document
	 * @param indenting level of indenting
	 * @return the XML data as a single String. 
	 */
	public String getXMLDataText(Document doc, int indenting) {
//		ByteOutputStream bos;
//		/**
//		 * XML document now created - output it to file. 
//		 */
//		DataOutputStream dos = new DataOutputStream(bos = new ByteOutputStream());
//
//		OutputFormat of = new OutputFormat("XML","ISO-8859-1", indenting>0);
//		if (indenting>0) {
//			of.setIndent(3);
//		}
//		XMLSerializer serializer = new XMLSerializer(bos,of);
//		// As a DOM Serializer
//		try {
//			serializer.asDOMSerializer();
//			serializer.serialize( doc.getDocumentElement() );
//			bos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		}
//		String str = new String(bos.getBytes());
//		return str;
		String asString = null;
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			if (indenting>0) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indenting));
			} else {
				transformer.setOutputProperty(OutputKeys.INDENT, "no");
			}
			transformer.transform(domSource, result);
			asString = writer.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}			
		return asString;
	}
	

}
