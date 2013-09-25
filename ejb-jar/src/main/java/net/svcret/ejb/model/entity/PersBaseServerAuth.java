package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import net.svcret.admin.shared.model.BaseDtoServerSecurity;
import net.svcret.admin.shared.model.ServerSecurityEnum;
import net.svcret.ejb.api.ICredentialGrabber;

@Table(name = "PX_SERVER_AUTH")
@Entity()
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "AUTH_TYPE", length = 20, discriminatorType = DiscriminatorType.STRING)
public abstract class PersBaseServerAuth<T extends PersBaseServerAuth<?, ?>, G extends ICredentialGrabber> extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = {})
	@JoinColumn(name = "SERV_AUTH_PID", referencedColumnName = "PID")
	private BasePersAuthenticationHost myAuthenticationHost;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Column(name = "SAUTH_ORDER", nullable = false)
	private int myOrder;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID")
	private BasePersServiceVersion myServiceVersion;

	/**
	 * @return the authenticationHost
	 */
	public BasePersAuthenticationHost getAuthenticationHost() {
		return myAuthenticationHost;
	}

	public abstract ServerSecurityEnum getAuthType();

	public abstract Class<? extends G> getGrabberClass();

	/**
	 * @return the optLock
	 */
	public int getOptLock() {
		return myOptLock;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return myOrder;
	}

	/**
	 * @return the pid
	 */
	public Long getPid() {
		return myPid;
	}

	/**
	 * @return the serviceVersion
	 */
	public BasePersServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public void loadAllAssociations() {
		// nothing
	}

	// /**
	// * Subclasses must provide an implementation which compares all
	// * relevant properties to the subclass type
	// */
	// protected abstract boolean relevantPropertiesEqual(T theT);

	public abstract void merge(BasePersObject theObj);

	/**
	 * @param theAuthenticationHost
	 *            the authenticationHost to set
	 */
	public void setAuthenticationHost(BasePersAuthenticationHost theAuthenticationHost) {
		myAuthenticationHost = theAuthenticationHost;
	}

	/**
	 * @param theOptLock
	 *            the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theOrder
	 *            the order to set
	 */
	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

	/**
	 * @param theServiceVersion
	 *            the serviceVersion to set
	 */
	public void setServiceVersion(BasePersServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
	}

	public BaseDtoServerSecurity toDto() {
		BaseDtoServerSecurity retVal = createDtoAndPopulateWithTypeSpecificEntries();
		retVal.setPid(this.getPid());
		retVal.setAuthHostPid(getAuthenticationHost().getPid());
		retVal.setAuthHostId(getAuthenticationHost().getModuleId());
		return retVal;
	}

	protected abstract BaseDtoServerSecurity createDtoAndPopulateWithTypeSpecificEntries();

}
