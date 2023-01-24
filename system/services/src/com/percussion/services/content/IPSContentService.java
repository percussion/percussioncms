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
package com.percussion.services.content;

import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSFolderProperty;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * This interface defines various services used for content and content
 * design objects.
 */
public interface IPSContentService
{
   /**
    * Create a new keyword with an empty choice list.
    * 
    * @param label the label of the new keyword, not <code>null</code> or 
    *    empty, must be unique against all existing keywords.
    * @param description a description for the new keyword, may be
    *    <code>null</code> or empty.
    * @return the new keyword created, never <code>null</code>.
    */
   public PSKeyword createKeyword(String label, String description);
   
   /**
    * Find all keywords for the supplied label.
    * 
    * @param label the label for which to find the keywords, may be
    *    <code>null</code> or empty to get all keywords, sql type (%) 
    *    wildcards are supported.
    * @param sortProperty the name of the property by which to sort the results
    *    ascending, may be <code>null</code> or empty to skip sorting.
    * @return all found keywords for the supplied label, never <code>null</code>
    *    may be empty.
    */
   public List<PSKeyword> findKeywordsByLabel(String label, 
      String sortProperty);
   
   /**
    * Finds all keyword choices for the supplied keyword type.
    * 
    * @param type the kewyword type for which to find all choices, not 
    *    <code>null</code> or empty.
    * @param sortProperty the name of the property by which to sort the results
    *    ascending, may be <code>null</code> or empty to skip sorting.
    * @return all keywords which represent choices for the specified keyword 
    *    type, never <code>null</code>, may be empty.
    */
   public List<PSKeyword> findKeywordChoices(String type, String sortProperty);
   
   /**
    * Load the keyword for the supplied id.
    * 
    * @param id the id of the keyword to load, not <code>null</code>.
    * @param sortProperty the name of the property by which to sort the 
    *    keyword choices ascending, may be <code>null</code> or empty to 
    *    skip sorting.
    * @return the loaded keyword, never <code>null</code>.
    * @throws PSContentException if the identified keyword does not exist.
    */
   public PSKeyword loadKeyword(IPSGuid id, String sortProperty) 
      throws PSContentException;
   
   /**
    * Insert or update the supplied keyword.
    * 
    * @param keyword the keyword to save, not <code>null</code>.
    */
   public void saveKeyword(PSKeyword keyword);
   
   /**
    * Delete the supplied keyword and all its choices.
    * 
    * @param id the keyword to be deleted, not <code>null</code>.
    */
   public void deleteKeyword(IPSGuid id);
   
   /**
    * Create a new auto translation definition
    * 
    * @param contentTypeId The content type for which the auto translation is
    * defined.
    * @param locale The language string of the locale for which the auto
    * translation is defined, may not be <code>null</code> or empty.
    * @param workflowId The workflow id to use when generating translations.
    * @param communityId The community id to use when generating translation.
    * 
    * @return The instantiated auto translation, never <code>null</code>,
    * not yet persisted.
    */
   public PSAutoTranslation createAutoTranslation(long contentTypeId, 
      String locale, long workflowId, long communityId);
   
   /**
    * Loads all auto translation definitions
    * 
    * @return A list of all defined auto translations, never <code>null</code>,
    * may be empty.
    */
   public List<PSAutoTranslation> loadAutoTranslations();
   
   
   /**
    * Loads all the translation settings definitions by locale
    * @param loc the locale never <code>null</code>
    * @return A list of all translation settings definitions never 
    * <code>null</code>
    */
   public List<PSAutoTranslation> loadAutoTranslationsByLocale(String loc);
   
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
      String locale);   
   
   /**
    * Save the supplied auto translation definitions.
    * 
    * @param autoTranslation The auto translations to save, may not be 
    * <code>null</code>.
    */
   public void saveAutoTranslation(PSAutoTranslation autoTranslation);
   
   /**
    * Delete the specified auto translation.
    * 
    * @param contentTypeId The content type for which the auto translation is
    * defined.
    * @param locale The language string of the locale for which the auto
    * translation is defined, may not be <code>null</code> or empty.
    */
   public void deleteAutoTranslation(long contentTypeId, String locale);

   public List<PSFolderProperty>  getFolderProperties(String property);
}

