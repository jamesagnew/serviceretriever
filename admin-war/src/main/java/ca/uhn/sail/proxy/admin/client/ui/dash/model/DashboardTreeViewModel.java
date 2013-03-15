package ca.uhn.sail.proxy.admin.client.ui.dash.model;

import ca.uhn.sail.proxy.admin.shared.model.GDomainList;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.TreeViewModel;

public class DashboardTreeViewModel implements TreeViewModel {

	public <T> NodeInfo<?> getNodeInfo(T theValue) {
		
		if (theValue instanceof GDomainList) {
		}
		
		return null;
	}

	public boolean isLeaf(Object theValue) {
		// TODO Auto-generated method stub
		return false;
	}

}
