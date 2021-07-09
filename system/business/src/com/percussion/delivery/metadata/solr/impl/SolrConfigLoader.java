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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.metadata.solr.impl;

import com.percussion.server.PSServer;
import com.percussion.share.dao.PSSerializerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SolrConfigLoader
{
   /**
    * Logger for this class
    */
   public static final Logger log = LogManager.getLogger(SolrConfigLoader.class);

   /**
    * The configuration file path, never <code>null</code>.
    */
   
   private static File CONFIG_FILE = new File(PSServer.getRxDir(),
           "rxconfig/DeliveryServer/solr-servers.xml");
   
   public static PSSolrConfig getDeliveryServerConfig()
   {
       if (!CONFIG_FILE.exists())
          return new PSSolrConfig();
       



       try(InputStream in = new FileInputStream(CONFIG_FILE)){
           PSSolrConfig config = PSSerializerUtils.unmarshalWithValidation(in, PSSolrConfig.class);
           return config;
       }
       catch (Exception e)
       {
          String msg = "Unknown Exception";
          Throwable cause = e.getCause();
          if(cause != null && StringUtils.isNotBlank(cause.getLocalizedMessage()))
          {
             msg = cause.getLocalizedMessage();
          }
          else if(StringUtils.isNotBlank(e.getLocalizedMessage()))
          {
             msg = e.getLocalizedMessage();
          }
          log.error("Error getting solrConfig servers from configuration file: " +  msg,e);
          return new PSSolrConfig();
       }

   }

   public static String toXml(PSSolrConfig config)
   {
      return PSSerializerUtils.marshal(config); 
   }
 
   public static void saveSolrConfig(PSSolrConfig config)
   {

      try(FileOutputStream out = new  FileOutputStream(CONFIG_FILE)){
         IOUtils.write(toXml(config), out);
      }
      catch (IOException e)
      {
         log.error("Cannot save solr configuration to :"+CONFIG_FILE,e);
      }

   }
   
}
