package net.svcret.admin.client.ui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A {@link Cell} used to render a drop-down list.
 */
public class PSelectionCell extends AbstractInputCell<String, String> {

  interface Template extends SafeHtmlTemplates {
    @Template("<option value=\"{0}\">{1}</option>")
    SafeHtml deselected(String optionValue, String optionText);

    @Template("<option value=\"{0}\" selected=\"selected\">{1}</option>")
    SafeHtml selected(String option, String optionText);
  }

  private static Template template;

  private HashMap<String, Integer> indexForOptionValue = new HashMap<String, Integer>();
  private final List<String> optionValues;
  private final List<String> optionTexts;
  
  /**
   * Construct a new {@link SelectionCell} with the specified options.
   *
   * @param options the options in the cell
   */
  public PSelectionCell(List<String> optionValues, List<String> optionTexts) {
    super(BrowserEvents.CHANGE);
    if (template == null) {
      template = GWT.create(Template.class);
    }
    this.optionValues = new ArrayList<String>(optionValues);
    this.optionTexts = new ArrayList<String>(optionTexts);
    
    int index = 0;
    for (String option : optionValues) {
      indexForOptionValue.put(option, index++);
    }
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, String value,
      NativeEvent event, ValueUpdater<String> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    String type = event.getType();
    if (BrowserEvents.CHANGE.equals(type)) {
      Object key = context.getKey();
      SelectElement select = parent.getFirstChild().cast();
      String newValue = optionValues.get(select.getSelectedIndex());
      setViewData(key, newValue);
      finishEditing(parent, newValue, key, valueUpdater);
      if (valueUpdater != null) {
        valueUpdater.update(newValue);
      }
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    // Get the view data.
    Object key = context.getKey();
    String viewData = getViewData(key);
    if (viewData != null && viewData.equals(value)) {
      clearViewData(key);
      viewData = null;
    }

    int selectedIndex = getSelectedIndex(viewData == null ? value : viewData);
    sb.appendHtmlConstant("<select tabindex=\"-1\">");
    int index = 0;
    for (String option : optionValues) {
    	String optionText = optionTexts.get(index);
      if (index++ == selectedIndex) {
		sb.append(template.selected(option, optionText));
      } else {
        sb.append(template.deselected(option, optionText));
      }
    }
    sb.appendHtmlConstant("</select>");
  }

  private int getSelectedIndex(String value) {
    Integer index = indexForOptionValue.get(value);
    if (index == null) {
      return -1;
    }
    return index.intValue();
  }
}
