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
package com.percussion.i18n.tmxdom;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class wraps the the body part of the TMX document. One can modify the
 * body by adding or removing the translation units. Refer to the DTD of the TMX
 * document for more details of the structure of this node.
 * @see PSTmxNode
 */
public class PSTmxBody
   extends PSTmxNode
{
   /**
    * Map of all translation units of this node. A TMX body is just a set of
    * translation units.
    */
   protected Map m_TransUnits = new HashMap();

   /**
    * Name of this node. This is the element tag name of the DOM element
    * associated with this node.
    */
   public static final String TMXNODENAME =
      IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXBODY];

   /**
    * Convenience ctor that calls {@link #PSTmxBody(IPSTmxDocument, boolean)
    * this(TMXDoc, true)}.
    */
   PSTmxBody(IPSTmxDocument TMXDoc)
   {
      this(TMXDoc, true);
   }

   /**
    * Constructor that takes the TMX document object. Parses the document and
    * builds the translationa units. If the document does not have a body part,
    * It will add an empty one.
    * 
    * @param TMXDoc must not be <code>null</code>.
    * @param createDefault If <code>true</code>, a variant is also
    * added for the default language to each translation unit if it does not 
    * already exist.  If <code>false</code>, no defaults are added.
    * 
    * @throws IllegalArgumentException if supplied TMXDocument is 
    * <code>null</code>.
    */
   PSTmxBody(IPSTmxDocument TMXDoc, boolean createDefault)
   {
      if(TMXDoc == null)
      {
         throw new IllegalArgumentException(
            "TMXDoc must not be null in PSTmxBody constructor");
      }
      m_Parent = TMXDoc;
      m_PSTmxDocument = TMXDoc;
      Element elem = getBodyElement(m_PSTmxDocument);
      if(null ==elem)
      {
         m_DOMElement = m_PSTmxDocument.getDOMDocument().createElement(TMXNODENAME);
         PSXmlDocumentBuilder.copyTree(m_PSTmxDocument.getDOMDocument(),
            m_PSTmxDocument.getDOMDocument().getDocumentElement(),
            m_DOMElement, false);
      }
      else
      {
         m_DOMElement = elem;
      }
      processBodyElement(createDefault);
   }

   
   /**
    * Helper function to locate and return the body element of the TMX document.
    * @param TMXDocument must not be <code>null</code>.
    * @return the body DOM element. May be <code>null</code>.
    * @throws IllegalArgumentException if supplied TMXDocument is <code>null</code>.
    */
   private Element getBodyElement(IPSTmxDocument TMXDocument)
      throws IllegalArgumentException
   {
      if(TMXDocument == null)
      {
         throw new IllegalArgumentException(
            "TMXDocument must not be null in getBodyElement()");
      }
      NodeList nl = TMXDocument.getDOMElement().getElementsByTagName(TMXNODENAME);
      return (nl ==null || nl.getLength() < 1)? null:(Element)nl.item(0);
   }

   /**
    * Helper method that locates all translation units and parses to Translation
    * Unit objects.
    * 
    * @param createDefault If <code>true</code>, a variant is also
    * added for the default language to each translation unit if it does not 
    * already exist.  If <code>false</code>, no defaults are added.
    */
   private void processBodyElement(boolean createDefault)
   {
      NodeList nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXTRANSLATIONUNIT]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxTranslationUnit tu = new PSTmxTranslationUnit(m_PSTmxDocument, 
            (Element)nl.item(i), createDefault);
         m_TransUnits.put(tu.getKey(), tu);
      }
   }

   /**
    * Method to get the translation units of the body.
    * @return iterator of all translation units. Never <code>null</code>, may
    * be <code>empty</code>
    */
   protected Iterator getTraslationUnits()
   {
      return m_TransUnits.entrySet().iterator();
   }

   /* Implementation of the method from the interface <code>IPSTmxNode</code>.*/
   public void merge(IPSTmxNode node)
      throws PSTmxDomException
   {
      if(!(node instanceof IPSTmxTranslationUnit))
      {
         throw new PSTmxDomException("onlyOneTypeAllowedToMerge",
            "IPSTmxTranslationUnit");
      }
      IPSTmxTranslationUnit srcTu = (IPSTmxTranslationUnit)node;
      IPSTmxTranslationUnit temp = (IPSTmxTranslationUnit)
         m_TransUnits.get(srcTu.getKey());
      boolean exists = (temp!=null);
      PSTmxConfigParams options = getTMXDocument().getMergeConfig()
         .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_TU);
      /*
       *Apply merge rules here
       */
      if(exists)
      {
         if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_DELETEIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            removeTranslationUnit(srcTu);
         }
         else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_REPLACEIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            removeTranslationUnit(temp);
            addTranslationUnit(srcTu);
         }
         else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_MODIFYIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            temp.merge(srcTu);
         }
      }
      else
      {
         if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_ADDIFNOTEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            addTranslationUnit(srcTu);
         }
      }
   }

   /**
    * This method adds the supplied tanslation unit if it does not exist. Use
    * {@link #merge} to merge applying the merge rules.
    * @param    tu    translation unit object to be added. Must not be
    * <code>null</code>.
    * @throws PSTmxDomException when trying to add a translation unit that
    * already exists.
    */
   protected void addTranslationUnit(IPSTmxTranslationUnit tu)
      throws PSTmxDomException
   {
      if(tu==null)
      {
         throw new IllegalArgumentException("tu must not be null");
      }
      if(m_TransUnits.containsKey(tu.getKey())) //should this be ignored??
      {
         throw new PSTmxDomException("cannotAddTU", "");
      }
      Element newElemTu = (Element)PSXmlDocumentBuilder.copyTree(
       m_DOMElement.getOwnerDocument(), m_DOMElement, tu.getDOMElement(), true);
      m_TransUnits.put(
         tu.getKey(), new PSTmxTranslationUnit(getTMXDocument(), newElemTu));
   }

   /**
    * Method to remove a translation unit whose key matches with the supplied
    * one's.
    * @param    tu    translation unit object to be added. Must not be
    * <code>null</code>
    * @throws PSTmxDomException when trying to remove a nonexisting translation
    * unit.
    */
   protected void removeTranslationUnit(IPSTmxTranslationUnit tu)
      throws PSTmxDomException
   {
      IPSTmxTranslationUnit temp = (PSTmxTranslationUnit)
         m_TransUnits.get(tu.getKey());
      if(temp==null)//should this be ignored??
      {
         throw new PSTmxDomException("cannotRemoveTU", "");
      }
      //m_DOMElement.removeChild(temp.getDOMElement());
      if(temp.getDOMElement().getParentNode() == m_DOMElement)
         m_DOMElement.removeChild(temp.getDOMElement());
      m_TransUnits.remove(temp.getKey());
   }


}
