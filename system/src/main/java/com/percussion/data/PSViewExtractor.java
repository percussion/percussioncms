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

import com.percussion.design.objectstore.IPSReplacementValue;

/**
 * This class allows the {@link PSViewEvaluator} to be used as a data extractor
 * for a particular pageId.
 */
public class PSViewExtractor implements IPSDataExtractor
{
   /**
    * Construct an extractor for the specified page id.
    *
    * @param viewEvaluator The view evaluator constructed for the set of views
    * that are being used.  May not be <code>null</code>.
    * @param pageId The page id this extractor is being used with.
    *
    * @throws IllegalArgumentException if viewEvaluator is <code>null</code>.
    */
   public PSViewExtractor(PSViewEvaluator viewEvaluator, int pageId)
   {
      if (viewEvaluator == null)
         throw new IllegalArgumentException("viewEvaluator may not be null");

      m_viewEval = viewEvaluator;
      m_pageId = pageId;
   }


   /**
    * Convenience method calls {@link #extract(PSExecutionData, Object)
    * extract(data, null)}.
    */
   public Object extract(PSExecutionData data)
      throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extracts the next view to use by calling
    * {@link PSViewEvaluator#getNextView(PSExecutionData, int)} using the
    * supplied <code>data</code> and the <code>pageId</code> supplied during
    * construction.  Since this method is currently documented as never
    * returning <code>null</code> or empty, the <code>defValue</code> parameter
    * will not ever be utilized.
    *
    * @param data  The execution data associated with this request.
    *
    * @param defValue The default value to use if a value is not found.
    * Currently has no effect.
    *
    * @return The next view name. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if data is <code>null</code>.
    * @throws PSDataExtractionException if any errors occur.
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");

      Object result = m_viewEval.getNextView(data, m_pageId);

      return (result != null ? result : defValue);
   }

   /**
    * Get the source IPSReplacementValue object(s) used to create this
    * extractor.
    *
    * @return <code>null</code> always.
    */
   public IPSReplacementValue[] getSource()
   {
      return null;
   }

   /**
    * The pageId to use when retrieving the next view.  Supplied during
    * construction, immutable after that.
    */
   private int m_pageId;

   /**
    * The view evaluator to use to retrieve the next view.  Intialized during
    * construction, never <code>null</code> or modified after that.
    */
   private PSViewEvaluator m_viewEval;
}
