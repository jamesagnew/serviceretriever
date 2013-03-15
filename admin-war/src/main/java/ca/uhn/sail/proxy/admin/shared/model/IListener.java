package ca.uhn.sail.proxy.admin.shared.model;

public interface IListener {

	void loadingStarted(BaseGListenable<?> theListenable);
	
	void changed(BaseGListenable<?> theListenable);
	
}
