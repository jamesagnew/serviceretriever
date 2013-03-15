package ca.uhn.sail.proxy.admin.shared.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class StatusEnumTest {

	@Test
	public void testEntries() {
		
		List<String> expected = names(ca.uhn.sail.proxy.model.entity.PersServiceVersionUrlStatus.StatusEnum.values());
		List<String> actual = names(StatusEnum.values());
		
		assertEquals(expected, actual);
	}

	private List<String> names(Enum<?>[] theValues) {
		ArrayList<String> retVal = new ArrayList<String>();
		for (Enum<?> enum1 : theValues) {
			retVal.add(enum1.name());
		}
		java.util.Collections.sort(retVal);
		return retVal;
	}
	
}
