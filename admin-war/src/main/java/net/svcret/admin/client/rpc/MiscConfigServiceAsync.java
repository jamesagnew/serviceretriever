package net.svcret.admin.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MiscConfigServiceAsync {

	void loadLocalTimzoneOffsetInMillis(AsyncCallback<Long> theAsyncCallback);

}
