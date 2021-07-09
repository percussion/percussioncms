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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.search.lucene.textconverter;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.xml.sax.SAXException;

/**
 * Extracts the text from text files and returns it. It does not process 
 * the text, just returns it as it is in the file.  
 * 
 */
public class PSTextConverterPlainText implements IPSLuceneTextConverter
{

   /**
    * A static member to hold the name of this class.
    */
   private final static String ms_className = PSTextConverterPlainText.class.getName();
   

   /* (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      
   }

   /* (non-Javadoc)
    * @see com.percussion.search.lucene.textconverter.IPSLuceneTextConverter#getConvertedText(java.io.InputStream, java.lang.String)
    */
   public String getConvertedText(InputStream is, String mimetype)
         throws PSExtensionProcessingException
   {
      Validate.notNull(is, "is must not be null");
      
      String resultText = "";
      try
      {
         resultText = IOUtils.toString(is, IPSUtilsConstants.RX_JAVA_ENC);
      }
      catch (IOException e)
      {
         throw new PSExtensionProcessingException(ms_className,e);
      }
      return resultText;
   }

}
