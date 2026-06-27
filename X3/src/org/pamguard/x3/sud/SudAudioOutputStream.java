package org.pamguard.x3.sud;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.pamguard.x3.x3.CRC16;
import org.pamguard.x3.x3.X3FrameEncode;

/**
 * Writes audio data to a SUD file using X3 compression.
 * <p>
 * SUD files are the file format used by SoundTrap devices. They contain chunks
 * of X3-compressed audio data along with XML metadata defining the audio
 * format. This class writes a SUD file that is compatible with
 * {@code SudAudioInputStream} for reading.
 * <p>
 * The SUD file structure is:
 * <ol>
 * <li>A fixed-size file header ({@code SudHeader})</li>
 * <li>XML metadata chunks (ChunkId=0) that define the X3 and WAV data
 * handlers</li>
 * <li>Audio data chunks containing X3-compressed, byte-swapped samples</li>
 * </ol>
 * <p>
 * Multiple audio channels are supported. Audio samples must be provided as
 * interleaved short arrays (ch0s0, ch1s0, ch0s1, ch1s1, ...).
 * <p>
 * Usage:
 * 
 * <pre>
 * try (SudAudioOutputStream out = new SudAudioOutputStream(new FileOutputStream("audio.sud"), 96000, 2)) {
 * 	short[] samples = ...; // interleaved samples, nSamples * nChannels
 * 	out.write(samples, nSamples);
 * }
 * </pre>
 *
 * @author Jamie Macaulay
 */
public class SudAudioOutputStream implements Closeable {

	/**
	 * XML metadata chunk ID. All XML configuration chunks use this ID.
	 */
	private static final int XML_CHUNK_ID = 0;

	/**
	 * The chunk ID assigned to the X3 compression handler in the XML metadata.
	 * This handler is referenced as the source for WAV_HANDLER_ID chunks.
	 */
	private static final int X3_HANDLER_ID = 1;

	/**
	 * The chunk ID used for all audio data chunks. Each chunk processed with this
	 * ID is first decompressed through the X3 handler (X3_HANDLER_ID), then
	 * processed as WAV audio.
	 */
	private static final int WAV_HANDLER_ID = 2;

	/**
	 * The magic number that must appear at the start of every valid chunk header.
	 */
	private static final int CHUNK_MAGIC = 0xA952;

	/**
	 * Default number of audio samples per chunk.
	 */
	private static final int DEFAULT_CHUNK_SAMPLES = 1000;

	/** Underlying output stream. */
	private final DataOutputStream out;

	/** Audio sample rate in Hz. */
	private final int sampleRate;

	/** Number of audio channels. */
	private final int nChannels;

	/** Bits per sample – always 16 for SUD/X3. */
	private final int bitsPerSample = 16;

	/** Number of samples per audio chunk. */
	private final int chunkSamples;

	/** X3 encoder instance reused across chunks. */
	private final X3FrameEncode x3Encoder;

	/** Recording start time in milliseconds since the Unix epoch. */
	private final long startTimeMs;

	/** Running total of samples written (per channel). */
	private long totalSamplesWritten = 0;

	/**
	 * Creates a {@code SudAudioOutputStream} using the current wall-clock time as
	 * the recording start time and the default chunk size.
	 *
	 * @param out        the underlying output stream; the caller retains ownership
	 *                   of flushing/closing if this constructor throws
	 * @param sampleRate audio sample rate in Hz
	 * @param nChannels  number of audio channels (≥ 1)
	 * @throws IOException if writing the SUD file header or XML metadata fails
	 */
	public SudAudioOutputStream(OutputStream out, int sampleRate, int nChannels) throws IOException {
		this(out, sampleRate, nChannels, DEFAULT_CHUNK_SAMPLES, System.currentTimeMillis());
	}

