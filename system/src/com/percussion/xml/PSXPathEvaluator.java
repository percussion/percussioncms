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

package com.percussion.xml;

import com.icl.saxon.Context;
import com.icl.saxon.Controller;
import com.icl.saxon.expr.Expression;
import com.icl.saxon.expr.StandaloneContext;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.om.AbstractNode;
import com.icl.saxon.om.NodeEnumeration;
import com.icl.saxon.om.NodeInfo;
import com.percussion.data.PSInternalRequestURIResolver;
import com.percussion.security.xml.PSCatalogResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Uses the SAXON transformer to evaluate XPath expressions against a given
 * XML source.
 */
public class PSXPathEvaluator
{
   /**
    * Constructs a <code>PSXPathEvaluator</code> to apply XPath expressions
    * to an {@link InputStream}.
    *
    * @param source Target of XPath expressions, not <code>null</code>.
    * The contents of the stream must be parsable into XML.  The source is
    * copied into an internal representation, therefore modifications to the
    * source object after this constructor has run will not visible to this
    * evaluator.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code>.
    * @throws TransformerException if <code>source</code> could not be parsed.
    */
   public PSXPathEvaluator(InputStream source) throws TransformerException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      init(new Controller().makeBuilder().build(
         new SAXSource(new InputSource(source))));
   }

   /**
    * Constructs a <code>PSXPathEvaluator</code> to apply XPath expressions
    * to a {@link org.w3c.dom.Document Document}.
    *
    * @param source Target of XPath expressions, not <code>null</code>, may be
    * empty.  The source is copied into an internal representation, therefore
    * modifications to the source object after this constructor has run will
    * not visible to this evaluator.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code>.
    * @throws TransformerException if an identify transformer could not be
    * constructed or if an error occurs while copying <code>source</code>.
    */
   public PSXPathEvaluator(Document source) throws TransformerException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      DOMSource ds = new DOMSource(source);
      DOMResult dr = new DOMResult();

      // make a new identity transform
      TransformerFactory tfactory = TransformerFactory.newInstance();
      PSCatalogResolver cr = new PSCatalogResolver();
      cr.setInternalRequestURIResolver(new PSInternalRequestURIResolver());
      tfactory.setURIResolver(cr);
      Transformer idt = tfactory.newTransformer();

      // transform just copies the nodes
      idt.transform(ds, dr);

      NodeInfo ni = (NodeInfo)dr.getNode();
      init(ni);
   }
   
   /**
    * One that takes a set of props with OutputKeys.
    * @param source Target of XPath expressions, not <code>null</code>, may be
    * empty.  The source is copied into an internal representation, therefore
    * modifications to the source object after this constructor has run will
    * not visible to this evaluator.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code>.
    * @throws TransformerException if an identify transformer could not be
    * constructed or if an error occurs while copying <code>source</code>.
    * @param props set of props with OutputKeys, never <code>null</code>.
    * @throws TransformerException if an identify transformer could not be
    * constructed or if an error occurs while copying <code>source</code>.
    */
   public PSXPathEvaluator(Document source, Properties props)
      throws TransformerException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      DOMSource ds = new DOMSource(source);
      DOMResult dr = new DOMResult();

      // make a new identity transform
      TransformerFactory tfactory = TransformerFactory.newInstance();
      Transformer idt = tfactory.newTransformer();
      Properties defaultProps = idt.getOutputProperties();
      
      if (props!=null && props.size()>0)
      {
         //set given props
         if (defaultProps!=null)
         {
            defaultProps.putAll(props);
            idt.setOutputProperties(defaultProps);
         }
         else
         {
            idt.setOutputProperties(props);
         }
      }
      
      // transform just copies the nodes
      idt.transform(ds, dr);

      NodeInfo ni = (NodeInfo)dr.getNode();
      
      init(ni);
   }

   /**
    * Constructor for evaluating XPath expressions against a particular node.
    * The <code>node</code> param passed to this constructor should be an
    * instance of <code>com.icl.saxon.om.AbstractNode</code>. Typically, this
    * will be obtained by calling the <code>enumerate()</code> method on a
    * <code>PSXPathEvaluator</code> constructed from a document object.
    *
    * @param node node to be used for evaluating XPath expressions, may not
    * be <code>null</code>, must be an instance of
    * <code>com.icl.saxon.om.AbstractNode</code>
    *
    * @throws TransformerException if any error occurs
    * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
    * or not an instance of <code>com.icl.saxon.om.AbstractNode</code>
    */
   public PSXPathEvaluator(Node node) throws TransformerException
   {
      if (!(node instanceof AbstractNode))
         throw new IllegalArgumentException(
            "node must be an instance of AbstractNode");
      init((NodeInfo)node);
   }

   /**
    * Initialize the member variables.
    *
    * @param node Create a new context with a given node, assumed not
    * <code>null</code>
    */
   private void init(NodeInfo node)
   {
      m_ctl = new Controller();
      m_nodeContext = m_ctl.makeContext(node);
      m_standAloneContext = new StandaloneContext(m_ctl.getNamePool());
   }

   /**
    * Gets the string value of an XPath expression applied to the XML object
    * that was provided when this <code>PSXPathEvaluator</code> was constructed.
    *
    * @param xpath XPath expression to evaluate, may not be <code>null</code> or
    * empty
    *
    * @return the string value of the specified xpath, will be empty if the
    * expression does not evaluate to any node. Never <code>null</code>.
    */
   public String evaluate(String xpath)
      throws XPathException
   {
      if (xpath == null || xpath.trim().length() == 0)
         throw new IllegalArgumentException("xpath may not be null or empty");

      Expression expr = Expression.make( xpath, m_standAloneContext );
      return expr.evaluateAsString( m_nodeContext );

   }

   /**
    * Return an iterator over a list of
    * <code>com.icl.saxon.om.AbstractNode</code> objects.
    * <code>AbstractNode</code> implements the interfaces
    * <code>org.w3c.dom.Node</code> and <code>com.icl.saxon.om.NodeInfo</code>
    * and so can be used as a bridge between Xerces and Saxon. While Xerces
    * uses <code>Node</code> interface, Saxon uses <code>NodeInfo</code>
    * interface.
    *
    * @param xpath the XPath expression to be evaluated, this should
    * evaluate to a nodeset, may not be <code>null</code> or empty
    *
    * @param sorted Indicates whether the nodes are required in document order.
    * If this is <code>false</code>, they may come in any order, but there will
    * be no duplicates.
    *
    * @return an iterator of nodes in the nodeset obtained by evaluating the
    * XPath expression, never <code>null</code>, may be empty
    */
   public Iterator enumerate(String xpath, boolean sorted)
      throws XPathException
   {
      if (xpath == null || xpath.trim().length() == 0)
         throw new IllegalArgumentException("xpath may not be null or empty");

      List list = new ArrayList();

      Expression expr = Expression.make(xpath, m_standAloneContext);
      NodeEnumeration ne = null;
      try
      {
         ne = expr.enumerate(m_nodeContext, sorted);
         while (ne.hasMoreElements())
         {
            AbstractNode node = (AbstractNode)ne.nextElement();
            list.add(node);
         }
      }
      catch (XPathException ex)
      {
         // When the expression does not return a nodeset, XPathException is
         // thrown. Return an iterator over an empty list in this case.
      }
      return list.iterator();
   }

   /**
    * Saxon's processing is controlled by this class. Initialized in the
    * <code>init()</code> method,  never <code>null</code> or modified
    * after initialization.
    */
   private Controller m_ctl;

   /**
    * The root node of the XML object that is searched by this class.
    * Initialized in the <code>init()</code> method,  never <code>null</code>
    * or modified after initialization.
    */
   private Context m_nodeContext;

   /**
    * Static information about namespaces and such. Initialized in the
    * <code>init()</code> method,  never <code>null</code> or modified
    * after initialization.
    */
   private StandaloneContext m_standAloneContext;
}


