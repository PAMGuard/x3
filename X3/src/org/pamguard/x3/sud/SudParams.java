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
	 * Contains a hashmap of all the file types (a key with the data handler type and file suffix) that
	 * should be saved. A file will be save if the map returns true OR null. i.e. if
	 * this contains no data all filetypes will be saved. Note that disabling a file
	 * save does not disable it from processing - listeners etc. will all still be
	 * triggered.
	 */
	public HashMap<ISudarKey, Boolean> fileSuffixSave = new HashMap<ISudarKey, Boolean>();

	/**
	 * Contains a hashmap with file suffix that should be disabled completely. This
	 * means the chunk will not be processed by either it's data handler or any source
	 * data handlers. The chunk will not trigger any listeners. If null key is
	 * returned then the default is true to process
	 */
	public HashMap<ISudarKey, Boolean> fileSuffixDisable = new HashMap<ISudarKey, Boolean>();

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
		if (this.saveFolder != null) {
			String sudName = FilenameUtils.removeExtension(FilenameUtils.getName(sudFilePath));
			// create the new filename
			return this.saveFolder + File.separator + sudName;
		} else {
			return FilenameUtils.removeExtension(sudFilePath);
		}
	}

	@Override
	public SudParams clone() {
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
	 * Convenience function to set the file types that should be saved in the
	 * fileSuffixSave hash map.
	 * 
	 * @param saveWav   - true to save wav files.
	 * @param saveCSV   - true to save CSV files.
	 * @param saveXML   - true to save XML files≥
	 * @param saveClks- true to save both text and dwv click files.
	 */
	public void setFileSave(boolean saveWav, boolean saveCSV, boolean saveXML, boolean saveClks) {
		fileSuffixSave.put(new ISudarKey(ISudarDataHandler.WAV_FTYPE, "wav"), saveWav);
		fileSuffixSave.put(new ISudarKey(ISudarDataHandler.CSV_FTYPE, "csv"), saveCSV);
		fileSuffixSave.put(new ISudarKey(ISudarDataHandler.XML_FTYPE, "xml"), saveXML);
		fileSuffixSave.put(new ISudarKey(ISudarDataHandler.TXT_FTYPE, "bcl"), saveClks);
		fileSuffixSave.put(new ISudarKey(ISudarDataHandler.WAV_FTYPE, "dwv"), saveClks);
	}
	

	/**
	 * Convenience function to set the file types that should be disabled in the
	 * fileSuffixSave hash map. If clicks or wav are disabled then upstream x3
	 * decompression will also be disabled.
	 * <p>
	 * Caution: disabling an upstream process will disable all downstream processes
	 * e.g. disabling X3 will mean no wav will be written.
	 * <p>
	 * Note that xml cannot be disabled. 
	 * 
	 * @param saveWav   - true to save wav files.
	 * @param saveCSV   - true to save CSV files.
	 * @param saveClks- true to save both text and dwv click files.
	 */
	public void setSudEnable(boolean enableWav, boolean enableCSV, boolean enableClicks) {
		fileSuffixDisable.put(new ISudarKey(ISudarDataHandler.WAV_FTYPE, "wav"), enableWav);
		fileSuffixDisable.put(new ISudarKey(ISudarDataHandler.CSV_FTYPE, "csv"), enableCSV);

		fileSuffixDisable.put(new ISudarKey(ISudarDataHandler.TXT_FTYPE, "bcl"), enableClicks);
		fileSuffixDisable.put(new ISudarKey(ISudarDataHandler.WAV_FTYPE, "dwv"), enableClicks);
	}

	/**
	 * Convenience function to set whether wav files are saved.
	 * 
	 * @param saveWav - true to save wav files.
	 */
	public void setSaveWav(boolean saveWav) {
		fileSuffixSave.put(new ISudarKey(ISudarDataHandler.WAV_FTYPE, "wav"), saveWav);
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
	 * 
	 * @param ftype - the type of data - e.g. see ISudarDataHandler.WAV_TYPE
	 * @param fileSuffix -the file suffix is the type of file that is saved e.g. "wav"; 
	 * @return the file type.
	 */
	public boolean isFileSave(String ftype, String fileSuffix) {
		ISudarKey key = new ISudarKey(ftype, fileSuffix); 
		return isFileSave(key); 
	}
	
	/**
	 * Check whether a particular file type should be saved...
	 * 
	 * @param key - the ISudar key that describes the datas stream 
	 * @return the file type.
	 */
	public boolean isFileSave(ISudarKey key) {
		if (fileSuffixSave.get(key) == null) return true; 
		else return fileSuffixSave.get(key).booleanValue();
	}
	
	
	
	/**
	 * Check whether a particular file type should be completely disabled. 
	 * <p>
	 * Caution: disabling an upstream process will disable all downstream processes e.g. disabling X3 
	 * will mean no wav will be written. 
	 * 
	 * @param ftype - the type of data - e.g. see ISudarDataHandler.WAV_TYPE
	 * @param fileSuffix -the file suffix is the type of file that is saved e.g. "wav"; 
	 * @return true if the sud stream is enabled
	 */
	public boolean isSudEnable(String ftype, String fileSuffix) {
		ISudarKey key = new ISudarKey(ftype, fileSuffix); 
		return isSudEnable(key); 
	}
	
	/**
	 * Check whether a particular file type should be completely disabled
	 * <p>
	 * Caution: disabling an upstream process will disable all downstream processes e.g. disabling X3 
	 * will mean no wav will be written. 
	 * 
	 * @param key - the ISudar key that describes the data stream 
	 * @return true if the sud stream is enabled
	 */
	public boolean isSudEnable(ISudarKey key) {
		return (fileSuffixDisable.get(key) || fileSuffixDisable.get(key) == null);
	}


	

}
