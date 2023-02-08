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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Extracts the text from a supplied input stream corresponding to a PDF file.
 * It uses PDFBox to extract the text from the PDF Document. The following are
 * the limitations.
 * <ul>
 * <li>Extracts only text and no meta data like author or created date etc.</li>
 * <li>If the document is password protected, it tries to decrypt using empty
 * password. If succeeds extracts the text and returns, otherwise throws
 * appropritae exception wrapped in PSExtensionProcessingException.</li>
 * </ul>
 * 
 */
public class PSTextConverterPdf implements IPSLuceneTextConverter
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
      PDDocument pdfDocument = null;
      try
      {
         pdfDocument = PDDocument.load(is);
         if (pdfDocument.isEncrypted())
         {
            //Just try using the default password and move on

         }
         PDFTextStripper stripper = new PDFTextStripper();
         StringWriter writer = new StringWriter();
         stripper.writeText(pdfDocument, writer);
         resultText = writer.getBuffer().toString();
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException(m_className, e);
      }
      finally
      {
         if (pdfDocument != null)
         {
            try
            {
               pdfDocument.close();
            }
            catch (IOException e)
            {
               throw new PSExtensionProcessingException(m_className, e);
            }
         }

      }
      return resultText;
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
    * A member variable to hold the name of this class.
    */
   private String m_className = getClass().getName();
}
