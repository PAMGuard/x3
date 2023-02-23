package org.pamguard.x3.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.pamguard.x3.main.ConversionProgress;

public class ProgressPanel {

	private JPanel progressPanel;
	
	private JTextField sourceFile, destFile;
	private JProgressBar progressBar;
	
	public ProgressPanel() {
		progressPanel = new JPanel();
		progressPanel.setBorder(new TitledBorder("Conversion Progress"));
		progressPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		progressPanel.add(new JLabel("Source file: ", JLabel.RIGHT), c);
		c.gridx++;
		progressPanel.add(sourceFile = new JTextField(80), c);
		c.gridx = 0;
		c.gridy++;
		progressPanel.add(new JLabel("Dest file: ", JLabel.RIGHT), c);
		c.gridx++;
		progressPanel.add(destFile = new JTextField(80), c);
		c.gridx = 0;
		c.gridy++;
		progressPanel.add(new JLabel("Progress: ", JLabel.RIGHT), c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		progressPanel.add(progressBar = new JProgressBar(), c);
		
	}

	/**
	 * @return the progressPanel
	 */
	public JPanel getProgressPanel() {
		return progressPanel;
	}

	public void publishProgress(ConversionProgress next) {
		progressBar.setMaximum(next.nFiles);
		progressBar.setValue(next.iFile);
		if (next.sourceFile != null) {
			sourceFile.setText(next.sourceFile.getAbsolutePath());
		}
		else {
			sourceFile.setText("");
		}
		if (next.destFile != null) {
			destFile.setText(next.destFile.getAbsolutePath());
		}
		else {
			destFile.setText("");
		}
	}

}
