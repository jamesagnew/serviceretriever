package net.svcret.admin.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "IntArray")
public class DtoIntArray implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "val")
	private int[] myValues;

	public DtoIntArray() {
	}

	public DtoIntArray(int[] theValues) {
		myValues = theValues;
	}

	public int[] getValues() {
		return myValues;
	}

	public void setValues(int[] theValues) {
		myValues = theValues;
	}

	public static int[] from(DtoIntArray theIntArray) {
		if (theIntArray == null) {
			return null;
		}
		return theIntArray.getValues();
	}

	public static DtoIntArray to(int[] theValues) {
		if (theValues == null) {
			return null;
		}
		return new DtoIntArray(theValues);
	}

}
