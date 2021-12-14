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

package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * The PSComponentSummaries is a container class which contains a set of
 * PSComponentSummary objects
 */
public class PSComponentSummaries extends PSDbComponentSet
{
   /**
    * Default constructor.
    */
   public PSComponentSummaries()
   {
      super(PSComponentSummary.class);
   }

   /**
    * Ctor that takes an array of PSComponentSummary objects.
    * @param compArray array of PSComponentSummary objects,
    * never <code>null</code> may be <code>empty</code>. 
    */
   public PSComponentSummaries(PSComponentSummary[] compArray)
   {
      super(PSComponentSummary.class);
      
      if (compArray == null)
         throw new IllegalArgumentException("compArray may not be null");

      addAll(Arrays.asList(compArray));
   }

   /**
    * Creates an instance from a an array of Elements that was created
    * by a sequence of calls to PSComponentSummary.toXML();
    *
    * @param source A valid array of elements that meet the dtd defined in the
    * PSComponentSummary of {@link PSComponentSummary#toXml(Document)},
    * never <code>null</code>. 
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSComponentSummaries(Element[] source) throws PSUnknownNodeTypeException
   {
      super(PSComponentSummary.class);

      for (int i = 0; i < source.length; i++)
         super.add(new PSComponentSummary(source[i]));
   }

   /**
    * Creates an instance from a list of Elements that was created
    * by a sequence of calls to PSComponentSummary.toXML();
    *
    * @param source A valid list of {@link Element} objects that meet the dtd 
    *    defined in the PSComponentSummary of 
    *    {@link PSComponentSummary#toXml(Document)}, never <code>null</code>. 
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSComponentSummaries(List source) throws PSUnknownNodeTypeException
   {
      super(PSComponentSummary.class);

      Iterator elemts = source.iterator();
      while (elemts.hasNext())
      {
         Object elem = elemts.next();
         if (! (elem instanceof Element))
            throw new IllegalArgumentException(
                  "source must contain a list of Element objects");
         
         try
         {
         super.add(new PSComponentSummary((Element)elem));
         }
         catch (Exception e)
         {
            throw new PSUnknownNodeTypeException(0, e.toString());
         }
      }
   }
   
   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) one.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSComponentSummaries(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * Adds a component summary object to the summary list.
    *
    * @param summary The to be added object, it may not be <code>null</code>.
    */
   public void add(PSComponentSummary summary)
   {
      if (summary == null)
         throw new IllegalArgumentException("summary may not be null");

      super.add(summary);
   }

   /**
    * Get the component summary objects for a specified type
    *
    * @param type The type of the returned component. It must be
    *    <code>TYPE_XXX</code>.
    *
    * @return An iterator over <code>0</code> or more
    *    <code>PSComponentSummary</code> objects.
    */
   public Iterator getComponents(int type)
   {
      return getComponents(type, PSComponentSummary.GET_SUMMARY).iterator();
   }

   /**
    * Just like {@link #getComponents(int)}, except it returns a list.
    *
    * @return A list of <code>PSComponentSummary</code> objects, never
    *    <code>null</code>, but may be empty.
    */
   public List getComponentList(int type)
   {
      return getComponents(type, PSComponentSummary.GET_SUMMARY);
   }

   /**
    * Convenience method to get a list of component locators for a specified
    * type
    *
    * @param objectType The type of the returned component locators. It must be
    *    <code>TYPE_XXX</code>.
    *
    * @param locatorType The type of the locator requested. It must be one of the 
    * <code>PSComponentSummary.GET_XXX_LOCATOR</code> values.
    *
    * @return A list over <code>0</code> or more <code>PSLocator</code> 
    * objects.
    */
   public List getComponentLocators(int objectType, int locatorType)
   {
      return getComponents(objectType, locatorType);
   }

   /**
    * Just like the {@link #getComponentLocators(int, int)}, except it returns 
    * a list of names for the specified type.
    */
   public List getComponentNames(int type)
   {
      return getComponents(type, PSComponentSummary.GET_NAME);
   }

   /**
    * Convenience method to get a list of component summaries, locators, or
    * names for a specified type
    *
    * @param type The type of the returned component locators. It must be
    *    <code>TYPE_XXX</code>.
    *
    * @param whichInfo Specify with part of the summaries need to get. Assume
    *    it is of the <code>GET_XXX</code> values.
    *
    * @return A list over <code>0</code> or more
    *    <code>PSLocator</code> or <code>PSComponentSummary</code> objects.
    */
   private List getComponents(int type, int whichInfo)
   {
      PSComponentSummary.validateType(type);

      Iterator comps = super.iterator();

      List items = new ArrayList();
      while (comps.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) comps.next();
         if (summary.getType() == type)
         {
            switch (whichInfo)
            {
               case PSComponentSummary.GET_LOCATOR :
                  items.add(summary.getLocator());
                  break;
               case PSComponentSummary.GET_CURRENT_LOCATOR :
                  items.add(summary.getCurrentLocator());
                  break;
               case PSComponentSummary.GET_TIP_LOCATOR :
                  items.add(summary.getTipLocator());
                  break;
               case PSComponentSummary.GET_NAME :
                  items.add(summary.getName());
                  break;
               default :
                  items.add(summary);
                  break;
            }
         }
      }

      return items;
   }

   /**
    * Convenience method to get a list of locators of the component summaries
    * in this object.
    *
    * @return A list over <code>0</code> or more <code>PSLocator</code> objects.
    */
   public List getLocators()
   {
      Iterator comps = super.iterator();
      List locators = new ArrayList();

      while (comps.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) comps.next();
         locators.add(summary.getCurrentLocator());
      }

      return locators;
   }

   /**
    * Get a component summary from a given id.
    * 
    * @param id the retrieved component summary id.
    * 
    * @return the searched component summary object. It may be <code>null</code>
    *   if cannot find one.
    */
   public PSComponentSummary getComponentFromId(int id)
   {
      Iterator comps = super.iterator();

      PSComponentSummary summary;
      while (comps.hasNext())
      {
         summary = (PSComponentSummary) comps.next();
         if (summary.getContentId() == id)
            return summary;
      }
      return null;
   }
   /**
    * Get a list of component summary object.
    *
    * @return An iterator over zero or more <code>PSComponentSummary</code>
    *    objects. Never <code>null</code>, but may be empty.
    */
   public Iterator<PSComponentSummary> getSummaries()
   {
      return iterator();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.cms.objectstore.PSDbComponentSet#iterator()
    */
   @SuppressWarnings("unchecked")
   public Iterator<PSComponentSummary> iterator()
   {
      return super.iterator();
   }
   
   /**
    * Just like {@link #getSummaries()}, except this returns array of
    * zero or more <code>PSComponentSummary</code> objects.
    */
   public PSComponentSummary[] toArray()
   {
      PSComponentSummary[] sArray = new PSComponentSummary[super.size()];

      int i = 0;
      Iterator summaries = super.iterator();
      while (summaries.hasNext())
      {
         PSComponentSummary summary = (PSComponentSummary) summaries.next();
         sArray[i++] = summary;
      }

      return sArray;
   }

   /**
    * See {@link PSDbComponentList#toDbXml(Document, Element, IPSKeyGenerator, 
    * PSKey)}.
    * note: this operation is not supported for the read-only components.
    */
   @Override
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
   {
      throw new UnsupportedOperationException("toDbXml is not supported");
   }
}
