package net.svcret.core.ejb.importexport;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.util.ScanDtoUtil;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.model.entity.PersDomain;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ImportExportServiceBean implements IImportExportServiceLocal {

	private final JAXBContext myJaxbContext;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	public ImportExportServiceBean() throws JAXBException, IOException, ClassNotFoundException {
		List<Class<?>> classes = ScanDtoUtil.findMyTypes(DtoDomainList.class.getPackage().getName());
		myJaxbContext = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));

	}

	@Override
	public String exportDomain(long theDomainPid) throws UnexpectedFailureException {
		PersDomain domain = myServiceRegistry.getDomainByPid(theDomainPid);
		DtoDomain dto = domain.toDto();
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
