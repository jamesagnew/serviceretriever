package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.List;

public class GRecentMessageLists implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		if (myKeepFail > 0) {
			b.append("Success ").append(mySuccessList.size()).append('/').append(myKeepSuccess);
		}
		if (b.length() > 0) {
			b.append(", ");
		}
		if (myKeepFail > 0) {
			b.append("Fault ").append(myFaultList.size()).append('/').append(myKeepFault);
		}
		if (b.length() > 0) {
			b.append(", ");
		}
		if (myKeepFail > 0) {
			b.append("Fail ").append(myFailList.size()).append('/').append(myKeepFail);
		}
		if (b.length() > 0) {
			b.append(", ");
		}
		if (myKeepFail > 0) {
			b.append("SecurityFail ").append(mySecurityFailList.size()).append('/').append(myKeepSecurityFail);
		}
		if (b.length()==0) {
			b.append("None");
		}
		return b.toString();
	}

	private List<GRecentMessage> myFailList;
	private List<GRecentMessage> myFaultList;
	private int myKeepFail;
	private int myKeepFault;
	private int myKeepSecurityFail;
	private int myKeepSuccess;
	private List<GRecentMessage> mySecurityFailList;
	private List<GRecentMessage> mySuccessList;

	/**
	 * @return the failList
	 */
	public List<GRecentMessage> getFailList() {
		return myFailList;
	}

	/**
	 * @return the faultList
	 */
	public List<GRecentMessage> getFaultList() {
		return myFaultList;
	}

	/**
	 * @return the keepFail
	 */
	public int getKeepFail() {
		return myKeepFail;
	}

	/**
	 * @return the keepFault
	 */
	public int getKeepFault() {
		return myKeepFault;
	}

	/**
	 * @return the keepSecurityFail
	 */
	public int getKeepSecurityFail() {
		return myKeepSecurityFail;
	}

	/**
	 * @return the keepSuccess
	 */
	public int getKeepSuccess() {
		return myKeepSuccess;
	}

	/**
	 * @return the securityFailList
	 */
	public List<GRecentMessage> getSecurityFailList() {
		return mySecurityFailList;
	}

	/**
	 * @return the successList
	 */
	public List<GRecentMessage> getSuccessList() {
		return mySuccessList;
	}

	public boolean hasAtLeastOneList() {
		return myFailList != null || myFaultList != null || mySecurityFailList != null || mySuccessList != null;
	}

	/**
	 * @param theFailList
	 *            the failList to set
	 */
	public void setFailList(List<GRecentMessage> theFailList) {
		myFailList = theFailList;
	}

	/**
	 * @param theFaultList
	 *            the faultList to set
	 */
	public void setFaultList(List<GRecentMessage> theFaultList) {
		myFaultList = theFaultList;
	}

	/**
	 * @param theKeepFail
	 *            the keepFail to set
	 */
	public void setKeepFail(int theKeepFail) {
		myKeepFail = theKeepFail;
	}

	/**
	 * @param theKeepFault
	 *            the keepFault to set
	 */
	public void setKeepFault(int theKeepFault) {
		myKeepFault = theKeepFault;
	}

	/**
	 * @param theKeepSecurityFail
	 *            the keepSecurityFail to set
	 */
	public void setKeepSecurityFail(int theKeepSecurityFail) {
		myKeepSecurityFail = theKeepSecurityFail;
	}

	/**
	 * @param theKeepSuccess
	 *            the keepSuccess to set
	 */
	public void setKeepSuccess(int theKeepSuccess) {
		myKeepSuccess = theKeepSuccess;
	}

	/**
	 * @param theSecurityFailList
	 *            the securityFailList to set
	 */
	public void setSecurityFailList(List<GRecentMessage> theSecurityFailList) {
		mySecurityFailList = theSecurityFailList;
	}

	/**
	 * @param theSuccessList
	 *            the successList to set
	 */
	public void setSuccessList(List<GRecentMessage> theSuccessList) {
		mySuccessList = theSuccessList;
	}

}
