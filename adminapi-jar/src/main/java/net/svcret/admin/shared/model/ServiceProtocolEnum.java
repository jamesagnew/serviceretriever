package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ServiceProtocolEnum {

	/*
	 * NB: Add new entries to the natural order in the static block below
	 */
	JSONRPC20("JSON-RPC 2.0"), SOAP11("SOAP 1.1");

	private static List<ServiceProtocolEnum> ourNaturalOrder;

	static {
		List<ServiceProtocolEnum> naturalOrder = new ArrayList<ServiceProtocolEnum>();
		naturalOrder.add(SOAP11);
		naturalOrder.add(JSONRPC20);
		ourNaturalOrder = Collections.unmodifiableList(naturalOrder);
	}

	public static List<ServiceProtocolEnum> getNaturalOrder() {
		return ourNaturalOrder;
	}

	private String myNiceName;

	private ServiceProtocolEnum(String theName) {
		myNiceName = theName;
	}

	/**
	 * @return the niceName
	 */
	public String getNiceName() {
		return myNiceName;
	}

}
