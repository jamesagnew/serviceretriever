package net.svcret.ejb.model.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class PersContact extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	@Column(name = "CONTACT_NOTES", length = 2000)
	private String Notes;

	/**
	 * @return the notes
	 */
	public String getNotes() {
		return Notes;
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

	@Override
	public Long getPid() {
		return myPid;
	}

}
