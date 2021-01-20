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
package com.percussion.rx.design.impl;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.error.PSException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.utils.guid.IPSGuid;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

public class PSSearchModel extends PSDesignModel
{

   @Override
   public Object load(IPSGuid guid)
   {
      return loadSearchByGuid(guid, true);
   }

   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return loadSearchByGuid(guid, false);
   }

   @Override
   public Object load(String name)
   {
      return loadSearchByName(name, true);
   }

   @Override
   public Object loadModifiable(String name)
   {
      return loadSearchByName(name, false);
   }

   /**
    * Loads the readonly or modifiable search for the supplied guid based on the
    * readonly flag.
    * 
    * @param guid Must not be <code>null</code> and must be a search guid.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * search.
    * @return Object search object never <code>null</code>, throws
    * {@link RuntimeException} in case of an error.
    */
   private Object loadSearchByGuid(IPSGuid guid, boolean readonly)
   {
      // As we do not have the readonly and modifiable versions of Search
      // objects we simply return modifiable object for now for both readonly
      // and modifiable.
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");

      PSSearch search = null;
      try
      {
         PSComponentProcessorProxy proxy = PSDesignModelUtils
               .getComponentProxy();
         String compType = PSSearch.getComponentType(PSSearch.class);
         Element[] elements = proxy.load(compType,
               new PSKey[] { PSDesignModelUtils
                     .getComponentKey(guid, compType) });
         if (elements.length > 0)
         {
            search = new PSSearch(elements[0]);
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }

      if (search == null)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }

      return search;

   }

   /**
    * Loads the readonly or modifiable search for the supplied name based on the
    * readonly flag.
    * 
    * @param name Must not be blank.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * search.
    * @return Object search object never <code>null</code>, throws
    * {@link RuntimeException} in case of an error.
    */
   private Object loadSearchByName(String name, boolean readonly)
   {

      // As we do not have the readonly and modifiable versions of Search
      // objects we simply return modifiable object for now for both readonly
      // and modifiable.
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name must not be null");
      }
      PSSearch search = null;
      try
      {
         PSComponentProcessorProxy proxy = PSDesignModelUtils
               .getComponentProxy();
         Element[] elements = proxy.load(PSSearch
               .getComponentType(PSSearch.class), null);
         for (Element element : elements)
         {
            PSSearch s = new PSSearch(element);
            if (name.equals(s.getName()))
            {
               search = s;
               break;
            }
         }
      }
      catch (PSException e)
      {
         String msg = "Failed to get the design object for name ({0}) of "
               + "type ({1})";
         Object[] args = { name, getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      if (search == null)
      {
         String msg = "Failed to get the design object for name ({0}) of "
               + "type ({1})";
         Object[] args = { name, getType().name() };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }

      return search;

   }

   @Override
   public IPSGuid nameToGuid(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name must not be null");
      }
      PSSearch search = (PSSearch) load(name);
      return search.getGUID();
   }

   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj must not be null");

      if (!(obj instanceof PSSearch))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      try
      {
         PSComponentProcessorProxy proxy = PSDesignModelUtils
               .getComponentProxy();
         proxy.save(new IPSDbComponent[] { ((PSSearch) obj) });
      }
      catch (Exception e)
      {
         String msg = "Failed to save the search design object ({0})";
         Object[] args = { ((PSSearch) obj).getName() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }

   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      try
      {
         PSComponentProcessorProxy proxy = PSDesignModelUtils
               .getComponentProxy();
         String compType = PSSearch.getComponentType(PSSearch.class);
         int results = proxy.delete(compType,
               new PSKey[] { PSDesignModelUtils
                     .getComponentKey(guid, compType) });
         if (results < 1)
         {
            String msg = "Failed to delete the design object for guid {0}";
            Object[] args = { guid.toString() };
            throw new RuntimeException(MessageFormat.format(msg, args));
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
   }
}
