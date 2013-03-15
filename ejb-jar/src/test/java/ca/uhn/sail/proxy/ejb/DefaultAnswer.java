package ca.uhn.sail.proxy.ejb;

import static org.mockito.Mockito.*;

import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsExceptionClass;
import org.mockito.invocation.InvocationOnMock;

public final class DefaultAnswer extends ThrowsExceptionClass {
	
	private static boolean myActive = false;
	
	private static final long serialVersionUID = 1L;

	public static final MockSettings INSTANCE = withSettings().defaultAnswer(new DefaultAnswer()); 
	
	public DefaultAnswer() {
		super(Throwable.class);
	}

	@Override
	public Object answer(InvocationOnMock theInvocation) throws Throwable {
		if (theInvocation.getMethod().getReturnType() == void.class) {
			return null;
		}
		
		if (!myActive) {
			return Mockito.RETURNS_DEFAULTS.answer(theInvocation);
		}
		return super.answer(theInvocation);
	}
	
	public static void setDesignTime() {
		myActive = false;
	}

	public static void setRunTime() {
		myActive = true;
	}

}