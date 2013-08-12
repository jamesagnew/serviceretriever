package net.svcret.admin.client.ui.components;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that formats its child widgets using the default HTML layout
 * behavior.
 * 
 * <p>
 * <img class='gallery' src='doc-files/FlowPanel.png'/>
 * </p>
 */
public class HtmlNobrPanel extends ComplexPanel implements InsertPanel.ForIsWidget {
  /**
   * Creates an empty flow panel.
   */
  public HtmlNobrPanel() {
    setElement(DOM.createElement("nobr"));
  }

  /**
   * Adds a new child widget to the panel.
   * 
   * @param w the widget to be added
   */
  @Override
  public void add(Widget w) {
    add(w, getElement());
  }

  @Override
public void insert(IsWidget w, int beforeIndex) {
    insert(asWidgetOrNull(w), beforeIndex);
  }

  /**
   * Inserts a widget before the specified index.
   * 
   * @param w the widget to be inserted
   * @param beforeIndex the index before which it will be inserted
   * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
   *           range
   */
  @Override
public void insert(Widget w, int beforeIndex) {
    insert(w, getElement(), beforeIndex, true);
  }
}