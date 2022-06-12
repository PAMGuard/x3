package org.pamguard.x3.sud;

import java.io.DataInput;

import org.pamguard.x3.x3.X3FrameDecode;
import org.pamguard.x3.x3.X3FrameHeader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * Opens an X3 block
 * @author Jamie Macaulay
 *
 */
public class X3Handler implements ISudarDataHandler {

	private int[] chunkIds;

	/**
	 * X3 Frame decoder. 
	 */
	private X3FrameDecode x3FrameDecode;


	private X3FrameHeader x3Head = new X3FrameHeader();

	private Integer blockLength;

	private Integer nChan; 

	public X3Handler(String filePath) {
		x3FrameDecode = new X3FrameDecode(); 
	}

	@Override
	public void processChunk(ChunkHeader ch, byte[] buf) {
		
		x3Head.setnSamples((short) ch.SampleCount);
		x3Head.setCrcData((short) ch.DataCrc);
		x3Head.setId((byte) ch.ChunkId);
		x3Head.setnChan(nChan);
		x3Head.setCrcHead((short) ch.HeaderCrc);
		
//		System.out.println("Process X3 chunk: " + nChan);
		short[] wavData = x3FrameDecode.unpackX3Frame(x3Head, buf, 0, null,  blockLength); 
		
		//System.out.println("Process X3 chunk: " + buf.length + "  " + ch.SampleCount);

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub	
	}

	@Override
	public void init(DataInput inputStream, String innerXml, int id) {
		
		this.chunkIds = new int[]{id};
		

		Document doc = XMLFileHandler.convertStringToXMLDocument(innerXml.trim());

		//NodeList nodeList = doc.getDocumentElement().getChildNodes();
		NodeList nodeList = doc.getElementsByTagName("CFG");
				
		
		if(nodeList!=null && nodeList.getLength() > 0) {
			for (int i=0; i<nodeList.getLength(); i++) {

				//System.out.println("Child Nodes len: " +  nodeList.item(i).getChildNodes().getLength()); 

				for (int j=0; j< nodeList.item(i).getChildNodes().getLength(); j++) {

					//System.out.println("Child Node Names: "+ nodeList.item(i).getChildNodes().item(j).getNodeName()+ " attributes" + nodeList.item(i).getChildNodes().item(j).getAttributes() + "   "  +j);

					String content = nodeList.item(i).getChildNodes().item(j).getTextContent().trim();

					if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals("BLKLEN")) {
						blockLength = Integer.valueOf(content);
					}
					
					if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals("NCHS")) {
						nChan = Integer.valueOf(content);
					}

					
					//						Node nChan = nodeList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("NCHS");
					//						Node filter = nodeList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("FILTER");
					//

					//					nodeList.item(i).getAttributes().getLength();
					//					for (int jj=0; jj<nodeList.item(i).getChildNodes().item(j).getAttributes().getLength(); jj++) {
					//						if (nodeList.item(i).getChildNodes().item(j).getAttributes()!=null) {
					//						System.out.println("Attribute: " +  nodeList.item(i).getChildNodes().item(j).getAttributes().item(jj).getNodeName());
					//						}
					//					}
					//						if (blocklength!=null) {
					//							this.blockLength = Integer.valueOf(blocklength.getNodeValue()); 
					//						}
					//						if (nChan!=null) {
					//							this.nChan = Integer.valueOf(nChan.getNodeValue()); 
					//						}



				}

			}
		}
		
		x3Head.setnChan(nChan);
	
		//System.out.println("BLOCKLENGTH: " + blockLength + " nChan: " + nChan ); 
	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}


}
