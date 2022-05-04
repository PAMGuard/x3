package org.pamguard.x3.x3;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Frame header for Marks d3 x3 format data. 
 * Combines shared byte and short arrays for quick access. 
 * @author Doug Gillespie
 *
 */
public class X3FrameHeader {

	public static final int X3_HDRLEN = 20; // header length in bytes (MJ uses short words)
	
	private ByteBuffer byteBuffer;
	private ShortBuffer shortBuffer;
	private IntBuffer intBuffer;

	public static final short X3_KEY = 30771;

//	public short x3_key = X3_KEY;
//	public byte id;
//	public int nChan;
//	public short nSamples;
//	public short nBytes;
//	public long timeCode;
//	public int timeMicros;
//	public short crcHead;
//	public int crcData;
	
	public boolean crcHeadOk;

	
	/**
	 * Information we expect from each frame header.
	 * As per x3frameheader.c;
	 */
	public X3FrameHeader() {
//		byteBuffer = ByteBuffer.allocateDirect(X3_HDRLEN);
		byteBuffer = ByteBuffer.wrap(new byte[20]);
		shortBuffer = byteBuffer.asShortBuffer();
		intBuffer = byteBuffer.asIntBuffer();
		setX3_key(X3_KEY);
	}
	
	/**
	 * Read x3 frame header using same format as in Marks C code
	 * x3frameheader in x3frame.c
	 * @param data byte array of compressed data
	 * @return true if frame header read. 
	 */
	public boolean readHeader(byte[] data) {
		if (data == null || data.length < X3_HDRLEN) {
			return false;
		}
//		DataInputStream dis = new DataInputStream(new ByteInputStream(data, X3_HDRLEN));
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data, 0, X3_HDRLEN)); // add an offset of 0 to use ByteArrayInputStream constructor
		try {
			
			readHeader(dis);
			dis.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		short newCRC = CRC16.getCRC16(data, 16); // crc of first 8 words in buffer.
//		System.out.println(String.format("Old and new CRC codes are 0x%x and 0x%x", crcHead, newCRC));
		crcHeadOk = getCrcHead() == newCRC;
		return (getX3_key() == X3_KEY) & crcHeadOk;
	}
	
	/**
	 * Constructor used when writing data
	 * @param id data id
	 * @param nChan number of channels
	 * @param nSamples number of samples
	 * @param nBytes number of bytes
	 * @param timeCode timecode (Unix time - seconds since 1970)
	 * @param timeMicros time microseconds. 
	 */
	public X3FrameHeader(byte id, int nChan, short nSamples, short nBytes,
			long timeCode, int timeMicros) {
		super();
		byteBuffer.rewind();
		shortBuffer.rewind();
		shortBuffer.put(0, X3_KEY);
		byteBuffer.put(2, id);
		byteBuffer.put(3, (byte) nChan);
		shortBuffer.put(2, nSamples);
		shortBuffer.put(3, nBytes);
		intBuffer.put(2, (int) timeCode);
		intBuffer.put(3, timeMicros);
	}

	/**
	 * Read header data from an input stream. 
	 * @param dis input stream
	 * @return true if the correct number bytes was read. 
	 * @throws IOException 
	 */
	public boolean readHeader(DataInputStream dis) throws IOException {
		
		int bytesRead = dis.read(byteBuffer.array());
				
		return (bytesRead == X3_HDRLEN);
	}

	/**
	 * Write a header to an output stream 
	 * @param dos Data output stream
	 * @return always true
	 * @throws IOException
	 */
	public boolean writeHeader(DataOutputStream dos) throws IOException {
//		dos.writeShort(x3_key);
//		dos.write(id);
//		dos.write(nChan);
//		dos.writeShort(nSamples);
//		dos.writeShort(nBytes);
//		dos.writeInt((int) (timeCode/1000));
//		dos.writeInt(timeMicros);
//		dos.writeShort(crcHead);
//		dos.writeShort(crcData);
		dos.write(byteBuffer.array());
		
		return true;
	}

	@Override
	public String toString() {
		return String.format("Key: %d id: %d, nChan: %d, nSamples: %d, nBytes: %d, time: %d, crcs: %d,%d",
				getX3_key(), getId(), getnChan(), getnSamples(), getnBytes(), getTimeCode(), getCrcHead(), getCrcData());
	}
	
