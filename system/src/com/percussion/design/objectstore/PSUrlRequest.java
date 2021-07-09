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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implements the PSXUrlRequest DTD defined in BasicObjects.dtd.
 */
public class PSUrlRequest extends PSComponent implements IPSReplacementValue
{
   /**
    * Creates a new request object for the provided name, href and parameters.
    *
    * @param name an optional name, must be unique within the document in
    *    which it is used. May be <code>null</code>, not empty.
    * @param href the base URL part with the query string, might be
    *    <code>null</code> or empty.
    * @param parameters a collection of PSParam objects, never
    *    <code>null</code>, may be empty.
    */
   public PSUrlRequest(String name, String href, PSCollection parameters)
   {
      setName(name);
      setHref(href);
      setQueryParameters(parameters);
   }

   /**
    * Creates a new request object for the provided UDF. The UDF must return
    * a URL.
    *
    * @param name an optional name, must be unique within the document in
    *    which it is used. May be <code>null</code>, not empty.
    * @param converter a UDF which returns a URL. Never <coe>null</code>.
    */
   public PSUrlRequest(String name, PSExtensionCall converter)
   {
      setName(name);
      setConverter(converter);
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
   public PSUrlRequest(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      // allow subclasses to override (don't use "this")
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Copy constructor, creates a shallow copy.
    *
    * @param source the source to create a copy from, not <code>null</code>.
    */
   public PSUrlRequest(PSUrlRequest source)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      this.copyFrom(source); // make sure we don't get overridden
   }

   /**
    * Constructor for XML serialization by subclasses.  Needed because if
    * subclass calls {@link #PSUrlRequest(Element,IPSDocument,ArrayList) super}
    * in its constructor, then this class will call the subclass <code>
    * fromXml()</code> (because of it is overridden), but the subclass hasn't
    * had a chance to initialize its fields -- null pointer!
    */
   protected PSUrlRequest()
   {
   }

   // see interface for description
   public Object clone()
   {
      PSUrlRequest copy = (PSUrlRequest) super.clone();
      if (m_converter != null)
         copy.m_converter = (PSExtensionCall) m_converter.clone();
      // clone the PSCollection
      copy.m_queryParameters = new PSCollection(PSParam.class);
      for (int i = 0; i < m_queryParameters.size(); i++)
      {
         PSParam param = (PSParam) m_queryParameters.elementAt(i);
         copy.m_queryParameters.add(i, param.clone());
      }
      return copy;
   }

   /**
    * Get the request name.
    *
    * @return the name of this request, may be <code>null</code> but
    *    not empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Set the request name.
    *
    * @param name the name for this request. May be <code>null</code> but not
    *    empty.
    */
   public void setName(String name)
   {
      if (name != null && name.trim().length() == 0)
         throw new IllegalArgumentException("the name cannot be empty");

      m_name = name;
   }

   /**
    * Converts this <code>PSUrlRequest</code> object to a String by using
    * the either the base href or the string representation of the converter.
    * The parameter names and values are not currently included.
    *
    * @return the URL, never <code>null</code>, may be empty.
    * @see #getHref
    * @see PSExtensionCall#toString
    */
   public String toString()
   {
      String url;
      if (null == getConverter())
      {
         url = getHref();

         // at some point, we may want to show the parameter names and values
         // associated with this request. however, since the values are
         // IPSReplacementValues, it isn't obvious how they should be displayed
         /*
                  // convert query params to a hash
                  HashMap params = new HashMap();
                  for (Iterator i = getQueryParameters(); i.hasNext(); )
                  {
                     PSParam param = (PSParam) i.next();
                     params.put( param.getName(), param.getValue().toString() );
                  }
                  url = PSUrlUtils.createUrl(getHref(), params.entrySet().iterator(), null);
         */
      }
      else
      {
         url = getConverter().toString();
      }
      return url;
   }

   /**
    * Get the base URL.
    *
    * @return the base part of the URL. Never <code>null</code>, may
    *    be empty.
    */
   public String getHref()
   {
      return m_href;
   }

   /**
    * Set the new base URL.
    *
    * @param href the base part of the URL, might be <code>null</code> or
    *    empty.
    */
   public void setHref(String href)
   {
      if (href == null)
         m_href = "";
      else
         m_href = href;
   }

   /**
    * Gets the query parameters.
    *
    * @return an Iterator of PSParam objects, never <code>null</code>,
    * might be empty.
    */
   public Iterator getQueryParameters()
   {
      return m_queryParameters.iterator();
   }

   /**
    * Sets the query parameters.
    *
    * @param queryParameters a collection of PSParam objects. Not
    *    <code>null</code>, might be empty.
    */
   public void setQueryParameters(PSCollection queryParameters)
   {
      if (queryParameters == null)
         throw new IllegalArgumentException("queryPramaters cannot be null");

      if (!queryParameters
         .getMemberClassName()
         .equals(m_queryParameters.getMemberClassName()))
         throw new IllegalArgumentException("PSParam collection expected");

      m_queryParameters.clear();
      m_queryParameters.addAll(queryParameters);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int sum = 0;

      sum += m_anchor.hashCode();

      if (m_converter != null)
      {
         sum += m_converter.hashCode();
      }

      sum += m_href.hashCode();

      if (m_name != null)
      {
         sum += m_name.toLowerCase().hashCode();
      }

      for (Iterator iter = m_queryParameters.iterator(); iter.hasNext();)
      {
         Object element = (Object) iter.next();
         sum += element.hashCode();
      }

      return sum;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSUrlRequest))
         return false;

