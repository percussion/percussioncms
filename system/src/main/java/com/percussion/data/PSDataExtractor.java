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
 * The PSDataExtractor abstract class can be extended by classes wanting
 * to extend the IPSDataExtractor interface. This is not required. At this
 * time, only getSource is implemented in this class.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSDataExtractor implements IPSDataExtractor
{
   /**
    * Construct the extractor for the specified source object.
    *
    * @param   source      the source object for this extractor
    */
   protected PSDataExtractor(IPSReplacementValue source)
   {
      this(new IPSReplacementValue[] { source });
   }

   /**
    * Construct the extractor for the specified source object.
    *
    * @param   source      the source object(s) for this extractor
    */
   protected PSDataExtractor(IPSReplacementValue[] source)
   {
      super();
      m_sourceReplacementValues = source;
   }

   /**
    * Get the source IPSReplacementValue object used to create this
    * extractor.
    *
    * @return               the source object (may be <code>null</code>)
    */
   public IPSReplacementValue[] getSource()
   {
      return m_sourceReplacementValues;
   }
   
   /**
    * Gets the first IPSReplacementValue object used to create this extractor.
    * 
    * @return the first IPSReplacementValue object, may be <code>null</code>.
    */
   public IPSReplacementValue getSingleSource()
   {
      if (m_sourceReplacementValues == null || 
         m_sourceReplacementValues.length == 0)
         return null;
         
      return (IPSReplacementValue) m_sourceReplacementValues[0]; 
   }

   protected IPSReplacementValue[] m_sourceReplacementValues;
}

