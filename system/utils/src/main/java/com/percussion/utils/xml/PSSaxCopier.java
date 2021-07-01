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
package com.percussion.utils.xml;

import static com.percussion.utils.xml.PSSaxHelper.canBeSelfClosedElement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * A sax content handler that outputs the input SAX events to a new XML document
 * being written with an {@link javax.xml.stream.XMLStreamWriter}, which is
 * part of the STaX implementation. Ignores the start and end document calls to
 * support merging documents.
 * 
 * @author dougrand
 */
public class PSSaxCopier extends DefaultHandler2 
{
   /**
    * Logger for the sax copier
    */
   private static final Logger ms_log = LogManager.getLogger(PSSaxCopier.class);
   
   /**
    * The stream writer, initialized in the Ctor and never modified
    */
   protected XMLStreamWriter m_writer = null;
   
   /**
    * Maintains state of parsing a cdata so cdatas are maintained on output
    */
   private boolean m_inCData = false;
   
   /**
    * A map of elements to rename in copying
    */
   protected Map<String,String> m_elementRenames = new HashMap<String,String>();
   
   /**
    * A filler string that is used to fill the empty elements. So that they
    * are not self closed.
    */
   public static final String RX_FILLER = "##RX_FILLER##";

   /**
    * Flag to indicate the whether to fill the empty elements with filler text
    * or not.
    */
   private boolean m_addFillerTextToEmptyElements;
   
   /**
    * This member keeps track of the last character output. Used to determine
    * if we may need to output a single whitespace character to avoid one
    * of the elements that can't be output "empty" from being output as an
    * empty element. Set in the {@link #characters(char[], int, int)} method,
    * reset almost everywhere else.
    */
   protected int m_lastcharcount = 0;

