package org.pamguard.x3.main;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.pamguard.x3.gui.X3Gui;

public class X3Main {

	public X3Main() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		X3Gui x3Gui = new X3Gui();
	}

	public static void main(String[] args) {
		X3Main x3Main = new X3Main();
		x3Main.run();
	}

	private void run() {
		
	}

}
