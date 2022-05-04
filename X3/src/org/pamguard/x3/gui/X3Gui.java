package org.pamguard.x3.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.pamguard.x3.main.ConversionProgress;
import org.pamguard.x3.main.ConversionWorker;
import org.pamguard.x3.main.X3Params;

public class X3Gui {

	private ExplorePanel x3Panel;
	private ExplorePanel wavPanel;
	
	public static final int WAV_FILES = 1;
	public static final int X3_FILES = 2;
	
	private X3Params x3Params = new X3Params();
	private JFrame mainFrame;
	private ProgressPanel progressPanel;
	private boolean enabled = true;
	private ConversionWorker conWorker;

	public X3Gui() {
		loadParams();
		
		mainFrame = new JFrame("X3 Unpacker");
		mainFrame.setSize(new Dimension(800,600));
		mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new GUIFrameListener());
		JPanel mainPanel = new JPanel();
		mainFrame.setLayout(new BorderLayout());
		mainFrame.add(BorderLayout.CENTER, mainPanel);
		mainPanel.setLayout(new GridLayout(1, 2));
		String[] xTypes = {".x3a", ".x3"};
		String[] wavTypes = {".wav"};
		x3Panel = new ExplorePanel(this, X3_FILES, "X3 Files", xTypes);
		wavPanel = new ExplorePanel(this, WAV_FILES, "WAV Files", wavTypes);
		
		mainPanel.add(x3Panel.getMainPanel());
		mainPanel.add(wavPanel.getMainPanel());
		
		progressPanel = new ProgressPanel();
		mainFrame.add(BorderLayout.SOUTH, progressPanel.getProgressPanel());
		
		
		if (x3Params.locationOnScreen != null) {
			mainFrame.setLocation(x3Params.locationOnScreen);
		}

		mainFrame.setVisible(true);
		mainFrame.pack();
		
		SwingUtilities.invokeLater(new SetupWindows());
		
//		mainPanel.add(new ExplorePanel().getMainPanel());
	}
	
	class SetupWindows implements Runnable {

		@Override
		public void run() {
			wavPanel.setCurrentFolder(x3Params.wavFolder);
			x3Panel.setCurrentFolder(x3Params.x3Folder);			
		}
		
	}

	private boolean saveParams() {
		x3Params.wavFolder = wavPanel.getCurrentFolder();
		x3Params.x3Folder = x3Panel.getCurrentFolder();
		x3Params.locationOnScreen = mainFrame.getLocation();
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getParamsFile()));
			oos.writeObject(x3Params);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadParams() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getParamsFile()));
			Object o = ois.readObject();
			if (o == null) {
				return false;
			}
			if (o.getClass() == X3Params.class) {
				x3Params = (X3Params) o;
				return true;
			}
		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
