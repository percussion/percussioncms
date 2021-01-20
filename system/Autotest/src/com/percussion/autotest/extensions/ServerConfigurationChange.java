/*[ ServerConfigurationChange.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.extensions;

import com.percussion.autotest.empire.IPSAutoTestJavaExtension;
import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSServerConfiguration;

import java.util.Properties;

public class ServerConfigurationChange implements IPSAutoTestJavaExtension
{
   public void runExtensionAction(org.w3c.dom.Element info)
      throws Exception
   {
      String changeType = info.getAttribute("settingType");
      if ((null!= changeType) && (changeType.equals("exitSecurity")))
      {
         String hostName = info.getAttribute("hostName");
         String port = info.getAttribute("port");
         String loginId = info.getAttribute("loginId");
         String loginPw = info.getAttribute("loginPw");
         boolean secure = true;

         // port comes through as ":port-number"
         port = port.substring(1);

         // host comes through as "http://host-name-or-number"
         hostName = hostName.substring(7);

         String newSetting = info.getAttribute("sandboxSecurity");
         if ((null != newSetting) && newSetting.equalsIgnoreCase("off"))
         {
            secure = false;
         }
         setSandboxSecurity(hostName, port, loginId, loginPw, secure);
      }
   }

   private void setSandboxSecurity( String host, String port,
                                    String id, String pw, boolean setOn)
      throws Exception
   {
      // Need to open up a PSDesignerConnection to the server
      // and get a PSObjectStore with which to modify the
      // server configuration
      Properties connProps = new Properties();

      connProps.setProperty("hostName", host);
      connProps.setProperty("port", port);
      connProps.setProperty("loginId", id);
      connProps.setProperty("loginPw", pw);

      PSDesignerConnection conn = new PSDesignerConnection(connProps);
      PSObjectStore objStore = new PSObjectStore(conn);

      // Get the server configuration and change it, if necessary.
      PSServerConfiguration cfg = objStore.getServerConfiguration(true, true);
      if ((cfg != null) && (cfg.getUseSandboxSecurity() != setOn))
      {
         cfg.setUseSandboxSecurity(setOn);
         objStore.saveServerConfiguration(cfg, true);
      } else
      {
         objStore.releaseServerConfigurationLock(cfg);
      }
   }
}