      PSUrlRequest t = (PSUrlRequest) o;

      boolean equal = true;
      if (!compare(m_converter, t.m_converter))
         equal = false;
      else if (!compare(getHref(), t.getHref()))
         equal = false;
      else if (!compare(getName(), t.getName()))
         equal = false;
      else if (!compareAsSet(m_queryParameters, t.m_queryParameters))
         equal = false;
      else if (!compare(getAnchor(), t.getAnchor()))
         equal = false;

      return equal;
   }

   /**
    * Get the anchor part of the URL.
    *
    * @return the anchor part of the URL, never <code>null</code>,
    *    might be empty.
    */
   public String getAnchor()
   {
      return m_anchor;
   }

   /**
    * Set a new anchor part for this URL.
    *
    * @param anchor the new anchor part, not <code>null</code>, might be empty.
    */
   public void setAnchor(String anchor)
   {
      if (anchor == null)
         throw new IllegalArgumentException("anchor cannot be null");

      m_anchor = anchor;
   }

   /**
    * Get the converter extension.
    *
    * @return a UDF which creates a URL, might be
    *    <code>null</code>.
    */
   public PSExtensionCall getConverter()
   {
      return m_converter;
   }

   /**
    * Set the new converter extension.
    *
    * @param converter the new converter extension, not <code>null</code>.
    */
   public void setConverter(PSExtensionCall converter)
   {
      if (converter == null)
         throw new IllegalArgumentException("converter cannot be null");

      m_converter = converter;

      // href and query parameters are not used when converter is set
      setHref("");
      m_queryParameters.clear();
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSUrlRequest, not <code>null</code>.
    */
   public void copyFrom(PSUrlRequest c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      };

      m_converter = c.getConverter();
      m_href = c.getHref();
      m_name = c.getName();
      m_queryParameters = c.m_queryParameters;
      m_anchor = c.m_anchor;
   }

   /**
    * @see IPSComponent
    */
   public void fromXml(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
            XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE,
            args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags =
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags =
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the name attribute
         data = tree.getElementData(NAME_ATTR);
         if (data != null && data.trim().length() > 0)
            m_name = data;

         // REQUIRED: get the converter extension or the URL parts
         node = tree.getNextElement(PSExtensionCall.ms_NodeType, firstFlags);
         if (node != null)
         {
            m_converter =
               new PSExtensionCall(node, parentDoc, parentComponents);
         }
         else
         {
            node = tree.getNextElement(HREF_ELEM, firstFlags);
            if (node == null)
            {
               Object[] args =
                  {
                     XML_NODE_NAME,
                     PSExtensionCall.ms_NodeType + " and " + HREF_ELEM,
                     "null" };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD,
                  args);
            }
            setHref(PSXmlTreeWalker.getElementData(node));

            node = tree.getNextElement(PSParam.XML_NODE_NAME, nextFlags);
            while (node != null)
            {
               m_queryParameters.add(
                  new PSParam(node, parentDoc, parentComponents));

               node = tree.getNextElement(PSParam.XML_NODE_NAME, nextFlags);
            }

            node = tree.getNextElement(ANCHOR_ELEM, nextFlags);
            if (node != null)
               m_anchor = PSXmlTreeWalker.getElementData(node);
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
      if (m_name != null && m_name.trim().length() > 0)
         root.setAttribute(NAME_ATTR, getName());

      if (m_converter != null)
      {
         // REQUIRED: create converter extension
         root.appendChild(m_converter.toXml(doc));
      }
      else
      {
         // REQUIRED: create URL parts
         PSXmlDocumentBuilder.addElement(doc, root, HREF_ELEM, m_href);
         Iterator it = getQueryParameters();
         while (it.hasNext())
            root.appendChild(((IPSComponent) it.next()).toXml(doc));
         PSXmlDocumentBuilder.addElement(doc, root, ANCHOR_ELEM, m_anchor);
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
         if (m_converter == null)
         {
            // we must have the parts
            if (m_href == null)
               context.validationError(
                  this,
                  IPSObjectStoreErrors.INVALID_URL_REQUEST,
                  null);
            Iterator it = getQueryParameters();
            while (it.hasNext())
                ((IPSComponent) it.next()).validate(context);
         }
         else
            m_converter.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   // see IPSReplacementValue
   public String getValueType()
   {
      return VALUE_TYPE;
   }

   // see IPSReplacementValue
   public String getValueDisplayText()
   {
      if (m_converter == null)
         return m_href + "?p1=1&p2=2...#anchor";
      else
         return m_converter.getValueDisplayText();
   }

   // see IPSReplacementValue
   public String getValueText()
   {
      if (m_converter == null)
      {
         String paramString = "";
         String delimiter = "?";
         Iterator it = getQueryParameters();
         while (it.hasNext())
         {
            paramString += delimiter;
            paramString += (String) it.next();
            delimiter = "&";
         }

         String anchorString = "";
         if (m_anchor.trim().length() > 0)
            anchorString += "#" + m_anchor;

         return m_href + paramString + anchorString;
      }
      else
         return m_converter.getValueText();
   }

   /**
    * Compare two collection elements as if they were sets. Two such elements
    * are equal if each element in one is present in the other, regardless of
    * ordering constraints.
    *
    * @param first
    * @param second
    * @return If the two collections are equal as sets
    */
   private boolean compareAsSet(PSCollection first, PSCollection second)
   {
      Set a = new HashSet(first);
      Set b = new HashSet(second);

      return a.equals(b);
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXUrlRequest";

   /**
    * The value type associated with this instances of this class, used as the
    * value type in IPSReplacementValue.
    */
   public static final String VALUE_TYPE = "UrlRequest";

   /**
    * An optional identifier for this request. Must be unique within the
    * document it is used. Might be <code>null</code> but not empty.
    */
   private String m_name = null;

   /**
    * The base part of the URL including the query string, not
    * <code>null</code> after construction, might be empty.
    */
   private String m_href = "";

   /**
    * A collection of PSParam objects, not <code>null</code>, might be empty
    * after construction.
    */
   protected PSCollection m_queryParameters = new PSCollection(PSParam.class);

   /** The anchor part of the URL, never <code>null</code>, might be empty. */
   private String m_anchor = "";

   /** A UDF whose return value must be a URL, might be <code>null</code> */
   private PSExtensionCall m_converter = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String ANCHOR_ELEM = "Anchor";
   private static final String HREF_ELEM = "Href";
   private static final String NAME_ATTR = "name";

}
