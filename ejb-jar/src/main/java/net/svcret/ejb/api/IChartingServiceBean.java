package net.svcret.ejb.api;

import java.io.IOException;

import javax.ejb.Local;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.ex.UnexpectedFailureException;

@Local
public interface IChartingServiceBean {

	byte[] renderLatencyGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderUsageGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderPayloadSizeGraphForServiceVersion(long thePid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderThrottlingGraphForServiceVersion(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderUserMethodGraphForUser(long theUserPid, TimeRange theRange) throws UnexpectedFailureException, IOException;
}
