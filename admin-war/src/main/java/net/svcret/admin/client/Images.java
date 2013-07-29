package net.svcret.admin.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Images extends ClientBundle {

	@Source("net/svcret/images/icon_save_24.png")
	ImageResource iconSave();

	@Source("net/svcret/images/icon_add_24.png")
	ImageResource iconAdd();

	@Source("net/svcret/images/icon_edit_24.png")
	ImageResource iconEdit();

	@Source("net/svcret/images/icon_remove_24.png")
	ImageResource iconRemove();

	@Source("net/svcret/images/dash_secure_24.png")
	ImageResource dashSecure();

	@Source("net/svcret/images/icon_status_24.png")
	ImageResource iconStatus();

	@Source("net/svcret/images/icon_test_24.png")
	ImageResource iconTest();

	@Source("net/svcret/images/icon_i_16.png")
	ImageResource iconI16();

	@Source("net/svcret/images/icon_arrow_simple_right_24.png")
	ImageResource arrowSimpleRight();

	@Source("net/svcret/images/icon_arrow_simple_left_24.png")
	ImageResource arrowSimpleLeft();
	
	@Source("net/svcret/images/dash_monitor_ok_16.png")
	ImageResource dashMonitorOk();

	@Source("net/svcret/images/dash_monitor_norules_16.png")
	ImageResource dashMonitorNorules();
	
	@Source("net/svcret/images/dash_monitor_alert_16.png")
	ImageResource dashMonitorAlert();
}