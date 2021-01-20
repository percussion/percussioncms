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
package com.percussion.deployer.install;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.install.IPSUpgradeModule;
import com.percussion.install.IPSUpgradePlugin;
import com.percussion.install.PSPluginResponse;
import com.percussion.install.PSUpgradePluginRelationship;
import com.percussion.install.RxUpgrade;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.util.IOTools;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Updates the package element version information for each of the system
 * relationships.
 */
public class PSUpgradePluginRelationshipVersions implements IPSUpgradePlugin
{
   /**
    * Perform updates.
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      int respType = PSPluginResponse.SUCCESS;
      String respMsg = "";
      Connection conn = null;
      PrintStream logger = config.getLogStream();
      
      try
      {
         IPSPkgInfoService pkgInfoSvc = 
            PSPkgInfoServiceLocator.getPkgInfoService();
         
         conn = RxUpgrade.getJdbcConnection();
             
         PSUpgradePluginRelationship relPlugin = 
            new PSUpgradePluginRelationship();
         relPlugin.setDbProperties(RxUpgrade.getRxRepositoryProps());
         
         Document cfgDoc = relPlugin.getRelationshipConfigs(logger, conn);
         PSRelationshipConfigSet cfgSet = relPlugin.getConfigSet(cfgDoc);
         for (int i = 0; i < cfgSet.size(); i++)
         {
            PSRelationshipConfig relConfig = 
               (PSRelationshipConfig) cfgSet.get(i);
            if (relConfig.isSystem())
            {
               // load version (checksum)
               Document doc = PSXmlDocumentBuilder.createXmlDocument();
               long version = IOTools.getChecksum(PSXmlDocumentBuilder.toString(
                     relConfig.toXml(doc)));

               // find the package element
               String msg;
               String relName = relConfig.getName();
               IPSGuid relGuid = PSIdNameHelper.getGuid(relName,
                     PSTypeEnum.RELATIONSHIP_CONFIGNAME);
               PSPkgElement pkgElem = pkgInfoSvc.findPkgElementByObject(
                     relGuid);
               if (pkgElem != null)
               {
                  // update package element version
                  pkgElem = pkgInfoSvc.loadPkgElementModifiable(
                        pkgElem.getGuid());
                  pkgElem.setVersion(version);
                  pkgInfoSvc.savePkgElement(pkgElem);
                  
                  msg = "Updated package element version for system "
                     + "relationship '" + relName + "'";
               }
               else
               {
                  msg = "Could not find package element for system "
                     + "relationship '" + relName + "'";
               }
               
               logger.println(msg);
            }
         }
      }
      catch (Exception e)
      {
         respType = PSPluginResponse.EXCEPTION;
         respMsg = e.getMessage();
         e.printStackTrace(logger);
      }
      finally
      {
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               e.printStackTrace(logger);
            }
         }
      }

      return new PSPluginResponse(respType, respMsg);
   }
}
