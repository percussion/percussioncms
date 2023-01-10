package com.percussion.cx;


import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSUserInfo;
import com.percussion.cx.javafx.PSDesktopExplorerWindow;
import com.percussion.cx.objectstore.PSMenuAction;
import com.percussion.guitools.PSDialog;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSLogin;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PSContentExplorerFrame extends PSDesktopExplorerWindow implements AppletStub, AppletContext
{

   private static final long serialVersionUID = 1L;

   static Logger log = Logger.getLogger(PSContentExplorerFrame.class);
  
   private PSContentExplorerHelper helper = new PSContentExplorerHelper();

   /**
    * hashmap containing all parameters needed for the applet
    */

   Map<String, String> parameters = new HashMap<String, String>();

   /**
    * the login panel, containing all GUI elements for the login dialog
    */
   private PSContentExplorerLoginPanel loginPanel = null;
   
   private URI serverUri = null;
   
   private boolean fixedHost = false;

   private PSUserInfo userInfo = null;
    
   /**
    * The constuctor sets the applications main frame size and title. It adds a
    * new window listener to watch for closing events. Then it performs the same
    * initialization as a browser.
    * @param uri
    */
   public PSContentExplorerFrame(URI uri)
   {

      super();
      
      serverUri = uri;
      
      // root ContentExplorer URI
      try
      {
         log.debug("uri="+uri.toString());
         mi_actionurl = new URI(uri.getScheme(),uri.getHost(),"/Rhythmyx/sys_cx/mainpage.html",null).toString();
      }
      catch (URISyntaxException e)
      {
          throw new IllegalArgumentException("Invalid root server URI",e);
      }
      
      // focus should be set on this window when refreshing to allow screen reader to read
      this.setAutoRequestFocus(true);
      
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            PSContentExplorerFrame.this.setResizable(true);
            if (uri.getScheme() != null && uri.getHost() !=null)
            {
               PSContentExplorerFrame.this.setParameter("serverName", uri.getHost());
               PSContentExplorerFrame.this.setParameter("protocol", uri.getScheme());
               int port = uri.getPort();
               if (port==-1)
                  port = ("https".equals(uri.getScheme())) ? 443:80;
               PSContentExplorerFrame.this.setParameter("port", String.valueOf(port));
            }
            
            PSContentExplorerFrame.this.setTitle(getCETitle());
            
            // this get's moved to the action listener of the login
            // applet.init();
            // applet.start();
            ImageIcon icon = PSContentExplorerHelper.getImageIcon(this.getClass());

            if (null != icon)
               PSContentExplorerFrame.this.setIconImage(icon.getImage()); // adding Rhythmyx icon to title
               // bar
             PSContentExplorerFrame.this.pack();
            PSContentExplorerFrame.this.setupAppletAndLogin();
         }
     });
      
      
   }

   private String getCETitle()
   {
     
      String host = this.getParameter("serverName");
      String protocol = this.getParameter("protocol");
      String port = this.getParameter("port");
      String urlString = PSContentExplorerHelper.getResources().getString("contentExplorer") ;
      if (host!=null && protocol !=null)
      {
         URI uri = null;
         if (StringUtils.isEmpty(port))
         {
            if (protocol.equals("https"))
               port = "443";
            else
               port = "80";
         }
   
            try
            {
               uri = new URI(protocol, null, host, Integer.parseInt(port), null,null,null);
            }
            catch (NumberFormatException | URISyntaxException e)
            {
               log.debug("cannot parse uri", e);
            }
            urlString += " - " + uri.toString();
      

      }
     
      return urlString;
   }

   /**
    * 
    */
   private void setupAppletAndLogin()
   {
     
      if (applet == null)
      {
         applet = new PSContentExplorerApplet();
         this.add(applet,  BorderLayout.CENTER);
      }
    
      mergeParams(PSContentExplorerHelper.initializeDefaultParameters());
      
      parameters.put("LABEL", "Desktop Content Explorer");
   
      //this.customizeOuterAppletFrame();
      applet.setStub(this);
      applet.setIsApplication(true);
      
      customizeOuterAppletFrame();
  
  
      // before initializing, show the login panel
      this.showLogin();
   }

   private void customizeOuterAppletFrame()
   {
      this.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            applet.stop();
            applet.destroy();
            Platform.exit();
         }
      });

      this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

      this.pack();
      this.setResizable(true);

   }


   public void mergeParams(Map<String, String> params)
   {
      // merge parameters into default
      if (params.size() > 0)
      {

         for (String key : params.keySet())
         {
            parameters.put(key, params.get(key));
         }
      }

   }
   public void showLogin()
   {

      try
      {
         if (loginPanel == null)
         {
            // create the login panel, which is always the first panel shown
            loginPanel = new PSContentExplorerLoginPanel(this,applet);
           
         }
                
         loginPanel.setAlwaysOnTop(true);
         loginPanel.setVisible(true);
         
         if (PSCESessionManager.getInstance().isLoggedIn())
         {
            initCESession();
         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }

   
  
   @Override
   public WebEngine getEngine()
   {
      return applet.getEngine();
   }
   
   public void initCESession() throws PSCmsException {

   
  
     this.setTitle(getCETitle());
     
     // Clear all cookies that may have been left over from previouse
     CookieManager manager = new CookieManager();
     manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
     CookieHandler.setDefault(manager);

    PSLogin login = PSCESessionManager.getInstance().getLoginInfo();
    PSCommunity[] communities = login.getCommunities();
    long commId = 0;
    String defaultComm = login.getDefaultCommunity();
    for (PSCommunity comm:communities) {
        if(defaultComm.equals(comm.getName())){
            commId = comm.getId();
            break;
        }
    }
    PSUserInfo userInfo = new PSUserInfo(login.getSessionId(),
    PSCESessionManager.getInstance().getUserName(),commId,login.getDefaultLocaleCode(),login.getRoles(),login.getSessionTimeout()
    );


     applet.init();
     applet.setupApplet(userInfo);
     applet.start();
     
     Dimension dim = this.getSize();
     if (dim.width<1000) dim.width = PSContentExplorerConstants.APPLET_WIDTH+20;
     if (dim.height<800) dim.height = PSContentExplorerConstants.APPLET_HEIGHT;
     
     Dimension dim2 = Toolkit.getDefaultToolkit().getScreenSize();
     this.setLocation((dim2.width/2) - (dim.width /2), (dim2.height/2) - (dim.height/2));
     
     
     SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
           PSContentExplorerFrame frame = PSContentExplorerApplication.getBaseFrame();
           frame.setSize(dim);
           //repaint to show all the above changes.
           frame.toFront();
           if (loginPanel!=null)
              loginPanel.dispose();
           loginPanel=null;
           frame.setVisible(true);
        }
     });
     
  }
   
   
   /**
    * Centers the dialog on the screen, based on its current size.
    */
   public void center()
   {
      Rectangle bounds = PSDialog.getScreenBoundsAt(this.getLocation());
      Dimension size = getSize();
      setLocation(bounds.x + (( bounds.width - size.width ) / 2),
           bounds.y + (( bounds.height - size.height ) / 2 ));
   }

   /**
    * Minimal implementation for AppletStub.
    *
    */
   public boolean isActive()
   {
      return false;
   }

   public URL getDocumentBase()
   {
      return null;
   }

   public URL getCodeBase()
   {
      return helper.getCodeBase();
   }

   public String getParameter(String key)
   {

      String returnValue = "";

      returnValue = parameters.get(key);

      return returnValue;
   }

   public void setParameter(String paremeter, String value)
   {
      this.parameters.put(paremeter, value);
   }

   public Map<String, String> getParameters()
   {
      return parameters;
   }

   public void setParameters(Map<String, String> paremeters)
   {
      this.parameters = paremeters;
   }

   public AppletContext getAppletContext()
   {
      return this;
   }

   public void appletResize(int width, int height)
   {
   }

   /**
    * Minimal implementation for AppletContext.
    *
    */
   public AudioClip getAudioClip(URL url)
   {
      return null;
   }

   public Image getImage(URL url)
   {
      return null;
   }

   public Applet getApplet(String name)
   {
      return this.applet;
   }

   public Enumeration getApplets()
   {
      return Collections.enumeration(Collections.singletonList(this.applet));
   }

   public void showDocument(URL url)
   {
   }

   public void showDocument(URL url, String taget)
   {
   }

   public void showStatus(String status)
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.applet.AppletContext#getStream(java.lang.String)
    */
   public InputStream getStream(String key)
   {
      // TODO - Implement for JDK 1.4
      throw new UnsupportedOperationException("This method is not yet implemented");
   }

   @Override
   public void setStream(String key, InputStream stream) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Iterator<String> getStreamKeys()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void reload()
   {
      cleanup();
      if (applet == null)
      {
         applet = new PSContentExplorerApplet();
         this.add(applet,  BorderLayout.CENTER);
      }
      applet.init();
      applet.setupApplet(userInfo);
      applet.start();
      this.toFront();
    
   }

   public void cleanup()
   {
      if (applet != null)
      {
         applet.stop();
         applet.destroy();
      }
   }

   public PSContentExplorerApplet getApplet()
   {
      return this.applet;
   }

   public void logout()
   {
      this.setVisible(false);
      cleanup();
      if (applet!=null)
      {
         PSCESessionManager sessionManager = PSCESessionManager.getInstance();
         sessionManager.shutdown();
         applet.logout();
         sessionManager.setLoggedIn(false);
         this.remove(applet);
         applet=null;
      }

      setupAppletAndLogin();
   }

   @Override
   public boolean validateOpen(String mi_actionurl, String mi_target, String mi_style, PSSelection selection,
         PSMenuAction action)
   {
      return false;
   }

   @Override
   public JFrame instanceOpen()
   {
       return this;
   }

   @Override
   public void reload(Map<String, String> parameters)
   {
      Map<String, String> params = getParameters();
      //Don't use specific item unless when resetting unless specified
      params.remove(PSContentExplorerConstants.PARAM_CONTENTID);
      params.remove(PSContentExplorerConstants.PARAM_REVISIONID);
      
      if (parameters!=null)
         params.putAll(parameters);
      reload();
   }
   
   
}