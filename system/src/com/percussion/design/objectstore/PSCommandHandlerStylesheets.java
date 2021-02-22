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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implements the PSXCommandHandlerStylesheet DTD defined in BasicObjects.dtd.
 *
 * A map of collections of PSConditionalStylesheet objects. The last entry
 * in the PSConditionalStylesheet collections is the default stylesheet
 * which has a condition always evaluating to <code>true</code>.
 */
@SuppressWarnings("serial")
public class PSCommandHandlerStylesheets extends PSComponent
{
   /**
    * Create a new map of command handler stylesheets for the provided name
    * and default stylesheet.
    *
    * @param name the name of the command handler to create this for, never
    *    <code>null</code> or empty.
    * @param stylesheet the default stylesheet for this handler,
    *    not <code>null</code>.
    */
   public PSCommandHandlerStylesheets(String name, PSStylesheet stylesheet)
   {
      setDefaultStylesheet(name, stylesheet);
   }

   /**
    * Create a new map of command handler stylesheets for the provided name
    * and stylesheet collection.
    *
    * @param name the name of the command handler to create this for, never
    *    <code>null</code> or empty.
    * @param stylesheets a collection of PSConditionalStylesheet objects for
    *    the provided command handler name. Not <code>null</code> or empty.
    *    The last entry must have a condition evaluating to <code>true</code>.
    */
   public PSCommandHandlerStylesheets(String name, PSCollection stylesheets)
   {
      addStylesheets(name, stylesheets);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSCommandHandlerStylesheets(Element sourceNode, IPSDocument parentDoc,
                                      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Copy constructor, creates a shallow copy.
    *
    * @param source the source to create a copy from, not <code>null</code>.
    */
   public PSCommandHandlerStylesheets(PSCommandHandlerStylesheets source)
   {
      copyFrom(source);
   }

   /**
    * Needed for serialization.
    */
   protected PSCommandHandlerStylesheets()
   {
   }

   /**
    * Returns the stylesheet collection (a collection of
    * PSConditionalStylesheet objects) for the provided command handler name.
    * The last entry is the default stylesheet with a condition evaluating
    * always to <code>true</code>.
    *
    * @param name the name of the command handler we want the
    *    stylesheets for. Never <code>null</code> or empty.
    * @return a collection of PSConditionalStylesheet objects,
    *    never <code>null</code>, might be empty.
    */
   public Iterator getStylesheets(String name)
   {
      validate(name);

      PSCollection c = (PSCollection) m_stylesheets.get(name);
      if (c != null)
         return c.iterator();

      return PSIteratorUtils.emptyIterator();
   }

   /**
    * Get the default stylesheet for the provided command handler name.
    *
    * @param name the command handler name, not <code>null</code> or empty.
    * @return the default stylesheet, <code>null</code> if not
    *    found.
    */
   public PSStylesheet getDefaultStylesheet(String name)
   {
      validate(name);
      PSCollection c = (PSCollection) m_stylesheets.get(name);
      if (c == null)
         return null;

      return (PSStylesheet) c.get(c.size()-1);
   }

   /**
    * Set a new default stylesheet for the provided command handler name.
    *
    * @param name a valid command handler name, never
    *    <code>null</code> or empty.
    * @param stylesheet the default stylesheet, never <code>null</code>.
    * @throws IllegalArgumentExcption if the provided command handler name is
    *    <code>null</code> or empty or if the stylesheet is <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setDefaultStylesheet(String name, PSStylesheet stylesheet)
   {
      validate(name);
      if (stylesheet == null)
         throw new IllegalArgumentException("the stylesheet cannot be null");

      // create a conditional stylesheet evaluating always <code>true</code>
      try
      {
         PSConditional condition = new PSConditional(
            new PSTextLiteral("1"), PSConditional.OPTYPE_EQUALS,
            new PSTextLiteral("1"));
         PSCollection conditions = new PSCollection(
            (new PSConditional()).getClass());
         conditions.add(condition);
         PSRule rule = new PSRule(conditions);
         PSCollection rules = new PSCollection(rule.getClass());
         rules.add(rule);
         PSConditionalStylesheet condStylesheet =
            new PSConditionalStylesheet(stylesheet, rules);

         PSCollection c = (PSCollection) m_stylesheets.get(name);
         if (c == null)
         {
            // must be a new handler, create a collection
            c = new PSCollection(condStylesheet.getClass());
         }

         // add/replace the default stylesheet
         if (c.isEmpty())
            c.add(condStylesheet);
         else
            c.set(c.size()-1, condStylesheet);

         m_stylesheets.put(name, c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Adds/replaces the collection of PSConditionalStylesheet objects for
    * the provided command handler name. The entire collection is expected
    * with the last entry beeing the default stylesheet, a conditional
    * stylesheet with a conditiona always <code>true</code>.
    *
    * @param name a valid command handler name, never
    *    <code>null</code> or empty.
    * @param stylesheets a collection of PSConditionalStylesheet objects,
    *    never <code>null</code>, must have at least one entry, the last
    *    entry is the default stylesheet with a condition always
    *    <code>true</code>.
    * @throws IllegalArgumentExcption if the provided command handler name is
    *    <code>null</code> or empty or if the stylesheet collection is
    *    <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public void addStylesheets(String name, PSCollection stylesheets)
   {
      validate(name);
      validate(stylesheets);

      m_stylesheets.put(name, stylesheets);
   }

   /**
    * Adds/replaces all conditional stylesheets. This will add/replace all
    * but the last entry in the current collection.
    *
    * @param name a valid command handler name, never
    *    <code>null</code> or empty.
    * @param stylesheets a collection of PSConditionalStylesheet objects,
    *    not <code>null</code>, might be empty.
    * @throws IllegalArgumentExcption if the provided command handler name is
    *    <code>null</code> or empty or if the stylesheet collection is
    *    <code>null</code> or if no command handler was found for the provided
    *    name.
    */
   @SuppressWarnings("unchecked")
   public void addConditionalStylesheets(String name, PSCollection stylesheets)
   {
      validate(name);
      if (stylesheets == null)
         throw new IllegalArgumentException("the stylesheets cannot be null");

      if (m_stylesheets.get(name) == null)
         throw new IllegalArgumentException("command handler not found");

      PSCollection c = new PSCollection(
         (new PSConditionalStylesheet()).getClass());
      c.addAll(stylesheets);
      c.add(getDefaultStylesheet(name));

      m_stylesheets.put(name, c);
   }

   /**
    * Removes the command handler stylesheets from this map and returns the
    * removed collection.
    *
    * @param name the command handler name to remove the stylesheet collection
    *    for, never <code>null</code> or empty.
    * @return the stylesheet collection removed or
    *    <code>null</code> if not found.
    */
   public PSCollection removeStylesheets(String name)
   {
      validate(name);
      return (PSCollection) m_stylesheets.remove(name);
   }

   /**
    * Get a list of known command handler names.
    *
    * @return a list of command handler names, never
    *    <code>null</code> or empty.
    */
   public Iterator getCommandHandlerNames()
   {
      return m_stylesheets.keySet().iterator();
   }
   
   /**
    * Get the name of the file that stores this shared field group.
    * 
    * @return the filename, may be <code>null</code>, never empty.
    */
   public String getFilename()
   {
      return m_filename;
   }
   
   /**
    * Set the name of the file that stores this shared field group.
    * 
    * @param filename the filename, may be <code>null</code>, not empty.
    */
   public void setFilename(String filename)
   {
      if (filename != null && filename.trim().length() == 0)
         throw new IllegalArgumentException("filename cannot be empty");
      
      m_filename = filename;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSCommandHandlerStylesheets, not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void copyFrom(PSCommandHandlerStylesheets c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_stylesheets = new HashMap(c.m_stylesheets);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSCommandHandlerStylesheets)) return false;
      if (!super.equals(o)) return false;
      PSCommandHandlerStylesheets that = (PSCommandHandlerStylesheets) o;
      return Objects.equals(m_stylesheets, that.m_stylesheets) &&
              Objects.equals(m_filename, that.m_filename);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_stylesheets, m_filename);
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the filename attribute
         String test = tree.getElementData(FILENAME_ATTR);
         if (test != null && test.trim().length() > 0)
            setFilename(test.trim());

         // REQUIRED: get the command handler stylesheets (at least one)
         node = tree.getNextElement(COMMAND_HANDLER_ELEM, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               COMMAND_HANDLER_ELEM,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         while(node != null)
         {
            Node current = tree.getCurrent();

            String name = node.getAttribute(COMMAND_HANDLER_NAME_ATTR);
            if (name == null || name.trim().length() == 0)
            {
               Object[] args =
               {
                  COMMAND_HANDLER_ELEM,
                  COMMAND_HANDLER_NAME_ATTR,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            // OPTIONAL: get all stylesheets
            PSCollection stylesheets = new PSCollection(
               (new PSConditionalStylesheet()).getClass());
            node = tree.getNextElement(
               PSConditionalStylesheet.XML_NODE_NAME, firstFlags);
            while(node != null)
            {
               stylesheets.add(new PSConditionalStylesheet(
                  node, parentDoc, parentComponents));
               node = tree.getNextElement(
                  PSConditionalStylesheet.XML_NODE_NAME, nextFlags);
            }

            // REQUIRED: the default stylesheet
            if (stylesheets.size() > 0)
               node = tree.getNextElement(
                  PSStylesheet.XML_NODE_NAME, nextFlags);
            else
               node = tree.getNextElement(
                  PSStylesheet.XML_NODE_NAME, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSStylesheet.XML_NODE_NAME,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            PSStylesheet stylesheet = new PSStylesheet(
               node, parentDoc, parentComponents);

            setDefaultStylesheet(name, stylesheet);
            addConditionalStylesheets(name, stylesheets);

            tree.setCurrent(current);

            node = tree.getNextElement(COMMAND_HANDLER_ELEM,
               nextFlags | PSXmlTreeWalker.GET_NEXT_ALLOW_PARENTS);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      if (getFilename() != null)
         root.setAttribute(FILENAME_ATTR, m_filename);

      // REQUIRED: create the command handler stylesheets (at least one)
      Iterator names = getCommandHandlerNames();
      if (names.hasNext())
      {
         while (names.hasNext())
         {
            Element elem = doc.createElement(COMMAND_HANDLER_ELEM);
            String name = (String) names.next();
            elem.setAttribute(COMMAND_HANDLER_NAME_ATTR, name);

            PSCollection stylesheets = (PSCollection) m_stylesheets.get(name);
            if (stylesheets != null && !stylesheets.isEmpty())
            {
               // create all conditional stylesheets
               for (int i=0; i<stylesheets.size()-1; i++)
                  elem.appendChild(
                     ((IPSComponent) stylesheets.get(i)).toXml(doc));

               // create the default stylesheet
               PSConditionalStylesheet defaultStylesheet =
                  (PSConditionalStylesheet) stylesheets.get(stylesheets.size()-1);
               elem.appendChild(
                  (new PSStylesheet(defaultStylesheet.getRequest())).toXml(doc));
            }

            root.appendChild(elem);
         }
      }

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         Iterator names = getCommandHandlerNames();
         while (names.hasNext())
         {
            Iterator it = getStylesheets((String) names.next());
            if (!it.hasNext())
            {
               context.validationError(this,
                  IPSObjectStoreErrors.INVALID_COMMAND_HANDLER_STYLESHEETS, null);
            }
            while (it.hasNext())
               ((PSConditionalStylesheet) it.next()).validate(context);
         }
      }
      finally
      {
         context.popParent();
      }
   }

   /**
    * Validates the provided command handler name.
    *
    * @param name the command handler name to validate.
    */
   public void validate(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException(
            "the command handler name cannot be null or empty");
   }

   /**
    * Validates the provided stylesheet collection.
    *
    * @param stylesheets the stylesheet collection to validate.
    */
   public void validate(PSCollection stylesheets)
   {
      if (stylesheets == null || stylesheets.isEmpty())
         throw new IllegalArgumentException(
            "the stylesheet collection cannot be null or empty");

      if (!stylesheets.getMemberClassName().equals(
         "com.percussion.design.objectstore.PSConditionalStylesheet"))
         throw new IllegalArgumentException(
            "PSConditionalStylesheet collection expected");
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXCommandHandlerStylesheets";

   /**
    * A hash map of command handler stylesheet collections (collections of
    * PSConditionalStylesheet objects) , useing the command handler name
    * as key.
    */
   private HashMap m_stylesheets = new HashMap();
   
   /**
    * The name of the file storing this shared field group, may be 
    * <code>null</code>, never empty.
    */
   private String m_filename = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String COMMAND_HANDLER_ELEM = "CommandHandler";
   private static final String COMMAND_HANDLER_NAME_ATTR = "name";
   private static final String FILENAME_ATTR = "filename";
}