   /**
    * Ctor
    * 
    * @param writer the stream writer, never <code>null</code>
    * @param renames a map of names to modify when copying elements, may be
    * <code>null</code> or empty
    * @param addFillerTextToEmptyElements a flag to indicate whether the
    * empty elements needs to be added with filler text so that the parser
    * does not self close them. The filter text added is {@link #RX_FILLER}.
    * It is callers responsibility to replace this filler with empty string
    * after getting the string from the writer.
    */
   public PSSaxCopier(XMLStreamWriter writer, Map<String, String> renames,
         boolean addFillerTextToEmptyElements)
   {
      if (writer == null)
      {
         throw new IllegalArgumentException("writer may not be null");
      }
      m_writer = writer;
      if (renames != null)
         m_elementRenames = renames;
      m_addFillerTextToEmptyElements = addFillerTextToEmptyElements;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
    */
   @Override
   public void characters(char[] ch, int start, int length) throws SAXException
   {
      m_lastcharcount += length;
      try
      {
         if (m_inCData)
            m_writer.writeCData(new String(ch, start, length));
         else
            m_writer.writeCharacters(ch, start, length);
      }
      catch (XMLStreamException e)
      {
         throw new SAXException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public void endElement(String uri, String localName, String qName)
         throws SAXException
   {
      try
      {
         // Fill the filler text to empty elements to avoid self closing.
         if ((!canBeSelfClosedElement(qName))
               && m_addFillerTextToEmptyElements)
         {
            if (m_lastcharcount == 0)
            {
               m_writer.writeCharacters(RX_FILLER);
            }
         }
         m_writer.writeEndElement();
         resetCharCount();
      }
      catch (XMLStreamException e)
      {
         throw new SAXException(e);
      }
   }

   /* (non-Javadoc) 
    * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
    */
   @Override
   public void error(SAXParseException error) throws SAXException
   {
      ms_log.error("Problem processing data at line "
            + error.getLineNumber() + " at column " + error.getColumnNumber()
            + ":" + error.getLocalizedMessage());
   }

   /* (non-Javadoc) 
    * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
    */
   @Override
   public void fatalError(SAXParseException error) throws SAXException
   {
      ms_log.fatal("Problem processing data at line "
            + error.getLineNumber() + " at column " + error.getColumnNumber()
            + ":" + error.getLocalizedMessage());

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#processingInstruction(java.lang.String,
    *      java.lang.String)
    */
   @Override
   public void processingInstruction(String target, String data)
         throws SAXException
   {
      resetCharCount();
      try
      {
         m_writer.writeProcessingInstruction(target, " " + data);
      }
      catch (XMLStreamException e)
      {
         throw new SAXException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#skippedEntity(java.lang.String)
    */
   @Override
   public void skippedEntity(String name) throws SAXException
   {
      try
      {
         m_writer.writeEntityRef(name);
      }
      catch (XMLStreamException e)
      {
         throw new SAXException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
    *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
   @Override
   public void startElement(String uri, String localName, String qName,
         Attributes attributes) throws SAXException
   {
      resetCharCount();
      try
      {
         String nName = m_elementRenames.get(qName);
         if (nName != null)
         {
            qName = nName;
         }
         m_writer.writeStartElement(qName);
         for (int i = 0; i < attributes.getLength(); i++)
         {
            String name = attributes.getQName(i);
            m_writer.writeAttribute(name, attributes.getValue(i));
         }
      }
      catch (XMLStreamException e)
      {
         throw new SAXException(e);
      }
   }

   /**
    * Reset the character count on methods that introduce a boundary.
    */
   protected void resetCharCount()
   {
      m_lastcharcount = 0;
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
    */
   @Override
   public void warning(SAXParseException warning) throws SAXException
   {
      ms_log.warn("Problem processing data at line "
            + warning.getLineNumber() + " at column "
            + warning.getColumnNumber() + ":" + warning.getLocalizedMessage());

   }

   /* (non-Javadoc)
    * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
    */
   @Override
   public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
   {
      PSEntityResolver resolver = PSEntityResolver.getInstance();
      return resolver.resolveEntity(publicId, systemId);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#attributeDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException
   {
      resetCharCount();
      super.attributeDecl(eName, aName, type, mode, value);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#comment(char[], int, int)
    */
   @Override
   public void comment(char[] ch, int start, int length) throws SAXException
   {
      resetCharCount();
      try
      {
         m_writer.writeComment(new String(ch, start, length));
      }
      catch (XMLStreamException e)
      {
         ms_log.error(e);
      }
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#elementDecl(java.lang.String, java.lang.String)
    */
   @Override
   public void elementDecl(String name, String model) throws SAXException
   {
      resetCharCount();
      super.elementDecl(name, model);
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.ext.LexicalHandler#endCDATA()
    */
   @Override
   @SuppressWarnings("unused")
   public void endCDATA() throws SAXException
   {
      super.endCDATA();
      m_inCData = false;
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.xml.sax.ext.LexicalHandler#startCDATA()
    */
   @Override
   @SuppressWarnings("unused")
   public void startCDATA() throws SAXException
   {
      super.startCDATA();
      m_inCData = true;
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#endDTD()
    */
   @Override
   public void endDTD() throws SAXException
   {
      resetCharCount();
      super.endDTD();
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#endEntity(java.lang.String)
    */
   @Override
   public void endEntity(String name) throws SAXException
   {
      resetCharCount();
      super.endEntity(name);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#externalEntityDecl(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException
   {
      resetCharCount();
      super.externalEntityDecl(name, publicId, systemId);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#getExternalSubset(java.lang.String, java.lang.String)
    */
   @Override
   public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException
   {
      resetCharCount();
      return super.getExternalSubset(name, baseURI);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#internalEntityDecl(java.lang.String, java.lang.String)
    */
   @Override
   public void internalEntityDecl(String name, String value) throws SAXException
   {
      resetCharCount();
      super.internalEntityDecl(name, value);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#resolveEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException
   {
      PSEntityResolver resolver = PSEntityResolver.getInstance();
      return resolver.resolveEntity(publicId, systemId);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#startDTD(java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   public void startDTD(String name, String publicId, String systemId) throws SAXException
   {
      resetCharCount();
      super.startDTD(name, publicId, systemId);
   }

   /* (non-Javadoc)
    * @see org.xml.sax.ext.DefaultHandler2#startEntity(java.lang.String)
    */
   @Override
   public void startEntity(String name) throws SAXException
   {
      resetCharCount();
      super.startEntity(name);
   }
   
   /* (non-Javadoc)
    * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int,
    *      int)
    */
   @Override
   public void ignorableWhitespace(char[] ch, int start, int length)
         throws SAXException
   {
      try
      {
         m_lastcharcount += length;
         m_writer.writeCharacters(ch, start, length);
      }
      catch (XMLStreamException e)
      {
         throw new SAXException(e);
      }
   }
}
