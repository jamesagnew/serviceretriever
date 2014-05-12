package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ServiceProtocolEnum {

	HL7OVERHTTP("HL7 over HTTP", "application/hl7-v2"), 
	
	/*
	 * NB: Add new entries to the natural order in the static block below
	 */
	JSONRPC20("JSON-RPC 2.0", "application/json"), 
	
	SOAP11("SOAP 1.1", "text/xml"), 
	
	VIRTUAL("Virtual Service", ""), 
	
	REST("REST", "")
	
	;

	private static List<ServiceProtocolEnum> ourNaturalOrder;

	static {
		List<ServiceProtocolEnum> naturalOrder = new ArrayList<ServiceProtocolEnum>();
		naturalOrder.add(SOAP11);
		naturalOrder.add(JSONRPC20);
		naturalOrder.add(HL7OVERHTTP);
		naturalOrder.add(REST);
		naturalOrder.add(VIRTUAL);
		ourNaturalOrder = Collections.unmodifiableList(naturalOrder);
	}

	private String myNiceName;

	private String myRequestContentType;
	private ServiceProtocolEnum(String theName, String theRequestContentType) {
		myNiceName = theName;
		myRequestContentType=theRequestContentType;
	}

	/**
	 * @return the niceName
	 */
	public String getNiceName() {
		return myNiceName;
	}

	public String getRequestContentType() {
		return myRequestContentType;
	}

	public static List<ServiceProtocolEnum> getNaturalOrder() {
		return ourNaturalOrder;
	}

}
