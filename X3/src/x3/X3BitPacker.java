package x3;

import java.util.Arrays;

/**
 * Bit packer to pack sequential bits of data into byte chunks. Should consider
 * converting this to integers since they will be loads more efficient for bit
 * operations than single bytes. 
 * @author Doug Gillespie
 *
 */
public class X3BitPacker {

	private byte[] bytes;
	private Object nBits;
	private int bitPos, bytePos;

	/**
	 * Masks for reverse bit ordering (i.e. first bit is the most significant)
	 */
	private static final byte[] _masks = {(byte) 0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1};
			
	/**
	 * Construct a new bit packer. 
	 * @param bytes data to pack or unpack. 
	 */
	public X3BitPacker(byte[] bytes) {
		this.bytes = bytes;
		nBits = bytes.length * 8;
		bitPos = bytePos = 0;
		
		/*
		 * These lines seem like an efficient way to convert a byte array 
		 * into an int array - shouldn't actually involve any copying of data, 
		 * just shifting of pointers. 
		 */
//		ByteBuffer bBuff = ByteBuffer.wrap(bytes);
//		IntBuffer iBuff = bBuff.asIntBuffer();
//		System.out.println("Int buffer is direct = " + iBuff.isDirect());
		
	}
	
	/**
	 * Read the next bit
	 * @return the next bit
	 */
	boolean getBit() {
		boolean ans = ((bytes[bytePos] & _masks[bitPos%8]) != 0);
		if (++bitPos == 8) {
			bytePos++;
			bitPos = 0;
		}
		return ans;
	}
	
	/**
	 * Set the next bit to 1.
	 */
	public void setBit() {
		bytes[bytePos] |= _masks[bitPos];
		if (++bitPos == 8) {
			bytePos++;
			bitPos = 0;
		}
	}
	
	
	/**
	 * Read a number of bits into a short integer<p>
	 * Note that this function does NOT correct the sign of
	 * bit packed data using < 15 bits. 
	 * @param nBits number of bits to read
	 * @return short integer read. 
	 */
	short getBits(int nBits) {
		short ans = 0;
		while (nBits > 0) {
			nBits--;
			if (getBit()) 
			ans |= 1<<nBits;
		}
		return ans;
	}
	
	/**
	 * Write a number of bits into the byte array
	 * @param pattern pattern of bits to write
	 * @param nBits number of bits to write. 
	 */
	void setBits(int pattern, int nBits) {
		while (nBits > 0) {
			nBits--;
			if ((pattern & 1<<nBits) != 0) {
				setBit();
			}
			else {
				skipBits(1);
			}
		}
	}
	
	/**
	 * Skip a number of bits. 
	 * @param nBits number of bits to skip.
	 */
	void skipBits(int nBits) {
		bitPos += nBits;
		bytePos += bitPos/8;
		bitPos = bitPos%8;
	}
	
	/**
	 * Fix the signs of an array of data read using the nextBits() function
	 * @param data data to fix
	 * @param nBits number of bits used (including the sign bit)
	 * @param offset offset in the array to start at. 
	 * @param n number of words to fix
	 * @param stride step size between words. 
	 */
	public void fixSign(short[] data, int nBits, int offset, int n, int stride) {
		short w;
		short offs, half ;

		half = (short) (1<<(nBits-1)) ;
		offs = (short) (half<<1) ;
		int p = offset;
		for (int i = 0; i < n; i++, p+= stride) {
			w = data[p];
			data[p] = (short) ((w>=half) ? w-offs : w) ;
		}
	}
	
	/**
	 * Fix the sign of a single word of data read using the nextBits function<p>
	 * Note that the array version of this will be more efficient. 
	 * @param data unsigned data. 
	 * @param nBits number of bits (including the sign bit)
	 * @return signed data. 
	 */
	public short fixSign(short data, int nBits) {
		short half = (short) (1<<(nBits-1)) ;
		short offs = (short) (half<<1) ;
		return (short) (data >= half ? data-offs : data);
	}

	/**
	 * @return the bitPos
	 */
	public int getBitPos() {
		return bitPos;
	}

	/**
	 * @return the bytePos
	 */
	public int getBytePos() {
		return bytePos;
	}

	/**
	 * 
	 * @return  the number of bytes actually used.
	 */
	public int getUsedBytes() {
		int used = getBytePos();
		if (bitPos > 0) {
			used ++;
		}
		return used;
	}

	/**
	 * Clear an array completely. 
	 */
	public void clear() {
		Arrays.fill(bytes, (byte)0);
	}
}
