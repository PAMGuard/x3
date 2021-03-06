package x3;

/**
 * 
 * Functions to decode a single x3 frame. Although the header structures are different in the d3
 * code from Mark and the code I wrote in PAMBuoy, the frame packing is fortunately identical
 * so this code will work with both formats. 
 * @author Doug Gillespie
 *
 */
public class X3FrameDecode {

	static short[] irt = RiceTable.makeInverseRice(20);

	public X3FrameDecode() {
	}

	/**
	 * Unpack a frame of X3 data. 
	 * @param x3Head X3 header
	 * @param x3Data x3 data
	 * @param offset x3 data offset in bytes
	 * @param data Array for wav data
	 * @param blockLen block length in x3 data. 
	 * @return array of unpacked data (generally the same as the input data array unless it needed to be reallocated)
	 */
	public short[] unpackX3Frame(X3FrameHeader x3Head, byte[] x3Data, int offset, short[] data, int blockLen) {
		X3BitPacker bits = new X3BitPacker(x3Data);
		//		first get the data from the block header...
		int nChan = x3Head.getnChan();
		int nFrames = x3Head.getnSamples();
		int iBit = 0;
		if (data == null || data.length != nChan*nFrames) {
			data = new short[nChan*nFrames];
		}
		int j = offset;
		for (int i = 0; i < nChan; i++, j+=2) {
			data[i] = (short) ((x3Data[j]&0xFF)<<8 | (x3Data[j+1]&0xFF));
		}
		nFrames--;
		iBit = 8 * (2 * nChan + offset);
		bits.skipBits(iBit);
		int p = nChan;
		while (nFrames > 0) {
			int nBlock = Math.min(nFrames, blockLen);
			for (int iChan = 0; iChan < nChan; iChan++) {
				int codeType = bits.getBits(2);
				//				System.out.println(String.format("rem frames %d chan %d code %d", nFrames, iChan, codeType));
				iBit +=2;
				switch (codeType) {
				case 1:
					iBit = unpackRice(bits, 0, data, p+iChan, nBlock, nChan);
					break;
				case 2:
					iBit = unpackRice(bits, 1, data, p+iChan, nBlock, nChan);
					break;
				case 3:
					iBit = unpackRice(bits, 3, data, p+iChan, nBlock, nChan);
					break;
				case 0:
					iBit = bfpDecode(bits, data, p+iChan, nBlock, nChan);
					break;
				}
			}
			nFrames -= nBlock;
			p += nBlock*nChan;
		}
		//		System.out.println("Block decoded");
		return data;
	}

	/**
	 * Integrate unpacked wav data. 
	 * @param data data
	 * @param offset offset start point in data
	 * @param n number of samples to integrate
	 * @param stride number of channels of interleaved data
	 */
	private static void integrate(short[] data, int offset, int n, int stride) {
		int p = offset;
		for (int i = 0; i <n; i++, p+= stride) {
			data[p] += data[p-stride];
		}
	}

	/**
	 * Unpack BFP coded data
	 * @param bits bit packed array
	 * @param outData output data array
	 * @param offset offset into output data array
	 * @param n number of samples to unpack
	 * @param stride number of channels of interleaved data
	 * @return 0 on success
	 */
	private static short bfpDecode(X3BitPacker bits, short[] outData, int offset, int n, int stride) {
		int bfBits = bits.getBits(4)+1;
		//		if (bfBits != 8) {
		//			System.out.println("BF Bits: " + bfBits);
		//		}
		// need to read one more bit that it says since the number might have a sign on it. 
		int p = offset;
		for (int i = 0; i < n; i++, p+= stride) {
			outData[p] = bits.getBits(bfBits);
		}
		if (bfBits <= 15) {
			bits.fixSign(outData, bfBits, offset, n, stride);
			integrate(outData, offset, n, stride);
		}
		return 0;
	}


	/**
	 * Unpack rice encoded data
	 * @param bits bit packed array
	 * @param iRice rice order
	 * @param outData output data array
	 * @param offset offset into output data array
	 * @param n number of samples to unpack
	 * @param stride number of channels of interleaved data
	 * @return 0 on success
	 */
	private short unpackRice(X3BitPacker bits, int iRice, short[] outData, int offset, int n, int stride) {
		int p = offset;
		for (int i = 0; i < n; i++, p+= stride) {
			int count = 0;
			while (!bits.getBit()) {
				count++;
			}
			//			int extra = bits.nextBits(iRice);
			int index = (count<<iRice) + bits.getBits(iRice);
			try {
			outData[p] = irt[index];
			}
			catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}
		integrate(outData, offset, n, stride);
		return 0;
	}

}
