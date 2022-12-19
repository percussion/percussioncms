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
package com.percussion.i18n.tmxdom;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class wraps the header DOM node of the TMX document and provides easy
 * methods to manipulate the header properties. We use the header element to
 * store the supported languages by the TMX document using the &lt;prop&gt;
 * child elements of the &lt;header&gt; element.
 */
public class PSTmxHeader
   extends PSTmxNode implements IPSTmxHeader
{
   /**
    * Contsructor. (ani:typo) Takes the TMX document object parses for the header, notes
    * and supported languages.
    * @param TMXDoc parent TMX Document object, must not be <code>null</code>.
    */
   PSTmxHeader(IPSTmxDocument TMXDoc)
   {
      if(TMXDoc == null)
         throw new IllegalArgumentException("TMXDoc must not be null");

      m_Parent = TMXDoc;
      m_PSTmxDocument = TMXDoc;
      Element elem = getHeaderElement(m_PSTmxDocument);
      if(null ==elem)
      {
         m_DOMElement =
            m_PSTmxDocument.getDOMDocument().createElement(TMXNODENAME);
         PSXmlDocumentBuilder.copyTree(m_PSTmxDocument.getDOMDocument(),
            m_PSTmxDocument.getDOMDocument().getDocumentElement(),
            m_DOMElement, false);
      }
      else
      {
         m_DOMElement = elem;
      }
      processHeaderElement();
   }

   /**
    * Helper method to process the header element. Parses for the supported
    * languages and Notes and stores to appropriate objects. Adds all default
    * required properties.
    */
   private void processHeaderElement()
   {
      NodeList nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXPROPERTY]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxProperty prop = new PSTmxProperty(m_PSTmxDocument,
            (Element)nl.item(i));
         m_Languages.put(prop.getValue(), prop);
      }
      nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXNOTE]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxNote note = new PSTmxNote(m_PSTmxDocument, (Element)nl.item(i));
         m_Notes.put(note.getLang(), note);
      }
      //Add all defualt properties
      setProperty(IPSTmxHeader.PROP_DATA_TYPE,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_DATA_TYPE);
      setProperty(IPSTmxHeader.PROP_ADMIN_LANG,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_ADMIN_LANG);
      setProperty(IPSTmxHeader.PROP_CREATION_TOOL,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_CREATION_TOOL);
      setProperty(IPSTmxHeader.PROP_CREATION_TOOL_VERSION,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_CREATION_TOOL_VERSION);
      setProperty(IPSTmxHeader.PROP_O_TMF,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_O_TMF);
      setProperty(IPSTmxHeader.PROP_SEG_TYPE,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_SEG_TYPE);
      setProperty(IPSTmxHeader.PROP_SRC_LANG,
         IPSTmxDtdConstants.DEFAULT_VALUE_ATTR_SRC_LANG);
   }

   /**
    * Helper method to get the header element of the TMX document.
    * @param TMXDocument TMX document object, must not be <code>null</code>.
    * @return header element may be <code>null</code>.
    */
   private Element getHeaderElement(IPSTmxDocument TMXDocument)
   {
      Element domElement = TMXDocument.getDOMElement();
      if (domElement==null) return null;
      NodeList nl = TMXDocument.getDOMElement().getElementsByTagName(TMXNODENAME);
      return (nl ==null || nl.getLength() < 1)? null:(Element)nl.item(0);
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void addLanguage(String language)
   {
      if(language==null || language.trim().length() < 1)
         throw new IllegalArgumentException("Language must not be null or empty");

      if(m_Languages.get(language) != null)
         return;

      Element prop = getTMXDocument().getDOMDocument().createElement(
         IPSTmxDtdConstants.ELEM_PROP);
      prop.setAttribute(IPSTmxDtdConstants.ATTR_TYPE,
         IPSTmxDtdConstants.ATTR_VAL_SUPPORTEDLANGUAGE);
      Text value = getTMXDocument().getDOMDocument().createTextNode(language);
      PSXmlDocumentBuilder.copyTree(prop.getOwnerDocument(), prop, value, false);
      PSXmlDocumentBuilder.copyTree(m_DOMElement.getOwnerDocument(),
         m_DOMElement, prop, false);
      m_Languages.put(language, new PSTmxProperty(m_PSTmxDocument, prop));
   }

   /*
    * implementation of the method defined in the interface
    */
   public void setProperty(int type, String value)
      throws PSTmxDomException
   {
      if(value==null)
         value = "";

      String attrib = null;
      try
      {
         attrib = IPSTmxHeader.PROPNAMEMAP[type];
      }
      catch(Exception e)
      {
         throw new PSTmxDomException("invalidPropTypeForHeader", "");
      }
      m_DOMElement.setAttribute(attrib, value);
   }

   /**
    *
    * @return array of language strings of of suported languages. May be
    * <code>null</code>.
    */
   public Object[] getSupportedLanguages()
   {
      return m_Languages.keySet().toArray();
   }

   /**
    * Map of all notes in the header. Never <code>null</code>. May be
    * <code>empty</code>.
    */
   protected Map m_Notes = new HashMap();
   /**
    * Map of all supported languages in the header. Never <code>null</code>.
    * May be <code>empty</code>. These are actually {@link IPSTmxProperty}
    * object with special attribute of "supportedlanguage".
    */
   protected Map m_Languages = new HashMap();

   /**
    * Node type value for the TMX header, constant
    */
   public static final int TMXNODETYPE = IPSTmxNode.TMXHEADER;

   /**
    * Node name value for the TMX header, constant
    */
   public static final String TMXNODENAME = IPSTmxNode.NODENAMEMAP[TMXNODETYPE];
}
