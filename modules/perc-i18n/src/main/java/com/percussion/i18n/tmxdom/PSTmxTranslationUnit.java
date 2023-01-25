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
package com.percussion.i18n.tmxdom;

import com.percussion.i18n.PSI18nUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class wraps the functionality of translation unit DOM element as
 * an easy to use TMX node. The TMX counterpart provides methods to manipulate
 * the translation unit, the most important one being to merge two nodes
 * appying the merge configuration.
 */
public class PSTmxTranslationUnit
   extends PSTmxNode
   implements IPSTmxTranslationUnit
{
   /**
    * Convenience version that calls 
    * {@link #PSTmxTranslationUnit(IPSTmxDocument, Element, boolean} 
    * PSTmxTranslationUnit(tmxdoc, tu, true)}.
    */
   PSTmxTranslationUnit(IPSTmxDocument tmxdoc, Element tu)
   {
      this(tmxdoc, tu, true);
   }
   
   /**
    * Constructor. Takes the parent TMX document object and the DOM element
    * representing the translation unit. All the components of the translation
    * unit variant are optionally constructed from the given DOM element.
    * 
    * @param tmxdoc parent TMX document, nust not be <code>null</code>.
    * @param tu DOM element for the translation unit to be contstructed, must
    * not be <code>null</code>.
    * @param createDefault If <code>true</code>, a variant is also
    * added for the default language if it does not already exist.  If 
    * <code>false</code>, no default is  added.
    * 
    * @throws IllegalArgumentException if tmxDoc or tu is <code>null</code>
    */
   PSTmxTranslationUnit(IPSTmxDocument tmxdoc, Element tu, 
      boolean createDefault)
   {
      if(tmxdoc == null)
         throw new IllegalArgumentException("tmxdoc must not be null");
      if(tu == null)
         throw new IllegalArgumentException("tu must not be null");

      m_DOMElement = tu;
      m_PSTmxDocument = tmxdoc;

      NodeList nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXTRANSLATIONUNITVARIANT]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxTranslationUnitVariant tuv = new PSTmxTranslationUnitVariant(
            m_PSTmxDocument, (Element)nl.item(i));
         m_TransUnitVariants.put(tuv.getLang(), tuv);
      }

      nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXNOTE]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxNote note = new PSTmxNote(m_PSTmxDocument, 
            (Element)nl.item(i));
         m_Notes.put(note.getLang(), note);
      }

      nl = m_DOMElement.getElementsByTagName(
         IPSTmxNode.NODENAMEMAP[IPSTmxNode.TMXPROPERTY]);
      for(int i=0; nl!=null && i<nl.getLength(); i++)
      {
         PSTmxProperty prop =
            new PSTmxProperty(m_PSTmxDocument, (Element)nl.item(i));
         m_Properties.put(prop.getType(), prop);
      }
         
      if (createDefault)
      {
         
         IPSTmxTranslationUnitVariant variant = (PSTmxTranslationUnitVariant)
            m_TransUnitVariants.get(PSI18nUtils.DEFAULT_LANG);
         if(variant==null)
         {
            variant = getTMXDocument().createTranslationUnitVariant(
               PSI18nUtils.DEFAULT_LANG,
               PSI18nUtils.getLastSubKey(getKey()));
            addTuv(variant, false);
         }
         else
         {
            variant.addSegment(getTMXDocument()
               .createSegment(PSI18nUtils.getLastSubKey(getKey())));
         }
      }
   }

   /*
    * Implementation of the method defined in the interface
    */
   public String getKey()
   {
      return m_DOMElement.getAttribute(IPSTmxDtdConstants.ATTR_TUID);
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
   public Iterator getTransUnitVariants()
   {
      return m_TransUnitVariants.entrySet().iterator();
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.i18n.tmxdom.IPSTmxTranslationUnit#getTransUnitVariant(java.lang.String)
    */
   public IPSTmxTranslationUnitVariant getTransUnitVariant(String lang)
   {
      if (lang == null || lang.trim().length() == 0)
      {
         throw new IllegalArgumentException("lang may not be null or empty");
      }
      return (IPSTmxTranslationUnitVariant) m_TransUnitVariants.get(lang);
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void merge(IPSTmxNode node)
      throws PSTmxDomException
   {
      if(!(node instanceof IPSTmxTranslationUnit))
      {
         throw new PSTmxDomException("onlyOneTypeAllowedToMerge",
            "IPSTmxTranslationUnit");
      }
      IPSTmxTranslationUnit srcTu = (IPSTmxTranslationUnit)node;
      mergeNotes(srcTu);
      mergeProperties(srcTu);
      mergeTuvs(srcTu);
   }

   /**
    * Helper method to merge the TMX note object from the supplied TMX
    * translation unit variant object with that of the current TMX node applying
    * the merge rules. Uniqueness of the note object is established based on
    * the language value of the note object.
    * @param tu translation unit variant object to be merged. Must not be
    * <code>null</code>.
    * @throws IllegalArgumentException if tu is <code>null</code>
    */
   private void mergeNotes(IPSTmxTranslationUnit tu)
   {
      if(tu == null)
      {
         throw new IllegalArgumentException("tu object must not be null");
      }
      Map.Entry entry = null;
      IPSTmxNote noteSrc = null;
      IPSTmxNote note = null;
      boolean exists = false;
      Iterator iter = tu.getNotes();
      while(iter.hasNext())
      {
         entry = (Map.Entry)iter.next();
         noteSrc = (IPSTmxNote)entry.getValue();
         note = (IPSTmxNote)m_Notes.get(noteSrc.getLang());
         exists = (note!=null);
         PSTmxConfigParams options = getTMXDocument().getMergeConfig()
            .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_TU_NOTE);
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
               addNote(noteSrc, false);
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
               addNote(noteSrc, false);
            }
         }
      }
   }

   /**
    * Helper method to merge the TMX property object from the supplied TMX
    * translation unit object with that of the current TMX node applying the
    * merge rules. Uniqueness of the property object is established based on
    * the property type values.
    * @param tu translation unit object to be merged. Must not be <code>null</code>.
    * @throws IllegalArgumentException if tu is <code>null</code>
    */
   protected void mergeProperties(IPSTmxTranslationUnit tu)
   {
      if(tu == null)
      {
         throw new IllegalArgumentException("tu object must not be null");
      }
      Map.Entry entry = null;
      IPSTmxProperty propSrc = null;
      IPSTmxProperty prop = null;
      boolean exists = false;
      Iterator iter = tu.getProperties();
      while(iter.hasNext())
      {
         entry = (Map.Entry)iter.next();
         propSrc = (IPSTmxProperty)entry.getValue();
         prop = (IPSTmxProperty)m_Properties.get(propSrc.getType());
         exists = (prop!=null);
         PSTmxConfigParams options = getTMXDocument().getMergeConfig()
            .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_TU_PROPERTY);
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
    * Helper method to merge the TMX translation unit variant object from the
    * supplied TMX translation unit object with that of the current TMX node
    * applying the merge rules. Uniqueness of the property object is established
    * based on language value associated with node.
    * @param tu translation unit object to be merged. Must not be <code>null</code>.
    * @throws IllegalArgumentException if tu is <code>null</code>
    */
   protected void mergeTuvs(IPSTmxTranslationUnit tu)
   {
      if(tu == null)
      {
         throw new IllegalArgumentException("tu object must not be null");
      }
      Map.Entry entry = null;
      IPSTmxTranslationUnitVariant tuvSrc = null;
      IPSTmxTranslationUnitVariant tuv = null;
      boolean exists = false;
      Iterator iter = tu.getTransUnitVariants();
      Object o = null;
      while(iter.hasNext())
      {
         entry = (Map.Entry)iter.next();
         tuvSrc = (IPSTmxTranslationUnitVariant)entry.getValue();
         o = m_TransUnitVariants.get(tuvSrc.getLang());
         tuv = (IPSTmxTranslationUnitVariant)o;
         exists = (tuv!=null);
         PSTmxConfigParams options = getTMXDocument().getMergeConfig()
            .getConfigParams(IPSTmxMergeConfig.MERGE_NODEID_TUV);
         if(exists)
         {
            if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_DELETEIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               removeTuv(tuv);
            }
            else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_REPLACEIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               removeTuv(tuv);
               addTuv(tuvSrc, false);
            }
            else if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_MODIFYIFEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               tuv.merge(tuvSrc);
            }
         }
         else
         {
            if(options.getParam(IPSTmxMergeConfig.MERGE_OPTION_ADDIFNOTEXISTS)
               .equalsIgnoreCase(IPSTmxMergeConfig.YES))
            {
               addTuv(tuvSrc, false);
            }
         }
      }
   }

   /**
    * Helper method to remove the translation unit variant node matching with
    * supplied TMX Node.
    * @param tuv TMX translation unit variant to be removed from this node.
    * Nothing happens if <code>null</code> is supplied or matching node does not
    * exist in the current node. Two translation unit variants are assumed to be
    * matching if the language associated with the nodes are the same.
    */
   private void removeTuv(IPSTmxTranslationUnitVariant tuv)
   {
      if(tuv == null)
         return;

      IPSTmxTranslationUnitVariant tuv1 = (IPSTmxTranslationUnitVariant)
         m_TransUnitVariants.get(tuv.getLang());
      if(tuv1==null)
         return;

      //m_DOMElement.removeChild(tuv1.getDOMElement());
      if(tuv1.getDOMElement().getParentNode() == m_DOMElement)
         m_DOMElement.removeChild(tuv1.getDOMElement());
      m_TransUnitVariants.remove(tuv1.getLang());
   }

   /**
    * Helper method to remove the note node matching with supplied TMX Node.
    * @param note TMX note to be removed from this node. Nothing happens if
    * <code>null</code> is supplied or matching note does not exist in the
    * current node. Two notes are assumed to be matching if the language
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
    * current node.Two properties are assumed to be matching if the type
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
   public void addTuv(IPSTmxTranslationUnitVariant tuv, boolean overwrite)
   {
      if(tuv == null)
         return;

      boolean containsKey = m_TransUnitVariants.containsKey(tuv.getLang());
      if(!containsKey || (containsKey && overwrite))
      {
         if (containsKey)
            removeTuv(tuv);
         
         Element newElemTuv = (Element)PSXmlDocumentBuilder.copyTree(
            m_DOMElement.getOwnerDocument(), m_DOMElement,
            tuv.getDOMElement(), false);
         m_TransUnitVariants.put(tuv.getLang(),
            new PSTmxTranslationUnitVariant(getTMXDocument(), newElemTuv));
      }
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void addNote(IPSTmxNote note, boolean overwrite)
   {
      if(note == null)
         return;

      boolean containsKey = m_Notes.containsKey(note.getLang());
      if(!containsKey || (containsKey && overwrite))
      {
         if (containsKey)
            removeNote(note);
        
         Element newElemNote = (Element)PSXmlDocumentBuilder.copyTree(
            m_DOMElement.getOwnerDocument(),
            m_DOMElement, note.getDOMElement(), false);
         m_Notes.put(note.getLang(),
            new PSTmxNote(getTMXDocument(), newElemNote));
      }
   }

   /*
    * Implementation of the method defined in the interface
    */
   public void addProperty(IPSTmxProperty prop)
   {
      if(prop == null)
         return;

      if(!m_Properties.containsKey(prop.getType()))
      {
         Element newProp = (Element)PSXmlDocumentBuilder.copyTree(
            m_DOMElement.getOwnerDocument(),
            m_DOMElement, prop.getDOMElement(), false);
         m_Properties.put(prop.getType(),
            new PSTmxProperty(getTMXDocument(), newProp));
      }
   }

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
    * Map of all TMX translation unit variants associated with this node.
    * Never <code>null</code>, can be <code>empty</code>.
    */
   protected Map m_TransUnitVariants = new HashMap();
}
