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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * The PSLiteralSet class is used to maintain a grouping of literals. The
 * number of objects permitted can be limited, such as when using a
 * BETWEEN clause which requires two values.
 *
 * @author       Tas Giakouminakis
 * @version     1.0
 * @since       1.0
 */
public class PSLiteralSet extends PSCollectionComponent implements IPSReplacementValue
{
   /**
    * The value type associated with this instances of this class.
    */
   public static final String      VALUE_TYPE      = "LiteralSet";

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSLiteralSet(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this(PSLiteral.class);
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   public PSLiteralSet(String className) throws ClassNotFoundException
   {
      super(className);
   }

   public PSLiteralSet(Class type)
   {
      super(type);
   }

   /**
    * Convert the PSLiteralSet object into a string, which is a comma separated list
    * of all the elements. For example, suppose there is a PSLiteralSet object
    * mySet which has three elements, firstName, midName, and lastName. Calling
    * mySet.toString() yields a string "firstName, midName, lastName".
    */
   public String toString()
   {
      String oneString = "";

      if (isEmpty())
         return oneString;

      oneString = getValueText();
      return oneString;
   }

   // *********** IPSReplacementValue Interface Implementation ***********

   /**
    * Get the type of replacement value this object represents.
    */
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   /**
    * Get the text which can be displayed to represent this value.
    */
   public String getValueDisplayText()
   {
      return getValueText();
   }

   /**
    * Get the implementation specific text which for this value.
    */
   public String getValueText()
   {
      // Return a comma separated list of the value text from each element.
      StringBuilder buf = new StringBuilder(4*size());

      if (size() > 0)
         buf.append(((PSLiteral)get(0)).getValueText());
      for (int i = 1; i < size(); i++)
      {
         buf.append(", ");
         buf.append(((PSLiteral)get(i)).getValueText());
      }
      return buf.toString();
   }

   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException

   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      int type = 0; // 1 = date literal, 2 = text, 3 = numeric

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      org.w3c.dom.Node cur = tree.getCurrent();   // cur = <PSXLiteralSet>

      Element el = tree.getNextElement(PSDateLiteral.ms_NodeType, firstFlags);

      if (el == null){
         tree.setCurrent(cur);
         el = tree.getNextElement(PSTextLiteral.ms_NodeType, firstFlags);
         if (el == null){
            tree.setCurrent(cur);
            el = tree.getNextElement(PSNumericLiteral.ms_NodeType, firstFlags);
            if (el != null)
               type = 3;
         }
         else
            type = 2;
      }
      else
         type = 1;

      switch (type)
      {
      case 0:
         return; // empty set
      case 1: // date literal
         do
         {
            add(new PSDateLiteral((Element)tree.getCurrent(), parentDoc, parentComponents));
         } while (null != (el = tree.getNextElement(PSDateLiteral.ms_NodeType, nextFlags)));
         break;
      case 2: // text literal
         do
         {
            add(new PSTextLiteral((Element)tree.getCurrent(), parentDoc, parentComponents));
         } while (null != (el = tree.getNextElement(PSTextLiteral.ms_NodeType, nextFlags)));
         break;
      case 3: // numeric literal
         do
         {
            add(new PSNumericLiteral((Element)tree.getCurrent(), parentDoc, parentComponents));
         } while (null != (el = tree.getNextElement(PSNumericLiteral.ms_NodeType,nextFlags)));
         break;
      }
   }

   public Element toXml(Document doc)
   {
      // create a PSXLiteralSet element with many literals under it
      Element   root = doc.createElement(ms_NodeType);
      for (int i = 0; i < size(); i++)
      {
         PSLiteral lit = (PSLiteral)get(i);
         root.appendChild(lit.toXml(doc));
      }
      return root;
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param   cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      cxt.pushParent(this);
      try
      {
         for (int i = 0; i < size(); i++)
         {
            PSLiteral lit = (PSLiteral)get(i);
            lit.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   /**
    * The maximum number of entries that this set can hold. Addition
    * of an element will fail (returning false) when the added
    * object would cause the number of entries to exceed this value.
    */
   private int m_maxEntries;

   /**
    * package access on this so they may reference each other in fromXml
    */
   static final String      ms_NodeType = "PSXLiteralSet";
}
