package net.svcret.core.propcap;

import net.svcret.core.api.SrBeanIncomingRequest;
import net.svcret.core.api.SrBeanProcessedRequest;
import net.svcret.core.invoker.soap.InvocationFailedException;
import net.svcret.core.model.entity.BasePersServiceVersion;

public interface IPropertyCaptureService {

	void captureRequestProperties(BasePersServiceVersion theServiceVersion, SrBeanIncomingRequest theRequest, SrBeanProcessedRequest theInvocationResult) throws InvocationFailedException;

}
