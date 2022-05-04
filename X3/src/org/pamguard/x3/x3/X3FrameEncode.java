package org.pamguard.x3.x3;

/**
 * Functions to encode a single X3 frame. 
 * @author Doug
 *
 */
public class X3FrameEncode {

	public static final int[] riceThresholds = {3, 8, 19};
	public static final int[] riceOrders = {0, 1, 3};
	private RiceTable[] riceTables;
	public static int frameSamples = 1000;
	public static int blockSamples = 20;
	public int[] codeCount = new int[5];
	
	
	private short[] filteredData = new short[blockSamples];
	
	/**
	 * Constructor - created the required standard rice tables. 
	 */
	public X3FrameEncode() {
		riceTables = new RiceTable[riceOrders.length];
		for (int i = 0; i < riceOrders.length; i++) {
			riceTables[i]= new RiceTable(riceOrders[i], riceThresholds[i]);
		}
	}
	
	/**
	 * Encode a frame of audio data. 
	 * @param data sound data array
	 * @param packedData packed data array
	 * @param nChan number of channels
	 * @param nSamples number of samples
	 * @return number of bytes of packed data. 
	 */
	public int encodeFrame(short[] data, byte[] packedData, int nChan, int nSamples) {
		X3BitPacker bits = new X3BitPacker(packedData);
		bits.clear();
		// don't leave a gap at the top of the packed data which will later take the header.
		//start by packing the first sample for each channel.
		int bytePos = 0;
		for (int i = 0; i <nChan; i++) {
			packedData[bytePos++] = (byte) ((data[i]>>>8) & 0xFF);
			packedData[bytePos++] = (byte) (data[i] & 0xFF);
		}
		bits.skipBits(nChan*16);
		int nLeft = nSamples - 1;
		int dataPos = nChan;
		while (nLeft > 0) {
			int nBlock = Math.min(nLeft, blockSamples);
			for (int i = 0; i <nChan; i++) {
				packBlock(bits, data, dataPos+i, nBlock,nChan);
			}
			nLeft -= nBlock;
			dataPos += nBlock*nChan;
		}
		return bits.getUsedBytes();
		
	}

	/**
	 * Pack a block of data
	 * @param bits bit array to pack data into
	 * @param data data to pack
	 * @param dataPos position in wav data
	 * @param nBlock number of samples to pack
	 * @param nChan number of channels in data.
	 */
	private void packBlock(X3BitPacker bits, short[] data, int dataPos, int nBlock, int nChan) {
		int ma = filterData(data, dataPos, nBlock, nChan);
		if (ma > riceThresholds[2]) {
			// block encode
			int nb;
			for(nb=0; ma>0; nb++, ma>>=1) ;  // find the number of bits needed to code ma
			if (nb >= 15) {
				bits.setBits(15, 6);                 // add 6 bit BFP header to the bit stream
				blockPack(bits, data, dataPos, 16, nBlock, nChan);
				codeCount[4]++;
			}
			else {
				// don't forget the extra bit which is needed for the sign. 
				bits.setBits(nb, 6);                 // add 6 bit BFP header to the bit stream
				blockPack(bits, filteredData, 0, nb+1, nBlock, 1);
				codeCount[3]++;
			}
			return;
		}
		/*
		 * Otherwise it's rice encoding. 
		 */
		int riceLev = 0;
		if (ma > riceThresholds[0]) riceLev++;
		if (ma > riceThresholds[1]) riceLev++;
		codeCount[riceLev]++;
		RiceTable riceTable = riceTables[riceLev];
		int[] rCodes = riceTable.riceCodes;
		int[] rBits = riceTable.riceBits;
		int rMid = riceTable.midPoint;
		// write a two bit block header
		bits.setBits(riceLev+1, 2);
		// write the data codes.
		for (int i = 0; i < nBlock; i++) {
			bits.setBits(rCodes[rMid+filteredData[i]], rBits[rMid+filteredData[i]]);
		}
	}
	
	/**
	 * Pack a block of data into the bit array using bfp coding
	 * @param bits bit array to pack into
	 * @param data data to pack. This may be filtered or unfiltered (unfiltered used for bfp 16)
	 * @param dataPos start position in the data
	 * @param nBits number of bits to pack
	 * @param nBlock number of samples 
	 * @param stride step between samples in the data. 
	 */
	private void blockPack(X3BitPacker bits, short[] data, int dataPos, int nBits,
			int nBlock, int stride) {
		int pos = dataPos;
		for (int i = 0; i < nBlock; i++, pos += stride) {
			bits.setBits(data[pos], nBits);
		}
	}

	/**
	 * Diff filter the data for a single channel. Note that this always operates 
	 * with dataPos being at least the second sample, so it's always fine to 
	 * pack up to do the diff.  
	 * @param data array of multi channel wav data
	 * @param dataPos position in wav data (including channel offset)
	 * @param nBlock number of samples to filter
	 * @param stride step between samples (number of channels)
	 * @return the maximum absolute number in the filtered data. 
	 */
	private int filterData(short[] data, int dataPos, int nBlock, int stride) {
		int m = 0;
		int pos = dataPos;
		int d;
		for (int i = 0; i < nBlock; i++, pos+=stride) {
			d = (data[pos] - data[pos-stride]);
			m = Math.max(m, Math.abs(d));
			filteredData[i] = (short) d;
		}
		return m;
	}
}
