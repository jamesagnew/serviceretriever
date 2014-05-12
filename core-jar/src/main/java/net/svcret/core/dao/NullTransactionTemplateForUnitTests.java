package net.svcret.core.dao;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public final class NullTransactionTemplateForUnitTests extends TransactionTemplate {
	private static final long serialVersionUID = 1L;

	@Override
	public <T> T execute(TransactionCallback<T> theAction) throws TransactionException {
		return theAction.doInTransaction(null);
	}
}