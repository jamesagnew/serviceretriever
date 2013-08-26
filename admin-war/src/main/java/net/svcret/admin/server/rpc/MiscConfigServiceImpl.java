package net.svcret.admin.server.rpc;

import java.util.TimeZone;

import net.svcret.admin.client.rpc.MiscConfigService;

public class MiscConfigServiceImpl extends BaseRpcServlet implements MiscConfigService {

	private static final long serialVersionUID = 1L;

	@Override
	public long loadLocalTimzoneOffsetInMillis() {
		return TimeZone.getDefault().getRawOffset();
//		return TimeZone.getDefault().getOffset(System.currentTimeMillis());
	}

	
	public static void main(String[] args) {
		
//		System.out.println(TimeZone.getDefault().getRawOffset() / (60* 60 ));
//		System.out.println(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (60 * 60 * 1000L));
		
	}
}
