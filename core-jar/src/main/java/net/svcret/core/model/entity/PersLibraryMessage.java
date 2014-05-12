package net.svcret.core.model.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;

//@formatter:off
@Entity
@Table(name = "PX_LIB_MSG")
@NamedQueries(value = { 
		@NamedQuery(name = Queries.LIBRARY_FINDALL, query = Queries.LIBRARY_FINDALL_Q),
		@NamedQuery(name = Queries.LIBRARY_FINDBYDOMAIN, query = Queries.LIBRARY_FINDBYDOMAIN_Q),
		@NamedQuery(name = Queries.LIBRARY_FINDBYSVC, query = Queries.LIBRARY_FINDBYSVC_Q), 
		@NamedQuery(name = Queries.LIBRARY_FINDBYSVCVER, query = Queries.LIBRARY_FINDBYSVCVER_Q) })
//@formatter:on
public class PersLibraryMessage extends BasePersObject {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "myPk.myMessage", cascade = {}, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<PersLibraryMessageAppliesTo> myAppliesTo;

	@Column(name = "CONTENT_TYPE", length = PersServiceVersionUrlStatus.MAX_LENGTH_CONTENT_TYPE, nullable = false)
	private String myContentType;

	@Column(name = "MSG_DESC", nullable = true)
	private String myDescription;

	@Lob()
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "MSG_BODY", nullable = false)
	private String myMessage;

	@Column(name = "MSG_BODY_CHARS", nullable = false)
	private int myMessageChars;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PID")
	private Long myPid;

	public Set<PersLibraryMessageAppliesTo> getAppliesTo() {
		if (myAppliesTo == null) {
			myAppliesTo = new HashSet<PersLibraryMessageAppliesTo>();
		}
		return myAppliesTo;
	}

	public void setAppliesTo(BasePersServiceVersion... theServiceVersions) {
		setAppliesTo(Arrays.asList(theServiceVersions));
	}

	public void setAppliesTo(Collection<BasePersServiceVersion> theServiceVersions) {
		// Remove
		for (PersLibraryMessageAppliesTo nextAppliesTo : new ArrayList<PersLibraryMessageAppliesTo>(getAppliesTo())) {
			if (!theServiceVersions.contains(nextAppliesTo.getPk().getServiceVersion())) {
				getAppliesTo().remove(nextAppliesTo);
			}
		}

		// Add
		for (BasePersServiceVersion nextWantedSvcVer : theServiceVersions) {
			boolean found = false;
			for (PersLibraryMessageAppliesTo nextAppliesTo : getAppliesTo()) {
				if (nextAppliesTo.getPk().getServiceVersion().equals(nextWantedSvcVer)) {
					found = true;
					break;
				}
			}

			if (!found) {
				myAppliesTo.add(new PersLibraryMessageAppliesTo(this, nextWantedSvcVer));
			}
		}
	}

	public String getContentType() {
		return myContentType;
	}

	public String getDescription() {
		return myDescription;
	}

	public String getMessageBody() {
		return myMessage;
	}

	@Override
	public Long getPid() {
		return myPid;
	}

	public void setContentType(String theContentType) {
		myContentType = theContentType;
	}

	public void setDescription(String theDescription) {
		myDescription = theDescription;
	}

	public void setMessage(String theMessage) {
		Validate.notNull(theMessage);
		myMessage = theMessage;
		myMessageChars = theMessage.length();
	}

	public void setPid(Long thePid) {
		myPid = thePid;
	}

	public int getMessageLength() {
		return myMessageChars;
	}

}
