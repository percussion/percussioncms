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
package com.percussion.server.cache;

import com.percussion.cms.objectstore.PSCmsObject;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderAcl;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.legacy.data.PSItemEntry;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains the cached folder information. It may contain skeleton
 * information initially. Other data, such as the {@link PSFolder} object will 
 * be lazily loaded later.
 */
public class PSFolderEntry extends PSItemEntry
{

   private static final Logger log = LogManager.getLogger(PSFolderEntry.class);

   /**
    * Constructs an instance from the supplied parameters.
    *
    * @param contentId
    *           the content id of the item.
    * @param name
    *           the name of the item, assume not <code>null</code> or empty.
    * @param communityId
    *           the community id of the item.
    * @param contenttypeId
    *           the content id of the item.
    * @param objectType
    *           the object type number.
    */
   public PSFolderEntry(int contentId, String name, int communityId,
         int contenttypeId, int objectType)
   {
     super(contentId, name, communityId, contenttypeId, objectType);
   }

   /**
    * Constructs a instance from the supplied folder object.
    *  
    * @param folder the folder object, never <code>null</code>.
    */
   PSFolderEntry(PSFolder folder)
   {
      super(((PSLocator) folder.getLocator()).getId(), folder.getName(), folder
            .getCommunityId(), FOLDER_CONTENT_TYPE_ID, PSCmsObject.TYPE_FOLDER);
      
      updateFolder(folder);
   }

   /**
    * Returns the folder acl of the folder item.
    *
    * @return folder acl, may be <code>null</code> if the folder does not
    *    have an Acl.
    */
   public PSFolderAcl getFolderAcl()
   {
      return m_folderAcl;
   }

   /**
    * Package protected method. Set the folder acl. The folder Acl will be 
    * set by the {@link PSItemSummaryCache} initially for the folder items that
    * have Acl properties.
    * 
    * @param acl the Acl of the folder.
    */
   void setFolderAcl(PSFolderAcl acl)
   {
      m_folderAcl = acl;
   }
   
   /**
    * Returns the folder object, which may be lazily loaded.
    * 
    * @return the folder object, may be <code>null</code> if has not been
    *    lazily loaded.
    */
   public PSFolder getFolder()
   {
      return m_folder;
   }

   /**
    * Returns the publishing file name, the same value that is described
    * in {@link PSFolder#getPubFileName()}. 
    * 
    * @return the publishing file name described above, never <code>null</code>
    *    or empty.
    */
   public String getPubFileName()
   {
      if (m_pubFileNameProperty != null && m_pubFileNameProperty.length() != 0)
         return m_pubFileNameProperty;
      else
         return getName();
   }
   
   /**
    * Get the global template property of the folder. See
    * {@link PSFolder#getGlobalTemplateProperty()} for detail.
    * 
    * @return the global template property. It may be <code>null</code> or 
    *    empty if this property is not defined in this folder.
    */
   public String getGlobalTemplateProperty()
   {
      if (m_folder == null)
         return m_globalTemplateProperty;
      else
         return m_folder.getGlobalTemplateProperty();
   }
   
   /**
    * Returns the XML representation of this folder acl instance. It is in
    * the format of:
    * <pre>
    * &lt;!ELEMENT PSXItemEntry (PSXObjectAcl? PSXFolder?)&gt;
    * </pre>
    * 
    * @param doc
    *           the document used to generate XML element, never
    *           <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (m_folderAcl == null)
      {
         return super.toXml(doc);
      }
      else
      {
         Element root = super.toXml(doc);
         root.appendChild(m_folderAcl.toXml(doc));
         if (m_folder != null)
            root.appendChild(m_folder.toXml(doc));
         return root;
      }
   }
   
   /**
    * Override derived method, set the name for folder object if exist.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null");
      
      super.setName(name);
      
      if (m_folder != null)
         m_folder.setName(name);
   }
   
   /**
    * Package protected method. Updates the folder entry with the supplied
    * folder object. 
    * 
    * @param folder the updated folder object.
    */
   void updateFolder(PSFolder folder)
   {
      // create a PSFolderAcl from a PSObjectAcl
      PSFolderAcl acl = null;
      try
      {
         int contentId = ((PSLocator)folder.getLocator()).getId();
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         acl = new PSFolderAcl(folder.getAcl().toXml(doc), contentId,
            folder.getCommunityId());
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }

      m_folderAcl = acl;
      m_communityId = folder.getCommunityId();
      PSFolderProperty prop = folder
            .getProperty(PSFolder.PROPERTY_PUB_FILE_NAME);
      if (prop == null)
         m_pubFileNameProperty = null;
      else
         m_pubFileNameProperty = prop.getValue().trim();
      
      // don't call setName() which will 'dirty' for folder object
      m_name = folder.getName(); 
      m_folder = folder;
      
   }
   
   /**
    * Sets the value of property {@link PSFolder#PROPERTY_PUB_FILE_NAME}. 
    * 
    * @param pubFileName the new value of the above property, may be 
    *    <code>null</code> or empty.
    */
   void setPubFileNameProperty(String pubFileName)
   {
      if (m_folder != null)
         throw new IllegalStateException("Cannot set a property after PSFolder is cached.");
      
      m_pubFileNameProperty = pubFileName;
      if (m_pubFileNameProperty != null)
         m_pubFileNameProperty = m_pubFileNameProperty.trim();
   }
   
   /**
    * Sets the value of the global template property. 
    * See {@link PSFolder#setGlobalTemplateProperty(String)} for detail.
    * <p>
    * This should not be called after the {@link PSFolder} object is lazily
    * cached.
    * 
    * @param template the to be set global template, may be <code>null</code> 
    *    or empty.
    */
   void setGlobalTemplateProperty(String template)
   {
      if (m_folder != null)
         throw new IllegalStateException("Cannot set a property after PSFolder is cached.");
         
      m_globalTemplateProperty = template;
      if (m_globalTemplateProperty != null)
         m_globalTemplateProperty = m_globalTemplateProperty.trim();
   }
   
   /**
    * The property value of {@link PSFolder#PROPERTY_PUB_FILE_NAME}. It may be
    * <code>null</code> or empty. There is no leading or trailing white space 
    * if not <code>null</code>. 
    */
   private String m_pubFileNameProperty = null;
   
   /**
    * The global template property, see {@link #getGlobalTemplateProperty()}
    * for its description. This will not be used after the {@link #m_folder} is
    * lazily cached. 
    */
   private String m_globalTemplateProperty = null;
      
   /**
    * The Acl of the folder. It may be <code>null</code> if the folder Acl
    * has not been set, which means the folder does not have an Acl.
    */
   private PSFolderAcl m_folderAcl = null;

   /**
    * The placeholder for a folder object. It may be <code>null</code> if
    * it item is not a folder or the folder has not been lazily loaded.
    */
   private PSFolder m_folder = null;

   /**
    * The content type id for folders.
    */
   public static int FOLDER_CONTENT_TYPE_ID = 101;

}
