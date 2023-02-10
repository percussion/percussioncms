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
package com.percussion.data;

import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.extension.PSExtensionException;

import java.util.Iterator;

/**
 * Extends the rule evaluator to evaluate conditional URL's.
 */
public class PSConditionalUrlEvaluator extends PSRuleListEvaluator
{
   /**
    * Constructs a new conditional url evaluator.
    *
    * @param rules an iterator of PSRule objects, not <code>null</code>.
    * @param request the url request to evaluate, not <code>null</code>.
    */
   public PSConditionalUrlEvaluator(Iterator rules, PSUrlRequest request)
      throws PSNotFoundException, PSExtensionException
   {
      super(rules);

      if (request == null)
         throw new IllegalArgumentException("request cannot be null");

      m_hrefExtractor = new PSUrlRequestExtractor(request);
   }

   /**
    * Get the url-string for the provided execution data.
    *
    * @param data the execution data, not <code>null</code>.
    * @return the url-string, might be <code>null</code>.
    */
   public String getUrl(PSExecutionData data)
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data cannot be null");

      return m_hrefExtractor.extract(data).toString();
   }

   /** The URL extractor, never <code>null</code> after construction */
   private IPSDataExtractor m_hrefExtractor = null;
}
