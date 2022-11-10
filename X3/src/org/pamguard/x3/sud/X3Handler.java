
package org.pamguard.x3.sud;

import java.util.HashMap;

import org.pamguard.x3.utils.XMLUtils;
import org.pamguard.x3.x3.X3BitPacker;
import org.pamguard.x3.x3.X3FrameDecode;
import org.pamguard.x3.x3.X3FrameHeader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;




/**
 * Opens an X3 blocks in SUD files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class X3Handler implements ISudarDataHandler {
	
	static int[] RSUFFS = {0,1,3} ;
	
	
	static short[]   IRT = {0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6,-7,7,-8,8,-9,9,-10,10,
		-11,11,-12,12,-13,13,-14,14,-15,15,-16,16,-17,17,-18,18,
		-19,19,-20,20,-21,21,-22,22,-23,23,-24,24,-25,25,-26,26};


	private int[] chunkIds;

	/**
	 * X3 Frame decoder. 
	 */
	private X3FrameDecode x3FrameDecode;

	private X3FrameHeader x3Head = new X3FrameHeader();

	private Integer blockLength;

	private Integer nChan;

	private X3BitPacker bs;

	/**
	 * A string enum to define the handler
	 */
	private String ftype; 


	public X3Handler(SudParams filePath, String ftype) {
		x3FrameDecode = new X3FrameDecode(); 
		this.ftype = ftype; 
	}

	int readCount = 0;
	@Override
	/**
	 * Process a chunk of X3 data. The uncompressed data is saved to the buffer of sudChunk. 
	 * @param - the sudChunk containing the X3 compressed data. 
	 */
	public void processChunk(Chunk sudChunk) {
		
	
		
		byte[] buf = sudChunk.buffer;
		ChunkHeader ch = sudChunk.chunkHeader;
		
		//System.out.println("Process X3 file: " + buf.length);

		
		//System.out.println("Process X3 chunk START: " + buf.length + " first byte: " + Byte.toUnsignedInt(buf[0]) + " samples: " + ch.SampleCount);
	
		
		//set the relevent data for the X3 header
		x3Head.setnSamples((short) ch.SampleCount);
		x3Head.setCrcData((short) ch.DataCrc);
		x3Head.setId((byte) ch.ChunkId);
		x3Head.setnChan(nChan);
		x3Head.setCrcHead((short) ch.HeaderCrc);
		
		short[] b = null;

		int[] bOutPos = new int[] {0,1,2,3};
		short[] bOut  = new short[ch.SampleCount * nChan];
		
		
		short[] last = new short[4];
		
		//very important or absolutely nothing works. 
		XMLFileHandler.swapEndian(buf);
		
		//create a bit stream to read individual bits from the byte array. 
		bs = new X3BitPacker(buf);

		for(int c=0; c<nChan;c++) {
			int j = Short.toUnsignedInt(bs.getBits(16));
			//System.out.println("Bit read j: " + j);
			bOut[bOutPos[c]] = (short) j;
			last[c] = bOut[bOutPos[c]];
			bOutPos[c]+=nChan;
		}
		
		try {
		readCount = 0;
		int samplesToGo = (ch.SampleCount-1);
		while(samplesToGo > 0) {
			//int c = 0;
			int n=0;
			n = Math.min(blockLength, samplesToGo);
			BlockDecodeOut blockOut;
			//System.out.println("Count: " + readCount + " samplesToGo " + samplesToGo);
			for(int c=0; c<nChan;c++) {
				//run the X3 decompression on a block!
					blockOut = BlockDecode(bs, b, last[c], n);
					n = blockOut.n; 
					b= blockOut.array;
		

				readCount++;
				last[c] = b[b.length-1];
				for(int i=0; i<b.length; i++) {
					bOut[bOutPos[c]] = b[i];
					bOutPos[c] += nChan;
				}
			}
			samplesToGo -= n;
		}
		
		
		byte[] buf2 = new byte[bOut.length*2];
		for(int i=0; i< bOut.length; i++) {
			buf2[i*2] = (byte)(bOut[i] & 0x00ff);
			buf2[(i*2)+1] = (byte)((bOut[i] >> 8)& 0x00ff);
		}
		
		sudChunk.buffer = buf2; 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
					
//		System.out.println("Process X3 chunk: " + nChan);
		//short[] wavData = x3FrameDecode.unpackX3Frame(x3Head, buf, 0, null,  blockLength); 
		
		//System.out.println("Process X3 chunk END: " + buf.length + "  " + ch.SampleCount);

	}

	private BlockDecodeOut BlockDecode(X3BitPacker bs2, short[] buf, short last, int count) throws Exception {
		
//		int iChan = 0;
				
//		this.x3Head.setnSamples((short) 16);
//		
//		byte[] x3Data = bs2.getBytes(count);
//		
//
//		System.out.println(readCount +  ": Block decode: bitpos: " + bs2.getBitPos() + " last: " + last + " count: " + count + " x3Data " + x3Data[0]);
//
//		short[] hello = x3FrameDecode.unpackX3Frame(x3Head, x3Data,  0, buf,  blockLength);
//		
//		System.out.println(readCount + ": Block decode result: " + hello.length + " First sample: " + hello[0]);
//		
//		return new BlockDecodeOut(hello.length, hello);
	
		
		int nb = 0;
		//System.out.println(readCount + ": bitpos 0: " +  bs2.getBitPos() +   " bytePos: " +  bs2.getBytePos());
		int code = bs2.ReadInt(2);
		//System.out.println(readCount + ": bitpos 1: " + bs2.getBitPos() + " code: " + code);
		if(code==0) {
			//bfp or pass thru block
			nb = bs2.ReadInt(4); 
//			System.out.println(readCount + ": bitpos 2: " + bs2.getBitPos());
			//System.out.println("nb: " + nb);
			if(nb>0) {
				nb++;
			}
			else {
				int nn = bs2.ReadInt(6) + 1; //FIXME - issue when input>4
				//System.out.println("nn: " + nn);
				if (nn > blockLength) throw new Exception("bad block length: " + nn);
				count = nn;
				code = bs2.ReadInt(2);
				if(code == 0) {
					nb = bs2.ReadInt(4) + 1;
				}
			}
		}
		
		//System.out.println(readCount + ": Block decode: bitpos: " + bs2.getBitPos() +   " bytePos: " +  bs2.getBytePos() + " last: " + last + " count: " + count);

		buf = new short[count];
		
		//System.out.println(count + ": Unpack Rice: " + count);
		
//		int iBit;
//		int p = nChan;
		if(code > 0) {
			//unpackr(bufIn, buf, count, code-1);
//			iBit = X3FrameDecode.unpackRice(bs2, code, buf, p+iChan, count, iChan);
			unpackr(bs2, buf, count,  code-1);
		}
		else {
			//npack(bufIn, buf, nb, count);
			unpack(bs2, buf, nb, count);

			if(nb == 16) return  new BlockDecodeOut(count, buf);;
		}
		
//		X3FrameDecode.integrate(buf, last, buf.length, 0);
		
		integrate(buf, last, buf.length);

		//System.out.println(readCount +  ": Block decode result: bitPos: " + bs2.getBitPos() +   " bytePos: " +  bs2.getBytePos() +  " len: " + buf.length + " first sample: " + buf[0] + " second sample: " + buf[1]);
		
		
		
		return new BlockDecodeOut(buf.length, buf);	
	}
	
	void integrate(short[] op, short last, int count )
	{
		// De-emphasis filter to reverse the diff in the compressor.
		// Filters operates in-place.
		int    k ;
		for(k=0; k<count; k++) {
			last += op[k];
			op[k] = last ;
		}
	}
	
	
	public void unpackr(X3BitPacker b, short[] bufOut, int n, int code) throws Exception
	{
		// unpacker for variable length Rice codes
		// Returns 0 if ok, 1 if there are not enough bits in the stream.
		long ow = 0;
		long msk ;
		int ntogo = 0;
		int ns;
		int suff;
		int nsuffix = RSUFFS[code];
		int lev = 1<<nsuffix ;
		
		for(int k=0; k<n; k++) {      // Do for n words
			// First find the end of the variable length section.
			// If there is an end and a complete suffix in the current word, it will
			// have a value of at least 1<<nsuffix. If not, append the next word from
			// the stream
			
			
			//System.out.println("1: ntogo: " + ntogo + " lev: " + lev + " b " + b.getBitPos());

			long [] ntogo1 = b.readIntLargerThan(lev);
						
			ntogo = (int) ntogo1[0];
			ow = ntogo1[1];
			
//			if (readCount==487) System.out.println(k +": ntogo: " + ntogo + " lev: " + lev + " ow: " + ow+ " bitpos " 
//			+ b.getBitPos() + " bytepos: " + b.getBytePos() +  " bytes N: " + b.getNBytes());

			
			// ow is now guaranteed to have a start and a suffix.
			// Find the start (i.e., the first 1 bit from the left)
			for(ns=1, msk = 1<<(ntogo-1); ns<=ntogo && (ow & msk)==0; ns++, msk>>=1);
			if(ns>ntogo) {
				throw new Exception("rice decode error"); //error
			}
			ntogo -= ns+nsuffix ;
			suff = (int)((ow >> ntogo) & (lev-1));
			ow &= (1<<ntogo)-1 ;
			bufOut[k] = (short) IRT[lev*(ns-1)+suff];
		}
		//TODO: do we need to return any unused bits to the input stream?
		if(ntogo > 0)
			throw new Exception("ntogo > 0 !");
	}
	
	void unpack(X3BitPacker bufIn, short[] bufOut, int nb, int count) throws Exception
	{
		for(int i=0; i<count; i++) {
			bufOut[i] = FixSign(bufIn.ReadInt(nb), nb);
		}
	}
	

	short FixSign(int d, int nbits)
	{
		int half = (int)(1<<(nbits-1));
		int offs = (int)(half<<1);
		return (short)((d >= half) ? (int) d-offs : d);
	}
	


	@Override
	public void close() {
		// TODO Auto-generated method stub	
	}

	@Override
	/**
	 * Unpack the X3 XML to get some basic info such as the block length and the number of channels. 
	 */
	public void init(LogFileStream inputStream, String innerXml, int id) {
		
		this.chunkIds = new int[]{id};
		

		Document doc = XMLFileHandler.convertStringToXMLDocument(innerXml.trim());

		//NodeList nodeList = doc.getDocumentElement().getChildNodes();
		NodeList nodeList = doc.getElementsByTagName("CFG");
				
		HashMap<String, String> nodeContent = XMLUtils.getInnerNodeContent(new String[] {"BLKLEN", "NCHS"},  nodeList);

		blockLength = Integer.valueOf(nodeContent.get("BLKLEN"));
		
		//System.out.println("Block Length: " + blockLength);
		nChan = Integer.valueOf(nodeContent.get("NCHS"));

//		if(nodeList!=null && nodeList.getLength() > 0) {
//			for (int i=0; i<nodeList.getLength(); i++) {
//
//				//System.out.println("Child Nodes len: " +  nodeList.item(i).getChildNodes().getLength()); 
//
//				for (int j=0; j< nodeList.item(i).getChildNodes().getLength(); j++) {
//
//					//System.out.println("Child Node Names: "+ nodeList.item(i).getChildNodes().item(j).getNodeName()+ " attributes" + nodeList.item(i).getChildNodes().item(j).getAttributes() + "   "  +j);
//
//					String content = nodeList.item(i).getChildNodes().item(j).getTextContent().trim();
//
//					if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals("BLKLEN")) {
//						blockLength = Integer.valueOf(content);
//					}
//					
//					if (nodeList.item(i).getChildNodes().item(j).getNodeName().equals("NCHS")) {
//						nChan = Integer.valueOf(content);
//					}
//
//				}
//			}
//		}
		
		x3Head.setnChan(nChan);
	
		//System.out.println("BLOCKLENGTH: " + blockLength + " nChan: " + nChan ); 
	}

	@Override
	public int[] getChunkID() {
		return chunkIds;
	}

	
	/**
	 * Holds output info for the block decode. 
	 * @author Jamie Macaulay
	 *
	 */
	class BlockDecodeOut {
		
		 public BlockDecodeOut(int n, short[] array) {
			super();
			this.n = n;
			this.array = array;
		}

		int n; 
		 
		 short [] array;
	}

	@Override
	public String getHandlerType() {
		return ftype;
	}


}
