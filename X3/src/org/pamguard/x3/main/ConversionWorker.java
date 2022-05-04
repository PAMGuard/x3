package org.pamguard.x3.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

import org.pamguard.x3.gui.X3Gui;
import org.pamguard.x3.x3.X3Encoder;
import org.pamguard.x3.x3.X3FileSystem;
import org.pamguard.x3.x3.X3JNIEncoder;
import org.pamguard.x3.x3.X3JavaEncoder;

public class ConversionWorker extends SwingWorker<Integer, ConversionProgress>{

	private X3Encoder x3Encoder;

	private X3Gui x3Gui;
	private ArrayList<File> sourceList;
	private ArrayList<File> destList;
	private int overwriteOption;
	private volatile boolean keepGoing = true;
	private int sourceType;

	private X3JNIEncoder x3OldEncoder;

	public ConversionWorker(X3Gui x3Gui, ArrayList<File> sourceList, ArrayList<File> destList, int sourceType, int overwriteOption) {
		this.x3Gui = x3Gui;
		this.sourceList = sourceList;
		this.destList = destList;
		this.sourceType = sourceType;
		this.overwriteOption = overwriteOption;
		x3OldEncoder = new X3JNIEncoder();
		x3Encoder = new X3JavaEncoder();
	}
	
	public void stopNow() {
		this.keepGoing  = false;
	}

	@Override
	protected Integer doInBackground() throws Exception {
		for (int i = 0; i < sourceList.size(); i++) {
			if (keepGoing == false) {
				return null;
			}
			
			publish(new ConversionProgress(1, sourceList.get(i), destList.get(i), sourceList.size(), i));

			convertFile(sourceList.get(i), destList.get(i));

//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		publish(new ConversionProgress(2, null, null, sourceList.size(), sourceList.size()));
		return null;
	}

	private void convertFile(File sourceFile, File destFile) {
		// check the dest file path exists.
		String destParent = destFile.getParent();
		if (destParent != null) {
			File destFolder = new File(destParent);
			if (destFolder.exists() == false) {
				destFolder.mkdirs();
			}
		}
		if (destFile.exists() && overwriteOption != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		switch(sourceType) {
		case X3Gui.X3_FILES:
			/*
			 * Need to work out if it's the old Decimus format of the newer official d3 format
			 */
			int x3Type = X3FileSystem.getX3Type(sourceFile);
			switch (x3Type) {
			case X3FileSystem.X3_UNKNOWN:
				break;
			case X3FileSystem.X3_PAMBUOY:
				x3OldEncoder.x3ToWav(sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
				break;
			case X3FileSystem.X3_D3X3A:
				x3Encoder.x3ToWav(sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
				break;
			}
			break;
		case X3Gui.WAV_FILES:
			x3Encoder.wavToX3(sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
		}
			
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process(List<ConversionProgress> conList) {
		Iterator<ConversionProgress> it = conList.iterator();
		while (it.hasNext()) {
			x3Gui.publishProgress(it.next());
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		x3Gui.conversionComplete();
	}

	/**
	 * @return the sourceList
	 */
	public ArrayList<File> getSourceList() {
		return sourceList;
	}

	/**
	 * @return the destList
	 */
	public ArrayList<File> getDestList() {
		return destList;
	}

	/**
	 * @return the overwriteOption
	 */
	public int getOverwriteOption() {
		return overwriteOption;
	}

	/**
	 * @return the keepGoing
	 */
	public boolean isKeepGoing() {
		return keepGoing;
	}

	/**
	 * @return the sourceType
	 */
	public int getSourceType() {
		return sourceType;
	}
	
	

}
