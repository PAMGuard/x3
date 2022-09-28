package org.pamguard.x3.sud;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat.Encoding;

/**
 * Opens a .sud file as an AudioinputStream.
 * <p>
 * This acts as any other AudioInputStream using an audio buffer and continuous
 * decompression of X3 data to read files. The skip function has been
 * implemented so that it skips through chunks of the file that do not need
 * decompressed and thus will work nearly as fast as uncompressed data.
 * <p>
 * This can be used to integrate sud decompression and file reading, including
 * quick spectrogram scrolling into other Java programs.
 * 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudAudioInputStream extends AudioInputStream {

	/**
	 * Sud files just use a default FMT tag.
	 */
	private final static int FMT_SUD_TAG = 1;

	/**
	 * The sud file expander.
	 */
	private SudFileExpander sudFileExpander;

	/**
	 * A current buffer of audiodata.
	 */
	private byte[] audioBuffer;

	/**
	 * The current read index.
	 */
	private int readIndex = 0;

	/**
	 * The total samples read in all previous buffers. Note that the total samples
	 * read is samplesRead + readIndex.
	 */
	private int bytesRead = 0;

	/**
	 * The total samples in the file.
	 */
	private long totalBytes = 0;

	public SudAudioInputStream(SudFileExpander sudFileExpander, AudioFormat format, long length) {
		super(sudFileExpander.getSudInputStream(), format, length);
		this.sudFileExpander = sudFileExpander;
		this.totalBytes = length;
		sudFileExpander.addSudFileListener((chunkId, chunk) -> {
			// here is the wav data
			if (sudFileExpander.getChunkIDString(chunkId).equals("wav")) {
				sudPrint("New wav data: No. bytes: " + chunk.buffer.length + " Total samples read: " + bytesRead
						+ " of " + totalBytes);
				this.audioBuffer = chunk.buffer;
			}
		});

	}

	/**
	 * The {@code InputStream} from which this {@code AudioInputStream} object was
	 * constructed.
	 */
	private InputStream stream;

	private Chunk currentAudioChunk;

	private int count = 0;

	private boolean verbose;

	private ChunkHeader lastWavChunk;

	/**
	 * Open a sud input stream.
	 * <p>
	 * This function reads a .sud file, figures out if it contains acoustic data,
	 * creates an AudioFormat object form header data and prepares a
	 * SudAudioInputStream ready to read audioData.
	 * 
	 * @param file - the .sud file.
	 * @return a SudAudioInputStream which can be used to stream audio data e.g. in
	 *         a similar way to a raw .wav file.
	 * @throws Exception - throws an exception if the file is incorrect or the .sud
	 *                   file does not contain audio data.
	 */
	public static SudAudioInputStream openInputStream(File file) throws Exception {
		return openInputStream(file, false);
	}

	/**
	 * Open a sud input stream.
	 * <p>
	 * This function reads a .sud file, figures out if it contains acoustic data,
	 * creates an AudioFormat object form header data and prepares a
	 * SudAudioInputStream ready to read audioData.
	 * 
	 * @param file    - the .sud file.
	 * @param verbose - true to print more information on the output stream.
	 * @return a SudAudioInputStream which can be used to stream audio data e.g. in
	 *         a similar way to a raw .wav file.
	 * @throws Exception - throws an exception if the file is incorrect or the .sud
	 *                   file does not contain audio data.
	 */
	public static SudAudioInputStream openInputStream(File file, boolean verbose) throws Exception {

		SudFileExpander sudFileExpander = new SudFileExpander(file);

		/*
		 * Read the .sud header
		 */
		SudHeader sudHeader = sudFileExpander.openSudFile(file);

		// ArrayList<Integer> totalWavSamplesChunk = new ArrayList<Integer>();

		ChunkHeader lastWavChunk = null;
		WavFileHandler wavFileHandler = null;
		long totalSamples = 0;
		// int mark = 0;

		// Iterate through all the sud chunks to figure out which data handlers the sud
		// files need
		ChunkHeader chunkHeader;
		int count = 0;
		while (true) {
			try {
				chunkHeader = ChunkHeader.deSerialise(sudFileExpander.getSudInputStream());

				if (chunkHeader.checkId()) {
					byte[] data = new byte[chunkHeader.DataLength];

					sudFileExpander.getSudInputStream().readFully(data);
					// System.out.println("--------------");
					// System.out.println(chunkHeader.toHeaderString());
					count++;

					// only process chunks if they are XML heades
					if (chunkHeader.ChunkId == 0) {
						sudFileExpander.processChunk(chunkHeader.ChunkId, new Chunk(data, chunkHeader));

						// mark the last point at which a ChunkID of 0 is found. Means we don't need to
						// iterate through
						// this part of the stream again.
						// mark = sudFileExpander.getSudInputStream().available();
					}

					// System.out.println(sudFileExpander.getChunkIDString(chunkHeader.ChunkId));

					// count the number of samples.
					if (sudFileExpander.getChunkIDString(chunkHeader.ChunkId).equals("wav")) {

						sudPrint("HeaderCrc: " + chunkHeader.HeaderCrc + " totalSamples: " + totalSamples, verbose);

						/**
						 * SoundTraps, especially running at high sample rates, might drop samples. The
						 * samples are added as zeros (or not added at all). Counting samples is
						 * therefore a little problematic - we must add the zeros if these are indeed
						 * implemented.
						 * 
						 */
						if (lastWavChunk != null && sudFileExpander.getSudParams().zeroPad) {

							totalSamples = totalSamples + nWavSamples(chunkHeader, lastWavChunk,
									wavFileHandler.getSampleRate(), sudFileExpander.getSudParams().zeroPad);

						} else {
							// this is the first time a wav chunk has been encountered. Get the sample rate.
							// Do we have wav file data handlers? If not then this is not an audio stream
							// and throw an exception.
							ArrayList<IDSudar> dataHandlers = new ArrayList<IDSudar>(
									sudFileExpander.getDataHandlers().values());
							// find the wav file data handler
							for (int i = 0; i < dataHandlers.size(); i++) {
								if (dataHandlers.get(i).dataHandler instanceof WavFileHandler) {
									wavFileHandler = (WavFileHandler) dataHandlers.get(i).dataHandler;
								}
							}

							if (wavFileHandler == null) {
								throw new Exception("The .sud file does not contain any audio data");
							}

						}
						lastWavChunk = chunkHeader;
					}
				}
			} catch (EOFException eof) {
				break;
			}
		}

		sudPrint("No. data handlers: " + sudFileExpander.getDataHandlers().size(), verbose);

		int blockAlign = wavFileHandler.getNChannels() * (wavFileHandler.getBitsPerSample() / 8);

		// Create the audio format from the data in the .sud file data handler header.
		AudioFormat audioFormat = new AudioFormat(getEncoding(FMT_SUD_TAG), wavFileHandler.getSampleRate(),
				wavFileHandler.getBitsPerSample(), wavFileHandler.getNChannels(), blockAlign,
				wavFileHandler.getSampleRate(), false);

		// Now iterate through the file until we get to the very first X3

		sudPrint("Reset the input stream: ", verbose);

		// reset the stream
		sudFileExpander.resetInputStream();

		int available = sudFileExpander.getSudInputStream().available();

		// skip so we are at the start if the chunks.
		// sudFileExpander.getSudInputStream().skip((available-mark));

		// //the number of samples in total.
		// long nFrames = wavHeader.getDataSize() / wavHeader.getBlockAlign();

		SudAudioInputStream sudAudioInputStream = new SudAudioInputStream(sudFileExpander, audioFormat,
				totalSamples * (wavFileHandler.getBitsPerSample() / 8));

		sudAudioInputStream.setVerbose(verbose);

		// get ready with the first chunk
		sudAudioInputStream.nextChunk();

		return sudAudioInputStream;
	}

	/**
	 * Get the wav samples for a chunk of data. This is not straightforward because
	 * the SoundTrap misses some samples which need to be zero filled. This function
	 * calculates the number of samples in the chunkHeader based on the current and
	 * previous chunk header.
	 * 
	 * @param chunkHeader  - the current wav header.
	 * @param lastWavChunk - the previous wav header.
	 * @param fs           - the sample rate in samples per second.
	 * @param zeroPad      - true if zero padding is enabled.
	 * @return the number of wav samples in the current chunk. Note this is in
	 *         SAMPLES not bytes.
	 */
	public static int nWavSamples(ChunkHeader chunkHeader, ChunkHeader lastWavChunk, float fs, boolean zeroPad) {

		if (lastWavChunk == null)
			return chunkHeader.SampleCount;

		if (!zeroPad)
			return chunkHeader.SampleCount;

		int elapsedTimeS = (chunkHeader.TimeS - lastWavChunk.TimeS);

		// the time between chunks in micoseconds
		long elapsedTimeUs = (long) ((elapsedTimeS * 1000000) + (chunkHeader.TimeOffsetUs - lastWavChunk.TimeOffsetUs));

		// the time between chunks calculated using samples.
		long calculatedTime = (long) ((long) lastWavChunk.SampleCount * 1000000 / fs);

		// these two times should be the same,
		int error = (int) (elapsedTimeUs - calculatedTime); // this is the total number of samples to add

		int samplesToAdd = 0;
		if ((Math.abs(error) > (calculatedTime * WavFileHandler.timeErrorWarningThreshold))) {
			samplesToAdd = (int) (error * fs / 1000000);
		}

		return chunkHeader.SampleCount + samplesToAdd;
	}

	/**
	 * Go to the next chunk.
	 */
	private void nextChunk() {
		nextChunk(0);
	}

	/**
	 * Go to the chunk that contains current samples + samplesToSkip
	 * 
	 * @param the number of samples to skip.
	 */
	private void nextChunk(int bytes2Skip) {
		int bytes2SkipLeft = bytes2Skip;
		this.readIndex = 0;
		ChunkHeader chunkHeader;
		this.audioBuffer = null; // reset the audio buffer.
		while (true) {
			try {
				// System.out.println("Deserialise: " +
				// sudFileExpander.getSudInputStream().available());
				chunkHeader = ChunkHeader.deSerialise(sudFileExpander.getSudInputStream());
				count++;

				if (chunkHeader.checkId()) {

					byte[] data = new byte[chunkHeader.DataLength];

					if (sudFileExpander.getChunkIDString(chunkHeader.ChunkId).equals("wav")) {
						// how many samples are in this chunk
						int bytesInChunk = (this.getFormat().getSampleSizeInBits() / 8) * nWavSamples(chunkHeader,
								lastWavChunk, this.getFormat().getSampleRate(), sudFileExpander.getSudParams().zeroPad);

						if (bytes2SkipLeft < bytesInChunk) {

							sudFileExpander.getSudInputStream().readFully(data);

							sudPrint("Chunk ID: " + chunkHeader.ChunkId + "  magic OK? " + chunkHeader.checkId() + " "
									+ sudFileExpander.getChunkIDString(chunkHeader.ChunkId) + "data len: "
									+ data.length);

							// remember that there are no X3 chunks as such - the wav chunk has a source ID
							// which accesses the chunk...
							// only process chunks if they are XML heades
							sudFileExpander.processChunk(chunkHeader.ChunkId, new Chunk(data, chunkHeader));
							// if we are skipping lots of samples then
							// the .sud file listener will now save the decompressed wave data to the
							// buffer. This will be read until
							// the next chunk is required.

							// now if we have any samples to skip need to up these.
							readIndex = readIndex + bytes2SkipLeft;
							bytesRead = bytesRead + bytes2SkipLeft;
							return;
						} else {

							sudPrint("Skip the chunk: ");

							// skip the compressed data here
							sudFileExpander.getSudInputStream().skip(data.length);

							// we have, however, skipped far more raw audio bytes than compressed data.
							bytes2SkipLeft = bytes2SkipLeft - bytesInChunk;
							// keep updating the bytes read
							this.bytesRead = bytesRead + bytesInChunk;
						}

						lastWavChunk = chunkHeader;
					} else {
						// if not a wav file then process the chunk normally - stuff such as .CSV files
						// should be decompressed and saved to the file system.
						sudFileExpander.getSudInputStream().readFully(data);
						sudFileExpander.processChunk(chunkHeader.ChunkId, new Chunk(data, chunkHeader));
					}
				}
			} catch (EOFException eof) {
				// Hmmmmm - this is not the way to do things but there does not seems to be a
				// number of chunks in the header?
				// System.out.println("Close the file: ");
				sudFileExpander.closeFileExpander();
				eof.printStackTrace();
				return;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}

		}
	}

	/**
	 * Obtains the audio format of the sound data in this audio input stream.
	 *
	 * @return an audio format object describing this stream's format
	 */
	public AudioFormat getFormat() {
		return super.getFormat();
	}

	/**
	 * Obtains the length of the stream, expressed in sample frames rather than
	 * bytes.
	 *
	 * @return the length in sample frames
	 */
	public long getFrameLength() {
		return frameLength;
	}

	/**
	 * Reads the next byte of data from the audio input stream. The audio input
	 * stream's frame size must be one byte, or an {@code IOException} will be
	 * thrown.
	 *
	 * @return the next byte of data, or -1 if the end of the stream is reached
	 * @throws IOException if an input or output error occurs
	 * @see #read(byte[], int, int)
	 * @see #read(byte[])
	 * @see #available
	 */
	@Override
	public int read() throws IOException {
		if (audioBuffer == null)
			throw new IOException("The audio buffer is null");
		if ((readIndex) >= audioBuffer.length) {
			nextChunk(0);
		}
		if (audioBuffer == null)
			throw new IOException("The audio buffer is null");
		bytesRead++;
		return audioBuffer[readIndex++];
	}

	/**
	 * Reads some number of bytes from the audio input stream and stores them into
	 * the buffer array {@code b}. The number of bytes actually read is returned as
	 * an integer. This method blocks until input data is available, the end of the
	 * stream is detected, or an exception is thrown.
	 * <p>
	 * This method will always read an integral number of frames. If the length of
	 * the array is not an integral number of frames, a maximum of
	 * {@code b.length - (b.length % frameSize)} bytes will be read.
	 *
	 * @param b the buffer into which the data is read
	 * @return the total number of bytes read into the buffer, or -1 if there is no
	 *         more data because the end of the stream has been reached
	 * @throws IOException if an input or output error occurs
	 * @see #read(byte[], int, int)
	 * @see #read()
	 * @see #available
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Reads up to a specified maximum number of bytes of data from the audio
	 * stream, putting them into the given byte array.
	 * <p>
	 * This method will always read an integral number of frames. If {@code len}
	 * does not specify an integral number of frames, a maximum of
	 * {@code len - (len % frameSize)} bytes will be read.
	 *
	 * @param b   the buffer into which the data is read
	 * @param off the offset, from the beginning of array {@code b}, at which the
	 *            data will be written
	 * @param len the maximum number of bytes to read
	 * @return the total number of bytes read into the buffer, or -1 if there is no
	 *         more data because the end of the stream has been reached
	 * @throws IOException if an input or output error occurs
	 * @see #read(byte[])
	 * @see #read()
	 * @see #skip
	 * @see #available
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = 0;
		for (int i = off; i < (len + off); i++) {
			b[i] = (byte) this.read();
			count++;
		}
		return count;
	}

	/**
	 * Skips over and discards a specified number of bytes from this audio input
	 * stream.
	 * <p>
	 * This method will always skip an integral number of frames. If {@code n} does
	 * not specify an integral number of frames, a maximum of
	 * {@code n - (n % frameSize)} bytes will be skipped.
	 *
	 * @param n the requested number of bytes to be skipped
	 * @return the actual number of bytes skipped
	 * @throws IOException if an input or output error occurs
	 * @see #read
	 * @see #available
	 */
	@Override
	public long skip(long n) throws IOException {
		/**
		 * Skipping is a little tricky because we don't want to decompress every X3
		 * chunk on the way to getting to the right spot.
		 */

		/**
		 * There are two options here; 1) There are enough samples in the buffer for a
		 * skip 2) We need to go through chunks to get to the skipped data.
		 */
		int audioBufLeft = this.audioBuffer.length - readIndex;

		if (n >= audioBufLeft) {
			bytesRead = bytesRead + audioBufLeft;

			// note that the next chunk function will update the samples read now
			nextChunk((int) (n - audioBufLeft));
		} else {
			readIndex = (int) (readIndex + n);
			bytesRead = (int) (bytesRead + n);
		}

		return -1;

	}

	/**
	 * .sud files are a compressed data stream and therefore it is impossible to
	 * know the exact number of samples.
	 */
	@Override
	public int available() throws IOException {
		return (int) (this.totalBytes - bytesRead);
	}

	/**
	 * Closes this audio input stream and releases any system resources associated
	 * with the stream.
	 *
	 * @throws IOException if an input or output error occurs
	 */
	@Override
	public void close() throws IOException {
		this.sudFileExpander.closeFileExpander();
		sudFileExpander.getSudInputStream().close();
	}

	/**
	 * Marks the current position in this audio input stream.
	 *
	 * @param readlimit the maximum number of bytes that can be read before the mark
	 *                  position becomes invalid
	 * @see #reset
	 * @see #markSupported
	 */
	@Override
	public void mark(int readlimit) {
		// do nothing.
	}

	/**
	 * Mark is not supported in .sud files because we are decompressing data as we
	 * go - going back would mean finding the position in compressed data which
	 * would be very complicated - better to just start again.
	 */
	@Override
	public boolean markSupported() {
		return false;
	}

	/**
	 * Repositions this audio input stream to the position it had at the time its
	 * {@code mark} method was last invoked.
	 *
	 * @throws IOException if an input or output error occurs
	 * @see #mark
	 * @see #markSupported
	 */
	@Override
	public void reset() throws IOException {

	}

	static AudioFormat.Encoding getEncoding(int formatCode) {
		switch (formatCode) {
		case 1:
			return Encoding.PCM_SIGNED;
		case 3:
			return Encoding.PCM_FLOAT;
		}
		return null;
	}

	/**
	 * Set to true to print information on the sud audio stream as it expands data.
	 * 
	 * @param verbose - true to set more verbose print statements.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Print a statement of verbose.
	 * 
	 * @param message - the message to print.
	 */
	private void sudPrint(String message) {
		sudPrint(message, verbose);
	}

	/**
	 * Print a statement of verbose.
	 * 
	 * @param message - the message to print.
	 */
	private static void sudPrint(String message, boolean verbose) {
		if (verbose) {
			System.out.println(message);
		}
	}

	/**
	 * Test decompression on a file using a SudAudioInputStream. 
	 * @param args - input args are null. 
	 */
	public static void main(String[] args) {

		long time0 = System.currentTimeMillis();
		String filePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/sud_decompression/singlechan_exmple/67411977.171215195605.sud";
		SudAudioInputStream sudAudioInputStream = null;

		boolean verbose = false; // true to print more stuff.
		try {
			sudAudioInputStream = SudAudioInputStream.openInputStream(new File(filePath), verbose);

			System.out.println("sudAudioInputStream.available(): " + sudAudioInputStream.available());

			sudAudioInputStream.skip(500000 * 2);

			System.out.println("sudAudioInputStream.available(): " + sudAudioInputStream.available());

			while (sudAudioInputStream.available() > 0) {
				sudAudioInputStream.read();
			}
			// while (true) {
			// sudAudioInputStream.read();
			// }
			System.out.println("sudAudioInputStream.available(): " + sudAudioInputStream.available());
			sudAudioInputStream.close();

		} catch (Exception e) {

			e.printStackTrace();
		}
		long time1 = System.currentTimeMillis();

		System.out.println("Processing time: " + (time1 - time0));
	}

}
