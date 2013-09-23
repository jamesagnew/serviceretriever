package net.svcret.ejb.ejb.importexport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import net.svcret.ejb.api.IServiceRegistry;
import net.svcret.ejb.model.entity.PersDomain;

import org.junit.Test;

public class ImportExportServiceBeanTest {

	@Test
	public void testExport() {
		
		IServiceRegistry sr = mock(IServiceRegistry.class);
		PersDomain domain;
		when(sr.getDomainByPid(1L)).thenReturn(domain);
		
	}
	
}
