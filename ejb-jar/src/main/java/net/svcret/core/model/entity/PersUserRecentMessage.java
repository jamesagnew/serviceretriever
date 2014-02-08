package net.svcret.core.model.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.core.api.IDao;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

//@formatter:off
@Table(name = "PX_USER_RCNT_MSG")
@Entity()
@NamedQueries({ 
	@NamedQuery(name = Queries.USER_RECENTMSGS, query = Queries.USER_RECENTMSGS_Q), 
	@NamedQuery(name = Queries.USER_RECENTMSGS_COUNT, query = Queries.USER_RECENTMSGS_COUNT_Q) 
})
@org.hibernate.annotations.Table(appliesTo = "PX_USER_RCNT_MSG", indexes = { 
		@Index(name = "PX_USER_RCNT_MSG_IDX_WT", columnNames = { "USER_PID", "RESPONSE_TYPE", "XACT_TIME" }),
		@Index(name = "PX_USER_RCNT_MSG_IDX_NT", columnNames = { "USER_PID", "RESPONSE_TYPE" }) 
	})
//@formatter:on
public class PersUserRecentMessage extends BasePersSavedTransactionRecentMessage {

	private static final long serialVersionUID = 1L;

	@ManyToOne()
	@JoinColumn(name = "METHOD_PID", referencedColumnName = "PID", nullable = true)
	@ForeignKey(name="FK_USER_RCNTMSG_METHOD")
	private PersMethod myMethod;

	@ManyToOne()
	@JoinColumn(name = "SVC_VER_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@ManyToOne()
	@JoinColumn(name = "USER_PID", referencedColumnName = "PID", nullable = false)
	private PersUser myUser;

	@Override
	public void addUsingDao(IDao theDaoBean) {
		theDaoBean.saveUserRecentMessage(this);
	}

	public PersMethod getMethod() {
		return myMethod;
	}

	@Override
	public RecentMessageTypeEnum getRecentMessageType() {
		return RecentMessageTypeEnum.USER;
	}

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @return the serviceVersion
	 */
	public PersUser getUser() {
		return myUser;
	}

	public void setMethod(PersMethod theMethod) {
		myMethod = theMethod;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	/**
	 * @param theUser
	 *            the serviceVersion to set
	 */
	public void setUser(PersUser theUser) {
		myUser = theUser;
	}

	@Override
	public long trimUsingDao(IDao theDaoBean) {
		theDaoBean.trimUserRecentMessages(myUser, getResponseType(), myUser.getAuthenticationHost().determineKeepNumRecentTransactions(getResponseType()));
		return getRequestBodyBytes() + getResponseBodyBytes();
	}

}
