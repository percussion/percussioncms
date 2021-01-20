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
package com.percussion.rx.config.impl;

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.deployer.server.PSDeploymentHandler;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.tools.PSParseFragments;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Configuration definition generator. This tool can create a config
 * definition shell based on a passed in descriptor and its element
 * selections.
 * @author erikserating
 *
 */
public class PSConfigDefGenerator
{

   /**
    * Private ctor to inhibit instantiation.
    */
   private PSConfigDefGenerator()
   {
      init();
   }
   
   /**
    * Retrieve the singleton instance of the config def
    * generator.
    * @return the config def generator, never <code>null</code>.
    */
   public static PSConfigDefGenerator getInstance()
   {
      if(ms_instance == null)
         ms_instance = new PSConfigDefGenerator();
      return ms_instance;
   }
   
   /**
    * Generate a config def shell based on the passed in descriptor.
    * @param desc export descriptor, cannot be <code>null</code>.
    * @return the contents for a config def
    */
   public String generate(PSExportDescriptor desc)
   {
      if(desc == null)
         throw new IllegalArgumentException("descriptor cannot be null.");
      PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
      PSDependencyManager dm = dh.getDependencyManager();
      
      String packageName = desc.getName();
      Iterator<? extends PSDependency> it = desc.getPackages();
      StringBuilder sb = new StringBuilder();
      sb.append(ms_fragments.get("XMLHEAD"));
      while(it.hasNext())
      {
         PSDependency el = it.next();
         if(el.getObjectType().equals("Custom"))
         {
            Iterator<PSDependency> children = el.getDependencies();
            if(children.hasNext())
               el = children.next();
         }
         String oName = el.getDisplayName();
         PSTypeEnum typeEnum  = dm.getGuidType(el.getObjectType());
         if(typeEnum == null)
            continue;
         String oType = typeEnum.toString();
         String frag = getFragment(oName, oType, packageName);
         if(frag != null)
            sb.append(frag);
      }
      sb.append("</beans>");
      return sb.toString();
   }
   
   /**
    * Gets the appropriate fragment and does token replacement on 
    * it. 
    * @param objectname assumed not <code>null</code>.
    * @param objecttype assumed not <code>null</code>.
    * @param packagename assumed not <code>null</code>.
    * @return the token replaced fragment or <code>null</code> if
    * not found.
    */
   private String getFragment(
      String objectname, String objecttype, String packagename)
   {
      String fragment = ms_fragments.get(objecttype.toUpperCase());
      if (fragment == null)
         return null;
      fragment = StringUtils.replace(fragment, OBJECT_NAME_TOKEN, objectname);
      fragment = StringUtils.replace(fragment, SOLUTION_OBJECT_NAME_TOKEN,
            packagename + "." + objectname);
      return fragment;
   }
   
   /**
    * Initialize by loading fragments.
    */
   private void init()
   {
      try
      {
         parseFragmentFile();
      }
      catch (IOException e)
      {
         // FIXME Auto-generated catch block
         e.printStackTrace();
      }
   }
   
   /**
    * Parse out the file fragments.
    * @throws IOException
    */
   private void parseFragmentFile() throws IOException
   {
      String raw = getFragementFileContents();
      ms_fragments = PSParseFragments.parseContent(raw);
   }
   
   /**
    * Retrieve the fragment file.
    * @return
    * @throws IOException
    */
   public String getFragementFileContents() throws IOException
   {
      InputStream in = null;
      try
      {
         in = getClass().getResourceAsStream(FRAGMENT_FILE);
         StringBuilder out = new StringBuilder();
         byte[] b = new byte[4096];
         for (int n; (n = in.read(b)) != -1;) 
         {
             out.append(new String(b, 0, n));
         }
         return out.toString();

      }      
      finally
      {
         if(in != null)
         {
            in.close();
         }
      }      
   }   
   
   
   /**
    * Singleton instance of the generator.
    */
   private static PSConfigDefGenerator ms_instance;
   
   /**
    * Cache of config def fragments
    */
   private Map<String, String> ms_fragments;
   
   /**
    * Name of the fragment file.
    */
   private static final String FRAGMENT_FILE = 
      "PSConfigDefGeneratorFragments.txt";
   
   private static final String OBJECT_NAME_TOKEN = "${objName}";
   
   private static final String SOLUTION_NAME_TOKEN = "publisherPrefix.solutionName";

   private static final String SOLUTION_OBJECT_NAME_TOKEN = SOLUTION_NAME_TOKEN
         + ".objName";
}
