package net.svcret.admin.client.ui.catalog;

import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.shared.model.GConfig;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class EndpointRendererSoap11 extends BaseEndpointRenderer<DtoServiceVersionSoap11> {

	EndpointRendererSoap11(GConfig theConfig) {
		super(theConfig);
	}
	
	@Override
	public Widget render(DtoServiceVersionSoap11 theObject) {
		FlowPanel retVal=new FlowPanel();
		
		if (theObject.isUseDefaultProxyPath()) {
			String url = getUrlBase() + theObject.getDefaultProxyPath();
			Anchor endpoint = new Anchor("Endpoint");
			endpoint.setHref(url);
			retVal.add(endpoint);
			
			url = getUrlBase() + theObject.getDefaultProxyPath() + "?wsdl";
			endpoint = new Anchor("WSDL");
			endpoint.setHref(url);
			endpoint.getElement().getStyle().setPaddingLeft(4, Unit.PX);
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

			url = getUrlBase() + theObject.getExplicitProxyPath() + "?wsdl";
			endpoint = new Anchor("WSDL");
			endpoint.setHref(url);
			endpoint.getElement().getStyle().setPaddingLeft(4, Unit.PX);
			retVal.add(endpoint);
		}
		
		return retVal;
	}


}
