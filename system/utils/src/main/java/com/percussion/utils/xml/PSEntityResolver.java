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
package com.percussion.utils.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author dougrand
 *
 * This entity resolver allows references to files in the rhythmyx root
 * directory by looking for the initial string /Rhythmyx/. It then resolves
 * the rest of the string to a path in the resolution directory. If the passed
 * reference is not appropriate for this resolver or the reference is not
 * found, the default action will occur by the 
 * {@link #resolveEntity(String, String)} method returning <code>null</code>.
 */
@SuppressWarnings(value={"unchecked"})
public class PSEntityResolver implements EntityResolver
{
   /**
    * 127.0.0.1
    */
   private static final String LOCALHOST_IP_ADDRESS = "127.0.0.1";
   
   /**
    * Constant for http protocol
    */
   private static final String HTTP_PROTOCOL = "http";

   /**
    * Debug helper that allows to trace who last set the root,
    * never <code>null</code>.
    * Initialized by {@link #setResolutionHome(File)}. 
    */
   private static Throwable ms_lastSetResolutionHomeStackTrace = new Throwable();
   
   /**
    * Assumed entity root reference string.
    */
   private static final String RXROOT = "/Rhythmyx/";
   
   /**
    * Initialized in the {@link #getInstance()} method, never changed
    * afterward.
    */
   private static PSEntityResolver ms_singleton = null;
   
   /**
    * The directory to resolve references from. This is the current working
    * directory by default, but can be overridden.
    */
   private static volatile File ms_resolutionHome = new File("."); 
   
   /**
    * Map of public ids to local resource files containing the DTD, never 
    * <code>null</code>, may be empty.  
    */
   private static Map<String, String> ms_localResources = 
      new HashMap<String, String>(); 
   
   static
   {
      // load map from file
      Properties props = new Properties();
      try
      {
         props.load(PSEntityResolver.class.getResourceAsStream(
            "PSLocalDTDResources.properties"));
         Enumeration keys = props.propertyNames();
         while(keys.hasMoreElements())
         {
            String key = (String) keys.nextElement();
            ms_localResources.put(key, props.getProperty(key));
         }
      }
      catch (IOException e)
      {
         LogManager.getLogger(PSEntityResolver.class).warn(
            "Failed to load resource file, local resolution of external URLs " +
            "will be disabled.", e);
      }
   }
   
   /**
    * Hide constructor to force use of factory. There only needs to be
    * a single instance of this class
    */
   private PSEntityResolver()
   {
   }
   
   /**
    * Get the singleton
    * 
    * @return The singleton, never <code>null</code>.
    */
   public static synchronized PSEntityResolver getInstance()
   {
      if (ms_singleton == null)
      {
         ms_singleton = new PSEntityResolver();
      }
      return ms_singleton;
   }

   /* (non-Javadoc)
    * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
    */
   public InputSource resolveEntity(String publicid, String systemid)
      throws SAXException, IOException
   {
      if (publicid != null); // We ignore public ids 
      
      if (systemid == null) return null;
      
      try
      {
         URI url = new URI(systemid);
         if (url.getScheme().startsWith(HTTP_PROTOCOL))
         {
            // ignore external urls
            if (!(url.getHost().equals("localhost") || url.getHost().equals(
               LOCALHOST_IP_ADDRESS)))
               return getLocalSource(publicid, systemid, true);
         }
      }
      catch (Exception e)
      {
         // ignore, not a url, try the public id anyway, and return if we
         // find it. Used for local references from xhtml strict, but fine
         // for other files that are referenced with a public id
         InputSource rval = getLocalSource(publicid, systemid, false);
         if (rval != null)
         {
            return rval;
         }
      }
      
      final String FILE_PROTO_HEADER = "file:"; 
      if (systemid.startsWith(FILE_PROTO_HEADER))
      {
         // Remove "file:", and any extra leading slashes
         final int HEADER_LEN = FILE_PROTO_HEADER.length(); 
         int i = HEADER_LEN;
         for(; i < systemid.length(); i++)
         {
            if (systemid.charAt(i) != '/') break;
         }
         systemid = systemid.substring(i == HEADER_LEN ? HEADER_LEN : i - 1);
      }
      else if(systemid.startsWith(HTTP_PROTOCOL + "://" + LOCALHOST_IP_ADDRESS))
      {
         int index = systemid.indexOf(RXROOT);
         systemid = systemid.substring(index);
      }
      
      File entityFile = null;
      
      if (systemid.startsWith(RXROOT))
      {
         // Entities that appear to be in the Rhythmyx directory
         entityFile = new File(ms_resolutionHome,
            systemid.substring(RXROOT.length()));
      }
      else if (!systemid.startsWith("/"))
      {
         // Relative paths
         entityFile = new File(ms_resolutionHome, systemid);
      }
         
      // For any of the above that matched, we try and resolve
      if (entityFile != null)
      {
         if (entityFile.exists())
         {
            FileInputStream is = new FileInputStream(entityFile);
            InputSource source = getInputSource(publicid, systemid, is);
            return source;
         }
         else
         {
            Logger logger = LogManager.getLogger(PSEntityResolver.class);
            if (logger!=null)
               logger.error("entityFile doesn't exist! file: " +
                  entityFile.getAbsolutePath(),
                  ms_lastSetResolutionHomeStackTrace);
         }
      }
      
      return null;
   }

   /**
    * Create an input source from an input stream.
    * 
    * @param publicid The public id if known, may be <code>null</code> or empty.
    * @param systemid The system id if konwn, may be <code>null</code> or empty.
    * @param is The input stream to use, may not be <code>null</code>.
    * 
    * @return The input source, never <code>null</code>.
    */
   private InputSource getInputSource(String publicid, String systemid, 
      InputStream is)
   {
      InputSource source = new InputSource(is);
      source.setSystemId(systemid);
      source.setPublicId(publicid);
      
      return source;
   }

   /**
    * Get a local source for a public id.  Used to avoid obtaining sources from
    * an external url.  Public ids that are known to reference external URLs may
    * be mapped to a local resource location, in which case an input source to
    * that resource is returned.
    *  
    * @param publicid The public id, may be <code>null</code> or empty.
    * @param warnIfMissing if the public id isn't found and this is <code>true</code>
    * then output a warning to the log
    * 
    * @return The input source to the local resource if one is configured for
    * the supplied public id, or <code>null</code> if one is not configured or
    * if the supplied public id is <code>null</code> or empty.
    */
   private InputSource getLocalSource(String publicid, String systemid,
      boolean warnIfMissing)
   {
      InputSource source = null;
      
      if (StringUtils.isBlank(publicid))
         return source;
      
      String localResource = ms_localResources.get(publicid.replace(' ', '_'));
      if (!StringUtils.isBlank(localResource))
      {
         InputStream in = this.getClass().getResourceAsStream(localResource);
         if (in != null)
         {
            source = getInputSource(publicid, systemid, in);
         }
         else
         {
            if (warnIfMissing)
            {
               LogManager.getLogger(this.getClass()).warn(
                  "Failed to load local resource for entity resolution: " + 
                  localResource);
            }
         }
      }
      else
      {
         if (warnIfMissing)
         {
            LogManager.getLogger(this.getClass()).warn(
                  "Failed to locate matching local resource for public id: " +
                  publicid);
         }
      }
      
      return source;
   }

   /**
    * @return the current resolution home. This is used to resolve entity
    *         reference that the resolver recognizes.
    */
   public File getResolutionHome()
   {
      return ms_resolutionHome;
   }

   /**
    * Modifies the resolution home. This call should never be required by 
    * a normal running server. It is available for use by tools or the 
    * installer to allow variant configurations to be supported by the
    * resolver.
    *  
    * @param dir a new resolution home, must never be <code>null</code> and
    * must point to a valid directory.
    */
   public synchronized static void setResolutionHome(File dir)
   {
      if (dir == null)
      {
         throw new IllegalArgumentException("dir must never be null");
      }
      if (dir.exists() == false)
      {
         throw new IllegalArgumentException(
            "dir must exist: " + dir.getAbsolutePath());
      }
      if (dir.isDirectory() == false)
      {
         throw new IllegalArgumentException(
            "dir must be a directory: " + dir.getAbsolutePath());
      }
      
      if (!ms_resolutionHome.getAbsolutePath().equals(dir.getAbsolutePath()))
      {
         //remember who set it, so that we could trace later
         ms_lastSetResolutionHomeStackTrace = 
            new Throwable("EntityResolver last setResolutionHome stack.");
      }
               
      Logger logger = LogManager.getLogger(PSEntityResolver.class);
      if (logger!=null)
         logger.info("Entity Resolution home set to:  " +
               dir.getAbsolutePath());
      
      ms_resolutionHome = dir;
   }

}
