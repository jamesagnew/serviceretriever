package net.svcret.ejb.ejb.importexport;

import javax.ejb.Local;

import net.svcret.admin.api.UnexpectedFailureException;

@Local
public interface IImportExportServiceLocal {

	String exportDomain(long theDomainPid) throws UnexpectedFailureException;

	String exportDomain(String theDomainId)  throws UnexpectedFailureException;

}
