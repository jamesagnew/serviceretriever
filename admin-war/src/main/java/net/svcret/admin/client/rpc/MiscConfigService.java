package net.svcret.admin.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("miscconfig")
public interface MiscConfigService extends RemoteService {

	long loadLocalTimzoneOffsetInMillis();

}
