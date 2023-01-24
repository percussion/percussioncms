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

package com.percussion.cx;

import com.percussion.util.PSHttpConnection;
import com.percussion.webservices.faults.PSNotAuthenticatedFault;
import com.percussion.webservices.security.LoginRequest;
import com.percussion.webservices.security.LoginResponse;
import com.percussion.webservices.security.SecuritySOAPStub;
import com.percussion.webservices.security.data.PSLogin;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Window;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PSCESessionManager implements Runnable
{
   private static final String LOGOUT_NOW = "Logout Now";

   private static final String EXTEND_SESSION = "Extend Session";

   static Logger log = Logger.getLogger(PSCESessionManager.class);

   private ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

   private JDialog dialog = null;

   private JLabel timeLabel = new JLabel("00:00:00");

   private long expireTime = 0;

   private long warningTime = 0;

   private  long lastCheckTime = 0;

   private ScheduledFuture<?> countdown;

   private PSLogin loginInfo = null;

   private boolean extendSession = false;
   
   private AtomicBoolean dialogOn = new AtomicBoolean(false);

   private volatile static PSCESessionManager instance = null;

   private String protocol;

   private String server;

   private String port;

   private boolean isLoggedIn = false;

   private String userName = "unknown";
   


 
   public PSCESessionManager()
   { 
       timeLabel.setName("timeRemaining");
   }

   public void start(String protocol, String server, String port, PSLogin loginInfo)
   {
    
      this.protocol = protocol;
      this.server = server;
      this.port = port;
      this.loginInfo = loginInfo;
      ex = Executors.newSingleThreadScheduledExecutor();
      ex.schedule(this, 0, TimeUnit.MILLISECONDS);
      
   }
   
   @Override
   public void run()
   {
      try
      {
         PSContentExplorerApplet applet = PSContentExplorerApplication.getApplet();
         
         long expiry = 0;
         long warning = 0;
         try
         {
            String resource = "/Rhythmyx/sessioncheck";
            if (extendSession)
            {
               log.debug("Extending session");
               resource += "?extendSession=true";
               extendSession = false;
            }
            
            URL url =   new URL(protocol, server,
                  Integer.parseInt(port), resource);
            
            PSHttpConnection httpConnection = new PSHttpConnection(new URL(protocol, server,
                  Integer.parseInt(port),"/Rhythmyx/sys_resources"), loginInfo.getSessionId());
            JSONObject sessionObject = httpConnection.getJSON(url);

            expiry = sessionObject.getLong("expiry");
            warning = sessionObject.getLong("warning");
            lastCheckTime = System.currentTimeMillis();
            expireTime = lastCheckTime + expiry;
            warningTime = expireTime - warning;

         }
         catch (Exception e)
         {
            // Assume that countdown is continuing
            log.warn("Failed to check session state server may be down or network connection lost :" + e.getLocalizedMessage());
            log.debug("Failed to check session state server may be down or network connection lost:" + e.getLocalizedMessage(), e);
            long currTime = System.currentTimeMillis();
            expiry = expireTime - currTime;
            warning = warningTime - currTime;
         }

         log.debug("session status check: expiry=" + expiry + " warning=" + warning);

         if (expiry <= 0)
         {
            if (isLoggedIn)
            {
               if (dialog != null)
               {
                  dialog.dispose();
               }
               PSContentExplorerApplication.getBaseFrame().logout();
               shutdown();
            }
         }
         else if (warning > 0 && expiry <= warning)
         {

            if (dialogOn.compareAndSet(false, true))
            {
               Object[] options =
               { applet.getResourceString(getClass(), 
                     EXTEND_SESSION), applet.getResourceString(getClass(), 
                           LOGOUT_NOW)};

               SwingUtilities.invokeLater(() -> {
                  if (!ex.isShutdown())
                     countdown = ex.scheduleAtFixedRate(new Countdown(), 0, 1, TimeUnit.SECONDS);

                  Object[] message =
                  { applet.getResourceString(getClass(), 
                        "You will be automatically logged out in\n"), timeLabel};
                  JOptionPane optionPane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_NO_OPTION, null, options, options[0]);

                  dialog = optionPane.createDialog(applet.getDialogParentFrame(),  applet.getResourceString(getClass(), 
                        "Are you still there?"));
                  dialog.pack();
                  dialog.setVisible(true);

                  countdown.cancel(false);
                  if(optionPane.getValue() == null){
                     log.debug("Close / Cross dialog button clicked. ");
                     if (countdown != null) {
                        countdown.cancel(false);
                     }

                     if (dialog != null && dialog.isVisible())
                     {
                        dialog.dispose();
                     }
                     return;
                  }
                  if (optionPane.getValue() != null && optionPane.getValue().equals(options[1]))
                  {
                     PSContentExplorerApplication.getBaseFrame().logout();
                     isLoggedIn=false;
                     shutdown();
                     return;
                  }
                  else if (optionPane.getValue() != null && optionPane.getValue().equals(options[0]))
                  {
                     log.debug("Setting extendSession");
                     extendSession = true;
                     if (!ex.isShutdown())
                        ex.schedule(this, 0, TimeUnit.MILLISECONDS);
                     return;
                  }
               });
               // take the chance to save the applet state while there is no
               // activity
               // need to make sure that saving the state does not reset session
               // time
               if (applet.getOptionsManager() != null)
               {
                  applet.saveAppletState();
                  applet.getOptionsManager().save(true);

               }
            }
            if (!ex.isShutdown())
               ex.schedule(this, expiry, TimeUnit.MILLISECONDS);
               log.debug("Waiting for logout in " + expiry + "ms");

         }
         else
         {
            if (dialogOn.compareAndSet(true, false))
            {
               log.debug("Turning off warning");
               SwingUtilities.invokeLater(() -> {
                  // stop countdown timer
                     if (countdown != null)
                        countdown.cancel(false);
                     if (dialog != null && dialog.isVisible())
                     {
                        dialog.dispose();
                     }

                  });
            }
            if (!ex.isShutdown())
               ex.schedule(this, (expiry - warning), TimeUnit.MILLISECONDS);
            log.debug("Waiting for warning in " + (expiry - warning) + "ms");
         }

      }
      catch (Exception e)
      {

         log.error("Error with session check thread", e);
      }

   }

   public void shutdown()
   {
      SwingUtilities.invokeLater(() -> {
         if (!ex.isShutdown())
         {
            if (dialog != null && dialog.isVisible())
            {
               dialog.dispose();
            }
            ex.shutdown();
         }
      });
   }

   private class Countdown implements Runnable
   {
      @Override
      public void run()
      {
         long remainTime = expireTime - System.currentTimeMillis();
         SwingUtilities.invokeLater(() -> {
            String timeText = DurationFormatUtils.formatDuration(remainTime, "HH:mm:ss");
            log.debug("Remaing Time : " + remainTime + " -- Time Text : " + timeText);
            timeLabel.setText(timeText);
            if(remainTime<=0 || DurationFormatUtils.formatDuration(remainTime, "HH:mm:ss").equalsIgnoreCase("00:00:00")){
               log.debug("Stopping timer and logging out after timeout.");
               if (countdown != null){
                  countdown.cancel(false);
                  log.debug("Timer stopped.");
               }

               if (dialog != null && dialog.isVisible())
               {
                  dialog.dispose();
               }
               closeAllDialog();
               PSContentExplorerApplication.getBaseFrame().logout();
               isLoggedIn=false;
               shutdown();
               return;
            }
         });
      }

   }

   private void closeAllDialog(){
      Window[] windows = Window.getWindows();
      for (Window window : windows) {
         if (window instanceof JDialog) {
            JDialog dialog = (JDialog) window;
            if (dialog.getContentPane().getComponents().length>0 && dialog.getContentPane().getComponent(0) instanceof JOptionPane){
               dialog.dispose();
            }
         }
      }
   }

   public void stop()
   {
      ex.shutdown();
   }
   
   
   public void login(String protocol, String server, String port, String uid, String password, String locale) throws Exception
   {
      Integer timeoutInSec = 30;
      try
      {
         // Clear all cookies that may have been left over from previouse
         CookieManager manager = new CookieManager();
         manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
         CookieHandler.setDefault(manager);
         
         PSWsUtils.setConnectionInfo(protocol, server, Integer.parseInt(port));
         
         SecuritySOAPStub securitySession = PSWsUtils.getSecurityService();
         // convert timeout to milliseconds
         securitySession.setTimeout(timeoutInSec * 1000);
         LoginRequest logRequest = new LoginRequest(uid,password,
              null, null, locale);

         // convert time from seconds to milliseconds
         log.debug("Logging in ...");
         LoginResponse resp = securitySession.login(logRequest);
         log.debug("Got login response");
         PSLogin login = resp.getPSLogin();
         String session = login.getSessionId();
         PSWsUtils.setRxSessionHeader(securitySession, session);
         
         if (login.getCommunities().length>0)
         {
            log.info("logged in using community "+login.getDefaultCommunity()+ " locale = "+login.getDefaultLocaleCode());
            log.info("session="+login.getSessionId());
           
           
         }
         
         // Replicated javascript session check
         // hold the resulting login info in loginInfo
         
         this.userName = uid;
         
         this.start(protocol,server,port,login);
         
         isLoggedIn = true;
         
         return;

      }
      catch (PSNotAuthenticatedFault e)
      {
         if (log.isDebugEnabled())
             log.debug("Failed to login.  resetting applet",e);
          else
             log.info("Header login failed.  Failed to authenticate with username and password. "+ e.getErrorMessage());
          PSContentExplorerApplication.getBaseFrame().logout();
          isLoggedIn=false;
          shutdown();
          throw new RuntimeException("Authentication failed. Invalid User Name and/or Password.");
      }
      catch (Exception e)
      {
         if (log.isDebugEnabled())
            log.debug("Failed to login.  resetting applet",e);
         else
            log.info("Header login failed.  Session may have expired or connection lost "+ e.getMessage());
         PSContentExplorerApplication.getBaseFrame().logout();
         isLoggedIn=false;
         shutdown();
         throw new RuntimeException("Cannot connect to server or timed out");
      }
 
   }
   

   public static PSCESessionManager getInstance()
   {
      if (instance==null)
      {
         instance = new PSCESessionManager();
      }
      return instance;
   }
   
   public PSLogin getLoginInfo()
   {
      return loginInfo;
   }

   public boolean isLoggedIn()
   {
      return isLoggedIn;
   }
   
   public void setLoggedIn(boolean isLoggedIn)
   {
      this.isLoggedIn = isLoggedIn;
   }
   
   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }


}
