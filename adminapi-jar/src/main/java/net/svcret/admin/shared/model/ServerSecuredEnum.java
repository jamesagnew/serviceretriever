package net.svcret.admin.shared.model;

public enum ServerSecuredEnum {

	FULLY, PARTIALLY, NONE;

	public static ServerSecuredEnum merge(ServerSecuredEnum theInitial, ServerSecuredEnum theAdd) {
		if (theInitial == null) {
			return theAdd;
		}
		if (theInitial == theAdd) {
			return theInitial;
		}

		if (theAdd != null) {
			switch (theAdd) {
			case FULLY:
				switch (theInitial) {
				case NONE:
				case PARTIALLY:
					return PARTIALLY;
				default:
				}

			case NONE:
				switch (theInitial) {
				case FULLY:
				case PARTIALLY:
					return PARTIALLY;
				default:
				}

			default:
			}
		}

		return theInitial;
	}

}
