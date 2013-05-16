package net.svcret.ejb.model.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.svcret.ejb.api.IDao;

import org.hibernate.annotations.Index;

@Table(name = "PX_USER_RCNT_MSG")
@Entity()
@NamedQueries({ @NamedQuery(name = Queries.USER_RECENTMSGS, query = Queries.USER_RECENTMSGS_Q), @NamedQuery(name = Queries.USER_RECENTMSGS_COUNT, query = Queries.USER_RECENTMSGS_COUNT_Q) })
@org.hibernate.annotations.Table(appliesTo = "PX_USER_RCNT_MSG", indexes = { @Index(name = "PX_USER_RCNT_MSG_IDX_WT", columnNames = { "USER_PID", "RESPONSE_TYPE", "XACT_TIME" }), @Index(name = "PX_USER_RCNT_MSG_IDX_NT", columnNames = { "USER_PID", "RESPONSE_TYPE" }) })
public class PersUserRecentMessage extends BasePersRecentMessage {

	private static final long serialVersionUID = 1L;

	@ManyToOne()
	@JoinColumn(name = "USER_PID", referencedColumnName = "PID", nullable = false)
	private PersUser myUser;

	@ManyToOne()
	@JoinColumn(name = "SVC_VER_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	/**
	 * @return the serviceVersion
	 */
	public PersUser getUser() {
		return myUser;
	}

	/**
	 * @param theUser
	 *            the serviceVersion to set
	 */
	public void setUser(PersUser theUser) {
		myUser = theUser;
	}
	
	@Override
	public void addUsingDao(IDao theDaoBean) {
		theDaoBean.saveUserRecentMessage(this);
	}

	@Override
	public void trimUsingDao(IDao theDaoBean) {
		theDaoBean.trimUserRecentMessages(myUser, getResponseType(), myUser.getAuthenticationHost().determineKeepNumRecentTransactions(getResponseType()));
	}


}
