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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSFields;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchField;
import com.percussion.cms.objectstore.PSSearchMultiProperty;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import com.percussion.webservices.ui.data.CommunityRef;
import com.percussion.webservices.ui.data.PSSearchDef;
import com.percussion.webservices.ui.data.PSSearchDefType;
import com.percussion.webservices.ui.data.PSViewDef;
import com.percussion.webservices.ui.data.PSViewDefType;
import com.percussion.webservices.ui.data.Property;
import com.percussion.webservices.ui.data.SearchField;
import com.percussion.webservices.ui.data.SearchFieldType;
import com.percussion.webservices.ui.data.SearchViewParentCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;

/**
 * Converts between {@link com.percussion.cms.objectstore.PSSearch}
 * and {@link com.percussion.webservices.ui.data.PSSearchDef} types. 
 * Converts between {@link com.percussion.cms.objectstore.PSSearch}
 * and {@link com.percussion.webservices.ui.data.PSViewDef} types. 
 */
public class PSSearchViewConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSSearchViewConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(Class type, Object value)
   {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         if (value instanceof PSSearchDef)
            return getSearchFromClient((PSSearchDef) value);
         else
            return getViewFromClient((PSViewDef) value);            
      }
      else
      {
         PSSearch source = (PSSearch) value;

         if (type.getName().equals(PSSearchDef.class.getName()))
            return getSearchFromServer(source);
         else
            return getViewFromServer(source);
      }
   }

   /**
    * Converts a list of {@link PSSearch} objects to array of either 
    * {@link PSSearchDef} or {@link PSViewDef}.
    * Note, the framework for list to array conversion does not work for the
    * scenario described above. We use this as a workaround until the framework
    * is capable to deal with this. 
    *  
    * @param type the converted class, must be either {@link PSSearchDef} or 
    *    {@link PSViewDef}, never <code>null</code>. 
    * @param value the to be converted list, never <code>null</code>, may be
    *    empty.
    * 
    * @return the converted array, never <code>null</code>, may be empty.
    */
   public static Object convertListToArray(Class type, List<PSSearch> value)
   {
      if ( type != PSSearchDef.class && type != PSViewDef.class)
         throw new IllegalArgumentException("type must be either PSSearchDef.class or PSViewDef.class.");
      if (value == null)
         throw new IllegalArgumentException("value must not be null.");
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      
      Converter converter = factory.getConverter(PSSearch.class);
      Object result = null;
      
      if (type == PSSearchDef.class)
      {
         PSSearchDef[] searches = new PSSearchDef[value.size()];
         for (int i=0; i<value.size(); i++)
         {
            searches[i] = (PSSearchDef)converter.convert(type, value.get(i));
         }
         result = searches;
      }
      else
      {
         PSViewDef[] views = new PSViewDef[value.size()];
         for (int i=0; i<value.size(); i++)
         {
            views[i] = (PSViewDef)converter.convert(type, value.get(i));
         }
         result = views;         
      }
         
      return result;
   }   

   /**
    * Converts a search definition from objectstore to webservice object.
    * @param source the source object, assumed not <code>null</code>.
    * @return the converted object, assumed not <code>null</code>.
    */
   private PSSearchDef getSearchFromServer(PSSearch source)
   {
      PSSearchDef target = null;

      long id = (new PSDesignGuid(source.getGUID())).getValue();
      long dspFormatId = (new PSDesignGuid(new PSGuid(
            PSTypeEnum.DISPLAY_FORMAT, source.getDisplayFormatId())))
            .getValue();
      String visibleUser = null;
      if (source.hasProperty(PSSearch.PROP_USERNAME))
         visibleUser = source.getProperty(PSSearch.PROP_USERNAME);

      target = new PSSearchDef(
            id,
            source.getDescription(),
            getCommunities(source),
            getSearchFields(source),            
            getProperties(source),
            source.getName(),
            source.getLabel(),
            source.isCaseSensitive(),
            source.getMaximumResultSize(),
            getParentCategory(source),
            dspFormatId,
            source.getDisplayName(),
            source.getUrl(),
            getSearchType(source),
            source.isUserCustomizable(),
            visibleUser);

      return target;
   }

   /**
    * Creates a objectstore search definition from a webservice search def.
    * @param source the webservice search def, assumed not <code>null</code>.
    * @return the converted search definition, never <code>null</code>.
    */
   private PSSearch getSearchFromClient(
         com.percussion.webservices.ui.data.PSSearchDef source)
   {
      try
      {
         PSSearch target;
         target = new PSSearch();

         // get the UUId
         long id = (new PSDesignGuid(source.getId())).longValue();
         PSKey key = PSSearch.createKey(new String[]{String.valueOf(id)});
         long dspFormatId = (new PSDesignGuid(source.getDisplayFormatId()))
               .longValue();

         key.setPersisted(false);
         target.setLocator(key);
         target.setInternalName(source.getName());
         target.setDisplayName(source.getLabel());
         target.setDescription(source.getDescription());
         target.setCaseSensitive(source.isCaseSensitive());
         target.setMaximumNumber(source.getItemLimit());
         target.setParentCategory(getParentCategory(source.getParentCategory()));
         target.setDisplayFormatId(String.valueOf(dspFormatId));
         target.setDisplayName(source.getDisplayFormatName());
         setVisibleCommunitiesOrUser(target, source.getVisibleUser(), source
               .getProperties(), source.getCommunities());
         setProperties(target, source.getProperties());
         target.setType(getSearchType(source));

         if (source.isUserCustomizable())
            target.setUserCustomizable(true);

         setFields(target, source.getSearchFields());

         String url = source.getUrl();
         if (!StringUtils.isBlank(url))
         {
            target.setCustom(true);
            target.setUrl(url);
         }

         return target;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   /**
    * Converts a search definition from objectstore to webservice view object.
    * @param source the source object, assumed not <code>null</code>.
    * @return the converted object, assumed not <code>null</code>.
    */
   private PSViewDef getViewFromServer(PSSearch source)
   {
      PSViewDef target = null;

      long id = (new PSDesignGuid(source.getGUID())).getValue();
      long dspFormatId = (new PSDesignGuid(new PSGuid(
            PSTypeEnum.DISPLAY_FORMAT, source.getDisplayFormatId())))
            .getValue();

      target = new PSViewDef(
            id,
            source.getDescription(),
            getCommunities(source),
            getSearchFields(source),
            getProperties(source),
            source.getName(),
            source.getLabel(),
            source.isCaseSensitive(),
            source.getMaximumResultSize(),
            getParentCategory(source),
            dspFormatId,
            source.getDisplayName(),
            source.getUrl(),
            getViewType(source));

      return target;
   }
   
   /**
    * Creates a objectstore search definition from a webservice view def.
    * @param source the webservice view def, assumed not <code>null</code>.
    * @return the converted view definition, never <code>null</code>.
    */
   private PSSearch getViewFromClient(PSViewDef source)
   {
      try
      {
         PSSearch target;
         target = new PSSearch();

         // get the UUId
         long id = (new PSDesignGuid(source.getId())).longValue();
         PSKey key = PSSearch.createKey(new String[]{String.valueOf(id)});
         long dspFormatId = (new PSDesignGuid(source.getDisplayFormatId()))
               .longValue();

         key.setPersisted(false);
         target.setLocator(key);
         target.setInternalName(source.getName());
         target.setDisplayName(source.getLabel());
         target.setDescription(source.getDescription());
         target.setCaseSensitive(source.isCaseSensitive());
         target.setMaximumNumber(source.getItemLimit());
         target.setParentCategory(getParentCategory(source.getParentCategory()));
         target.setDisplayFormatId(String.valueOf(dspFormatId));
         target.setDisplayName(source.getDisplayFormatName());
         setVisibleCommunitiesOrUser(target, null, source.getProperties(),
               source.getCommunities());
         setProperties(target, source.getProperties());
         setFields(target, source.getSearchFields());

         target.setType(PSSearch.TYPE_VIEW);
         if (source.getType().getValue().equals(CUSTOM_VIEW))
         {
            target.setCustom(true);
            target.setUrl(source.getUrl());
         }

         return target;
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Converts search type from objectstore to webservice.
    * @param source the source object, assumed not <code>null</code>.
    * @return the converted type, never <code>null</code>.
    */
   private PSSearchDefType getSearchType(PSSearch source)
   {
      String type;

      if (source.isCustomSearch())
         type = CUSTOM_SEARCH;
      else if (source.isStandardSearch())
         type = STANDARD_SEARCH;
      else
         type = USER_SEARCH;

      return PSSearchDefType.fromString(type);
   }
   
   /**
    * Converts the search type from webservice to objectstore.
    *
    * @param source the webservice object, assumed not <code>null</code>.
    *
    * @return the converted objectstore search type, never <code>null</code> or
    *    empty.
    */
   private String getSearchType(PSSearchDef source)
   {
      String tgtType = source.getType().getValue();
      if (tgtType.equals(STANDARD_SEARCH))
         tgtType = PSSearch.TYPE_STANDARDSEARCH;
      else if (tgtType.equals(CUSTOM_SEARCH))
         tgtType = PSSearch.TYPE_CUSTOMSEARCH;
      else
         tgtType = PSSearch.TYPE_USERSEARCH;

      return tgtType;
   }

   /**
    * Converts search type from objectstore to webservice.
    * @param source the source object, assumed not <code>null</code>.
    * @return the converted type, never <code>null</code>.
    */
   private PSViewDefType getViewType(PSSearch source)
   {
      String type;

      if (source.isCustomView())
         type = CUSTOM_VIEW;
      else 
         type = STANDARD_VIEW;

      return PSViewDefType.fromString(type);
   }

   /**
    * Constants defined in com.percussion.webservices.ui.data.PSViewType
    */
   private static final String STANDARD_VIEW = PSViewDefType._standardView;
   private static final String CUSTOM_VIEW = PSViewDefType._customView;
   
   /**
    * Constants defined in com.percussion.webservices.ui.data.PSVDefType
    */
   private static final String STANDARD_SEARCH = PSSearchDefType._standardSearch;
   private static final String CUSTOM_SEARCH = PSSearchDefType._customSearch;
   private static final String USER_SEARCH = PSSearchDefType._userSearch;

   /**
    * Gets the visible communities from the server object.
    * @param source the server object, never <code>null</code>.
    * @return the visible communities, may be <code>null</code> if the search
    *    is only visible by a user.
    */
   protected CommunityRef[] getCommunities(PSSearch source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null.");
      
      if (source.getShowTo() == PSSearch.SHOW_TO_USER)
         return null;

      List<CommunityRef> tgtComms = new ArrayList<CommunityRef>();
      CommunityRef community;
      Map<IPSGuid, String> srcCommunities = source.getAllowedCommunities();
      if (srcCommunities != null)
      {
         for (Map.Entry<IPSGuid, String> comm : srcCommunities.entrySet())
         {
            community = new CommunityRef(
               (new PSDesignGuid(comm.getKey())).getValue(), comm.getValue());
            tgtComms.add(community);
         }
      }
      // get the community ids only
      else if (source.getShowTo() == PSSearch.SHOW_TO_COMMUNITY)
         // ignore SHOW_TO_ALL_COMMUNITIES,
         // which is handled by getProperties(PSSearch source)
      {
         String[] communityIds =
            source.getPropertyValues(PSSearch.PROP_COMMUNITY);
         if (communityIds != null)
         {
            for (String id : communityIds)
            {
               PSDesignGuid guid = new PSDesignGuid(new PSGuid(
                     PSTypeEnum.COMMUNITY_DEF, Long.parseLong(id)));
               community = new CommunityRef(guid.getValue(), "");
               tgtComms.add(community);
            }
         }
      }

      CommunityRef[] result =
         new CommunityRef[tgtComms.size()];
      tgtComms.toArray(result);
      return result;
   }

   /**
    * Gets the webservice search fields from the objectstore search fields.
    * @param source the source object, never <code>null</code>.
    * @return the converted search fields, never <code>null</code>, but may be
    *    empty.
    */
   @SuppressWarnings("unchecked")
   protected SearchField[] getSearchFields(PSSearch source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null.");
      
      List<SearchField> tgtFields =
         new ArrayList<SearchField>();
      Iterator srcFields = source.getFields();
      PSSearchField srcField;
      SearchField tgtField;
      while (srcFields.hasNext())
      {
         srcField = (PSSearchField) srcFields.next();
         List<String> srcValues = srcField.getFieldValues();
         String[] tgtValues = new String[srcValues.size()];
         srcValues.toArray(tgtValues);

         // get position
         org.apache.axis.types.UnsignedInt position =
            new org.apache.axis.types.UnsignedInt(srcField.getPosition());

         tgtField = new SearchField(
           tgtValues,
           srcField.getFieldName(),
           srcField.getDisplayName(),
           srcField.getFieldDescription(),
           getSearchFieldType(srcField),
           srcField.getOperator(),
           srcField.usesExternalOperator(),
           position,
           srcField.getMnemonic());

         tgtFields.add(tgtField);

      }

      SearchField[] result =
         new SearchField[tgtFields.size()];
      tgtFields.toArray(result);
      return result;
   }

   /**
    * Gets a wevservice field type from a objectstore search field.
    * @param srcField the source field object, asssumed not <code>null</code>.
    * @return the created field type, never <code>null</code>.
    */
   private SearchFieldType getSearchFieldType(PSSearchField srcField)
   {
      String type;
      if (srcField.isNumberValue())
         type = SearchFieldType._number;
      else if (srcField.isDateValue())
         type = SearchFieldType._date;
      else
         type = SearchFieldType._text;

      return SearchFieldType.fromString(type);
   }

   /**
    * Converts parent category from objectstore to webservice.
    * @param source the source object, never <code>null</code>.
    * @return the converted object, never <code>null</code>.
    */
   protected com.percussion.webservices.ui.data.SearchViewParentCategory getParentCategory(PSSearch source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null.");
            
      int catId = source.getParentCategory();
      String catName = null;
      for (PSSearch.ParentCategory cat : PSSearch.ParentCategory.values())
      {
         if (cat.getId() == catId)
         {
            catName = cat.getName();
            break;
         }
      }
      if (catName == null) // defaults to ALL_CONTENT if not match
         catName = PSSearch.ParentCategory.ALL_CONTENT.getName();

      return com.percussion.webservices.ui.data.SearchViewParentCategory.fromString(catName);
   }

   /**
    * Gets the webservice properties from server object.
    * @param source the server object, never <code>null</code>.
    * @return the webservice properties, never <code>null</code>, may be empty.
    */
   protected Property[] getProperties(PSSearch source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null.");
            
      List<Property> ps = new ArrayList<Property>();

      // get unknown properties
      Iterator props = source.getProperties();
      while (props.hasNext())
      {
         PSSearchMultiProperty prop = (PSSearchMultiProperty) props.next();
         String pname = prop.getName();
         if ( (!PSSearch.PROP_COMMUNITY.equals(pname)) &&
               (!PSSearch.PROP_USER_CUSTOMIZABLE.equals(pname)) &&
               (!PSSearch.PROP_USERNAME.equals(pname)))
         {
            Iterator values = prop.iterator();
            while (values.hasNext())
            {
               String value = (String) values.next();
               Property p = new Property(pname, value);
               ps.add(p);
            }
         }
      }

      // handle sys_community = -1 (all communities)
      if (source.getShowTo() == PSSearch.SHOW_TO_ALL_COMMUNITIES)
         ps.add(new Property(PSSearch.PROP_COMMUNITY,
               PSSearch.PROP_COMMUNITY_ALL));

      Property[] result = new Property[ps.size()];
      ps.toArray(result);
      return result;
   }

   /**
    * Gets the properties from the webservice to objectstore search object.
    *
    * @param target the objectstore search object, never <code>null</code>.
    * @param properties webservice property objects, may be <code>null</code>.
    */
   protected void setProperties(PSSearch target,
         com.percussion.webservices.ui.data.Property[] properties)
   {
      if (target == null)
         throw new IllegalArgumentException("target may not be null.");
      if (properties == null)
         return;
      
      for (com.percussion.webservices.ui.data.Property prop : properties)
      {
         // skip sys_community property, which is handled by
         // setVisibleCommunitiesOrUser()
         if (!PSSearch.PROP_COMMUNITY.equalsIgnoreCase(prop.getName()))
         {
            target.setProperty(prop.getName(), prop.getValue(), true);
         }
      }
   }

   /**
    * Set the visible user or communities property for the target search object
    * from the webservice search definition.
    *
    * @param target the target search object, assumed not <code>null</code>.
    * @param visibleUser the visible user, may be <code>null</code> or empty.
    * @param properties the user properties, may be <code>null</code> or empty.
    * @param communities the visible communities, not <code>null</code>, may
    *    be empty.
    */
   protected void setVisibleCommunitiesOrUser(PSSearch target,
         String visibleUser, Property[] properties, CommunityRef[] communities)
   {
      if (properties == null && communities == null)
         throw new IllegalArgumentException("properties and communities cannot be both null.");
      
      // check visible user first
      if (visibleUser != null && visibleUser.trim().length() > 0)
      {
         target.setShowTo(PSSearch.SHOW_TO_USER, visibleUser);
         return;
      }

      // must be visible communities

      boolean hasAllCommunities = false;
      // is there a property sys_community = -1 ?
      if (properties != null)
      {
         for (Property prop : properties)
         {
            if (PSSearch.PROP_COMMUNITY.equalsIgnoreCase(prop.getName())
                  && PSSearch.PROP_COMMUNITY_ALL.equals(prop.getValue()))
            {
               hasAllCommunities = true;
               break;
            }
         }
      }
      if (hasAllCommunities)
      {
         target.setShowTo(PSSearch.SHOW_TO_ALL_COMMUNITIES,
               PSSearch.PROP_COMMUNITY_ALL);
         return;
      }
      else
      {
         if (communities == null)
            throw new IllegalArgumentException(
                  "communites may not be null if sys_community = -1 does not exist in the properties.");
            
         //target.removeCommunity(PSSearch.PROP_COMMUNITY_ALL);
         if (target.doesPropertyHaveValue(PSSearch.PROP_COMMUNITY,
               PSSearch.PROP_COMMUNITY_ALL))
         {
            target.removeProperty(PSSearch.PROP_COMMUNITY,
                  PSSearch.PROP_COMMUNITY_ALL);
         }

         for (CommunityRef c : communities)
         {
            PSDesignGuid guid = new PSDesignGuid(c.getId());
            target.setShowTo(PSSearch.SHOW_TO_COMMUNITY, String.valueOf(guid
                  .longValue()));
         }
      }
   }

   /**
    * Gets the numeric value from the webservice category.
    *
    * @param category the webservice category, never <code>null</code>.
    *
    * @return the numeric value of the category in 
    *   {@link PSSearch.ParentCategory}.
    */
   protected int getParentCategory(SearchViewParentCategory category)
   {
      if (category == null)
         throw new IllegalArgumentException("category may not be null.");

      String catName = category.getValue();

      for (PSSearch.ParentCategory cat : PSSearch.ParentCategory.values())
      {
         if (cat.getName().equalsIgnoreCase(catName))
            return cat.getId();
      }
      // not possible
      throw new IllegalStateException(
            "Value of ParentCategory must be one of the names defined in PSSearch.ParentCategory.");
   }

   /**
    * Copy the search field from webservice to server objects.
    * @param target the server object, never <code>null</code>.
    * @param srcFields the webservice field objects, may be <code>null</code>.
    */
   protected void setFields(PSSearch target, SearchField[] srcFields)
   {
      int id = target.getId();
      if (id == -1)
         throw new IllegalStateException("target.getId() must not be -1.");
      
      PSSFields fields = target.getFieldContainer();
      fields.clear();

      if (srcFields == null) // there is no fields defined
         return;
      
      for (SearchField f : srcFields)
      {
         String type = f.getType().getValue();
         if (PSSearchField.TYPE_DATE.equalsIgnoreCase(type))
            type = PSSearchField.TYPE_DATE;
         else if (PSSearchField.TYPE_NUMBER.equalsIgnoreCase(type))
            type = PSSearchField.TYPE_NUMBER;
         else
            type = PSSearchField.TYPE_TEXT;

         PSSearchField tgtField = new PSSearchField(f.getName(), f
               .getLabel(), f.getMnemonic(), type, f.getDescription());
         PSKey key = PSSearchField.createKey(f.getName(), id);
         key.setPersisted(false);
         tgtField.setLocator(key);
         tgtField.setPosition(f.getPosition().intValue());

         List<String> values = new ArrayList<String>(Arrays.asList(
            f.getValues()));
         
         if (f.isExternalOperator())
            tgtField.setExternalFieldValues("CONCEPT", values);
         else
            tgtField.setFieldValues(f.getOperator(), values);

         fields.add(tgtField);
      }
   }
}
