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
package com.percussion.services.content.data;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a content object which is currently an item or folder.
 */
public class PSItemSummary
{
   /**
    * The content id.
    */
   protected int contentId;
   
   /**
    * The revision id.
    */
   protected int revision;
   
   /**
    * Revision lock of the item.
    */
   protected boolean revisionLock = false;
   
   /**
    * The item name, the <code>sys_title</code> for items, the folder name for 
    * folders, never <code>null</code> or empty.
    */
   protected String name;
   
   /**
    * The content type id.
    */
   protected int contentTypeId;
   
   /**
    * The content type name, never <code>null</code> or empty.
    */
   protected String contentTypeName;
   
   /**
    * The object type, never <code>null</code>.
    */
   protected ObjectTypeEnum objectType = ObjectTypeEnum.ITEM;
   
   /**
    * The allowed operations for this item, never <code>null</code>, may be 
    * empty.
    */
   protected Collection<OperationEnum> operations = 
      new ArrayList<>();
   
   /**
    * Use this constructor to create an item.
    * 
    * @param contentId the content id.
    * @param revision the revision id.
    * @param name the item name, not <code>null</code> or empty.
    * @param contentTypeId the items content type id.
    * @param contentTypeName the items content type name, not 
    *    <code>null</code> or empty.
    * @param revisionLock if the content item is revisionable.
    */
   public PSItemSummary(int contentId, int revision, String name, 
      int contentTypeId, String contentTypeName, boolean revisionLock)
   {
      setGUID(new PSLegacyGuid(contentId, revision));
      setName(name);
      setContentTypeId(contentTypeId);
      setContentTypeName(contentTypeName);
      if (contentTypeId == PSFolder.FOLDER_CONTENT_TYPE_ID) 
      {
         setRevisionLock(false);
         setObjectType(ObjectTypeEnum.FOLDER);
      }
      else 
      {
         setRevisionLock(revisionLock);
         setObjectType(ObjectTypeEnum.ITEM);
      }
   }
   
   /**
    * Use this constructor to create a folder.
    * 
    * @param contentId the folder id.
    * @param name the folder name, not <code>null</code> or empty.
    */
   public PSItemSummary(int contentId, String name)
   {
      setGUID(new PSLegacyGuid(contentId, -1));
      setName(name);
      setContentTypeId(PSFolder.FOLDER_CONTENT_TYPE_ID);
      setContentTypeName("Folder");
      setObjectType(ObjectTypeEnum.FOLDER);
      setRevisionLock(false);
   }

   /**
    * Should only be used by webservice converters. 
    */
   public PSItemSummary()
   {
   }
   
   /**
    * Get the item id.
    * 
    * @return the item id, never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return new PSLegacyGuid(contentId, revision);
   }
   
   /**
    * Set a new item id.
    * 
    * @param id the id, must be an instanceof <code>PSLecacyGuid</code>.
    */
   public void setGUID(IPSGuid id)
   {
      if (!(id instanceof PSLegacyGuid))
         throw new IllegalArgumentException(
            "id must be an innstanceof PSLegacyGuid");
    
      PSLegacyGuid guid = (PSLegacyGuid) id;
      contentId = guid.getContentId();
      revision = guid.getRevision();
   }
   
   /**
    * Get the content type id of this item.
    * 
    * @return the content type id.
    */
   public int getContentTypeId()
   {
      return contentTypeId;
   }
   
   /**
    * Set a new content type id.
    * 
    * @param id the new content type id.
    */
   public void setContentTypeId(int id)
   {
      contentTypeId = id;
   }
   
   /**
    * Get the item name.
    * 
    * @return the item name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Set a new item name.
    * 
    * @param name the new name, not <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      this.name = name;
   }
   
   /**
    * Get the content type name.
    * 
    * @return the content type name, never <code>null</code> or empty.
    */
   public String getContentTypeName()
   {
      return contentTypeName;
   }
   
