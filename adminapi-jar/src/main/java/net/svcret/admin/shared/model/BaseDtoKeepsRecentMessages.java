package net.svcret.admin.shared.model;

public abstract class BaseGKeepsRecentMessages extends BaseGObject {

	private static final long serialVersionUID = 1L;

	private boolean myCanInheritKeepNumRecentTransactions;
	private Integer myInheritedKeepNumRecentTransactionsFail;
	private Integer myInheritedKeepNumRecentTransactionsFault;
	private Integer myInheritedKeepNumRecentTransactionsSecurityFail;
	private Integer myInheritedKeepNumRecentTransactionsSuccess;
	private Integer myKeepNumRecentTransactionsFail;
	private Integer myKeepNumRecentTransactionsFault;
	private Integer myKeepNumRecentTransactionsSecurityFail;
	private Integer myKeepNumRecentTransactionsSuccess;

	public Integer getInheritedKeepNumRecentTransactionsFail() {
		return myInheritedKeepNumRecentTransactionsFail;
	}

	public Integer getInheritedKeepNumRecentTransactionsFault() {
		return myInheritedKeepNumRecentTransactionsFault;
	}

	public Integer getInheritedKeepNumRecentTransactionsSecurityFail() {
		return myInheritedKeepNumRecentTransactionsSecurityFail;
	}

	public Integer getInheritedKeepNumRecentTransactionsSuccess() {
		return myInheritedKeepNumRecentTransactionsSuccess;
	}

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

	public boolean isCanInheritKeepNumRecentTransactions() {
		return myCanInheritKeepNumRecentTransactions;
	}

	public void merge(BaseGObject theObject) {
		super.merge(theObject);
		
		BaseGKeepsRecentMessages obj = (BaseGKeepsRecentMessages) theObject;
		setKeepNumRecentTransactionsSuccess(obj.getKeepNumRecentTransactionsSuccess());
		setKeepNumRecentTransactionsFail(obj.getKeepNumRecentTransactionsFail());
		setKeepNumRecentTransactionsFault(obj.getKeepNumRecentTransactionsFault());
		setKeepNumRecentTransactionsSecurityFail(obj.getKeepNumRecentTransactionsSecurityFail());
	}

	public void setCanInheritKeepNumRecentTransactions(boolean theCanInheritKeepNumRecentTransactions) {
		myCanInheritKeepNumRecentTransactions = theCanInheritKeepNumRecentTransactions;
	}

	public void setInheritedKeepNumRecentTransactionsFail(Integer theInheritedKeepNumRecentTransactionsFail) {
		myInheritedKeepNumRecentTransactionsFail = theInheritedKeepNumRecentTransactionsFail;
	}

	public void setInheritedKeepNumRecentTransactionsFault(Integer theInheritedKeepNumRecentTransactionsFault) {
		myInheritedKeepNumRecentTransactionsFault = theInheritedKeepNumRecentTransactionsFault;
	}

	public void setInheritedKeepNumRecentTransactionsSecurityFail(Integer theInheritedKeepNumRecentTransactionsSecurityFail) {
		myInheritedKeepNumRecentTransactionsSecurityFail = theInheritedKeepNumRecentTransactionsSecurityFail;
	}

	public void setInheritedKeepNumRecentTransactionsSuccess(Integer theInheritedKeepNumRecentTransactionsSuccess) {
		myInheritedKeepNumRecentTransactionsSuccess = theInheritedKeepNumRecentTransactionsSuccess;
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
