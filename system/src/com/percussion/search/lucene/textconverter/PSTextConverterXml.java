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
import com.percussion.search.lucene.PSSearchUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
         StringBuffer sb = new StringBuffer();
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
