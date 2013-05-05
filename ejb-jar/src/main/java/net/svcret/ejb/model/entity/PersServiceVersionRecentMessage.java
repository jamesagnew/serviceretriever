package net.svcret.ejb.model.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

//@formatter:off
@Table(name = "PX_SVC_VER_RCNT_MSG")
@Entity()
@NamedQueries({
	@NamedQuery(name = Queries.SVCVER_RECENTMSGS, query = Queries.SVCVER_RECENTMSGS_Q),
	@NamedQuery(name = Queries.SVCVER_RECENTMSGS_COUNT, query = Queries.SVCVER_RECENTMSGS_COUNT_Q)
})
@org.hibernate.annotations.Table(appliesTo="PX_SVC_VER_RCNT_MSG", indexes= {
	@Index(name="PX_SVC_VER_RCNT_MSG_IDX_WT", columnNames= {"SVC_VERSION_PID", "RESPONSE_TYPE", "XACT_TIME"}),
	@Index(name="PX_SVC_VER_RCNT_MSG_IDX_NT", columnNames= {"SVC_VERSION_PID", "RESPONSE_TYPE"})
})
//@formatter:on
public class PersServiceVersionRecentMessage extends BasePersRecentMessage {

	private static final long serialVersionUID = 1L;

	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID", nullable = false)
	private BasePersServiceVersion myServiceVersion;

	@ManyToOne()
	@JoinColumn(name = "USER_PID", referencedColumnName = "PID", nullable = true)
	private PersUser myUser;

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	/**
	 * @return the user
	 */
	public PersUser getUser() {
		return myUser;
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
	 *            the user to set
	 */
	public void setUser(PersUser theUser) {
		myUser = theUser;
	}

}
