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
package com.percussion.cms.objectstore;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This class adds a few methods that are specific to Active Assembly category
 * of relationships to make it more convenient and easy to manipulate active
 * assembly relationships. All the relationship properties that are specific to
 * active assembly are exposed in this class. Note these can also be manipulated
 * from the map returned by {@link PSRelationship#getProperties()} object 
 * however, it must be avoided and may result in expected behavior. For example,
 * if one sets the sort order using the property map, it will most probably be
 * overridden by the active assembly processor.
 *
 * @author RammohanVangapalli
 */
public class PSAaRelationship extends PSRelationship
{
   /**
    * Construct a new active assembly relaionship knowing the owner locator,
    * contentid of the dependent, id of the slot the dependent belongs and the
    * variantid of the dependent.
    * 
    * @param owner locator of the parent or owner item of the relationship, must
    *           not be <code>null</code>.
    * @param dependent locator of the dependent item of the relationship, must
    *           not be <code>null</code>.
    * @param slot slot in the parent item's variant to which the dependent
    *           belongs to. Must not be <code>null</code>.
    * @param variant variant of the dependent item related to the owner item via
    *           this relationship. Must not be <code>null</code>.
    * @param config relationship config object that the slot accepts, must not
    *           be <code>null</code>.
    *           
    * @deprecated use
    *             {@link #PSAaRelationship(PSLocator, PSLocator, IPSTemplateSlot, IPSAssemblyTemplate)}
    *             instead.
    */
   public PSAaRelationship(PSLocator owner, PSLocator dependent, PSSlotType slot,
      PSContentTypeVariant variant, PSRelationshipConfig config)
   {
      super(-1, owner, dependent, config);
      setSlot(slot);
      setVariant(variant);
   }
   
   /**
    * Constructs an active assembly relationship given the relationship, slot 
    * and the variant objects this relationship is associated with. The slotid 
    * and variantid properties from the source relationship (if present) are 
    * ignored. Exception is thrown if the relationship config name of the 
    * supplied relationship does not match with that for the supplied slot. 
    * @param relationship source relationship object must not be 
    * <code>null</code>.
    * @param slot must not be <code>null</code>.
    * @param variant must not be <code>null</code>.
    * 
    * @deprecated use
    *             {@link #PSAaRelationship(PSRelationship, IPSTemplateSlot, IPSAssemblyTemplate)}
    *             instead.
    */
   public PSAaRelationship(PSRelationship relationship, PSSlotType slot,
      PSContentTypeVariant variant)
   {
      super(relationship.getId(), relationship);
      setSlot(slot);
      setVariant(variant);
   }

   /**
    * Constructs an active assembly relationship given the relationship, slot 
    * and the template objects this relationship is associated with. The slot id 
    * and template id properties from the source relationship (if present) are 
    * ignored. 
    *  
    * @param relationship source relationship object must not be 
    * <code>null</code>.
    * @param slot the slot object, must not be <code>null</code>. This is a
    *    transient object and will not be persisted with the relationship when
    *    the relationship instance is saved in the repository.
    * @param template the template object, must not be <code>null</code>. This 
    *    is a transient object and will not be persisted with the relationship 
    *    when the relationship instance is saved in the repository.
    * 
    * @throws IllegalArgumentException if the relationship config of the 
    *    supplied relationship does not contain user properties for slot id and 
    *    template id.
    * 
    * @since 6.0
    */
   @SuppressWarnings("deprecation")
   public PSAaRelationship(PSRelationship relationship, IPSTemplateSlot slot, 
         IPSAssemblyTemplate template)
   {
      this(relationship, new PSSlotType(slot), 
            new PSContentTypeVariant(template));
   }
   
   /**
    * Constructs an active assembly relationship between the specified owner
    * and dependent, assigning the specified slot and template as properties
    * of the relationship.  The relationship configuration is determined by
    * the slot.
    * 
    * @param owner locator of the parent or owner item of the relationship, must
    * not be <code>null</code>.
    * @param dependent locator of the dependent item of the relationship,
    * must not be <code>null</code>.
    * @param slot the slot that will be assigned to the relationship, not
    * <code>null</code>. 
    * @param template the template of the dependent item that will be assigned to 
    * the relationship. Must not be <code>null</code>.
    * 
    * @since 6.0
    */
   @SuppressWarnings("deprecation")
   public PSAaRelationship(PSLocator owner, PSLocator dependent,
         IPSTemplateSlot slot, IPSAssemblyTemplate template) 
   {
      super(-1, owner, dependent);
      
      if (slot == null)
         throw new IllegalArgumentException("slot may not be null");
      if (template == null)
         throw new IllegalArgumentException("template may not be null");
      
      PSRelationshipConfig config = PSRelationshipCommandHandler
            .getRelationshipConfig(slot.getRelationshipName());
      setConfig(config);
      // setSlot() must be called after the setConfig(...)
      setSlot(new PSSlotType(slot));
      setVariant(new PSContentTypeVariant(template));
   }
   
   /**
    * Constructs an instance from the specified relationship object.
    * <p>
    * Note, it is caller's responsibility to set the slot and variant objects
    * for this instance.
    * 
    * @param source the source object, may not be <code>null</code> and
    *    its category must be 
    *    {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}. The properties
    *    of both {@link IPSHtmlParameters#SYS_SLOTID} and 
    *    {@link IPSHtmlParameters#SYS_VARIANTID} must not be <code>null</code>
    *    or empty.
    */
   public PSAaRelationship(PSRelationship source)
   {
      super(source);
      
      if (! source.getConfig().getCategory().equalsIgnoreCase(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY))
      {
         throw new IllegalArgumentException(
               "The category of the source object must be "
                     + PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      }
      String slotId = getProperty(IPSHtmlParameters.SYS_SLOTID);
      if (StringUtils.isBlank(slotId))
         throw new IllegalArgumentException(
               "sys_slotid property must not be null or empty.");
      String templetId = getProperty(IPSHtmlParameters.SYS_VARIANTID);
      if (StringUtils.isBlank(templetId))
         throw new IllegalArgumentException(
               "sys_variantid property must not be null or empty.");
   }
   
   /**
    * Set the slot for the dependent itens of the the relationship.
    * @param slot slot in the parent item's variant to which the dependent
    * belongs to, must not be <code>null</code>.
    * @param config relationship config object that the slot accepts, must not
    * be <code>null</code>. This is not used.
    * 
    * @deprecated use {@link #setSlot(PSSlotType)} instead.
    */
   public void setSlot(PSSlotType slot, PSRelationshipConfig config)
   {
      if (slot == null)
         throw new IllegalArgumentException("slot must not be null");

      if (config == null)
         throw new IllegalArgumentException("config must not be null");

      setSlot(slot);
   }

   /**
    * Set the slot for the dependent item of the the relationship.
    * <p>
    * Note, the specified slot is a transient object. The association of the
    * specified slot and the relationship will be persisted when the 
    * relationship is saved in the repository, but not the slot instance itself.
    * 
    * @param slot slot in the parent item's template to which the dependent
    *    belongs to, must not be <code>null</code>.
    *    
    * @throws IllegalArgumentException if the 
    *    {@link IPSHtmlParameters#SYS_SLOTID} is not one of the registered 
    *    user properties.
    *    
    * @deprecated use {@link #setSlot(IPSTemplateSlot)} instead.
    */
   public void setSlot(PSSlotType slot)
   {
      if (slot == null)
         throw new IllegalArgumentException("slot must not be null");

      m_slot = slot;
      setProperty(IPSHtmlParameters.SYS_SLOTID, ""+slot.getSlotId());
   }
   
   /**
    * Set the slot for the dependent item of the the relationship.
    * <p>
    * Note, the specified slot is a transient object. The association of the
    * specified slot and the relationship will be persisted when the 
    * relationship is saved in the repository, but not the slot instance itself.
    * 
    * @param slot slot in the parent item's template to which the dependent
    *    belongs to, must not be <code>null</code>.
    *    
    * @throws IllegalArgumentException if the 
    *    {@link IPSHtmlParameters#SYS_SLOTID} is not one of the registered 
    *    user properties.
    */
   public void setSlot(IPSTemplateSlot slot)
   {
      if (slot == null)
         throw new IllegalArgumentException("slot must not be null");

      setSlot(new PSSlotType(slot));
   }

   /**
    * Set the variant for the dependent items in the relationships.
    * <p>
    * Note, the specified variant is a transient object. The association of the
    * specified variant and the relationship will be persisted when the 
    * relationship is saved in the repository, but not the variant instance 
    * itself.
    * 
    * @param variant the variant to set for the dependent item in the
    *    relationship, must not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if the 
    *    {@link IPSHtmlParameters#SYS_VARIANTID} is not one of the registered 
    *    user properties.
    */
   @SuppressWarnings("deprecation")
   public void setVariant(PSContentTypeVariant variant)
   {
      if (variant == null)
         throw new IllegalArgumentException("variant must not be null");
         
      m_variant = variant;
      setProperty(IPSHtmlParameters.SYS_VARIANTID, ""+variant.getVariantId());
   }
   
   /**
    * Set the template for the dependent items in the relationships.
    * <p>
    * Note, the specified template is a transient object. The association of the
    * specified template and the relationship will be persisted when the 
    * relationship is saved in the repository, but not the template instance 
    * itself.
    * 
    * @param template the template to set for the dependent item in the
    *    relationship, must not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if the 
    *    {@link IPSHtmlParameters#SYS_VARIANTID} is not one of the registered 
    *    user properties.
    */
   @SuppressWarnings("deprecation")
   public void setTemplate(IPSAssemblyTemplate template)
   {
      if (template == null)
         throw new IllegalArgumentException("template must not be null");
      
      setVariant(new PSContentTypeVariant(template));   
   }


   /**
    * Access method for the slot of the dependent item in the relationship.
    * <p>
    * Note, the returned slot is a transient object. The association of the
    * slot and the relationship will be persisted when the relationship is 
    * saved in the repository, but not the slot instance itself.
    * 
    * @return slot object for the dependent item in the relationship. 
    *    Never <code>null</code>.
    *    
    * @throws IllegalStateException if the slot object has not been set yet.
    */
   public PSSlotType getSlot()
   {
      if (m_slot == null)
         throw new IllegalStateException("The slot object has not been set.");
      
      return m_slot;
   }

   /**
    * Gets the name of the slot, where the id of the slot is a property of this
    * relationship. However, the name of the slot a transient data, and will 
    * not be persisted with this object. 
    * 
    * @return slot name, may be <code>null</code> if the slot object has not
    *   been set.
    */
   public String getSlotName()
   {
      return (m_slot == null) ? null : m_slot.getSlotName();
   }
   
   /**
    * Gets the name of the template, where the id of the template is a property
    * of this relationship. However, the name of the template is a transient 
    * data, and will not be persisted with this object. 
    * 
    * @return the template name, may be <code>null</code> if the tempate object 
    *   has not been set.
    */
   @SuppressWarnings("deprecation")
   public String getTemplateName()
   {
      return (m_variant == null) ? null : m_variant.getName();
   }
   
   /**
    * Gets the id of the slot of the dependent item in the relationship.
    *
    * @return the slot id, never <code>null</code>.
    */
   public IPSGuid getSlotId()
   {
      int id = Integer.parseInt(getProperty(IPSHtmlParameters.SYS_SLOTID));
      return new PSGuid(PSTypeEnum.SLOT, id);
   }
   
   /**
    * Gets the template id of the dependent item in the relationship.
    *
    * @return template id, never <code>null</code>.
    */
   public IPSGuid getTemplateId()
   {
      int id = Integer.parseInt(getProperty(IPSHtmlParameters.SYS_VARIANTID));
      return new PSGuid(PSTypeEnum.TEMPLATE, id);
   }

   /**
    * Access method for the variant of the dependent item in the relationship.
    * <p>
    * Note, the returned variant is a transient object. The association of the
    * variant and the relationship will be persisted when the relationship is 
    * saved in the repository, but not the variant instance itself.
    * 
    * @return variant object for the dependent item in the relationship. 
    *    Never <code>null</code>.
    *    
    * @throws IllegalStateException if the variant object has not been set yet.
    */
   @SuppressWarnings("deprecation")
   public PSContentTypeVariant getVariant()
   {
      if (m_variant == null)
         throw new IllegalStateException(
               "The variant object has not been set yet.");
      
      return m_variant;
   }

   /**
    * Sets the sort rank property of the relationship. The sort rank is the
    * position of the dependent item in the slot of its parent item.
    *
    * @param sortRank zero based number.
    */
   public void setSortRank(int sortRank)
   {
      setProperty(IPSHtmlParameters.SYS_SORTRANK, String.valueOf(sortRank));
   }

   /**
    * Gets the sort rank property of the relationship. The sort rank is the
    * position of the dependent item in the slot of its parent item.
    *
    * @return zero based number. Default to <code>0</code>.
    */
   public int getSortRank()
   {
      return Integer.parseInt(getProperty(IPSHtmlParameters.SYS_SORTRANK));
   }

   /**
    * Sets the site id property of the relationship.
    *
    * @param siteId the new site id, never <code>null</code>.
    */
   public void setSiteId(IPSGuid siteId)
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null.");

      setProperty(IPSHtmlParameters.SYS_SITEID, String.valueOf(siteId
            .longValue()));
   }

   /**
    * Gets the site id property of the relationship.
    *
    * @return the value of the site id property. It may be <code>null</code>
    *    if the site id property does not exist in this relationship.
    */
   public IPSGuid getSiteId()
   {
      String siteId = getProperty(IPSHtmlParameters.SYS_SITEID);
      if (siteId == null || siteId.trim().length() == 0)
      {
         return null;
      }
      else
      {
         int id = Integer.parseInt(siteId);
         if (id > 0)
            return new PSGuid(PSTypeEnum.SITE, id);
         else
            return null;
      }
   }

   /**
    * Sets the folder id property of the relationship.
    *
    * @param folderId the new folder id. The value of the folder id property 
    *    will be removed if it is <code>-1</code>.
    */
   public void setFolderId(int folderId)
   {
      if (folderId != -1)
      {
         setProperty(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(folderId));
      }
      else
      {
         setProperty(IPSHtmlParameters.SYS_FOLDERID, null);
      }
      
   }

   /**
    * Gets the folder id property of the relationship.
    *
    * @return the value of the folder id property. It may be <code>-1</code>
    *    if the folder id property does not exist in this relationship.
    */
   public int getFolderId()
   {
      String folderId = getProperty(IPSHtmlParameters.SYS_FOLDERID);
      if (folderId == null || folderId.trim().length() == 0)
      {
         return -1;
      }
      else
      {
         int id = Integer.parseInt(folderId);
         return id > 0 ? id : -1;
      }
   }

   /**
    * Sets the site name of the relationship. It is a transient property,
    * which will not be persisted in the repository.
    *
    * @param siteName the site name, may be <code>null</code> or empty.
    */
   public void setSiteName(String siteName)
   {
      m_siteName = siteName;
   }

   /**
    * Gets the site name of the relationship. It is a transient property,
    * which will not be persisted in the repository.
    *
    * @return site name, may be <code>null</code> or empty.
    */
   public String getSiteName()
   {
      return m_siteName;
   }

   /**
    * Sets the folder name of the relationship. It is a transient property,
    * which will not be persisted in the repository.
    *
    * @param folderName the site name, may be <code>null</code> or empty.
    */
   public void setFolderName(String folderName)
   {
      m_folderName = folderName;
   }

   /**
    * Gets the folder name of the relationship. It is a transient property,
    * which will not be persisted in the repository.
    *
    * @return folder name, may be <code>null</code> or empty.
    */
   public String getFolderName()
   {
      return m_folderName;
   }

   /**
    * Sets the folder path of the relationship. It is a transient property,
    * which will not be persisted in the repository.
    *
    * @param folderPath the folder path, may be <code>null</code> or empty.
    */
   public void setFolderPath(String folderPath)
   {
      m_folderPath = folderPath;
   }

   /**
    * Gets the folder path of the relationship. It is a transient property,
    * which will not be persisted in the repository.
    *
    * @return folder path, may be <code>null</code> or empty.
    */
   public String getFolderPath()
   {
      return m_folderPath;
   }

   /**
    * The site name, see {@link #getSiteName()} for description.
    * Default to <code>null</code>.
    */
   private String m_siteName = null;

   /**
    * The folder name, see {@link #getFolderName()} for description.
    * Default to <code>null</code>.
    */
   private String m_folderName = null;

   /**
    * The folder path, see {@link #getFolderPath()} for description.
    * Default to <code>null</code>.
    */
   private String m_folderPath = null;
   
   /**
    * Just like {@link PSRelationship#getAllUserProperties()}, except it
    * excludes Active Assembly specific user properties.
    * 
    * @return the user properties without Active Assembly specific properties,
    *   never <code>null</code>.
    *   
    * @see com.percussion.design.objectstore.PSRelationship#getAllUserProperties()
    */
   public List<PSRelationshipPropertyData> getAllAaUserProperties()
   {
      List<PSRelationshipPropertyData> result = 
         new ArrayList<PSRelationshipPropertyData>(
               super.getAllUserProperties().size());
      for (PSRelationshipPropertyData prop : super.getAllUserProperties())
      {
         if (! isKnownProperty(prop.getName()))
            result.add(prop);
      }
      return result;
   }

  /**
   * Determines if the specified name is one of the predefined (known) 
   * user property of the Active Assembly relationship.
   * 
   * @param pname the property name in question, may be <code>null</code>
   *  or empty.
   * 
   * @return <code>true</code> if the specified name is one of the predefined
   *  user properties; <code>false</code> otherwise.
   */
  private boolean isKnownProperty(String pname)
  {
     for (String name : ms_knownUserProps)
     {
        if (name.equalsIgnoreCase(pname))
           return true;
     }

     return false;
  }

   /**
    * List of relationship poperties that are specific to Active Assembly
    * category of relationships. These are are exposed to the implementer by
    * names and user never should need to manipulate as relationship propertes
    * from the base class.
    */
   private static String[] ms_knownUserProps = new String[] 
   {
      IPSHtmlParameters.SYS_SLOTID,
      IPSHtmlParameters.SYS_VARIANTID,
      IPSHtmlParameters.SYS_SORTRANK,
      IPSHtmlParameters.SYS_SITEID,
      IPSHtmlParameters.SYS_FOLDERID
   };

   /**
    * Reference to the slot object corresponding to the slotid this active
    * assembly relationship is associated with. Set in the constructor or
    * whenever the slis set.
    * <p>
    * Note, the slot is a transient object. The association of the
    * slot and the relationship will be persisted when the relationship is 
    * saved in the repository, but not the slot instance itself.
    * 
    * @see #PSAaRelationship(PSLocator, PSLocator, PSSlotType, PSContentTypeVariant, PSRelationshipConfig)
    * @see PSAaRelationship#setSlot(PSSlotType, PSRelationshipConfig)
    */
   @SuppressWarnings("deprecation")
   private PSSlotType m_slot  = null;

   /**
    * Reference to the variant object corresponding to the variantid this active
    * assembly relationship is associated with. Set in the constructor or
    * whenever the avariantid is set.
    * <p>
    * Note, the variant is a transient object. The association of the
    * variant and the relationship will be persisted when the relationship is 
    * saved in the repository, but not the variant instance itself.
    * 
    * @see #PSAaRelationship(PSLocator, PSLocator, PSSlotType, PSContentTypeVariant, PSRelationshipConfig)
    * @see #setVariant(PSContentTypeVariant)
    */
   @SuppressWarnings("deprecation")
   private PSContentTypeVariant m_variant = null;
   
   /**
    * Generated serial #
    */
   private static final long serialVersionUID = -2373447152305534692L;
   
   
}
