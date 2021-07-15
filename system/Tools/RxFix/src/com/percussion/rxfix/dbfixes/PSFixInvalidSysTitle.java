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
package com.percussion.rxfix.dbfixes;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.rxfix.IPSFix;
import com.percussion.server.PSServer;
import com.percussion.server.cache.PSCacheException;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSCacheProxy;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Detect items and folders where the sys title does not match replacement rules
 * 
 */
public class PSFixInvalidSysTitle extends PSFixDBBase implements IPSFix
{
   private static final String ASSET_FOLDER = "/Root/Folders/$System$/Assets";

   private static final String SITE_ROOT = "/Root/Sites/";

   private static final String ASSET_ROOT = "/Root/Folders/$System$";

   private static final Set<String> publishableTypeNames = new HashSet<String>(Arrays.asList(new String[]
   {"percFileAsset", "percImageAsset", "percFlashAsset", "percPage"}));

   /**
    * The log4j logger used for this class.
    */
   private static final Logger log = LogManager.getLogger(PSFixInvalidSysTitle.class);

   /**
    * Ctor
    * 
    * @throws SQLException
    * @throws NamingException
    */
   public PSFixInvalidSysTitle() throws NamingException, SQLException
   {
      super();
   }

   PSStringTemplate ms_findSysTitleChar = new PSStringTemplate("SELECT CONTENTID,TITLE FROM {schema}."
         + IPSConstants.CONTENT_STATUS_TABLE + " WHERE TITLE like ? ESCAPE '$'");

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);
      
      if (PSCacheManager.isAvailable()) {
         PSCacheManager cacheManager = PSCacheManager.getInstance();
         cacheManager.flush();
         PSCacheProxy.flushFolderCache();
      }

      // Run query on contentstatus table to find any item that does not invalid
      // character rule.
      Set<Integer> items = getIdsToFix();

      if (items.size() == 0)
      {
         logInfo(null, "No change required");
         return;
      }

      Set<Long> pubTypeIds = getPublishableTypeIds();

      PSServerFolderProcessor proc = PSServerFolderProcessor.getInstance();
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      // group changes by folder, This way we can make sure we do not duplicate
      // a name in a folder

      HashMap<Integer, Set<Integer>> folders = getChangeItemFolderGroups(items, proc);
      // Store the list of folder ids (path heierarchy) for each item.
      Map<Integer, List<PSLocator>> folderPaths = new HashMap<>();

      // store the item id with its old and new name.
      Map<Integer, ChangeItem> itemChangeNameMap = new HashMap<>();

      //Create new names based other items in folders
      calculateUniqueItemNames(proc, objMgr, folders, folderPaths, itemChangeNameMap);

      HashMap<String, List<ChangeItem>> siteChangeList = new HashMap<String, List<ChangeItem>>();

      // Calculate new and old folder paths
      createFolderPaths(pubTypeIds, proc, objMgr, folders, itemChangeNameMap, siteChangeList);

      // Create CSV
      if (!preview && siteChangeList.size() > 0)
      {
         for (String key : siteChangeList.keySet())
         {
            createOrAppendToCsv(key, siteChangeList.get(key));
         }

      }

      // log all changes
      for (ChangeItem item : itemChangeNameMap.values())
      {
         String oldPath = item.oldPath;
         String newPath = item.newPath;
         if (preview)
         {
            log.warn("Preview mode.  Would change invalid filepath " + oldPath + " to " + newPath);
            logInfo(String.valueOf(item.summary.getContentId()), "Would Change path " + oldPath + " to " + newPath);
         }
         else
         {
            log.warn("Changing invalid filepath " + oldPath + " to " + newPath);
            logInfo(String.valueOf(item.summary.getContentId()), "Changed path " + oldPath + " to " + newPath);

         }

      }

      // Do the actual update
      if (!preview)
      {
         updateComponentSummaries(objMgr, itemChangeNameMap);
      }

   }

   /**
    * 
    * Create new names for items looking at other items in the same folder.  Items not in any folder we can just
    * change,  If file or folder already exists a number is added to create uniqueness.
    * 
    * @param proc
    * @param objMgr
    * @param folders
    * @param folderPaths
    * @param itemChangeNameMap
    * @throws PSCmsException
    */
   private void calculateUniqueItemNames(PSServerFolderProcessor proc, IPSCmsObjectMgr objMgr,
         HashMap<Integer, Set<Integer>> folders, Map<Integer, List<PSLocator>> folderPaths,
         Map<Integer, ChangeItem> itemChangeNameMap) throws PSCmsException
   {
      // Calculate new names for items
      for (Entry<Integer, Set<Integer>> folderEntries : folders.entrySet())
      {
         int testFolder = folderEntries.getKey();

         // items not in a folder can be renamed without conflict
         if (testFolder == -1)
         {

            for (Integer item : folderEntries.getValue())
            {
               folderPaths.put(item, new ArrayList<PSLocator>());

               PSComponentSummary compSummary = objMgr.loadComponentSummary(item);
               ChangeItem ci = calculateNewName(compSummary, new HashSet<String>());
               itemChangeNameMap.put(item, ci);
            }
         }
         else
         {
            PSLocator folderLocator = new PSLocator(testFolder);

            // Update ancestor folder ids for folder.

            // get current child items to check uniqueness.
            // set will be updated as we process each item.
            Set<String> namesInFolder = new HashSet<String>();
            PSComponentSummary[] folderItems = proc.getChildSummaries(folderLocator);
            for (PSComponentSummary folderItem : folderItems)
            {
               namesInFolder.add(folderItem.getName());
            }

            // Calculate new names
            for (PSComponentSummary folderItem : folderItems)
            {
               if (folderEntries.getValue().contains(folderItem.getContentId()))
               {
                  namesInFolder.remove(folderItem.getName());

                  ChangeItem ci = calculateNewName(folderItem, namesInFolder);

                  itemChangeNameMap.put(folderItem.getContentId(), ci);
               }
            }

         }
      }
   }

   /**
    * 
    * Calculate the full folder paths of changed items, taking into account any ancestor folders 
    * 
    * @param pubTypeIds
    * @param proc
    * @param objMgr
    * @param folders
    * @param itemChangeNameMap
    * @param siteChangeList
    * @throws PSCmsException
    */
   private void createFolderPaths(Set<Long> pubTypeIds, PSServerFolderProcessor proc, IPSCmsObjectMgr objMgr,
         HashMap<Integer, Set<Integer>> folders, Map<Integer, ChangeItem> itemChangeNameMap,
          HashMap<String, List<ChangeItem>> siteChangeList)
         throws PSCmsException
   {
      Map<Integer, String> itemNonChangedName = new HashMap<Integer, String>();
      
      for (Entry<Integer, Set<Integer>> folderEntries : folders.entrySet())
      {

         StringBuilder origFolderPath = new StringBuilder();
         StringBuilder newFolderPath = new StringBuilder();

         int testFolder = folderEntries.getKey();

         if (testFolder > 0)
         {
            // items not in a folder will not get path here.
            PSLocator folderLocator = new PSLocator(testFolder);
            List<PSLocator> ancestors = proc.getAncestorLocators(folderLocator);
            ancestors.add(folderLocator);
            calculateFolderPath(objMgr, itemChangeNameMap, itemNonChangedName, ancestors, origFolderPath, newFolderPath);
         }

         for (Integer item : folderEntries.getValue())
         {

            ChangeItem ci = itemChangeNameMap.get(item);

            String oldPath = origFolderPath.toString() + "/" + ci.summary.getName();
            String newPath = newFolderPath.toString() + "/" + ci.newName;
            ci.oldPath = oldPath;
            ci.newPath = newPath;

            String siteName = StringUtils.substringBetween(oldPath, SITE_ROOT, "/");
            boolean isAsset = oldPath.startsWith(ASSET_FOLDER);
            boolean publishable = false;

            if (StringUtils.isNotEmpty(siteName))
            {
               String oldSitePath = "/" + StringUtils.substringAfter(oldPath, SITE_ROOT + siteName + "/");
               String newSitePath = "/" + StringUtils.substringAfter(newPath, SITE_ROOT + siteName + "/");
               ci.newSitePath = newSitePath;
               ci.oldSitePath = oldSitePath;
               // percEs- are hidden external link folders
               if (!oldSitePath.startsWith("/.system") && !oldSitePath.contains("percEs-"))
                  publishable = true;
            }

            if (isAsset)
            {

               ci.oldSitePath = StringUtils.substringAfter(oldPath, ASSET_ROOT);
               ci.newSitePath = StringUtils.substringAfter(newPath, ASSET_ROOT);

               siteName = "asset";
               publishable = pubTypeIds.contains(ci.summary.getContentTypeId()) || ci.summary.isFolder();

            }

            if (ci.summary.isFolder())
            {
               ci.oldSitePath += "/";
               ci.newSitePath += "/";
            }

            if (publishable)
               addChangeItem(siteChangeList, ci, siteName);

         }

      }
   }

   /**
    * @param key
    * @param list
    */
   private void createOrAppendToCsv(String key, List<ChangeItem> list)
   {
      BufferedWriter pw = null;
      String filename = "";
      try
      {
         filename = "logs/redirects/" + key + "_redirect.csv";

         File file = new File(PSServer.getRxDir(), filename);

         boolean newFile = false;
         if (!file.exists())
         {
            file.getParentFile().mkdirs();
            file.createNewFile();
            newFile = true;

         }

         pw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));

         if (newFile)
         {
            pw.write("category,condition,redirectTo");
            pw.newLine();
         }
         Collections.sort(list);
         for (ChangeItem item : list)
         {
            pw.write(csvEscape("AUTOGEN") + "," + csvEscape(item.oldSitePath) + "," + csvEscape(item.newSitePath));
            pw.newLine();
         }
         pw.flush();
         pw.close();

      }
      catch (IOException e)
      {
         log.error("Cannot write to csv file " + filename, e);
      }
      finally
      {
         if (pw != null)
            try {
               pw.close();
            } catch(Exception e){
               //NOOP
            }
      }
   }
   
   /**
    * Wraps string with a double quote.  Any double quotes in the string
    * are escaped with a double quote.  Matches Excel format.
    * @param string
    * @return the escaped string
    */
   private String csvEscape(String string)
   {
      return "\""+ string.replace("\"", "\"\"") + "\"";
   }
   /**
    * @param siteChangeList
    * @param ci
    * @param name
    */
   private void addChangeItem(HashMap<String, List<ChangeItem>> siteChangeList, ChangeItem ci, String name)
   {
      List<ChangeItem> assetChangeList = siteChangeList.get(name);
      if (assetChangeList == null)
      {
         assetChangeList = new ArrayList<ChangeItem>();
         siteChangeList.put(name, assetChangeList);
      }
      assetChangeList.add(ci);
   }

   private Set<Long> getPublishableTypeIds()
   {
      Set<Long> typeIds = new HashSet<Long>();
      PSItemDefManager defMgr = PSItemDefManager.getInstance();

      for (String typeName : publishableTypeNames)
      {
         try
         {
            long id = defMgr.contentTypeNameToId(typeName);
            typeIds.add(id);
         }
         catch (PSInvalidContentTypeException e)
         {
            log.error("Cannot find get type " + typeName, e);
         }
      }
      return typeIds;

   }

   /**
    * @param objMgr
    * @param itemChangeNameMap
    */
   @SuppressWarnings("unchecked")
   private void updateComponentSummaries(IPSCmsObjectMgr objMgr, Map<Integer, ChangeItem> itemChangeNameMap)
   {
      for (ChangeItem ci : itemChangeNameMap.values())
      {
         PSComponentSummary summary = ci.summary;
         summary.setName(ci.newName);
         try
         {
            objMgr.saveComponentSummaries(Collections.singletonList(summary));
         }
         catch (Exception e)
         {
            logFailure(String.valueOf(ci.summary.getContentId()), "Failed to update component summary name");
            log.error("Failed to update component summary name for id " + ci.summary.getContentId() + " path "
                  + ci.summary.getName(), e);

         }

      }

      try
      {
         PSCacheProxy.flushFolderCache();
      }
      catch (PSCacheException e)
      {
         log.error(
               "Failed to clear the folder cache after updating invalid characters in titles. Restart Server", e);
      }
   }

   /**
    * @param objMgr
    * @param itemChangeNameMap
    * @param itemNonChangedName
    * @param ancestors
    * @param origFolderPath
    * @param newFolderPath
    */
   private void calculateFolderPath(IPSCmsObjectMgr objMgr, Map<Integer, ChangeItem> itemChangeNameMap,
         Map<Integer, String> itemNonChangedName, List<PSLocator> ancestors, StringBuilder origFolderPath,
         StringBuilder newFolderPath)
   {
      for (PSLocator ancestor : ancestors)
      {

         // get path part if name changed
         ChangeItem changeItem = itemChangeNameMap.get(ancestor.getId());
         if (changeItem != null)
         {
            origFolderPath.append('/');
            origFolderPath.append(changeItem.summary.getName());
            newFolderPath.append('/');
            newFolderPath.append(changeItem.newName);
         }
         else
         {
            // name not change, cache the value for other folders.
            String name = itemNonChangedName.get(ancestor.getId());
            if (name == null)
            {
               PSComponentSummary summary = objMgr.loadComponentSummary(ancestor.getId());
               itemNonChangedName.put(ancestor.getId(), summary.getName());
               name = summary.getName();

            }
            origFolderPath.append('/');
            origFolderPath.append(name);
            newFolderPath.append('/');
            newFolderPath.append(name);
         }
      }
   }

   /**
    * @param items
    * @param proc
    * @return
    * @throws PSCmsException
    */
   private HashMap<Integer, Set<Integer>> getChangeItemFolderGroups(Set<Integer> items, PSServerFolderProcessor proc)
         throws PSCmsException
   {
      // Need to make sure changed titles are unique in folders.
      // Group by parent folder
      HashMap<Integer, Set<Integer>> folders = new HashMap<Integer, Set<Integer>>();
      for (Integer item : items)
      {
         PSComponentSummary[] folderSummaries = proc.getParentSummaries(new PSLocator(item));
         if (folderSummaries != null && folderSummaries.length > 0)
         {
            for (PSComponentSummary folderSummary : folderSummaries)
            {
               Set<Integer> folderItemSet = folders.get(folderSummary.getContentId());
               if (folderItemSet == null)
               {
                  folderItemSet = new HashSet<Integer>();
                  folders.put(folderSummary.getContentId(), folderItemSet);
               }
               folderItemSet.add(item);
            }
         }
         else
         {
            Set<Integer> folderItemSet = folders.get(-1);
            if (folderItemSet == null)
            {
               folderItemSet = new HashSet<Integer>();
               folders.put(-1, folderItemSet);

            }
            folderItemSet.add(item);
         }
      }
      return folders;
   }

   /**
    * Do a direct sql check to find any items that have invalid characters and return the ids
    * If nothing returned here we do not need to do anything else. 
    *
    * @return
    * @throws NamingException
    * @throws SQLException
    * @throws PSStringTemplateException
    */
   private Set<Integer> getIdsToFix() throws NamingException, SQLException, PSStringTemplateException
   {
      Connection c = PSConnectionHelper.getDbConnection();
      Set<Integer> items = new HashSet<Integer>();
      try
      {

         // Find candidate records and fix them for items that should be
         // in public state

         for (int i = 0; i < IPSConstants.INVALID_ITEM_NAME_CHARACTERS.length(); i++)
         {

            char testChar = IPSConstants.INVALID_ITEM_NAME_CHARACTERS.charAt(i);
            PreparedStatement st = null;
            ResultSet rs = null;
            try
            {
               st = PSPreparedStatement.getPreparedStatement(c, ms_findSysTitleChar.expand(m_defDict));
               if (testChar == '%' || testChar == '_')
                  st.setString(1, "%$" + testChar + "%");
               else
                  st.setString(1, "%" + testChar + "%");
               rs = st.executeQuery();

               while (rs.next())
               {
                  int contentId = rs.getInt(1);
                  // String sys_title = rs.getString(2);
                  items.add(contentId);
               }
            }
            finally
            {
               try
               {
                  rs.close();
               }
               catch (Exception e)
               {
               }
               try
               {
                  st.close();
               }
               catch (Exception e)
               {
               }
            }

         }

      }
      finally
      {
         if (c != null)
            c.close();
      }
      return items;
   }

   /**
    * @param folderItem
    * @param names
    * @return
    */
   private ChangeItem calculateNewName(PSComponentSummary folderItem, Set<String> names)
   {

      String currentName = folderItem.getName();
      int counter = 1;
      String newName = PSFolderPathUtils.replaceInvalidItemNameCharacters(currentName);
      while (names.contains(newName))
      {
         newName = PSFolderPathUtils.addEnumeration(PSFolderPathUtils.replaceInvalidItemNameCharacters(currentName),
               counter++);
      }

      names.remove(currentName);
      names.add(newName);

      return new ChangeItem(folderItem, newName);
   }

   @Override
   public String getOperation()
   {
      return "Fix invalid sys_title";
   }

   /**
    * @author stephenbolton
    *
    */
   static class ChangeItem implements Comparable<ChangeItem>
   {
      private final PSComponentSummary summary;

      private final String newName;

      private String oldPath;

      private String newPath;

      private String oldSitePath;

      private String newSitePath;

      public ChangeItem(PSComponentSummary summary, String newName)
      {
         this.summary = summary;
         this.newName = newName;
      }

      public int compare(ChangeItem o1, ChangeItem o2)
      {
         return o1.oldSitePath.compareTo(o2.oldSitePath);
      }

      @Override
      public int compareTo(ChangeItem o)
      {
         return oldSitePath.compareTo(o.oldSitePath);
      }

   }
}
