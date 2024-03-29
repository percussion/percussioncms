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
package com.percussion.fastforward.sfp;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Class defines some useful static methods related a Site.
 * 
 * @author James Schultz
 */
public class PSSite
{
   /**
    * Uses the site id parameter to lookup the site folder root path stored
    * in the RXSITES table.
    * @param siteid Id of the site whose site folder root will be returned. if
    * provided null, null will be returned
    * @param request
    * @return the site folder root of the supplied site, may be null or empty.
    */
   public static String lookupFolderRootForSite(
      String siteid,
      IPSRequestContext request) 
   {
      String folderRoot = null;

      if (siteid != null)
      {
         // build and execute an interal request
         Map lookupParams = new HashMap(1);
         lookupParams.put(IPSHtmlParameters.SYS_SITEID, siteid);
         IPSInternalRequest lookupRequest =
            request.getInternalRequest(
               LOOKUP_SITE_FOLDER_ROOT,
               lookupParams,
               false);
         if (lookupRequest == null)
         {
            request.printTraceMessage(
               "ERROR: cannot find query resource: " + LOOKUP_SITE_FOLDER_ROOT);
         }
         else
         {
            try
            {
               Document results = lookupRequest.getResultDoc();
               folderRoot = parseFolderRootForSite(results);
            }
            catch (PSInternalRequestCallException e)
            {
               request.printTraceMessage(
                  "ERROR: while making internal request to "
                     + LOOKUP_SITE_FOLDER_ROOT);
               request.printTraceMessage(e.getMessage());
            }
         }
      }
      return folderRoot;
   }

   /**
    * Parses XML document to extract the site folder root from the following
    * structure:<br>
    * <pre><code>
    * &lt;!ELEMENT lookupSiteFolderRoot (folderPath?)>
    * &lt;!ELEMENT folderPath (#PCDATA)>
    * </code></pre>
    * @param resultXml the XML document to be parsed
    * @return the site folder root, may be null or empty
    */
   public static String parseFolderRootForSite(Document resultXml)
   {
      String folderRoot = null;
      if (resultXml != null)
      {
         Element root = resultXml.getDocumentElement();
         if (root != null)
         {
            NodeList path = root.getElementsByTagName("folderPath");
            Node n = path.item(0);
            if (n != null)
               folderRoot = PSXmlTreeWalker.getElementData(n);
         }
      }
      return folderRoot;
   }

