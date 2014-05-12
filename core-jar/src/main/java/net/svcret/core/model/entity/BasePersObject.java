package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.Transient;

import com.google.common.base.Objects;

public abstract class BasePersObject implements Serializable {

	public static final String NET_SVCRET_UNITTESTMODE = "net.svcret.unittestmode";

	private static final long serialVersionUID = 1L;

	private static Boolean myUnitTestMode;
	
	@Transient
	private boolean myNewlyCreated;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object theObj) {
		if (theObj == null) {
			return false;
		}
		
		if (getClass().equals(theObj.getClass()) == false) {
			return false;
		}

		BasePersObject obj = (BasePersObject) theObj;
		
		if (getPid() == null && obj.getPid() == null) {
			return this == theObj;
		}
		
		return Objects.equal(getPid(), obj.getPid());
	}

	public abstract Long getPid();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(getPid());
	}

	/**
	 * @return the newlyCreated
	 */
	public boolean isNewlyCreated() {
		return myNewlyCreated;
	}

	/**
	 * @param theNewlyCreated
	 *            the newlyCreated to set
	 */
	public void setNewlyCreated(boolean theNewlyCreated) {
		myNewlyCreated = theNewlyCreated;
	}

	public static boolean isUnitTestMode() {
		if (myUnitTestMode == null) {
			myUnitTestMode = "true".equals(System.getProperty(NET_SVCRET_UNITTESTMODE));
		}
		return myUnitTestMode;
	}

	/**
	 * This is needed for some unit tests due to the following bug:
	 * https://hibernate.atlassian.net/browse/HHH-7541
	 */
	public static String trimClobForUnitTest(String theRequestBody) {
		if (BasePersObject.isUnitTestMode() && theRequestBody!=null&&theRequestBody.length() > 250) {
			return theRequestBody.substring(0, 250);
		}
		return theRequestBody;
	}

}
