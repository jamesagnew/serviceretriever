package net.svcret.ejb.api;

import javax.ejb.Local;

@Local
public interface IScheduler {

	public abstract void reloadUserRegistry();

}
