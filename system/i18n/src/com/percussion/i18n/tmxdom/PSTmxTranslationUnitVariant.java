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

import com.percussion.i18n.PSI18nUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class wraps the functionality of translation unit variant DOM element as
 * an easy to use TMX node. The TMX counterpart provides methods to manipulate
 * the translation unit variant the most important one being to merge two nodes
 * appying the merge configuration.
 */
public class PSTmxTranslationUnitVariant
   extends PSTmxNode
   implements IPSTmxTranslationUnitVariant
{
   /**
    * Constructor. Takes the parent TMX document object and the DOM element
    * representing the translation unit variant. All the components of the
    * translation unit variant are constructed from the given DOM element.
    * @param tmxdoc parent TMX document, nust not be <code>null</code>.
    * @param tuv DOM element for the translation unit variant to be
    * contstructed, must not be <code>null</code>.
    * @throws IllegalArgumentException if tmxDoc or tuv is <code>null</code>
    */
   PSTmxTranslationUnitVariant(IPSTmxDocument tmxdoc, Element tuv)
   {
      if(tmxdoc == null)
         throw new IllegalArgumentException("tmxdoc must not be null");
      if(tuv == null)
         throw new IllegalArgumentException("tuv must not be null");

      m_DOMElement = tuv;
      m_PSTmxDocument = tmxdoc;

      m_Lang = m_DOMElement.getAttribute(IPSTmxDtdConstants.ATTR_XML_LANG);
      NodeList nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXSEGMENT]);
      if(nl!=null && nl.getLength()>0)
         m_Segment = new PSTmxSegment(m_PSTmxDocument, (Element)nl.item(0));

      nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXNOTE]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxNote note = new PSTmxNote(m_PSTmxDocument, (Element)nl.item(i));
         m_Notes.put(note.getLang(), note);
      }

      nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXPROPERTY]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxProperty prop = new PSTmxProperty(m_PSTmxDocument,
            (Element)nl.item(i));
         m_Properties.put(prop.getType(), prop);
      }
   }

   /*
    * Implementation of the method defined in the interface
    */
   public Iterator getNotes()
   {
      return m_Notes.entrySet().iterator();
   }

   /*
    * Implementation of the method defined in the interface
    */
   public Iterator getProperties()
   {
      return m_Properties.entrySet().iterator();
   }

   /*
    * Implementation of the method defined in the interface
    */
   public IPSTmxSegment getSegment()
   {
      return m_Segment;
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void addNote(IPSTmxNote note)
   {
      if(note == null)
         return;

      if(!m_Notes.containsKey(note.getLang()))
      {
         Element newElemNote = (Element)PSXmlDocumentBuilder.copyTree(
            m_DOMElement.getOwnerDocument(),
            m_DOMElement, note.getDOMElement(), true);
         m_Notes.put(
            note.getLang(), new PSTmxNote(getTMXDocument(), newElemNote));
      }
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void addProperty(IPSTmxProperty property)
   {
      if(property == null)
         return;

      if(!m_Properties.containsKey(property.getType()))
      {
         Element newProperty = (Element)PSXmlDocumentBuilder.copyTree(
            m_DOMElement.getOwnerDocument(),
            m_DOMElement, property.getDOMElement(), false);
         m_Properties.put(property.getType(),
            new PSTmxProperty(getTMXDocument(), newProperty));
      }
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void addSegment(IPSTmxSegment segment)
   {
      if(segment == null)
         return;
      //already exists?
      if(!(m_Segment == null))
         return;
      Element newSegment = (Element)PSXmlDocumentBuilder.copyTree(
         m_DOMElement.getOwnerDocument(),
         m_DOMElement, segment.getDOMElement(), false);
      m_Segment = new PSTmxSegment(getTMXDocument(), newSegment);
   }

   /**
    * Helper method to remove the segment node matching with supplied TMX Node.
    * @param segment TMX note to be removed from this node. Nothing happens if
    * <code>null</code> is supplied or matching segment does not exist in the
    * current node. Two segments are assumed to be matching if the values are
    * the same.
    */
   private void removeSegment(IPSTmxSegment segment)
   {
      if(m_Segment == null)
         return;
      //if values match set the value to empty. Never remove the segment
      //as a whole.
      if(m_Segment.getValue().equals(segment.getValue()))
         m_Segment.setValue("");
   }

   /**
    * Helper method to remove the note node matching with supplied TMX Node.
    * @param note TMX note to be removed from this node. Nothing happens if
    * <code>null</code> is supplied or matching note does not exist in the
    * current node. Two notes are assumed to be matcheding if the language
    * associated with the notes are the same.
    */
   private void removeNote(IPSTmxNote note)
   {
      if(note == null)
         return;

      IPSTmxNote note1 = (IPSTmxNote)m_Notes.get(note.getLang());
      if(note1==null)
         return;
      //m_DOMElement.removeChild(note1.getDOMElement());
      if(note1.getDOMElement().getParentNode() == m_DOMElement)
         m_DOMElement.removeChild(note1.getDOMElement());
      m_Notes.remove(note1.getLang());
   }

   /**
    * Helper method to remove the property node matching with supplied TMX Node.
    * @param prop TMX property to be removed from this node. Nothing happens if
    * <code>null</code> is supplied or matching property does not exist in the
    * current node.Two properties are assumed to be matcheding if the type
    * associated with the properties are the same.
    */
   private void removeProperty(IPSTmxProperty prop)
   {
      if(prop == null)
         return;

      IPSTmxProperty prop1 = (IPSTmxProperty)m_Properties.get(prop.getType());
      if(prop1==null)
         return;
      //m_DOMElement.removeChild(prop.getDOMElement());
      if(prop.getDOMElement().getParentNode() == m_DOMElement)
         m_DOMElement.removeChild(prop.getDOMElement());
      m_Properties.remove(prop.getType());
   }

   /*
    * Implementation of the method defined in the interface
    */
   public String getLang()
   {
      return m_Lang;
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void merge(IPSTmxNode node)
      throws PSTmxDomException
   {
      if(!(node instanceof IPSTmxTranslationUnitVariant))
      {
         throw new PSTmxDomException("onlyOneTypeAllowedToMerge",
            "IPSTmxTranslationUnitVariant");
      }
      IPSTmxTranslationUnitVariant srcTuv = (IPSTmxTranslationUnitVariant)node;
      mergeNotes(srcTuv);
      mergeProperties(srcTuv);
      mergeSegment(srcTuv);
   }

   /**
    * Helper method to merge the TMX note object from the supplied TMX
    * translation unit variant object with that of the current TMX node applying
    * the merge rules. Uniqueness of the note object is established based on
    * the language value of the note object.
    * @param tuv translation unit variant object to be merged. Must not be
    * <code>null</code>.
    * @throws IllegalArgumentException if tuv is <code>null</code>
    */
   private void mergeNotes(IPSTmxTranslationUnitVariant tuv)
   {
      if(tuv == null)
      {
         throw new IllegalArgumentException("tuv object must not be null");
      }
      Map.Entry entry = null;
      IPSTmxNote noteSrc = null;
      IPSTmxNote note = null;
      boolean exists = false;
      Iterator iter = tuv.getNotes();
      while(iter.hasNext())
      {
         entry = (Map.Entry)iter.next();
         noteSrc = (IPSTmxNote)entry.getValue();
         note = (IPSTmxNote)m_Notes.get(noteSrc.getLang());
         exists = (note!=null);
         PSTmxConfigParams options = getTMXDocument().getMergeConfig()
            .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_TUV_NOTE);
         if(exists)
         {
            if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_DELETEIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               removeNote(note);
            }
            else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_REPLACEIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               removeNote(note);
               addNote(noteSrc);
            }
            else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_MODIFYIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               note.merge(noteSrc);
            }
         }
         else
         {
            if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_ADDIFNOTEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               addNote(noteSrc);
            }
         }
      }
   }

   /**
    * Helper method to merge the TMX property object from the supplied TMX
    * translation unit variant object with that of the current TMX node applying
    * the merge rules. Uniqueness of the property object is established based on
    * the property type values.
    * @param tuv translation unit variant object to be merged. Must not be
    * <code>null</code>.
    * @throws IllegalArgumentException if tuv is <code>null</code>
    */
   private void mergeProperties(IPSTmxTranslationUnitVariant tuv)
   {
      if(tuv == null)
      {
         throw new IllegalArgumentException("tuv object must not be null");
      }
      Map.Entry entry = null;
      IPSTmxProperty propSrc = null;
      IPSTmxProperty prop = null;
      boolean exists = false;
      Iterator iter = tuv.getProperties();
      while(iter.hasNext())
      {
         entry = (Map.Entry)iter.next();
         propSrc = (IPSTmxProperty)entry.getValue();
         prop = (IPSTmxProperty)m_Properties.get(propSrc.getType());
         exists = (prop!=null);
         PSTmxConfigParams options = getTMXDocument().getMergeConfig()
            .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_TUV_PROPERTY);
         if(exists)
         {
            if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_DELETEIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               removeProperty(prop);
            }
            else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_REPLACEIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               removeProperty(prop);
               addProperty(propSrc);
            }
            else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_MODIFYIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               prop.merge(propSrc);
            }
         }
         else
         {
            if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_ADDIFNOTEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               addProperty(propSrc);
            }
         }
      }
   }

   /**
    * Helper method to merge the TMX segment object from the supplied TMX
    * translation unit variant object with that of the current TMX node applying
    * the merge rules. Uniqueness of the segment object is established based on
    * the values of the segments.
    * @param tuv translation unit variant object to be merged. Must not be
    * <code>null</code>.
    * @throws IllegalArgumentException if tuv is <code>null</code>
    */
   private void mergeSegment(IPSTmxTranslationUnitVariant tuv)
   {
      if(tuv == null)
      {
         throw new IllegalArgumentException("tuv object must not be null");
      }
      IPSTmxSegment seg = tuv.getSegment();
      boolean exists = (m_Segment!=null);
      PSTmxConfigParams options = getTMXDocument().getMergeConfig()
         .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_SEGMENT);
      if(exists)
      {
         if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_DELETEIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            //Should never delete, instead make it empty
            m_Segment.setValue("");
         }
         else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_REPLACEIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            removeSegment(m_Segment);
            addSegment(seg);
         }
         else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_MODIFYIFEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            m_Segment.merge(seg);
         }
      }
      else
      {
         if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_ADDIFNOTEXISTS)
            .equalsIgnoreCase(IPSTmxMergeConfig.YES))
         {
            addSegment(seg);
         }
      }
   }

   /**
    * TMX segment object associated with this node. Normally not <code>null</code>
    * after construction, but it is possible to have a <code>null</code> value.
    */
   protected IPSTmxSegment m_Segment = null;

   /**
    * Map of all TMX notes associated with this node. Never <code>null</code>,
    * can be <code>empty</code>.
    */
   protected Map m_Notes = new HashMap();

   /**
    * Map of all TMX properties associated with this node. Never <code>null</code>,
    * can be <code>empty</code>.
    */
   protected Map m_Properties = new HashMap();

   /**
    * The default language this node is associated with. Every translation
    * variant must be associated with a language.
    */
   protected String m_Lang = PSI18nUtils.DEFAULT_LANG;
}
