package net.svcret.admin.shared.enm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ThrottlePeriodEnumTest {

	@Test
	public void testToRequestsPerSecond() {
		
		assertEquals(1.0, ThrottlePeriodEnum.SECOND.toRequestsPerSecond(1), 0.0001);
		assertEquals(0.5, ThrottlePeriodEnum.SECOND.toRequestsPerSecond(2), 0.0001);
		assertEquals(0.25, ThrottlePeriodEnum.SECOND.toRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / 60.0, ThrottlePeriodEnum.MINUTE.toRequestsPerSecond(1), 0.0001);
		assertEquals(0.5 / 60.0, ThrottlePeriodEnum.MINUTE.toRequestsPerSecond(2), 0.0001);
		assertEquals(0.25 / 60.0, ThrottlePeriodEnum.MINUTE.toRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.toRequestsPerSecond(1), 0.0001);
		assertEquals(0.5 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.toRequestsPerSecond(2), 0.0001);
		assertEquals(0.25 / (60.0 * 60.0), ThrottlePeriodEnum.HOUR.toRequestsPerSecond(4), 0.0001);

		assertEquals(1.0 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.toRequestsPerSecond(1), 0.0001);
		assertEquals(0.5 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.toRequestsPerSecond(2), 0.0001);
		assertEquals(0.25 / (60.0 * 60.0 * 24.0), ThrottlePeriodEnum.DAY.toRequestsPerSecond(4), 0.0001);

	}
	
}
