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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * The PSResultPage class defines the style sheet to associate with the
 * result data. This allows formatting to be applied to the output data.
 * Conditionals can be associated with the result page allowing different
 * results to be generated based upon the request context.
 *
 * @see PSResultPageSet
 * @see PSResultPageSet#getResultPages
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSResultPage extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                                          object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                                          object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                                          if the XML element node is not of the
    *                                          appropriate type
    */
   public PSResultPage(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSResultPage() {
      super();
      m_conditionals = new PSCollection(
         com.percussion.design.objectstore.PSConditional.class);
      m_requestLinks = new PSCollection(
         com.percussion.design.objectstore.PSRequestLink.class);
   }

   /**
    * Construct a result page object.
    *
    * @param   styleSheet      the URL of the style sheet defining the output
    *                                    format for the results
    */
   public PSResultPage(java.net.URL styleSheet)
   {
      this();
      setStyleSheet(styleSheet);
   }

   /**
    * Get the style sheet defining the output format for the results.
    * <p>
    * This can be a CSS or XSL file. If HTML conversion is desired, this
    * must be an XSL file which generates HTML output. CSS or XSL specific
    * formatting objects are not supported for HTML generation.
    *
    * @return      the URL of the style sheet defining the output
    *                        format for the results. If this is null, the default
    *                        E2 style sheet is being used.
    */
   public java.net.URL getStyleSheet()
   {
      return m_styleSheet;
   }

   /**
    * Get the style sheet defining the output format for the results.
    * <p>
    * This can be a CSS or XSL file. If HTML conversion is desired, this
    * must be an XSL file which generates HTML output. CSS or XSL specific
    * formatting objects are not supported for HTML generation.
    * <p>
    * The URL defining the style sheet must refer to a file which is
    * available to the E2 server at run-time. This can be on the E2 file
    * system, or on a web site which E2 can access. As such, validation
    * of the URL is not performed until the application is saved to the
    * server.
    *
    * @param   styleSheet      the URL of the style sheet defining the output
    *                                    format for the results. Specify null to use
    *                                    the default E2 style sheet.
    */
   public void setStyleSheet(java.net.URL styleSheet)
   {
      m_styleSheet = styleSheet;
   }

   /**
    * Get the conditional statements associated with this object. If the
    * conditionals evaluate to true, this object will be used.
    *
    * @return       a collection of PSConditional objects (may be null)
    *
    * @see          com.percussion.design.objectstore.PSConditional
    */
   public com.percussion.util.PSCollection getConditionals()
   {
      return m_conditionals;
   }

   /**
    * Set the conditional statement(s) associated with this object. If the
    * conditional evaluates to true, this object will be used.
    * <p>
    * Specifying a conditional of null will allow this object to be used
    * without testing any conditions. Result pages can be chained. The
    * conditionals will be tested in the order in which the result page
    * objects exist in the chain. When an objects conditions are met, or a
    * null condition is encountered, no further execution will occur. Be sure
    * not to include more than one result page with a null conditional. Also
    * be sure not to place the result page with the null conditional before
    * result pages with conditionals.
    * <P>
    * Conditional syntax will not be validated until the application
    * is saved to the server.
    *
    * @param      conditionals         the new conditional statement(s)
    *                                                (may be null)
    *
    * @see          com.percussion.design.objectstore.PSConditional
    */
   public void setConditionals(PSCollection conditionals)
   {
      IllegalArgumentException ex = validateConditionals(conditionals);
      if (ex != null)
         throw ex;

      m_conditionals = conditionals;
   }

   private static IllegalArgumentException validateConditionals(
      PSCollection conditionals)
   {
      if (conditionals != null) {
         if (!com.percussion.design.objectstore.PSConditional.class.isAssignableFrom(
            conditionals.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type, Conditional: " +
               conditionals.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Get the data sets which are linked to this result page. E2 can be
    * used to generate the appopriate URL links in the result page.
    * <p>
    * When defining pages with relationships to other pages, E2 can be used
    * to dynamically generate the appropriate URL to access the desired
    * data set. For instance, one data set may return a list of orders.
    * From this result page, it may be desirable to get the order details.
    * By linking the order list result page to the order detail data tank,
    * E2 can automatically generate the appropriate URL to access a
    * specific order.
    *
    * @return      a collection containing the linked data sets
    *                        (PSRequestLink objects) (may be null)
    */
   public com.percussion.util.PSCollection getRequestLinks()
   {
      return m_requestLinks;
   }

   /**
    * Overwrite the request links associated with this result page with
    * the specified collection. If you only want to modify certain links,
    * add a new link, etc. use getRequestLinks to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * When defining pages with relationships to other pages, E2 can be used
    * to dynamically generate the appropriate URL to access the desired
    * data set. For instance, one data set may return a list of orders.
    * From this result page, it may be desirable to get the order details.
    * By linking the order list result page to the order detail data tank,
    * E2 can automatically generate the appropriate URL to access a
    * specific order.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSResultPage object. Any subsequent changes made to the object by
    * the caller will also effect the result page object.
    *
    * @param      links    the new request links (may be null)
    *
    * @see         PSRequestLink
    */
   public void setRequestLinks(com.percussion.util.PSCollection links)
   {
      IllegalArgumentException ex = validateRequestLinks(links);
      if (ex != null)
         throw ex;

      m_requestLinks = links;
   }

   private static IllegalArgumentException validateRequestLinks(
      PSCollection links)
   {
      if (links != null) {
         if (!com.percussion.design.objectstore.PSRequestLink.class.isAssignableFrom(
            links.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type, Request Link: " +
               links.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param pipe a valid PSResultPage. 
    */
   public void copyFrom( PSResultPage page )
   {
      copyFrom((PSComponent) page );
      // assume page is in valid state
      m_styleSheet = page.getStyleSheet();
      m_conditionals = page.getConditionals();
      m_requestLinks = page.getRequestLinks();
      m_encoding = page.getCharacterEncoding();
      m_mimeType = page.getMimeType();
      m_extensions = page.getExtensions();
      setAllowNamespaceCleanup(page.allowNamespaceCleanup());
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXResultPage XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *          &lt;!--
    *                  PSXResultPage defines the style sheet to associate with the
    *                  result data. This allows formatting to be applied to the output
    *                  data. Conditionals can be associated with the result page
    *                  allowing different results to be generated based upon the
    *                  request context.
    *
    *                  Object References:
    *
    *                  PSXConditional - the conditional statement associated with this
    *                  object. If the conditional evaluates to true, this object will be
    *                  used. Once a matching condition is found, no further evaluation
    *                  is done.
    *
    *                  PSXRequestLink - the data sets which are linked to this result
    *                  page. When defining pages with relationships to other pages, E2
    *                  can be used to dynamically generate the appropriate URL to access
    *                  the desired data set. For instance, one data set may return a
    *                  list of orders. From this result page, it may be desirable to get
    *                  the order details. By linking the order list result page to the
    *                  order detail data tank, E2 can automatically generate the
    *                  appropriate URL to access a specific order.
    *          --&gt;
    *          &lt;!ELEMENT PSXResultPage    (styleSheet, PSXConditional*,
    *                                         PSXRequestLink*, mimeType?,
    *                                   extensionsSupported?)&gt;
    *          &lt;!ATTLIST PSXResultPage
    *              id                    #CDATA           #REQUIRED
    *              allowNamespaceCleanup #CDATA           #OPTIONAL
    *          &gt;
    *          &lt;!--
    *       characterEncoding - Is there a character encoding associated
    *       with this result page?  If so specify the encoding here.  If you
    *       want to exclude encoding, no matter what, set this to an
    *       empty string.
    *          --&gt;
    *          &lt;!ATTLIST PSXRequestor
    *       characterEncoding   #CDATA           #OPTIONAL
    *                  &gt;
    *
    *          &lt;!--
    *                  the URL of the style sheet defining the output format for the
    *                  results. This can be a CSS or XSL file. If HTML conversion is
    *                  desired, this must be an XSL file which generates HTML output.
    *                  CSS or XSL specific formatting objects are not supported for
    *                  HTML generation. The URL must refer to a file which is available
    *                  to the E2 server at run-time. This can be on the E2 file system,
    *                  or on a web site which E2 can access.
    *          --&gt;
    *          &lt;!ELEMENT styleSheet       (#PCDATA)&gt;
    *
    *          &lt;!--
    *                  the mime type associated with this request page, if set,
    *       the mime type will be set to this value (as extracted) for
    *       any requests which use this result page,
    *          --&gt;
    *          &lt;!ELEMENT mimeType       (IPSReplacementValue)&gt;
    *          &lt;!--
    *                  the extensions which will be handled by this result page.  to
    *       allow this result page to process all extensions, omit this
    *       element
    *          --&gt;
    *          &lt;!ELEMENT extensionsSupported      (extension*)&gt;
    *
    *          &lt;!--
    *                  an extension accepted by this result page
    *          --&gt;
    *          &lt;!ELEMENT extension               (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXResultPage XML element node
    */
   //Ravi URL object will be saved as text in XML
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));
      root.setAttribute(ATTR_ALLOW_NAMESPACE_CLEANUP,
         Boolean.toString(m_allowNamespaceCleanup));

      if (m_styleSheet != null)
      {
         PSXmlDocumentBuilder.addElement(   doc, root, "styleSheet",
            m_styleSheet.toExternalForm());
      }

      IPSComponent comp;

      // conditionals are a collection
      if (m_conditionals != null) {
         for (int i = 0; i < m_conditionals.size(); i++) {
            comp = (IPSComponent)m_conditionals.get(i);
            if (comp != null)
               root.appendChild(comp.toXml(doc));
         }
      }

      //private          com.percussion.util.PSCollection        m_requestLinks = null;
      if (m_requestLinks != null) {
         int size = m_requestLinks.size();
         for(int i=0; i < size; i++) {
            comp = (IPSComponent)m_requestLinks.get(i);
            if(null != comp)
               root.appendChild(comp.toXml(doc));
         }
      }

      if (m_encoding != null)
         root.setAttribute("characterEncoding", m_encoding);

      if (m_mimeType != null)
      {
         Element mimeType = PSXmlDocumentBuilder.addEmptyElement(doc, root, "mimeType");
         mimeType.appendChild(((IPSComponent) m_mimeType).toXml(doc));
      }

      if (m_extensions != null)
      {
         Element parent = PSXmlDocumentBuilder.addEmptyElement(doc, root, "extensionsSupported");
         Iterator i = m_extensions.iterator();
         while (i.hasNext())
         {
            String extension = i.next().toString();
            PSXmlDocumentBuilder.addElement(doc, parent, "extension", extension);
         }
      }

      return root;
   }

   /**
    * This method is called to populate a PSResultPage Java object
    * from a PSXResultPage XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                                   of type PSXResultPage
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
         
         sTemp = tree.getElementData(ATTR_ALLOW_NAMESPACE_CLEANUP);
         if(sTemp != null && sTemp.length() > 0)
         {
            m_allowNamespaceCleanup = Boolean.valueOf(sTemp).booleanValue();
         }
         else
         {
            m_allowNamespaceCleanup = false;
         }

         sTemp = tree.getElementData("styleSheet");
         if ((sTemp == null) || (sTemp.length() == 0))
            setStyleSheet(null);
         else {
            try {
               m_styleSheet = new URL(sTemp);
            } catch (MalformedURLException e) {
               Object[] args = { ms_NodeType, "styleSheet",
                                 "(URL: " + sTemp + ") " + e.getMessage() };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         org.w3c.dom.Node cur = tree.getCurrent();   // cur = <PSXResultPage>

         m_conditionals.clear();
         String curNodeType = PSConditional.ms_NodeType;
         if (tree.getNextElement(curNodeType, firstFlags) != null){
            PSConditional conditional;
            do{
               conditional = new PSConditional(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_conditionals.add(conditional);
            } while (tree.getNextElement(curNodeType, nextFlags) != null);
         }

         tree.setCurrent(cur);

         m_requestLinks.clear();
         curNodeType = PSRequestLink.ms_NodeType;
         if (tree.getNextElement(curNodeType, firstFlags) != null){
            PSRequestLink   link;
            do{
               link = new PSRequestLink(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_requestLinks.add(link);
            } while (tree.getNextElement(curNodeType, nextFlags) != null);
         }

         // load the character encoding
         m_encoding = tree.getElementData("characterEncoding");

         // load the MIME type
         tree.setCurrent(cur);
         Element mimeNode = tree.getNextElement("mimeType", firstFlags);
         if (mimeNode != null)
         {
            Element replacementValNode = tree.getNextElement(firstFlags);
            if (replacementValNode != null)
               setMimeType(
                  PSReplacementValueFactory.getReplacementValueFromXml(
                  parentDoc, parentComponents, replacementValNode,
                  mimeNode.getTagName(), replacementValNode.getTagName()));
         }

         tree.setCurrent(cur);
         // Check to see what extensions this guy handles...
         if (tree.getNextElement("extensionsSupported", firstFlags) != null)
         {
            String extension = "";
            HashSet set = new HashSet();
            if (tree.getNextElement("extension", firstFlags) != null)
            {
               do
               {
                  extension = tree.getElementData("extension", false);
                  if (extension != null)
                     set.add(extension);
               } while (tree.getNextElement("extension", nextFlags) != null);
            }
            m_extensions = set;
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

      IllegalArgumentException ex = validateConditionals(m_conditionals);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateRequestLinks(m_requestLinks);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      if (m_extensions != null)
      {
         Iterator i = m_extensions.iterator();
         while (i.hasNext())
         {
            if (i.next() == null)
               cxt.validationError(this, 0,
                  "a specified extension in the extension list is null");
         }
      }

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_conditionals != null)
         {
            for (int i = 0; i < m_conditionals.size(); i++)
            {
               Object o = m_conditionals.get(i);
               if (o == null)
                  cxt.validationError(this, 0, "conditional " + i + " == null");
               else if (!(o instanceof PSConditional))
               {
                  Object[] args = new Object[] { "conditional " + i, o.getClass().getName() };
                  cxt.validationError(this, 0, args);
               }
               else
               {
                  PSConditional cond = (PSConditional)o;
                  cond.validate(cxt);
               }
            }
         }

         if (m_requestLinks != null)
         {
            for (int i = 0; i < m_requestLinks.size(); i++)
            {
               Object o = m_requestLinks.get(i);
               if (o == null)
                  cxt.validationError(this, 0, "request link " + i + " == null");
               else if (!(o instanceof PSRequestLink))
               {
                  Object[] args = new Object[] { "request link " + i, o.getClass().getName() };
                  cxt.validationError(this, 0, args);
               }
               else
               {
                  PSRequestLink reqLink = (PSRequestLink)o;
                  reqLink.validate(cxt);
               }
            }
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSResultPage))
         return false;

      PSResultPage other = (PSResultPage)o;

      if (!compare(m_styleSheet, other.m_styleSheet))
         return false;

      if (!compare(m_conditionals, other.m_conditionals))
         return false;

      if (!compare(m_requestLinks, other.m_requestLinks))
         return false;

      if (!compare(m_mimeType, other.m_mimeType))
         return false;

      if (!compare(m_encoding, other.m_encoding))
         return false;

      if (!compare(m_extensions, other.m_extensions))
         return false;
      
      if(!(allowNamespaceCleanup() == other.allowNamespaceCleanup()))
         return false;

      return true;  
   }
   
   public int hashCode()
   {
      int hash = super.hashCode();
      hash += (m_styleSheet.hashCode() 
         + m_conditionals.hashCode()
         + m_requestLinks.hashCode()
         + m_mimeType.hashCode()
         + m_encoding.hashCode()
         + m_extensions.hashCode()
         + (allowNamespaceCleanup() ? 1 : 0));
      
      return hash;
   }
   
      

   /**
    * Set the MIME type for the specified result page. Set this MIME type
    *    to null to use the requestor's mime map.  Set it to the empty string
    *    (i.e. new PSTestLiteral("")) to omit the mime-type from the http
    *    header.  Or set this to a valid replacement value which will be
    *    set in the http header when this result page is used.
    *
    * @param mimeType  The MIME type override for this result page.
    *                      Can be <code>null</code> (no override)
    *                      or the empty string (omit MIME type from header)
    */
   public void setMimeType(IPSReplacementValue mimeType)
   {
      m_mimeType = mimeType;
   }

   /**
    * Get the MIME type for the specified result page.  If set,
    *    this value is used as the HTTP Content-Type header value in
    *    the response. If null, the Requestor MIME map will be honored,
    *    if empty string, the Content-Type header is not sent as
    *    part of the response.
    *
    * @return  The MIME type override for this result page.
    *                      Can be <code>null</code> (no override)
    *                      or the empty string (omit MIME type from header)
    */
   public IPSReplacementValue getMimeType()
   {
      return m_mimeType;
   }

   /**
    * Get the encoding scheme associated with this result page.  If set,
    *    this value is used as the HTTP character-encoding header value in
    *    the response.  If this is null, the Requestor encoding will be used.
    *    If it is the empty string, the encoding will not be set in the
    *    header.
    *
    * @return  The character encoding string identifier.
    *             Can be <code>null</code> (no override)
    *             or the empty string (omit encoding from header).
    */
   public String getCharacterEncoding()
   {
      return m_encoding;
   }

   /**
    * Set the encoding scheme associated with this result page.  To use
    *    the Requestor's encoding, set this value to null.  To omit encoding,
    *    set the value to the empty string ("").  Otherwise set it to the
    *    character encoding identifier you wish to be set in the http header.
    *
    * @param  encoding The encoding string identifier. UTF-8 is an example.
    *          Can be <code>null</code> (no override)
    *          or the empty string (omit encoding from header).
    */
   public void setCharacterEncoding(String encoding)
   {
      m_encoding = encoding;
   }

   /**
    * Is the specified request handled by this result page?
    *
    * <B>Note:</B> Comparisons on extensions are case insensitive, and
    * are stored in lower case in the map.
    *
    * @param reqUrl the url associated with the request
    *
    *
    * @return  <code>true</code> if it is supported,
    *          <code>false</code> otherwise
    */
   public boolean requestIsSupported(String reqUrl)
   {
      if (reqUrl == null)
         return false;

      if ((m_extensions == null) || (m_extensions.size() == 0))
      {
         return true;
      }

      reqUrl = reqUrl.toLowerCase();
      int slashIndex = reqUrl.lastIndexOf('/');
      if (slashIndex > -1)
      {
         String resourcePortion = reqUrl.substring(slashIndex + 1);
         int dotIndex = resourcePortion.indexOf('.');
         if (dotIndex > -1)
         {
            String extension = resourcePortion.substring(dotIndex + 1);
            if (m_extensions.contains(extension.toLowerCase()))
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Set the collection of extensions supported by this result page.  All
    *    members must be <code>String</code> objects which indicate extensions
    *    accepted by this result page.
    *
    * <B>Note:</B>  These extensions must be lower case!  This is to ensure
    *    that we treat extensions case-insensitive.
    *
    * @param c The collection of extensions supported. If empty or
    *    <code>null</code> all extensions will be accepted.
    */
   public void setExtensions(Collection c)
   {
      if (c != null)
      {
         if (c.contains(null))
            throw new IllegalArgumentException("extension collection cannot contain null");
         m_extensions = c;
      }
      else
         m_extensions = new HashSet();
   }

   /**
    * Return the collection of extensions supported by this result page.
    *    All entries will be strings, and no entry will be <code>null</code>.
    *    An empty collection indicates that all extensions are supported by
    *    this result page.
    *
    * @return  the extensions supported, never <code>null</code>
    */
   public Collection getExtensions()
   {
      return m_extensions;
   }
   /**
    * Creates a deep copy of this PSResultPage object
    * @return a clone of this instance
    */
   public Object clone()
   {
      PSResultPage copy = (PSResultPage) super.clone();

      //only string no need to clone each member of m_extensions
      copy.m_extensions = new HashSet(m_extensions);

      copy.m_mimeType = (IPSReplacementValue)m_mimeType.clone();
      copy.m_conditionals = (PSCollection) m_conditionals.clone();
      copy.m_requestLinks = (PSCollection) m_requestLinks.clone();
      copy.setAllowNamespaceCleanup(allowNamespaceCleanup());
      return copy;
   }
   
   /**
    * Indicates that XHTML compliance namespace cleanup can be
    * performed on the results of the style sheet transformation for
    * this results page. 
    * @return <code>true</code> if the namespace cleanup can run for
    * this result page.
    */
   public boolean allowNamespaceCleanup()
   {
      return m_allowNamespaceCleanup;      
   }
   
   /**
    * Sets flag that indicates that XHTML compliance namespace cleanup can be
    * performed on the results of the style sheet transformation for
    * this results page. 
    */
   public void setAllowNamespaceCleanup(boolean allow)
   {
      m_allowNamespaceCleanup = allow;
   }

   // NOTE: when adding members, be sure to update the copyFrom, equals, and validate methods

   /**
    * The collection of supported extensions, never <code>null</code>,
    *    and will not contain any null entries in the set.
    */
   private  Collection           m_extensions = new HashSet();

   /**
    * The replacement value signifying the MIME type to be used when this
    *    result page is selected.  The initial value is <code>null</code>,
    *    indicating that the Requestor's MIME map will be used by default.
    */
   private  IPSReplacementValue  m_mimeType = null;

   /**
    * The string signifying the character encoding to be used when this
    *    result page is selected.  The initial value is <code>null</code>,
    *    indicating that the Requestor's character encoding setting
    *    will be used by default.
    */
   private  String               m_encoding = null;

   private  URL                  m_styleSheet = null;
   private  PSCollection         m_conditionals = null;
   private  PSCollection         m_requestLinks = null;
   
   /**
    * Flag indicating that XHTML compliance namespace cleanup can be
    * performed on the results of the style sheet transformation for
    * this results page. Defaults to <code>false</code>.
    */
   private boolean m_allowNamespaceCleanup = false;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType   = "PSXResultPage";
   
   /**
    *  XML attribute that indicates if the namspace cleanup is allowed
    */
   private static final String ATTR_ALLOW_NAMESPACE_CLEANUP = 
      "allowNamespaceCleanup";
}

