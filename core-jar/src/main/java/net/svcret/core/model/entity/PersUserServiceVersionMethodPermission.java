package net.svcret.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Table(name = "PX_USER_PERM_SVCVER_METHOD", uniqueConstraints = { // -
@UniqueConstraint(columnNames = { "SVCVER_METHOD_PID", "USER_PERM_SVCVER_PID" }) })
@Entity
public class PersUserServiceVersionMethodPermission extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "SVCVER_METHOD_PID", referencedColumnName = "PID", nullable = false,foreignKey=@ForeignKey(name="FK_UPSM_METHOD"))
	
	private PersMethod myServiceVersionMethod;

	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_PERM_SVCVER_PID", referencedColumnName = "PID", nullable = false,foreignKey=@ForeignKey(name="FK_UPSM_PERM"))
	private PersUserServiceVersionPermission myServiceVersionPermission;

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceDomain
	 */
	public PersMethod getServiceVersionMethod() {
		return myServiceVersionMethod;
	}

	/**
	 * @return the serviceUser
	 */
	public PersUserServiceVersionPermission getServiceVersionPermission() {
		return myServiceVersionPermission;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServiceVersionMethod
	 *            the serviceDomain to set
	 */
	public void setServiceVersionMethod(PersMethod theServiceVersionMethod) {
		myServiceVersionMethod = theServiceVersionMethod;
	}

	/**
	 * @param theServiceVersionPermission
	 *            the domainPermission to set
	 */
	public void setServiceVersionPermission(PersUserServiceVersionPermission theServiceVersionPermission) {
		myServiceVersionPermission = theServiceVersionPermission;
	}

	public void loadAllAssociations() {
		// nothing
	}

}
