package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.BaseGKeepsRecentMessages;

@MappedSuperclass
public abstract class BasePersKeepsRecentTransactions extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Column(name = "AUDIT_LOG_ENABLE", nullable = true)
	private Boolean myAuditLogEnable;

	@Column(name = "KEEP_RCNT_NUM_FAIL", nullable = true)
	private Integer myKeepNumRecentTransactionsFail;

	@Column(name = "KEEP_RCNT_NUM_FAULT", nullable = true)
	private Integer myKeepNumRecentTransactionsFault;

	@Column(name = "KEEP_RCNT_NUM_SECFAIL", nullable = true)
	private Integer myKeepNumRecentTransactionsSecurityFail;

	@Column(name = "KEEP_RCNT_NUM_SUC", nullable = true)
	private Integer myKeepNumRecentTransactionsSuccess;
	
	public abstract boolean canInheritKeepNumRecentTransactions();

	public abstract boolean determineInheritedAuditLogEnable();

	public abstract Integer determineInheritedKeepNumRecentTransactions(ResponseTypeEnum theResultType);

	public Integer determineKeepNumRecentTransactions(ResponseTypeEnum theResultType) {
		Integer retVal;
		switch (theResultType) {
		case FAIL:
			retVal = myKeepNumRecentTransactionsFail;
			break;
		case FAULT:
			retVal = myKeepNumRecentTransactionsFault;
			break;
		case SECURITY_FAIL:
			retVal = myKeepNumRecentTransactionsSecurityFail;
			break;
		case SUCCESS:
			retVal = myKeepNumRecentTransactionsSuccess;
			break;
		case THROTTLE_REJ:
			retVal = 0;
			break;
		default:
			throw new IllegalStateException("Unknown type: " + theResultType);
		}

		return retVal;

	}

	public Boolean getAuditLogEnable() {
		return myAuditLogEnable;
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

	public void merge(BasePersObject theObject) {
		BasePersKeepsRecentTransactions obj = (BasePersKeepsRecentTransactions) theObject;
		setKeepNumRecentTransactionsFail(obj.getKeepNumRecentTransactionsFail());
		setKeepNumRecentTransactionsSecurityFail(obj.getKeepNumRecentTransactionsSecurityFail());
		setKeepNumRecentTransactionsFault(obj.getKeepNumRecentTransactionsFault());
		setKeepNumRecentTransactionsSuccess(obj.getKeepNumRecentTransactionsSuccess());
	}

	public void populateKeepRecentTransactionsFromDto(BaseGKeepsRecentMessages<?> theDto) {
		setKeepNumRecentTransactionsFail(theDto.getKeepNumRecentTransactionsFail());
		setKeepNumRecentTransactionsSecurityFail(theDto.getKeepNumRecentTransactionsSecurityFail());
		setKeepNumRecentTransactionsFault(theDto.getKeepNumRecentTransactionsFault());
		setKeepNumRecentTransactionsSuccess(theDto.getKeepNumRecentTransactionsSuccess());
	}

	public void populateKeepRecentTransactionsToDto(BaseGKeepsRecentMessages<?> theDto) {
		theDto.setKeepNumRecentTransactionsFail(this.getKeepNumRecentTransactionsFail());
		theDto.setKeepNumRecentTransactionsSecurityFail(this.getKeepNumRecentTransactionsSecurityFail());
		theDto.setKeepNumRecentTransactionsFault(this.getKeepNumRecentTransactionsFault());
		theDto.setKeepNumRecentTransactionsSuccess(this.getKeepNumRecentTransactionsSuccess());

		theDto.setCanInheritKeepNumRecentTransactions(canInheritKeepNumRecentTransactions());
		if (canInheritKeepNumRecentTransactions()) {
			theDto.setInheritedKeepNumRecentTransactionsFail(determineInheritedKeepNumRecentTransactions(ResponseTypeEnum.FAIL));
			theDto.setInheritedKeepNumRecentTransactionsSecurityFail(determineInheritedKeepNumRecentTransactions(ResponseTypeEnum.SECURITY_FAIL));
			theDto.setInheritedKeepNumRecentTransactionsFault(determineInheritedKeepNumRecentTransactions(ResponseTypeEnum.FAULT));
			theDto.setInheritedKeepNumRecentTransactionsSuccess(determineInheritedKeepNumRecentTransactions(ResponseTypeEnum.SUCCESS));
		}
	}

	public void setAuditLogEnable(Boolean theAuditLogEnable) {
		myAuditLogEnable = theAuditLogEnable;
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
