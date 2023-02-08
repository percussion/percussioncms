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
import com.percussion.search.lucene.PSSearchUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts the text from xml and returns. Walks through all the nodes and gets
 * the text from only text nodes and concatnates them with a space. 
 */
public class PSTextConverterXml implements IPSLuceneTextConverter
{
   /*
    * (non-Javadoc)
    * @see com.percussion.search.lucene.textconverter.IPSLuceneTextConverter#getConvertedText(java.io.InputStream, java.lang.String)
    */
   public String getConvertedText(InputStream is, String mimetype)
      throws PSExtensionProcessingException
   {
      String resultText = "";
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(is, false);
         StringBuilder sb = new StringBuilder();
         PSSearchUtils.getNodeText(sb,doc.getDocumentElement());
         resultText = sb.toString();
      }
      catch (IOException e)
      {
         throw new PSExtensionProcessingException(m_className, e);
      }
      catch (SAXException e)
      {
         throw new PSExtensionProcessingException(m_className, e);
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
   // TODO Auto-generated method stub

   }

   /**
    * A memeber variable to hold the name of this class.
    */
   private String m_className = getClass().getName();
}
