package ca.uhn.sail.proxy.admin.client.ui.config.sec;

import com.google.gwt.user.client.ui.Widget;

public interface IProvidesViewAndEdit<T> {

	Widget provideView(int theRow, T theObject);

	/**
	 * 
	 * @param theObject
	 * @param theValueChangeHandler
	 *            Notified when values change. The object will be updated by the
	 *            provider so the VCF doesn't need to handle that, but it might
	 *            want to store the resulting data
	 * @return
	 */
	Widget provideEdit(int theRow, T theObject, IValueChangeHandler theValueChangeHandler);

	public interface IValueChangeHandler
	{
		
		void onValueChange();
		
	}
	
}
