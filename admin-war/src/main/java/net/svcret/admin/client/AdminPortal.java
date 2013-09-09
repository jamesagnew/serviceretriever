package net.svcret.admin.client;

import net.svcret.admin.client.rpc.HttpClientConfigService;
import net.svcret.admin.client.rpc.HttpClientConfigServiceAsync;
import net.svcret.admin.client.rpc.MiscConfigService;
import net.svcret.admin.client.rpc.MiscConfigServiceAsync;
import net.svcret.admin.client.rpc.ModelUpdateService;
import net.svcret.admin.client.rpc.ModelUpdateServiceAsync;
import net.svcret.admin.client.ui.layout.OuterLayoutPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class AdminPortal implements EntryPoint, UncaughtExceptionHandler {

	public static final Images IMAGES = GWT.create(Images.class);

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	public static final ModelUpdateServiceAsync MODEL_SVC = GWT.create(ModelUpdateService.class);

	/**
	 * Messages
	 */
	public static final Messages MSGS = GWT.create(Messages.class);

	public static final HttpClientConfigServiceAsync SVC_HTTPCLIENTCONFIG = GWT.create(HttpClientConfigService.class);

	public static final MiscConfigServiceAsync SVC_MISCCONFIG = GWT.create(MiscConfigService.class);

	/**
	 * This is the entry point method.
	 */
	@Override
	public void onModuleLoad() {
//		GWT.setUncaughtExceptionHandler(this);

		RootLayoutPanel rootPanel = RootLayoutPanel.get();
		rootPanel.add(new OuterLayoutPanel());

	}

	@Override
	public void onUncaughtException(Throwable theE) {
		GWT.log("Uncaught exception", theE);
		Window.alert("Uncaught exception: " + theE.toString());
		reportError("Uncaught exception", theE);
	}

	public static void reportError(String theMessage, Throwable theException) {
		Throwable exception = theException;
		if (exception instanceof UmbrellaException) {
			UmbrellaException ue = (UmbrellaException) exception;
			exception = ue.getCause();
		}

		MODEL_SVC.reportClientError(theMessage, exception, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable theCaught) {
				// nothing
			}

			@Override
			public void onSuccess(Void theResult) {
				// nothing
			}
		});
	}
}
