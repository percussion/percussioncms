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
