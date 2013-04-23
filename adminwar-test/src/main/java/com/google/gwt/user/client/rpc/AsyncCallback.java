package com.google.gwt.user.client.rpc;

public interface AsyncCallback<T> {

	public void onFailure(Throwable theCaught);

	public void onSuccess(T theResult);

}
