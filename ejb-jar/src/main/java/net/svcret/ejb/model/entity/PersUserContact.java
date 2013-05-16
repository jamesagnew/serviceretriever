package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
	private String Notes;

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return Notes;
	}

	@Override
	public Long getPid() {
		return myPid;
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
		Notes = theNotes;
	}

	/**
	 * @param thePid
	 *            the pid to set
	 */
	public void setPid(Long thePid) {
		myPid = thePid;
	}

}
