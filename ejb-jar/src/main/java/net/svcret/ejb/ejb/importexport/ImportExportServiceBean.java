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
import net.svcret.admin.shared.model.GService;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.PersDomain;

@Stateless
public class ImportExportServiceBean implements IImportExportServiceLocal {

	private final JAXBContext myJaxbContext;

	@EJB
	private IServiceRegistry myServiceRegistry;

	public ImportExportServiceBean() throws JAXBException {

		myJaxbContext = JAXBContext.newInstance(GDomainList.class, GDomain.class, GService.class);

	}

	@Override
	public String exportDomain(long theDomainPid) throws UnexpectedFailureException {
		PersDomain domain = myServiceRegistry.getDomainByPid(theDomainPid);
		GDomain dto = domain.toDto();
		String retVal;

		Marshaller marshaller;
		try {
			marshaller = myJaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			StringWriter w = new StringWriter();
			marshaller.marshal(dto, w);
			retVal = w.toString();
		} catch (JAXBException e) {
			throw new UnexpectedFailureException(e);
		}

		return retVal;
	}

	@Override
	public String exportDomain(String theDomainId) throws UnexpectedFailureException {
		for (PersDomain next : myServiceRegistry.getAllDomains()) {
			if (next.getDomainId().equals(theDomainId)) {
				return exportDomain(next.getPid());
			}
		}
		throw new IllegalArgumentException("Unknown domain ID: " + theDomainId);
	}

	@VisibleForTesting
	void setServiceRegistryForUnitTest(IServiceRegistry theSr) {
		myServiceRegistry = theSr;
	}

}
