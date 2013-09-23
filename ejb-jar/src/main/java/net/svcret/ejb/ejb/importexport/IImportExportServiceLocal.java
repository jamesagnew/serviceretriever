package net.svcret.ejb.ejb.importexport;

import javax.ejb.Local;

import net.svcret.ejb.ex.UnexpectedFailureException;

@Local
public interface IImportExportServiceLocal {

	String exportDomain(long theDomainPid) throws UnexpectedFailureException;

}
