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


/**
 * The PSErrorWebPages class defines the pages which will be returned
 * on error. This allows the E2 error handling to be customized
 * on an application basis. If an error page is not defined for a
 * particular error, E2's default error page is returned.
 * <p>
 * When E2 hits an error, it provdes error information
 * in the form of an XML document. To provide diagnostic information to
 * the requestor, it may be preferred to use style sheets which E2 can
 * merge with the XML document to return a descriptive error page.
 * <p>
 * This class is a collection containing PSCustomError objects, and as such
 * is derived from the PSCollection class. Simply use the methods defined
 * in PSCollection to gain access to the PSCustomError objects defined for
 * the collection object.
 *
 * @see PSApplication#getErrorWebPages
 * @see PSCustomError
 * @see com.percussion.util.PSCollection
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSErrorWebPages extends PSCollectionComponent
{
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
   public PSErrorWebPages(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Default construct for fromXml, serialization, etc.
    */
   PSErrorWebPages()
   {
      this(true);
   }

   /**
    * Construct an empty error web pages object.
    *
    * @param returnHtml    <code>true</code> to return the error page as
    *                      HTML, <code>false</code> to return it as XML
    */
   public PSErrorWebPages(boolean returnHtml)
   {
      super( (new PSCustomError()).getClass() );
   }

   /**
    * Is the error page returned as HTML?
    *
    * @return               <code>true</code> if the error page is returned
    *                      as HTML, <code>false</code> otherwise
    */
   public boolean isHtmlReturned()
   {
      return m_html;
   }

   /**
    * Is the error page returned as HTML?
    *
    * @param returnHtml    <code>true</code> to return the error page as
    *                      HTML, <code>false</code> to return it as XML
    */
   public void setHtmlReturned(boolean returnHtml)
   {
      m_html = returnHtml;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param  errPages a valid PSErrorWebPages.
    *
    */
   public void copyFrom( PSErrorWebPages errPages )
   {
      copyFrom((PSCollectionComponent) errPages );
      // assume object is valid
      // must use errPages as source (bug id TGIS-4BPTR6)
      m_html = errPages.m_html;
   }

   public boolean equals(Object o)
   {
      boolean bEqual = true;
      if ( !super.equals(o))
         bEqual = false;
      if (o instanceof PSErrorWebPages)
      {
         PSErrorWebPages other = (PSErrorWebPages)o;
         if ( m_html != other.m_html )
            bEqual = false;
      }

      return bEqual;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXErrorWebPages XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXErrorWebPages defines the pages which will be returned on
    *       error. This allows the E2 error handling to be customized on
    *       an application basis. If an error page is not defined for a
    *       particular error, E2's default error page is returned.
    *
    *       When E2 hits an error, it provdes error information in the form
    *       of an XML document. To provide diagnostic information to the
    *       requestor, it may be preferred to use style sheets which E2 can
    *       merge with the XML document to return a descriptive error page.
    *
    *       Object References:
    *
    *       PSXCustomError - this class is a container for PSCustomError
    *       objects, which define the error code/page combinations.
    *    --&gt;
    *    &lt;!ELEMENT PSXErrorWebPages (PSXCustomError*)&gt;
    *
    *    &lt;!--
    *       attributes for this object:
    *
    *       returnHtml - is the error page returned as HTML?
    *    --&gt;
    *    &lt;!ATTLIST PSXErrorWebPages
    *       returnHtml     %PSXIsEnabled   #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXErrorWebPages XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private          boolean         m_html = true;
      root.setAttribute("returnHtml", m_html ? "yes" : "no");

      //convert all PSCustormErros to xml
      PSCustomError      customError;
      int size = size();
      for (int i=0; i < size; i++)
      {
         customError = (PSCustomError)get(i);
         root.appendChild(customError.toXml(doc));
      }

      return root;
   }

   /**
    * This method is called to populate a PSErrorWebPages Java object
    * from a PSXErrorWebPages XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXErrorWebPages
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
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

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         //private          boolean         m_html = true;
         sTemp = tree.getElementData("returnHtml");
         m_html = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         //read all customError object in this collection
         clear();
         PSCustomError    customError;
         String curNodeType = PSCustomError.ms_NodeType;
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
               curNode != null;
               curNode = tree.getNextElement(curNodeType, nextFlags))
         {
            customError = new PSCustomError(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
            add(customError);
         }
      } finally {
         resetParentList(parentComponents, parentSize);
      }
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
         super.validate(cxt);
      }
      finally
      {
         cxt.popParent();
      }
   }

   private          boolean         m_html = true;

   /* public access on this so they may reference each other in fromXml,
    * including legacy classes */
   public static final String   ms_NodeType            = "PSXErrorWebPages";
}
