package net.svcret.admin.client.ui.catalog;

import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class Soap11EndpointRenderer extends BaseEndpointRenderer<GSoap11ServiceVersion> {

	Soap11EndpointRenderer(GConfig theConfig) {
		super(theConfig);
	}
	
	@Override
	public Widget render(GSoap11ServiceVersion theObject) {
		FlowPanel retVal=new FlowPanel();
		
		if (theObject.isUseDefaultProxyPath()) {
			String url = getUrlBase() + theObject.getDefaultProxyPath();
			Anchor endpoint = new Anchor("Endpoint");
			endpoint.setHref(url);
			retVal.add(endpoint);
			
			url = getUrlBase() + theObject.getDefaultProxyPath() + "?wsdl";
			endpoint = new Anchor("WSDL");
			endpoint.setHref(url);
			retVal.add(endpoint);
		}

		if (StringUtil.isNotBlank(theObject.getExplicitProxyPath())) {
			String url = getUrlBase() + theObject.getExplicitProxyPath();
			Anchor endpoint = new Anchor("Endpoint");
			endpoint.setHref(url);
			retVal.add(endpoint);

			url = getUrlBase() + theObject.getExplicitProxyPath() + "?wsdl";
			endpoint = new Anchor("WSDL");
			endpoint.setHref(url);
			retVal.add(endpoint);
		}
		
//		if (retVal.get)
//		endpoint.getElement().getStyle().setPaddingLeft(4, Unit.PX);
		
		return retVal;
	}


}
