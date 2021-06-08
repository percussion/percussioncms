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
package com.percussion.design.objectstore;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
/**
 * Implementation of the interface <code>IPSJavaPluginConfig</code>.
 */
public class PSJavaPluginConfig implements IPSJavaPluginConfig
{
   private static final Logger log = LogManager.getLogger(PSJavaPluginConfig.class);
   /**
    * Default ctor. Creates the configuration with just one entry for the
    * default plugin.
    */
   public PSJavaPluginConfig()
   {
      addDefaultPlugin();
   }

   /**
    * Ctor taking the sourceElement for the plugin configuration.
    * @param srcElem the source element, must not be <code>null</code> and must
    * conform to the DTD specified in the interface definition.
    * @throws PSUnknownNodeTypeException if the element specified does not
    * conform to the DTD required.
    */
   public PSJavaPluginConfig(Element srcElem) throws PSUnknownNodeTypeException
   {
      fromXml(srcElem, null, null);
   }

   /**
    * Add a new plugin to the configuration. If already exists, the old one will
    * be replaced. Existence is checked based on the OS and browser keys.
    * @param plugin new plugin object to add, must not be <code>null</code>.
    * @thorws IllegalArgumentException if the plugin supplied is <code>null</code>.
    */
   public void addPlugin(IPSJavaPlugin plugin)
   {
      if(plugin == null)
         throw new IllegalArgumentException("plugin must not be null");

      String key = makeKey(plugin.getOsKey(), plugin.getBrowserKey());
      m_plugins.put(key, plugin);
   }



   /**
    * Remove and return an existing plugin from the configuration. Nothing
    * happens if does already exist. Existence is checked based on the OS and
    * browser keys.
    * @param plugin new plugin object to add, must not be <code>null</code>.
    * @return removed plugin object, can be <code>null</code> only if supplied
    * one does not exist.
    * @thorws IllegalArgumentException if the plugin supplied is <code>null</code>.
    */
   public PSJavaPlugin  removePlugin(PSJavaPlugin plugin)
   {
      if(plugin == null)
         throw new IllegalArgumentException("plugin must not be null");
      String key = makeKey(plugin.getOsKey(), plugin.getBrowserKey());
      if(m_plugins.containsKey(key))
      {
         PSJavaPlugin tmp = (PSJavaPlugin)m_plugins.get(key);
         m_plugins.remove(key);
         return tmp;
      }
      return null;
   }

   /**
    * Returns all plugins provisioned in the config.xml.
    * @return collection of IPSJavaPlugin objects, never <code>null</code>,
    * may be <code>empty</code>.
    */
   public Collection getAllPlugins()
   {
      return Collections.unmodifiableCollection(m_plugins.values());
   }

   //Implementation of the method defined in the interface
   public IPSJavaPlugin getPlugin(String httpUserAgent)
   {
      String osKey = getOSKeyFromUserAgent(httpUserAgent);
      String browserKey = getBrowserKeyFromUserAgent(httpUserAgent);
      return getPlugin(osKey, browserKey);
   }

   //Implementation of the method defined in the intreface
   public IPSJavaPlugin getPlugin(String osKey, String browserKey)
   {
      String key = makeKey(osKey, browserKey);
      if(m_plugins.containsKey(key))
         return (IPSJavaPlugin)m_plugins.get(key);
      return null;
   }

   //Implementation of the method defined in the intreface
   public IPSJavaPlugin getDefaultPlugin()
   {
      return (IPSJavaPlugin)m_plugins.get(
                  makeKey(DEFAULT_OSKEY, DEFAULT_BROWSERKEY));
   }

   //Implementation of the method defined in the intreface
   //Implementation of the interface method
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if(!sourceNode.getTagName().equals(XML_NODE_NAME))
      {
         String[] args= {XML_NODE_NAME, sourceNode.getTagName()};
         throw new PSUnknownNodeTypeException(1001, args);
      }

      //make sure we have the default plugin
      addDefaultPlugin();

