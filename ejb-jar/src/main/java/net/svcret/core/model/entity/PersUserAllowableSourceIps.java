package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Table(name = "PX_USER_ALLOW_SRC_IP")
@Entity
public class PersUserAllowableSourceIps implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "IP", length = 200, nullable = false)
	private String myIp;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID", nullable = false)
	private Long myPid;

	@JoinColumn(name = "USER_PID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
	private PersUser myUser;

	@Column(name="IP_ORDER", nullable=false)
	private Integer myOrder;
	
	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return myOrder;
	}

	/**
	 * @param theOrder the order to set
	 */
	public void setOrder(Integer theOrder) {
		myOrder = theOrder;
	}

	public PersUserAllowableSourceIps() {
	}

	public PersUserAllowableSourceIps(PersUser theUser, String theIp) {
		setUser(theUser);
		setIp(theIp);
	}

	public String getIp() {
		return myIp;
	}

	public Long getPid() {
		return myPid;
	}

	public PersUser getUser() {
		return myUser;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(myIp).append(myUser).toHashCode();
	}

	@Override
	public boolean equals(Object theObj) {
		if (!(theObj instanceof PersUserAllowableSourceIps)) {
			return false;
		}

		PersUserAllowableSourceIps obj = (PersUserAllowableSourceIps) theObj;

		return myIp.equals(obj.getIp()) && myUser.equals(obj.getUser());
	}

	public void setIp(String theIp) {
		myIp = theIp;
	}

	public void setUser(PersUser theUser) {
		myUser = theUser;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("User", getUser().getUsername()).append("Ip", getIp()).toString();
	}

}
