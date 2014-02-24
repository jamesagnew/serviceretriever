package net.svcret.admin.shared;

public class ArrayUtil {

	public static boolean hasValues(int[] theSuccess) {
		if (theSuccess != null) {
			for (int integer : theSuccess) {
				if (integer != 0) {
					return true;
				}
			}
		}
		return false;
	}
}
