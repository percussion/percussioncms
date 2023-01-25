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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSFileInfo;
import com.percussion.rxverify.data.PSInstallation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
   private static final Object[] RULES =
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
   @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
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
      l.info("Verify contents of installation {}" , rxdir);

      Iterator iter = installation.getFileCategories();
      while (iter.hasNext())
      {
         String category = (String) iter.next();
         List<String> bomelements = installation.getFiles(category);
         List<String> realelements = existing.getFiles(category);
         Set<String> bomset = new HashSet<>(bomelements);
         Set<String> realset = null;
         Map<String,PSFileInfo> realmap = new HashMap<>();

         if (realelements != null)
         {
            realset = new HashSet<>(realelements);
            for (String s : realset) {
               //FB: BC_IMPOSSIBLE_CAST NC 1-17-16
               File f = new File(s);
               PSFileInfo file = new PSFileInfo(f, f.getParentFile().getPath());
               realmap.put(file.getPath(), file);
            }
         }
         else
         {
            realset = new HashSet<>();
         }

         if (realelements == null || realelements.isEmpty())
         {
            l.warn( "{} missing", category );
         }
         else if (bomset.equals(realset))
         {
            l.info("{} installed",category );
         }
         else
         {
            bomset.removeAll(realset);
            l.warn("{} has missing or non-matching files",category );
            for (String s : bomset) {
               File f = new File(s);
               PSFileInfo missingelement = new PSFileInfo(f, f.getParentFile().getPath());

               String path = missingelement.getPath();
               PSFileInfo realelement =
                       realmap.get(path);
               if (realelement == null) {
                  l.debug("Missing file {}", path);
               } else if (!Arrays.equals(realelement.getDigest(), missingelement.getDigest())) {
                  l.debug("Modified file {}", path);
               } else if (realelement.getSize() != missingelement.getSize()) {
                  l.debug("Size does not match {}", path);
               } else {
                  l.debug("Error {}", path);
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
    */
   private void generate2(File rxdir, PSInstallation installation) 
   throws NoSuchAlgorithmException, DigestException, IOException
   {
      // Recurse into sub-directories
      File[] dirs = rxdir.listFiles();

      for (File file : dirs) {
         if (file.isDirectory()) {
            generate2(file, installation);
         } else {
            String rpath = relPath(file);
            String category = getCategory(rpath);
            if (!category.equals("IGNORE")) {
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

      if (!absPath.startsWith(rxAbsPath))
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
