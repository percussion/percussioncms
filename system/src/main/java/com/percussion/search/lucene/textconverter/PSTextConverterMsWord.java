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
import com.percussion.search.lucene.IPSLuceneConstants;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts the text from input stream corresponding to Microsoft Word file
 * using POI api.
 * @author bjoginipally
 *
 */
public class PSTextConverterMsWord implements IPSLuceneTextConverter
{
   /*
    * (non-Javadoc)
    * @see com.percussion.search.lucene.textconverter.IPSLuceneTextConverter#getConvertedText(java.io.InputStream, java.lang.String)
    */
   public String getConvertedText(InputStream is, String mimetype)
      throws PSExtensionProcessingException
   {
      if (is == null)
         throw new IllegalArgumentException("is must not be null");

      String resultText = "";
      
      try
      {
         POITextExtractor ext = getTextExtractor(mimetype, is);
         resultText = ext.getText();
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException(m_className, e);
      }
      return resultText;
   }


   private POITextExtractor getTextExtractor(String mimetype, InputStream is) throws IOException
   {
      POITextExtractor extractor;
      
      if (IPSLuceneConstants.MIME_TYPE_APPLICATION_BY_MSWORD.equalsIgnoreCase(mimetype))
         extractor = new WordExtractor(is);
      else
      {
         extractor = new XWPFWordExtractor(new XWPFDocument(is));
      }
      
      return extractor;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {

   }

   /**
    * A memeber variable to hold the name of this class.
    */
   private String m_className = getClass().getName();
}
