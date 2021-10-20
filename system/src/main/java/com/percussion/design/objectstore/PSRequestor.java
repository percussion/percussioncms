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

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/**
 * The PSRequestor class is used to define what constitutes a request the
 * the data set is interested in.
 * <p>
 * The requestor contains the URL and selection parameters. Selection
 * parameters are provided as conditionals which reference the input data.
 * This is often an INPUT parameter defined on a HTML FORM. If all the
 * request criteria is met, the request will be processed.
 *
 * @see PSDataSet
 * @see PSDataSet#getRequestor
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSRequestor extends PSComponent
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
   public PSRequestor(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a requestor object.
    */
   public PSRequestor()
   {
      super();
      m_selectionParams = new PSCollection(
         com.percussion.design.objectstore.PSConditional.class);
      m_validationRules = new PSCollection(
         com.percussion.design.objectstore.PSConditional.class);
   }

   /**
    * Get the name of the request page. The request page is concatenated
    * with the server request root and the application request root to
    * provide the complete request URL. For instance, a server request
    * root of <code>E2</code>, an application request root of
    * <code>ProductCatalog</code> and a request page of <code>products.xml</code>
    * will cause the associated data set to respond to requests for the
    * URL <code>/E2/ProductCatalog/products.xml</code>.
    * <p>
    * When the request page is specified with an extension, only requests
    * exactly matching the specified page will be accepted. If an extension
    * is not specified in the request page setting, requests will be
    * handled as follows:
    * <table border="1">
    * <tr><td>Extension</td><td>Output</td></tr>
    * <tr><td>-none-</td><td>XML data (MIME type text/xml)</td></tr>
    * <tr><td>.xml</td><td>XML data (MIME type text/xml)</td></tr>
    * <tr><td>.txt</td><td>plain text stream containing XML data
    *                      (MIME type text/plain)</td></tr>
    * <tr><td>.html</td><td>If HTML output generation is enabled, the HTML
    *                       output (MIME type text/html). If HTML output
    *                       generation is not enabled, an error occurs.
    *                       </td></tr>
    * <tr><td>.htm</td><td>If HTML output generation is enabled, the HTML
    *                      output (MIME type text/html). If HTML output
    *                      generation is not enabled, an error occurs.
    *                      </td></tr>
    * <tr><td>other</td><td>An error occurs</td></tr>
    * </table>
    *
    * @return      the name of the request page
    */
   public java.lang.String getRequestPage()
   {
      return m_requestPageName;
   }

   /**
    * Set the name of the request page. The request page is concatenated
    * with the server request root and the application request root to
    * provide the complete request URL. For instance, a server request
    * root of <code>E2</code>, an application request root of
    * <code>ProductCatalog</code> and a request page of <code>products.xml</code>
    * will cause the associated data set to respond to requests for the
    * URL <code>/E2/ProductCatalog/products.xml</code>.
    * <p>
    * When the request page is specified with an extension, only requests
    * exactly matching the specified page will be accepted. If an extension
    * is not specified in the request page setting, requests will be
    * handled as follows:
    * <table border="1">
    * <tr><td>Extension</td><td>Output</td></tr>
    * <tr><td>-none-</td><td>XML data (MIME type text/xml)</td></tr>
    * <tr><td>.xml</td><td>XML data (MIME type text/xml)</td></tr>
    * <tr><td>.txt</td><td>plain text stream containing XML data
    *                      (MIME type text/plain)</td></tr>
    * <tr><td>.html</td><td>If HTML output generation is enabled, the HTML
    *                       output (MIME type text/html). If HTML output
    *                       generation is not enabled, an error occurs.
    *                       </td></tr>
    * <tr><td>.htm</td><td>If HTML output generation is enabled, the HTML
    *                      output (MIME type text/html). If HTML output
    *                      generation is not enabled, an error occurs.
    *                      </td></tr>
    * <tr><td>other</td><td>An error occurs</td></tr>
    * </table>
    *
    * @param  name   the name of the request page
    */
   public void setRequestPage(java.lang.String name)
   {
      IllegalArgumentException ex = validateRequestPage(name);
      if (ex != null)
         throw ex;

      m_requestPageName = name;
   }

   private static IllegalArgumentException validateRequestPage(String name)
   {
      if ((name == null) || (name.length() == 0))
         return new IllegalArgumentException("requestor page name is null");

      return null;
   }

   /**
    * Get the selection criteria for this request.
    * <P>
    * Selection criteria can
    * be provided as conditionals which reference the input data.
    * This is often an INPUT parameter defined on a HTML FORM. The selection
    * criteria will be executed against the incoming data. If the data
    * matches, the request will be handled.
    *
    * @return      a collection containing the selection criteria
    *             (PSConditional objects). May be empty, but never <code>
    *             null</code>.
    *
    * @see         PSConditional
    */
   public com.percussion.util.PSCollection getSelectionCriteria()
   {
      return m_selectionParams;
   }

   /**
    * Overwrite the selection criteria associated with the specified
    * collection. If you only want to modify certain criteria, add a
    * new condition, etc. use getSelectionCriteria to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * Selection criteria can
    * be provided as conditionals which reference the input data.
    * This is often an INPUT parameter defined on a HTML FORM. The selection
    * criteria will be executed against the incoming data. If the data
    * matches, the request will be handled.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSRequestor object. Any subsequent changes made to the object by
    * the caller will also effect the requestor object.
    *
    * @param params   the new selection parameters. If <code>null</code>, all
    * existing criteria are cleared.
    *
    * @see            PSConditional
    */
   public void setSelectionCriteria(com.percussion.util.PSCollection params)
   {
      IllegalArgumentException ex = validateSelectionCriteria(params);
      if (ex != null)
         throw ex;

      if ( null == params )
         m_selectionParams.clear();
      else
         m_selectionParams = params;
   }

   private static IllegalArgumentException validateSelectionCriteria(
      PSCollection params)
   {
      if (params != null) {
         if (!com.percussion.design.objectstore.PSConditional.class.isAssignableFrom(
            params.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type, Selection Parameters: " +
               params.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Get the validation rules to be applied against incoming data.
    *   <P>
    * Validation rules are defined as
    * PSConditional objects which may be chained together to validate
    * the incoming data sent by the requestor. The rules are executed
    * against the input data. If the input data is deemed invalid,
    * an error is returned to the requestor.
    *
    * @return      a collection containing the rules (PSConditional objects).
    * May be empty, but never <code>null</code>.
    *
    * @see         PSConditional
    */
   public com.percussion.util.PSCollection getValidationRules()
   {
      return m_validationRules;
   }

   /**
    * Overwrite the validation rules with the specified collection.
    * If you only want to modify certain rules, add a new rule, etc. use
    * getValidationRules to get the existing collection and modify the
    * returned collection directly.
    * <p>
    * Validation rules are defined as
    * PSConditional objects which may be chained together to validate
    * the incoming data sent by the requestor. The rules are executed
    * against the input data. If the input data is deemed invalid,
    * an error is returned to the requestor.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSRequestor object. Any subsequent changes made to the object by
    * the caller will also effect the requestor object.
    *
    * @param rules   the new input data validation rules (may be null). If
    * <code>null</code>, all existing rules are cleared.
    *
    * @see            #getValidationRules
    * @see            PSConditional
    */
   public void setValidationRules(com.percussion.util.PSCollection rules)
   {
      IllegalArgumentException ex = validateValidationRules(rules);
      if (ex != null)
         throw ex;

      if ( null == rules )
         m_validationRules.clear();
      else
         m_validationRules = rules;
   }

   private static IllegalArgumentException validateValidationRules(
      PSCollection rules)
   {
      if (rules != null) {
         if (!com.percussion.design.objectstore.PSConditional.class.isAssignableFrom(
            rules.getMemberClassType()))
         {
            return new IllegalArgumentException("coll bad content type, Validation Rules: " +
               rules.getMemberClassName());
         }
      }

      return null;
   }

   /**
    * Get the MIME type to use on output. This should normally be null
    * to allow the engine to decide the type based upon the user request.
    * The primary use of MIME type is to supply images. Set the MIME type
    * to the image type stored in the back-end. For instance, if GIF images
    * are being stored, use "image/gif" as the MIME type. When the MIME
    * type override is enabled, only a single back-end column may be
    * selected from the back-end. Attempting to query more than one column
    * will cause an error.
    *
    * @return               the output MIME type or <code>null</code>
    */
   public IPSReplacementValue getOutputMimeType()
   {
      return m_outputMimeType;
   }

   /**
    * Set the MIME type to use on output. This should normally be null
    * to allow the engine to decide the type based upon the user request.
    * The primary use of MIME type is to supply images. Set the MIME type
    * to the image type stored in the back-end. For instance, if GIF images
    * are being stored, use "image/gif" as the MIME type. When the MIME
    * type override is enabled, only a single back-end column may be
    * selected from the back-end. Attempting to query more than one column
    * will cause an error.
    *
    * @param   mimeType      the output MIME type or <code>null</code>
    */
   public void setOutputMimeType(IPSReplacementValue mimeType)
   {
      m_outputMimeType = mimeType;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param req a valid PSRequestor.
    */
   public void copyFrom( PSRequestor req )
   {
      copyFrom((PSComponent) req );
      // assume requestor is in valid state
      m_outputMimeType = req.getOutputMimeType();
      m_requestPageName = req.getRequestPage();
      m_selectionParams = req.getSelectionCriteria();
      m_validationRules = req.getValidationRules();
      m_encoding = req.getCharacterEncoding();
      m_mimeProps = req.getMimeProperties();
      m_directDataStream = req.isDirectDataStream();
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXRequestor XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXRequestor is used to define what constitutes a request the
    *       data set is interested in. The requestor contains the URL and
    *       selection parameters. Selection parameters are provided as
    *       conditionals which reference the input data. This is often an
    *       INPUT parameter defined on a HTML FORM. If all the request
    *       criteria is met, the request will be processed.
    *    --&gt;
    *    &lt;!ELEMENT PSXRequestor         (outPutMimeType?, requestPage?,
    *                                      SelectionCriteria?,
    *                                      ValidationRules?, MimeProperties?)&gt;
    *
    *    &lt;!--
    *       allowHtmlOutput - Is HTML output permitted? Users may request the
    *       result data be returned in HTML or XML format. For performance
    *       reasons, returning data in HTML format is less desirable. Support
    *       for HTML can be disabled to help increase server performance.
    *
    *       directDataStream will be used to determine whether the server
    *       will either handle this request as a direct stream from one
    *       back end column ("yes") or will do xml processing on the result
    *       set ("no")
    *
    *       characterEncoding - Is there a character encoding associated
    *       with this request?  If so specify the encoding here.  If you
    *       want to exclude encoding, no matter what, set this to an
    *       empty string.
    *    --&gt;
    *    &lt;!ATTLIST PSXRequestor
    *       allowHtmlOutput     %PSXIsEnabled    #OPTIONAL
    *       outputMimeType        #CDATA             #OPTIONAL
    *       directDataStream    "yes":"no"       #OPTIONAL
    *       characterEncoding   #CDATA           #OPTIONAL
    *      &gt;
    *
    *    &lt;!--
    *       the mime type to override all requests with, regardless of the
    *       extension.  this mime type will be used unless a mime type is
    *       specified on a result page which is used to process this request
    *    --&gt;
    *    &lt;!ELEMENT outputMimeType         (IPSReplaceMentValue)&gt;
    *
    *    &lt;!--
    *       the name of the request page. The request page is concatenated
    *       with the server request root and the application request root to
    *       provide the complete request URL. For instance, a server request
    *       root of "E2", an application request root of "ProductCatalog" and
    *       a request page of "products.xml" will cause the associated data
    *       set to respond to requests for the URL
    *       "/E2/ProductCatalog/products.xml".
    *
    *    --&gt;
    *    &lt;!ELEMENT requestPage         (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the selection criteria for this request. Selection criteria can
    *       be provided as conditionals which reference the input data.
    *       This is often an INPUT parameter defined on a HTML FORM. The
    *         selection criteria will be executed against the incoming data.
    *         If the data matches, the request will be handled.
    *    --&gt;
    *    &lt;!ELEMENT SelectionCriteria   (PSXConditional*)&gt;
    *
    *    &lt;!--
    *       the validation rules to be applied against incoming data.
    *       Validation rules are defined as PSConditional objects
    *       which may be chained together to validate the incoming data
    *         sent by the requestor. The rules are executed against the
    *         input data. If the input data is deemed invalid,
    *       an error is returned to the requestor.
    *    --&gt;
    *    &lt;!ELEMENT ValidationRules      (PSXConditional*)&gt;
    *
    *    &lt;!--
    *       ***
    *       the mime properties to be handled by this requestor
    *       this element will contain 0 or more elements named after
    *       the extension handled by this requestor, each of these elements
    *       will contain an IPSReplacement value from which the associated
    *       mime type will be retrieved.
    *    --&gt;
    *    &lt;!ELEMENT MimeProperties      (***the mime properties***)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXRequestor XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      root.setAttribute("directDataStream", m_directDataStream ? "yes" : "no");

      if (m_outputMimeType != null)
      {
         Element outputMimeNode = PSXmlDocumentBuilder.addEmptyElement(doc, root, "outputMimeType");
         outputMimeNode.appendChild(((IPSComponent) m_outputMimeType).toXml(doc));
      }

      //private          String      m_requestPageName = "";
      PSXmlDocumentBuilder.addElement(doc, root, "requestPage", m_requestPageName);

      Element         parent;
      IPSComponent   comp;
      int            size;
      //private          com.percussion.util.PSCollection    m_selectionParams = null;
      if (m_selectionParams != null) {
         parent = PSXmlDocumentBuilder.addEmptyElement(doc, root, "SelectionParams");
         size = m_selectionParams.size();
         for(int i=0; i < size; i++) {
            comp = (IPSComponent)m_selectionParams.get(i);
            parent.appendChild(comp.toXml(doc));
         }
      }

      //private          com.percussion.util.PSCollection    m_validationRules = null;
      if (m_validationRules != null) {
         parent = PSXmlDocumentBuilder.addEmptyElement(doc, root, "ValidationRules");
         size = m_validationRules.size();
         for(int i=0; i < size; i++) {
            comp = (IPSComponent)m_validationRules.get(i);
            parent.appendChild(comp.toXml(doc));
         }
      }

      PSXmlDocumentBuilder.addElement(doc, root, "characterEncoding", m_encoding);

      // store the appropriate mime properties object
      Element configNode = PSXmlDocumentBuilder.addEmptyElement(
         doc, root, "MimeProperties");

      if (m_mimeProps != null)
      {
         Iterator iKey = m_mimeProps.keySet().iterator();
         while (iKey.hasNext())
         {
            String key = (String) iKey.next();
            IPSComponent val = (IPSComponent) m_mimeProps.get(key);
            parent = PSXmlDocumentBuilder.addEmptyElement(
               doc, configNode, key);
            parent.appendChild(val.toXml(doc));
         }
      }

      return root;
   }

   /**
    * This method is called to populate a PSRequestor Java object
    * from a PSXRequestor XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRequestor
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
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

         // the MIME type to use
         // change to IPSReplacementValue!
         Node cur = tree.getCurrent();
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         setOutputMimeType(null);

         cur = tree.getCurrent();   // cur = <PSXRequestor>
         /* Backwards compatibility here */
//         sTemp = tree.getElementData("outputMimeType");
//         if ((sTemp != null) && (sTemp.length() > 0))
//         {
//            setOutputMimeType(new PSTextLiteral(sTemp));
//         } else
//         {
            Element mimeNode = tree.getNextElement("outputMimeType", firstFlags);
            if (mimeNode != null)
            {
               Element replacementValNode = tree.getNextElement(firstFlags);
               if (replacementValNode != null)
                  setOutputMimeType(
                     PSReplacementValueFactory.getReplacementValueFromXml(
                     parentDoc, parentComponents, replacementValNode,
                     mimeNode.getTagName(), replacementValNode.getTagName()));
            }
            tree.setCurrent(cur);
//         }

         //private          String      m_requestPageName = "";
         m_requestPageName = tree.getElementData("requestPage");
         if (m_requestPageName == null)
            m_requestPageName = "";

         cur = tree.getCurrent();   // cur = <PSXRequestor>

         //private          com.percussion.util.PSCollection    m_selectionParams = null;
         m_selectionParams.clear();
         if (tree.getNextElement("SelectionParams", firstFlags) != null) {
            String curNodeType = PSConditional.ms_NodeType;
            if (tree.getNextElement(curNodeType, firstFlags) != null){
               PSConditional selector;
               do{
                  selector = new PSConditional(
                     (Element)tree.getCurrent(), parentDoc, parentComponents);
                  m_selectionParams.add(selector);
               } while (tree.getNextElement(curNodeType, nextFlags) != null);
            }
         }

         tree.setCurrent(cur);

         //private          com.percussion.util.PSCollection    m_validationRules = null;
         m_validationRules.clear();
         if (tree.getNextElement("ValidationRules", firstFlags) != null) {
            String curNodeType = PSConditional.ms_NodeType;
            if (tree.getNextElement(curNodeType, firstFlags) != null){
               PSConditional cond = null;
               do{
                  cond = new PSConditional(
                     (Element)tree.getCurrent(), parentDoc, parentComponents);
                  m_validationRules.add(cond);
               } while (tree.getNextElement(curNodeType, nextFlags) != null);
            }
         }

         tree.setCurrent(cur);

         // load the direct data stream flag
         sTemp = tree.getElementData("directDataStream");
         if (sTemp != null)
            m_directDataStream = sTemp.equalsIgnoreCase("yes");
         else  // backwards compatibility
            m_directDataStream = (m_outputMimeType != null);

         // load the character encoding
         m_encoding = tree.getElementData("characterEncoding");

         // load the mime properties
         m_mimeProps = new HashMap();

         firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         if (tree.getNextElement("MimeProperties", firstFlags) != null)
         {
            firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
            nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
            int treeFlags = firstFlags;
            while ((cur = tree.getNextElement(treeFlags)) != null)
            {
               String extension = tree.getCurrent().getNodeName();
               IPSReplacementValue val = null;
               /* It's not tree.getElementData(nodeName, false) */
               Element replElement = tree.getNextElement(
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
               Element node = (Element)tree.getCurrent();
               val = PSReplacementValueFactory.getReplacementValueFromXml(
                  parentDoc, parentComponents, node,
                  extension, node.getNodeName());
               m_mimeProps.put(extension.toLowerCase(), val);
               treeFlags = nextFlags;
               tree.setCurrent(cur);
            }
         } else {
            // If there's nothing here, set it to the default case
            Iterator i = ms_defaultMimeMap.keySet().iterator();
            while (i.hasNext())
            {
               String key = (String) i.next();
               m_mimeProps.put(key.toLowerCase(), ms_defaultMimeMap.get(key));
            }
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

      IllegalArgumentException ex = validateRequestPage(m_requestPageName);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateSelectionCriteria(m_selectionParams);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateValidationRules(m_validationRules);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      // do children
      cxt.pushParent(this);
      try
      {
         if (m_selectionParams != null)
         {
            for (int i = 0; i < m_selectionParams.size(); i++)
            {
               Object o = m_selectionParams.get(i);
               if (o == null)
                  cxt.validationError(this, 0, "selection param " + i + " == null");
               else if (!(o instanceof PSConditional))
               {
                  Object[] args = new Object[] { "selection param " + i, o.getClass().getName() };
                  cxt.validationError(this, 0, args);
               }
               else
               {
                  PSConditional cond = (PSConditional)o;
                  cond.validate(cxt);
               }
            }
         }

         for (int i = 0; i < m_validationRules.size(); i++)
         {
            Object o = m_validationRules.get(i);
            if (o == null)
               cxt.validationError(this, 0, "validation rule " + i + " == null");
            else if (!(o instanceof PSConditional))
            {
               Object[] args = new Object[] { "validation rule " + i, o.getClass().getName() };
               cxt.validationError(this, 0, args);
            }
            else
            {
               PSConditional cond = (PSConditional)o;
               cond.validate(cxt);
            }
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRequestor)) return false;
      if (!super.equals(o)) return false;
      PSRequestor that = (PSRequestor) o;
      return m_directDataStream == that.m_directDataStream &&
              Objects.equals(m_mimeProps, that.m_mimeProps) &&
              Objects.equals(m_encoding, that.m_encoding) &&
              Objects.equals(m_outputMimeType, that.m_outputMimeType) &&
              Objects.equals(m_requestPageName, that.m_requestPageName) &&
              Objects.equals(m_selectionParams, that.m_selectionParams) &&
              Objects.equals(m_validationRules, that.m_validationRules);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_mimeProps, m_encoding, m_directDataStream, m_outputMimeType, m_requestPageName, m_selectionParams, m_validationRules);
   }

   /**
    * Get the MIME properties.  The properties will be a map
    *    containing the request extension as the key and the MIME type
    *    as the value.
    * <B> Note: </B> Modifications to this class will be reflected in
    *    this requestor!
    */
   public HashMap getMimeProperties()
   {
      return m_mimeProps;
   }

   /**
    * Set the MIME properties.  The properties will be a map
    *    containing the request extension as the key and the MIME type
    *    as the value.
    *
    * <B>Note:<B> The keys in the mime properties map must be lower case!
    *
    * @param  props  The map of extension/mime-type pairs.
    *                      Can be <code>null</code>
    */
   public void setMimeProperties(HashMap props)
   {
      m_mimeProps = props;
   }

   /**
    * Get the MIME type for this requestor.
    *
    * @param  fileExtension  The extension of the request.
    *                      Never <code>null</code>.
    *
    * @return the string returned by the replacementvalue, or null
    *    if a mimeType could not be determined
    */
   public IPSReplacementValue getMimeType(String fileExtension)
   {
      if (fileExtension == null)
         throw new IllegalArgumentException("an extension must be specified");

      if (m_outputMimeType != null)
         return m_outputMimeType;

      if (fileExtension.startsWith("."))
         fileExtension = fileExtension.substring(1);

      return (IPSReplacementValue) m_mimeProps.get(fileExtension.toLowerCase());
   }

   /**
    * Check whether the following extension is handled by this
    *    requestor.
    *
    * @param  extension  The extension of the request.
    *                      Never <code>null</code>.
    *
    * @return <code>true</code>, or <code>false</code> if a mimeType
    *    mapping could not be found.
    */
   public boolean isExtensionSupported(String extension)
   {
      if (extension == null)
         throw new IllegalArgumentException("an extension must be specified");

      if (m_outputMimeType != null)
         return true;

      if (m_mimeProps.get(extension) != null)
         return true;

      return false;
   }

   /**
    * Set the character encoding scheme associated with this requestor.
    *    Specifying the empty string will indicate that no encoding is
    *    to be used in the http content header.
    *
    * @return  The encoding string identifier.  UTF-8 is an example.
    *             Never <code>null</code>.
    */
   public String getCharacterEncoding()
   {
      return m_encoding;
   }

   /**
    * Set the encoding scheme associated with this requestor.  Use
    *    the empty string to indicate that the encoding parameter of
    *    the http content header is not to be set.
    *
    * @param  encoding The encoding string identifier. UTF-8 is an example.
    *          Never <code>null</code>.
    */
   public void setCharacterEncoding(String encoding)
   {
      if (encoding == null)
         throw new IllegalArgumentException("encoding string can not be null");

      m_encoding = encoding;
   }

   /**
    * Set whether or not this requestor is for a single column request
    *    for streaming data without using the Xml mapper.
    *
    * @param enableDirectDataStream set to
    *    <code>true</code> to set direct data mode, and <code>false</code>
    *    if this request is to be handled through the Xml processor
    */
   public void setDirectDataStream(boolean enableDirectDataStream)
   {
      m_directDataStream = enableDirectDataStream;
   }

   /**
    * Is this requestor for a single column request for streaming
    * data with no mapping?
    *
    * @return  <code>true</code> if so, and <code>false</code>
    *    if this request is to be handled through the Xml processor
    */
   public boolean isDirectDataStream()
   {
      return m_directDataStream;
   }

   private static HashMap ms_defaultMimeMap = new HashMap();

   static
   {
      ms_defaultMimeMap.put("html", new PSTextLiteral(IPSMimeContentTypes.MIME_TYPE_TEXT_HTML));
      ms_defaultMimeMap.put("htm", new PSTextLiteral(IPSMimeContentTypes.MIME_TYPE_TEXT_HTML));
   }


   /* NOTE: when adding members, be sure to update the copyFrom,
      equals, and validate methods
      Also: toXml and fromXml when appropriate */

   /** The mime properties for this dataset, where the key is the file
    *    extension, and the value is the mime-type string.
    */
   private HashMap m_mimeProps = null;

   /** The character encoding associated with this dataset,
    *    never <code>null</code>, the empty string indicates no encoding.
    */
   private String m_encoding = "";

   /**
    * Does this request select data from one column and stream it
    *    directly?  Defaults to <code>false</code>
    */
   private boolean m_directDataStream = false;

   private      IPSReplacementValue  m_outputMimeType = null;
   private      String               m_requestPageName = "";

   /** never <code>null</code> after it has been inited in ctor */
   private     PSCollection         m_selectionParams = null;   //PSConditional objects

   /** never <code>null</code> after it has been inited in ctor */
   private     PSCollection         m_validationRules = null;   // PSConditional objects

   /* package access on this so they may reference each other in fromXml */
   static public final String   ms_NodeType            = "PSXRequestor";
}

