package net.svcret.ejb.api;

import java.io.IOException;

import javax.ejb.Local;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.ex.ProcessingException;

@Local
public interface IChartingServiceBean {

	byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, ProcessingException;

	byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, ProcessingException;

	byte[] renderPayloadSizeGraphForServiceVersion(long thePid, TimeRange theRange) throws IOException, ProcessingException;

	
}
