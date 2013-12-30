package net.svcret.admin.shared.model;

public interface IHasThrottle<T extends IThrottleable> {

	T getThrottle();
	
	void setThrottle(T theThrottle);
	
	T instantiateNew();
	
}
