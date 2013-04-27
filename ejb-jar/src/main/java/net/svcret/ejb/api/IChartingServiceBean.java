package net.svcret.ejb.api;

import java.io.IOException;

import javax.ejb.Local;

@Local
public interface IChartingServiceBean {

	byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid) throws IOException;

	byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid) throws IOException;
	
}
