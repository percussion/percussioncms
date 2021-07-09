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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.search.lucene.PSSearchUtils;
import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.xml.PSXmlDocumentBuilder;

/**
 * Extracts the text from html and returns. Walks through all the nodes and gets
 * the text from only text nodes and concatenates them with a space. Uses
 * {@link PSXmlDocumentBuilder#createXmlDocument(InputStream, boolean)} to parse
 * the input html document. Text from comments and attributes is not extracted.
 * 
 */
public class PSTextConverterHtml implements IPSLuceneTextConverter
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.lucene.textconverter.IPSLuceneTextConverter#getConvertedText(java.io.InputStream,
    * java.lang.String)
    */
   public String getConvertedText(InputStream is, String mimetype)
      throws PSExtensionProcessingException
   {
      if (is == null)
         throw new IllegalArgumentException("is must not be null");
      String resultText = "";
      try
      {
         String content = IOUtils.toString(is, IPSUtilsConstants.RX_JAVA_ENC);
         if(content != null && content.length() > 0)
         {
            String contentStr = content;
            try
            {
               resultText = extractTextFromValidXml(contentStr);
            }
            catch(SAXException e) 
            {
               if(!(contentStr.trim().startsWith("<") && contentStr.trim().endsWith(">")))
               {
                  //Lets us assume this as plain text.
                  resultText = contentStr;
               }
               else
               {
                   
                  //Apply tidy, then attempt extraction
                  resultText = extractTextFromLooseHtml(contentStr);
               }
            }
         }
      }
      catch (IOException e)
      {
         throw new PSExtensionProcessingException(m_className,e);
      }
      return resultText;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    * java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {

   }

   /**
    * Attempts to extract the text content from the given html String.
    * 
    * @param html String containing html content.
    * 
    * @return the extracted text, never <code>null</code>.
    * 
    * @throws IOException if an I/O error occurs.
    * @throws SAXException if a parsing error occurs.
    */
   private String extractTextFromValidXml(String html) throws IOException, SAXException
   {
      Document root = PSXmlDocumentBuilder.createXmlDocument(new StringReader(html), false);
      Element rawDoc = root.getDocumentElement();

      StringBuffer sb = new StringBuffer();
      sb.setLength(0);
      PSSearchUtils.getNodeText(sb, rawDoc);
      
      return sb.toString();
   }
   
   /**
    * Extract text content from HTML using Jericho.
    * http://stackoverflow.com/questions/240546/removing-html-from-a-java-string
    * @param htmlText
    * @return not <code>null</code>
    */
   private String extractTextFromLooseHtml(String htmlText) {
       Source htmlSource = new Source(htmlText);
       Segment htmlSeg = new Segment(htmlSource, 0, htmlText.length());
       Renderer htmlRend = new Renderer(htmlSeg);
       return htmlRend.toString();
   }
   
   /**
    * A memeber variable to hold the name of this class.
    */
   private String m_className = getClass().getName();
}
