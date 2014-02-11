package net.svcret.core.util;

import static org.mockito.Mockito.*;

import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;
import org.springframework.beans.factory.FactoryBean;

public class MockitoFactoryBean<T> implements FactoryBean<T> {

	private Class<T> myClassToMock;

	public void setClassToMock(Class<T> theClassToMock) {
		myClassToMock = theClassToMock;
	}

	@Override
	public T getObject() throws Exception {
		return mock(myClassToMock, new ReturnsDeepStubs());
	}

	@Override
	public Class<?> getObjectType() {
		return myClassToMock;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
	
}
