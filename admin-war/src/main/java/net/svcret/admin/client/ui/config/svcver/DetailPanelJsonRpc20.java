package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

public class DetailPanelJsonRpc20 extends BaseDetailPanel<DtoServiceVersionJsonRpc20> {

	/**
	 * Constructor
	 */
	public DetailPanelJsonRpc20(AbstractServiceVersionPanel theParent, DtoServiceVersionJsonRpc20 theServiceVersion) {
		super(theParent, theServiceVersion);
	}

	@Override
	protected void addProtocolSpecificPanelsToTop(boolean theIsAddPanel) {
		// none
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.JSONRPC20;
	}

}
