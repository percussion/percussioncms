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

package com.percussion.delivery.metadata.solr.impl;

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.util.PSPurgableTempFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PSSolrDeliveryHandler
{
   private static final String ENABLE_CLIENT_SASL_KEY = "zookeeper.sasl.client";

   private static final String LOGIN_CONTEXT_NAME_KEY = "zookeeper.sasl.clientconfig";

   private static String saslConfigName = null;

   /**
    * Logger for this class
    */
   public static Log log = LogFactory.getLog(PSSolrDeliveryHandler.class);

   private boolean fatalError = false;

   private SolrServer serverConfig = null;

   private String serverType = "PRODUCTION";

   private String siteName;
   
   private SolrClient solrClient = null;

   public PSSolrDeliveryHandler(String siteName, String serverType, boolean forceSolrClean) throws PSDeliveryException
   {
      // Without this zookeeper looks for sasl config in
      // AppServer/server/rx/conf/login-config.xml
      // Default loging contetxt name is "Client" but can be changed with server
      // properties "zookeeper.sasl.clientconfig"
      // If sequrity is required we should use other mechanism. Use of server
      // property to set client config may make
      // having multiple configurations not be thread safe, so we synchronize
      // the access.

      System.setProperty(ENABLE_CLIENT_SASL_KEY, "false");

      this.siteName = siteName;
      this.serverType = serverType;

      PSSolrConfig config = SolrConfigLoader.getDeliveryServerConfig();
      
      if (config!=null && config.getSolrServer() != null)
      {
         for (SolrServer solrConfig : config.getSolrServer())
         {
            if ( solrConfig.isEnabledSite(siteName) && 
                  (solrConfig.getServerType() == null && serverType.equalsIgnoreCase("PRODUCTION")
                  || (solrConfig.getServerType() != null && solrConfig.getServerType().equalsIgnoreCase(serverType)) ))
            {
               serverConfig = solrConfig;
               break;
            }
         }
         if (serverConfig !=null && forceSolrClean)
            deleteAllSolrEntries();
      }
   }

   private void deleteAllSolrEntries() throws PSDeliveryException
   {
      if (!isEnabled())
         return;

      log.debug("Deleting existing metadata entries");

      if (!serverConfig.isCleanAllOnFullPublish())
         return;

      synchronized (this)
      {
         SolrClient solrClient = getClient();
         try
         {
            solrClient.deleteByQuery("*:*");
            serverConfig.setDelivered(true);
         }
         catch (SolrServerException e)
         {
            rollback();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());

         }
         catch (IOException e)
         {
            rollback();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }

         catch (Exception e ){
             rollback();
             throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }
      }

   }

   public void sendMetadataToSolr(String path, IPSMetadataEntry entry, PSPurgableTempFile psPurgableTempFile) throws PSDeliveryException
   {
      if (!isEnabled())
         return;

      if (!serverConfig.isActive())
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, null, "Skipped due to previous fatal error or max Solr errors reached");

      synchronized (this)
      {
         SolrClient client = getClient();

         if (entry.getType()!=null && entry.getType().equals("page"))
            sendMetadata(path, serverConfig, entry, client,psPurgableTempFile);
         else
         {
            sendFile(path, serverConfig, client, transform(serverConfig,entry), psPurgableTempFile);
         }
      }
      if (!serverConfig.isDelivered())
         serverConfig.setDelivered(true);

   }


   private void sendMetadata(String path, SolrServer solrConfig, IPSMetadataEntry entry, SolrClient client,PSPurgableTempFile psPurgableTempFile)
         throws PSDeliveryException
   {
      if (solrConfig.isEnabledSite(siteName))
      {
         
         
        
         boolean success = sendMetadata(path, solrConfig, client, transform(solrConfig,entry),psPurgableTempFile);
         solrConfig.setDelivered(true);
         if (!solrConfig.isDelivered())
            solrConfig.setDelivered(success);
      }
   }

   private void sendFile(String path, SolrServer solrConfig, SolrClient client, Set<IPSMetadataProperty> metaset, PSPurgableTempFile psPurgableTempFile) throws PSDeliveryException

   {
      try
      {
    
         ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");

         
         String type = null;
         log.debug("Sending File to Solr");
         req.setParam("literal.id", path);
         log.debug("literal.id:"+path);
         for (IPSMetadataProperty property : metaset)
         {
            if (property.getName().equals("dcterms:format"))
               type = property.getValue();
            req.setParam("literal." + property.getName(), property.getValue());
            log.debug("literal." + property.getName()+":"+property.getValue());
         }
         
         req.addFile(psPurgableTempFile, type);
         
         NamedList<Object> result;
        
         result = client.request(req);
         log.info("Solr Result: " + result);
      }
      catch (SolrServerException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());

      }
      catch (IOException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());

      }
   

   }

   private boolean sendMetadata(String path, SolrServer solrConfig, SolrClient client, Set<IPSMetadataProperty> metaset,PSPurgableTempFile psPurgableTempFile )
         throws PSDeliveryException
   {
      SolrInputDocument doc = new SolrInputDocument();
      log.debug("Sending Page Metadata");
      doc.addField("id", path);
      log.debug("id:"+path);
    
     
      for (IPSMetadataProperty meta : metaset)
      {
         log.debug(meta.getName()+":"+meta.getValue());
         doc.addField(meta.getName(), meta.getValue());
      }
      
      UpdateResponse result;
      try
      {
         result = client.add(doc);
         sendFile(path, solrConfig, client, metaset, psPurgableTempFile);
      }
      catch (SolrException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
      }
      catch (SolrServerException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
      }
      catch (IOException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
      }
      log.debug("Solr Result: " + result);
      return true;
   }

   public void delete(String path) throws PSDeliveryException
   {

      if (!isEnabled())
         return;
      
      if (!serverConfig.isActive())
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, null,
               "Max Solr Errors Reached");
      
      synchronized (this)
      {
         SolrClient client = getClient();

  

         try
         {
            client.deleteById(String.valueOf(path));
            if (!serverConfig.isDelivered())
               serverConfig.setDelivered(true);
         }
         catch (SolrException e)
         {
            serverConfig.incrError();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }
         catch (SolrServerException e)
         {
            serverConfig.incrError();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());

         }
         catch (IOException e)
         {
            serverConfig.incrError();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }
      }

   }

   public void commit() throws PSDeliveryException
   {
      if (!isEnabled())
         return;

      // No items were delivered. Nothing to commit

      if (!serverConfig.isDelivered())
         return;
      
      if (!serverConfig.isActive())
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, null,
               "Max Solr Errors Reached");

      synchronized (this)
      {
         SolrClient client = getClient();

         log.info("Committing solr changes for for site " + this.siteName + " type=" + this.serverType + " solrUrl="
               + serverConfig.getSolrHost());

         try
         {
            client.commit();
         }
         catch (SolrException e)
         {
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }
         catch (SolrServerException e)
         {
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }
         catch (IOException e)
         {
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, e.getMessage());
         }
         finally
         {
            serverConfig=null;
            if (client != null)
            {
               try
               {
                  client.close();
               }
               catch (IOException e)
               {
               }
            }
         }
      }

   }

   public void rollback()
   {

      // No items were delivered. Nothing to commit
      if (!serverConfig.isDelivered())
         return;

      synchronized (this)
      {
         SolrClient client = getClient();
       
         if (!serverConfig.isActive())
            return;

         log.error("Rolling back solr changes on error for site " + this.siteName + " solrUrl="
               + serverConfig.getSolrHost());

         if (client != null)
         {
            try
            {
               client.rollback();
            }
            catch (SolrException e)
            {
               log.debug("Exception attempting to roll back Solr, continue anyway", e);
            }
            catch (SolrServerException e)
            {
               log.debug("Exception attempting to roll back Solr, continue anyway", e);
            }
            catch (IOException e)
            {
               log.debug("Exception attempting to roll back Solr, continue anyway", e);
            }
            finally
            {
               serverConfig=null;
               
               if (client != null)
               {
                  try
                  {
                     client.close();
                     
                  }
                  catch (IOException e)
                  {
                  }
               }
            }
         }
      }

   }

   public boolean isEnabled()
   {

      return !(fatalError || serverConfig == null);
   }

   private Set<IPSMetadataProperty> transform(SolrServer solrConfig,IPSMetadataEntry entry)
   {
      Set<IPSMetadataProperty> transformed = new HashSet<>();
      
      transformed.add(new PSMetadataProperty("name", entry.getName()));
      transformed.add(new PSMetadataProperty("linktext", entry.getLinktext()));
      transformed.add(new PSMetadataProperty("type", entry.getType()));
      transformed.add(new PSMetadataProperty("site", entry.getSite()));
      transformed.add(new PSMetadataProperty("folder", entry.getFolder()));
      transformed.add(new PSMetadataProperty("pagepath", entry.getPagepath()));
      
      
      
      for (IPSMetadataProperty property : entry.getProperties())
      {
         if (solrConfig.hasMetaMapping(property.getName()))
         {
            transformed.add(new PSMetadataProperty(solrConfig.getMetaMapping(property.getName()), property.getValue()));
         } else {
            transformed.add(new PSMetadataProperty(property.getName(), property.getValue()));
            
         }
      }
      return transformed;
   }


    private synchronized SolrClient getClient() {
        if (!isEnabled())
            return null;

        if (!serverConfig.isActive())
            return null;

        Boolean isCloudServer = serverConfig.isServerCloudType();

        //Depend upon server type Client object would be returned
        if (isCloudServer) {

            if (serverConfig.getSaslContextName() != null) {
                System.setProperty(ENABLE_CLIENT_SASL_KEY, "true");
                System.setProperty(LOGIN_CONTEXT_NAME_KEY, serverConfig.getSaslContextName());
            } else {
                if (saslConfigName != null) {
                    System.setProperty(ENABLE_CLIENT_SASL_KEY, "false");
                    saslConfigName = null;
                }
            }
            if (solrClient == null) {
                log.info("Connecting to Solr with Zookeeper with sasl authentication in AppServer/server/rx/conf/login-conf.xml with configured context name "
                        + serverConfig.getSaslContextName());
                // Must make sure to close cloudClient Instance in commit or
                // rollback.
                @SuppressWarnings("resource")
                CloudSolrClient cloudClient = new CloudSolrClient.Builder().withZkHost(serverConfig.getSolrHost()).build();
                cloudClient.setDefaultCollection(serverConfig.getDefaultCollection());
                solrClient = cloudClient;
            }
        } else {

            if (solrClient == null) {
              //  @SuppressWarnings("resource")

              /* System.setProperty("javax.net.ssl.keyStore", "C:\\Program Files\\Java\\jre1.8.0_60\\bin\\solr-ssl.keystore.jks");
               System.setProperty("javax.net.ssl.keyStorePassword", "secret");
               System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files\\Java\\jre1.8.0_60\\bin\\solr-ssl.keystore.jks");
               System.setProperty("javax.net.ssl.trustStorePassword", "secret");*/



               HttpSolrClient httpSolrClient = new HttpSolrClient.Builder(serverConfig.getSolrHost()).build();
               httpSolrClient.setUseMultiPartPost(true);
                solrClient = httpSolrClient;
            }

        }

        return solrClient;
    }
}
