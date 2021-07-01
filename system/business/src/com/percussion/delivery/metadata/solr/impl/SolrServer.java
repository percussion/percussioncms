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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SolrServer")
public class SolrServer
{
   
   public static final Logger log = LogManager.getLogger(SolrServer.class);
   

   @XmlElement(name = "serverType")
   private String serverType;
  
   @XmlElement(name = "solrHost", required = true)
   private String solrHost;
   
   @XmlElement(name = "defaultCollection")
   private String defaultCollection;
   
   @XmlElement(name = "saslContextName")
   private String saslContextName;

   @XmlElement(name = "maxErrors")
   private int maxErrors = 4;
   
   @XmlElement()
   private boolean cleanAllOnFullPublish;
   
   @XmlElement(name = "metadataMap", required = true)
   @XmlJavaTypeAdapter(MetadataMapAdaptor.class)
   private Map<String,String> metadataMap;
   
   @XmlElement(name = "site")
   @XmlElementWrapper(name = "enabledSites", required = true)
   private List<String> enabledSites;
  
   @XmlTransient
   private boolean fatalError = false;
   @XmlTransient
   private boolean delivered = false;
   @XmlTransient
   private int errorCount = 0;



    //By default we are creating standalone client type
    @XmlElement()
    private boolean serverCloudType=false;

    public boolean isServerCloudType() {
        return serverCloudType;
    }

    public void setServerCloudType(boolean serverCloudType) {
        this.serverCloudType = serverCloudType;
    }

    /***
     * Return the DTS server type for the site.  Defaults to PRODUCTION if not specified.
     * @return Never null
     */
   public String getServerType()
   {
       if(serverType == null || serverType.trim().equals("")){
           log.warn("Missing serverType element in solr-servers.xml for solrHost " + solrHost + ". Defaulting to PRODUCTION");
           serverType="PRODUCTION";
       }
      return serverType;
   }

   public void setServerType(String serverType)
   {
      this.serverType = serverType;
   }
   
   public String getSolrHost()
   {
      return solrHost;
   }
   public void setSolrHost(String solrHost)
   {
      this.solrHost = solrHost;
   }
   
   public boolean hasMetaMapping(String name)
   {
      return metadataMap == null ? false : metadataMap.containsKey(name);
   }
  
   public boolean isFatalError()
   {
      return fatalError;
   }
   public void setFatalError(boolean fatalError)
   {
      this.fatalError = fatalError;
   }
   public boolean isDelivered()
   {
      return delivered;
   }
   public void setDelivered(boolean delivered)
   {
      this.delivered = delivered;
   }
   
   public boolean solrConfig()
   {
      return cleanAllOnFullPublish;
   }
   
   public boolean isCleanAllOnFullPublish()
   {
      return this.cleanAllOnFullPublish;
   }
   
   public void setCleanAllOnFullPublish(boolean cleanAllOnFullPublish)
   {
      this.cleanAllOnFullPublish = cleanAllOnFullPublish;
   }
   
   public String getDefaultCollection()
   {
      return defaultCollection;
   }
   public void setDefaultCollection(String defaultCollection)
   {
      this.defaultCollection = defaultCollection;
   }
   
   public String getSaslContextName()
   {
      return saslContextName;
   }
   public void setSaslContextName(String saslContextName)
   {
      this.saslContextName = saslContextName;
   }

   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlRootElement(name = "metadataMap")
   public static class MetadataMap {
       @XmlElement(name = "entry", required = true)
       private final List<SolrMetaMapEntry> a = new ArrayList<>();
       public List<SolrMetaMapEntry> getSolrMetaMapEntry() {
           return this.a;
       }
   }
   
   
   public static class MetadataMapAdaptor extends XmlAdapter<MetadataMap, Map<String,String>> {

      public MetadataMapAdaptor()
      {
         
      }
      @Override
      public MetadataMap marshal(Map<String,String> v) throws Exception {
          MetadataMap myMap = new MetadataMap();
          List<SolrMetaMapEntry> aList = myMap.getSolrMetaMapEntry();
          for ( Map.Entry<String,String> e : v.entrySet() ) {
              aList.add(new SolrMetaMapEntry(e.getKey(), e.getValue()));
          }
          return myMap;
      }

      @Override
      public Map<String,String> unmarshal(MetadataMap v) throws Exception {
          Map<String,String> map = new HashMap<>();
          for ( SolrMetaMapEntry e : v.getSolrMetaMapEntry() ) {
              map.put(e.getKey(), e.getValue());
          }
          return map;
      }
  }

   public String getMetaMapping(String name)
   {
      return metadataMap == null ? null : metadataMap.get(name);
   }
   
   public List<String> getEnabledSites() {
      return this.enabledSites;
   }
   
   public boolean isEnabledSite(String site)
   {
      return this.enabledSites == null ? false : this.enabledSites.contains(site);
   }
   
   public void addSiteEntry(String siteName)
   {
      if (enabledSites == null)
         enabledSites = new ArrayList<>();
      enabledSites.add(siteName);
   }
   public void addMetaMapEntry(String key, String value)
   {
      if (metadataMap == null)
         metadataMap = new HashMap<>();
      metadataMap.put(key, value);
   }
   
   public boolean isActive() {
      return (!fatalError && errorCount < maxErrors);
   }
   
   public void incrError()
   {
      errorCount++;
      if (maxErrors > 0 && errorCount == maxErrors)
         log.error("Max error count "+maxErrors+" for solr server "+solrHost+" reached");
   }
}