//	private void putShort(int pos, short data) {
//		headData[pos] = (byte) ((data>>8) & 0xFF);
//		headData[pos+1] = (byte) (data & 0xFF);
//	}
//	
//	private short getShort(int pos) {
//		return (short) ((headData[pos]&0xFF) << 8 | headData[pos+1]&0xFF); 
//	}

	/**
	 * @return the X3 key - always 30771
	 */
	public short getX3_key() {
		return shortBuffer.get(0);
	}

	/**
	 * Set the x3 header key
	 * @param x3_key always 30771
	 */
	public void setX3_key(short x3_key) {
		shortBuffer.put(0, x3_key);
	}

	/**
	 * 
	 * @return the header id (an integer module number, 0 for the file header xml data)
	 */
	public byte getId() {
		return byteBuffer.get(2);
	}

	/**
	 * Set the module id
	 * @param id module id
	 */
	public void setId(byte id) {
		byteBuffer.put(2, id);
	}

	/**
	 * 
	 * @return the number of channels in subsequent data
	 */
	public int getnChan() {
		return byteBuffer.get(3);
	}

	/**
	 * Set the number of channels
	 * @param nChan number of channels
	 */
	public void setnChan(int nChan) {
		byteBuffer.put(3, (byte) nChan);
	}

	/**
	 * 
	 * @return the number of samples in the next frame. 
	 */
	public short getnSamples() {
		return shortBuffer.get(2);
	}

	/**
	 * Set the number of samples in the next frame
	 * @param nSamples number of samples
	 */
	public void setnSamples(short nSamples) {
		shortBuffer.put(2, nSamples);
	}

	/**
	 * 
	 * @return the number of bytes in the next frame
	 */
	public short getnBytes() {
		return shortBuffer.get(3);
	}

	/**
	 * Set the number of bytes in the next frame
	 * @param nBytes number of bytes. 
	 */
	public void setnBytes(short nBytes) {
		shortBuffer.put(3, nBytes);
	}

	/**
	 * 
	 * @return the unix time code for the nect frame
	 */
	public int getTimeCode() {
		return intBuffer.get(2);
	}

	/**
	 * Set the unix time for the next frame
	 * @param timeCode seconds since 1970
	 */
	public void setTimeCode(int timeCode) {
		intBuffer.put(2,  timeCode);
	}

	/**
	 * 
	 * @return The number of microseconds to add to the TimeCode
	 */
	public int getTimeMicros() {
		return intBuffer.get(3);
	}

	/**
	 * Set the number of microseconds to add to the time code
	 * @param timeMicros microseconds. 
	 */
	public void setTimeMicros(int timeMicros) {
		intBuffer.put(3,  timeMicros);
	}

	/**
	 * 
	 * @return 16 bit CRC code for the header data
	 */
	public short getCrcHead() {
		return shortBuffer.get(8);
	}

	/**
	 * Set a 16 bit CRC code for the first 16 bytes of the frame header
	 * @param crcHead 16 bit CRC
	 */
	public void setCrcHead(short crcHead) {
		shortBuffer.put(8, crcHead);
	}

	/**
	 * 
	 * @return 16 bit CRC code for the subsequent n bytes of dat.a 
	 */
	public short getCrcData() {
		return shortBuffer.get(9);
	}

	/**
	 * Set a 16 bit CRC code for the data
	 * @param crcData 16 bit CRC code. 
	 */
	public void setCrcData(short crcData) {
		shortBuffer.put(9, crcData);
	}

	public byte[] getHeadData() {
		boolean hasArray = byteBuffer.hasArray();
		if (hasArray == false) {
			return null;
		}
		return byteBuffer.array();
	}

}
