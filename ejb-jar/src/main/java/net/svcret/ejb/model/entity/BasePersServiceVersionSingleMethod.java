package net.svcret.ejb.model.entity;

import java.util.Collection;

/**
 * Base class for a service version type that has only one method
 */
public abstract class BasePersServiceVersionSingleMethod extends BasePersServiceVersion {

	private static final long serialVersionUID = 1L;
	
	private static final String METHOD_NAME = "Invoc";

	@Override
	public void prePersist() {
		if (super.getMethods().size() > 1) {
			super.retainOnlyMethodsWithNames(METHOD_NAME);
		}
		if (super.getMethods().size() == 0) {
			super.getOrCreateAndAddMethodWithName(METHOD_NAME);
		}else {
			super.getMethods().get(0).setName(METHOD_NAME);
		}
	}
	
	@Override
	public void addMethod(PersServiceVersionMethod theMethod) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void retainOnlyMethodsWithNamesAndUnknownMethod(Collection<String> theIds) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void retainOnlyMethodsWithNames(String... theUrlIds) {
		throw new UnsupportedOperationException();
	}

}
