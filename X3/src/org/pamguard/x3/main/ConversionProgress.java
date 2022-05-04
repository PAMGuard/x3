package org.pamguard.x3.main;

import java.io.File;

public class ConversionProgress {

	public int state;
	public File sourceFile;
	public File destFile;
	public int nFiles;
	public int iFile;

	public ConversionProgress(int state, File sourceFile, File destFile, int nFiles, int iFile) {
		this.state = state;
		this.sourceFile = sourceFile;
		this.destFile = destFile;
		this.nFiles = nFiles;
		this.iFile = iFile;
	}

}
