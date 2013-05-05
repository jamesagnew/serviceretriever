package net.svcret.admin.shared.model;


public abstract class BaseGKeepsRecentMessages<T> extends BaseGObject<T> {

	private static final long serialVersionUID = 1L;

	private Integer myKeepNumRecentTransactionsFail;
	private Integer myKeepNumRecentTransactionsFault;
	private Integer myKeepNumRecentTransactionsSecurityFail;
	private Integer myKeepNumRecentTransactionsSuccess;

	/**
	 * @return the keepNumRecentTransactionsFail
	 */
	public Integer getKeepNumRecentTransactionsFail() {
		return myKeepNumRecentTransactionsFail;
	}

	/**
	 * @return the keepNumRecentTransactionsFault
	 */
	public Integer getKeepNumRecentTransactionsFault() {
		return myKeepNumRecentTransactionsFault;
	}

	/**
	 * @return the keepNumRecentTransactionsSecurityFail
	 */
	public Integer getKeepNumRecentTransactionsSecurityFail() {
		return myKeepNumRecentTransactionsSecurityFail;
	}

	/**
	 * @return the keepNumRecentTransactionsSuccess
	 */
	public Integer getKeepNumRecentTransactionsSuccess() {
		return myKeepNumRecentTransactionsSuccess;
	}

	/**
	 * @param theKeepNumRecentTransactionsFail
	 *            the keepNumRecentTransactionsFail to set
	 */
	public void setKeepNumRecentTransactionsFail(Integer theKeepNumRecentTransactionsFail) {
		myKeepNumRecentTransactionsFail = theKeepNumRecentTransactionsFail;
	}
	/**
	 * @param theKeepNumRecentTransactionsFault
	 *            the keepNumRecentTransactionsFault to set
	 */
	public void setKeepNumRecentTransactionsFault(Integer theKeepNumRecentTransactionsFault) {
		myKeepNumRecentTransactionsFault = theKeepNumRecentTransactionsFault;
	}
	/**
	 * @param theKeepNumRecentTransactionsSecurityFail
	 *            the keepNumRecentTransactionsSecurityFail to set
	 */
	public void setKeepNumRecentTransactionsSecurityFail(Integer theKeepNumRecentTransactionsSecurityFail) {
		myKeepNumRecentTransactionsSecurityFail = theKeepNumRecentTransactionsSecurityFail;
	}
	/**
	 * @param theKeepNumRecentTransactionsSuccess
	 *            the keepNumRecentTransactionsSuccess to set
	 */
	public void setKeepNumRecentTransactionsSuccess(Integer theKeepNumRecentTransactionsSuccess) {
		myKeepNumRecentTransactionsSuccess = theKeepNumRecentTransactionsSuccess;
	}

}
