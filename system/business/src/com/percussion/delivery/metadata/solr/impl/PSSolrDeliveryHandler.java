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

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;
import com.percussion.error.PSExceptionUtils;
import com.percussion.rx.delivery.IPSDeliveryErrors;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.util.PSPurgableTempFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   public static final Logger log = LogManager.getLogger(PSSolrDeliveryHandler.class);

   private boolean fatalError = false;

   private SolrServer serverConfig = null;

   private String serverType;

   private String siteName;
   
   private SolrClient solrClient = null;

   public PSSolrDeliveryHandler(String siteName, String serverType, boolean forceSolrClean) throws PSDeliveryException
   {
      // Without this zookeeper looks for sasl config in
      // TODO: Where is this config pulled from under jetty?
      // Default logging context name is "Client" but can be changed with server
      // properties "zookeeper.sasl.clientconfig"
      // If security is required we should use other mechanism. Use of server
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
            if(solrClient != null) {
               solrClient.deleteByQuery("*:*");
               serverConfig.setDelivered(true);
            }
         } catch (Exception e)
         {
            rollback();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e, PSExceptionUtils.getMessageForLog(e));

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
            if(client!=null) {
               sendFile(path, serverConfig, client, transform(serverConfig, entry), psPurgableTempFile);
            }
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
         log.debug("literal.id: {}",path);
         for (IPSMetadataProperty property : metaset)
         {
            if (property.getName().equals("dcterms:format"))
               type = property.getValue();
            req.setParam("literal." + property.getName(), property.getValue());
            log.debug("literal. {}:{}",
                    property.getName(),
                    property.getValue());
         }
         
         req.addFile(psPurgableTempFile, type);
         
         NamedList<Object> result;
        
         result = client.request(req);
         log.info("Solr Result: {}" , result);
      }
      catch (SolrServerException | IOException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e,PSExceptionUtils.getMessageForLog(e));

      }


   }

   private boolean sendMetadata(String path, SolrServer solrConfig, SolrClient client, Set<IPSMetadataProperty> metaset,PSPurgableTempFile psPurgableTempFile )
         throws PSDeliveryException
   {
      SolrInputDocument doc = new SolrInputDocument();
      log.debug("Sending Page Metadata");
      doc.addField("id", path);
      log.debug("id:{}",path);
    
     
      for (IPSMetadataProperty meta : metaset)
      {
         log.debug("{}:{}",meta.getName(),meta.getValue());
         doc.addField(meta.getName(), meta.getValue());
      }
      
      UpdateResponse result;
      try
      {
         result = client.add(doc);
         sendFile(path, solrConfig, client, metaset, psPurgableTempFile);
      }
      catch (SolrException | SolrServerException | IOException e)
      {
         solrConfig.incrError();
         throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e,PSExceptionUtils.getMessageForLog(e));
      }
      log.debug("Solr Result: {}" , result);
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
            if(client!=null) {
               client.deleteById(String.valueOf(path));
               if (!serverConfig.isDelivered())
                  serverConfig.setDelivered(true);
            }
         }
         catch (SolrException | SolrServerException | IOException e)
         {
            serverConfig.incrError();
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e,PSExceptionUtils.getMessageForLog(e));
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
         log.info("Committing solr changes for for site {} type={} solrUrl={}",
                 this.siteName,
                 this.serverType,
                 serverConfig.getSolrHost());

         try(SolrClient client = getClient())
         {
            if(client!=null) {
               client.commit();
            }
         }
         catch (SolrException | SolrServerException | IOException e)
         {
            throw new PSDeliveryException(IPSDeliveryErrors.SOLR_COMMUNICATION_EXCEPTION, e,PSExceptionUtils.getMessageForLog(e));
         } finally
         {
            serverConfig=null;
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
         if (!serverConfig.isActive())
            return;

         log.error("Rolling back solr changes on error for site {} solrUrl={}",
                 this.siteName,
                 serverConfig.getSolrHost());

         try(  SolrClient client = getClient())
         {
            if(client!=null) {
               client.rollback();
            }
         }
         catch (SolrException | SolrServerException | IOException e)
         {
            log.debug("Exception attempting to roll back Solr, continue anyway", e);
         } finally
         {
            serverConfig=null;
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

        boolean isCloudServer = serverConfig.isServerCloudType();

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
                // Must make sure to close cloudClient Instance in commit or
                // rollback.
                CloudSolrClient cloudClient = new CloudSolrClient.Builder().withZkHost(serverConfig.getSolrHost()).build();
                cloudClient.setDefaultCollection(serverConfig.getDefaultCollection());
                solrClient = cloudClient;
            }
        } else {

            if (solrClient == null) {
               HttpSolrClient httpSolrClient = new HttpSolrClient.Builder(serverConfig.getSolrHost()).build();
               httpSolrClient.setUseMultiPartPost(true);
                solrClient = httpSolrClient;
            }

        }

        return solrClient;
    }
}
