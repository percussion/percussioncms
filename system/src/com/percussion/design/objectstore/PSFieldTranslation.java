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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation for the PSXFieldTranslation DTD in BasicObjects.dtd.
 */
public class PSFieldTranslation extends PSComponent
{
   /**
    * Constructs a new field translation object.
    *
    * @param translations set of UDF extensions, not <code>null</code>.
    */
   public PSFieldTranslation(PSExtensionCallSet translations)
   {
      setTranslations(translations);
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
   public PSFieldTranslation(Element sourceNode, IPSDocument parentDoc,
                             ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSFieldTranslation()
   {
   }

   /**
    * Get the field translations.
    *
    * @return the field translations, never
    *    <code>null</code>, migt be empty. Garanteed only UDF's.
    */
   public PSExtensionCallSet getTranslations()
   {
      return m_translations;
   }

   /**
    * Set new translations.
    *
    * @param translations the new translations, never <code>null</code>,
    *    might be empty, only UDF's allowed.
    */
   public void setTranslations(PSExtensionCallSet translations)
   {
      if (translations == null)
         throw new IllegalArgumentException("translations can't be null");

      m_translations = translations;
   }

   /**
    * Get the error message.
    *
    * @return the error message, might be <code>null</code>.
    */
   public PSDisplayText getErrorMessage()
   {
      return m_errorMessage;
   }

   /**
    * Set a new error message.
    *
    * @param errorMessage the new error message, might be <code>null</code>.
    */
   public void setErrorMessage(PSDisplayText errorMessage)
   {
      m_errorMessage = errorMessage;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSFieldTranslation, not <code>null</code>.
    */
   public void copyFrom(PSFieldTranslation c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setErrorMessage(c.getErrorMessage());
      setTranslations(c.getTranslations());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSFieldTranslation)) return false;
      if (!super.equals(o)) return false;
      PSFieldTranslation that = (PSFieldTranslation) o;
      return Objects.equals(m_translations, that.m_translations) &&
              Objects.equals(m_errorMessage, that.m_errorMessage);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_translations, m_errorMessage);
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

         // REQUIRED: the translations
         node = tree.getNextElement(
            PSExtensionCallSet.ms_NodeType, firstFlags);
         if (node != null)
         {
            m_translations = new PSExtensionCallSet(
               node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSExtensionCallSet.ms_NodeType,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // OPTIONAL: the error message (ErrorLabel/PSXDisplayText)
         node = tree.getNextElement(ERROR_LABEL_ELEM, nextFlags);
         if (node != null)
         {
            node = tree.getNextElement(PSDisplayText.XML_NODE_NAME, firstFlags);
            if (node != null)
            {
               m_errorMessage = new PSDisplayText(node, parentDoc, 
                     parentComponents);               
            }
            else
            {
               Object[] args =
               {
                  PSExtensionCallSet.ms_NodeType,
                  ERROR_LABEL_ELEM,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
           }
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

      // REQUIRED: create translations
      root.appendChild(m_translations.toXml(doc));

      // OPTIONAL: create the error message
      if (m_errorMessage != null)
      {
         Element errorLabel = doc.createElement(ERROR_LABEL_ELEM);
         root.appendChild(errorLabel);
         errorLabel.appendChild(m_errorMessage.toXml(doc));
      }

      return root;
   }

   // see IPSComponent
   @Override
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_translations != null)
            m_translations.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_FIELD_TRANSLATION, null);

         if (m_errorMessage != null)
            m_errorMessage.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXFieldTranslation";

   /**
    * Name of the otional child element that will contain the error label.
    * (package access for unit test)
    */
   static final String ERROR_LABEL_ELEM = "ErrorLabel";

   /**
    * The field translations, never <code>null</code> after construction. Only
    * UDF extensions are allowed.
    */
   private PSExtensionCallSet m_translations = null;

   /** The error display text, might be <code>null</code>. */
   private PSDisplayText m_errorMessage = null;
}

