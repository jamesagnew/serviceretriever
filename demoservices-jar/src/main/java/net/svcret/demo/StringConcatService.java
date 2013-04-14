package net.svcret.demo;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@Stateless
@WebService(name = "StringConcatService", portName = "DemoServicePort", serviceName = "DemoServiceSvc", targetNamespace = "net:svcret:demo")
@SOAPBinding(style = Style.RPC)
public class StringConcatService {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(StringConcatService.class);

	@WebMethod(action="addStrings")
	public String addStrings(String theFirstString, String theSecondString) {
		ourLog.info("Demo service invocation: " + theFirstString + " + " + theSecondString);
		return theFirstString + theSecondString;
	}

	@WebMethod(action="addStringsSlow")
	public String addStringsSlow(String theFirstString, String theSecondString) {
		ourLog.info("Demo service invocation (Slow): " + theFirstString + " + " + theSecondString);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// ignore
		}
		return theFirstString + theSecondString;
	}

}
