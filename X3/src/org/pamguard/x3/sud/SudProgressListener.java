package org.pamguard.x3.sud;


/**
 * The progress listener for file decompression. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface SudProgressListener  {
	
	/**
	 * The progress of the .sud file expansion
	 * @param fileProgress - the progress of the current file (0-1)
	 * @param file - the current file being processed 0->nFiles-1
	 * @param nFiles - the total number of files to be processed.
	 */
	public void progress(double fileProgress, int file, int nFiles);

}
