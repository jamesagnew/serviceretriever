package net.svcret.ejb.api;

import java.io.IOException;

import javax.ejb.Local;

import net.svcret.ejb.ex.ProcessingException;

@Local
public interface IChartingServiceBean {

	byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid) throws IOException, ProcessingException;

	byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid) throws IOException, ProcessingException;

	byte[] renderPayloadSizeGraphForServiceVersion(long thePid) throws IOException, ProcessingException;
	
}
