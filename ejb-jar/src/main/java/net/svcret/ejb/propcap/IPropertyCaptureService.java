package net.svcret.ejb.propcap;

import javax.ejb.Local;

import net.svcret.ejb.api.SrBeanIncomingRequest;
import net.svcret.ejb.api.SrBeanProcessedRequest;
import net.svcret.ejb.invoker.soap.InvocationFailedException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;

@Local
public interface IPropertyCaptureService {

	void captureRequestProperties(BasePersServiceVersion theServiceVersion, SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theInvocationResult) throws InvocationFailedException;

}
