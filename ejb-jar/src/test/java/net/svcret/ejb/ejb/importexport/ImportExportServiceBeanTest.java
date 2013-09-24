package net.svcret.ejb.ejb.importexport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.PersDomain;

import org.junit.Test;

public class ImportExportServiceBeanTest {
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ImportExportServiceBeanTest.class);
	private int myNextInt=1;
	private long myNextLong=1;
	
	@Test
	public void testExport() throws Exception {
		
		ImportExportServiceBean svc = new ImportExportServiceBean();
		
		IServiceRegistry sr = mock(IServiceRegistry.class);
		svc.setServiceRegistryForUnitTest(sr);
		
		PersDomain domain = new PersDomain();
		domain.setPid(myNextLong++);
		domain.setDomainId("thisDomainId");
		domain.setDomainName("thisDomainName");
		domain.setKeepNumRecentTransactionsSuccess(myNextInt++);
		domain.setKeepNumRecentTransactionsFault(myNextInt++);
		domain.setKeepNumRecentTransactionsFail(myNextInt++);
		domain.setKeepNumRecentTransactionsSecurityFail(myNextInt++);
		domain.setObscureRequestElementsInLog(createObscureElements());
		
		when(sr.getDomainByPid(1L)).thenReturn(domain);
		String output = svc.exportDomain(1L);
		ourLog.info("XML:\n{}", output);
	}

	private Set<String> createObscureElements() {
		HashSet<String> retVal = new HashSet<String>();
		retVal.add("thisObscure" + myNextInt++);
		retVal.add("thisObscure" + myNextInt++);
		return retVal;
	}
	
}
