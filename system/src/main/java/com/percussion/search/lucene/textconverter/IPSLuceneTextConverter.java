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
package com.percussion.search.lucene.textconverter;

import com.percussion.extension.IPSExtension;
import com.percussion.extension.PSExtensionProcessingException;

import java.io.InputStream;

/**
 * The core lucene search engine handles only Strings for indexing. The content
 * from attached files and rich text fields needs to be converted before feeding
 * it to Lucene for indexing. Lucene provides several text converters from
 * contributors.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSLuceneTextConverter extends IPSExtension
{
   /**
    * Extracts the text from the given InputStream using the provided mimetype
    * and returns the String. The implementor may return <code>null</code> or
    * empty in which case the caller does not index the content. Thrown
    * exception is logged and the content is not indexed.
    * 
    * @param is The InputStream from which the text needs to be extracted. The
    * caller owns the responsibility of closing this InputStream.
    * @param mimetype The mimetype of the InputStream as provided by the user
    * and not guaranteed to match. It is implementers responsibility to either
    * throw an exception or guess the mimetype of the input stream correctly and
    * return the extracted text from it.
    * @return extracted text from the InputStream. If text extraction results in
    * empty string or <code>null</code> the implementors can return as is. In
    * that case the caller will not index the item.
    * @throws PSExtensionProcessingException if an exception occurs which
    * prevents the proper handling of this request. In this case caller logs it
    * and does not index the content.
    */
   public String getConvertedText(InputStream is, String mimetype)
      throws PSExtensionProcessingException;

}
