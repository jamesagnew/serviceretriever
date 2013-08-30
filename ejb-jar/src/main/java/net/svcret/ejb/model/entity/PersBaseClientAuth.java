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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.ejb.model.entity.soap.PersWsSecUsernameTokenClientAuth;

import com.google.common.base.Objects;

@Table(name = "PX_CLIENT_AUTH")
@Entity()
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "AUTH_TYPE", length = 20, discriminatorType = DiscriminatorType.STRING)
public abstract class PersBaseClientAuth<T extends PersBaseClientAuth<?>> extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@Column(name="CAUTH_ORDER", nullable=false)
	private int myOrder;
	
	@Column(name = "PASSWORD", length = 100)
	private String myPassword;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@ManyToOne()
	@JoinColumn(name = "SVC_VERSION_PID", referencedColumnName = "PID")
	private BasePersServiceVersion myServiceVersion;

	@Column(name = "USERNAME", length = 100)
	private String myUsername;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object theObj) {
		return getClass().equals(theObj.getClass()) && 
				(Objects.equal(myPid, ((PersBaseClientAuth<?>) theObj).myPid) || (relevantPropertiesEqual((T)theObj)));
	}

	public abstract ClientSecurityEnum getAuthType();

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
	 * @return the password
	 */
	public String getPassword() {
		return myPassword;
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

	/**
	 * @return the username
	 */
	public String getUsername() {
		return myUsername;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(myPid);
	}

	public void loadAllAssociations() {
		// nothing
	}

	public void merge(BasePersObject theObj) {
		PersWsSecUsernameTokenClientAuth obj = (PersWsSecUsernameTokenClientAuth) theObj;
		
		setUsername(obj.getUsername());
		setPassword(obj.getPassword());
		setServiceVersion(obj.getServiceVersion());
	}

	/**
	 * Subclasses must provide an implementation which compares all
	 * relevant properties to the subclass type
	 */
	protected abstract boolean relevantPropertiesEqual(T theT);

	/**
	 * @param theOptLock
	 *            the optLock to set
	 */
	public void setOptLock(int theOptLock) {
		myOptLock = theOptLock;
	}

	/**
	 * @param theOrder the order to set
	 */
	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	/**
	 * @param thePassword
	 *            the password to set
	 */
	public void setPassword(String thePassword) {
		myPassword = thePassword;
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

	/**
	 * @param theUsername
	 *            the username to set
	 */
	public void setUsername(String theUsername) {
		myUsername = theUsername;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		b.append("pid", getPid());
		return b.toString();
	}

}
