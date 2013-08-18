package net.svcret.ejb.api;

import java.io.IOException;

import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.soap.PersServiceVersionSoap11;

public interface IServiceInvokerSoap11 extends IServiceInvoker<PersServiceVersionSoap11> {

	byte[] createWsdlBundle(PersServiceVersionSoap11 theSvcVer) throws ProcessingException, IOException;

}
