package net.svcret.core.invoker.soap;

import net.svcret.admin.api.ProcessingException;
import net.svcret.core.invoker.IServiceInvoker;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;

public interface IServiceInvokerSoap11 extends IServiceInvoker {

	byte[] createWsdlBundle(PersServiceVersionSoap11 theSvcVer) throws ProcessingException;

}
