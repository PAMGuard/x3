package org.pamguard.x3.sud;

import java.io.Serializable;

public class SUDClickDetectorInfo implements Serializable {

	public static final long serialVersionUID = 1L;

	/*
	 * These first two probably come from a dwv section:
	 * <CFG ID="12" FTYPE="wav" CODEC="11">
<SRC ID="11" />
<FS> 384000 </FS>
<NBITS> 16 </NBITS>
<NCHS> 1 </NCHS>
<SUFFIX> dwv </SUFFIX>
<TIMECHK> 0 </TIMECHK>
<BLKLEN> 498 </BLKLEN>
</CFG>
<CFG ID="13">
<SRC ID="9" />
<PROC> snip </PROC>
<LEN> 498 </LEN>
</CFG>

but might also come from the main sampling section. Try the dwv bit first. 
	 */
	public int sampleRate;
	
	public int nChan;
	
	/*
	 *  these all come out of a XML section like this:
<CFG ID="8">
<PROC> FILT </PROC>
<NFILT> 15 </NFILT>
</CFG>
<CFG ID="9">
<SRC ID="6" />
<PROC> CDET </PROC>
<DETTHR TYPE="relative"> 15 </DETTHR>
<BLANKING UNIT="samples"> 384 </BLANKING>
<PREDET UNIT="samples"> 249 </PREDET>
<POSTDET UNIT="samples"> 249 </POSTDET>
<LEN UNIT="samples"> 38 </LEN>
<USING ID="8" />
</CFG>
	 *  
	 */
	public double detThr;
	
	public int blankingSamples;
	
	public int preSamples;
	
	public int postSamples;
	
	public int lenSamples;
	
}
