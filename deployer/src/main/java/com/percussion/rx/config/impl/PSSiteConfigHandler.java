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
package com.percussion.rx.config.impl;

import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The handler for configuring Site objects.
 *
 * @author YuBingChen
 */
public class PSSiteConfigHandler extends PSObjectConfigHandler
{
   public PSSiteConfigHandler()
   {
      m_siteNames.put(NAMES, null);
   }
   
   @Override
   public boolean equals(Object otherObj)
   {
      if ((!(otherObj instanceof PSSiteConfigHandler) || (!super
            .equals(otherObj))))
      {
         return false;
      }
      
      PSSiteConfigHandler other = (PSSiteConfigHandler) otherObj;
      return new EqualsBuilder().append(m_siteNames, other.m_siteNames)
            .isEquals();
   }
      
   @Override
   public int hashCode()
   {
      return super.hashCode()
            + new HashCodeBuilder().append(m_siteNames).toHashCode();
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.config.IPSConfigHandler#getType()
    */
   @Override
   public PSTypeEnum getType()
   {
      return PSTypeEnum.SITE;
   }

   /**
    * Sets the type enum of the design object wired by spring.
    */
   @Override
   public void setType(PSTypeEnum type)
   {
      if (!type.equals(PSTypeEnum.SITE))
         throw new IllegalArgumentException("type must be SITE.");
   }
   
   /*
    * //see base interface method for details
    */
   @Override
   public void setName(String name)
   {
      if (getNames() != null)
         throw new IllegalStateException("Cannot set both name and names");
      
      super.setName(name);
   }

   /**
    * Gets a list of Site names. This may be the {@link #NAMES} property of the 
    * handler that is wired by Spring bean file.
    * 
    * @return a list of Site names as a {@link List List&lt;String>} type or it 
    * may be a string that contains a ${place-holder}. It may be 
    * <code>null</code> if the {@link #setNames(Object)} has never been called
    * or wired by Spring framework.
    */
   public Object getNames()
   {
      return m_siteNames.get(NAMES);
   }
   
   /**
    * Sets the name of the design objects. This is used when there are more
    * than one design objects for the handler to process or apply the properties
    * with. This cannot be used in conjunction with {@link #setName(String)}.
    *  
    * @param names the names of the design objects. It is either a 
    * {@link List List&lt;String>} type or a string that contains a 
    * ${place-holder}. It may not be <code>null</code> or empty.
    */
   public void setNames(Object names)
   {
      if (getName() != null)
         throw new IllegalStateException("Cannot set both names and name.");
      
      m_siteNames.put(NAMES, names);      
   }

   /*
    * //see base class method for details
    */
   @Override
   public List<PSPair<Object, ObjectState>> getDesignObjects(
         Map<String, Object> cachedObjs)
   {
      List<PSPair<Object, ObjectState>> result = new ArrayList<PSPair<Object, ObjectState>>();
      for (PSPair<String, ObjectState> name : getObjectNames())
      {
         Object site = getSite(name.getFirst(), cachedObjs, getSiteModel());
         result.add(new PSPair<Object, ObjectState>(site, name.getSecond()));
      }
      return result;
   }

   @Override
   public Object getDefaultDesignObject(Map<String, Object> cachedObjs)
   {
      Object site = null;
      String siteName = getName();
      // if the name property is blank get the first available name from site
      // names object.
      if (StringUtils.isBlank(siteName))
      {
         Object namesObj = getNames();
         // If the names object value is a replacement value then get all site
         // names by turning it into *
         if ((namesObj instanceof String)
               && ((String) namesObj).contains("${"))
         {
            namesObj = "*";
         }
         Collection<String> names = PSConfigUtils.getObjectNames(namesObj,
               getSiteModel(), NAMES);
         if (names != null && !names.isEmpty())
            siteName = names.iterator().next();
      }
      if (StringUtils.isNotBlank(siteName))
      {
         site = getSite(siteName, cachedObjs, getSiteModel());
      }
      return site;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   protected Collection<String> getCurNames()
   {
      Collection<String> names;
      if (StringUtils.isNotBlank(getName()))
      {
         names = new ArrayList<String>();
         names.add(getName());
      }
      else
      {
         names = PSConfigUtils.getObjectNames(getNames(), getSiteModel(), NAMES);
      }
      return names;
   }

   /*
    * //see base class method for details
    */
   @Override
   protected Collection<String> getPrevNames()
   {
      Collection<String> names = null;
      if (StringUtils.isNotBlank(getName()))
      {
         names = new ArrayList<String>();
         names.add(getName());
      }
      else
      {
         Map<String, Object> prevProps = getPrevExtraProperties();
         if (prevProps != null && (!prevProps.isEmpty()))
         {
            names = PSConfigUtils.getObjectNames(prevProps.get(NAMES),
                  getSiteModel(), NAMES);
         }
      }
      return names;
   }
   
   /**
    * Gets the Location Scheme model.
    * @return the model, never <code>null</code>.
    */
   private IPSDesignModel getSiteModel()
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      return factory.getDesignModel(PSTypeEnum.SITE);
   }

   /**
    * This method does the actual work for {@link #getDesignObjects(Map)}.
    * The cached object map will be updated by this method.
    * 
    * @param ctxName the context name, assumed not <code>null</code> or empty.
    * @param cachedObjs the cached Location Scheme objects, never 
    * <code>null</code>, may be empty.
    * @param model the Location Scheme model, never <code>null</code>.
    * 
    * @return the cached, loaded or created Location Scheme, never 
    * <code>null</code>.
    */
   private Object getSite(String name,
         Map<String, Object> cachedObjs, IPSDesignModel model)
   {
      if (cachedObjs.get(name) != null)
         return cachedObjs.get(name);
      
      IPSSite site = (IPSSite) model.loadModifiable(name);
      cachedObjs.put(name, site);
      return site;
   }

   /*
    * //see base class method for details
    */
   @Override
   public boolean isGetDesignObjects()
   {
      return true;
   }
   
   /*
    * //see base class method for details
    */
   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> getExtraProperties()
   {
      return m_siteNames;
   }

   /*
    * //see base class method for details
    */
   @Override
   public void setExtraProperties(Map<String, Object> props)
   {
      m_siteNames.putAll(props);
   }

   /**
    * Holds the {@link #NAMES} property, the map key is {@link #NAMES}, the
    * value is a list of Site names, a string with pattern names, or 
    * <code>null</code> if the property is not defined. 
    */
   private Map<String, Object> m_siteNames = new HashMap<String, Object>();
   
   /**
    * The list of Site names property. See {@link #getNames()} for detail.
    */
   public static final String NAMES = "names";
}
