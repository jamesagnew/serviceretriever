package net.svcret.ejb.model.entity;

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
	private Long myPid;

	@JoinColumn(name = "USER_PID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY, cascade = {})
	private PersUser myUser;

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
		return new HashCodeBuilder().append(myUser).append(myIp).toHashCode();
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
