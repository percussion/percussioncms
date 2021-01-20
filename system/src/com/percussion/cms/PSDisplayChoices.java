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
package com.percussion.cms;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSChoiceFilter;
import com.percussion.design.objectstore.PSDisplayText;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a set of choices and an optional choice filter.
 * Handles serializing this data to and from its XML representation.
 *
 * @todo Refactor the PSChoiceBuilder to use this class if possible.
 */
public class PSDisplayChoices implements Cloneable
{
   /**
    * Construct this object from its member data.
    * 
    * @param choices An iterator over zero or more <code>PSEntry</code> objects
    * defining the value and label of the choices, may be <code>null</code> if 
    * the choices are to be supplied later. 
    * @param filter An optional filter to use to limit the choices based on
    * another field value, may be <code>null</code>.
    */
   public PSDisplayChoices(Iterator<PSEntry> choices, PSChoiceFilter filter)
   {
      if (choices != null)
      {
         m_choices = new ArrayList<PSEntry>();
         while (choices.hasNext())
         {
            PSEntry entry = choices.next();
            m_choices.add(entry);
         }         
      }
      
      m_filter = filter;
   }
   
   /**
    * Construct this object from its xml representation.  See
    * {@link #toXml(Document)} for the expected xml format for the
    * <code>src</code> element.
    *
    * @param src The src element, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSUnknownNodeTypeException if <code>src</code> does not have the
    * expected format.
    */
   public PSDisplayChoices(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      if (!XML_NODE_NAME.equals(src.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, src.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(src);

      // default to true 
      boolean choicesLoaded = !ATTR_VAL_FALSE.equalsIgnoreCase(
         src.getAttribute(CHOICES_LOADED_ATTR));
      
      // load choices
      if (choicesLoaded)
      {
         m_choices = new ArrayList<PSEntry>();
         Element entryEl = tree.getNextElement(DISPLAYENTRY_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (entryEl != null)
         {
            String val = null;
            Element valEl = tree.getNextElement(DISPLAYVALUE_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            if (valEl != null)
               val = tree.getElementData();
            tree.setCurrent(entryEl);
            
            String label = null;
            Element labelEl = tree.getNextElement(DISPLAYLABEL_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            if (labelEl != null)
               label = tree.getElementData();
            tree.setCurrent(entryEl);
            
            //If label is empty or the value is empty treat it as empty entry
            boolean emptyEntry =
               (val == null || val.trim().length() == 0 ||
                  label == null || label.trim().length() == 0);
            //Skip empty entries
            if(!emptyEntry)
               m_choices.add(new PSEntry(val, new PSDisplayText(label)));
            
            entryEl = tree.getNextElement(DISPLAYENTRY_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }
      // load filter
      tree.setCurrent(src);
      Element filterEl = tree.getNextElement(PSChoiceFilter.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (filterEl != null)
         m_filter = new PSChoiceFilter(filterEl, null, null);
   }
   
   /**
    * Determine if choices were supplied during construction.  
    * 
    * @return <code>true</code> if they were supplied, <code>false</code> if
    * not.  Note that {@link #getChoices()} may return an empty iterator in 
    * either case. 
    */
   public boolean areChoicesLoaded()
   {
      return m_choices != null;
   }

   /**
    * Serialize this object to its XML representation.
    *
    * @param doc The document to use, may not be <code>null</code>.

    * @return The element, never <code>null</code>.  The format returned is:
    *
    *  &lt;!ELEMENT DisplayChoices (DisplayEntry*, PSXChoiceFilter?)>
    *  &lt;!ATTLIST DisplayChoices
    *     areChoicesLoaded (yes | no ) "yes"
    *  >
    *  &lt;!ELEMENT DisplayEntry (Value, DisplayLabel)>
    *  &lt;!ELEMENT Value (#PCDATA)>
    *  &lt;!ELEMENT DisplayLabel (#PCDATA)>
    *
    * @todo Bring this into compliance with the DisplayChoice element in the
    * sys_ContentEditor.dtd if the <code>PSChoiceBuilder</code> is refactored
    * to use this class.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element dispChoicesEl = doc.createElement(XML_NODE_NAME);
      boolean choicesLoaded = m_choices != null;
      
      dispChoicesEl.setAttribute(CHOICES_LOADED_ATTR, choicesLoaded ? 
         ATTR_VAL_TRUE : ATTR_VAL_FALSE);
      
      if (choicesLoaded)
      {
         Iterator<PSEntry> choices = m_choices.iterator();
         while (choices.hasNext())
         {
            PSEntry entry = choices.next();
            dispChoicesEl.appendChild(createDisplayEntry(doc, entry.getValue(),
               entry.getLabel().getText()));
         }         
      }


      if (m_filter != null)
         dispChoicesEl.appendChild(m_filter.toXml(doc));

      return dispChoicesEl;
   }

   /**
    * Get the list of choices.
    *
    * @return An iterator over zero or more <code>PSEntry</code> objects, never
    * <code>null</code>, may be empty if there are no choices available, or if
    * they were not supplied during construction.  
    * Use {@link #areChoicesLoaded()} to determine the meaning of an empty 
    * result. 
    */
   public Iterator<PSEntry> getChoices()
   {
      if (m_choices == null)
         return PSIteratorUtils.emptyIterator();
      
      return m_choices.iterator();
   }

   /**
    * Get the choice filter. This may be used to filter the set of choices,
    * possibly based on other values.
    *
    * @return The choice filter, may be <code>null</code> if none supplied.
    */
   public PSChoiceFilter getChoiceFilter()
   {
      return m_filter;
   }

   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   //see base class for description
   public Object clone()
   {
      PSDisplayChoices copy = null;

      try
      {
         copy = (PSDisplayChoices) super.clone();
         List<PSEntry> choices = null;
         if (m_choices != null)
         {
            choices = new ArrayList<PSEntry>();
            Iterator<PSEntry> entries = m_choices.iterator();
            while (entries.hasNext())
            {
               PSEntry entry = entries.next();
               choices.add((PSEntry)entry.clone());
            }
         }
         copy.m_choices = choices;
      }
      catch (CloneNotSupportedException e) {} // cannot happen

      return copy;
   }

   // see base class for description
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /**
    * Creates a new DisplayEntry node.
    *
    * @param doc the document in which to create the new element, assumed
    *    not <code>null</code>.
    * @param value the value string of the DisplayEntry to create, assumed
    *    not <code>null</code>.
    * @param label the label string of the DisplayEntry to create, assumed
    *    not <code>null</code>.
    *
    * @return the newly created DisplayElement, never <code>null</code>.
    */
   private static Element createDisplayEntry(Document doc, String value,
      String label)
   {
      Element displayEntry = doc.createElement(DISPLAYENTRY_NAME);
      Element displayValue = doc.createElement(DISPLAYVALUE_NAME);
      Element displayLabel = doc.createElement(DISPLAYLABEL_NAME);

      displayValue.appendChild(doc.createTextNode(value));
      displayLabel.appendChild(doc.createTextNode(label));
      displayEntry.appendChild(displayValue);
      displayEntry.appendChild(displayLabel);

      return displayEntry;
   }

   /** The element name used for choices to produce the output XML. */
   public static final String XML_NODE_NAME = "DisplayChoices";

   /**
    * A list of <code>PSEntry</code> objects, <code>null</code> if choices 
    * are not supplied, may be empty.  Intialized during the ctor if choices are
    * supplied, never modified after that.
    */
   private List<PSEntry> m_choices = null;

   /**
    * Optional filter supplied during construction, may be <code>null</code>,
    * never modified after that.
    */
   private PSChoiceFilter m_filter = null;

   // private xml constants
   private static final String DISPLAYENTRY_NAME = "DisplayEntry";
   private static final String DISPLAYVALUE_NAME = "Value";
   private static final String DISPLAYLABEL_NAME = "DisplayLabel";
   private static final String CHOICES_LOADED_ATTR = "areChoicesLoaded";
   private static final String ATTR_VAL_TRUE = "yes";
   private static final String ATTR_VAL_FALSE = "no";

}
