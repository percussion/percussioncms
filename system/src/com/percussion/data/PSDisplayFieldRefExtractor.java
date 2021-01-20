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

import com.percussion.cms.PSContentEditorWalker;
import com.percussion.design.objectstore.PSDisplayFieldRef;

import org.w3c.dom.Document;

/**
 * A data extractor for DisplayField reference elements as specified in the 
 * sys_ContentEditor.dtd.
 */
public class PSDisplayFieldRefExtractor extends PSDataExtractor
{
   /**
    * Construct a data extractor for the provided source.
    *
    * @param source the DisplayField  reference to create this extractor for,
    *    not <code>null</code>.
    * @throws IllegalArgumentException if the provided source is 
    *    <code>null</code>.
    */
   public PSDisplayFieldRefExtractor(PSDisplayFieldRef source)
   {
      super(source);

      if (source == null)
         throw new IllegalArgumentException("the source cannot be null");
      
      m_fieldRef = source.getValueText();
   }

   // see IPSDataExtractor for description
   public Object extract(PSExecutionData data)
      throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Gets the input document from the execution data and extracts the 
    * 'Value' from the 'Control' which matches the field reference 
    * created in the constructor.
    *
    * @return the 'Value' of the matching 'Control' as String or a 
    *    'DisplayChoice' element for arrays, depending on the type of the 
    *    'Control', never <code>null</code>.
    * @see IPSDataExtractor
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      Object result = defValue;

      Document doc = data.getInputDocument();
      if (doc != null)
         result = PSContentEditorWalker.getDisplayFieldValue(doc, m_fieldRef);

      return result;
   }
   
   /**
    * The DisplayField name to be extracted, initialized in ctor, nerver
    * <code>null</code> or empty after that.
    */
   private String m_fieldRef = null;
}
