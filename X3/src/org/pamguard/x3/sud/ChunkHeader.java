package org.pamguard.x3.sud;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;

import org.pamguard.x3.x3.CRC16;

/**
 * The chunk header for each block within the sud files. 
 * <p>
 * The chunk header provides a data integrity check and also contains
 * information on what type of data are present in the block. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ChunkHeader implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The total number of bytes in the chunk. 
	 */
	public static final transient long NUM_BYTES = 20;
	
	public int majicNo; //UInt16
	public int ChunkId;  //UInt16
	public int DataLength; //UInt16
	public int SampleCount; //UInt16
	public int TimeS; //UInt32
	public int TimeOffsetUs; //UInt32
	public int DataCrc; //UInt16
	public int HeaderCrc; //UInt16
	
	

	public static ChunkHeader deSerialise(DataInput bufinput) throws IOException {
		
		ChunkHeader header = new ChunkHeader(); 
		byte[] headData = new byte[20];
		bufinput.readFully(headData);
		DataInput di = new SudDataInputStream(new ByteArrayInputStream(headData));

		header.majicNo = di.readUnsignedShort();
		header.ChunkId = di.readUnsignedShort();
		header.DataLength = di.readUnsignedShort();
		header.SampleCount = di.readUnsignedShort(); 
		
		header.TimeS = di.readInt();//the unix time of the chunk. 
		header.TimeOffsetUs = di.readInt();
		header.DataCrc = di.readUnsignedShort();
		header.HeaderCrc = di.readUnsignedShort();

		int crc = CRC16.getCRC16(headData, 2, 16);
		if (crc < 0) crc += 65536;

		return header;
	}
	
	/**
	 * Get the time in standard Java milliseconds. 
	 * @return
	 */
	public long getMillisTime() {
		return Integer.toUnsignedLong(TimeS) * 1000L + TimeOffsetUs / 1000;
	}
	
	/**
	 * Get the time in standard  Microseconds. This is on the same reference as Java milliseconds
	 * but with higher resolution.  
	 * @return
	 */
	public long getMicrosecondTime() {
		return Integer.toUnsignedLong(TimeS) * 1000000L + TimeOffsetUs;
	}
	
	/**
	 * Check that the chunk is valid - this simply checks a "magic" number
	 * in the header that should be the correct number.  
	 * @return true if the chunk header is valid. 
	 */
	public boolean checkId() {
		return majicNo == 0xA952; // 43346 dec.
	}
	
	
	public String toHeaderString() {
		return String.format("majicNo: %d\nChunkId: %d\nDataLength: %d\nSampleCount: %d\nTimeS: %d\nTimeOffsetUs: %d\nDataCrc: %d\n"
				+ "HeaderCrc: %d\n", 
				majicNo, ChunkId, DataLength, SampleCount, TimeS, TimeOffsetUs, DataCrc, 
				HeaderCrc);
	}

}
