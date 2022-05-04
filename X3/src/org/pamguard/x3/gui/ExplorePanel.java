package org.pamguard.x3.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Panel to browse a load of files in a folder. 
 * @author Doug Gillespie
 *
 */
public class ExplorePanel {

	private JPanel mainPanel;
	private JFileChooser fileChooser;
	private X3FileFilter fileFilter;
	private X3Gui x3Gui;
	private int fileType;
	private String[] fileEnds;
	
	public ExplorePanel(X3Gui x3Gui, int fileType, String panelName, String[] fileEnds) {
		this.x3Gui = x3Gui;
		this.fileType = fileType;
		this.fileEnds = fileEnds;
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder(panelName));
		fileChooser = new XFileChooser();
		fileChooser.setPreferredSize(new Dimension(400,500));
	    fileFilter = new X3FileFilter(fileEnds);
	    fileChooser.setFileFilter(fileFilter);
	    fileChooser.setAcceptAllFileFilterUsed(false);
	    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    fileChooser.setMultiSelectionEnabled(true);
	    fileChooser.setApproveButtonText("Convert");
//	    fileChooser.addPropertyChangeListener(new PropertyChangeListener() {
//	        public void propertyChange(PropertyChangeEvent evt) {
//	          if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
//	            JFileChooser chooser = (JFileChooser) evt.getSource();
//	            File oldDir = (File) evt.getOldValue();
//	            File newDir = (File) evt.getNewValue();
//
//	            File curDir = chooser.getCurrentDirectory();
//	          }
//	        }
//	      });
	    
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, fileChooser);
	}

	/**
	 * @return the mainPanel
	 */
	public JComponent getMainPanel() {
		return mainPanel;
	}

	class XFileChooser extends JFileChooser {

		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see javax.swing.JFileChooser#approveSelection()
		 */
		@Override
		public void approveSelection() {
//			System.out.println("Selection approved");
			File[] selFiles = fileChooser.getSelectedFiles();
			x3Gui.convertFiles(ExplorePanel.this, selFiles);
		}

		/* (non-Javadoc)
		 * @see javax.swing.JFileChooser#cancelSelection()
		 */
		@Override
		public void cancelSelection() {
//			System.out.println("Selection cancelled");
			x3Gui.cancelConversion(ExplorePanel.this);
		}
		
	}
	
	public File getCurrentFolder() {
		return fileChooser.getCurrentDirectory();
	}

	public void setCurrentFolder(File folder) {
		if (folder == null || folder.exists() == false) {
			return;
		}
		fileChooser.setCurrentDirectory(folder);
	}

	/**
	 * @return the fileType
	 */
	public int getFileType() {
		return fileType;
	}

	/**
	 * @return the fileEnd
	 */
	public String getFileEnd() {
		return fileEnds[0];
	}

	public void setEnabled(boolean enabled) {
		fileChooser.setEnabled(enabled);
		mainPanel.setEnabled(enabled);
	}

	/**
	 * Called when files are added to force 
	 * the chooser to update itself. 
	 */
	public void updateChooser() {
		fileChooser.rescanCurrentDirectory();
	}

	/**
	 * @return the fileFilter
	 */
	public X3FileFilter getFileFilter() {
		return fileFilter;
	}

}
