package com.percussion.cx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.Desktop;
import java.net.URI;

public class PSHyperlinkListener implements ChangeListener<State>, EventListener {
	private static Logger log = Logger.getLogger(PSHyperlinkListener.class);
	
	private static final String CLICK_EVENT = "click";
	private static final String ANCHOR_TAG = "a";
	
	private final WebView webView;
	
	public PSHyperlinkListener(WebView webView) {
	    this.webView = webView;
	}
	
	@Override
	public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
	    if (State.SUCCEEDED.equals(newValue)) {
	        Document document = webView.getEngine().getDocument();
	        NodeList anchors = document.getElementsByTagName(ANCHOR_TAG);
	        for (int i = 0; i < anchors.getLength(); i++) {
	            Node node = anchors.item(i);
	            EventTarget eventTarget = (EventTarget) node;
	            eventTarget.addEventListener(CLICK_EVENT, this, false);
	        }
	    }
	}
	
	@Override
	public void handleEvent(Event event) {
	    HTMLAnchorElement anchorElement = (HTMLAnchorElement)event.getCurrentTarget();
	    openLinkInBrowser(anchorElement.getHref());
	
	    event.preventDefault();
	}
	
	private void openLinkInSystemBrowser(String url) {
	    log.debug(String.format("Opening link '{0}' in default system browser.", url));
	
	    try {
	        URI uri = new URI(url);
	        Desktop.getDesktop().browse(uri);
	    } catch (Throwable e) {
	        log.error(String.format("Error on opening link '{0}' in system browser.", url),e);
	    }
	}
	
	private void openLinkInBrowser(String href) {
		if (Desktop.isDesktopSupported()) {
	        openLinkInSystemBrowser(href);
	    } else {
	        log.warn(String.format("OS does not support desktop operations like browsing. Cannot open link '{0}'.", href));
	    }
	}
}