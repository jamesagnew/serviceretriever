package net.svcret.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.svcret.core.ex.ProcessingRuntimeException;


public class Messages {
	private static final String BUNDLE_NAME = "net.svcret.core.MessagesBundle"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			throw new ProcessingRuntimeException("Missing key: " + key, e);
		}
	}
	
	public static String getString(String key, Object... theArgs) {
		return MessageFormat.format(getString(key), theArgs);
	}
	
	
}
