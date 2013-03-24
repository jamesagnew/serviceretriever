package net.svcret.admin.shared.model;

public interface IProvidesUrlCount {

	/**
	 * @return the urlsActive
	 */
	public abstract int getUrlsActive();

	/**
	 * @return the urlsFailed
	 */
	public abstract int getUrlsDown();

	/**
	 * @return the urlsUnknown
	 */
	public abstract int getUrlsUnknown();

}