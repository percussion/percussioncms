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
package com.percussion.services.content.data;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSItemSummary)) return false;
      PSItemSummary that = (PSItemSummary) o;
      return contentId == that.contentId && revision == that.revision && isRevisionLock() == that.isRevisionLock() && getContentTypeId() == that.getContentTypeId() && Objects.equals(getName(), that.getName()) && Objects.equals(getContentTypeName(), that.getContentTypeName()) && getObjectType() == that.getObjectType() && Objects.equals(getOperations(), that.getOperations());
   }

   @Override
   public int hashCode() {
      return Objects.hash(contentId, revision, isRevisionLock(), getName(), getContentTypeId(), getContentTypeName(), getObjectType(), getOperations());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSItemSummary{");
      sb.append("contentId=").append(contentId);
      sb.append(", revision=").append(revision);
      sb.append(", revisionLock=").append(revisionLock);
      sb.append(", name='").append(name).append('\'');
      sb.append(", contentTypeId=").append(contentTypeId);
      sb.append(", contentTypeName='").append(contentTypeName).append('\'');
      sb.append(", objectType=").append(objectType);
      sb.append(", operations=").append(operations);
      sb.append('}');
      return sb.toString();
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
