/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.cx.javafx;

import com.percussion.cx.PSContentExplorerApplet;
import com.percussion.cx.PSJavaBridge;
import com.percussion.cx.PSSelection;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.guitools.PSDialog;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class PSDesktopExplorerWindow extends JFrame
{
   
   static Logger log = Logger.getLogger(PSDesktopExplorerWindow.class);
   
   protected PSJavaBridge bridge = new PSJavaBridge(this);
   
   protected String target;

   protected String parentTarget;
   
   protected PSContentExplorerApplet applet = null;
  
   protected volatile String mi_actionurl;
   protected String mi_style = "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=1,width=780,height=400";
   protected PSSelection selection;
   protected PSMenuAction action;
   protected BrowserProps browserProps;

   protected volatile WebView webView;

   protected WebEngine engine;

   protected boolean windowLoaded = false;

   protected JSObject window = null;
   protected PSDesktopExplorerWindow opener = null;

   private volatile boolean isClosed;

   protected static AtomicBoolean firebug = new AtomicBoolean(false);
   
   private static final PSWindowManager wgmr = PSWindowManager.getInstance();
   
   public PSDesktopExplorerWindow()
   {
      super();
   }
   
   public PSDesktopExplorerWindow(String string)
   {
      super(string);
   }
   
   public JFrame open(String parent, String mi_actionurl, String mi_target, String mi_style,PSSelection selection, PSMenuAction action)
   {
      this.parentTarget = parent;
      this.target = mi_target;
      this.mi_actionurl = mi_actionurl;
      if (StringUtils.isNotEmpty(mi_style))
         this.mi_style = mi_style;
      this.selection = selection;
      this.action = action;
      this.browserProps = new BrowserProps(this.mi_style);
     
      Dimension windowSize = new Dimension(this.browserProps.getWidth(), this.browserProps.getHeight() + 20);
      setPreferredSize(windowSize);
      setSize(windowSize);
      
      PSDesktopExplorerWindow parentWin = getParentDceWindow();
      if (parentWin!=null)
      {
         this.opener = parentWin;
         Point parentLocation = parentWin.getLocation();
         int wdwLeft = (int) parentLocation.getX() + 50;
         int wdwTop = (int) parentLocation.getY() + 50;
         setLocation(wdwLeft, wdwTop);
         
      } 
      else 
      {
         Rectangle bounds = PSDialog.getScreenBoundsAt(this.getLocation());
         Dimension size = getSize();
         setLocation(bounds.x + (( bounds.width - size.width ) / 2),
              bounds.y + (( bounds.height - size.height ) / 2 ));
         
      }
      
      setAutoRequestFocus(true);
      
      JFrame result = instanceOpen();
      windowLoaded = true;
      
      this.setFocusable(true);
      this.requestFocus();
      this.setAutoRequestFocus(true);
      this.addComponentListener(new ComponentAdapter()
      {
         public void componentResized(ComponentEvent evt) {
            Component c = (Component)evt.getSource();
            Rectangle r = PSDesktopExplorerWindow.this.getBounds();
            log.debug("Window "+target+" Resized to " +r.height + ", " + r.width);
            PSWindowManager.getInstance().windowResized(target, r.height, r.width);
        }
      });
      return result;
   }
   public abstract boolean validateOpen(String mi_actionurl, String mi_target, String mi_style,PSSelection selection, PSMenuAction action);

   public abstract JFrame instanceOpen();

   public void managerClose()
   {
      wgmr.close(this.target);
   }

   public String getTarget()
   {
      return this.target;
   }
   
   public void setTarget(String target)
   {
      this.target = target;
   }

   public String getUrl()
   {
      return this.mi_actionurl;
   }

   public PSDesktopExplorerWindow getParentDceWindow()
   {
      return wgmr.getWindow(this.parentTarget);
   }

   public JSObject getJSWindow()
   {
      return (JSObject) getEngine().executeScript("window");
   }

   public String getParentTarget()
   {
      return parentTarget;
   }

   public PSContentExplorerApplet getApplet()
   {
      return applet;
   }
   
   public void reload() {
      return;
   }
   
   public void reload(Map<String,String> params)
   {
      return;
   }

   public void reloadParent(HashMap<String, String> newParams)
   {
      PSDesktopExplorerWindow parentWindow = PSWindowManager.getInstance().getWindow(getParentTarget());
      if (parentWindow!=null)
      {
         SwingUtilities.invokeLater( () ->parentWindow.reload(newParams));
      }
   }


   
   public void closeDceWindow() {
         wgmr.close(target);
   }

   public PSDesktopExplorerWindow openChildWindow(String mi_actionurl2, String mi_target, String mi_style2, PSSelection mi_selection,
         PSMenuAction action2)
   {
       return wgmr.openWithParent(target, mi_actionurl2, mi_target, mi_style2, mi_selection, action2);
   }
   
   public WebEngine getEngine()
   {
      return engine;
   }

   public void setEngine(WebEngine engine)
   {
      this.engine = engine;
   }
   
   protected void setJavaBridge()
   {
    
      JSObject window = getJSWindow();
     

      if (window != null && !window.toString().equals("undefined"))
      {
       
         PSDesktopExplorerWindow parentDce = getParentDceWindow();
         if (parentDce!=null)
         {
            JSObject opener = parentDce.getJSWindow();
            JSObject currentOpener=(JSObject)window.getMember("opener");
            if (currentOpener==null || opener!=currentOpener )
            {   
               window.setMember("opener", opener);
               
            }
            
         }     
         else 
         {
            log.debug("No parent for window " + this.target + "with url "+mi_actionurl);
         }
         
         Object currJava = (Object)window.getMember("java");
         if(currJava==null || currJava.toString().equals("undefined"))
         {
            window.setMember("java", this.bridge);
            if (applet!=null)
               window.setMember("contentexplorer", applet);
            
            getEngine().executeScript("if(typeof perfInserted == 'undefined') {onerror = function(msg,url,line) { java.log(msg +', url: '+url+', line:'+line); };console.log = function(message){ java.log(message); };window.close = function() { return java.closeWindow();};window.open = function(url, name, specs, replace) { win = java.openWindow(url, name, specs , replace); java.log('window open='+win); return win;};percInserted=true;console.log('inserted perc js overrides')}");
           
         }

         Object currUtils = (Object)window.getMember("percUtils");
         if(currUtils==null || currUtils.toString().equals("undefined")){
            window.setMember("percUtils", PSWebViewUtils.getInstance());
         }
         if (firebug.get())
            showFirebug();
 
      }

   }
   
   public void showFirebug(){
      getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
   }
   
   public boolean isClosed()
   {
      return isClosed;
   }

   public void setClosed(boolean isClosed)
   {
      this.isClosed = isClosed;
   }
}
