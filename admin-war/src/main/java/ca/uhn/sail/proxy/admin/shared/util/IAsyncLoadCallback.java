package ca.uhn.sail.proxy.admin.shared.util;


public interface IAsyncLoadCallback<T> {

		  /**
		   * Called when an asynchronous call completes successfully.
		   * 
		   * @param result the return value of the remote produced call
		   */
		  void onSuccess(T result);

}
