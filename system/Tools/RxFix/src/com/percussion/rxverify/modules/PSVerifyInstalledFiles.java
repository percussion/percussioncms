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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSFileInfo;
import com.percussion.rxverify.data.PSInstallation;

import java.io.File;
import java.io.IOException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author dougrand
 * 
 * This class verifies that all files were installed correctly
 */
public class PSVerifyInstalledFiles implements IPSVerify
{
   /*
    * Initialized in the calls to <code>generate</code>
    */
   private File m_rxdir = null;
   
   /**
    * These rules are used for generating the bill of materials file from a
    * known good Rhythmyx installation.
    * <P>
    * Each rule is a <code>Pattern</code> meant to match the pathname for a
    * file, followed by a category for a matching file. The rules are searched
    * front to back, and the first match "wins". Wildcards are allowed. Note
    * that the special category "IGNORED" means that matching files are not
    * placed in the bill of materials.
    */
   private static final Object RULES[] =
   {
         Pattern.compile("/_.*"), "IGNORE", // Installer detritus
         // Binaries and libraries are platform specific 
         Pattern.compile("/bin/.*"), "IGNORE", 
         // Old dynamic apps  
         Pattern.compile("/\056sys.*"), "IGNORE",
         // ECC
         Pattern.compile("/EnterpriseContentConnector\056.*"), "ECC",
         // WebDave
         Pattern.compile("/InstallableApps/apache/.*"), "WebDav",
         Pattern.compile("/InstallableApps/bea/.*"), "WebDav",
         Pattern.compile("/InstallableApps/ibm/.*"), "WebDav",
         // BEA 8
         Pattern.compile("/InstallableApps/Weblogic/.*"), "BEA 8.1 Portal",
         // Websphere
         Pattern.compile("/InstallableApps/Websphere/.*"), "Websphere Portal",
         // Publisher
         Pattern.compile("/InstallableApps/AllInOne/.*"), "Publisher",
         // Servlet
         Pattern.compile("/InstallableApps/FrontEnd/.*"), "Servlet",
         // Tomcat temp
         Pattern.compile("/AppServer/work/.*"), "IGNORE",
         Pattern.compile("/AppServer/temp/.*"), "IGNORE",
         Pattern.compile("/AppServer/webapps/.*/WEB-INF/web.000"), "IGNORE",
         // Tomcat
         Pattern.compile("/AppServer/.*"), "Tomcat",
         // Convera temp and data
         Pattern.compile("/sys_search/rware/rx/logs/.*"), "IGNORE",
         Pattern.compile("/sys_search/rware/rx/data/.*"), "IGNORE",
         Pattern.compile("/sys_search/rware/rx/indexes/.*"), "IGNORE",
         // Convera
         Pattern.compile("/sys_search/.*"), "FTS",
         // Java
         Pattern.compile("/JRE/.*"), "Java",
         // Exits
         Pattern.compile("/Exits/.*"), "Exits",
         Pattern.compile("/Docs/.*"), "Docs", 
         // Sample content - being removed from 5.5
         Pattern.compile("/rx_.*"), "IGNORE", 
         // Zho
         Pattern.compile("/rxs_.*"), "FastForward",
         // Ignore log files
         Pattern.compile("/.*\056log"), "IGNORE",
         // All Remaining files are part of the server
         Pattern.compile("/.*"), "Server" 
   };
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#verify(java.util.Map, java.io.File)
    */
   @SuppressWarnings("unchecked")
   public void verify(File rxdir, File originalRxDir,
         PSInstallation installation)
   throws NoSuchAlgorithmException, DigestException, IOException
   {
      if (installation == null)
      {
         throw new IllegalArgumentException("installation must never be null");
      }
      if (rxdir == null)
      {
         throw new IllegalArgumentException("rxdir must never be null");
      }

      Logger l = LogManager.getLogger(getClass());

      PSInstallation existing = new PSInstallation();
      generate(rxdir, existing);

      // Compare the two maps and print out information
      l.info("Verify contents of installation " + rxdir);

      Iterator iter = installation.getFileCategories();
      while (iter.hasNext())
      {
         String category = (String) iter.next();
         List<String> bomelements = installation.getFiles(category);
         List<String> realelements = existing.getFiles(category);
         Set<String> bomset = new HashSet<String>(bomelements);
         Set<String> realset = null;
         Map<String,PSFileInfo> realmap = new HashMap<String,PSFileInfo>();

         if (realelements != null)
         {
            realset = new HashSet<String>(realelements);
            Iterator<String> eiter = realset.iterator();
            while (eiter.hasNext())
            {
               //FB: BC_IMPOSSIBLE_CAST NC 1-17-16
              File f = new File(eiter.next());
               PSFileInfo file = new PSFileInfo(f,f.getParentFile().getPath()) ;
               realmap.put(file.getPath(), file);
            }
         }
         else
         {
            realset = new HashSet<String>();
         }

         if (realelements == null || realelements.size() == 0)
         {
            l.warn(category + " missing");
         }
         else if (bomset.equals(realset))
         {
            l.info(category + " installed");
         }
         else
         {
            bomset.removeAll(realset);
            l.warn(category + " has missing or non-matching files");
            Iterator<String> missing = bomset.iterator();
            while (missing.hasNext())
            {
               //FB: BC_IMPOSSIBLE_CAST NC 1-17-16
               File f = new File(missing.next());
               PSFileInfo missingelement = new PSFileInfo(f,f.getParentFile().getPath()) ;
  
               String path = missingelement.getPath();
               PSFileInfo realelement = 
                  realmap.get(path);
               if (realelement == null)
               {
                  l.debug("Missing file " + path);
               } //FB: EC_BAD_ARRAY_COMPARE NC 1-17-16
               else if (! Arrays.equals(realelement.getDigest(), missingelement.getDigest()))
               {
                  l.debug("Modified file " + path);
               }
               else if (realelement.getSize() != missingelement.getSize())
               {
                  l.debug("Size does not match " + path);
               }
               else
               {
                  l.debug("Error " + path);
               }
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.rxverify.IPSVerify#generate(java.io.File,
    *      com.percussion.rxverify.PSInstallation)
    */
   public void generate(File rxdir, PSInstallation installation)
         throws NoSuchAlgorithmException, DigestException, IOException
   {
      if (rxdir == null)
      {
         throw new IllegalArgumentException("rxdir must never be null");
      }
      if (installation == null)
      {
         throw new IllegalArgumentException("installation must never be null");
      }
      m_rxdir = rxdir;
      generate2(rxdir, installation);
   }
   
   /**
    * Does the actual generation after {@link #generate(File, PSInstallation)}
    * sets the {@link #m_rxdir} instance variable
    * @throws IOException
    * @throws DigestException
    * @throws NoSuchAlgorithmException
    * @see com.percussion.rxverify.modules.IPSVerify#generate(java.io.File,
    *      com.percussion.rxverify.PSInstallation)
    */
   private void generate2(File rxdir, PSInstallation installation) 
   throws NoSuchAlgorithmException, DigestException, IOException
   {
      // Recurse into sub-directories
      File dirs[] = rxdir.listFiles();

      for (int i = 0; i < dirs.length; i++)
      {
         File file = dirs[i];
         if (file.isDirectory())
         {
            generate2(file, installation);
         }
         else
         {
            String rpath = relPath(file);
            String category = getCategory(rpath);
            if (category.equals("IGNORE") == false)
            {
               PSFileInfo fi = 
                  new PSFileInfo(file, rpath);
               installation.addFile(category, fi);
            }
         }
      }
   }

   /**
    * Generate a relative path using information about the rhythmyx directory
    * 
    * @param file a file that must exist under the same directory as specified
    *           in the instance variable {@link #m_rxdir}or an exception will
    *           be thrown
    * @return the right substring after the matching path
    */
   private String relPath(File file)
   {
      String absPath = file.getAbsolutePath();
      String rxAbsPath = m_rxdir.getAbsolutePath();

      if (absPath.startsWith(rxAbsPath) == false)
      {
         throw new IllegalArgumentException(
               "The passed file not in the rx directory " + absPath);
      }
      String rel = absPath.substring(rxAbsPath.length());
      return rel.replace('\\', '/');
   }

   /**
    * Finds the rule that matches a given passed relative path
    * 
    * @param relativePath relative path, assumed not <code>null</code> or
    *           empty
    * @return the matching category from the rules or "UNKNOWN" if nothing
    *         matches.
    */
   private String getCategory(String relativePath)
   {
      for (int i = 0; i < RULES.length; i = i + 2)
      {
         if (RULES.length <= i + 1)
         {
            throw new IllegalArgumentException(
                  "RULES array must have an even number of entries");
         }
         Pattern rule = (Pattern) RULES[i];
         if (rule.matcher(relativePath).matches())
         {
            return (String) RULES[i + 1];
         }
      }
      return "UNKNOWN";
   }

}
