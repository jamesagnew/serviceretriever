package net.svcret.core.ejb.importexport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import net.svcret.admin.shared.enm.ServerSecurityModeEnum;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.ejb.importexport.ImportExportServiceBean;
import net.svcret.core.model.entity.PersDomain;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersService;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;

import org.hamcrest.core.StringContains;
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
		
		PersService service = new PersService();
		service.setDomain(domain);
		service.setPid(myNextLong++);
		service.setServiceId("thisServiceId");
		service.setServiceName("thisServiceName");
		
		PersServiceVersionSoap11 ver = new PersServiceVersionSoap11();
		ver.setService(service);
		ver.setPid(myNextLong++);
		ver.setVersionId("thisVersionId");
		ver.setServerSecurityMode(ServerSecurityModeEnum.REQUIRE_ANY);
		
		PersHttpClientConfig hc = new PersHttpClientConfig();
		hc.setDefaults();
		hc.setPid(myNextLong++);
		ver.setHttpClientConfig(hc);
		
		when(sr.getDomainByPid(1L)).thenReturn(domain);
		String output = svc.exportDomain(1L);
		ourLog.info("XML:\n{}", output);
		
		org.junit.Assert.assertThat(output, StringContains.containsString("<Version xsi:type=\"ns2:ServiceVersionSoap11\""));
	}

	private Set<String> createObscureElements() {
		HashSet<String> retVal = new HashSet<String>();
		retVal.add("thisObscure" + myNextInt++);
		retVal.add("thisObscure" + myNextInt++);
		return retVal;
	}
	
}