	/**
	 * Creates a {@code SudAudioOutputStream} with full control over chunk size and
	 * start time.
	 *
	 * @param out          the underlying output stream
	 * @param sampleRate   audio sample rate in Hz
	 * @param nChannels    number of audio channels (≥ 1)
	 * @param chunkSamples number of samples per channel per audio chunk
	 * @param startTimeMs  recording start time in ms since the Unix epoch
	 * @throws IOException if writing the SUD file header or XML metadata fails
	 */
	public SudAudioOutputStream(OutputStream out, int sampleRate, int nChannels, int chunkSamples, long startTimeMs)
			throws IOException {
		this.out = new DataOutputStream(out);
		this.sampleRate = sampleRate;
		this.nChannels = nChannels;
		this.chunkSamples = chunkSamples;
		this.startTimeMs = startTimeMs;
		this.x3Encoder = new X3FrameEncode();

		writeFileHeader();
		writeXmlMetadata();
	}

	/**
	 * Writes the 30-byte SUD file header.
	 * <p>
	 * The file header is the very first data in a SUD file. Its fields use
	 * little-endian byte order for 16-bit values and a "PDP-endian" (middle-endian)
	 * byte order for 32-bit values – matching what
	 * {@code SudDataInputStream.readInt()} expects.
	 */
	private void writeFileHeader() throws IOException {
		int nowSecs = (int) (startTimeMs / 1000L);

		writeUInt16(1); // HostCodeVersion
		writeInt32PDP(nowSecs); // HostTime
		out.writeByte(0); // DeviceType
		out.writeByte(0); // DeviceCodeVersion
		writeInt32PDP(nowSecs); // DeviceTime
		writeInt32PDP(0); // DeviceIdentifier
		writeInt32PDP(chunkSamples * (bitsPerSample / 8) * nChannels); // BlockLength
		writeUInt16(0); // StartBlock
		writeUInt16(0); // EndBlock
		writeInt32PDP(0); // NoOfBlocks (unknown at write time)
		writeUInt16(0); // Crc (not verified by the reader)
	}

	/**
	 * Writes the two XML metadata chunks that define the audio data handlers.
	 * <p>
	 * Two XML chunks are required to avoid assigning the same source ID ({@code
	 * srcID}) to both handlers, which would cause infinite recursion in the reader:
	 * <ul>
	 * <li>Chunk 1 – defines the X3 decompression handler (ID={@value #X3_HANDLER_ID},
	 * no source).</li>
	 * <li>Chunk 2 – defines the WAV output handler (ID={@value #WAV_HANDLER_ID},
	 * source={@value #X3_HANDLER_ID}).</li>
	 * </ul>
	 * Each chunk's payload is the UTF-8 XML string pairwise byte-swapped, which is
	 * exactly what {@code XMLFileHandler.processChunk()} reverses on reading.
	 */
	private void writeXmlMetadata() throws IOException {
		int nowSecs = (int) (startTimeMs / 1000L);

		// Chunk 1: X3 handler – no <SRC>, so srcID=0 (no upstream handler).
		String xml1 = "<SUDAR>"
				+ "<CFG ID=\"" + X3_HANDLER_ID + "\" FTYPE=\"x3v2\">"
				+ "<NCHS>" + nChannels + "</NCHS>"
				+ "<FS>" + sampleRate + "</FS>"
				+ "<NBITS>" + bitsPerSample + "</NBITS>"
				+ "<BLKLEN>" + X3FrameEncode.blockSamples + "</BLKLEN>"
				+ "</CFG>"
				+ "</SUDAR>";
		writeXmlChunk(xml1, nowSecs);

		// Chunk 2: WAV handler – <SRC ID="X3_HANDLER_ID"/> sets srcID so that
		// each audio chunk is first decompressed through the X3 handler.
		String xml2 = "<SUDAR>"
				+ "<SRC ID=\"" + X3_HANDLER_ID + "\"/>"
				+ "<CFG ID=\"" + WAV_HANDLER_ID + "\" FTYPE=\"wav\">"
				+ "<FS>" + sampleRate + "</FS>"
				+ "<NCHS>" + nChannels + "</NCHS>"
				+ "<SUFFIX>wav</SUFFIX>"
				+ "<TIMECHK>1</TIMECHK>"
				+ "<NBITS>" + bitsPerSample + "</NBITS>"
				+ "<CHANNEL>-1</CHANNEL>"
				+ "</CFG>"
				+ "</SUDAR>";
		writeXmlChunk(xml2, nowSecs);
	}

