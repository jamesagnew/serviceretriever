package net.svcret.admin.client.ui.config.auth;

import static net.svcret.admin.client.AdminPortal.MSGS;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.HtmlH1;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.DtoAuthenticationHostLdap;

import com.google.gwt.user.client.ui.TextBox;

public class LdapAuthenticationHostEditPanel extends BaseAuthenticationHostEditPanel<DtoAuthenticationHostLdap> {

	private TextBox myUrlTextBox;
	private TextBox myBindUserTextBox;
	private TextBox myBindPasswordTextBox;
	private TextBox myAuthenticateBaseDnTextBox;
	private TextBox myAuthenticateFilterTextBox;

	public LdapAuthenticationHostEditPanel(AuthenticationHostsPanel thePanel, DtoAuthenticationHostLdap theAuthHost) {
		super(thePanel, theAuthHost);

		// URL

		getContentPanel().add(new HtmlH1(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_LdapPropertiesTitle()));

		TwoColumnGrid propsGrid = new TwoColumnGrid();
		getContentPanel().add(propsGrid);

		myUrlTextBox = new TextBox();
		myUrlTextBox.setValue(theAuthHost.getUrl());
		propsGrid.addRow(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_UrlName(), myUrlTextBox);
		propsGrid.addDescription(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_UrlDescription());

		// Bind User

		myBindUserTextBox = new TextBox();
		myBindUserTextBox.setValue(theAuthHost.getBindUserDn());
		propsGrid.addRow(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_BindUserDnName(), myBindUserTextBox);
		propsGrid.addDescription(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_BindUserDnDescription());

		myBindPasswordTextBox = new TextBox();
		myBindPasswordTextBox.setValue(theAuthHost.getBindUserPassword());
		propsGrid.addRow(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_BindUserPasswordName(), myBindPasswordTextBox);
		propsGrid.addDescription(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_BindUserPasswordDescription());
		
		// Authenticate Base DN

		myAuthenticateBaseDnTextBox = new TextBox();
		myAuthenticateBaseDnTextBox.setValue(theAuthHost.getAuthenticateBaseDn());
		propsGrid.addRow(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_AuthenticateBaseDnName(), myAuthenticateBaseDnTextBox);
		propsGrid.addDescription(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_AuthenticateBaseDnDescription());
		myAuthenticateFilterTextBox = new TextBox();
		myAuthenticateFilterTextBox.setValue(theAuthHost.getAuthenticateFilter());
		propsGrid.addRow(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_AuthenticateFilterName(), myAuthenticateFilterTextBox);
		propsGrid.addDescription(AdminPortal.MSGS.ldapAuthenticationHostEditPanel_AuthenticateFilterDescription("{0}"));

	}

	@Override
	protected String getPanelDescription() {
		return AdminPortal.MSGS.localDatabaseAuthenticationHostEditPanel_description();
	}

	@Override
	protected String getPanelTitle() {
		return MSGS.localDatabaseAuthenticationHostEditPanel_title();
	}

	@Override
	protected void applySettingsFromUi() {
		getAuthHost().setUrl(myUrlTextBox.getValue());
		getAuthHost().setBindUserDn(myBindUserTextBox.getValue());
		getAuthHost().setBindUserPassword(myBindPasswordTextBox.getValue());
		getAuthHost().setAuthenticateBaseDn(myAuthenticateBaseDnTextBox.getValue());
		getAuthHost().setAuthenticateFilter(myAuthenticateFilterTextBox.getValue());
	}

}
