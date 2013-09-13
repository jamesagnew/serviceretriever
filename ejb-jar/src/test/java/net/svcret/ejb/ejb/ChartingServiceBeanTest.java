package net.svcret.ejb.ejb;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.shared.model.TimeRange;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.api.IRuntimeStatusQueryLocal;
import net.svcret.ejb.api.IScheduler;
import net.svcret.ejb.model.entity.InvocationStatsIntervalEnum;
import net.svcret.ejb.model.entity.PersConfig;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStats;
import net.svcret.ejb.model.entity.PersInvocationMethodUserStatsPk;
import net.svcret.ejb.model.entity.PersServiceVersionMethod;
import net.svcret.ejb.model.entity.PersUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;

public class ChartingServiceBeanTest {

	private ChartingServiceBean mySvc;
	private IConfigService myConfigSvc;
	private IDao myDao;
	private IScheduler myScheduler;
	private IRuntimeStatusQueryLocal myStatus;
	private SimpleDateFormat myFmt;
	private PersConfig myConfig;
	private SimpleDateFormat myFmtSecs;

	@Before
	public void before() throws Exception {
		myFmt = new SimpleDateFormat("HH:mm");
		myFmtSecs = new SimpleDateFormat("HH:mm:ss");
		
		mySvc = new ChartingServiceBean();
		
		myConfigSvc=mock(IConfigService.class);
		myConfig= new PersConfig();
		myConfig.setNow(myFmt.parse("14:05").getTime());
		when(myConfigSvc.getConfig()).thenReturn(myConfig);
		
		mySvc.setConfigForUnitTest(myConfigSvc);
		
		myDao = mock(IDao.class);
		mySvc.setDaoForUnitTest(myDao);
		
		myScheduler =mock(IScheduler.class);
		mySvc.setSchedulerForUnitTest(myScheduler);
		
		myStatus=mock(IRuntimeStatusQueryLocal.class);
		mySvc.setStatusForUnitTest(myStatus);
		
	}
	
	@Test
	public void testGenerateUserMethodGraph() throws Exception {
		
		PersUser user = new PersUser();
		user.setPid(1L);
		
		TimeRange range = new TimeRange();
		range.setNoPresetFrom(myFmtSecs.parse("04:00:01"));
		range.setNoPresetTo(myFmtSecs.parse("14:00:01"));
		
		PersServiceVersionMethod m1 = mock(PersServiceVersionMethod.class, new ReturnsDeepStubs());
		when(m1.getPid()).thenReturn(11L);
		when(m1.getName()).thenReturn("MethodName123");
		when(m1.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m1.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m1.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersServiceVersionMethod m2 = mock(PersServiceVersionMethod.class, new ReturnsDeepStubs());
		when(m2.getPid()).thenReturn(21L);
		when(m2.getName()).thenReturn("MethodName22");
		when(m2.getServiceVersion().getVersionId()).thenReturn("Version1");
		when(m2.getServiceVersion().getService().getServiceId()).thenReturn("Service1");
		when(m2.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
		PersServiceVersionMethod m3 = mock(PersServiceVersionMethod.class, new ReturnsDeepStubs());
		when(m3.getPid()).thenReturn(31L);
		when(m3.getName()).thenReturn("MethodName3");
		when(m3.getServiceVersion().getVersionId()).thenReturn("Version2");
		when(m3.getServiceVersion().getService().getServiceId()).thenReturn("Service2");
		when(m3.getServiceVersion().getService().getDomain().getDomainId()).thenReturn("Domain1");
	
		when(myDao.getUser(eq(1L))).thenReturn(user);
		when(myDao.getServiceVersionMethodByPid(eq(11L))).thenReturn(m1);
		when(myDao.getServiceVersionMethodByPid(eq(21L))).thenReturn(m2);
		when(myDao.getServiceVersionMethodByPid(eq(31L))).thenReturn(m3);
		
		List<PersInvocationMethodUserStats> statsList=new ArrayList<PersInvocationMethodUserStats>();
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:10"), m1, user)));
		statsList.get(statsList.size()-1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:20"), m1, user)));
		statsList.get(statsList.size()-1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m1, user)));
		statsList.get(statsList.size()-1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:30"), m2, user)));
		statsList.get(statsList.size()-1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m2, user)));
		statsList.get(statsList.size()-1).addSuccessInvocation(100, 200, 300);
		statsList.add(new PersInvocationMethodUserStats(new PersInvocationMethodUserStatsPk(InvocationStatsIntervalEnum.TEN_MINUTE, myFmt.parse("04:40"), m3, user)));
		statsList.get(statsList.size()-1).addSuccessInvocation(100, 200, 300);
		
		when(myDao.getUserStatsWithinTimeRange(eq(user), eq(range.getNoPresetFrom()), eq(range.getNoPresetTo()))).thenReturn(statsList);
		
		byte[] bytes = mySvc.renderUserMethodGraphForUser(user.getPid(), range);
		
		new File("target/test-pngs").mkdirs();
		FileOutputStream fos = new FileOutputStream("target/test-pngs/userMethod.png", false);
		fos.write(bytes);
		fos.close();
		
	}
	
	@Test
	public void testGenerateUserMethodGraphNoData() throws Exception {
		
		PersUser user = new PersUser();
		user.setPid(1L);
		
		TimeRange range = new TimeRange();
		range.setNoPresetFrom(myFmt.parse("04:00"));
		range.setNoPresetTo(myFmt.parse("14:00"));
			
		when(myDao.getUser(eq(1L))).thenReturn(user);
		
		List<PersInvocationMethodUserStats> statsList=new ArrayList<PersInvocationMethodUserStats>();
		when(myDao.getUserStatsWithinTimeRange(eq(user), eq(range.getNoPresetFrom()), eq(range.getNoPresetTo()))).thenReturn(statsList);
		
		byte[] bytes = mySvc.renderUserMethodGraphForUser(user.getPid(), range);
		
		new File("target/test-pngs").mkdirs();
		FileOutputStream fos = new FileOutputStream("target/test-pngs/userMethodNoData.png", false);
		fos.write(bytes);
		fos.close();
		
	}

}
