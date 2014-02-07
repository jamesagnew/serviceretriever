package net.svcret.ejb.ejb.importexport;

import net.svcret.admin.api.UnexpectedFailureException;

public interface IImportExportServiceLocal {

	String exportDomain(long theDomainPid) throws UnexpectedFailureException;

	String exportDomain(String theDomainId)  throws UnexpectedFailureException;

}
