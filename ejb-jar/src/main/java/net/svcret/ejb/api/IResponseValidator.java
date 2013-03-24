package net.svcret.ejb.api;

public interface IResponseValidator {

	ValidationResponse validate(String theBody, int theStatusCode, String theContentType);

	public static class ValidationResponse {
		private final boolean myValidates;
		private final String myFailureExplanation;

		public ValidationResponse(boolean theValidates, String theFailureExplanation) {
			super();
			myValidates = theValidates;
			myFailureExplanation = theFailureExplanation;
		}

		public ValidationResponse(boolean theValidates) {
			if (theValidates == false) {
				throw new IllegalArgumentException("Must provide a message for validation=false");
			}
			myValidates = theValidates;
			myFailureExplanation = null;
		}

		/**
		 * @return the validates
		 */
		public boolean isValidates() {
			return myValidates;
		}

		/**
		 * @return the failureExplanation
		 */
		public String getFailureExplanation() {
			return myFailureExplanation;
		}
	}

}
