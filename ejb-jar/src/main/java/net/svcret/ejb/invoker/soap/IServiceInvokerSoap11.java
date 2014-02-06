package net.svcret.ejb.invoker.soap;

import net.svcret.admin.api.ProcessingException;
import net.svcret.ejb.invoker.IServiceInvoker;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

public interface IServiceInvokerSoap11 extends IServiceInvoker {

	byte[] createWsdlBundle(PersServiceVersionSoap11 theSvcVer) throws ProcessingException;

}
