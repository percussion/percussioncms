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

package com.percussion.xml;

import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The PSXmlTreeWalker class is used to simplify processing of XML trees.
 * It is a generic walker used to easily find top level elements,
 * child elements and their children.
 * <p>
 * To use the PSXmlTreeWalker, construct a walker for the XML document.
 * The walker can then be used to search for entries, retrieve a subset
 * of the document, etc. This greatly simplifies processing results, as XML
 * documents can contain fairly complex structures.
 */
public class PSXmlTreeWalker implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -5622257592034103174L;

   /**
    * If a matching node is not found during the traversal, reset the
    * current node to the one prior to the search.
    */
   public static final int GET_NEXT_RESET_CURRENT      = 0x0001;

   /**
    * Allow traversal of parent nodes.
    */
   public static final int GET_NEXT_ALLOW_PARENTS      = 0x0002;

   /**
    * Allow traversal of siblings.
    */
   public static final int GET_NEXT_ALLOW_SIBLINGS      = 0x0004;

   /**
    * Allow traversal of children.
    */
   public static final int GET_NEXT_ALLOW_CHILDREN      = 0x0008;

   /**
    * A shorthand way to refer to the current node's parent.
    */
   public static final String NODENAME_PARENT   = "..";

   /**
    * A shorthand way to refer to the current node.
    */
   public static final String NODENAME_CURRENT   = ".";

   /**
    * The header which can be used to get a node using the relative
    * position of the root. Simply append the node name under the root
    * to this name. For instance, to get the first node named "data" from
    * the root, use
    * RELATIVE_NODENAME_ROOT + "data", which resolves to "/data".
    */
   public static final String RELATIVE_NODENAME_ROOT   = "/";

   /**
    * The header which can be used to get a node using the relative
    * position of the parent. Simply append the node name under the parent
    * to this name. For instance, to get the first node named "data" from
    * the parent of the current node, use
    * RELATIVE_NODENAME_PARENT + "data", which resolves to "../data".
    */
   public static final String RELATIVE_NODENAME_PARENT   = "../";

   /**
    * The header which can be used to get a node using the relative
    * position of the current node. Simply append the node name under the
    * current node to this name. For instance, to get the first node named
    * "data" from the current node, use
    * RELATIVE_NODENAME_CURRENT + "data", which resolves to "./data".
    * This is not really required, as "data" is also sufficient to find
    * the appropriate child of the current node.
    */
   public static final String RELATIVE_NODENAME_CURRENT   = "./";

   /**
    * The delimiter used to separate XML elements in an XPath expression.
    */
   public static final String XML_ELEMENT_DELIMITER = "/";
   
   /**
    * Creates a walker for the specified document. Walkers provide
    * a simplified way to traverse XML documents.
    *
    * @param    doc    the document to traverse
    */
   public PSXmlTreeWalker(Document doc)
   {
      super();

      m_doc  = doc;
      m_root = doc.getDocumentElement();
      m_cur  = m_root;
   }

   /**
    * Creates a walker for the specified element. Walkers provide
    * a simplified way to traverse XML documents.
    *
    * @param    root       the root node for traversal
    */
   public PSXmlTreeWalker(Element root)
   {
      super();

      m_doc  = null;
      m_root = root;
      m_cur  = m_root;
   }

   /**
    * Creates a walker for the specified node. Walkers provide
    * a simplified way to traverse XML documents.
    *
    * @param    root       the root node for traversal
    */
   public PSXmlTreeWalker(Node root)
   {
      super();

      m_doc  = null;
      m_root = root;
      m_cur  = m_root;
   }

   /**
    * Get the value (data) associated with an element, optionally starting
    * the search for the element from the root node.
    * <P>
    * A subset of the XSL positioning rules are supported. When
    * <code>fromRoot</code> is <code>false</code>, relative positioning
    * can be used from the current node (<code>./</code>) or from the
    * current node's parent (<code>../</code>). Attribute names can be
    * located by preceeding the attribute name with <code>@</code>.
    * Also, the tree can be traversed hierarchically to access children,
    * such as <code>./Name/first</code> to access the data associated with
    * the <code>first</code> which is a child of the <code>Name</code>
    * element which is a child of the current node.
    *
    * @param   name         the name of the element to retrieve
    *
    * @param   fromRoot      <code>true</code> to start the search from
    *                        the root element; <code>false</code> to start
    *                        from the current element in the tree
    *
    * @return               the value of the element or <code>null</code>
    *                        if a matching element is not found
    */
   public java.lang.String getElementData(java.lang.String name,
   boolean fromRoot)
   {
      String   ret = null;
      Node      cur, savedCur;

      savedCur = m_cur;   // to reset if we repositioned to the parent node
      if (fromRoot)
         cur = m_root;
      else
         cur = m_cur;

      if (cur != null) {
         try {
            if (name.equals(NODENAME_CURRENT)) {
               // we want the data from the current element; otherwise null
               if (cur instanceof Element)
                  return getElementData((Element)cur);
               else
                  return null;
            }
            else if (name.equals(NODENAME_PARENT)) {
               // we want the data from the parent element; otherwise null
               cur = cur.getParentNode();
               if ((cur != null) && (cur instanceof Element))
                  return getElementData((Element)cur);
               else
                  return null;
            }
            else if (name.startsWith(RELATIVE_NODENAME_PARENT)) {
               // we want the data from a child of our parent node
               m_cur = cur.getParentNode();
               return getElementData(
               name.substring(RELATIVE_NODENAME_PARENT.length()),
               false);   // use the node we just set
            }
            else if (name.startsWith(RELATIVE_NODENAME_CURRENT)) {
               // we want the data from this node. just strip off the
               // header and fall into the default logic below
               name = name.substring(RELATIVE_NODENAME_CURRENT.length());
            }
            else if (name.startsWith(RELATIVE_NODENAME_ROOT)) {
               // we want the data from the root node. just strip off the
               // header, set current to the doc, which is above the root,
               // so we can find the root when entering the default logic
               // below
               name = name.substring(1);
               m_cur = m_doc;
            }

            if (name.startsWith("@")) {
               // @ means this is an attribute, so look only in attributes
               ret = getDataFromAttribute(cur, name.substring(1));
            }
            else {
               // see if we have nesting within the name
               int childPos = name.indexOf('/');
               if (childPos != -1) {
                  // can only be found under the children of this node
                  m_cur = getNextElement(name.substring(0, childPos),
                  GET_NEXT_ALLOW_CHILDREN);

                  return getElementData(name.substring(childPos+1), false);
               }

               // is the current element the one we're looling for?
               if ((ret = getNodeMatchValue(cur, name)) == null) {
                  // is this an attribute of the current element?
                  if ((ret = getDataFromAttribute(cur, name)) == null) {
                     // well then it had better be child data
                     ret = getDataFromChildElement(cur, name);
                  }
               }
            }
         } catch (StringIndexOutOfBoundsException e) {
            /* this occurs when the name they've supplied is invalid
             * which causes substring to throw the exception. For instance,
             * the name "./Name/" has no field to get data from, which will
             * fail as we call substring with the index past the last /.
             *
             * We can add an additional check above to avoid the exception,
             * but the incidence of this type of exception should be minimal
             * (I dare say non-existant). As such, the catch should be
             * more efficient.
             */
            return null;
         } finally {
            m_cur = savedCur;
         }
      }

      return ret;
   }

   /**
    * Get the value (data) associated with an element. The search for a
    * matching element will start from the root node.
    *
    * @param   name         the name of the element to retrieve
    *
    * @return               the value of the element or <code>null</code>
    *                        if a matching element is not found
    */
   public String getElementData(String name)
   {
      return getElementData(name, true);
   }

   /**
    * Gets the text data associated with the current node of this walker, by
    * concatenating the values of all child TEXT_NODEs and
    * ENTITY_REFERENCE_NODEs.
    * @return text data of the node; never <code>null</code>, may be empty if
    * the current node has no text data.
    */
   public String getElementData()
   {
      return getElementData( getCurrent() );
   }

   /**
    * Get the value (text data) associated with the specified element.
    * If the specified element is null or has no text data,
    * returns the empty string.
    *
    * @param   element the element to retrieve data from
    *
    * @return  the value of the element, never <code>null</code> may be empty if
    * the specified element is <code>null</code> or has no text data.
    */
   public static String getElementData(Element element)
   {
      return getElementData((Node)element);
   }

   /**
    * Get the value (text data) associated with the specified node.
    * If the specified node is <code>null</code> or has no text data,
    * returns the empty string.
    *
    * @param   node the element or entity ref node to retrieve the data from,
    * if it is <code>null</code>, returns an empty string.
    *
    * @return   the value of the element, never <code>null</code> may be empty if
    * the specified element is <code>null</code> or has no text data.
    */
   public static String getElementData(Node node)
   {
      StringBuffer ret = new StringBuffer();
      Node text;

      if (node != null)
      {
         for (text = node.getFirstChild();
         text != null;
         text = text.getNextSibling() )
         {
            /* the item's value is in one or more text nodes which are
             * its immediate children
             */
            if (text.getNodeType() == Node.TEXT_NODE)
               ret.append(text.getNodeValue());
            else
               /***
                * DB: when there are embedded entities in element data, the
                * "Actual Value" of the entity will be contained in one or more
                * Text nodes as children of the entity ref node.  We call ourselves
                * recursively to process these additional nodes.
                ***/
               if (text.getNodeType() == Node.ENTITY_REFERENCE_NODE)
               {
                  ret.append(getElementData(text));
               }
         }
      }

      return ret.toString();
   }

   /**
    * Get the next element with the specified name. This method will
    * traverse parents if it cannot find siblings or children with
    * the specified name. Use getNextElement(name, true) to only
    * traverse siblings and children.
    *
    * @param   name           the name of the element to retrieve
    *
    * @return                 the requested Element node
    */
   public Element getNextElement(String name)
   {
      int flags = GET_NEXT_ALLOW_SIBLINGS | GET_NEXT_ALLOW_CHILDREN |
      GET_NEXT_ALLOW_PARENTS;
      return getNextElement(name, flags);
   }

   /**
    * Get the next element with the specified name, optionally traversing
    * only siblings and children of the current node.
    *
    * @param   name            the name of the element to retrieve
    *
    * @param   noParents      <code>true</code> to traverse only siblings
    *                           and children
    *
    * @return                  the requested Element node
    */
   public Element getNextElement(String name, boolean noParents)
   {
      int flags = GET_NEXT_ALLOW_SIBLINGS | GET_NEXT_ALLOW_CHILDREN |
      (noParents ? 0 : GET_NEXT_ALLOW_PARENTS);
      return getNextElement(name, flags);
   }

   /**
    * Get the next element with the specified name, optionally traversing
    * only siblings and children of the current node or resetting
    * the current node upon failure.
    *
    * @param   name            the name of the element to retrieve
    *
    * @param   noParents      <code>true</code> to traverse only siblings
    *                           and children
    *
    * @param   resetCur         <code>true</code> to restore the current
    *                           node when a match is not found for the
    *                           specified element
    *
    * @return                  the requested Element node
    */
   public Element getNextElement(String name, boolean noParents,
   boolean resetCur)
   {
      int flags = GET_NEXT_ALLOW_SIBLINGS | GET_NEXT_ALLOW_CHILDREN;
      if (!noParents) flags |= GET_NEXT_ALLOW_PARENTS;
      if (resetCur) flags |= GET_NEXT_RESET_CURRENT;

      return getNextElement(name, flags);
   }

   /**
    * Get the next element with the specified name, optionally traversing
    * only siblings and children of the current node or resetting
    * the current node upon failure.
    *
    * @param   name            the name of the element to retrieve
    *
    * @param   flags            the appropriate GET_NEXT_xxx flags
    *
    * @return                  the requested Element node
    */
   public Element getNextElement(String name, int flags)
   {
      if (m_cur == null)
         return null;

      Node saveCur = m_cur;   // save this in case of failure
      boolean resetCur = ((flags & GET_NEXT_RESET_CURRENT) == GET_NEXT_RESET_CURRENT);
      boolean noParents = ((flags & GET_NEXT_ALLOW_PARENTS) == 0);
      boolean noSiblings = ((flags & GET_NEXT_ALLOW_SIBLINGS) == 0);

      Node  next;
      Node  stopNode;
      if (noSiblings)
         stopNode = m_cur;
      else if (noParents)
         stopNode = m_cur.getParentNode();
      else
         stopNode = m_root;

      if (name != null) {
         if (NODENAME_CURRENT.equals(name))
            return (Element)m_cur;
         else if (NODENAME_PARENT.equals(name)) {
            if (m_cur == null)
               return null;

            m_cur = m_cur.getParentNode();
            return (Element)m_cur;
         }
      }

      /* we allow searching for names of the form node1/.../nodex
       * when this syntax is encountered, we recurse until we get
       * to the name we need
       */
      int namePos = (name == null) ? -1 : name.indexOf('/');
      if (namePos != -1) {
         // first find the base name, which should be under us somewhere
         next = getNextElement(name.substring(0, namePos), flags);
         if (next != null) {
            // now try again for the child components. Since these must be
            // children, only use the children flag
            next = getNextElement(
            name.substring(namePos+1), GET_NEXT_ALLOW_CHILDREN);
         }
      }
      else {
         /* we need to traverse the tree by getting the first child of this
          * node. On subsequent iterations, we need to move back up the
          * children's siblings and parents. We tell it who our parent is
          * so we can verify it doesn't try going past our parent
          * (when noParent is set).
          * parent logic in here! Once we hit the node we started
          * with, we must stop if they don't want data from parents or
          * siblings.
          */
         int nextRunFlags = flags | GET_NEXT_ALLOW_SIBLINGS |
         GET_NEXT_ALLOW_PARENTS;

         for (next = getNext(flags, stopNode);
         next != null;
         next = getNext(nextRunFlags, stopNode) )
         {
            String unqualified_name = isQualifiedName(name) ?
               next.getNodeName() :
               getUnqualifiedNodeName(next);

            if ( (next instanceof org.w3c.dom.Element) &&
            ( (name == null) || name.equals(unqualified_name) ) )
            {   // return the node we found, it's a match
               break;
            }
         }
      }

      // if we didn't find a match and we need to reset the current node
      // on failure, do so now
      if ((next == null) && resetCur)
         m_cur = saveCur;

      return (Element)next;
   }

   /**
    * Return the node's name without any namespace qualifier
    * @param next Node make not be <code>null</code>
    * @return The unqualified name
    */
   protected String getUnqualifiedNodeName(Node next)
   {
       String unqualified_name = next.getLocalName();
       if (unqualified_name == null)
       {
         unqualified_name = next.getNodeName();
       }
       return unqualified_name;
   }

   /**
    * Indicates that the name passed in is qualified.
    *
    * @param the element name. May be <code>null</code>.
    * @return <code>true</code> if the name passed in is
    * qualified.
    */
   private boolean isQualifiedName(String name)
   {
      return null == name ? false :(name.indexOf(":") > 0);
   }

   /**
    * Get the next element, optionally traversing only siblings and
    * children of the current node.
    *
    * @param noParents <code>true</code> to traverse only siblings and
    * children.
    *
    * @return the next Element node, or <code>null</code> if there are
    * no more elements
    */
   public Element getNextElement(boolean noParents)
   {
      int flags = GET_NEXT_ALLOW_SIBLINGS | GET_NEXT_ALLOW_CHILDREN |
      (noParents ? 0 : GET_NEXT_ALLOW_PARENTS);
      return getNextElement(null, flags);
   }

   /**
    * Get the next element, optionally traversing only siblings and
    * children of the current node.
    *
    * @param   flags            the appropriate GET_NEXT_xxx flags
    *
    * @return the next Element node, or <code>null</code> if there are
    * no more elements
    */
   public Element getNextElement(int flags)
   {
      return getNextElement(null, flags);
   }

   /**
    * Get the next node in the tree. This may be at any level. The search
    * is done by walking down each child, and then across siblings and their
    * associated children.
    *
    * @return                 the next Node in the tree
    */
   public Node getNext() {
      int flags = GET_NEXT_ALLOW_SIBLINGS | GET_NEXT_ALLOW_CHILDREN |
      GET_NEXT_ALLOW_PARENTS;
      return getNext(flags);
   }

   /**
    * Get the next node in the tree, optionally traversing
    * only siblings and children of the current node.
    * This may be at any level. The search
    * is done by walking down each child, and then across siblings and their
    * associated children.
    *
    * @param   noParents        <code>true</code> to traverse only siblings
    *                       and children
    *
    * @return                 the next Node in the tree
    */
   public Node getNext(boolean noParents)
   {
      int flags = GET_NEXT_ALLOW_SIBLINGS | GET_NEXT_ALLOW_CHILDREN |
      (noParents ? 0 : GET_NEXT_ALLOW_PARENTS);
      return getNext(flags);
   }

   /**
    * Get the next node in the tree, optionally traversing
    * only siblings and children of the current node.
    * This may be at any level. The search
    * is done by walking down each child, and then across siblings and their
    * associated children.
    *
    * @param   flags            the appropriate GET_NEXT_xxx flags
    *
    * @return                  the next Node in the tree or <code>null</code>
    *                           if none found
    */
   public Node getNext(int flags)
   {
      return getNext(flags, m_root);
   }

   /**
    * Get the next node in the tree, optionally traversing
    * only siblings and children of the current node.
    * This may be at any level. The search
    * is done by walking down each child, and then across siblings and their
    * associated children.
    *
    * @param   flags            the appropriate GET_NEXT_xxx flags
    *
    * @param   stopNode         stop processing when this node is encountered
    *                           (returning <code>null</code> for next
    *
    * @return                  the next Node in the tree or <code>null</code>
    *                           if none found
    */
   public Node getNext(int flags, Node stopNode)
   {
      Node next = null;

      boolean noSiblings = (flags & GET_NEXT_ALLOW_SIBLINGS) == 0;
      boolean noKids = (flags & GET_NEXT_ALLOW_CHILDREN) == 0;
      boolean noParents = (flags & GET_NEXT_ALLOW_PARENTS) == 0;

      next = (noKids ? null : m_cur.getFirstChild());
      if (next == null) {
         next = (noSiblings ? null : m_cur.getNextSibling());
         if ( (next == null) && !noParents) {
            for (Node parent = m_cur.getParentNode();
            next == null;
            parent = parent.getParentNode())
            {
               if ((parent == stopNode) || (parent == null))
                  break;

               next = (noSiblings ? null : parent.getNextSibling());
            }
         }
      }

      m_cur = next;  // set the current position, which may be null!

      return next;
   }

   /**
    * Get the node the tree walker is currently position on.
    *
    * @return                 the current Node
    */
   public Node getCurrent()
   {
      return m_cur;
   }
   
   /**
    * Get the node name for the node that the tree walker is currently 
    * positioned on.
    * 
    * @return the current {@link Node} name, never <code>null</code>.
    */
   public String getCurrentNodeName()
   {
      return getUnqualifiedNodeName(m_cur);
   }

   /**
    * Set the node the tree walker is currently position on.
    *
    * @param   node           the node to set as the current node
    */
   public void setCurrent(Node node)
   {
      m_cur = node;
   }

   /**
    * Write the tree associated with this walker to the specified
    * stream.
    *
    * @param    ps       the print stream to write to
    */
   public void write(PrintStream ps) throws IOException
   {
      write(new PrintWriter(ps));
   }


   /**
    * Write the tree associated with this walker to the specified
    * stream.
    *
    * @param    out       the output stream to write to
    */
   public void write(OutputStream out) throws IOException
   {
      write(new OutputStreamWriter(out, IPSUtilsConstants.RX_JAVA_ENC));
   }

  /**
   * Write the tree associated with this walker to the specified
   * stream.
   *
   * @param     out        the output stream to write to
   *
   */
   public void write(Writer out) throws IOException
   {
      write(out, true);
   }

   /**
    * Write the tree associated with this walker to the specified
    * stream.
    *
    * @param     out        the output stream to write to
    *
    * @param   indentFlag does this printwriter indent?
    */
   public void write(Writer out, boolean indentFlag) throws IOException
   {
      write(out, indentFlag, false, false);
   }


   /**
    * Write the tree associated with this walker, calls
    * {@link #write(Writer, boolean, boolean, boolean)
    * write(out, indentFlag, omitXMLDeclaration, omitDocumentType, "UTF-8")} 
    */
   public void write(Writer out, boolean indentFlag,
      boolean omitXMLDeclaration, boolean omitDocumentType) throws IOException
   {
      write(out, indentFlag, omitXMLDeclaration, omitDocumentType, "UTF-8");  
   }
   
   /**
    * Write the tree associated with this walker to the specified
    * stream.
    *
    * @param out the output stream to write to. May not be <code>null</code>.
    * This method does not close the output stream.
    * @param indentFlag controls whether the output is indentented or written
    * as a continuous stream. See {@link OutputKeys#INDENT} for more 
    * information about indentation behavior. Most uses of this method
    * should pass <code>true</code> to have this routine indent.
    * @param omitXMLDeclaration If <code>true</code> then the output will
    * not contain a standard xml header. 
    * @param omitDocumentType If <code>true</code> then the output will 
    * definitely not include a DOCTYPE declaration. If <code>false</code> then
    * the DOCTYPE and any contained references will be dependent on the
    * contents of the document. Note that inline DOCTYPE declarations will
    * always be included as they are simply processing instructions in
    * the document structure.
    * @param encoding The encoding to use for the output file, may 
    * be <code>null</code> or empty. Defaults to "UTF-8".
    */
   public void write(Writer out, boolean indentFlag,
      boolean omitXMLDeclaration, boolean omitDocumentType,
      String encoding) throws IOException
   {
      if (out == null)
         throw new IllegalArgumentException("out may not be null");
         
      if (encoding == null || encoding.trim().length() == 0)
      {
         encoding = "UTF-8";
      }  
      
      try
      {
         DOMSource domSource = null;
         StreamResult streamResult = new StreamResult(out);
                 
         Transformer serializer = null;
         synchronized (ms_transformerFactory)
         {
            serializer = ms_transformerFactory.newTransformer();
         }
         
         serializer.setOutputProperty(OutputKeys.ENCODING, encoding);
         serializer.setOutputProperty(OutputKeys.METHOD, "xml");
         serializer.setOutputProperty(OutputKeys.INDENT, 
            indentFlag ? "yes" : "no");
         serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
            omitXMLDeclaration ? "yes" : "no");
         // This is ignored by saxon but used by xalan - which makes this
         // code work correctly in Eclipse
         serializer.setOutputProperty(
               "{http://xml.apache.org/xslt}indent-amount", "2" );
         
         if (! omitDocumentType && m_doc != null)
         {
            DocumentType type = m_doc.getDoctype();
            if (type != null && type.getPublicId() != null)
               serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, 
                  type.getPublicId());
            if (type != null && type.getSystemId() != null)
               serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, 
                  type.getSystemId());
         }
         
         if (m_doc != null)
         {
            domSource = new DOMSource(m_doc);
         }
         else
         {
            if (ms_saxon_loaded 
               && serializer.getClass().getName().startsWith("com.icl.saxon")
               && !(m_root.getClass().getName().endsWith("DocumentImpl")))
            {
               // Saxon does not support transforming a node
               Document doc = PSXmlDocumentBuilder.createXmlDocument();
               Node imported = doc.importNode(m_root, true);
               doc.appendChild(imported);
               domSource = new DOMSource(doc);
            }
            else
            {
               domSource = new DOMSource(m_root);
            }
         }      
         
         serializer.transform(domSource, streamResult);
         out.flush();
      }
      catch (TransformerException e)
      {
         throw new IOException(e.getLocalizedMessage());
      }
   }   
  

   /**
    * Get the lowest level element from the specified lists. This is used
    * to determine which node can be used as an iterator over the tree.
    *
    * @param   elements         a list containing the String names of the
    *                           elements to examine
    *
    * @return                  the name of the node to use as the iterator
    *                           (may be <code>null</code>)
    */
   @SuppressWarnings("unchecked")
   public static String getLowestLevelElement(List elements)
   {
      int size = elements.size();
      if (size == 0)
         return null;

      if (size == 1)
      {
         /* in this case, back up one level so we support multiple rows.  If
          * we are on the root, then just pass that back.
          */
         String elem = (String)elements.get(0);
         int pos = elem.lastIndexOf('/');
         if (pos != -1)
         {
            elem = elem.substring(0, pos);
         }
         return elem;
      }

      String[] sortedElements = new String[size];
      elements.toArray(sortedElements);
      java.util.Arrays.sort(sortedElements);

      /* at this point we have a sorted array of the element names. This
       * should simplify the logic. We need to see what the lowest element
       * is in the tree which can be used for traversal.
       */

      // first trim down the elements to strip out any attributes
      String baseElement = null;
      boolean baseWasStripped = false;

      for (int i = 0; i < size; i++) {
         String el = sortedElements[i];
         int pos = el.lastIndexOf('@');
         if (pos != -1) {
            el = el.substring(0, pos-1);   // strip / as well
            sortedElements[i] = el;
         }

         // now we can test commonality with our neighbors
         if (i == 0) {   // no comparison to do here
            baseElement = el;
            baseWasStripped = false;
         }
         else {
            if (el.startsWith(baseElement)) {
               // if this was previously stripped, don't rebuild this for
               // children of the same element
               if (!baseWasStripped ||
               (el.lastIndexOf('/') > baseElement.length()))
               {
                  // use the current element as the base now
                  baseElement = el;
                  baseWasStripped = false;
               }
            }
            else {   // get the common denominator between these two
               /* fix for bug id Rx-99-10-0184 - keep the original base
                * element intact and store the new base in a temp var.
                * we can then report the error using the correct base
                * element name rather than null which was being returned
                * by getBaseElement. Of course, on success we must then
                * set baseElement to newBase
                */
               final String newBase = getBaseElement(baseElement, el);
               if (newBase == null)
               {   // there are elements in different trees here!?
                  Object[] args = {
                     getRootNodeName(baseElement),
                     getRootNodeName(el) };
                  PSInvalidXmlException ex = new PSInvalidXmlException(
                     IPSXmlErrors.XML_TWO_ROOT_ELEMENTS, args);
                  throw new RuntimeException(ex.getLocalizedMessage());
               }

               baseElement = newBase;
               baseWasStripped = true;
            }
         }
      }

      return baseElement;
   }

   /**
    * Get the common base of the two supplied xpath strings.
    * 
    * @param currentBase the current base element, an xpath <code>String</code>
    *    that may be <code>null</code> or empty.
    * @param xmlField the xml field element, an xpath <code>String</code> that
    *    may be <code>null</code> or empty.
    * @return the common base element found for the supplied xpath strings,
    *    may be <code>null</code> or empty. If the supplied current base is 
    *    <code>null</code> or empty, the xml field is returned. If the supplied
    *    xml field is <code>null</code> empty, the current base will be 
    *    returned.
    */
   public static String getBaseElement(String currentBase, String xmlField)
   {
      if (currentBase == null || currentBase.trim().length() == 0)
      {
         if (xmlField != null)
         {
            int pos = xmlField.lastIndexOf(XML_ELEMENT_DELIMITER);
            if (pos != -1)
               return xmlField.substring(0, pos);
         }
         
         return xmlField;
      }
      
      if (xmlField == null || xmlField.trim().length() == 0)
         return currentBase;
         
      boolean currentBaseStartsWithDelimiter = 
         currentBase.startsWith(XML_ELEMENT_DELIMITER);
      boolean xmlFieldStartsWithDelimiter = 
         xmlField.startsWith(XML_ELEMENT_DELIMITER);
      if (currentBaseStartsWithDelimiter != xmlFieldStartsWithDelimiter)
         return null;
      
      StringTokenizer currentBaseTokenizer = new StringTokenizer(currentBase, 
         XML_ELEMENT_DELIMITER);
         
      StringTokenizer xmlFieldTokenizer = new StringTokenizer(xmlField, 
         XML_ELEMENT_DELIMITER);
      
      String baseElement = null;
      while (currentBaseTokenizer.hasMoreTokens())
      {
         String currentBaseToken = currentBaseTokenizer.nextToken();
         if (xmlFieldTokenizer.hasMoreTokens())
         {
            String xmlFieldToken = xmlFieldTokenizer.nextToken();
            if (currentBaseToken.equals(xmlFieldToken))
            {
               if (baseElement == null)
                  baseElement = currentBaseToken;
               else
                  baseElement += XML_ELEMENT_DELIMITER + currentBaseToken;
            }
            else
               break;
         }
         else
            break;
      }
      
      if (baseElement != null && currentBaseStartsWithDelimiter)
         baseElement = XML_ELEMENT_DELIMITER + baseElement;
         
      return baseElement;
   }

   /**
    * Get the relative XML field name for the specified field using
    * the specified base. If the fields are unrelated, <code>xmlField</code>
    * will be returned untouched.
    *
    * @param   base            the base to use for the relative field name
    *
    * @param   xmlField         the XML field to be converted
    *
    * @return                  the relative XML field name
    */
   public static String getRelativeFieldName(String base, String xmlField)
   {
      String ret = xmlField;

      if (xmlField.equals(base))
      {
         ret = ".";
      }
      else if (xmlField.startsWith(base))
      {
         int len;
         if (base.endsWith("/"))
            len = base.length();
         else
            len = base.length() + 1;
         ret = xmlField.substring(len);
      }
      else
      {
         String commonBase = getBaseElement(base, xmlField);
         if (commonBase != null)
         {
            /* need to check if the XML field is actually the common
             * base element.
             * This problem was uncovered fixing bug id TGIS-4BUPSU
             */
            int pos = commonBase.length();
            if (pos == xmlField.length())
            {   // the XML field is the same as the base element
               ret = ".";
            }
            else
            {
               ret = xmlField.substring(pos+1);
               do
               {
                  ret = "../" + ret;
               } while ( (pos = base.indexOf('/', pos+1)) != -1);
            }
         }
      }

      return ret;
   }

   /**
    * Get the root node name from the specified XML field.
    *
    * @param   name      the XML field name
    *
    * @return            the root node name or null if name is null or empty
    */
   public static String getRootNodeName(String name)
   {
      if (name == null)
         return null;

      int len = name.length();
      if (len == 0)
         return null;

      if (name.startsWith("/"))
      {
         if (len == 1)
            return null;

         name = name.substring(1);
         len--;
      }

      int pos = name.indexOf('/');
      if (pos == -1)
         return name;

      return name.substring(0, pos);
   }

   private String getDataFromAttribute(Node cur, String name)
   {
      NamedNodeMap map = cur.getAttributes();
      if (map != null) {
         Node attr = map.getNamedItem(name);
         if (attr != null)
            return ((Attr)attr).getValue();
      }

      return null;
   }

   private String getDataFromChildElement(Node cur, String name)
   {
      String ret = null;

      // this is stored at the top of the doc (off the root)
      for (cur = cur.getFirstChild();
      cur != null;
      cur = cur.getNextSibling())
      {
         if ( (ret = getNodeMatchValue(cur, name)) != null)
            break;
      }

      return ret;
   }

   private String getNodeMatchValue(Node node, String name)
   {
      String nodeName = isQualifiedName(name) ?
         node.getNodeName() :
         getUnqualifiedNodeName(node);

      if ((nodeName != null) && name.equals(nodeName)) {
         return getElementData((Element)node);
      }

      return null;
   }

   /**
    * A private utility method to convert special characters to entity
    * references.
    *
    * The characters will be converted according to this scheme:
    * <TABLE BORDER=1>
    * <TR>
    *   <TD>&amp;</TD>
    * <TD>&amp;amp;</TD>
    * </TR>
    * <TR>
    *   <TD>&lt;</TD>
    * <TD>&amp;lt;</TD>
    * </TR>
    * <TR>
    *   <TD>&gt;</TD>
    * <TD>&amp;gt;</TD>
    * </TR>
    * <TR>
    *   <TD>&apos;</TD>
    * <TD>&amp;apos;</TD>
    * </TR>
    * <TR>
    *   <TD>&quot;</TD>
    * <TD>&amp;quot;</TD>
    * </TR>
    * </TABLE>
    *
    * @param   input
    *
    * @throws IOException if I/O error occurs.
    */
   public static void convertToXmlEntities(String input, Writer out)
      throws IOException
   {
      /* This implementation should be fairly efficient in that
       * it minimizes the number of function calls. Hence, we
       * operate on an array rather than a String object. We
       * collect the output in a string buffer, and here too
       * we try to minimize the number of function calls.
       *
       * We do not add each normal character to the output
       * buffer one at a time. Instead, we build a "run" of
       * normal characters and, upon encountering a special
       * character, we write the previous normal run before
       * we write the special replacement.
       */
      char[] chars = input.toCharArray();
      int len = chars.length;

      char c;

      // the start of the latest run of normal characters
      int startNormal = 0;

      int i = 0;
      while (true)
      {
         if (i == len)
         {
            if (startNormal != i)
               out.write(chars, startNormal, i - startNormal);
            break;
         }
         c = chars[i];
         switch (c)
         {
            case '&' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&amp;");
               break;
            case '<' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&lt;");
               break;
            case '>' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&gt;");
               break;
            case '\'' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&#39;");
               break;
            case '"' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&quot;");
               break;
            default:
            {
               int num = c;
               if (num > 126) // this is some kind of funky character
               {
                  if (startNormal != i)
                     out.write(chars, startNormal, i - startNormal);
                  startNormal = i + 1;
                  out.write("&#");
                  out.write(Integer.toString(num));
                  out.write(";");
                  break;
               }
               //else do nothing...this char becomes part of the normal run
            }
         }
         i++;
      }
   }

   /**
    * Just like {@link #convertToXmlEntities(String, Writer)}, except this
    * suppress the IOException.
    */
   public static void convertToXmlEntities(String input, PrintWriter out)
   {
      Writer writer = (Writer) out;
      try
      {
         convertToXmlEntities(input, writer);
      }
      catch (IOException ioe) // ignore
      {
      }
   }
   
   /**
    * A private utility method to convert special characters to entity
    * references.
    *
    * The characters will be converted according to this scheme:
    * <TABLE BORDER=1>
    * <TR>
    *   <TD>&amp;</TD>
    * <TD>&amp;amp;</TD>
    * </TR>
    * <TR>
    *   <TD>&lt;</TD>
    * <TD>&amp;lt;</TD>
    * </TR>
    * <TR>
    *   <TD>&gt;</TD>
    * <TD>&amp;gt;</TD>
    * </TR>
    * <TR>
    *   <TD>&apos;</TD>
    * <TD>&amp;apos;</TD>
    * </TR>
    * <TR>
    *   <TD>&quot;</TD>
    * <TD>&amp;quot;</TD>
    * </TR>
    * </TABLE>
    *
    * @param   input
    *
    * @return   String A new String with all special characters transformed
    * into their entities.
    */
   public static String convertToXmlEntities(String input)
   {
      java.io.StringWriter writer = new java.io.StringWriter(
      (int)(input.length() * 1.5));
      convertToXmlEntities(input, new PrintWriter(writer));
      return writer.toString();

   }

   private org.w3c.dom.Document    m_doc;
   private org.w3c.dom.Node      m_root;
   private org.w3c.dom.Node      m_cur;

   /**
    * Sets the boolean indicating if the special characters in the Xml Document
    * should be transformed into the corresponding entities.
    * @param convertXmlEntities <code>true</code> if the special characters in
    * the Xml Document should be transformed into the corresponding entities,
    * <code>false</code> otherwise.
    */
   public void setConvertXmlEntities(boolean convertXmlEntities)
   {
      m_convertXmlEntities = convertXmlEntities;
   }

   /**
    * Returns the boolean indicating if the special characters in the Xml Document
    * should be transformed into the corresponding entities.
    * @return <code>true</code> if the special characters in
    * the Xml Document should be transformed into the corresponding entities,
    * <code>false</code> otherwise.
    */
   public boolean getConvertXmlEntities()
   {
      return m_convertXmlEntities;
   }

   /**
    * <code>true</code> if the special characters in the Xml Document should be
    * transformed into the corresponding entities, <code>false</code> otherwise.
    */
   private boolean m_convertXmlEntities = false;

   /**
    * Main method - used for tesing.
    * Usage is :
    * -i (INPUT_XML_FILE_PATH)
    * -o (OUTPUT_XML_FILE_PATH)
    */
   public static void main(String argv[])
   {
      if (argv.length != 4)
      {
         System.out.println("Invalid number of arguments.");
         printUsage();
         System.exit(1);
      }
      if (!argv[0].equalsIgnoreCase("-i"))
      {
         System.out.println("Invalid first argument.");
         printUsage();
         System.exit(1);
      }
      String inputFilePath = argv[1];
      File inputFile = new File(inputFilePath);
      if (!inputFile.isFile())
      {
         System.out.println("Invalid input file path: " + inputFilePath);
         System.exit(1);
      }
      if (!argv[2].equalsIgnoreCase("-o"))
      {
         System.out.println("Invalid third argument.");
         printUsage();
         System.exit(1);
      }
      String outputFilePath = argv[3];
      File outputFile = new File(outputFilePath);
      File parentFile = outputFile.getParentFile();
      if (parentFile != null)
      {
         if (!parentFile.isDirectory())
            parentFile.mkdirs();
      }
      try
      {
         InputSource in = new InputSource(new FileInputStream(inputFile));
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
         walker.setConvertXmlEntities(true);
         walker.write(new FileOutputStream(outputFile));
      }
      catch (Throwable t)
      {
         System.out.println(t.getMessage());
         t.printStackTrace();
      }
   }

   /**
    * Prints the usage of this class.
    */
   public static void printUsage()
   {
      System.out.println("Usage is : ");
      System.out.println("-i [INPUT_XML_FILE_PATH] -o [OUTPUT_XML_FILE_PATH]");
   }
   
   /**
    * Static boolean indicates if saxon is available to avoid throwing
    * an exception in write. Initialized in static block, never modified
    * afterward.
    */
   private static boolean ms_saxon_loaded;

   /**
    * The transformer factory used to create 
    * {@link javax.xml.transform.Transformer}. It is initialized when the 
    * current class is loaded. Only one instance of the transformer is needed 
    * per JVM, but need to be used in a sychronized block since it is not 
    * thread safe.
    */
   private static TransformerFactory ms_transformerFactory = TransformerFactory
         .newInstance();

   static
   {
      try
      {
         Class x = Thread.currentThread().getContextClassLoader().loadClass("com.icl.saxon.Controller");
         ms_saxon_loaded = x != null;
      }
      catch(LinkageError | ClassNotFoundException e)
      {
         ms_saxon_loaded = false;
      }
   }

}
