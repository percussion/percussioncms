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
package com.percussion.design.objectstore;

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation for the PSXApplicationFlow DTD in BasicObjects.dtd.
 *
 * A map of collections of PSConditionalRequest objects. The last entry
 * in the PSConditionalRequest collections is the default request
 * which has a condition always evaluating to <code>true</code>.
 */
@SuppressWarnings("serial")
public class PSApplicationFlow extends PSComponent
{
   /**
    * Create a new map of command handler redirects for the provided name
    * and default redirect.
    *
    * @param name the name of the command handler to create this for, never
    *    <code>null</code> or empty.
    * @param redirect the default redirect request for this handler,
    *    not <code>null</code>.
    */
   public PSApplicationFlow(String name, PSUrlRequest redirect)
   {
      setDefaultRedirect(name, redirect);
   }

   /**
    * Create a new map of command handler redirects for the provided name
    * and redirect request collection (a collection of PSConditionalRequest
    * objects).
    *
    * @param name the name of the command handler to create this for, never
    *    <code>null</code> or empty.
    * @param redirects a collection of PSConditionalRequest objects for
    *    the provided command handler name. Not <code>null</code> or empty.
    *    The last entry must have a condition evaluating to <code>true</code>.
    */
   public PSApplicationFlow(String name, PSCollection redirects)
   {
      addRedirects(name, redirects);
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
   public PSApplicationFlow(Element sourceNode, IPSDocument parentDoc,
                            List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Copy constructor, creates a shallow copy.
    *
    * @param source the source to create a copy from, not <code>null</code>.
    */
   public PSApplicationFlow(PSApplicationFlow source)
   {
      copyFrom(source);
   }

   /**
    * Needed for serialization.
    */
   protected PSApplicationFlow()
   {
   }


   // see interface for description
   @SuppressWarnings("unchecked")
   public Object clone()
   {
      PSApplicationFlow copy = (PSApplicationFlow) super.clone();
      copy.m_redirects = (HashMap) m_redirects.clone();
      // because the HashMap clone is shallow, must clone each value in the map
      for (Iterator iter = copy.m_redirects.entrySet().iterator();
           iter.hasNext();)
      {
         Map.Entry entry = (Map.Entry) iter.next();
         PSCollection value = (PSCollection) entry.getValue();
         // clone the PSCollection
         PSCollection valueCopy = new PSCollection( PSConditionalRequest.class );
         for (int i = 0; i < value.size(); i++)
         {
            PSConditionalRequest request  =
               (PSConditionalRequest) value.elementAt( i );
            valueCopy.add( i, request.clone() );
         }
         entry.setValue( valueCopy );
      }
      return copy;
   }


   /**
    * Returns an iterator (a collection of
    * PSConditionalRequest objects) for the provided command handler name.
    * The last entry is the default redirect with a condition evaluating
    * always to <code>true</code>.
    *
    * @param name the name of the command handler we want the
    *    redirect for. Never code>null</code> or empty.
    * @return a collection of PSConditionalRequest objects,
    *    <code>null</code> if not found, but never empty.
    */
   public Iterator getRedirects(String name)
   {
      validate(name);
      PSCollection redirects = (PSCollection) m_redirects.get(name);
      if (redirects != null)
         return redirects.iterator();

      return null;
   }

   /**
    * Returns the redirect collection (a collection of
    * PSConditionalRequest objects) for the provided command handler name.
    * The last entry is the default redirect with a condition evaluating
    * always to <code>true</code>.
    *
    * @param name the name of the command handler we want the
    *    redirect for. Never <code>null</code> or empty.
    * @return a collection of PSConditionalRequest objects,
    *    <code>null</code> if not found, but never empty.
    */
   public PSCollection getRedirectCollection(String name)
   {
      validate(name);

      return (PSCollection) m_redirects.get(name);
   }

   /**
    * Get the default redirect for the provided command handler name.
    *
    * @param name the command handler name, not <code>null</code> or empty.
    * @return the default redirect, <code>null</code> if not found.
    */
   public PSUrlRequest getDefaultRedirect(String name)
   {
      validate(name);
      PSCollection c = (PSCollection) m_redirects.get(name);
      if (c == null)
         return null;

      return (PSUrlRequest) c.get(c.size()-1);
   }

   /**
    * Set a new default redirect for the provided command handler name.
    *
    * @param name a valid command handler name, never
    *    <code>null</code> or empty.
    * @param redirect the default redirect, never <code>null</code>.
    * @throws IllegalArgumentException if the provided command handler name is
    *    <code>null</code> or empty or if the redirect is <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void setDefaultRedirect(String name, PSUrlRequest redirect)
   {
      validate(name);
      if (redirect == null)
         throw new IllegalArgumentException("the redirect cannot be null");

      // create a conditional request evaluating always <code>true</code>
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
         PSConditionalRequest condRequest =
            new PSConditionalRequest(redirect, rules);

         PSCollection c = (PSCollection) m_redirects.get(name);
         if (c == null)
         {
            // must be a new handler, create a collection
            c = new PSCollection(condRequest.getClass());
         }

         // add/replace the default stylesheet
         if (c.isEmpty())
            c.add(condRequest);
         else
            c.set(c.size()-1, condRequest);

         m_redirects.put(name, c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Adds/replaces the collection of PSConditionalRequest objects for
    * the provided command handler name. The entire collection is expected
    * with the last entry beeing the default redirect, a conditional
    * request with a condition always <code>true</code>.
    *
    * @param name a valid command handler name, never
    *    <code>null</code> or empty.
    * @param redirects a collection of PSConditionalRequest objects,
    *    never <code>null</code>, must have at least one entry, the last
    *    entry is the default redirect with a condition always
    *    <code>true</code>.
    * @throws IllegalArgumentException if the provided command handler name is
    *    <code>null</code> or empty or if the redirect collection is
    *    <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void addRedirects(String name, PSCollection redirects)
   {
      validate(name);
      validate(redirects);

      m_redirects.put(name, redirects);
   }

   /**
    * Adds/replaces all conditional redirects. This will add/replace all
    * but the last entry in the current collection.
    *
    * @param name a valid command handler name, never
    *    <code>null</code> or empty.
    * @param redirects a collection of PSConditionalRequest objects,
    *    not <code>null</code>, might be empty.
    * @throws IllegalArgumentException if the provided command handler name is
    *    <code>null</code> or empty or if the redirect collection is
    *    <code>null</code> or if no command handler was found for the provided
    *    name.
    */
   @SuppressWarnings("unchecked")
   public void addConditionalRedirects(String name, PSCollection redirects)
   {
      validate(name);
      if (redirects == null)
         throw new IllegalArgumentException("the redirects cannot be null");

      if (m_redirects.get(name) == null)
         throw new IllegalArgumentException("command handler not found");

      PSCollection c = new PSCollection( PSConditionalRequest.class );
      c.addAll(redirects);
      c.add(getDefaultRedirect(name));

      m_redirects.put(name, c);
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
    * Removes the command handler redirects from this map and returns the
    * removed collection.
    *
    * @param name the command handler name to remove the redirect collection
    *    for, never <code>null</code> or empty.
    * @return the redirect collection removed or
    *    <code>null</code> if not found.
    */
   public PSCollection removeStylesheets(String name)
   {
      validate(name);
      return (PSCollection) m_redirects.remove(name);
   }

   /**
    * Get a list of known command handler names.
    *
    * @return a list of command handler names, never
    *    <code>null</code> or empty.
    */
   public Iterator getCommandHandlerNames()
   {
      return m_redirects.keySet().iterator();
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSApplicationFlow, not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public void copyFrom(PSApplicationFlow c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_redirects = new HashMap(c.m_redirects);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSApplicationFlow)) return false;
      if (!super.equals(o)) return false;
      PSApplicationFlow that = (PSApplicationFlow) o;
      return Objects.equals(m_redirects, that.m_redirects) &&
              Objects.equals(m_filename, that.m_filename);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_redirects, m_filename);
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
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

         // REQUIRED: get the command handler (at least one)
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
         while (node != null)
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

            // OPTIONAL: get all conditional redirects
            PSCollection redirects =
               new PSCollection( PSConditionalRequest.class );
            node = tree.getNextElement(
               PSConditionalRequest.XML_NODE_NAME, firstFlags);
            while (node != null)
            {
               redirects.add(new PSConditionalRequest(
                  node, parentDoc, parentComponents));
               node = tree.getNextElement(
                  PSConditionalRequest.XML_NODE_NAME, nextFlags);
            }

            // REQUIRED: the default redirect
            if (!redirects.isEmpty())
               node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME, nextFlags);
            else
               node = tree.getNextElement(PSUrlRequest.XML_NODE_NAME, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSUrlRequest.XML_NODE_NAME,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
            PSUrlRequest redirect = new PSUrlRequest(
               node, parentDoc, parentComponents);

            setDefaultRedirect(name, redirect);
            addConditionalRedirects(name, redirects);

            tree.setCurrent(current);

            node = tree.getNextElement(COMMAND_HANDLER_ELEM, nextFlags);
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

            PSCollection redirects = (PSCollection) m_redirects.get(name);
            if (redirects != null && !redirects.isEmpty())
            {
               // create all conditional stylesheets
               for (int i=0; i<redirects.size()-1; i++)
                  elem.appendChild(
                     ((IPSComponent) redirects.get(i)).toXml(doc));

               // create the default stylesheet
               PSConditionalRequest defaultRedirect =
                  (PSConditionalRequest) redirects.get(redirects.size()-1);
               elem.appendChild(
                  (new PSUrlRequest(defaultRedirect)).toXml(doc));
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
            Iterator it = getRedirects((String) names.next());
            if (!it.hasNext())
            {
               context.validationError(this,
                  IPSObjectStoreErrors.INVALID_COMMAND_HANDLER_REDIRECTS, null);
            }
            while (it.hasNext())
               ((PSConditionalRequest) it.next()).validate(context);
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
    * Validates the provided redirect collection.
    *
    * @param stylesheets the stylesheet collection to validate.
    */
   public void validate(PSCollection redirects)
   {
      if (redirects == null || redirects.isEmpty())
         throw new IllegalArgumentException(
            "the redirect collection cannot be null or empty");

      if (!redirects.getMemberClassName().equals(
         "com.percussion.design.objectstore.PSConditionalRequest"))
         throw new IllegalArgumentException(
            "PSConditionalRequest collection expected");
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXApplicationFlow";

   /**
    * Maps a command handler name (<code>String</code>) to a collection of
    * redirects (<code>PSCollection</code> of <code>PSConditionalRequest
    * </code>).  Never <code>null</code>.
    */
   private HashMap m_redirects = new HashMap();
   
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

