package net.svcret.admin.client.ui.components;

import net.svcret.admin.shared.enm.ThrottlePeriodEnum;
import net.svcret.admin.shared.model.IHasThrottle;
import net.svcret.admin.shared.model.IThrottleable;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;

public class ThrottleEditorGrid extends TwoColumnGrid {

	private CheckBox myThrottleCheckbox;
	private IntegerBox myThrottleNumberBox;
	private ListBox myThrottlePeriodCombo;
	private CheckBox myThrottleQueueCheckbox;
	private IntegerBox myThrottleQueueMaxLength;
	private IHasThrottle<?> myThrottle;

	public ThrottleEditorGrid() {
		// TODO Auto-generated constructor stub
	}

	public ThrottleEditorGrid(IHasThrottle<?> theThrottle) {
		setThrottle(theThrottle);
	}

	public <T extends IThrottleable> void setThrottle(final IHasThrottle<T> theThrottle) {
		if (myThrottle != null) {
			throw new IllegalStateException("Already have throttle");
		}
		myThrottle = theThrottle;
		
		myThrottleCheckbox = new CheckBox("Enable Throttling");
		myThrottleNumberBox = new IntegerBox();
		myThrottlePeriodCombo = new ListBox(false);
		for (ThrottlePeriodEnum next : ThrottlePeriodEnum.values()) {
			myThrottlePeriodCombo.addItem(next.getDescription(), next.name());
		}
		HorizontalPanel throttleCheckboxValuePanel = new HorizontalPanel();
		throttleCheckboxValuePanel.add(myThrottleNumberBox);
		throttleCheckboxValuePanel.add(new HTML("&nbsp;requests&nbsp;/&nbsp;"));
		throttleCheckboxValuePanel.add(myThrottlePeriodCombo);
		addRow(myThrottleCheckbox, throttleCheckboxValuePanel);
		addDescription("If enabled, requests from this user will not be permitted to proceed " + "faster than the given rate.");

		myThrottleQueueCheckbox = new CheckBox("Queue");
		myThrottleQueueMaxLength = new IntegerBox();
		addRow(myThrottleQueueCheckbox, myThrottleQueueMaxLength);
		addDescription("If enabled, up to the given number of requests will be allowed to queue if they arrive " + "faster than the given throttle limit. After the throttle period has been passed, they will be permitted to "
				+ "proceed (and will receive a normal response). If more than the given number of requests arrive during the " + "throttle period, any excess requests will be rejected immediately.");
		
		updateControls();
		
		myThrottleCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				if (theEvent.getValue() && theThrottle.getThrottle() == null) {
					theThrottle.setThrottle(theThrottle.instantiateNew());
					theThrottle.getThrottle().setThrottleMaxRequests(10);
					theThrottle.getThrottle().setThrottlePeriod(ThrottlePeriodEnum.SECOND);
					updateControls();
				} else if (theEvent.getValue() == false && theThrottle.getThrottle() != null) {
					theThrottle.setThrottle(null);
					updateControls();
				}
			}
		});

		myThrottleQueueCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theEvent) {
				if (theEvent.getValue() && myThrottle.getThrottle().getThrottleMaxQueueDepth() == null) {
					myThrottle.getThrottle().setThrottleMaxQueueDepth(10);
				}else				if (theEvent.getValue() ==false && myThrottle.getThrottle().getThrottleMaxQueueDepth() != null) {
					myThrottle.getThrottle().setThrottleMaxQueueDepth(null);
				}
				updateControls();
			}
		});
		
		myThrottleNumberBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> theEvent) {
				myThrottle.getThrottle().setThrottleMaxRequests(theEvent.getValue());
			}
		});
		
		myThrottlePeriodCombo.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				myThrottle.getThrottle().setThrottlePeriod(ThrottlePeriodEnum.valueOf(myThrottlePeriodCombo.getValue(myThrottlePeriodCombo.getSelectedIndex())));
			}
		});
		
		myThrottleQueueMaxLength.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> theEvent) {
				myThrottle.getThrottle().setThrottleMaxQueueDepth(theEvent.getValue());
			}
		});
		
	}

	protected void updateControls() {
		if (myThrottle.getThrottle() != null && myThrottle.getThrottle().getThrottleMaxRequests() == null) {
			myThrottle.setThrottle(null);
		}
		
		myThrottleCheckbox.setValue(myThrottle.getThrottle() != null, false);
		myThrottleNumberBox.setEnabled(myThrottleCheckbox.getValue());
		myThrottlePeriodCombo.setEnabled(myThrottleCheckbox.getValue());
		myThrottleQueueCheckbox.setEnabled(myThrottleCheckbox.getValue());
		myThrottleQueueMaxLength.setEnabled(myThrottleCheckbox.getValue());
		
		if (myThrottle.getThrottle() != null) {
			
			myThrottleNumberBox.setValue(myThrottle.getThrottle().getThrottleMaxRequests(), false);
			myThrottlePeriodCombo.setItemSelected(myThrottle.getThrottle().getThrottlePeriod().ordinal(), true);

			if (myThrottle.getThrottle().getThrottleMaxQueueDepth() != null) {
				myThrottleQueueCheckbox.setValue(true, false);
				myThrottleQueueMaxLength.setEnabled(true);
				myThrottleQueueMaxLength.setValue(myThrottle.getThrottle().getThrottleMaxQueueDepth(), false);
			}else {
				myThrottleQueueMaxLength.setEnabled(false);
			}
			
		}

	}
	
	

}
