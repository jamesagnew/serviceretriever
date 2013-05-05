package net.svcret.ejb.api;

import java.util.Date;

import javax.ejb.Local;

import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersUser;

@Local
public interface ITransactionLogger {

	void logTransaction(Date theTransactionDate, BasePersServiceVersion theServiceVersion, PersUser theUser, String theRequestBody, InvocationResponseResultsBean theInvocationResponse);

	void flush();


}
