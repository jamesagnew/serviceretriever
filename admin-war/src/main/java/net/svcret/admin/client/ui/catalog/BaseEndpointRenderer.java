package net.svcret.admin.client.ui.catalog;

import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GConfig;

import com.google.gwt.user.client.ui.Widget;

public abstract class BaseEndpointRenderer<T extends BaseGServiceVersion> {

	private GConfig myConfig;

	public BaseEndpointRenderer(GConfig theConfig) {
		myConfig =theConfig;
	}

	/**
	 * @return the urlBase
	 */
	public String getUrlBase() {
		return myConfig.getProxyUrlBases().iterator().next();
	}

	public abstract Widget render(T theObject);
	
}
