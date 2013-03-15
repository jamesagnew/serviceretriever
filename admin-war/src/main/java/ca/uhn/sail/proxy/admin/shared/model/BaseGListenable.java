package ca.uhn.sail.proxy.admin.shared.model;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BaseGListenable<T> implements Serializable, IsSerializable {

	private static final long serialVersionUID = 1L;

	private List<IListener> myListeners = new ArrayList<IListener>();
	private boolean myInitialized;

	/**
	 * @return the updating
	 */
	public boolean isInitialized() {
		return myInitialized;
	}

	protected void markInitialized() {
		if (!myInitialized) {
			myInitialized = true;
			fireInitialized();
		}
	}

	private void fireInitialized() {
		GWT.log(new Date() + " - " + getClass().getName() + " is initialized");
		for (IListener next : new ArrayList<IListener>(myListeners)) {
			GWT.log(new Date() + " - Notifying listener of type " + next.getClass().getName() + " of 'initialized' by class " + getClass().getName());
			next.loadingStarted(this);
		}
	}

	protected void fireChanged() {
		GWT.log(new Date() + " - " + getClass().getName() + " has changed");
		for (IListener next : new ArrayList<IListener>(myListeners)) {
			GWT.log(new Date() + " - Notifying listener of type " + next.getClass().getName() + " of 'initialized' by class " + getClass().getName());
			next.changed(this);
		}
	}

	public void addListener(IListener theListener) {
		myListeners.add(theListener);
	}

	public void removeListener(IListener theListener) {
		myListeners.remove(theListener);
	}

	public boolean hasListeners() {
		return !myListeners.isEmpty();
	}

}
