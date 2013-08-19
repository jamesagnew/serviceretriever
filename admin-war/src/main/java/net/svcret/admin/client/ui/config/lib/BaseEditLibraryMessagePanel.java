package net.svcret.admin.client.ui.config.lib;

import java.util.TreeSet;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

public abstract class BaseEditLibraryMessagePanel extends FlowPanel {

	private LoadingSpinner myInitialSpinner;
	private EditableField myDescriptionEditor;
	private DtoLibraryMessage myMessage;
	private GDomainList myDomainList;
	private EditableField myContentTypeEditor;
	private TextArea myMessageEditor;
	private LoadingSpinner mySaveSpinner;
	private FlowPanel myTopPanel;
	private FlowPanel myAppliesToPanel;
	private ListBox myContentTypeSuggestionsBox;

	public BaseEditLibraryMessagePanel() {
		FlowPanel topPanel = new FlowPanel();
		add(topPanel);

		topPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(getDialogTitle());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		topPanel.add(titleLabel);

		myTopPanel = new FlowPanel();
		topPanel.add(myTopPanel);
		myTopPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		
		myInitialSpinner = new LoadingSpinner();
		myTopPanel.add(myInitialSpinner);
		myInitialSpinner.show();

	}

	public void setContents(DtoLibraryMessage theMessage) {
		myMessage = theMessage;
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theDomainList) {
				myDomainList = theDomainList;
				initUi();
				updateContentTypeSuggestions();
			}
		});
	}

	protected void updateContentTypeSuggestions() {
		TreeSet<String> suggestions = new TreeSet<String>();
		for (Long next : myMessage.getAppliesToServiceVersionPids()) {
			suggestions.add(myDomainList.getServiceVersionByPid(next).getProtocol().getRequestContentType());
		}
		
		myContentTypeSuggestionsBox.clear();
		myContentTypeSuggestionsBox.addItem("Suggestions...");
		for (String next : suggestions) {
			myContentTypeSuggestionsBox.addItem(next);
		}
		
		if (StringUtil.isBlank(myContentTypeEditor.getValue())) {
			if (myContentTypeSuggestionsBox.getItemCount()> 1) {
				myContentTypeEditor.setValue(myContentTypeSuggestionsBox.getItemText(1));
			}
		}
		
	}

	private void initUi() {
		myInitialSpinner.hideCompletely();
		
		TwoColumnGrid grid = new TwoColumnGrid();
		myTopPanel.add(grid);
		grid.setMaximizeSecondColumn();
		
		myDescriptionEditor = new EditableField();
		myDescriptionEditor.setValue(myMessage.getDescription());
		myDescriptionEditor.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				myMessage.setDescription(myDescriptionEditor.getValue());
			}
		});
		grid.addRow("Description", myDescriptionEditor);
		
		FlowPanel ctPanel = new FlowPanel();
		myContentTypeEditor = new EditableField();
		myContentTypeEditor.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		myContentTypeEditor.setValue(myMessage.getContentType());
		myContentTypeEditor.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				myMessage.setContentType(myContentTypeEditor.getValue());
			}
		});
		myContentTypeSuggestionsBox = new ListBox(false);
		myContentTypeSuggestionsBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				if (myContentTypeSuggestionsBox.getSelectedIndex() > 0) {
					myContentTypeEditor.setValue(myContentTypeSuggestionsBox.getItemText(myContentTypeSuggestionsBox.getSelectedIndex()));
				}
			}
		});
		ctPanel.add(myContentTypeEditor);
		ctPanel.add(myContentTypeSuggestionsBox);
		grid.addRow("Content Type", ctPanel);
		
		myMessageEditor = new TextArea();
		myMessageEditor.setVisibleLines(10);
		myMessageEditor.setWidth("100%");
		myMessageEditor.setValue(myMessage.getMessage());
		myMessageEditor.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				myMessage.setMessage(myMessageEditor.getValue());
			}
		});
		grid.addRow("Message", myMessageEditor);
		
		HorizontalPanel savePanel = new HorizontalPanel();
		myTopPanel.add(savePanel);
		
		PButton saveButton = new PButton(AdminPortal.IMAGES.iconSave(), AdminPortal.MSGS.actions_Save());
		savePanel.add(saveButton);
		
		mySaveSpinner = new LoadingSpinner();
		savePanel.add(mySaveSpinner);
		
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		
		/*
		 * Applies to
		 */
		
		FlowPanel appliesToPanel = new FlowPanel();
		add(appliesToPanel);

		appliesToPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Applies To");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		appliesToPanel.add(titleLabel);

		myAppliesToPanel = new FlowPanel();
		appliesToPanel.add(myAppliesToPanel);
		myAppliesToPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);

		MessageAppliesToPanel messageAppliesToPanel = new MessageAppliesToPanel();
		messageAppliesToPanel.setMessage(myDomainList, myMessage);
		myAppliesToPanel.add(messageAppliesToPanel);
		messageAppliesToPanel.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent theEvent) {
				updateContentTypeSuggestions();
			}
		});
	}

	protected void save() {
		if (StringUtil.isBlank(myMessage.getDescription())) {
			mySaveSpinner.showMessage("No description provided!", false);
			return;
		}
		if (StringUtil.isBlank(myMessage.getContentType())) {
			mySaveSpinner.showMessage("No content type provided!", false);
			return;
		}
		if (myMessage.getAppliesToServiceVersionPids().isEmpty()) {
			mySaveSpinner.showMessage("No services have been selected in 'Applies To' section below!", false);
			return;
		}
		
		mySaveSpinner.showMessage("Saving...", true);
		AdminPortal.MODEL_SVC.saveLibraryMessage(myMessage, new AsyncCallback<Void>() {

			@Override
			public void onSuccess(Void theResult) {
				mySaveSpinner.showMessage("Saved", false);
			}

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
		});
	}

	protected abstract String getDialogTitle();

}
