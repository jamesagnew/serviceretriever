package net.svcret.admin.client.ui.dash.model;

import static net.svcret.admin.client.AdminPortal.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.Sparkline;
import net.svcret.admin.client.ui.stats.DateUtil;
import net.svcret.admin.shared.model.BaseGDashboardObject;
import net.svcret.admin.shared.model.BaseGDashboardObjectWithUrls;
import net.svcret.admin.shared.model.StatusEnum;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseDashModel implements IDashModel {

	private static NumberFormat ourDecimalFormat = NumberFormat.getFormat("0.0");

	private BaseGDashboardObject<?> myModel;

	public BaseDashModel(BaseGDashboardObject<?> theModel) {
		myModel = theModel;
	}

	@Override
	public Widget renderLastInvocation() {
		if (!(myModel instanceof BaseGDashboardObjectWithUrls<?>)) {
			return null;
		}

		Date lastInvoc = myModel.getLastSuccessfulInvocation();
		String text = DateUtil.formatTimeElapsedForLastInvocation(lastInvoc);
		return new Label(text);

	}

	@Override
	public final Widget renderLatency() {
		return returnSparklineFor60mins(myModel.getLatency60mins(), myModel.getStatsInitialized(), Integer.toString(myModel.getAverageLatency()), "ms");
	}

	protected Widget renderName(String thePrefix, String theName, String thePostFix) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleName(CssConstants.UNSTYLED_TABLE);
		if (thePrefix != null) {
			hp.add(new HTML(thePrefix));
		}
		hp.add(new HTML(theName));
		if (thePostFix != null) {
			hp.add(new HTML(thePostFix));
		}
		return hp;
	}

	@Override
	public Widget renderSecurity() {
		if (!(myModel instanceof BaseGDashboardObjectWithUrls<?>)) {
			return null;
		}

		ImageResource image = null;
		String text = null;

		BaseGDashboardObjectWithUrls<?> obj = (BaseGDashboardObjectWithUrls<?>) myModel;
		switch (obj.getServerSecured()) {
		case FULLY:
			image = AdminPortal.IMAGES.dashSecure();
			text = AdminPortal.MSGS.dashboard_SecuredFully();
			break;
		case PARTIALLY:
			image = AdminPortal.IMAGES.dashSecure();
			text = AdminPortal.MSGS.dashboard_SecuredPartial();
			break;
		case NONE:
			text = AdminPortal.MSGS.dashboard_NotSecured();
			break;
		}

		FlowPanel retVal = new FlowPanel();
		retVal.setStyleName(CssConstants.DASHBOARD_SECURITY_PANEL);

		if (image != null) {
			Image img = new Image(image);
			img.getElement().getStyle().setDisplay(Display.INLINE);
			retVal.add(img);
		}

		Label lbl = new Label(text);
		lbl.getElement().getStyle().setDisplay(Display.INLINE);
		retVal.add(lbl);

		return retVal;
	}

	@Override
	public final Widget renderUsage() {
		int[] list = myModel.getTransactions60mins();
		double averagePerMin = myModel.getAverageTransactionsPerMin();
		return returnSparklineFor60MinsUsage(list, myModel.getStatsInitialized(), averagePerMin);
	}

	private static String formatDouble(double theNumber) {
		return ourDecimalFormat.format(theNumber);
	}

	public static Widget returnBarSparklineFor60mins(int[] theList, Date theStatsInitialized, String theCurrentValue, String theUnitDesc) {
		if (theList == null) {
			GWT.log(new Date() + " - No 60 minutes data");
			return null;
		}
		String text = theCurrentValue + theUnitDesc;

		List<Long> dates = new ArrayList<Long>();
		long nextDate = theStatsInitialized.getTime() - (60 * 60 * 1000L);
		for (int i = 0; i < 60; i++) {
			dates.add(nextDate);
			nextDate += (60 * 1000L);
		}

		Sparkline retVal = new Sparkline(theList, dates, text);
		retVal.setBar(true);
		retVal.setWidth("100px");
		return retVal;
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

	public static Widget returnSparklineFor60mins(int[] theList, Date theStatsInitialized, String theCurrentValue, String theUnitDesc) {
		if (theList == null) {
			GWT.log(new Date() + " - No 60 minutes data");
			return null;
		}
		String text = theCurrentValue + theUnitDesc;

		List<Long> dates = new ArrayList<Long>();
		long nextDate = theStatsInitialized.getTime() - (60 * 60 * 1000L);
		for (int i = 0; i < 60; i++) {
			dates.add(nextDate);
			nextDate += (60 * 1000L);
		}

		Sparkline retVal = new Sparkline(theList, dates, text);
		retVal.setWidth("100px");
		return retVal;
	}

	public static Widget returnSparklineFor60MinsUsage(int[] list, Date theStatsInitialized, double averagePerMin) {
		if (averagePerMin < 0.1) {
			return returnBarSparklineFor60mins(list, theStatsInitialized, formatDouble(averagePerMin * 60), "/hr");
		} else {
			return returnBarSparklineFor60mins(list, theStatsInitialized, formatDouble(averagePerMin), "/min");
		}
	}

}
