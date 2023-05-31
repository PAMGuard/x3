package org.pamguard.x3.sud;

import java.io.File;
import java.util.HashMap; 

import org.apache.commons.io.FilenameUtils;


/**
 * Parameters for sud parameters. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudParams implements Cloneable {
	
	
//	/**
//	 * True to save meta data such as xml files. 
//	 */
//	public boolean saveXML = true;

	/**
	 * True to zero pad the wav files. 
	 */
	public boolean zeroPad = true;
	
//	/**
//	 * True to save wav files. 
//	 */
//	public boolean saveWav = true; 
//	
//	/**
//	 * True to save csv files. 
//	 */
//	public boolean saveCSV = true; 
//	
//	/**
//	 * True to save click diles
//	 */
//	public boolean saveTxtFiles = true; 
//	
//	/**
//	 * True to save wav dwv files. These are the wav data associated with clicks. 
//	 */
//	public boolean saveDwv = true; 
//	
	
	/**
	 * Contains a hasmap of all the file types (called FILESUFFIX in metadata) that should be saved. A file 
	 * will be save if the map returns true OR null. i.e. if this contains no data all filetypes will be saved. 
	 */
	public HashMap<String, Boolean> fileSuffixSave = new HashMap<String, Boolean>(); 
	
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
	 * Convenience function to set the file types that should be saved in the fileSuffixSave hash map. 
	 * @param saveWav - true to save wav files. 
	 * @param saveCSV - true to save CSV files.
	 * @param saveXML - true to save XML files≥ 
	 * @param saveClks- true to save both text and dwv click files.
	 */
	public void setFileSave(boolean saveWav, boolean saveCSV, boolean saveXML, boolean saveClks) {
		fileSuffixSave.put("wav", saveWav); 
		fileSuffixSave.put("csv", saveCSV); 
		fileSuffixSave.put("xml", saveXML); 
		fileSuffixSave.put("bcl", saveClks); 
		fileSuffixSave.put("dwv", saveClks); 
	}

	/**
	 * Convenience function to set whether wav files are saved. 
	 * @param saveWav - true to save wav files. 
	 */
	public void setSaveWav(boolean saveWav) {
		fileSuffixSave.put("wav", saveWav); 
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

	/**
	 * Check whether a particular file type should be saved...
	 * @param fileSuffix - the file to save
	 * @return the file type. 
	 */
	public boolean isFileSave(String fileSuffix) {
		return (fileSuffixSave.get(fileSuffix) ||  fileSuffixSave.get(fileSuffix)==null); 
	}



	
	

}
