package net.svcret.ejb.ejb;

import java.io.IOException;
import java.io.Reader;

public class CapturingReader extends Reader {

	private Reader myWrap;
	private StringBuilder myBuilder;

	public CapturingReader(Reader theWrap) {
		myWrap = theWrap;
		myBuilder = new StringBuilder();
	}
	
	@Override
	public int read(char[] theCbuf, int theOff, int theLen) throws IOException {
		int retVal = myWrap.read(theCbuf, theOff, theLen);
		if (retVal > 0) {
			myBuilder.append(theCbuf, theOff, retVal);
		}
		return retVal;
	}

	public String getCapturedString() {
		return myBuilder.toString();
	}
	
	@Override
	public void close() throws IOException {
		myWrap.close();
	}

}