	/**
	 * Encodes an XML string as a SUD chunk with {@value #XML_CHUNK_ID}.
	 * <p>
	 * The XML bytes are pairwise byte-swapped before storage – this is reversed
	 * automatically by {@code XMLFileHandler.processChunk()} on reading. The byte
	 * array is padded to an even length when necessary, since the SUD CRC function
	 * operates on 16-bit words.
	 */
	private void writeXmlChunk(String xml, int timeSecs) throws IOException {
		byte[] xmlBytes = xml.getBytes("UTF-8");

		// Pad to an even length (CRC and swapEndian both work on pairs of bytes).
		if (xmlBytes.length % 2 != 0) {
			byte[] padded = new byte[xmlBytes.length + 1];
			System.arraycopy(xmlBytes, 0, padded, 0, xmlBytes.length);
			xmlBytes = padded;
		}

		// The reader byte-swaps before interpreting XML, so write pre-swapped bytes.
		XMLFileHandler.swapEndian(xmlBytes);

		writeChunk(XML_CHUNK_ID, xmlBytes, 0, timeSecs, 0);
	}

	/**
	 * Encodes and writes a frame of audio samples as a SUD chunk.
	 * <p>
	 * The supplied samples must be interleaved across channels:
	 * {@code [ch0s0, ch1s0, ..., chNs0, ch0s1, ch1s1, ..., chNs1, ...]}.
	 * <p>
	 * Each call to this method encodes exactly {@code nSamples} samples per channel
	 * using X3 compression, byte-swaps the result, computes the data CRC, and
	 * writes a complete SUD chunk to the stream.
	 *
	 * @param samples  interleaved short samples ({@code nSamples * nChannels}
	 *                 values)
	 * @param nSamples number of samples <em>per channel</em>
	 * @throws IOException if writing to the underlying stream fails
	 */
	public void write(short[] samples, int nSamples) throws IOException {
		// Allocate a buffer guaranteed large enough for the worst-case X3 output.
		// X3 can occasionally produce slightly more bytes than raw PCM (rare edge case);
		// using 3× the raw size provides a comfortable safety margin.
		byte[] packedData = new byte[nSamples * nChannels * 3 + 16];

		int nBytes = x3Encoder.encodeFrame(samples, packedData, nChannels, nSamples);

		// Pad to an even number of bytes so that the pair-wise CRC and swapEndian
		// functions operate correctly.
		if (nBytes % 2 != 0) {
			nBytes++;
		}

		byte[] data = new byte[nBytes];
		System.arraycopy(packedData, 0, data, 0, nBytes);

		// The X3 decoder in X3Handler swaps bytes before feeding them to
		// the bit unpacker, so we must pre-swap here.
		XMLFileHandler.swapEndian(data);

		// Compute timing fields from total samples written so far.
		long elapsedMicros = (totalSamplesWritten * 1_000_000L) / sampleRate;
		int timeSecs = (int) (startTimeMs / 1000L + elapsedMicros / 1_000_000L);
		int timeOffsetUs = (int) (elapsedMicros % 1_000_000L);

		writeChunk(WAV_HANDLER_ID, data, nSamples, timeSecs, timeOffsetUs);

		totalSamplesWritten += nSamples;
	}