   /**
    * Gets the published filename for the supplied folder locator. If the 
    * folder property named {@link PSFolder#PROPERTY_PUB_FILE_NAME} is present, 
    * its value will be used as the file name for this folder. If this 
    * property is not defined, the folder name is returned.
    * 
    * @param helper relationship helper class object, must not be
    *           <code>null</code>.
    * @param locator the locator for the folder, must not be <code>null</code>.
    * @return the file name for this folder as described above, never
    *         <code>null</code> or empty.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public static String getFolderFileName(
         PSLocator locator) throws PSCmsException
   {
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }

      return PSServerFolderProcessor.getInstance().getPubFileName(locator.getId());
   }


   /**
    * Builds the folder path walking backwards from the selected site folder to
    * the site root. An empty list is returned if the <code>locator</code> is 
    * not a descendent of the <code>rootLoc</code> 
    * 
    * @param relHelper
    *           relationship helper class object, must not be <code>null</code>.
    * @param rootLoc
    *           the locator of the root folder for a site. Must not be 
    *           <code>null</code>.
    * @param locator
    *           the locator of an item or folder under the site folder to build
    *           path list. Must not be <code>null</code>.
    * @param addLocator <code>true</code> if add the <code>locator</code> to 
    *           the returned path; <code>false</code> don't add the 
    *           <code>locator</code> to the returned path. This is because
    *           the <code>locator</code> may not be a locator of a folder.
    * 
    * @return a list of {@link PSLocator}that represent the path. The 2nd
    *         element is the sub-folder of the 1st element, the 3nd element is
    *         the sub-folder of the 2nd element, and so on and so forth, the 
    *         last element is the <code>locator</code>. It may
    *         be empty if the <code>locator</code> is not a descendent of
    *         <code>rootLoc</code>. It never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public static List buildFolderPathList(
         int rootId, PSLocator locator, boolean addLocator)
         throws PSCmsException
   {
     
      if (rootId <=0)
         throw new IllegalArgumentException("rootid must not be > 0");
      if (locator == null)
         throw new IllegalArgumentException("locator must not be null");
      
      PSServerFolderProcessor fldProcessor = PSServerFolderProcessor.getInstance();
      List paths = fldProcessor.getFolderLocatorPaths(locator);
      Iterator pathsIt = paths.iterator();
      ListIterator walkPath;
      List path;
      PSLocator tmpLoc;
   
      while (pathsIt.hasNext())
      {
         walkPath = ((List) pathsIt.next()).listIterator();
         // collect the locators while walking the path from bottom up
         path = new ArrayList(); 
         while (walkPath.hasNext())
         {
            tmpLoc = (PSLocator) walkPath.next();
            if (tmpLoc.getId() == rootId)
            {
               Collections.reverse(path);
               if (addLocator)
                  path.add(locator);
               return path;
            }
            
            path.add(tmpLoc);
         }
      }
      
      return Collections.EMPTY_LIST;
   }
   
   /**
    * Renders the site folder path as a String. The path will always begin and
    * end with a {@link #SITE_PATH_SEPARATOR Separator}. If the list of folders
    * is empty, the returned path will consist of a single Separator.
    * 
   * @param helper relationship helper class object, must not be
    *           <code>null</code>.
    * @param siteFolderList a list of PSFolders that represent the path, must 
    * not be <code>null</code>, may be empty.
    * @return the site folder path. Never <code>null</code>.
    * @throws PSCmsException
    * 
    * @deprecated use {@link #renderSiteFolderPathLocators(List) instead.
    */
   public static String renderSiteFolderPath(
      List siteFolderList)
      throws PSCmsException
   {
      StringBuilder path = new StringBuilder();
      ListIterator it = siteFolderList.listIterator();
      while (it.hasNext())
      {
         PSFolder folder = (PSFolder) it.next();
         PSLocator loc = (PSLocator) folder.getLocator();
         path.append(SITE_PATH_SEPARATOR);
         path.append(getFolderFileName(loc));
      }
      path.append(SITE_PATH_SEPARATOR);
      return path.toString();
   }

   /**
    * Renders the site folder path as a String. The path will always begin and
    * end with a {@link #SITE_PATH_SEPARATOR Separator}. If the list of folders
    * is empty, the returned path will consist of a single Separator.
    * 
    * @param helper
    *           relationship helper class object, must not be <code>null</code>.
    * @param siteFolderList
    *           a list of {@link PSLocator}that represent the path, must not be
    *           <code>null</code>, may be empty. The 2nd element is the 
    *           sub-folder of the 1st element, the 3nd element is the sub-folder
    *           of the 2nd element, and so on and so forth. 
    * 
    * @return the site folder path. Never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   public static String renderSiteFolderPathLocators(List siteFolderList)
         throws PSCmsException
   {
      StringBuilder path = new StringBuilder();
      ListIterator it = siteFolderList.listIterator();
      PSLocator loc;
      while (it.hasNext())
      {
         loc = (PSLocator) it.next();
         path.append(SITE_PATH_SEPARATOR);
         path.append(getFolderFileName(loc));
      }
      path.append(SITE_PATH_SEPARATOR);
      return path.toString();
   }
   
   /**
    * Name of the Rhythmyx internal resource used to query the site folder root
    * for a given site id.
    */
   private static final String LOOKUP_SITE_FOLDER_ROOT =
      "rx_supportSiteFolderContentList/lookupSiteFolderRoot.xml";

   /**
    * Name of the request private object key that indicates to site folder
    * assembly that the path generation should be suppressed.
    */
   public static final String SUPPRESS_SITE_PATH_KEY =
      "ff-suppress-site-path-key";

   /**
    * String constant for the key to store the folder path as session object.
    */
   public static final String SITE_PATH_NAME =
      "com.percussion.fastforward.sfp.path";

   /**
    * String constant for path separator string used while building location
    * path.
    */
   public static final String SITE_PATH_SEPARATOR = "/";

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger ms_log = LogManager.getLogger(PSSite.class);
}
