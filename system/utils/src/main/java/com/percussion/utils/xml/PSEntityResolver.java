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
package com.percussion.utils.xml;

import com.percussion.utils.string.PSFolderStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author dougrand
 *
 * This entity resolver allows references files in the rhythmyx root
 * directory by looking for the initial string /Rhythmyx/. It then resolves
 * the rest of the string to a path in the resolution directory. If the passed
 * reference is not appropriate for this resolver or the reference is not
 * found, the default action will occur by the 
 * {@link #resolveEntity(String, String)} method returning <code>null</code>.
 * @deprecated
 */
@Deprecated
public class PSEntityResolver implements EntityResolver
{
   private static final Logger log = LogManager.getLogger(PSEntityResolver.class);

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
   private static Throwable lastSetResolutionHomeStackTrace = new Throwable();
   
   /**
    * Assumed entity root reference string.
    */
   private static final String RXROOT = "/Rhythmyx/";
   
   /**
    * Initialized in the {@link #getInstance()} method, never changed
    * afterward.
    */
   private static PSEntityResolver singleton = null;
   
   /**
    * The directory to resolve references from. This is the current working
    * directory by default, but can be overridden.
    */
   private static volatile File resolutionHome = new File(System.getProperty("rxdeploydir","."));
   
   /**
    * Map of public ids to local resource files containing the DTD, never 
    * <code>null</code>, may be empty.  
    */
   private static final Map<String, String> localResources =
      new HashMap<>();
   
   static
   {
      // load map from file
      Properties props = new Properties();
      try
      {
         props.load(PSEntityResolver.class.getResourceAsStream(
            "PSLocalDTDResources.properties"));
         Enumeration<?> keys = props.propertyNames();
         while(keys.hasMoreElements())
         {
            String key = (String) keys.nextElement();
            localResources.put(key, props.getProperty(key));
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
      if (singleton == null)
      {
         singleton = new PSEntityResolver();
      }
      return singleton;
   }


   private String stripHeaderAndTrailingSlashes(String header, String arg){
      // Remove "file:", and any extra leading slashes
      final int HEADER_LEN = header.length();
      int i = HEADER_LEN;
      for(; i < arg.length(); i++)
      {
         if (arg.charAt(i) != '/') break;
      }
      return arg.substring(i == HEADER_LEN ? HEADER_LEN : i - 1);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
    */
   public InputSource resolveEntity(String publicid, String systemid)
      throws IOException
   {
      if (systemid == null) {
         return null;
      }

      //Resolve local entities first
      InputSource rval = getLocalSource(publicid, systemid, true);
      if (rval != null) {
         return rval;
      }

      final String FILE_PROTO_HEADER = "file:";
      final String PERCUSSION_HEADER = "percussion:";
      String header=null;

      if (systemid.startsWith(FILE_PROTO_HEADER)) {
         header = FILE_PROTO_HEADER;
      }else if(systemid.startsWith(PERCUSSION_HEADER)) {
         header = PERCUSSION_HEADER;
      }

      if(header != null){
         systemid = stripHeaderAndTrailingSlashes(header, systemid);
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
         entityFile = new File(resolutionHome,
            systemid.substring(RXROOT.length()));
      }
      else if (systemid.startsWith("/"))
      {
         Path p;
         if(!systemid.startsWith(resolutionHome.getPath())) {
            p = Paths.get(resolutionHome.getPath(), systemid);
         }else{
            p = Paths.get(systemid);
         }
         if(!p.toFile().exists()){
            return null;
         }

         if(!PSFolderStringUtils.isChildOfFilePath(resolutionHome.toPath(), p)){
            log.warn("Blocking external entity reference: {} as it is outside of the installation folder: {}.",
                    systemid,resolutionHome.getAbsolutePath());
            return null;
         }

         entityFile = p.toFile();
      }else{
        Path p =  Paths.get(resolutionHome.getAbsolutePath(),systemid);
        if(!PSFolderStringUtils.isChildOfFilePath(resolutionHome.toPath(), p)){
           return null;
        }

        if(Files.exists(p)){
           entityFile = p.toFile();
        }
      }
         
      // For any of the above that matched, we try and resolve
      if (entityFile != null)
      {
         if (entityFile.exists())
         {
               FileInputStream is = new FileInputStream(entityFile);
               return getInputSource(publicid, systemid, is);

         }
         else
         {
               log.error("entityFile doesn't exist! file: {} Error: {}",
                  entityFile.getAbsolutePath(),
                  lastSetResolutionHomeStackTrace.getMessage());
               log.debug(lastSetResolutionHomeStackTrace);
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
      
      String localResource = localResources.get(publicid.replace(' ', '_'));
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
                  "Failed to load local resource for entity resolution: {}" ,
                  localResource);
            }
         }
      }
      else
      {
         if (warnIfMissing)
         {
            LogManager.getLogger(this.getClass()).warn(
                  "Failed to locate matching local resource for public id: {}" ,
                  publicid);
         }
      }
      
      return source;
   }

   /**
    * @return the current resolution home. This is used to resolve entity
    *         reference that the resolver recognizes.
    */
   public static synchronized File getResolutionHome()
   {
      return resolutionHome;
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
   public static synchronized  void setResolutionHome(File dir)
   {
      if (dir == null)
      {
         throw new IllegalArgumentException("dir must never be null");
      }
      if (!dir.exists())
      {
         throw new IllegalArgumentException(
            "dir must exist: " + dir.getAbsolutePath());
      }
      if (!dir.isDirectory())
      {
         throw new IllegalArgumentException(
            "dir must be a directory: " + dir.getAbsolutePath());
      }
      
      if (!resolutionHome.getAbsolutePath().equals(dir.getAbsolutePath()))
      {
         //remember who set it, so that we could trace later
         lastSetResolutionHomeStackTrace =
            new Throwable("EntityResolver last setResolutionHome stack.");
      }

         log.info("Entity Resolution home set to:  {} " ,
               dir.getAbsolutePath());
      
      resolutionHome = dir;
   }

}
