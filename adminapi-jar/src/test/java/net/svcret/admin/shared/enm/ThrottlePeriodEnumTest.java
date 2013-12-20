package net.svcret.admin.shared.enm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThrottlePeriodEnumTest {

	@Test
	public void testIntervalToRequestsPerSecond() {
		
		assertEquals(1.0, ThrottlePeriodEnum.SECOND.intervalToRequestsPerSecond(1), 0.0001);
		assertEquals(0.5, ThrottlePeriodEnum.SECOND.intervalToRequestsPerSecond(2), 0.0001);
		assertEquals(0.25, ThrottlePeriodEnum.SECOND.intervalToRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / 60.0, ThrottlePeriodEnum.MINUTE.intervalToRequestsPerSecond(1), 0.0001);
		assertEquals(0.5 / 60.0, ThrottlePeriodEnum.MINUTE.intervalToRequestsPerSecond(2), 0.0001);
		assertEquals(0.25 / 60.0, ThrottlePeriodEnum.MINUTE.intervalToRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.intervalToRequestsPerSecond(1), 0.0001);
		assertEquals(0.5 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.intervalToRequestsPerSecond(2), 0.0001);
		assertEquals(0.25 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.intervalToRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.intervalToRequestsPerSecond(1), 0.0001);
		assertEquals(0.5 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.intervalToRequestsPerSecond(2), 0.0001);
		assertEquals(0.25 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.intervalToRequestsPerSecond(4), 0.0001);

	}

	@Test
	public void testNumRequestsToRequestsPerSecond() {
		
		assertEquals(1.0, ThrottlePeriodEnum.SECOND.numRequestsToRequestsPerSecond(1), 0.0001);
		assertEquals(2.0, ThrottlePeriodEnum.SECOND.numRequestsToRequestsPerSecond(2), 0.0001);
		assertEquals(4.0, ThrottlePeriodEnum.SECOND.numRequestsToRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / 60.0, ThrottlePeriodEnum.MINUTE.numRequestsToRequestsPerSecond(1), 0.0001);
		assertEquals(2.0 / 60.0, ThrottlePeriodEnum.MINUTE.numRequestsToRequestsPerSecond(2), 0.0001);
		assertEquals(4.0 / 60.0, ThrottlePeriodEnum.MINUTE.numRequestsToRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.numRequestsToRequestsPerSecond(1), 0.0001);
		assertEquals(2.0 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.numRequestsToRequestsPerSecond(2), 0.0001);
		assertEquals(4.0 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.numRequestsToRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.numRequestsToRequestsPerSecond(1), 0.0001);
		assertEquals(2.0 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.numRequestsToRequestsPerSecond(2), 0.0001);
		assertEquals(4.0 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.numRequestsToRequestsPerSecond(4), 0.0001);

	}

}
