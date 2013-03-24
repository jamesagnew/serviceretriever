package net.svcret.admin.shared.model;

public interface IListener {

	void loadingStarted(BaseGListenable<?> theListenable);
	
	void changed(BaseGListenable<?> theListenable);
	
}
