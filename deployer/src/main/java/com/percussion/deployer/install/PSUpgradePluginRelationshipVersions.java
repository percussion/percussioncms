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
package com.percussion.deployer.install;

import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.error.PSExceptionUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

   private static final Logger log = LogManager.getLogger(PSUpgradePluginRelationshipVersions.class);

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
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         log.debug(logger);
         log.error(logger);
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
               log.error(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
               log.debug(logger);
               log.error(logger);
            }
         }
      }

      return new PSPluginResponse(respType, respMsg);
   }
}
