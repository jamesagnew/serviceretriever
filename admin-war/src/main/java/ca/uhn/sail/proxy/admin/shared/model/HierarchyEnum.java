package ca.uhn.sail.proxy.admin.shared.model;

public enum HierarchyEnum {

	DOMAIN(0),
	SERVICE(1),
	VERSION(2),
	METHOD(3);
	
	private int myOrdinal;

	HierarchyEnum(int theOrdinal) {
		myOrdinal = theOrdinal;
	}

	public static int getHighestOrdinal() {
		return METHOD.myOrdinal;
	}
	
	public int getOrdinal() {
		return myOrdinal;
	}
}
