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
package com.percussion.services.filter.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.catalog.IPSCatalogErrors;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSCatalogException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterService;
import com.percussion.services.filter.IPSFilterServiceErrors;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.IPSItemFilterRuleDef;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.filter.data.PSItemFilterRuleDef;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filter service manager performs CRUD operations on item filters.
 * 
 * @author dougrand
 * 
 */
@Transactional
@PSBaseBean("sys_filtermanager")
public class PSFilterManager
      implements
         IPSFilterService
{
   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }

   /**
    * Logger for filter manager
    */
   private static final Logger log = LogManager.getLogger(PSFilterManager.class);

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#createFilter(java.lang.String,
    *      java.lang.String)
    */
   public IPSItemFilter createFilter(String name, String description)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }

      return new PSItemFilter(name, description);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#loadFilter(java.util.List)
    */
   public List<IPSItemFilter> loadFilter(List<IPSGuid> ids) 
      throws PSNotFoundException
   {
      if (ids == null || ids.isEmpty())
      {
         throw new IllegalArgumentException("ids may not be null or empty");
      }
      List<IPSItemFilter> rval = new ArrayList<>();
      for (IPSGuid g : ids)
      {
         rval.add(loadFilter(g));
      }
      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#findFilterByName(java.lang.String)
    */
   public IPSItemFilter findFilterByName(String name) throws PSFilterException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      return getSession()
            .bySimpleNaturalId(PSItemFilter.class).load(name);
   }   

   /* (non-Javadoc)
    * @see com.percussion.services.filter.IPSFilterService#findFilterByID(
    * com.percussion.utils.guid.IPSGuid)
    */
   public IPSItemFilter findFilterByID(IPSGuid id) throws PSNotFoundException {
      return loadUnmodifiableFilter(id);
   }


   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#findAllFilters()
    */
   @SuppressWarnings("unchecked")
   public List<IPSItemFilter> findAllFilters()
   {
      Session s = getSession();

         Criteria c = s.createCriteria(PSItemFilter.class);
         return c.list();

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#findFilterByAuthType(int)
    */
   @SuppressWarnings("unchecked")
   public IPSItemFilter findFilterByAuthType(int authtype)
         throws PSFilterException
   {
      Session s = getSession();

         Criteria c = s.createCriteria(PSItemFilter.class);
         c.add(Restrictions.eq("legacy_authtype", authtype));
         List<PSItemFilter> results = c.list();
         if (results.isEmpty())
         {
            throw new PSFilterException(
                  IPSFilterServiceErrors.AUTHTYPE_MISSING, authtype);
         }
         return results.get(0);

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#saveFilter(com.percussion.services.filter.IPSItemFilter)
    */
   @Transactional
   public void saveFilter(IPSItemFilter filter)
   {
      if (filter == null)
         throw new IllegalArgumentException("filter may not be null");

      PSItemFilter f = (PSItemFilter) filter;

      Session session = getSession();
      try
      {
         if (f.getVersion() == null)
         {
            session.save(f);
         }
         else
         {
            PSItemFilter current = null;
            try
            {
               current = (PSItemFilter) findFilterByName(f.getName());
            }
            catch (PSFilterException e)
            {
              if(e.getErrorCode()!=IPSFilterServiceErrors.FILTER_MISSING)
              {
                 log.error("Exception finding item filter {}. Error: {}",
                         f.getName(),
                         PSExceptionUtils.getMessageForLog(e));
              }
            }
            if (current != null)
            {
               // Merge with existing
               current.merge(filter);

            } else
               session.persist(f);
         }
      }
      catch (Exception e)
      {
         log.error("Problem saving filter: {}. Error: {}" ,
                 filter.getName(),
                 PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException(e);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#deleteFilter(com.percussion.services.filter.IPSItemFilter)
    */
   public void deleteFilter(IPSItemFilter filter)
   {
      if (filter == null)
      {
         throw new IllegalArgumentException("filter may not be null");
      }

      getSession().delete(filter);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.filter.IPSFilterService#createRuleDef(java.lang.String,
    *      java.util.Map)
    */
   public IPSItemFilterRuleDef createRuleDef(String rule,
         Map<String, String> params)
   {
      if (rule == null)
      {
         throw new IllegalArgumentException("rule may not be null");
      }
      if (params == null)
      {
         throw new IllegalArgumentException("params may not be null");
      }
      IPSItemFilterRuleDef rval = new PSItemFilterRuleDef();
      rval.setRule(rule);
      for (Map.Entry<String, String> entry : params.entrySet())
      {
         rval.setParam(entry.getKey(), entry.getValue());
      }
      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#getTypes()
    */
   public PSTypeEnum[] getTypes()
   {
      return new PSTypeEnum[]
      {PSTypeEnum.ITEM_FILTER};
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#getSummaries(com.percussion.services.catalog.PSTypeEnum)
    */
   @SuppressWarnings("unchecked")
   public List<IPSCatalogSummary> getSummaries(PSTypeEnum type)
   {
      List<IPSCatalogSummary> rval = new ArrayList<>();

      Session s = getSession();

         if (type.getOrdinal() == PSTypeEnum.ITEM_FILTER.getOrdinal())
         {
            Criteria c = s.createCriteria(PSItemFilter.class);
            List<IPSItemFilter> results = c.list();
            for (IPSItemFilter f : results)
            {
               rval.add(new PSObjectSummary(f.getGUID(), f.getName(), f
                     .getName(), f.getDescription()));
            }
         }

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#loadByType(com.percussion.services.catalog.PSTypeEnum,
    *      java.lang.String)
    */
   @Transactional
   public void loadByType(PSTypeEnum type, String item)
         throws PSCatalogException
   {
      try
      {
         if (type.equals(PSTypeEnum.ITEM_FILTER))
         {
            IPSGuid guid = PSXmlSerializationHelper.getIdFromXml(
                  PSTypeEnum.ITEM_FILTER, item);
            IPSItemFilter temp;
            List<IPSGuid> guids = new ArrayList<>();
            guids.add(guid);
            temp = loadFilter(guids).get(0);

            temp.fromXML(item);
            saveFilter(temp);
         }
         else
         {
            throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type
                  .toString());
         }
      }
      catch (IOException | PSNotFoundException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.IO, e, type);
      }
      catch (SAXException | PSInvalidXmlException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.XML, e, item);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCataloger#saveByType(com.percussion.utils.guid.IPSGuid)
    */
   public String saveByType(IPSGuid id) throws PSCatalogException
   {
      try
      {

         if (id.getType() == PSTypeEnum.ITEM_FILTER.getOrdinal())
         {
            List<IPSGuid> ids = new ArrayList<>();
            ids.add(id);
            IPSItemFilter temp = loadFilter(ids).get(0);
            return temp.toXML();
         }
         else
         {
            PSTypeEnum type = PSTypeEnum.valueOf(id.getType());
            throw new PSCatalogException(IPSCatalogErrors.UNKNOWN_TYPE, type);
         }
      }
      catch (IOException | PSNotFoundException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.IO, e, id);
      }
      catch (SAXException e)
      {
         throw new PSCatalogException(IPSCatalogErrors.TOXML, e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSFilterService#findFiltersByName(String)
    */
   @SuppressWarnings("unchecked")
   public List<IPSItemFilter> findFiltersByName(String name)
   {
      List<IPSItemFilter> filters = new ArrayList<>();
      Session s = getSession();

      if (!StringUtils.isBlank(name) && !name.equals("%")) {
         try {
            filters.add(findFilterByName(name));
            return filters;
         } catch (PSFilterException e) {
            log.error("Cannot find filter",e);
         }
      }

      return s.createCriteria(PSItemFilter.class).addOrder(Order.asc("name"))
           .setCacheable(true).list();

   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSFilterService#loadFilter(IPSGuid)
    */
   public IPSItemFilter loadFilter(IPSGuid id) throws PSNotFoundException
   {
      Session session = getSession();

         IPSItemFilter filter =  session.get(PSItemFilter.class,
               id.longValue());
         if (filter == null)
            throw new PSNotFoundException(id);

         return filter;

   }

   public IPSItemFilter loadUnmodifiableFilter(IPSGuid id)
         throws PSNotFoundException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }

      return loadFilter(id);
   }
}
