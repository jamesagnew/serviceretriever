package net.svcret.admin.shared.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseDtoKeepsRecentMessages extends BaseGObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="config_AuditLogEnabled")
	private Boolean myAuditLogEnable;
	
	@XmlElement(name="runtime_CanInheritKeepNumRecentTransactions")
	private boolean myCanInheritKeepNumRecentTransactions;

	@XmlElement(name="runtime_InheritedAuditLogEnabled")
	private Boolean myInheritedAuditLogEnable;

	@XmlElement(name="runtime_InheritedKeepNumRecentTransactionsFail")
	private Integer myInheritedKeepNumRecentTransactionsFail;
	
	@XmlElement(name="runtime_InheritedKeepNumRecentTransactionsFault")
	private Integer myInheritedKeepNumRecentTransactionsFault;

	@XmlElement(name="runtime_InheritedKeepNumRecentTransactionsSecurityFail")
	private Integer myInheritedKeepNumRecentTransactionsSecurityFail;

	@XmlElement(name="runtime_InheritedKeepNumRecentTransactionsSuccess")
	private Integer myInheritedKeepNumRecentTransactionsSuccess;

	@XmlElement(name="runtime_KeepNumRecentTransactionsFail")
	private Integer myKeepNumRecentTransactionsFail;

	@XmlElement(name="runtime_KeepNumRecentTransactionsFault")
	private Integer myKeepNumRecentTransactionsFault;

	@XmlElement(name="runtime_KeepNumRecentTransactionsSecurityFail")
	private Integer myKeepNumRecentTransactionsSecurityFail;

	@XmlElement(name="runtime_KeepNumRecentTransactionsSuccess")
	private Integer myKeepNumRecentTransactionsSuccess;

	public Boolean getAuditLogEnable() {
		return myAuditLogEnable;
	}

	public Boolean getInheritedAuditLogEnable() {
		return myInheritedAuditLogEnable;
	}

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
		
		BaseDtoKeepsRecentMessages obj = (BaseDtoKeepsRecentMessages) theObject;
		setKeepNumRecentTransactionsSuccess(obj.getKeepNumRecentTransactionsSuccess());
		setKeepNumRecentTransactionsFail(obj.getKeepNumRecentTransactionsFail());
		setKeepNumRecentTransactionsFault(obj.getKeepNumRecentTransactionsFault());
		setKeepNumRecentTransactionsSecurityFail(obj.getKeepNumRecentTransactionsSecurityFail());
	}

	public void setAuditLogEnable(Boolean theAuditLogEnable) {
		myAuditLogEnable = theAuditLogEnable;
	}

	public void setCanInheritKeepNumRecentTransactions(boolean theCanInheritKeepNumRecentTransactions) {
		myCanInheritKeepNumRecentTransactions = theCanInheritKeepNumRecentTransactions;
	}

	public void setInheritedAuditLogEnable(boolean theInheritedAuditLogEnable) {
		myInheritedAuditLogEnable=theInheritedAuditLogEnable;
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
