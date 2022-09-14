package org.pamguard.x3.sud;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;


public class SudAudioInputStream extends AudioInputStream {
	
	/**
	 * Sud files just use a default FMT tag. 
	 */
	private final static int FMT_SUD_TAG = 1;

	public SudAudioInputStream(InputStream stream, AudioFormat format, long length) {
		super(stream, format, length);
		// TODO Auto-generated constructor stub
	}

	/**
	 * The {@code InputStream} from which this {@code AudioInputStream} object
	 * was constructed.
	 */
	private InputStream stream;
	
	private Chunk currentAudioChunk;
	
	
	public static SudAudioInputStream openInputStream(File file) throws Exception
	{
		
		SudFileExpander sudFileExpander = new SudFileExpander(file); 
		
		/*
		 * Read the .sud header 
		 */
		SudHeader sudHeader = sudFileExpander.openSudFile(file);
		
		ChunkHeader chunkHeader;
		int count = 0; 
		while(true){
			try {
				chunkHeader = ChunkHeader.deSerialise(sudFileExpander.getSudInputStream());

				if (chunkHeader.checkId()) {
					byte[] data = new byte[chunkHeader.DataLength];
					
					sudFileExpander.getSudInputStream().readFully(data);
					//				System.out.println("--------------");
					//				System.out.println(chunkHeader.toHeaderString());
					count++;
					
					//only process chunks if they are XML heades
					if (chunkHeader.ChunkId==0) {
						sudFileExpander.processChunk(chunkHeader.ChunkId, new Chunk(data, chunkHeader));
					}
				}
			}
			catch (EOFException eof) {
				break;
			}
		}
		
		IDSudar[] dataHandlers = 	(IDSudar[]) sudFileExpander.getDataHandlers().values().toArray();
		WavFileHandler wavFileHandler = null;
		//find the wav file data handler
		for (int i=0; i<dataHandlers.length ; i++) {
			if (dataHandlers[i].dataHandler instanceof WavFileHandler) {
				wavFileHandler = (WavFileHandler) dataHandlers[i].dataHandler;
			}
		}
		
		if (wavFileHandler == null) {
			throw new Exception("The sud file does not contain any audio data");
		}
		
		
		System.out.println("Data Handlers: " + sudFileExpander.getDataHandlers().size()); 
		
		int blockAlign = wavFileHandler.getNChannels() * (wavFileHandler.getBitsPerSample() / 8);

//		
		AudioFormat audioFormat = new AudioFormat(getEncoding(FMT_SUD_TAG), 
				wavFileHandler.getSampleRate(), wavFileHandler.getBitsPerSample(), wavFileHandler.getNChannels(), 
				blockAlign, wavFileHandler.getSampleRate(), false);
//		
//		
//
//		//the number of samples in total. 
//		long nFrames = wavHeader.getDataSize() / wavHeader.getBlockAlign();
		
	
		
		return null; 
	}


	/**
	 * Obtains the audio format of the sound data in this audio input stream.
	 *
	 * @return an audio format object describing this stream's format
	 */
	public AudioFormat getFormat() {
		return null;
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
		return -1; //TODO
	}

	/**
	 * Reads some number of bytes from the audio input stream and stores them
	 * into the buffer array {@code b}. The number of bytes actually read is
	 * returned as an integer. This method blocks until input data is available,
	 * the end of the stream is detected, or an exception is thrown.
	 * <p>
	 * This method will always read an integral number of frames. If the length
	 * of the array is not an integral number of frames, a maximum of
	 * {@code b.length - (b.length % frameSize)} bytes will be read.
	 *
	 * @param  b the buffer into which the data is read
	 * @return the total number of bytes read into the buffer, or -1 if there is
	 *         no more data because the end of the stream has been reached
	 * @throws IOException if an input or output error occurs
	 * @see #read(byte[], int, int)
	 * @see #read()
	 * @see #available
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}

	/**
	 * Reads up to a specified maximum number of bytes of data from the audio
	 * stream, putting them into the given byte array.
	 * <p>
	 * This method will always read an integral number of frames. If {@code len}
	 * does not specify an integral number of frames, a maximum of
	 * {@code len - (len % frameSize)} bytes will be read.
	 *
	 * @param  b the buffer into which the data is read
	 * @param  off the offset, from the beginning of array {@code b}, at which
	 *         the data will be written
	 * @param  len the maximum number of bytes to read
	 * @return the total number of bytes read into the buffer, or -1 if there is
	 *         no more data because the end of the stream has been reached
	 * @throws IOException if an input or output error occurs
	 * @see #read(byte[])
	 * @see #read()
	 * @see #skip
	 * @see #available
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return -1; //TODO
	}

	/**
	 * Skips over and discards a specified number of bytes from this audio input
	 * stream.
	 * <p>
	 * This method will always skip an integral number of frames. If {@code n}
	 * does not specify an integral number of frames, a maximum of
	 * {@code n - (n % frameSize)} bytes will be skipped.
	 *
	 * @param  n the requested number of bytes to be skipped
	 * @return the actual number of bytes skipped
	 * @throws IOException if an input or output error occurs
	 * @see #read
	 * @see #available
	 */
	@Override
	public long skip(long n) throws IOException {
		return -1; //TODO
	}

	/**
	 * Returns the maximum number of bytes that can be read (or skipped over)
	 * from this audio input stream without blocking. This limit applies only to
	 * the next invocation of a {@code read} or {@code skip} method for this
	 * audio input stream; the limit can vary each time these methods are
	 * invoked. Depending on the underlying stream, an {@code IOException} may
	 * be thrown if this stream is closed.
	 *
	 * @return the number of bytes that can be read from this audio input stream
	 *         without blocking
	 * @throws IOException if an input or output error occurs
	 * @see #read(byte[], int, int)
	 * @see #read(byte[])
	 * @see #read()
	 * @see #skip
	 */
	@Override
	public int available() throws IOException {
		return -1; //TODO
	}

	/**
	 * Closes this audio input stream and releases any system resources
	 * associated with the stream.
	 *
	 * @throws IOException if an input or output error occurs
	 */
	@Override
	public void close() throws IOException {
		stream.close();
	}

	/**
	 * Marks the current position in this audio input stream.
	 *
	 * @param  readlimit the maximum number of bytes that can be read before the
	 *         mark position becomes invalid
	 * @see #reset
	 * @see #markSupported
	 */
	@Override
	public void mark(int readlimit) {
		stream.mark(readlimit);
	}

	/**
	 * Repositions this audio input stream to the position it had at the time
	 * its {@code mark} method was last invoked.
	 *
	 * @throws IOException if an input or output error occurs
	 * @see #mark
	 * @see #markSupported
	 */
	@Override
	public void reset() throws IOException {
		stream.reset();
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

	
	public static void main(String[] args) {
		
		long time0 = System.currentTimeMillis();
		String filePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/sud_decompression/singlechan_exmple/67411977.171215195605.sud";

		try {
			SudAudioInputStream sudAudioInputStream = SudAudioInputStream.openInputStream(new File(filePath));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		long time1 = System.currentTimeMillis();
		
		System.out.println("Processing time: " +  (time1-time0));

		
	}
	


}
