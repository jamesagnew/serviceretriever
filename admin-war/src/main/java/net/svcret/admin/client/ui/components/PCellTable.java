package net.svcret.admin.client.ui.components;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.CellTable;

public class PCellTable<T> extends CellTable<T> {

	public PCellTable() {
		super(15, createResources());
		
//		setRowStyles(new RowStyles<T>() {
//			@Override
//			public String getStyleNames(Object theRow, int theRowIndex) {
//				if (theRowIndex % 2 == 0) {
//					return CssConstants.PROPERTY_TABLE_EVEN_ROW;
//				}
//				return CssConstants.PROPERTY_TABLE_ODD_ROW;
//			}
//		});
	}
	
	
	private static com.google.gwt.user.cellview.client.CellTable.Resources createResources() {
		return GWT.create(CellTableResource.class);
	}


	public interface CellTableResource extends CellTable.Resources {

		@Override
		@Source({CellTable.Style.DEFAULT_CSS, "net/svcret/admin/client/PCellTable.css"})
		CellTableStyle cellTableStyle();
		
	    /**
	     * The Interface CellTableStyle.
	     */
	    public interface CellTableStyle extends CellTable.Style {
	    };

	};
	
	
}
