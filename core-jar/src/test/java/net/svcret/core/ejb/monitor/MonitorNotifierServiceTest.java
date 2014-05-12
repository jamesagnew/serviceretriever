package net.svcret.core.ejb.monitor;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.core.ejb.monitor.MonitorNotifierService;
import net.svcret.core.model.entity.PersMonitorRuleFiring;
import net.svcret.core.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.core.model.entity.PersServiceVersionUrl;

import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

public class MonitorNotifierServiceTest {
private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MonitorNotifierServiceTest.class);
	@Test
	public void testGenerateEmail() {
		
		MonitorNotifierService svc = new MonitorNotifierService();
		
		PersMonitorRuleFiring firing = mock(PersMonitorRuleFiring.class, new ReturnsDeepStubs());
				
		ArrayList<PersMonitorRuleFiringProblem> problems = new ArrayList<PersMonitorRuleFiringProblem>();
		PersMonitorRuleFiringProblem prob1=mock(PersMonitorRuleFiringProblem.class, new ReturnsDeepStubs());
		PersMonitorRuleFiringProblem prob2=mock(PersMonitorRuleFiringProblem.class, new ReturnsDeepStubs());
		when(prob1.getLatencyAverageMillisPerCall()).thenReturn(104L);
		when(prob1.getLatencyThreshold()).thenReturn(40L);
		when(prob1.getLatencyAverageOverMinutes()).thenReturn(5L);
		when(prob2.getLatencyAverageMillisPerCall()).thenReturn(null);
		when(prob2.getFailedUrlMessage()).thenReturn("URL could not be reached");
		problems.add(prob2); // out of order intentionally
		problems.add(prob1);
		
		when(firing.getRule().getRuleName()).thenReturn("The Rule NameZZZ");
		when(firing.getStartDate()).thenReturn(new Date());
		when(firing.getProblems()).thenReturn(problems);
		when(firing.getRule().getRuleType()).thenReturn(MonitorRuleTypeEnum.ACTIVE);
		
		when(prob1.getServiceVersion().getVersionId()).thenReturn("V1");
		when(prob2.getServiceVersion().getVersionId()).thenReturn("V2");
		when(prob1.getServiceVersion().getService().getServiceName()).thenReturn("S1");
		when(prob2.getServiceVersion().getService().getServiceName()).thenReturn("S2");
		when(prob1.getServiceVersion().getService().getDomain().getDomainName()).thenReturn("D1");
		when(prob2.getServiceVersion().getService().getDomain().getDomainName()).thenReturn("D2");
		when(prob1.getUrl()).thenReturn(new PersServiceVersionUrl(0L, "dev1", "http://foo"));
		when(prob2.getUrl()).thenReturn(new PersServiceVersionUrl(0L, "dev2", "http://bar"));
		
		String email = svc.generateEmail(firing);
		
		ourLog.info("Email: {}", email);
		
	}
	
	
	public static void main(String[] args) {
		new MonitorNotifierServiceTest().testGenerateEmail();
	}
}
