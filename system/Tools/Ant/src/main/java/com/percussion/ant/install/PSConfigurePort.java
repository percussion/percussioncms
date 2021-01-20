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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.util.List;


/**
 * PSConfigurePort is a task which configures the rhythmyx port.
 *
 * The port information is configured in server.xml of the jboss-web.deployer.
 *
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="configurePort"
 *              class="com.percussion.ant.install.PSConfigurePort"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to configure the default Rhythmyx port.
 *
 *  <code>
 *  &lt;configurePort serverPort="9992"/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSConfigurePort extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      PSLogger.logInfo("Configuring port...");
      String port = RX_PORT;

      try
      {
         if (m_strServerPort.trim().length() == 0)
         {
            try {
               //Load server information from server.properties
               PSProperties props = new PSProperties(getRootDir()
                       + File.separator + getServerPropsLocation());

               //Get port
               port = props.getProperty(SERVER_PORT);
            }catch(Exception e){
               PSLogger.logWarn("Unable to locate port in: " + getRootDir()
                       + File.separator + getServerPropsLocation());
               port = getServerPort();
            }
         }
         else
         {
            //Use port property from IDE
            port = getServerPort();
         }

         PSLogger.logInfo("Rhythmyx port will be " + port);

          BaseContainerUtils utils = PSContainerUtilsFactory.getInstance();
         List<IPSConnector> connectors = utils.getConnectorInfo().getConnectors();

         for (IPSConnector connector : connectors)
         {
            int portNum = connector.getPort();
            int jBossPort = Integer.parseInt(RX_PORT_JBOSS);
            if (portNum == jBossPort)
            {
               connector.setPort(Integer.parseInt(port));
               break;
            }
         }

         utils.getConnectorInfo().setConnectors(connectors);

         PSContainerUtilsFactory.getConfigurationContextInstance().save();
      }
      catch (Exception e)
      {
         throw new BuildException(e.getMessage());
      }
   }

   /*************************************************************************
    * Property Accessors and Mutators
    *************************************************************************/

   /**
    * Accessor for the server properties location
    */
   public static String getServerPropsLocation()
   {
      return ms_strServerPropsLocation;
   }

   /**
    * Accessor for the server port
    */
   public String getServerPort()
   {
      if(m_strServerPort==null || m_strServerPort.trim().equals("")) {
        m_strServerPort="9992";
      }
      return m_strServerPort;
   }

   /**
    * Mutator for the server port.
    */
   public void setServerPort(String strServerPort)
   {
      m_strServerPort = strServerPort;
   }


   /**************************************************************************
    * Properties
    *************************************************************************/

   /**
    * The server properties location, relative to the Rhythmyx root.
    */
   private static String ms_strServerPropsLocation =
      "rxconfig/Server/server.properties";

   /**
    * The server port.
    */
   private String m_strServerPort = "";

   /**
    * The default rhythmyx port in jboss
    */
   static private final String RX_PORT_JBOSS = "8080";

   /**
    * The default rhythmyx port
    */
   static private final String RX_PORT = "9992";

   /**
    * The port property name in server.properties
    */
   static protected final String SERVER_PORT = "bindPort";


}
