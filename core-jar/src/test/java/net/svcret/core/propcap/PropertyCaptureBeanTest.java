package net.svcret.core.propcap;

import java.io.StringReader;

import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.invoker.soap.InvocationFailedException;
import net.svcret.core.invoker.soap.RequestPipelineTest;
import net.svcret.core.model.entity.BasePersServiceVersion;
import net.svcret.core.model.entity.PersPropertyCapture;
import net.svcret.core.model.entity.PersPropertyCapturePk;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;
import net.svcret.core.propcap.PropertyCaptureBean;

import org.junit.Assert;
import org.junit.Test;

public class PropertyCaptureBeanTest {
//	private static final Logger ourLog = LoggerFactory.getLogger(PropertyCaptureBeanTest.class);

	@Test
	public void testCaptureXml() throws InvocationFailedException {
		
		PropertyCaptureBean svc = new PropertyCaptureBean();
		BasePersServiceVersion svcVer = new PersServiceVersionSoap11();

		PersPropertyCapture cap = new PersPropertyCapture();
		cap.setPk(new PersPropertyCapturePk(svcVer, "prop1"));
		cap.setXpathExpression("//*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='someMethod']/*[local-name()='theMrn']");
		svcVer.getPropertyCaptures().add(cap);
		
		PersPropertyCapture cap2 = new PersPropertyCapture();
		cap2.setPk(new PersPropertyCapturePk(svcVer, "prop2"));
		cap2.setXpathExpression("//*[local-name()='Envelope2']/*[local-name()='Body2']/*[local-name()='someMethod']/*[local-name()='theMrn2']");
		svcVer.getPropertyCaptures().add(cap2);
		
		SrBeanIncomingRequest request=new SrBeanIncomingRequest();
		String inputString = RequestPipelineTest.createRequest("someMethod", true);
		request.setInputReader(new StringReader(inputString));
		request.drainInputMessage();
		SrBeanProcessedRequest invocationResult = new SrBeanProcessedRequest();
		
		svc.captureRequestProperties(svcVer, request, invocationResult);
		
		Assert.assertEquals("MRN0", invocationResult.getPropertyCaptures().get("prop1"));
		Assert.assertEquals("", invocationResult.getPropertyCaptures().get("prop2"));
		
	}
	
}
