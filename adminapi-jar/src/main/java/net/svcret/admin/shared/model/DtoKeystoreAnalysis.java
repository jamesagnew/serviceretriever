package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DtoKeystoreAnalysis implements Serializable {

	private static final long serialVersionUID = 1L;

	private HashMap<String, Date> myExpiryDate;
	private HashMap<String, String> myIssuer;
	private ArrayList<String> myKeyAliases;
	private boolean myPasswordAccepted;
	private String myProblemDescription;
	private HashMap<String, String> mySubject;
	private HashMap<String, Boolean> myKeyEntry;
	private String myPassword;

	public Map<String, Date> getExpiryDate() {
		if (myExpiryDate == null) {
			myExpiryDate = new HashMap<String, Date>();
		}
		return myExpiryDate;
	}

	public Map<String, String> getIssuer() {
		if (myIssuer == null) {
			myIssuer = new HashMap<String, String>();
		}
		return myIssuer;
	}

	public HashMap<String, Boolean> getKeyEntry() {
		if (myKeyEntry == null) {
			myKeyEntry = new HashMap<String, Boolean>();
		}
		return myKeyEntry;
	}

	public List<String> getKeyAliases() {
		if (myKeyAliases == null) {
			myKeyAliases = new ArrayList<String>();
		}
		return myKeyAliases;
	}

	public String getProblemDescription() {
		return myProblemDescription;
	}

	public Map<String, String> getSubject() {
		if (mySubject == null) {
			mySubject = new HashMap<String, String>();
		}
		return mySubject;
	}

	public boolean isPasswordAccepted() {
		return myPasswordAccepted;
	}

	public void setPasswordAccepted(boolean thePasswordAccepted) {
		myPasswordAccepted = thePasswordAccepted;
	}

	public void setProblemDescription(String theProblemDescription) {
		myProblemDescription = theProblemDescription;
	}

	public String getPassword() {
		return myPassword;
	}

	public void setPassword(String theString) {
		myPassword = theString;
	}

}