      NodeList nl = sourceNode.getElementsByTagName(IPSJavaPlugin.XML_NODE_NAME);
      for(int i=0; i<nl.getLength(); i++)
         addPlugin(new PSJavaPlugin((Element)nl.item(i)));
   }

   //Implementation of the method defined in the intreface
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      Iterator iter = m_plugins.keySet().iterator();
      while(iter.hasNext())
      {
         Object obj = m_plugins.get(iter.next());
         if(obj == null || !(obj instanceof PSJavaPlugin)) //Never happens!
            continue;
         root.appendChild(((PSJavaPlugin)obj).toXml(doc));
      }
      return root;
   }

   /**
    * See the {@link IPSCmsComponent#equals(Object) interface} for complete
    * details.
    */
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSJavaPluginConfig ))
         return false;

      PSJavaPluginConfig obj2 = (PSJavaPluginConfig) o;
      if(obj2.getAllPlugins().size() != getAllPlugins().size())
         return false;

      Iterator iter1 = obj2.getAllPlugins().iterator();
      Iterator iter2 = getAllPlugins().iterator();

      PSJavaPlugin plugin1 = null;
      PSJavaPlugin plugin2 = null;
      //Even if order changes we assume that the config is changed.
      while(iter1.hasNext() && iter2.hasNext())
      {
         plugin1 = (PSJavaPlugin)iter1.next();
         plugin2 = (PSJavaPlugin)iter2.next();
         if(!plugin1.equals(plugin2))
            return false;
      }
      return true;
   }
   
   
   /**
    * Config hash code.
    */
   @Override
   public int hashCode()
   {
      int hashCode = 0;
      for (final Object pluginObj : getAllPlugins())
      {
         final PSJavaPlugin plugin = (PSJavaPlugin) pluginObj;
         hashCode += plugin.hashCode();
      }
      return hashCode;
   }

   /**
    * See IPSComponent.
    */
   public int getId()
   {
      throw new UnsupportedOperationException("getId is not implemented");
   }

   /**
    * See IPSComponent.
    */
   public void setId(int id)
   {
      throw new UnsupportedOperationException("setId is not implemented");
   }

   /**
    * See IPSComponent.
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      throw new UnsupportedOperationException("validate is not implemented");
   }


   /**
    * See IPSComponent.
    */
   public Object clone()
   {
      throw new UnsupportedOperationException("clone is not implemented");
   }


   /**
    * Helper method to make a unique key for the plugin map.
    * @param osKey assumed not <code>null</code> or empty.
    * @param browserKey assumed not <code>null</code> or empty.
    * @return key string ready to use as the key for the plugin map, never
    * <code>null</code>or empty. This made up of a combination of os key and
    * browser key.
    */
   private String makeKey(String osKey, String browserKey)
   {
      return osKey + KEY_SEPARATOR + browserKey;
   }

   /**
    * Gets the browser from useragent, if userAgent is <code>null</code> or
    * empty or if the supported browser does not exist in userAgent then
    * default value of Any is returned.
    * 
    * @param userAgent, HTTP_USER_AGENT string, may be <code>null</code> or 
    * empty.
    * 
    * @return One of the {@link #BROWSER_LIST} values, or 
    * {@link #DEFAULT_BROWSERKEY} if the supplied agent is <code>null</code> or 
    * empty or not one of the known types as defined in 
    * {@link #BROWSER_LIST_USERAGENT}.
    */
    public static String getBrowserKeyFromUserAgent(String userAgent)
    {
      if(userAgent != null && userAgent.trim().length() > 1)
      {
         for(int i=0; i<BROWSER_LIST_USERAGENT.length; i++)
         {
            String key = BROWSER_LIST_USERAGENT[i];
            if(userAgent.indexOf(key) > -1)
               return BROWSER_LIST[i];
         }
      }
      return DEFAULT_BROWSERKEY;
    }

   /**
    * Gets the OS from useragent, if userAgent is <code>null</code> or
    * empty or if the supported OS does not exist in userAgent then
    * default value will be returned.
    * @param userAgent, HTTP_USER_AGENT string, may be <code>null</code> or
    * empty.
    * 
    * @return One of the {@link #OS_LIST} values, or {@link #DEFAULT_OSKEY} if 
    * the supplied agent is <code>null</code> or empty or not one of the known
    * types as defined in {@link #OS_LIST_USERAGENT}.
    */
    public static String getOSKeyFromUserAgent(String userAgent)
    {
      if(userAgent != null && userAgent.trim().length() > 1)
      {
         for(int i=0; i<OS_LIST_USERAGENT.length; i++)
         {
            String key = OS_LIST_USERAGENT[i];
            if(userAgent.indexOf(key) > -1)
               return OS_LIST[i];
         }
      }
      return DEFAULT_OSKEY;
    }
   /**
    * Adds the degfault plugin to the map if one already does not exist.
    */
   private void addDefaultPlugin()
   {
      String key = makeKey(DEFAULT_OSKEY, DEFAULT_BROWSERKEY);
      if(m_plugins.containsKey(key))
         return;

      PSJavaPlugin plugin = new PSJavaPlugin(
         DEFAULT_OSKEY,
         DEFAULT_BROWSERKEY,
         DEFAULT_VERSIONTOUSE,
         false,
         DEFAULT_DOWNLOADLOCATION);

      m_plugins.put(key, plugin);
   }

   /**
    * Map of all plugins in the configuration. Never <code>null</code> may be
    * empty. The key for the map is made up of a combination of OS and browser
    * keys.
    */
   private Map m_plugins = new HashMap();

   //List of supported OSs for easy access, never <code>null</code> or empty.
   private static List ms_SupportedOS = new ArrayList();
   //List of supported Browsers for easy access;
   private static List ms_SupportedBrowsers = new ArrayList();

   /**
    * Put the list of suppot OSs and Browsers in a List for easy access, never
    * <code>null</code> or empty.
    */
   static
   {
      for(int i=0; i<OS_LIST.length; i++)
         ms_SupportedOS.add(OS_LIST[i]);
      for(int i=0; i<BROWSER_LIST.length; i++)
         ms_SupportedBrowsers.add(BROWSER_LIST[i]);
   }

   /**
    * Main method for testing
    */
   public static void main(String[] args)
   {
      try
      {
         String userAgent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";
         String bkey = getBrowserKeyFromUserAgent(userAgent);
         String oskey = getOSKeyFromUserAgent(userAgent);
         log.info("Browser: {} ", bkey);
         log.info("OS: {} ", oskey);
      }
      catch(Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }
}
