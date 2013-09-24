package net.svcret.ejb.ejb.importexport;

import java.io.StringWriter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.common.annotations.VisibleForTesting;

import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.PersDomain;

@Stateless
public class ImportExportServiceBean implements IImportExportServiceLocal {

	@EJB
	private IServiceRegistry myServiceRegistry;
	
	private final JAXBContext myJaxbContext;
	
	public ImportExportServiceBean() throws JAXBException {
		
		myJaxbContext = JAXBContext.newInstance(GDomainList.class, GDomain.class);
		
	}
	
	@Override
	public String exportDomain(long theDomainPid) throws UnexpectedFailureException {
		PersDomain domain = myServiceRegistry.getDomainByPid(theDomainPid);
		GDomain dto = domain.toDto();
		String retVal;
		
		Marshaller marshaller;
		try {
			marshaller = myJaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			StringWriter w = new StringWriter();
			marshaller.marshal(dto, w);
			retVal = w.toString();
		} catch (JAXBException e) {
			throw new UnexpectedFailureException(e);
		}
		
		return retVal;
	}

	@VisibleForTesting
 void setServiceRegistryForUnitTest(IServiceRegistry theSr) {
		myServiceRegistry=theSr;
	}
	
}
