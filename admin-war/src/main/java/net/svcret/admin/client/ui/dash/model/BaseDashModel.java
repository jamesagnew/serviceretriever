package net.svcret.admin.client.ui.dash.model;

import java.util.Date;

import net.svcret.admin.client.ui.components.Sparkline;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.StatusEnum;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseDashModel implements IDashModel {
	
	private BaseGDashboardObject<?> myModel;
	private NumberFormat myDecimalFormat = NumberFormat.getFormat("0.0");
	
	public BaseDashModel(BaseGDashboardObject<?> theModel) {
		myModel = theModel;
	}
	
	@Override
	public final Widget renderLatency() {
		return returnSparklineFor60mins(myModel.getLatency60mins(),Integer.toString(myModel.getAverageLatency()), "ms");
	}
	
	@Override
	public final Widget renderUsage() {
		int[] list = myModel.getTransactions60mins();
		return returnSparklineFor60mins(list,formatDouble(myModel.getAverageTransactionsPerMin()),"/min");
	}

	private String formatDouble(double theNumber) {
		return myDecimalFormat.format(theNumber);
	}

	public static Widget returnImageForStatus(StatusEnum status) {
		if (status == null) {
			GWT.log("Status is null");
			return null;
		}

		switch (status) {
		case ACTIVE:
			return new Image("images/icon_check_16.png");
		case DOWN:
			return new Image("images/icon_warn_16.png");
		case UNKNOWN:
			return new Image("images/icon_unknown_16.png");
		}
		return null;
	}

	public static Widget returnSparklineFor60mins(int[] theList, String theCurrentValue, String theUnitDesc) {
		if (theList == null) {
			GWT.log(new Date() + " - No 60 minutes data");
			return null;
		}
		String text = theCurrentValue + theUnitDesc;
		Sparkline retVal = new Sparkline(theList, text);
		retVal.setWidth("100px");
		return retVal;
	}


}
