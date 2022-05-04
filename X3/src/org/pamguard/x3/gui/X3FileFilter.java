package org.pamguard.x3.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class X3FileFilter extends FileFilter {

	private String[] types;
	
	public X3FileFilter(String[] types) {
		super();
		this.types = new String[types.length];
		for (int i = 0;i < types.length; i++) {
			this.types[i] = types[i].toLowerCase();
		}
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String fl = f.getName().toLowerCase();
		for (int i = 0; i < types.length; i++) {
			if (fl.endsWith(types[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		String str = types[0];
		for (int i = 1; i < types.length; i++) {
			str += ", " + types[i];
		}
		return str + " files";
	}


}
