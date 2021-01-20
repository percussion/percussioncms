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
package com.percussion.data;

import com.percussion.design.objectstore.PSNotFoundException;
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
