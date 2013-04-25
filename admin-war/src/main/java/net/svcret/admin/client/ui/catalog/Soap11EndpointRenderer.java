package net.svcret.admin.client.ui.catalog;

import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;

import com.google.gwt.dom.client.Style.Unit;
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
		
		String url = getUrlBase() + theObject.getProxyPath();
		Anchor endpoint = new Anchor("Endpoint");
		endpoint.setHref(url);
		retVal.add(endpoint);
		
		url = getUrlBase() + theObject.getProxyPath() + "?wsdl";
		endpoint = new Anchor("WSDL");
		endpoint.setHref(url);
		retVal.add(endpoint);

		endpoint.getElement().getStyle().setPaddingLeft(4, Unit.PX);
		
		return retVal;
	}


}
