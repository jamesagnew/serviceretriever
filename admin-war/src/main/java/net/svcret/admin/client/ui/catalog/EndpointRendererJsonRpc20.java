package net.svcret.admin.client.ui.catalog;

import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class EndpointRendererJsonRpc20 extends BaseEndpointRenderer<DtoServiceVersionJsonRpc20> {

	EndpointRendererJsonRpc20(GConfig theConfig) {
		super(theConfig);
	}

	@Override
	public Widget render(DtoServiceVersionJsonRpc20 theObject) {
		FlowPanel retVal = new FlowPanel();

		if (theObject.isUseDefaultProxyPath()) {
			String url = getUrlBase() + theObject.getDefaultProxyPath();
			Anchor endpoint = new Anchor("Endpoint");
			endpoint.setHref(url);
			retVal.add(endpoint);
		}

		if (StringUtil.isNotBlank(theObject.getExplicitProxyPath())) {
			if (retVal.getWidgetCount()>0) {
				retVal.add(new HtmlBr());
			}
			String url = getUrlBase() + theObject.getExplicitProxyPath();
			Anchor endpoint = new Anchor("Endpoint");
			endpoint.setHref(url);
			retVal.add(endpoint);
		}

		return retVal;
	}

}
