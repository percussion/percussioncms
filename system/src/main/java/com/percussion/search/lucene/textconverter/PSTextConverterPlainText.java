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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.utils.tools.IPSUtilsConstants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
