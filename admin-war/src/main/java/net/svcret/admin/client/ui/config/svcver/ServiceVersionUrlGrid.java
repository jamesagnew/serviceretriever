package net.svcret.admin.client.ui.config.svcver;

import static net.svcret.admin.client.AdminPortal.*;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;
import net.svcret.admin.shared.util.StringUtil;

public class ServiceVersionUrlGrid extends BaseUrlGrid {

	private BaseDtoServiceVersion myServiceVersion;

	public ServiceVersionUrlGrid() {
		this.add(new Label("Each proxied service will have one or more implementation URLs. " + "When a client attempts to invoke a service that has been proxied, the ServiceProxy will "
				+ "forward this request to one of these implementations. Specifying more than one " + "implementation URL means that if one is unavailable, another can be tried (i.e. redundancy)."));

		init();

		this.add(new HtmlBr());

		PButton addButton = new PButton(IMAGES.iconAdd(), MSGS.actions_Add());
		this.add(addButton);
		HtmlLabel addNameLabel = new HtmlLabel("URL:", "addUrlTb");
		this.add(addNameLabel);
		final TextBox addText = new TextBox();
		addText.getElement().setId("addUrlTb");
		this.add(addText);
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				String urlText = addText.getValue();
				if (StringUtil.isBlank(urlText)) {
					Window.alert("Please enter a URL");
					addText.setFocus(true);
					return;
				}
				if (myServiceVersion.hasUrlWithName(urlText)) {
					Window.alert("Duplicate URL: " + urlText);
					return;
				}

				GServiceVersionUrl url = new GServiceVersionUrl();
				url.setUncommittedSessionId(BaseDetailPanel.newUncommittedSessionId());
				url.setUrl(urlText);

				myServiceVersion.getUrlList().add(url);

				for (int urlNum = myServiceVersion.getUrlList().size();; urlNum++) {
					String name = "url" + urlNum;
					if (myServiceVersion.getUrlList().getUrlWithId(name) == null) {
						url.setId(name);
						break;
					}
				}

				doUpdateUrlPanel(myServiceVersion.getUrlList().toList());

				addText.setValue("");
			}
		});
	}

	public BaseDtoServiceVersion getServiceVersion() {
		return myServiceVersion;
	}

	public void setServiceVersion(BaseDtoServiceVersion theServiceVersion) {
		myServiceVersion = theServiceVersion;
		updateUrlPanel();
	}

	private void updateUrlPanel() {
		if (myServiceVersion.getPidOrNull() == null) {
			doUpdateUrlPanel(myServiceVersion.getUrlList().toList());
			return;
		}

		ModelUpdateRequest request = new ModelUpdateRequest();
		for (GServiceVersionUrl next : myServiceVersion.getUrlList()) {
			if (next.getPidOrNull() != null) {
				request.addUrlToLoadStats(next.getPidOrNull());
			}
		}

		AsyncCallback<ModelUpdateResponse> callback = new AsyncCallback<ModelUpdateResponse>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(ModelUpdateResponse theResult) {
				BaseDtoServiceVersion newServiceVersion = theResult.getDomainList().getServiceVersionByPid(myServiceVersion.getPid());
				myServiceVersion.merge(newServiceVersion);
				doUpdateUrlPanel(myServiceVersion.getUrlList().toList());
			}
		};
		AdminPortal.MODEL_SVC.loadModelUpdate(request, callback);

	}

	@Override
	protected Widget createActionPanel(GServiceVersionUrl theUrl) {
		return new UrlEditButtonPanel(theUrl);
	}

	private final class UrlEditButtonPanel extends FlowPanel implements ClickHandler {
		private PButton myDeleteButton;
		private GServiceVersionUrl myUrl;

		private UrlEditButtonPanel(GServiceVersionUrl theUrl) {
			myUrl = theUrl;

			myDeleteButton = new PButton(IMAGES.iconRemove());
			myDeleteButton.addClickHandler(this);
			add(myDeleteButton);
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			PButton source = (PButton) theEvent.getSource();
			if (source == myDeleteButton) {
				if (Window.confirm("Delete - Are you sure?")) {
					myServiceVersion.getUrlList().remove(myUrl);
					doUpdateUrlPanel(myServiceVersion.getUrlList().toList());
				}
			}

			source.setEnabled(false);
		}
	}
	
	@Override
	protected EditableField createUrlWidget(final GServiceVersionUrl theUrl) {
		EditableField urlField = new EditableField();
		urlField.setMultiline(false);
		urlField.setTransparent(true);
		urlField.setProcessHtml(false);
		urlField.setMaxFieldWidth(180);
		urlField.setLabelIsPlainText(true);
		urlField.setShowTooltip(true);
		urlField.setValue(theUrl.getUrl());
		if (StringUtil.isBlank(theUrl.getId())) {
			urlField.setEditorMode();
		}
		urlField.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				theUrl.setUrl(theEvent.getValue());
			}
		});
		return urlField;
	}


	@Override
	protected String provideActionColumnHeaderText() {
		return "";
	}

}
