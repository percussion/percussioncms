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
