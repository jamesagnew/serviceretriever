package net.svcret.admin.shared;


public interface IAsyncLoadCallback<T> {

		  /**
		   * Called when an asynchronous call completes successfully.
		   * 
		   * @param result the return value of the remote produced call
		   */
		  void onSuccess(T result);

}