//			return false;
		}
		return false;
	}
	
	private File getParamsFile() {
		return new File(getSettingsFolder() + File.separator + "x3inflateconfig.ser");
	}
	/**
	 * Get the settings folder name and if necessary, 
	 * create the folder since it may not exist. 
	 * @return folder name string, (with no file separator on the end)
	 */
	private String getSettingsFolder() {
		String settingsFolder = System.getProperty("user.home");
		settingsFolder += File.separator + "Pamguard";
		// now check that folder exists
		File f = new File(settingsFolder);
		if (f.exists() == false) {
			f.mkdirs();
		}
		return settingsFolder;
	}
	
	private class GUIFrameListener extends WindowAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
		 */
		@Override
		public void windowClosing(WindowEvent arg0) {
			if (enabled == false) {
				return;
			}
			saveParams();
			System.exit(0);
		}
		
	}

	/**
	 * Called when the Convert button is pressed on either x3 or wav selection. 
	 * @param explorePanel
	 * @param selFiles
	 */
	public void convertFiles(ExplorePanel explorePanel, File[] selFiles) {
		if (enabled == false) {
			return;
		}
		if (selFiles == null || selFiles.length == 0) {
			return;
		}
		ExplorePanel otherPanel = getOtherPanel(explorePanel);
		File folder1 = explorePanel.getCurrentFolder();
		File folder2 = otherPanel.getCurrentFolder();
		ArrayList<File> fileList = getFullFileList(null, selFiles, explorePanel.getFileFilter());
		int nFiles = fileList.size();
		int nFolders = 0;
		for (int i = 0; i < selFiles.length; i++) {
			if (selFiles[i].isDirectory()) {
				nFolders++;
			}
		}
		String msg = String.format("Convert %d files in %d sub folders", nFiles, nFolders);
		int ans = JOptionPane.showConfirmDialog(mainFrame,  msg, "Convert " + explorePanel.getFileEnd() + " files", JOptionPane.YES_NO_CANCEL_OPTION);
		if (ans != JOptionPane.YES_OPTION) {
			System.out.println("Conversion cancelled");
			return;
		}
		
		ArrayList<File> destList = getDestinationList(explorePanel.getCurrentFolder(),  
				otherPanel.getCurrentFolder(), fileList, explorePanel.getFileEnd(), otherPanel.getFileEnd());
		// check how many exist
		int nExist = 0;
		for (int i = 0; i < destList.size(); i++) {
			if (destList.get(i).exists()) {
				nExist++;
			}
		}
		int overWriteOption = JOptionPane.NO_OPTION;
		if (nExist > 0) {
			msg = String.format("%d of %d destination files already exist. Do you want to overwrite them ?", nExist, nFiles);
			overWriteOption = JOptionPane.showConfirmDialog(mainFrame,  msg, "Convert " + explorePanel.getFileEnd() + " files", JOptionPane.YES_NO_CANCEL_OPTION);
		}
		if (overWriteOption == JOptionPane.CANCEL_OPTION) {
			return;
		}
		
		conWorker = new ConversionWorker(this, fileList, destList, explorePanel.getFileType(), overWriteOption);
		conWorker.execute();
		
		enableGUI(false);
	}
	
	private void enableGUI(boolean b) {
		x3Panel.setEnabled(b);
		wavPanel.setEnabled(b);		
		enabled  = b;
	}

	/**
	 * convert a list of source files into a list of destination files. 
	 * @param currentFolder
	 * @param currentFolder2
	 * @param fileList
	 * @param fileEnd2 
	 * @param fileEnd1 
	 * @return
	 */
	private ArrayList<File> getDestinationList(File currentFolder,
			File destFolder, ArrayList<File> fileList, String fileEnd1, String fileEnd2) {
		// for each file, need to strip off the current folder, retaining any sub folder 
		// information and then add that into the new folder
		ArrayList<File> destList = new ArrayList<File>();
		for (int i = 0; i < fileList.size(); i++) {
			File aFile = fileList.get(i);
			String path = aFile.getAbsolutePath();
			String end = path.replace(currentFolder.getAbsolutePath(), destFolder.getAbsolutePath());
			int lastDot = end.lastIndexOf('.');
			if (lastDot > 0) {
				end = end.substring(0, lastDot) + fileEnd2;
			}
			else {
				end = end.replace(fileEnd1, fileEnd2);
			}
			destList.add(new File(end));
//			System.out.println("Convert " + aFile.getAbsolutePath() + " to " + end);
		}
		return destList;
	}

	/**
	 * Get a fully recursive list of all files within a set of files and folders. 
	 * @param fileList
	 * @param filesAndFolders
	 * @return
	 */
	private ArrayList<File> getFullFileList(ArrayList<File> fileList, File[] filesAndFolders, X3FileFilter fileFilter) {
		if (fileList == null) {
			fileList = new ArrayList<File>();
		}
		for (int i = 0; i < filesAndFolders.length; i++) {
			if (filesAndFolders[i].isDirectory()) {
				addDirToFileList(fileList, filesAndFolders[i], fileFilter);
			}
			else {
				fileList.add(filesAndFolders[i]);
			}
		}
		
		return fileList;
	}
	
	private int addDirToFileList(ArrayList<File> fileList, File folder, X3FileFilter fileFilter) {
		int n = 0;
		File[] files = folder.listFiles();
		if (files == null) return 0;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				addDirToFileList(fileList, files[i], fileFilter);
			}
//			else if (files[i].getName().endsWith(fileEnd)) {
			else if (fileFilter.accept(files[i])) {
				fileList.add(files[i]);
				n++;
			}
		}
		return n;
	}
	

	private ExplorePanel getOtherPanel(ExplorePanel explorePanel) {
		if (explorePanel == x3Panel) {
			return wavPanel;
		}
		else if (explorePanel == wavPanel) {
			return x3Panel;
		}
		return null;
	}

	public void cancelConversion(ExplorePanel explorePanel) {
		if (conWorker != null) {
			conWorker.stopNow();
		}
	}

	/**
	 * Called back from conversion worker. 
	 * @param next
	 */
	public void publishProgress(ConversionProgress next) {
		progressPanel.publishProgress(next);
		// and update the destination 
		if (conWorker != null) {
			int sType = conWorker.getSourceType();
			if (sType == WAV_FILES) {
				x3Panel.updateChooser();
			}
			else {
				wavPanel.updateChooser();
			}
		}
	}

	public void conversionComplete() {
		enableGUI(true);
	}

}
