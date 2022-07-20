package org.pamguard.x3.sud;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Logs meta data from the decompresses files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class LogFileStream extends PrintWriter
{
	public LogFileStream(String path) throws FileNotFoundException {
		super(path);
		this.println("<ST>");
		this.flush();
	}
	
	public void writeXML(int id, String context, String name, String value)
	{
		this.println(String.format("<PROC_EVENT ID=\"%d\">", id));
		this.println(String.format("<%S %S=\"%S\"/>", context, name, value));
		this.println("</PROC_EVENT>");
		this.flush();
;
	}
	
	public void close()
	{
		this.println("</ST>");
		this.flush();
	}
}