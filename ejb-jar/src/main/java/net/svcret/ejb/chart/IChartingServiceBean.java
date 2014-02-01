package net.svcret.ejb.chart;

import java.io.IOException;

import javax.ejb.Local;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.ex.UnexpectedFailureException;

@Local
public interface IChartingServiceBean {

	byte[] renderSvcVerUsageGraph(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderSvcVerPayloadSizeGraph(long thePid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderSvcVerThrottlingGraph(long theServiceVersionPid, TimeRange theRange) throws IOException, UnexpectedFailureException;

	byte[] renderUserMethodGraphForUser(long theUserPid, TimeRange theRange) throws UnexpectedFailureException, IOException;

	byte[] renderSvcVerLatencyMethodGraph(long theSvcVerPid, TimeRange theRange, boolean theIndividualMethod) throws UnexpectedFailureException, IOException;
}
