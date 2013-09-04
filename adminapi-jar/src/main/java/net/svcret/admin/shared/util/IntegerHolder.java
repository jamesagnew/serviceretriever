package net.svcret.admin.shared.util;

public final class IntegerHolder {

	/** The <code>int</code> contained by this holder. */
	private int value;

	/**
	 * Make a new <code>IntHolder</code> with a <code>null</code> value.
	 */
	public IntegerHolder() {
	}

	/**
	 * Make a new <code>IntHolder</code> with <code>value</code> as the value.
	 * 
	 * @param value
	 *            the <code>int</code> to hold
	 */
	public IntegerHolder(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void increment() {
		value++;
	}

	public void setValue(int theValue) {
		value = theValue;

	}

	@Override
	public String toString() {
		return Integer.toString(value);
	}
}