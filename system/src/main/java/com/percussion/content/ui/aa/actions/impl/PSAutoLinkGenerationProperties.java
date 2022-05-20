/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.server.PSServer;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Loads the autoLinkGeneration properties file from the server's config
 * directory and performs parsing to provide easily usable data. A missing
 * file, or failure to load, is acceptable and results in a logged message with
 * all the <code>get</code> methods returning their default values as noted in
 * each method's doc.
 * 
 * @author PaulHoward
 */
public class PSAutoLinkGenerationProperties
{
   /**
    * Container for the loaded properties. Never <code>null</code>.
    * Initialized in ctor.
    */
   private Properties props = new Properties();

   /**
    * Logger to use, never <code>null</code>.
    */
   private static Log ms_log = LogFactory.getLog(PSAutoLinkGenerationProperties.class);

   /**
    * The location of the file the properties are loaded from when the no-param
    * ctor is used. Made public so users
    * of this class can monitor the file and call  when the file
    * changes if they desire.
    */
   public File getDefaultConfigFile()
   {
      return new File(PSServer.getRxConfigDir() + "/autoLinkGeneration.properties");
   }

   /**
    * Like the no-param ctor, but allows the caller to specify the properties
    * rather than load from the standard config file. 
    * 
    * @param configSource Expected to be equivalent to loading the file referenced 
    * by . If <code>null</code>, behaves just like
    * the no-param ctor was called.
    */
   public PSAutoLinkGenerationProperties(Properties configSource)
   {
      reload(configSource);
   }
   
   /**
    * Creates the instance and immediately attempts to load the configuration 
    * from the default properties file, logging the event.
    */
   public PSAutoLinkGenerationProperties()
   {
      reload(null);
   }

   /**
    * Causes this object to discard the current properties and reload them from
    * the source.
    * 
    * @param configSource If not <code>null</code>, this is used as the source 
    * of configuration. If <code>null</code>, then the default file specified by  is used.
    */
   public void reload(Properties configSource)
   {
      if (configSource != null)
      {
         props = configSource;
         return;
      }
      
      File file = getDefaultConfigFile();
      InputStream source;
      try
      {
         ms_log.info("Loading auto link generation properties from " + file.getAbsolutePath());
         source = new FileInputStream(file);
         props.load(source);
         ms_log.info("Successfully loaded file.");
      }
      catch (FileNotFoundException e)
      {
         ms_log.error("Configuration file not found.", e);
         //ignore error and continue
      }
      catch (IOException e)
      {
         ms_log.error("Configuration file failed to load", e);
         //ignore error and continue
      }
   }

   /**
    * Load a property and normalize the value.
    * @param key The name of the property to load. Assumed not <code>null</code>.
    * @return Either a non-empty string or <code>null</code>.
    */
   private String getProperty(String key)
   {
      String name = props.getProperty(key);
      return StringUtils.trimToNull(name);
   }
   
   /**
    * Returns the value of the managedExternalLink.ContentType config property.
    * 
    * @return A non-empty string with the value, or <code>null</code> if there
    * isn't one.
    */
   public String getManagedExternalLinkContentTypeName()
   {
      return getProperty("managedAutoLink.ContentTypeName");
   }
   
   /**
    * The page names, such as index.html, that will be used if a url has no page
    * name. An existing page that matches any name in this list will be
    * considered a match and will prevent creating of a managed external link
    * item.
    * 
    * @return Never <code>null</code>, may be empty if this property was not
    * specified in the config file.
    */
   public Collection<String> getDefaultPageNames()
   {
      String pageNamesCSV = getProperty("defaultPageNames");
      if (pageNamesCSV == null)
         return Collections.emptyList();

      return parse(pageNamesCSV, false);
   }

   /**
    * The name to use if a url has no page name and an external link item
    * needs to be created. Should match one of the entries in {@link #getDefaultPageNames()},
    * but no check is done for this.
    * 
    * @return Either a non-empty name or <code>null</code>.
    */
   public String getDefaultPageName()
   {
      return getProperty("defaultPageName");
   }
   
   /**
    * Returns the value of the templateName config property.
    * 
    * @return A non-empty string with the value, or <code>null</code> if there
    * isn't one.
    */
   public String getTemplateName()
   {
      return getProperty("templateName");
   }
   
   /**
    * Returns the value of the communityName config property.
    * 
    * @return A non-empty string with the value, or <code>null</code> if there
    * isn't one.
    */
   public String getCommunityName()
   {
      return getProperty("communityName");
   }

   
   /**
    * Returns the value of the transition trigger to use to push a newly
    * created item to a public state.
    * 
    * @return A non-empty string with the trigger name, or <code>null</code> if there
    * isn't one.
    */
   public String getWorkflowTriggerName()
   {
      return getProperty("workflowTriggerName");
   }
   
   /**
    * Returns the value of the externalLink.ContentType config property.
    * 
    * @return A non-empty string with the value, or <code>null</code> if there
    * isn't one.
    */
   public String getExternalLinkContentTypeName()
   {
      return getProperty("externalLink.ContentTypeName");
   }

   /**
    * Is there at least 1 managed site configured.
    * 
    * @return <code>true</code> if there is, otherwise <code>false</code>.
    */
   public boolean hasManagedSites()
   {
      return getManagedSitesAndAliases().size() > 0;
   }

   /**
    * Returns all the managed site names configured as the managedSites property
    * and any aliases for each one.
    * 
    * @return Each entry pair has the sitename as a key and a collection
    * containing all the aliases as a value. If there are no aliases, an empty
    * collection is returned. If there are no managed sites, an empty list is
    * returned. All entries are normalized to lower case and no leading/trailing
    * whitespace.
    */
   public Collection<PSPair<String, Set<String>>> getManagedSitesAndAliases()
   {
      String siteNamesCSV = getProperty("managedSites");
      if (siteNamesCSV == null)
         return Collections.emptyList();

      Collection<PSPair<String, Set<String>>> results = new ArrayList<PSPair<String, Set<String>>>();
      Collection<String> siteNames = parse(siteNamesCSV, false);
      for (String siteName : siteNames)
      {
         String aliasesCSV = getProperty(siteName + ".aliases");
         Set<String> aliases = parse(aliasesCSV, true);
         results.add(new PSPair<String, Set<String>>(siteName.toLowerCase(), aliases));
      }
      
      return results;
   }
   
   /**
    * Parse the supplied CSV string.
    * 
    * @param text The text to parse. Assumed not <code>null</code>.
    * @param lowerCase If <code>true</code>, the token is lower-cased before
    * returning, otherwise, the token is returned with unchanged case.
    * @return Each entry is non-empty, whitespace trimmed and lower-cased (if
    * the lowerCase flag is <code>true</code>. Never <code>null</code>.
    */
   private Set<String> parse(String text, boolean lowerCase)
   {
      Set<String> tokens = new HashSet<String>();
      StrTokenizer toker = StrTokenizer.getCSVInstance(text);
      while (toker.hasNext())
      {
         String token = StringUtils.trimToNull(toker.nextToken());
         if (token != null)
            tokens.add(lowerCase ? token.toLowerCase() : token);
      }
      return tokens;
   }
}
