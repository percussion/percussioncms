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

