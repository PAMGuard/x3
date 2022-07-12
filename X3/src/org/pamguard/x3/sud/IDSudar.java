package org.pamguard.x3.sud;


public class IDSudar {
	
	public IDSudar() {};
	
	public IDSudar(XMLFileHandler xmlHandler) {
		this.dataHandler = xmlHandler; 
	}

	/**
	 * The chunk may have a source ID - id a data handler to run before the iD data handler 
	 * is run. 
	 */
	public int srcID = 0; 
	
	/**
	 * The ID to use
	 */
	public int iD = 0; 
	
	/**
	 * The data handler
	 */
	public ISudarDataHandler dataHandler = null; 
	
}