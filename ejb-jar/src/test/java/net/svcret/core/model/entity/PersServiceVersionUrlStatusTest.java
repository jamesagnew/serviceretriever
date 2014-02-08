package net.svcret.core.model.entity;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.core.ejb.DefaultAnswer;
import net.svcret.core.model.entity.PersHttpClientConfig;
import net.svcret.core.model.entity.PersServiceVersionUrl;
import net.svcret.core.model.entity.PersServiceVersionUrlStatus;
import net.svcret.core.model.entity.soap.PersServiceVersionSoap11;

import org.junit.Before;
import org.junit.Test;


public class PersServiceVersionUrlStatusTest {

	@Before
	public void before() {
		DefaultAnswer.setDesignTime();
	}
	
	@Test
	public void testCircuitBreaker() throws InterruptedException {
//		Logger.getLogger("").setLevel(Level.FINEST);
//		Logger.getLogger("").getHandlers()[0].setLevel(Level.FINEST);
		
		
		PersHttpClientConfig cfg = mock(PersHttpClientConfig.class, DefaultAnswer.INSTANCE);
		when(cfg.getCircuitBreakerTimeBetweenResetAttempts()).thenReturn(500);
		when(cfg.isCircuitBreakerEnabled()).thenReturn(true);
		
		PersServiceVersionSoap11 version = mock(PersServiceVersionSoap11.class, DefaultAnswer.INSTANCE);
		when(version.getHttpClientConfig()).thenReturn(cfg);
		
		PersServiceVersionUrl url = mock(PersServiceVersionUrl.class, DefaultAnswer.INSTANCE);
		when(url.getServiceVersion()).thenReturn(version);
		
		PersServiceVersionUrlStatus status = new PersServiceVersionUrlStatus();
		status.setUrl(url);
		
		DefaultAnswer.setRunTime();
		
		assertEquals(true, status.attemptToResetCircuitBreaker());
		assertEquals(true, status.attemptToResetCircuitBreaker());

		status.setStatus(StatusEnum.UNKNOWN);
		assertEquals(true, status.attemptToResetCircuitBreaker());
		assertEquals(true, status.attemptToResetCircuitBreaker());

		status.setStatus(StatusEnum.ACTIVE);
		assertEquals(true, status.attemptToResetCircuitBreaker());
		assertEquals(true, status.attemptToResetCircuitBreaker());
		
		long now = System.currentTimeMillis();
		
		status.setStatus(StatusEnum.DOWN);
		Date nextAttempt = status.getNextCircuitBreakerReset();
		assertThat(nextAttempt.getTime(), greaterThanOrEqualTo(now + 500));
		assertThat(nextAttempt.getTime(), lessThan(now + 700));
		
		assertEquals(false, status.attemptToResetCircuitBreaker());
		assertEquals(false, status.attemptToResetCircuitBreaker());
		assertEquals(false, status.attemptToResetCircuitBreaker());
		assertEquals(false, status.attemptToResetCircuitBreaker());

		Thread.sleep(510);
		
		assertEquals(true, status.attemptToResetCircuitBreaker());
		assertEquals(false, status.attemptToResetCircuitBreaker());
		assertEquals(false, status.attemptToResetCircuitBreaker());
		assertEquals(false, status.attemptToResetCircuitBreaker());

	}
	
}
