package org.pamguard.x3.sud;

import java.io.File;

import org.apache.commons.io.FilenameUtils;


/**
 * Parameters for sud parameters. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudParams implements Cloneable {
	
	
	/**
	 * True to save meta data such as xml files. 
	 */
	public boolean saveMeta = true;

	/**
	 * True to zero pad the wav files. 
	 */
	public boolean zeroPad = true;
	
	/**
	 * True to save wav files. 
	 */
	public boolean saveWav = true; 
	
	/**
	 * The folder to save to. 
	 */
	public String saveFolder = null;
	
	
	private String sudFilePath = null;
	
	/**
	 * True to output more verbose data - use for debugging. 
	 */
	private boolean verbose = false;


	public String getSudFilePath() {
		return sudFilePath;
	}


	public void setSudFilePath(String sudFilePath) {
		this.sudFilePath = sudFilePath;
	} 
	
	
	/***
	 * Get general filename out. The general file name is the file path and name
	 * without an extension - the extension can then be added depending on the file
	 * type
	 * 
	 * @return the filename out e.g. /a/b/c/mynewfile
	 */
	public String getOutFilePath() {
		if (this.saveFolder!=null) {
			String sudName = FilenameUtils.removeExtension(FilenameUtils.getName(sudFilePath));
			//create the new filename
			return this.saveFolder + File.separator + sudName;
		}
		else  {
			return FilenameUtils.removeExtension(sudFilePath);
		}
	}
	
	@Override
	public SudParams clone(){
		SudParams newOne = new SudParams();
		try {
			newOne = (SudParams) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return newOne;		
	}


	/**
	 * @return the verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}


	/**
	 * @param verbose the verbose to set
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	
	
	

}
