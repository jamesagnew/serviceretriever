package net.svcret.admin.client.ui.config.svcver;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

public final class NullColumn<T> extends Column<T, String> {
	public NullColumn(Cell<String> theCell) {
		super(theCell);
	}

	@Override
	public String getValue(Object theObject) {
		return null;
	}
}