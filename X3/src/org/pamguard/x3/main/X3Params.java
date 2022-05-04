package org.pamguard.x3.main;

import java.awt.Point;
import java.io.File;
import java.io.Serializable;

/**
 * Serialisable parameters for X3 conversion program
 * @author Doug Gillespie
 *
 */
public class X3Params implements Serializable, Cloneable{

	private static final long serialVersionUID = 1L;

	public File x3Folder;
	
	public File wavFolder;
	
	public Point locationOnScreen;
	
	public X3Params() {
		// TODO Auto-generated constructor stub
	}

}
