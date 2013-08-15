package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "PX_USER_CONTACT")
public class PersUserContact extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@OneToMany(cascade = {}, mappedBy = "myContact")
	private Collection<PersUser> myUsers;

	@Column(name = "CONTACT_NOTES", length = 2000)
	private String myNotes;

	@Column(name = "EMAIL_ADDRS", length = 2000)
	private String myEmailAddresses;

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return myNotes;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public void setEmailAddresses(Set<String> theAddresses) {
		StringBuilder b = new StringBuilder();
		for (String next : theAddresses) {
			if (StringUtils.isNotBlank(next)) {
				if (b.length() > 0) {
					b.append('\n');
				}
				b.append(next);
			}
		}
		myEmailAddresses = b.toString();
	}

	public TreeSet<String> getEmailAddresses() {
		if (myEmailAddresses==null) {
			return new TreeSet<String>();
		}
		
		TreeSet<String> retVal = new TreeSet<String>();
		for (String next : myEmailAddresses.split("\\n")) {
			if (StringUtils.isNotBlank(next)) {
				retVal.add(next);
			}
		}
		
		return retVal;
	}

	/**
	 * @return the users
	 */
	public Collection<PersUser> getUsers() {
		if (myUsers == null) {
			myUsers = new ArrayList<PersUser>();
		}
		return myUsers;
	}

	/**
	 * @param theNotes
	 *            the notes to set
	 */
	public void setNotes(String theNotes) {
		myNotes = theNotes;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

}
