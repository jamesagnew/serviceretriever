package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.shared.model.GServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

public class DetailPanelJsonRpc20 extends BaseDetailPanel<GServiceVersionJsonRpc20> {

	/**
	 * Constructor
	 */
	public DetailPanelJsonRpc20(AbstractServiceVersionPanel theParent, GServiceVersionJsonRpc20 theServiceVersion) {
		super(theParent, theServiceVersion);
	}

	@Override
	protected void addProtocolSpecificPanelsToTop() {
		// none
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

}