   /**
    * Set a new content type name.
    * 
    * @param newName the new content type name, not <code>null</code> or empty.
    */
   public void setContentTypeName(String newName)
   {
      if (newName == null)
         throw new IllegalArgumentException("newName cannot be null");
      
      contentTypeName = newName;
   }
   
   
   /**
    * If the item has revision lock turned on.
    * @return never <code>null</code>.
    */
   public boolean isRevisionLock()
   {
      return revisionLock;
   }

   /**
    * @param revisionLock  never <code>null</code>.
    */
   public void setRevisionLock(boolean revisionLock)
   {
      this.revisionLock = revisionLock;
   }

   /**
    * Get the type of this object.
    * 
    * @return the object type, never <code>null</code>.
    */
   public ObjectTypeEnum getObjectType()
   {
      return objectType;
   }
   
   /**
    * Set a new object type.
    * 
    * @param type the new object type, not <code>null</code>.
    */
   public void setObjectType(ObjectTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");
      
      objectType = type;
   }
   
   /**
    * Get all allowed operations for this object.
    * 
    * @return the allowed operations, never <code>null</code>, may be empty.
    */
   public Collection<OperationEnum> getOperations()
   {
      return operations;
   }
   
   /**
    * Set new allowed operations.
    * 
    * @param operations the new operations, not <code>null</code>, may be empty.
    */
   public void setOperations(Collection<OperationEnum> operations)
   {
      if (operations == null)
         throw new IllegalArgumentException("operations cannot be null");
      
      this.operations = operations;
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }
   
   /**
    * The enumeration of all supported object types.
    */
   public enum ObjectTypeEnum
   {
      ITEM(1),
      FOLDER(2);
      
      /**
       * Get the ordinal of the enumeration.
       * 
       * @return the ordinal.
       */
      public int getOrdinal()
      {
         return mi_ordinal;
      }
      
      /**
       * Get the enumeration for the supplied ordinal.
       * 
       * @param ordinal the ordinal for which to get the enumeration.
       * @return the enumeration, never <code>null</code>.
       * @throws IllegalArgumentException if no enumeration exists for the
       *    supplied ordinal.
       */
      public static ObjectTypeEnum valueOf(int ordinal)
      {
         for (ObjectTypeEnum value : values())
            if (value.getOrdinal() == ordinal)
               return value;

         throw new IllegalArgumentException(
            "No object type is defined for the supplied ordinal.");
      }
      
      /**
       * Constructs an enumeration for the specified ordinal.
       * 
       * @param ordinal the enumeration ordinal.
       */
      private ObjectTypeEnum(int ordinal)
      {
         mi_ordinal = ordinal;
      }
      
      /**
       * Stores the enumeration ordinal.
       */
      private int mi_ordinal;
   }
   
   /**
    * The enumeration of all supported item operations.
    */
   public enum OperationEnum
   {
      NONE(0),
      READ(1),
      WRITE(2),
      TRANSITION(3),
      CHECKIN(4),
      CHECKOUT(5);
      
      /**
       * Get the ordinal of the enumeration.
       * 
       * @return the ordinal.
       */
      public int getOrdinal()
      {
         return mi_ordinal;
      }
      
      /**
       * Get the enumeration for the supplied ordinal.
       * 
       * @param ordinal the ordinal for which to get the enumeration.
       * @return the enumeration, never <code>null</code>.
       * @throws IllegalArgumentException if no enumeration exists for the
       *    supplied ordinal.
       */
      public static OperationEnum valueOf(int ordinal)
      {
         for (OperationEnum value : values())
            if (value.getOrdinal() == ordinal)
               return value;

         throw new IllegalArgumentException(
            "No operation is defined for the supplied ordinal.");
      }
      
      /**
       * Constructs an enumeration for the specified ordinal.
       * 
       * @param ordinal the enumeration ordinal.
       */
      private OperationEnum(int ordinal)
      {
         mi_ordinal = ordinal;
      }
      
      /**
       * Stores the enumeration ordinal.
       */
      private int mi_ordinal;
   }
}
