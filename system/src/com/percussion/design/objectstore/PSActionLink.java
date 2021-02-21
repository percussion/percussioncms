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
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implementation for the PSXActionLink DTD in BasicObjects.dtd.
 */
public class PSActionLink extends PSComponent
{
   /**
    * Creates a new action link.
    *
    * @param displayText the display text, not <code>null</code>.
    */
   public PSActionLink(PSDisplayText displayText)
   {
      setDisplayText(displayText);
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
   public PSActionLink(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }


   // see interface for description
   public Object clone()
   {
      PSActionLink copy = (PSActionLink) super.clone();
      copy.m_displayText = (PSDisplayText) m_displayText.clone();
      copy.m_parameters = new PSCollection( PSParam.class );
      for (int i = 0; i < m_parameters.size(); i++)
      {
         PSParam param = (PSParam) m_parameters.elementAt( i );
         copy.m_parameters.add( i, param.clone() );
      }
      return copy;
   }


   /**
    * Get the display text.
    *
    * @return the display text, never <code>null</code>.
    */
   public PSDisplayText getDisplayText()
   {
      return m_displayText;
   }

   /**
    * Set a new display text.
    *
    * @param displayText the new display text, not <code>null</code>.
    */
   public void setDisplayText(PSDisplayText displayText)
   {
      if (displayText == null)
         throw new IllegalArgumentException("displayText cannot be null");

      m_displayText = displayText;
   }

   /**
    * Get link parameters.
    *
    * @return a collection of PSParam objects, never
    *    <code>null</code>, might be empty.
    */
   public Iterator getParameters()
   {
      return m_parameters.iterator();
   }

   /**
    * Set new link parameters.
    *
    * @param parameters a collection of PSParam objects, not
    *    <code>null</code>, might be empty.
    */
   public void setParameters(PSCollection parameters)
   {
      if (parameters == null)
         throw new IllegalArgumentException("paramters cannot be null");

      if (!parameters.getMemberClassName().equals(
          m_parameters.getMemberClassName()))
         throw new IllegalArgumentException(
            "PSParam collection expected");

      m_parameters.clear();
      m_parameters.addAll(parameters);
   }

   /**
    * Is this action link disabled?
    *
    * @return <code>true</code> if disabled, <code>false</code> otherwise.
    */
   public boolean isDisabled()
   {
      return m_isDisabled;
   }

   /**
    * Set a new disabled status.
    *
    * @param isDisabled <code>true</code> to disable this action link,
    *    <code>false</code> otherwise.
    */
   public void setDisabled(boolean isDisabled)
   {
      m_isDisabled = isDisabled;
   }

   /**
    * Is this action a workflow transition?
    *
    * @return <code>true</code> if this is a workflow transition,
    *    <code>false</code> otherwise.
    */
   public boolean isTransition()
   {
      return m_isTransition;
   }

   /**
    * Set a new transition status.
    *
    * @param isTransition <code>true</code> to make this a workflow
    *    transition action link, <code>false</code> otherwise.
    */
   public void setTransition(boolean isTransition)
   {
      m_isTransition = isTransition;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSField, not <code>null</code>.
    */
   public void copyFrom(PSActionLink c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      m_displayText = c.m_displayText;
      m_parameters = c.m_parameters;
      m_isDisabled = c.m_isDisabled;
      m_isTransition = c.m_isTransition;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSActionLink)) return false;
      if (!super.equals(o)) return false;
      PSActionLink that = (PSActionLink) o;
      return m_isDisabled == that.m_isDisabled &&
              m_isTransition == that.m_isTransition &&
              Objects.equals(m_displayText, that.m_displayText) &&
              Objects.equals(m_parameters, that.m_parameters);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_displayText, m_parameters, m_isDisabled, m_isTransition);
   }

   // see IPSComponent
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

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         // OPTIONAL: get the isDisabled attribute
         data = tree.getElementData(IS_DISABLED_ATTR);
         if (data != null)
            m_isDisabled =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;

         // OPTIONAL: get the isTransition attribute
         data = tree.getElementData(IS_TRANSITION_ATTR);
         if (data != null)
            m_isTransition =
               data.equalsIgnoreCase(BOOLEAN_ENUM[0]) ? true : false;

         // REQUIRED: get the display text
         node = tree.getNextElement(PSDisplayText.XML_NODE_NAME, firstFlags);
         if (node == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSDisplayText.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else
            m_displayText = new PSDisplayText(node, parentDoc, parentComponents);

         // OPTIONAL: get the parameters
         node = tree.getNextElement(PSParam.XML_NODE_NAME, nextFlags);
         while (node != null)
         {
            m_parameters.add(new PSParam(node, parentDoc, parentComponents));
            node = tree.getNextElement(PSParam.XML_NODE_NAME, nextFlags);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   // see IPSComponent
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(IS_DISABLED_ATTR,
         m_isDisabled ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);
      root.setAttribute(IS_TRANSITION_ATTR,
         m_isTransition ? BOOLEAN_ENUM[0] : BOOLEAN_ENUM[1]);

      // REQUIRED: create the display text
      root.appendChild(m_displayText.toXml(doc));

      // OPTIONAL: create the parameters
      Iterator it = getParameters();
      while (it.hasNext())
         root.appendChild(((IPSComponent) it.next()).toXml(doc));

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
         if (m_displayText != null)
            ((IPSComponent) m_displayText).validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_ACTION_LINK, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXActionLink";

   /**
    * The action link display text, never <code>null</code> after
    * construction.
    */
   private PSDisplayText m_displayText;

   /**
    * The link parameters, a collection of PSParam objects. Never
    * <code>null</code> after construction, might be empty.
    */
   private PSCollection m_parameters = new PSCollection( PSParam.class );

   /**
    * Status whether or not this action link is enabled or disabled.
    */
   private boolean m_isDisabled = false;

   /**
    * Status whether or not this action link is a workflow taransition.
    */
   private boolean m_isTransition = false;

   /**
    * An array of XML attribute values for all boolean attributes. They are
    * ordered as <code>true</code>, <code>false</code>.
    */
   private static final String[] BOOLEAN_ENUM =
   {
      "yes", "no"
   };

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String IS_DISABLED_ATTR = "isDisabled";
   private static final String IS_TRANSITION_ATTR = "isTransition";
}

