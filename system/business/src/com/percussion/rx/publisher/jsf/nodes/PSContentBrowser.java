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
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the base class for the backing beans of browsing Folders (for a site
 * root) and browsing the Folders/Items (for testing a JEXL expression).   
 *
 * @author YuBingChen
 */
public abstract class PSContentBrowser
{
   /**
    * The class log.
    */
   private final static Logger log = LogManager.getLogger(PSContentBrowser.class);
   
   /**
    * The folder path, never <code>null</code> or empty after constructor.
    */
   private String m_path;
   
   /**
    * The folder id of the current Site Root.
    */
   private int m_folderId;
   
   /**
    * The list of child folders and/or items for the current folder id/path. 
    * This is used to cache the list, to avoid to obtain the list more than once.
    */
   private List<ChildItem> m_children;
   
   /**
    * The folder processor, set by {@link #getFolderSrv()}.
    */
   private PSServerFolderProcessor m_folderProcessor = null;
   
   /**
    * @return the folder processor, never <code>null</code>.
    */
   protected PSServerFolderProcessor getFolderSrv()
   {
      if (m_folderProcessor == null)
      {
         m_folderProcessor = PSServerFolderProcessor.getInstance();
      }
      return m_folderProcessor;
   }
   
   /**
    * @return current request object, never <code>null</code>.
    */
   protected PSRequest getRequest()
   {
      return (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST); 
      
   }
   
   /**
    * @return current folder path, never <code>null</code> or empty.
    */
   public String getPath()
   {
      return m_path;
   }

   /**
    * @return the current folder id. It is in sync with the folder path,
    * {@link #getPath()}
    */
   protected int getFolderId()
   {
      return m_folderId;
   }
   
   /**
    * Set the Site Root Path.
    * @param path the new path, never <code>null</code> or empty. It must be
    *    a valid folder path.
    */
   public void setPath(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path must not be null or empty.");
      
      int folderId = -1;
      try
      {
         folderId = getFolderSrv().getIdByPath(path);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failed to get folder id from path: {}, due to error: {}",path, e.getMessage());
         return;
      }

      if (folderId == -1)
         return;
      
      m_folderId = folderId;
      m_path = path;
      m_children = null;
   }
   
   /**
    * Set the (parent) folder to be the supplied folder.
    * 
    * @param id the ID of the new (parent) folder, assumed not <code>null</code>.
    * 
    * @return the outcome of the browser page. It may be <code>null</code>
    *    if error occurs.
    */
   public String gotoFolder(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null.");
      
      String[] paths;
      PSLegacyGuid legacyId = (PSLegacyGuid) id;
      PSLocator loc = new PSLocator(legacyId.getContentId());
      try
      {
         paths = getFolderSrv().getItemPaths(loc);
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failed to get path for folderId={}, due to error: {}", loc.getId(), e.getMessage());
         return null;
      }
      if (paths.length == 0)
      {
         log.warn("Cannot get path for folderId= {}", loc.getId());
         return null;
      }
      
      m_folderId = loc.getId();
      m_path = paths[0];
      m_children = null;
      
      return perform();
   }
   
   /**
    * @return the outcome of the current browsing page, must be defined by
    * the inherited classes.
    */
   protected abstract String perform();
   
   /**
    * Goto the parent of the current folder if there is one. 
    * Note, cannot goto parent if the current folder is "//Sites".
    *     
    * @return the outcome of this browser page. It is <code>null</code> if
    *    cannot goto parent of the current folder.
    */
   public String gotoParent()
   {

      if (m_folderId == PSFolder.ROOT_ID)
         return null; // already at the top, do nothing
      
      List<PSLocator> locPath = null;
      try
      {
         locPath =getFolderSrv().getAncestorLocators(new PSLocator(m_folderId));
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         log.error("Failed to get ancestor locators for folderid={}, due to error: {}", m_folderId, e.getMessage());
         return null;
      }
      PSLocator loc = locPath.get(locPath.size()-1);
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      return gotoFolder(mgr.makeGuid(loc));
   }
   
   /**
    * Get all child items or folders for the current (parent) folder.
    * 
    * @return the sub folders and/or items, never <code>null</code>, 
    *    may be empty.
    *    
    * @throws Exception if error occurs.
    */
   protected abstract List<ChildItem> getChildItems() throws Exception;
  
   /**
    * @return a list of child folders and/or items, never <code>null</code>, 
    * but may be empty.
    */
   @SuppressWarnings("unchecked")
   public List<ChildItem> getChildren()
   {
      if (m_children != null)
         return m_children;
      
      List<ChildItem> folders = new ArrayList<>();
      List<ChildItem> items = new ArrayList<>();
      
      try
      {
         List<ChildItem> childItems = getChildItems();
         for (ChildItem item : childItems)
         {
            if (item.isFolder())
               folders.add(item);
            else
               items.add(item);
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }

      Collections.sort(folders);
      Collections.sort(items);
      
      m_children = new ArrayList<>();
      m_children.addAll(folders);
      m_children.addAll(items);

      return m_children;
   }
   
   /**
    * This is called by the "Go" command button, used to let the framework
    * validate the input field of the Site Root Path and let the new value
    * take effect in the browsing activity.
    * 
    * @return the outcome of the browser page.
    */
   public String gotoFolder()
   {
      return perform();
   }

   /**
    * Performs on a child item or folder. The default behavior is to browsing
    * a sub folder. However, the inherited class must override this if it need
    * to perform or select an item.
    *  
    * @param sum the child item or folder, may not be <code>null</code>.
    * 
    * @return the outcome of the targeted page, never <code>null</code> or empty.
    */
   protected String childPerform(ChildItem item)
   {
      if (item == null)
         throw new IllegalArgumentException("item must not be null.");
      
      return gotoFolder(item.mi_id);
   }

   /**
    * This is the backing bean for a sub folder or item of the current
    * Folder Path, {@link #getPathBrowser()}.
    */
   public class ChildItem implements Comparable<ChildItem>
   {
      /**
       * The id of the item or folder, never <code>null</code> after ctor.
       */
      private IPSGuid mi_id;
      
      /**
       * The name of the item or folder, never <code>null</code> or empty
       * after ctor.
       */
      private String mi_name;
      
      /**
       * Determines if the child item is a folder or item. 
       * It is <code>true</code> if it is a folder.
       */
      private boolean mi_isFolder;
      
      /**
       * Creates an instance of the sub folder.
       * @param sum the summary of the sub folder, assumed not <code>null</code>.
       */
      public ChildItem(IPSGuid id, String name, boolean isFolder)
      {
         if (id == null)
            throw new IllegalArgumentException("id may not be null.");
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name may not be null or empty.");
         
         mi_id = id;
         mi_name = name;
         mi_isFolder = isFolder;
      }
      
      /**
       * See {@link Comparable#compareTo(Object)}
       */
      public int compareTo(ChildItem other)
      {
         return mi_name.compareTo(other.mi_name);
      }
      
      /**
       * @return the name of the sub folder or item, 
       * never <code>null</code> or empty.
       */
      public String getName()
      {
         return mi_name;
      }
      
      /**
       * @return <code>true</code> if the item is a folder.
       */
      public boolean isFolder()
      {
         return mi_isFolder;
      }
      
      /**
       * Set this folder as the parent folder for {@link #getPathBrowser}. 
       * @return the outcome of the path browser, may be <code>null</code> if
       *    error occurs.
       */
      public String perform()
      {
         return childPerform(this);
      }
   }
}
