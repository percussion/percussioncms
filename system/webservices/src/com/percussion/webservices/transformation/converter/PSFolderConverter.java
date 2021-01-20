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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSFolderProperty;
import com.percussion.cms.objectstore.PSObjectAcl;
import com.percussion.cms.objectstore.PSObjectAclEntry;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.content.PSFolderPropertiesProperty;
import com.percussion.webservices.content.PSFolderSecurityAclEntry;
import com.percussion.webservices.content.PSFolderSecurityAclEntryType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;

/**
 * Converts objects between the classes
 * {@link com.percussion.cms.objectstore.PSFolder} and
 * {@link com.percussion.webservices.content.PSFolder}
 * <p>
 * Note, when converting from webservice to objectore, the permissions 
 * will always be set to {@link PSObjectPermissions#ACCESS_ADMIN}. It is the
 * caller's responsibility to reset the permissions. The permissions value is
 * a transient data. See 
 * {@link com.percussion.cms.objectstore.PSFolder#getPermissions()} for detail.
 */
public class PSFolderConverter extends PSConverter
{
   /*
    * (non-Javadoc)
    *
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSFolderConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /*
    * (non-Javadoc)
    *
    * @see PSConverter#convert(Class, Object)
    */
   public Object convert(Class type, Object value) {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         com.percussion.webservices.content.PSFolder source =
            (com.percussion.webservices.content.PSFolder) value;

         int id = -1;
         if (source.getId() != null)
            id = new PSLegacyGuid(source.getId()).getContentId();
         int communityId = -1;
         if (source.getCommunity() != null)
         {
            communityId = (int) new PSGuid(PSTypeEnum.COMMUNITY_DEF, 
                  source.getCommunity().getId()).longValue();
         }
         int displayFormatId = -1;
         if (source.getDisplayFormat() != null)
         {
            displayFormatId = new PSGuid(PSTypeEnum.DISPLAY_FORMAT, 
               source.getDisplayFormat().getId()).getUUID();
         }

         // assumed full access for this transient data when converting
         // from webservice to objectstore. The caller needs to reset
         // this value as needed.
         int permissions = PSObjectPermissions.ACCESS_ADMIN;
         
         PSFolder target = new PSFolder(source.getName(), communityId, permissions,
               source.getDescription());
         if (id >= 0)
         {
            PSLocator locator = new PSLocator(id, 1);
            target.setLocator(locator);
         }
         
         if (displayFormatId != -1)
         {
            target.setDisplayFormatId(displayFormatId);
            target.setDisplayFormatName(source.getDisplayFormat().getName());
         }
         if (communityId != -1)
            target.setCommunityName(source.getCommunity().getName());
         if (! StringUtils.isBlank(source.getLocaleCode()))
            target.setLocale(source.getLocaleCode());
         target.setGlobalTemplateProperty(source.getGlobalTemplate());
         if (! StringUtils.isBlank(source.getPath()))
            target.setFolderPath(source.getPath());
         setProperties(target, source);
         setActSecurity(target, source);

         return target;
      }
      else // convert from server to webservice
      {
         PSFolder source = (PSFolder) value;
         
         // get the display format reference
         Reference displayFormat = null;
         if (source.getDisplayFormatId() >= 0)
         {
            PSDesignGuid id = new PSDesignGuid(PSTypeEnum.DISPLAY_FORMAT, 
                  source.getDisplayFormatId());
            displayFormat = new Reference(id.getValue(), 
                  source.getDisplayFormatName());
         }
         // get the community reference
         Reference community = null;
         if (source.getCommunityId() >= 0)
         {
            PSDesignGuid id = new PSDesignGuid(PSTypeEnum.COMMUNITY_DEF, 
                  source.getCommunityId());
            community = new Reference(id.getValue(), 
                  source.getCommunityName());
         }
            
         PSDesignGuid id = new PSDesignGuid(
            new PSLegacyGuid(source.getLocator()));

         com.percussion.webservices.content.PSFolder target = 
            new com.percussion.webservices.content.PSFolder(
                  getWsSecurity(source), getWsProperties(source),
                  displayFormat, community, id.getValue(), source.getName(),
                  source.getFolderPath(), source.getLocale(), 
                  source.getGlobalTemplateProperty(), 
                  source.getDescription());
                  
         return target;
      }
   }
   
   /**
    * Sets the properties from the specified webservice source object to 
    * the specified server target object.
    * 
    * @param target the target folder, assumed not <code>null</code>.
    * @param source the source folder, assumed not <code>null</code>.
    */
   private void setProperties(PSFolder target, 
         com.percussion.webservices.content.PSFolder source)
   {
      if (source.getProperties() == null)
         return;
      
      for (PSFolderPropertiesProperty prop : source.getProperties())
      {
         if (prop.getValue() != null)
         {
            target.setProperty(prop.getName(), prop.getValue(), 
                  prop.getDescription());
         }
      }
   }
   
   /**
    * Converts the properties from the objectstore to webservice objects,
    * excluding the known properties.
    * 
    * @param source the source folder, assumed not <code>null</code>.
    * 
    * @return the converted properties. It may be <code>null</code> if there
    *    is no unknown properties.
    */
   private PSFolderPropertiesProperty[] getWsProperties(PSFolder source)
   {
      List<PSFolderPropertiesProperty> tgtProps = 
         new ArrayList<PSFolderPropertiesProperty>(); 
      Iterator props = source.getProperties();
      PSFolderProperty prop;
      PSFolderPropertiesProperty tgtProp;
      while (props.hasNext())
      {
         prop = (PSFolderProperty) props.next();
         String pname = prop.getName();
         if ((!pname.equals(PSFolder.PROPERTY_DISPLAYFORMATID)) &&
               (!pname.equals(PSFolder.PROPERTY_GLOBALTEMPLATE)))
         {
            tgtProp = new PSFolderPropertiesProperty(prop.getName(), prop
                  .getValue(), prop.getDescription());
            tgtProps.add(tgtProp);
         }
      }
      if (tgtProps.isEmpty())
         return null;
      
      PSFolderPropertiesProperty[] result = 
         new PSFolderPropertiesProperty[tgtProps.size()];
      tgtProps.toArray(result);
      return result;
   }
   
   /**
    * Converts the security (or the ACL entries from the objectstore to 
    * webservice objects.
    * 
    * @param source the source folder, assumed not <code>null</code>.
    * 
    * @return the converted ACL entries. It may be <code>null</code> if there
    *    is no ACLs in the source folder.
    */
   private PSFolderSecurityAclEntry[] getWsSecurity(PSFolder source)
   {
      List<PSFolderSecurityAclEntry> tgtAclEntries = 
         new ArrayList<PSFolderSecurityAclEntry>();
      
      Iterator sourceAcls = source.getAcl().iterator();
      PSObjectAclEntry srcAcl;
      PSFolderSecurityAclEntry tgtAcl;
      PSFolderSecurityAclEntryType tgtType;
      while (sourceAcls.hasNext())
      {
         srcAcl = (PSObjectAclEntry) sourceAcls.next();
         
         // get the ACL type
         if (srcAcl.getType() == PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE)
            tgtType = PSFolderSecurityAclEntryType.role;
         else if (srcAcl.getType() == PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL)
            tgtType = PSFolderSecurityAclEntryType.virtual;
         else
            tgtType = PSFolderSecurityAclEntryType.user;
         
         tgtAcl = new PSFolderSecurityAclEntry(srcAcl.getName(), tgtType,
               srcAcl.hasReadAccess(), srcAcl.hasWriteAccess(), 
               srcAcl.hasAdminAccess());

         tgtAclEntries.add(tgtAcl);
      }
      
      if (tgtAclEntries.isEmpty())
         return null;
      
      PSFolderSecurityAclEntry[] result = 
         new PSFolderSecurityAclEntry[tgtAclEntries.size()];
      tgtAclEntries.toArray(result);
      
      return result;
   }
   
   /**
    * Sets the security or ACL entries from the specified webservice source 
    * object to the specified server target object.
    * 
    * @param target the target folder, assumed not <code>null</code>.
    * @param source the source folder, assumed not <code>null</code>.
    */
   private void setActSecurity(PSFolder target, 
         com.percussion.webservices.content.PSFolder source)
   {
      if (source.getSecurity() == null)
         return;
      
      PSObjectAclEntry aclEntry;
      PSObjectAcl tgtAcls = new PSObjectAcl();
      for (PSFolderSecurityAclEntry acl : source.getSecurity())
      {
         int permissions = PSObjectAclEntry.ACCESS_DENY;
         if (acl.isPermissionRread())
            permissions |= PSObjectAclEntry.ACCESS_READ;
         if (acl.isPermissionWrite())
            permissions |= PSObjectAclEntry.ACCESS_WRITE;
         if (acl.isPermissionAdmin())
            permissions |= PSObjectAclEntry.ACCESS_ADMIN;
         
         String srcType = acl.getType().getValue();
         int tgtType;
         if (TYPE_ROLE.equals(srcType))
            tgtType = PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE;
         else if (TYPE_VIRTUAL.equals(srcType))
            tgtType = PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL;
         else
            tgtType = PSObjectAclEntry.ACL_ENTRY_TYPE_USER;

         aclEntry = new PSObjectAclEntry(tgtType, acl.getName(), permissions);
         tgtAcls.add(aclEntry);
      }
      
      target.setAcl(tgtAcls);
   }
   
   // Constants defined in PSFolderSecurityAclEntryType
   //private final static String TYPE_USER = PSFolderSecurityAclEntryType._User;
   private final static String TYPE_ROLE =  PSFolderSecurityAclEntryType._role;
   private final static String TYPE_VIRTUAL = PSFolderSecurityAclEntryType._virtual;
}
