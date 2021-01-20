/*[ QAScriptDocument.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;
import java.io.StringReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.percussion.xml.PSXmlDocumentBuilder;

public class QAScriptDocument
    implements Serializable
{
    public QAScriptDocument(Document doc, String name)
    {
        if(doc == null)
        {
            throw new NullPointerException("doc == null");
        }
        else
        {
         m_doc = PSXmlDocumentBuilder.toString(doc);
            m_name = name;
         m_transientDoc = doc;
            return;
        }
    }

   public Document getDocument()
      throws IOException, SAXException
   {
      if (m_transientDoc == null)
         m_transientDoc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(m_doc), false);

      return m_transientDoc;
   }
   
    public String getName()
    {
        return m_name;
    }

   private transient Document m_transientDoc = null;
   
    private String m_doc;
    private String m_name;
}
