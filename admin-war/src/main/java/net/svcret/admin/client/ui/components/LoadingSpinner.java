package net.svcret.admin.client.ui.components;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class LoadingSpinner extends FlowPanel {

    private String myText;
    private HTML myLoadingLabel;
    private Image myImage;

    public LoadingSpinner(String theText) {
        setStyleName(MyResources.CSS.spinnerStatusMessageStyle());
        
        myText = theText;
        
        myImage = new Image(AdminPortal.IMAGES.spinner());
        myImage.setStyleName(MyResources.CSS.spinnerElement());
        this.add(myImage);
        
        myLoadingLabel = new HTML();
        myLoadingLabel.setStyleName(MyResources.CSS.spinnerElement());
        this.add(myLoadingLabel);
                
        hide();
    }

    public LoadingSpinner() {
        this("Loading...");
    }

    public void showMessage(String theMessage, boolean theShowSpinner) {
        myLoadingLabel.setHTML(theMessage);
        myImage.setVisible(theShowSpinner);
        this.setVisible(true);
    }
    
    public void show() {
        myLoadingLabel.setHTML(myText);
        myImage.setVisible(true);
        setVisible(true);
    }

    public void hide() {
        myLoadingLabel.setHTML("&nbsp;");
        myImage.setVisible(false);
    }
    
    public void hideCompletely() {
    	setVisible(false);
    }
    
}
