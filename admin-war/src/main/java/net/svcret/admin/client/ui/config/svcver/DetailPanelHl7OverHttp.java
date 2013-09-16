package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.shared.model.DtoServiceVersionHl7OverHttp;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

public class DetailPanelHl7OverHttp extends BaseDetailPanel<DtoServiceVersionHl7OverHttp> {

	/**
	 * Constructor
	 */
	public DetailPanelHl7OverHttp(AbstractServiceVersionPanel theParent, DtoServiceVersionHl7OverHttp theServiceVersion) {
		super(theParent, theServiceVersion);
	}


	@Override
	protected void addProtocolSpecificPanelsToTop(boolean theIsAddPanel) {
		// none
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.HL7OVERHTTP;
	}

}
