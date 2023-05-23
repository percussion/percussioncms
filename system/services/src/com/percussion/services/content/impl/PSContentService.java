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
package com.percussion.services.content.impl;


import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentErrors;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentException;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSAutoTranslationPK;
import com.percussion.services.content.data.PSFolderProperty;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementations for all content services.
 */
@Transactional
public class PSContentService
   implements IPSContentService
{

   private static final Logger log = LogManager.getLogger(IPSConstants.CONTENTREPOSITORY_LOG);

   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }

   /* (non-Javadoc)
    * @see IPSContentService#createKeyword(String, String)
    */
   @Transactional
   public PSKeyword createKeyword(String label, String description)
   {
      if (StringUtils.isBlank(label))
         throw new IllegalArgumentException("label cannot be null or empty");
      
      List<PSKeyword> keywords = findKeywordsByLabel(label, null);
      if (!keywords.isEmpty())
         throw new IllegalArgumentException(
            "label must be unique accross all existing keywords");
      
      IPSGuidManager guidManager = PSGuidManagerLocator.getGuidMgr();
      IPSGuid id = guidManager.createGuid(PSTypeEnum.KEYWORD_DEF);
      
      PSKeyword keyword = new PSKeyword(label, description, 
         String.valueOf(id.getUUID()));
      keyword.setGUID(id);
      
      return keyword;
   }

   /* (non-Javadoc)
    * @see IPSContentService#findKeywordsByLabel(String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSKeyword> findKeywordsByLabel(String label, String sortProperty)
   {
      Session session = getSession();

         if (StringUtils.isBlank(label))
            label = "%";
         
         // get all requested keywords
         Criteria criteria = session.createCriteria(PSKeyword.class);
         criteria.add(Restrictions.like("label", label));
         criteria.add(Restrictions.eq("keywordType", String.valueOf(1)));
         if (!StringUtils.isBlank(sortProperty))
            criteria.addOrder(Order.asc(sortProperty));
         
         List<PSKeyword> keywords = filterKeywordExcludes(criteria.list());
         
         // then get all choices for each result
         for (PSKeyword keyword : keywords)
         {
            List<PSKeywordChoice> choices = loadKeywordChoices(keyword, 
               sortProperty);
            keyword.setChoices(choices);
         }
         
         return keywords;

   }

   /* (non-Javadoc)
    * @see IPSContentService#findKeywordChoices(String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSKeyword> findKeywordChoices(String type, String sortProperty)
   {
      if (StringUtils.isBlank(type))
         throw new IllegalArgumentException("type cannot be null or empty");
      
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSKeyword.class);
         criteria.add(Restrictions.eq("keywordType", type));
         if (!StringUtils.isBlank(sortProperty))
            criteria.addOrder(Order.asc(sortProperty));
         
         return criteria.list();

   }

   /* (non-Javadoc)
    * @see IPSContentService#loadKeyword(IPSGuid, String)
    */
   @SuppressWarnings("unchecked")
   public PSKeyword loadKeyword(IPSGuid id, String sortProperty) 
      throws PSContentException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      /*
       * The user is not allowed to load keywords defined in the exclude
       * list. This should never happen.
       */
      validateKeywordId(id);
      
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSKeyword.class);
         criteria.add(Restrictions.eq("id", id.longValue()));
         
         List<PSKeyword> keywords = criteria.list();
         if (keywords.isEmpty())
            throw new PSContentException(IPSContentErrors.MISSING_KEYWORD, id);
         
         PSKeyword keyword = keywords.get(0);
         List<PSKeywordChoice> choices = loadKeywordChoices(keyword, 
            sortProperty);
         keyword.setChoices(choices);
         
         return keyword;

   }

   /* (non-Javadoc)
    * @see IPSContentService#saveKeyword(PSKeyword)
    */
   public void saveKeyword(PSKeyword keyword)
   {
      if (keyword == null)
         throw new IllegalArgumentException("keyword cannot be null");

      /*
       * The user is not allowed to save keywords defined in the exclude
       * list. This should never happen.
       */
      validateKeywordId(keyword.getGUID());
      
      Session session = getSession();

         session.saveOrUpdate(keyword);
         
         List<PSKeyword> existingChoices = findKeywordChoices(
            keyword.getValue(), null);
         for (PSKeywordChoice choice : keyword.getChoices())
         {
            boolean exists = false;
            for (PSKeyword existingChoice : existingChoices)
            {
               if (choice.getLabel().equalsIgnoreCase(
                  existingChoice.getLabel()))
               {
                  existingChoice.setDescription(choice.getDescription());
                  existingChoice.setValue(choice.getValue());
                  existingChoice.setSequence(choice.getSequence());
                  
                  // update existing choice
                  session.update(existingChoice);
                  
                  existingChoices.remove(existingChoice);
                  exists = true;
                  break;
               }
            }
            
            if (!exists)
            {
               IPSGuidManager guidManager = PSGuidManagerLocator.getGuidMgr();
               IPSGuid id = guidManager.createGuid(PSTypeEnum.KEYWORD_DEF);

               // create a new choice
               PSKeyword newKeyword = keyword.createKeyword(id, choice);
               session.persist(newKeyword);
            }
         }
         
         // delete removed choices
         for (PSKeyword choice : existingChoices)
            deleteKeywordChoice(choice.getGUID());

   }

   /**
    * Deletes a keyword choice. 
    * @param id the id of the keyword implementing a keyword choice to delete.
    * Not <code>null</code>.
    */
   private void deleteKeywordChoice(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      try
      {
         final PSKeyword keyword = loadKeyword(id, null);
         if (keyword.getKeywordType().equals("1"))
         {
            throw new IllegalArgumentException(
                  "The method should be called for a keyword choice only. "
                  + "id: " + id);
         }

         getSession().delete(keyword);
      }
      catch (PSContentException e)
      {
         // ignore non existing keyword
      }
   }

   // see interface
   @Transactional
   public void deleteKeyword(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");
      
      /*
       * The user is not allowed to delete keywords defined in the exclude
       * list. This should never happen.
       */
      validateKeywordId(id);
      
      try
      {
         PSKeyword keyword = loadKeyword(id, null);
         
         if (!keyword.getKeywordType().equals("1"))
         {
            throw new IllegalArgumentException(
                  "deleteKeyword was called for a keyword choice, "
                  + "not a keyword. id: " + id);
         }
         // delete all choices first
         if (!keyword.getChoices().isEmpty())
         {
            List<PSKeyword> choices = findKeywordChoices(
               keyword.getValue(), null);
            for (PSKeyword choice : choices)
               getSession().delete(choice);
         }
         
         // then delete the keyword
         getSession().delete(keyword);
      }
      catch (PSContentException e)
      {
         // ignore non existing keyword
      }
   }

   /**
    * Load the choices for the supplied keyword.
    * 
    * @param keyword the keyword for which to load the choices,
    *    assumed not <code>null</code>.  This may be a keyword choice.
    *    
    * @param sortProperty the property name by which to sort the choices 
    *    ascending, may be <code>null</code> or empty to skip sorting.
    * @return the list of choices for the supplied keyword, not
    *    <code>null</code>, may be empty.  Returns an empty list if the supplied
    *    keyword is not of type keyword, see {@link PSKeyword#getKeywordType()}.
    */
   private List<PSKeywordChoice> loadKeywordChoices(PSKeyword keyword, 
      String sortProperty)
   {

         List<PSKeywordChoice> choiceList = new ArrayList<>();
         
         //only look for choices if it is a keyword
         if (keyword.getKeywordType().equals(PSKeyword.KEYWORD_TYPE))
         {
            List<PSKeyword> choices = findKeywordChoices(keyword.getValue(), 
                  sortProperty);
            
            for (PSKeyword choice : choices)
               choiceList.add(new PSKeywordChoice(choice));
         }
         
         return choiceList;

   }

   /* (non-Javadoc)
    * @see IPSContentService#createAutoTranslation(long, String, long, long)
    */
   public PSAutoTranslation createAutoTranslation(long contentTypeId, 
      String locale, long workflowId, long communityId)
   {
      if (StringUtils.isBlank(locale))
         throw new IllegalArgumentException("locale may not be null or empty");
      
      PSAutoTranslation at = new PSAutoTranslation();
      at.setCommunityId(communityId);
      at.setContentTypeId(contentTypeId);
      at.setLocale(locale);
      at.setWorkflowId(workflowId);
      
      return at;
   }

   /* (non-Javadoc)
    * @see IPSContentService#loadAutoTranslations()
    */
   @SuppressWarnings("unchecked")
   public List<PSAutoTranslation> loadAutoTranslations()
   {
      Session session = getSession();
      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSAutoTranslation> criteria = builder.createQuery(PSAutoTranslation.class);
      Root<PSAutoTranslation> critRoot = criteria.from(PSAutoTranslation.class);

         return session.createQuery(criteria).getResultList();

   }

   /* (non-Javadoc)
    * @see IPSContentService#saveAutoTranslation(PSAutoTranslation)
    */
   public void saveAutoTranslation(PSAutoTranslation autoTranslation)
   {
      if (autoTranslation == null)
         throw new IllegalArgumentException("autoTranslation may not be null");
         getSession().saveOrUpdate(autoTranslation);
   }

   /* (non-Javadoc)
    * @see IPSContentService#deleteAutoTranslation(long, String)
    */
   public void deleteAutoTranslation(long contentTypeId, String locale)
   {
      if (StringUtils.isBlank(locale))
         throw new IllegalArgumentException("locale may not be null or empty");
      
      PSAutoTranslation at = loadAutoTranslation(contentTypeId, locale);
      if (at != null)
         synchronized (at)
         {
            getSession().delete(at);
            getSession().flush();
            getSession().evict(at);
         }
   }

   /**
    * Loads a single auto translation
    * 
    * @param contentTypeId The content type id of the auto translation.
    * @param locale The locale of the auto translation, may not be 
    * <code>null</code> or empty.
    * 
    * @return The auto translation, or <code>null</code> if not found.
    */
   public PSAutoTranslation loadAutoTranslation(long contentTypeId, 
      String locale)
   {
      if (StringUtils.isBlank(locale))
         throw new IllegalArgumentException("locale may not be null or empty");

         return getSession().get(
            PSAutoTranslation.class, new PSAutoTranslationPK(contentTypeId, 
               locale));

   }
   
   /**
    * Tests if the supplied id is in the excluded keyword list and throws
    * an <code>IllegalArgumentException</code> if so.
    * 
    * @param id the keyword id to test, assumed not <code>null</code>.
    */
   private void validateKeywordId(IPSGuid id)
   {
      for (IPSGuid exclude : ms_keywordExcludes)
      {
         if (exclude.equals(id))
            throw new IllegalArgumentException(
               "you are not allowed to delete the keyword for the supplied id");
      }
   }
   
   /**
    * Remove all excluded keywords from the supplied keyword list. See 
    * {@link #ms_keywordExcludes} for all defined excludes.
    * 
    * @param keywords the list of keywords to filter, assumed not
    *    <code>null</code>, may be empty.
    * @return the filtered keyword list, never <code>null</code>, may be empty.
    */
   private List<PSKeyword> filterKeywordExcludes(List<PSKeyword> keywords)
   {
      List<PSKeyword> filteredKeywords = new ArrayList<>(keywords);
      
      for (PSKeyword keyword : keywords)
      {
         for (IPSGuid exclude : ms_keywordExcludes)
         {
            if (keyword.getGUID().equals(exclude))
            {
               filteredKeywords.remove(keyword);
               break;
            }
         }
      }
      
      return filteredKeywords;
   }
   
   /**
    * A list with keywords which will be excluded from all results. Currently
    * this is only the first entry which is the list of all defined keyword
    * types.
    */
   private static final List<IPSGuid> ms_keywordExcludes = 
      new ArrayList<>();
   
   static
   {
      ms_keywordExcludes.add(new PSGuid(PSTypeEnum.KEYWORD_DEF, 1));
      PSXmlSerializationHelper.addType("auto-translation", PSAutoTranslation.class);
   }

   @SuppressWarnings("unchecked")
   /**
    * A query to return all the translation settings for the locale 
    * @param loc the locale never <code>null</code> or empty
    * @return a list of autotranslations may be empty
    */
   public List<PSAutoTranslation> loadAutoTranslationsByLocale(String loc)
   {
      return getSession().createQuery(
            "from PSAutoTranslation p where p.locale = :locale").setParameter("locale",loc).list();
   }
   
   
   /* (non-Javadoc)
    * @see IPSContentService#
    */
   @SuppressWarnings("unchecked")
   public List<PSFolderProperty>  getFolderProperties(String property) {
      Session session = getSession();
      List<PSFolderProperty> pSFolderPropertyList = new ArrayList<>();
      List<Object[]> list = new ArrayList<>();
      
      try
      {
         /*
         Criteria criteria = session.createCriteria(PSFolderProperty.class);
        criteria.add(Restrictions.eq("propertyName", property));
        list =  criteria.list();
        */

         StringBuilder query = new StringBuilder("from PSFolderProperty pfp, PSRelationshipData prd ");
         query.append(" WHERE propertyName = '" + property + "' and pfp.contentID=prd.dependent_id and prd.config_id != " + PSRelationshipConfig.ID_RECYCLED_CONTENT);
        
        Query result = session.createQuery(query.toString());
        list = result.list();
        
      } catch(HibernateException he){
         log.error(he.getMessage());
         log.debug(he.getMessage(), he);
      }

      for(Object[] o : list){
         pSFolderPropertyList.add((PSFolderProperty) o[0]);
      }
      return pSFolderPropertyList;
   }
   
   
   
   
}

