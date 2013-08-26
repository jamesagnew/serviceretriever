package net.svcret.admin.client.ui.stats;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.TimeRange;
import net.svcret.admin.shared.model.TimeRangeEnum;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.tractionsoftware.gwt.user.client.ui.UTCDateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCTimeBox;

public class TimeRangeSelectorPanel extends HorizontalPanel {

	private List<ChangeHandler> myChangeHandlers = new ArrayList<ChangeHandler>();
	private boolean myUpdating;
	private UTCDateBox myFromDatePicker;
	private UTCTimeBox myFromTimePicker;
	private UTCDateBox myToDatePicker;
	private UTCTimeBox myToTimePicker;
	private ListBox myTimeListBox;
	private long myLocalTimeZoneOffsetInMillis;
	private PButton myApplyButton;

	public TimeRangeSelectorPanel(boolean theAddApplyButton) {

		myTimeListBox = new ListBox();
		myTimeListBox.addChangeHandler(new MyPresetHandler());
		add(myTimeListBox);

		add(new Label("From:"));

		myFromDatePicker = new UTCDateBox();
		myFromDatePicker.addValueChangeHandler(new MyRangeHandler());
		add(myFromDatePicker);

		myFromTimePicker = new UTCTimeBox();
		myFromTimePicker.addValueChangeHandler(new MyRangeHandler());
		add(myFromTimePicker);

		add(new Label("To:"));

		myToDatePicker = new UTCDateBox();
		myToDatePicker.addValueChangeHandler(new MyRangeHandler());
		add(myToDatePicker);

		myToTimePicker = new UTCTimeBox();
		myToTimePicker.addValueChangeHandler(new MyRangeHandler());
		add(myToTimePicker);

		myTimeListBox.addItem("Custom");
		for (TimeRangeEnum next : TimeRangeEnum.values()) {
			myTimeListBox.addItem(next.getFriendlyName(), next.name());
		}

		myTimeListBox.setSelectedIndex(2);
		handlePresetChange();

		if (theAddApplyButton) {
			myApplyButton = new PButton("Refresh");
			add(myApplyButton);
			myApplyButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent theEvent) {
					notifyListeners();
				}
			});
		}

		Model.getInstance().loadLocalTimezoneOffsetInMillis(new IAsyncLoadCallback<Long>() {
			@Override
			public void onSuccess(Long theResult) {
				myLocalTimeZoneOffsetInMillis = theResult;
				handlePresetChange();
			}
		});

	}

	public void addChangeHandler(ChangeHandler theChangeHandler) {
		myChangeHandlers.add(theChangeHandler);
	}

	public TimeRange getSelectedRange() {
		TimeRange retVal = new TimeRange();
		retVal.setWithPresetRange(getSelectedPresetEnum());
		if (retVal.getWithPresetRange() == null) {
			retVal.setNoPresetFromDate(myFromDatePicker.getValue());
			retVal.setNoPresetFromTime(myFromTimePicker.getValue());
			retVal.setNoPresetToDate(myToDatePicker.getValue());
			retVal.setNoPresetToTime(myToTimePicker.getValue());
		}
		return retVal;
	}

	private TimeRangeEnum getSelectedPresetEnum() {
		if (myTimeListBox.getSelectedIndex() < 1) {
			return null; // first is "custom"
		}
		return TimeRangeEnum.valueOf(myTimeListBox.getValue(myTimeListBox.getSelectedIndex()));

	}

	public void handlePresetChange() {
		if (myUpdating) {
			return;
		}
		myUpdating = true;
		try {
			TimeRangeEnum selectedPreset = getSelectedPresetEnum();
			if (selectedPreset != null) {
				Date fromDate = new Date(System.currentTimeMillis() - (60 * 1000L * selectedPreset.getNumMins()) + myLocalTimeZoneOffsetInMillis);
				myFromDatePicker.setValue(fromDate.getTime());
				myFromTimePicker.setValue(fromDate.getTime());

				Date toDate = new Date(System.currentTimeMillis() + myLocalTimeZoneOffsetInMillis);
				myToDatePicker.setValue(toDate.getTime());
				myToTimePicker.setValue(toDate.getTime());
			}
			if (myApplyButton == null) {
				notifyListeners();
			}
		} finally {
			myUpdating = false;
		}
	}

	private void notifyListeners() {
		for (ChangeHandler next : myChangeHandlers) {
			next.onChange(null);
		}
	}

	public void handleRangeChange() {
		if (myUpdating) {
			return;
		}
		myUpdating = true;
		try {
			myTimeListBox.setSelectedIndex(0);
			if (myApplyButton == null) {
				notifyListeners();
			}
		} finally {
			myUpdating = false;
		}
	}

	public class MyPresetHandler implements ChangeHandler {

		@Override
		public void onChange(ChangeEvent theEvent) {
			handlePresetChange();
		}

	}

	public class MyRangeHandler implements ValueChangeHandler<Long> {

		@Override
		public void onValueChange(ValueChangeEvent<Long> theEvent) {
			handleRangeChange();
		}

	}

}
