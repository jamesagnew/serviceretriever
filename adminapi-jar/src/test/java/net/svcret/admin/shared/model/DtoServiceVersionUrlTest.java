package net.svcret.admin.shared.model;

import static org.junit.Assert.*;

import java.util.Date;

import net.svcret.admin.shared.enm.ResponseTypeEnum;

import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

public class DtoServiceVersionUrlTest {

	@Test
	public void testGetLastStatus() {
		
		GServiceVersionUrl s = new GServiceVersionUrl();
		assertNull(s.getStatsLastResponseType());
		
		s.setStatsLastFailure(new Date(10000));
		assertEquals(ResponseTypeEnum.FAIL, s.getStatsLastResponseType());
		
		s.setStatsLastFault(new Date(20000));
		assertEquals(ResponseTypeEnum.FAULT, s.getStatsLastResponseType());

		s.setStatsLastSuccess(new Date(30000));
		assertEquals(ResponseTypeEnum.SUCCESS, s.getStatsLastResponseType());

		s.setStatsLastFault(new Date(40000));
		assertEquals(ResponseTypeEnum.FAULT, s.getStatsLastResponseType());

	}
	

	@Test
	public void testGetLastResponseTypesFromMostRecentToLeast() {
		
		GServiceVersionUrl s = new GServiceVersionUrl();
		assertThat(s.getStatsLastResponseTypesFromMostRecentToLeast(), IsEmptyCollection.empty());
		
		s.setStatsLastFailure(new Date(10000));
		assertThat(s.getStatsLastResponseTypesFromMostRecentToLeast(), IsIterableContainingInOrder.contains(ResponseTypeEnum.FAIL));
		
		s.setStatsLastFault(new Date(20000));
		assertThat(s.getStatsLastResponseTypesFromMostRecentToLeast(), IsIterableContainingInOrder.contains(ResponseTypeEnum.FAULT,ResponseTypeEnum.FAIL));

		s.setStatsLastSuccess(new Date(30000));
		assertThat(s.getStatsLastResponseTypesFromMostRecentToLeast(), IsIterableContainingInOrder.contains(ResponseTypeEnum.SUCCESS, ResponseTypeEnum.FAULT,ResponseTypeEnum.FAIL));

		s.setStatsLastFault(new Date(40000));
		assertThat(s.getStatsLastResponseTypesFromMostRecentToLeast(), IsIterableContainingInOrder.contains(ResponseTypeEnum.FAULT,ResponseTypeEnum.SUCCESS, ResponseTypeEnum.FAIL));

	}

}