	/**
	 * Writes a SUD chunk (header + data) to the stream.
	 * <p>
	 * The 20-byte chunk header layout is:
	 * <pre>
	 * bytes  0-1   majicNo      (UInt16 LE)
	 * bytes  2-3   ChunkId      (UInt16 LE)
	 * bytes  4-5   DataLength   (UInt16 LE)
	 * bytes  6-7   SampleCount  (UInt16 LE)
	 * bytes  8-11  TimeS        (Int32  PDP-endian)
	 * bytes 12-15  TimeOffsetUs (Int32  PDP-endian)
	 * bytes 16-17  DataCrc      (UInt16 LE)
	 * bytes 18-19  HeaderCrc    (UInt16 LE)
	 * </pre>
	 *
	 * @param chunkId     the chunk identifier
	 * @param data        the payload bytes (already byte-swapped if required)
	 * @param sampleCount number of audio samples represented by this chunk (0 for
	 *                    XML chunks)
	 * @param timeSecs    Unix timestamp in whole seconds
	 * @param timeOffsetUs microsecond offset within the current second
	 */
	private void writeChunk(int chunkId, byte[] data, int sampleCount, int timeSecs, int timeOffsetUs)
			throws IOException {
		int dataLength = data.length;

		// Compute the CRC over the raw payload bytes as they appear in the file.
		int dataCrc = CRC16.calcSUD(data, dataLength) & 0xFFFF;

		byte[] header = new byte[20];

		// majicNo (UInt16 LE)
		putUInt16LE(header, 0, CHUNK_MAGIC);
		// ChunkId (UInt16 LE)
		putUInt16LE(header, 2, chunkId);
		// DataLength (UInt16 LE)
		putUInt16LE(header, 4, dataLength);
		// SampleCount (UInt16 LE)
		putUInt16LE(header, 6, sampleCount);
		// TimeS (Int32 PDP-endian)
		putInt32PDP(header, 8, timeSecs);
		// TimeOffsetUs (Int32 PDP-endian)
		putInt32PDP(header, 12, timeOffsetUs);
		// DataCrc (UInt16 LE)
		putUInt16LE(header, 16, dataCrc);
		// HeaderCrc (UInt16 LE) – compute CRC over bytes 2..17 (16 bytes at offset 2).
		// The reader reads this field but does not verify it; computed for completeness.
		int headerCrc = Short.toUnsignedInt(CRC16.getCRC16(header, 16, 2));
		putUInt16LE(header, 18, headerCrc);

		out.write(header);
		out.write(data);
	}

	// -------------------------------------------------------------------------
	// Binary serialisation helpers
	// -------------------------------------------------------------------------

	/**
	 * Writes a 16-bit unsigned integer as two bytes in little-endian order.
	 * This matches {@code SudDataInputStream.readUnsignedShort()}.
	 */
	private void writeUInt16(int value) throws IOException {
		out.write(value & 0xFF);
		out.write((value >> 8) & 0xFF);
	}

	/**
	 * Writes a 32-bit integer in PDP (middle-endian) byte order.
	 * <p>
	 * {@code SudDataInputStream.readInt()} returns:
	 * {@code w[1]<<24 | w[0]<<16 | w[3]<<8 | w[2]},
	 * so to write value {@code V} we lay down bytes:
	 * {@code [(V>>16)&FF, (V>>24)&FF, V&FF, (V>>8)&FF]}.
	 */
	private void writeInt32PDP(int value) throws IOException {
		out.write((value >> 16) & 0xFF); // w[0]
		out.write((value >> 24) & 0xFF); // w[1]
		out.write(value & 0xFF);         // w[2]
		out.write((value >> 8) & 0xFF);  // w[3]
	}

	/**
	 * Places a 16-bit unsigned integer at the given byte offset in little-endian
	 * order.
	 */
	private static void putUInt16LE(byte[] buf, int offset, int value) {
		buf[offset] = (byte) (value & 0xFF);
		buf[offset + 1] = (byte) ((value >> 8) & 0xFF);
	}

	/**
	 * Places a 32-bit integer at the given byte offset in PDP (middle-endian)
	 * order, matching the layout expected by
	 * {@code SudDataInputStream.readInt()}.
	 */
	private static void putInt32PDP(byte[] buf, int offset, int value) {
		buf[offset] = (byte) ((value >> 16) & 0xFF); // w[0]
		buf[offset + 1] = (byte) ((value >> 24) & 0xFF); // w[1]
		buf[offset + 2] = (byte) (value & 0xFF);          // w[2]
		buf[offset + 3] = (byte) ((value >> 8) & 0xFF);   // w[3]
	}

	// -------------------------------------------------------------------------
	// Closeable
	// -------------------------------------------------------------------------

	/**
	 * Flushes and closes the underlying stream.
	 *
	 * @throws IOException if closing fails
	 */
	@Override
	public void close() throws IOException {
		out.flush();
		out.close();
	}
}
