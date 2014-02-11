package net.svcret.core.model.entity;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import net.svcret.core.model.entity.PersMethodStatus;

import org.junit.Test;

public class BasePersMethodStatusTest {

	private SimpleDateFormat myFmt = new SimpleDateFormat("HH:mm");
	
	@Test
	public void doesRangeOverlapWithAnyOfMyRanges() throws ParseException {
		
		PersMethodStatus status = new PersMethodStatus();
		
		// Range is 01:10 - 01:20
		status.setValuesSuccessfulInvocation(myFmt.parse("01:10"));
		status.setValuesSuccessfulInvocation(myFmt.parse("01:20"));
		
		assertFalse(status.doesRangeOverlapWithAnyOfMyRanges(myFmt.parse("01:01"), myFmt.parse("01:02")));
		assertTrue(status.doesRangeOverlapWithAnyOfMyRanges(myFmt.parse("01:01"), myFmt.parse("01:12")));
		assertTrue(status.doesRangeOverlapWithAnyOfMyRanges(myFmt.parse("01:11"), myFmt.parse("01:12")));
		assertTrue(status.doesRangeOverlapWithAnyOfMyRanges(myFmt.parse("01:11"), myFmt.parse("01:22")));
		assertFalse(status.doesRangeOverlapWithAnyOfMyRanges(myFmt.parse("01:21"), myFmt.parse("01:22")));
		assertTrue(status.doesRangeOverlapWithAnyOfMyRanges(myFmt.parse("01:01"), myFmt.parse("01:31")));
	}

}
