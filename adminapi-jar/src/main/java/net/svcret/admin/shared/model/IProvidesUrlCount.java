package net.svcret.admin.shared.model;

public interface IProvidesUrlCount {

	/**
	 * @return the urlsActive
	 */
	public abstract Integer getUrlsActive();

	/**
	 * @return the urlsFailed
	 */
	public abstract Integer getUrlsDown();

	/**
	 * @return the urlsUnknown
	 */
	public abstract Integer getUrlsUnknown();

}