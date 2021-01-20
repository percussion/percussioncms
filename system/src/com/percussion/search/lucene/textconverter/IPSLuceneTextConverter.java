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
