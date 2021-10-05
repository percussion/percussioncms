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
package com.percussion.cx;

import com.percussion.cx.error.IPSContentExplorerErrors;
import com.percussion.cx.error.PSContentExplorerException;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents the Options element in the sys_Options.dtd.
 */
public class PSOptions implements IPSClientObjects
{
   /**
    * Constructs an empty options object based on the specified category, ready 
    * to take on new option objects.
    * 
    * @param category the name of the category for this set of options, must
    *    not be <code>null</code> or empty.
    */
   public PSOptions(String category)
   {
      if (category == null || category.trim().length() == 0)
         throw new IllegalArgumentException("category must not be null or empty");

      setCategory(category);
   }

   /**
    * Creates an instance from the XML representation specified in the
    * dtd mentioned in the  class description.
    *
    * @param optionsElement - must not be <code>null</code>, must be defined
    * as specified in the dtd list in the class description.
    * @throws PSContentExplorerException - if optionsElement has an invalid
    * definition.
    */
   public PSOptions(Element optionsElement) throws PSContentExplorerException
   {
      fromXml(optionsElement);
   }

   /** @see IPSClientObjects */
   public void fromXml(Element sourceNode) throws PSContentExplorerException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("optionsElement must not be null");

      try
      {
         // validate the root element
         PSXMLDomUtil.checkNode(sourceNode, ELEM_OPTIONS);

         // get category attribute (required)
         setCategory(
            PSXMLDomUtil.checkAttribute(sourceNode, ATTR_CATEGORY, true));

         // get list of options and create:
         Element option = PSXMLDomUtil.getFirstElementChild(sourceNode);
         PSOption theOption = null;
         while (option != null)
         {
            theOption = new PSOption(option);
            addOption(theOption.getOptionId(), theOption);
            option = PSXMLDomUtil.getNextElementSibling(option);
         }
      }
      catch (Exception e)
      {
         throw new PSContentExplorerException(
            IPSContentExplorerErrors.MISC_PROCESSING_OPTIONS_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Adds the option represented by the supplied parameters to its list of 
    * options. Replaces any option represented by <code>optionId</code> if it
    * exists.
    * 
    * @param context the context of the option, may not be <code>null</code> or
    * empty.
    * @param optionId the unique identifier of the option, may not be <code>null
    * </code> or empty.
    * @param optionValue the option value, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public void addOption(String context, String optionId, Object optionValue)
   {
      if (context == null || context.trim().length() == 0)
         throw new IllegalArgumentException("context may not be null or empty.");

      if (optionId == null || optionId.trim().length() == 0)
         throw new IllegalArgumentException("optionId may not be null or empty.");

      if (optionValue == null)
         throw new IllegalArgumentException("optionValue may not be null.");

      PSOption option = new PSOption(context, optionId, optionValue);
      addOption(optionId, option);
   }

   /** @see IPSClientObjects */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = doc.createElement(ELEM_OPTIONS);
      root.setAttribute(ATTR_CATEGORY, getCategory());

      PSOptionManagerConstants.toXmlCollection(root, doc, m_optionMap.values());

      return root;
   }

   /**
    * The key will be the optionId of the PSOption and stored as is,
    * therefore it is case sensitive and the PSOption itself
    * will be the value.
    *
    * @param theOptionId  must not be <code>null</code> or empty.
    * @param option must not be <code>null</code>
    */
   private void addOption(String theOptionId, PSOption option)
   {
      if (option == null)
         throw new IllegalArgumentException("option must not be null");

      m_optionMap.put(theOptionId, option);
   }

   /**
    * Returns an iterator of optionId's of the <code>PSOption</code> this
    * objects contains.
    *
    * @return an unmodifiable <code>Iterator</code> all of the
    * <code>PSOption</code> optionId's as <code>Strings</code>.  May be empty
    * but not <code>null</code>.
    *
    */
   public Iterator getOptionCategories()
   {
      return Collections.unmodifiableSet(m_optionMap.keySet()).iterator();
   }

   /**
    * Returns a <code>PSOption</code> object specified by the case-sensitive
    * optionid.
    *
    * @param optionid must not be <code>null</code> or empty.
    * @return the <code>PSOption</code> specified, any modifications to the
    * <code>PSOption</code> will affect this class, may be <code>null</code>
    * if not found.
    */
   public PSOption getOption(String optionid)
   {
      if (optionid == null || optionid.trim().length() == 0)
         throw new IllegalArgumentException("optionid must not be null or empty");

      return (PSOption)m_optionMap.get(optionid);
   }

   /**
    * Gets the category field.
    *
    * @return the category never <code>null</code> or empty.
    */
   public String getCategory()
   {
      return m_category;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * Overrides the method in {@link Object.equals(Object) Object} and adheres
    * to that contract.
    * @param obj the reference object with which to compare.
    * @return <code>true</code> if this object is the same as the
    * <code>obj</code> argument; <code>false</code> otherwise. If
    * <code>null</code> supplied or obj is not an instance of this class,
    * <code>false</code> is returned.
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(getClass().isInstance(obj)))
         return false;

      PSOptions comp = (PSOptions)obj;

      if (!PSOptionManagerConstants.compare(m_category, comp.m_category))
         return false;
      if (!PSOptionManagerConstants.compare(m_optionMap, comp.m_optionMap))
         return false;

      return true;
   }

   /**
    * Overridden to fulfill contract of this method as described in
    * {@link Object#hashCode() Object}.
    *
    * @return A hash code value for this object
    */
   public int hashCode()
   {
      int hash = 0;

      hash += m_category.hashCode();
      hash += m_optionMap.hashCode();

      return hash;
   }

   /**
    * Sets the category field.
    *
    * @param theCategory must not <code>null</code> or empty.
    */
   private void setCategory(String theCategory)
   {
      if (theCategory == null || theCategory.trim().length() == 0)
         throw new IllegalArgumentException("theCategory must not be null or empty");

      m_category = theCategory;
   }

   /**
    * This holds the category value, this never <code>null</code>, set by
    * <code>setCategory()</code>.
    */
   private String m_category = "";

   /**
    * A Map of PSOption objects, never <code>null</code>, may be empty, keys
    * are stored as is --  case sensitive.
    * @see #addOption(String, PSOption)
    */
   private Map m_optionMap = new HashMap();

   /**
    * Name of the element holding the options for one category of options.
    */
   private static final String ELEM_OPTIONS = "PSXOptions";

   /**
    * Attribute name for the element ELEM_OPTIONS representing the category of
    * options group.
    */
   private static final String ATTR_CATEGORY = "category";
}
