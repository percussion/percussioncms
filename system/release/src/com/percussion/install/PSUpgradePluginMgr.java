/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.install;

import org.w3c.dom.Element;

/*
 * This is the main class for processing the plugins.
 *
 */
public class PSUpgradePluginMgr
{
   /*
    * Constructor, creates this class object
    *
    * @param config, the upgrade module
    * May not be <code>null</code>.
    *
    * @param elemPlugin, plugin Element from the config module
    * May not be <code>null</code>.
    *
    */
   public PSUpgradePluginMgr(IPSUpgradeModule config, Element elemPlugin)
   {
      if(config == null)
      {
          throw new IllegalArgumentException("config may not be null.");
      }
      if(elemPlugin == null)
      {
         throw new IllegalArgumentException("elemPlugin may not be null.");
      }

      m_name = elemPlugin.getAttribute(IPSUpgradeModule.ATTR_NAME);
      m_class = InstallUtil.getElemValue(elemPlugin,
         IPSUpgradeModule.ELEM_CLASS);
      m_elemData = InstallUtil.getElement(elemPlugin,
         IPSUpgradeModule.ELEM_DATA);
      m_config = config;
   }

   /**
    * Calls the process method of appropriate plugin.  Checks the value returned.
    * If it is null, then it functions as today, otherwise, the value is added
    * to the RxISCompScanPanel's response object store.
    */
   public void execute()
   {
      try
      {
         IPSUpgradePlugin plugin = null;
         PSPluginResponse response = null;
         Object obj = Class.forName(m_class).newInstance();
         if(obj instanceof IPSUpgradePlugin)
         {
            plugin = (IPSUpgradePlugin)obj;
            response = plugin.process(m_config, m_elemData);
            
            if (RxUpgrade.ms_bPreUpgrade)
            {
               if (response != null)
                  RxUpgrade.addResponse(response);
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Name of the plugin assigned in constructor from config file.
    */
   String m_name = "";
   /**
    * Class Name of the plugin assigned in constructor from config file.
    */
   String m_class = "";
   /**
    * plugin element assigned in constructor from config file.
    */
   Element m_elemData = null;
   /**
    * Instance of upgrade module.
    */
   IPSUpgradeModule m_config = null;
   
}
