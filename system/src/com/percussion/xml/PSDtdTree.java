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

import com.percussion.design.catalog.PSCatalogException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServer;
import com.percussion.util.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.impl.dtd.XMLContentSpec;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The PSDtdTree class is used to contain
 * a DTD tree definition.
 *
 * @see   PSDtdElementEntry
 * @see   PSDtdNodeList
 * @see   PSDtdNode
 *
 * @author   David Gennaco
 * @version   1.0
 * @since   1.0
 */
public class PSDtdTree implements Serializable, PSDtdTreeVisitor, Cloneable
{

    private static final Logger log = LogManager.getLogger(PSDtdTree.class);
   /**
    * The character, represented as a string, that is used to separate path
    * components in a canonicalized path name. For example, /Person/Name/First.
   **/
   public static final String CANONICAL_PATH_SEP = "/";

   /**
    * This is the default prefix prepended to attribute names when creating a
    * path for it. For example, the following DTD fragment:<p/>
    * &lt;!ELEMENT First (#PCDATA) &gt;<p/>
    * &lt;!ATTLIST First isNickname ("yes" | "no") &gt;<p/>
    * would result in: /Person/Name/First/@isNickname <p/>
    * for the attribute.
   **/
   public static final String CANONICAL_ATTRIBUTE_PREFIX = "@";


   public static void main(String[] args)
   {
      if (args.length < 1)
      {
         log.info("No args");
         return;
      }

      PSDtdTree tree = null;

      try
      {
         String type = args[0];
         if (type.equalsIgnoreCase("stream"))
         {
            if (args.length < 3)
            {
               log.info("docType and filename required");
               return;
            }
            String docType = args[1];
            log.info("docType : {} ", docType);
            java.io.File file = new java.io.File(args[2]);
            InputStream in =
               new java.io.BufferedInputStream(
                  new java.io.FileInputStream(file));
            tree = new PSDtdTree(in, docType);
         }
         else if (type.equalsIgnoreCase("url"))
         {
            if (args.length < 2)
            {
               log.info("URL required.");
               return;
            }
            URL url = new URL(args[1]);
            tree = new PSDtdTree(url);
         }
         else
         {
            log.info("Unknown type: {} ", type);
         }
      /////////////// test code //////////////////////////
// test using: java -Djava.compiler=none com/percussion/xml/PSDtdTree stream Person Person.dtd
        log.info("  BEFORE: {} ", tree);
        tree.printElements();
        tree = (PSDtdTree)tree.clone();
      ////////////////////////////////////////////////////
            List cat = tree.getCatalog("/", "@");
         for (java.util.Iterator i = cat.iterator(); i.hasNext(); )
         {
            log.info(i.next().toString());
         }
      log.info("  AFTER: {} ", tree);
      }
      catch (Throwable t)
      {
          log.error(t.getMessage());
          log.debug(t.getMessage(), t);
      }
   }

  /**
    * Sets root to another name and value. Essentially changing the root
   * completely.
   */
  public void setRootName( String rootName)
  {
    String oldName = rootElement.getElement().getName();
    this.rootElement.getElement().setName( rootName );
    this.m_elements.put( rootName, m_elements.remove( oldName ) );
  }

   /**
    *
    */
   void addElement( String key, PSDtdElement value )
   {
      m_elements.put( key, value );
   }

  /**
   * Uses serialization to perform a deep copy of this object.
   */
  public Object clone() throws CloneNotSupportedException
  {
    Object clone = null;

    try {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream( 1000 );
      ObjectOutputStream objOutStream = new ObjectOutputStream( outStream );
      objOutStream.writeObject( this );
      objOutStream.close();
      byte[] data = outStream.toByteArray();

      ByteArrayInputStream inStream = new ByteArrayInputStream( data );
      ObjectInputStream objInStream = new ObjectInputStream( inStream );
      clone = objInStream.readObject();

      objInStream.close();
    }
    catch ( Exception e )
    {
      PSConsole.printMsg("Xml", e);
    }

    return clone;
  }

   /**
    * @author   DVG
    *
    * @version 1.0
    *
    * Constructs a DTD tree by parsing the DTD located by the given
    * URL. The URL must refer to valid content.
    *
    * @param   dtdURL   The URL location of the DTD to parse.
    *
    * @throws   PSCatalogException
    *
    */
   public PSDtdTree(URL dtdURL)
      throws PSCatalogException
   {
      m_dirty=false;

      if (dtdURL == null)
      {
         rootElement = null;
         m_elements = null;
         return;
      }

      InputStream in = null;
      String charSet = null;
      try
      {
         String prot = dtdURL.getProtocol().toLowerCase();
         if (prot.equals("file"))
         {
            String realPath = dtdURL.getFile();
            File file = new File(realPath);
            if (!(realPath.startsWith("/") || file.isAbsolute()))
            {
               realPath = PSServer.getRxFile(realPath);
            }
            in = new BufferedInputStream(new FileInputStream(realPath));
            charSet = PSCharSets.rxStdEnc();
         }
         else
         {
            // open the URL and get the content and its character set
            URLConnection conn = dtdURL.openConnection();

            String contentType = conn.getHeaderField("Content-Type");
            if (contentType != null)
            {
               try
               {
                  HashMap contentParams = new HashMap();
                  PSBaseHttpUtils.parseContentType(contentType, contentParams);
                  charSet = (String)contentParams.get("charset");
               }
               catch (IllegalArgumentException e)
               {
                  // ignore invalid content-type, we will default
               }
            }

            if (charSet == null)
               charSet = "ISO-8859-1"; // the default according to HTTP
         }

         parseDtd(in, dtdURL, charSet);
      }
      catch (IOException e)
      {
         throw new PSCatalogException(IPSXmlErrors.DTD_IO_ERROR, e.getMessage());
      }
      finally
      {
         if (in != null)
            try { in.close(); } catch (IOException e) { /* ignore */ }
      }
   }

   /**
    * @author   chadloder
    *
    * @version 1.9 1999/06/28
    *
    * Constructor from an InputStream and a DocType.
    *
    * @param   stream   An input stream positioned at the beginning of
    * a valid XML DTD.
    *
    * @param   docType   A string representing the doc type of the DTD,
    * which will only be used if the doc type is not present in the
    * document. Use <CODE>null</CODE> if you are sure that the document
    * has a doc type or if you don't want the operation to succeed if
    * there is no doc type present. A safe value to use for this param
    * is the base name of a file (for example, if the file is "Play.dtd",
    * try using "Play").
    *
    * @throws   PSCatalogException
    *
    */
   public PSDtdTree(InputStream stream, String docType, String encoding)
      throws PSCatalogException
   {
      m_dirty=false;
      parseDtd(stream, docType, encoding);
   }


   /**
    * @author   chadloder
    *
    * @version 1.4 1999/05/28
    *                                                                                                     /*
    * Constructor from an InputStream (using default encoding for the
    * platform).
    *
    * @param   stream   An input stream positioned at the beginning of
    * a valid XML DTD.
    *
    * @param   docType The document type.
    *
    * @throws   PSCatalogException
    *
    */
   public PSDtdTree(InputStream stream, String docType)
      throws PSCatalogException
   {
      this(stream, docType, PSCharSets.rxJavaEnc());
   }


   /**
    * Create a tree from the passed on document.
    *
    * @param doc The document containing the parsed DTD.
    * May not be <code>null</code>.
    *
    * @throws PSCatalogException if any error occurs parsing the DTD.
   **/
   public PSDtdTree(PSDtd doc)
         throws PSCatalogException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      parseDtd(doc);
   }

   /**
    * Constructor from a parser that has already parsed a valid XML document.
    * @param parser A parser that has already parsed the DTD.
    * May not be <code>null</code>.
    * @throws PSCatalogException if any error occurs parsing the DTD.
    */
   public PSDtdTree(PSDtdParser parser)
      throws PSCatalogException
   {
      if (parser == null)
         throw new IllegalArgumentException("parser may not be null");
      m_dirty=false;
      parseDtd(parser);
   }

   /**
    * Private utility method to parse a DTD given an InputStream.
    *
    * @param   stream   The input stream, positioned at the beginning
    * of a valid XML DTD.  Assumed not <code>null</code>.
    * @param   docType   The docType name. May be <code>null</code> or empty.
    * @param   encoding   The character encoding. May be <code>null</code> or empty.
    * @throws   PSCatalogException if any error occurs parsing the DTD.
    */
   private void parseDtd(InputStream stream, String docType, String encoding)
      throws PSCatalogException
   {
      m_dirty=false;

      if (stream.markSupported())
      {
         stream.mark(0); // mark beginning of DTD
      }

      XMLInputSource in = new XMLInputSource(null, null, null,
         new NoCloseInputStream(stream),
         /*PSCharSets.getStdName(encoding)*/ null);

      PSDtdParser p = new PSDtdParser();
      PSDtd myDTD = null;

      try
      {
         p.parseDtd(in);
         myDTD = p.getDtd();
         if (myDTD == null)
         {
            rootElement = null;
            m_elements = null;
         }
      }
      catch (Exception e)
      {
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
         throw new PSCatalogException(
            IPSServerErrors.XML_PARSER_SAX_ERROR,
            e.toString());
      }
      parseDtd(p);
   }

   /**
    * Private utility method to parse a DTD given an InputStream.
    * @param   stream   The input stream, positioned at the beginning
    * of a valid XML DTD.
    * @param url The URL to use for doctype. May be <code>null</code>.
    * @param encoding The character encoding. May be <code>null</code> or empty.
    * @throws PSCatalogException if any error occurs parsing the DTD.
    */
   private void parseDtd(InputStream stream, URL url, String encoding)
      throws PSCatalogException
   {
      m_dirty=false;

      XMLInputSource in = null;

      if (url != null)
         in = new XMLInputSource(null, url.toString(), null, stream, null);
      else
         in = new XMLInputSource(null, null, null, stream, null);

      PSDtdParser p = new PSDtdParser();
      PSDtd myDTD = null;

      try
      {
         p.parseDtd(in);
         myDTD = p.getDtd();
         if (myDTD == null) {
            rootElement = null;
            m_elements = null;
         }
      }
      catch (Exception e)
      {
            throw new PSCatalogException(
               IPSServerErrors.XML_PARSER_SAX_ERROR,
               e.toString());
      }
      parseDtd(p);
   }

   /**
    * Internal utility method to construct a DTD tree from a parser
    * that has already loaded and parsed a document.
    * @param p A parser that has parsed a DTD. Assumed not <code>null</code>
    * @throws PSCatalogException if any error occurs parsing the DTD.
    */
   private void parseDtd(PSDtdParser p)
      throws PSCatalogException
   {
      m_dirty=false;
      parseDtd(p.getDtd());
   }

   /**
    * Internal utility method to construct a DTD tree from a document.
    * @param p   A document that contains the DTD. Assumed not <code>null</code>
    * @throws PSCatalogException if any error occurs parsing the DTD.
    */
   private void parseDtd(PSDtd myDTD)
      throws PSCatalogException
   {
      m_dirty=false;
      if (myDTD == null)
      {
         rootElement = null;
         m_elements = null;
         return;
      }

      // Get the root element for this DTD
      PSXmlElementDecl rootElementDecl = myDTD.getElementDeclaration(
         myDTD.getName());

      if (rootElementDecl == null)
      {
         throw new PSCatalogException(
            IPSXmlErrors.DTD_ROOTNOTFOUND_ERROR,
            myDTD.getName());
      }

      m_elements = new HashMap();

      CMNode cm = myDTD.getContentModelNode(myDTD.getName());
      rootElement = (PSDtdElementEntry)processElementDecl(
         myDTD,
         rootElementDecl,
         cm,
         null,                                  // no parent
         PSDtdNode.OCCURS_ONCE);                // root always occurs once

   }

    /**
      * Processes an element declaration, creating a new DTD element if not
      * already defined.
      *
      * @param    dtd   The DTD. Assumed not <code>null</code>.
      * @param    elementDecl   The element declaration to process. Assumed not
      * <code>null</code>.
      * @param    parent   The element's parent, or <code>null</code> if the
      * element is root.
      * @param    occurrence   The occurrence type of the element within the parent.
      * @return  PSDtdNode   An element entry for this element.
      * Never <code>null</code>.
      * @throws  PSCatalogException if any error occurs parsing the DTD.
      */
   private PSDtdNode processElementDecl(
         PSDtd dtd,
         PSXmlElementDecl elementDecl,
         CMNode cm,
         PSDtdNode parent,
         int occurrence   )
      throws PSCatalogException
   {
      PSDtdElement element = (PSDtdElement) m_elements.get(elementDecl.getName());

      if (element == null)
      {
         element = new PSDtdElement(elementDecl.getName());
      }
      else
      {
         // pass - Content Model has already been processed, return element
         return new PSDtdElementEntry(element, parent, occurrence);
      }

      // Need to process attributes here
      element.setAttributes(dtd);

      // New element, add to HashMap and process content model
      m_elements.put(element.getName(), element);

      PSDtdElementEntry entry = new PSDtdElementEntry(element, parent,
         occurrence);

      switch (elementDecl.getContentType())
      {
      case PSXmlElementDecl.TYPE_ANY:
         /* Note - this denotes any element, so some day we may need to
            make sure to process all elements in the DTD for content models later
          */
         element.setContent(null, true);
         break;

      case PSXmlElementDecl.TYPE_EMPTY:
         element.setContent(null, false);
         break;

      case PSXmlElementDecl.TYPE_MIXED:
         element.setContent(new PSDtdDataElement(), false);
         break;

      default:
         // ElementDecl.MODEL_GROUP:
         element.setContent(getContent(dtd, cm, entry, null,
            PSDtdNode.OCCURS_ONCE), false);
      }

      return entry;
   }

   /**
    * Processes the content model, adding it to the element and
    * linking to the parent if necessary.
    *
    * @param    dtd   The DTD. Assumed not <code>null</code>.
    * @param    cm   The content model. Assumed not <code>null</code>.
    * @param    parent   The element's parent, or <code>null</code> if the
    * element is root.
    * @param    current   Used to process NodeLists. Assumed not <code>null</code>.
    * @param    occurrenceType The occurrence type of this content model
    * in this parent.
    * @return  PSDtdNode. May be <code>null</code>.
    * @throws  PSCatalogException if any error occurs parsing the DTD.
    */
   private PSDtdNode getContent(
         PSDtd dtd,
         CMNode cm,
         PSDtdNode parent,
         PSDtdNodeList current,
         int occurrenceType   )
      throws PSCatalogException
   {
      if (cm instanceof PSCM1op)
      {
      /* throw something if occurrenceType is something other
       than PSDtdNode.OCCURS_ONCE, as we do not support this currently... */
         if (occurrenceType != PSDtdNode.OCCURS_ONCE)
         {
            char[] ch1 = {PSDtdNode.getOccurrenceCharacter(occurrenceType)};
            char[] ch2 = {(char) ((PSCM1op) cm).getType()};
            String s1 = new String ( ch1 );
            String s2 = new String ( ch2 );
            Object[] args = { s1, s2 };
            throw(new PSCatalogException(
               IPSXmlErrors.DTD_MULTIPLE_OCCURRENCE_NOTSUPPORTED_ERROR, args));
         }

         int occurrenceSetType;
         switch (((PSCM1op) cm).getType())
         {
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE:
               occurrenceSetType = PSDtdNode.OCCURS_ANY;
               break;

            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE:
               occurrenceSetType = PSDtdNode.OCCURS_ATLEASTONCE;
               break;

            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE:
               occurrenceSetType = PSDtdNode.OCCURS_OPTIONAL;
               break;

            default:
               occurrenceSetType = PSDtdNode.OCCURS_ONCE;
               break;
         }

         /* We are only a pseudo-node process a real node type,
          with this as the occurrence setting */
         return getContent(dtd, ((PSCM1op) cm).getNode(), parent, null,
            occurrenceSetType);
      }
      else if (cm instanceof PSCM2op)
      {
         // Get the type and build the appropriate PS type
         if (current == null)
         {
            current = new PSDtdNodeList(((PSCM2op) cm).getType(),
               occurrenceType);
            current.setParent(parent);
         }

         CMNode leftNode = ((PSCM2op) cm).getLeft();
         CMNode rightNode = ((PSCM2op) cm).getRight();

         if (leftNode == null) {
            current = null;
            return null;
         }

         if ((leftNode instanceof PSCM2op) &&
            (((PSCM2op) leftNode).getType() == current.getType()))
         {
            getContent(dtd, leftNode, parent, current, occurrenceType);
         } else {
            current.add(getContent(dtd, leftNode, current, current,
               PSDtdNode.OCCURS_ONCE));
         }

         if (rightNode == null)
         {
            return current;
         } else if ((rightNode instanceof PSCM2op) &&
            (((PSCM2op) rightNode).getType() == current.getType()))
         {
            getContent(dtd, rightNode, parent, current, occurrenceType);
         } else {
            current.add(getContent(dtd, rightNode, current, current,
               PSDtdNode.OCCURS_ONCE));
         }

         return current;
      }
      else if (cm instanceof PSCMLeaf)
      {
         String leafName = ((PSCMLeaf) cm).getName();
         if (!leafName.equalsIgnoreCase("#PCDATA"))
         {
            PSXmlElementDecl elementDecl = dtd.getElementDeclaration(leafName);
            if (elementDecl == null)
            {
               throw (new PSCatalogException(
                  IPSXmlErrors.DTD_ELEMENT_NOTFOUND_ERROR, leafName));
            }
            // This is an element of TYPE_CHILDREN, but for the parent node's
            // Content Model it was a LEAF node.
            return processElementDecl(dtd, elementDecl,
               dtd.getContentModelNode(leafName),
               parent, occurrenceType);
         }
         else
         {
            // Return Data element - this is the end of a branch
            return ms_dataElement;
         }
      }
      return null;
   }

   /**
    *  Returns the root element entry of this DTD tree.
    *
    */
   public PSDtdElementEntry getRoot()
   {
      return rootElement;
   }

   /**
    *  Returns an element definition in this DTD tree based on the key
    *
    */
   public PSDtdElement getElement(Object key)
   {
      if (m_elements == null)
         return null;
      else
         return (PSDtdElement) m_elements.get(key);
   }


   /**
    * Finds the element entry object in the tree that matches the supplied name,
    * if there is one, and returns it. canonicalName is of the form 'Person/Name/First'
    * and is the form returned by getCatalog().
    *
    * @param canonicalName The fully qualified name of the entry to find.
    *
    * @return The entry by the supplied name or null if there isn't one.
    *
    * @see #getCatalog(String, String)
   **/
   public PSDtdElementEntry getEntryForName( String canonicalName )
   {
      String [] name = canonicalToArray( canonicalName );

      int namePart = 0;
      // check if this name starts at the root
      PSDtdNode node = rootElement;
      PSDtdElementEntry entry = null;
      ElementFinder finder = new ElementFinder();
      while ( node != null && namePart < name.length )
      {
         entry = (PSDtdElementEntry) node.acceptVisitor( finder, name[namePart] );
         if ( null != entry && namePart < name.length )
         {
            node = entry.getElement().getContent();
         }
         ++namePart;
      }
      PSDtdElementEntry result = null;
      if ( null != entry && entry.getElement().getName().equals( name[name.length-1]))
         result = entry;
      return result;
   }


   /**
    * This class adds new functionality to the PSDtdNode hierarchy. Each member
    * finds an element entry node that matches the supplied name. The rules for
    * finding a match are defined for each type below.
   **/
   private class ElementFinder implements PSDtdTreeVisitor
   {
      /**
       * If the name of the element associated w/ the supplied entry matches
       * the supplied name, the entry is returned, else null is returned.
       *
       * @param entry A valid element entry.
       *
       * @param name A valid String object that is the name of the element to find.
       *
       * @return The supplied entry if the name matches, else null.
      **/
      public Object visit( PSDtdElementEntry entry, Object name )
      {
         String entryName = (String) name;
         boolean match = entry.getElement().getName().equals( entryName );
         return match ? entry : null;
      }


      /**
       * This method scans all the children of the supplied list
       * looking for a match. If any child is a list itself, this method calls
       * itself w/ the new list.
       *
       *
       * @param list A valid node list.
       *
       * @param name A valid String object that is the name of the element to find.
       *
       * @return The entry that matches the supplied name, else null.
      **/
      public Object visit( PSDtdNodeList list, Object name )
      {
         PSDtdElementEntry result = null;
         int nodeCt = list.getNumberOfNodes();
         boolean bDone = false;
         for ( int i = 0; i < nodeCt && !bDone; ++i )
         {
            PSDtdNode node = list.getNode(i);
            result = (PSDtdElementEntry) node.acceptVisitor( this, name );
            if ( null != result )
               bDone = true;
         }
         return result;
      }

      /**
       * @return We're not interested in pure nodes, so null is always returned.
      **/
      public Object visit( PSDtdNode node, Object name )
      {
         return null;
      }

      /**
       * @return Since a data element doesn't have a name, null is always returned
      **/
      public Object visit( PSDtdDataElement data, Object name )
      {
         return null;
      }
   }

   /**
    * Parses the supplied name into its pieces, putting each piece into an
    * array. A name of the form "/Person/Name/First" (where "/" is the
    * canonical path separator) would be returned in the array as
    * a[0]="Person", a[1]="Name", a[2]="First".
    * <P>
    * The leading separator is optional. A 0-length array will be returned
    * if:
    * <UL>
    * <LI><CODE>canonicalName</CODE> specifies only a leading separator
    * ("/")
    * <LI><CODE>canonicalName</CODE> specifies only whitespace ("    ")
    * <LI><CODE>canonicalName</CODE> specifies only separators and whitespace
    * ("/ / /// / /")
    * <LI><CODE>canonicalName</CODE> specifies no characters at all ("")
    * </UL>
    * <P>
    * Extra separators will be ignored, and empty path components
    * are <B>not</B> be added to the canonical array. Therefore, a name of
    * "First//Second/Third//Fourth" will parse to a[0] = "First",
    * a[1] = "Second", a[2] = "Third", a[3] = "Fourth".
    * <P>
    * Extra whitespace before and after individual path components
    * will be silently stripped.
    *
    * @param canonicalName The path name of some element entry in a tree.
    * The expected seperater is CANONICAL_PATH_SEP. Must not be
    * <CODE>null</CODE>.
    *
    * @return The path components of the supplied name in an array. If the
    * array has length > 0, then the first element contains the path component
    * highest in the hierarchy. Never <CODE>null</CODE>. Every element in the
    * array will be a non-null String with length > 0.
    */
   public static String [] canonicalToArray( String canonicalName )
   {
      if (canonicalName == null)
         throw new IllegalArgumentException("canonicalName must not be null");

      StringTokenizer toker
         = new StringTokenizer(canonicalName, CANONICAL_PATH_SEP);

      List strings = new ArrayList(5); // average name has 5 components
      while (toker.hasMoreTokens())
      {
         String tok = toker.nextToken().trim();
         if (tok.length() > 0)
            strings.add( tok );
      }

      Object[] array = strings.toArray();
      String[] strArray = new String[array.length];
      java.lang.System.arraycopy( array, 0, strArray, 0, array.length );

      return strArray;
   }

   /**
    * Checks the supplied path and returns <code>true</code> if it represents
    * an attribute.
    *
    * @param path A fully qualified or relative path to an element/attribute.
    * The path must use the canonical path seperater.
    *
    * @param attributePrefix The (typically) character that identifies an
    * attribute. If null, the canonical prefix will be used.
    *
    * @return <code>true</code> if the supplied path is the path to an element
    * attribute, <code>false</code> otherwise.
   **/
   public static boolean isAttributePath( String path, String attributePrefix )
   {
      if ( null == attributePrefix )
         attributePrefix = CANONICAL_ATTRIBUTE_PREFIX;

      int start = path.lastIndexOf( CANONICAL_PATH_SEP );
      if ( start < 0 )
         start = 0;
      String test = path.substring( start );
      return test.startsWith( CANONICAL_ATTRIBUTE_PREFIX );
   }

   /**
    * Wraps a bare DTD in a valid XML document and sends it to the parser.
    * Some DTDs are not contained in an XML document ("bare"), and the parser
    * chokes on them. This method wraps the DTD in a small XML document and
    * then tries to parse it.
    *
    * @param   p   The DTD parser to use.
    * @param   docType The doc type.
    * @param   encoding   The character encoding
    * @throws   SAXException
    * @throws   SAXParseException
    * @throws   java.io.FileNotFoundException
    * @throws   java.io.IOException
    *
    */
   private void parseInXmlDocument(PSDtdParser p,
      InputStream stream,
      String docType,
      String encoding)
      throws SAXException,
            SAXParseException,
            FileNotFoundException,
            IOException,
            PSCatalogException
   {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      String javaEnc = PSCharSets.getJavaName(encoding);

      // first look for the "<?xml..." header in the stream so we know
      // whether we should write it or not
      // read one line from the stream
      // if it's the header, then we don't need to write it at all
      // if it's not the header, then we should write it before the
      // beginning of the stream
      stream.reset();
      PSInputStreamReader rdr = new PSInputStreamReader(stream, true);
      String firstLine = rdr.readLine(javaEnc);
      if (firstLine != null && firstLine.trim().startsWith("<?"))
      {
         bout.write(firstLine.getBytes(javaEnc));
         bout.write(("\r\n").getBytes(javaEnc));
         // we consumed the line then wrote it ourselves
      }
      else
      {
         stream.reset();

         bout.write( ("<?xml version='1.0' encoding='"
            + PSCharSets.getStdName(encoding) + "'?>\r\n").getBytes(javaEnc));
      }

      bout.write( ("<!DOCTYPE " + docType + " [\r\n").getBytes(javaEnc));

      // write the DTD stream into the new document
      int bytesRead = 0;
      byte[] bytes = new byte[2048];
      while (true)
      {
         bytesRead = stream.read(bytes);
         if (bytesRead < 1)
            break;
         bout.write(bytes, 0, bytesRead);
      }

      bout.write( ("\r\n]>").getBytes(javaEnc));

      parseByteArray(bout, p, encoding);
   }

   /**
    * Wraps a bare DTD in a valid XML document and sends it to the parser.
    * Some DTDs are not contained in an XML document ("bare"), and the parser
    * chokes on them. This method wraps the DTD in a small XML document and
    * then tries to parse it.
    *
    * @param   p   The DTD parser to use. Assumed not <code>null</code>.
    * @param   url   The URL of the document. Content must be accessible.
    * @param   encoding   The character encoding. May be <code>null</code> or empty.
    *
    * @throws   SAXException
    * @throws   SAXParseException
    * @throws   java.io.FileNotFoundException
    * @throws   java.io.IOException
    *
    */
   private void parseInXmlDocument(PSDtdParser p, URL url, String encoding)
      throws
         SAXException,
         SAXParseException,
         FileNotFoundException,
         IOException,
         PSCatalogException
   {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      String javaEnc = PSCharSets.getJavaName(encoding);
      String dtdName = "dtdName";
      dtdName = url.getFile();
      int pos, pos2;

      if (dtdName == null || dtdName.length() == 0) {
         dtdName = url.getHost();
      }

      if ((pos = dtdName.lastIndexOf('.')) != -1) {
         dtdName = dtdName.substring(0, pos);
      }

      pos = dtdName.lastIndexOf('/');
      pos2 = dtdName.lastIndexOf('\\');
      if (!(pos == -1 && pos2 == -1)) {
         dtdName = dtdName.substring(Math.max(pos, pos2) + 1);
      }

      bout.write(("<!DOCTYPE " + dtdName + " SYSTEM \"" + url.toExternalForm()
         + "\">\r\n").getBytes(javaEnc));

      bout.flush();

      parseByteArray(bout, p, encoding);
   }

   /**
    *
    * @param bout the byte stream to parse. Assumed not <code>null</code>.
    * @param   p   The DTD parser to use. Assumed not <code>null</code>.
    * @param   encoding   The character encoding. May be <code>null</code> or empty.
    * @throws SAXException
    * @throws SAXParseException
    * @throws FileNotFoundException
    * @throws IOException
    * @throws PSCatalogException
    */
   private void parseByteArray(ByteArrayOutputStream bout, PSDtdParser p, String encoding)
      throws
         SAXException,
         SAXParseException,
         FileNotFoundException,
         IOException,
         PSCatalogException
   {
      byte[] ba = bout.toByteArray();

      XMLInputSource in = new XMLInputSource(null, null, null,
         new ByteArrayInputStream(ba),
         PSCharSets.getStdName(encoding));

      p.parseDtd(in);
   }

    /**
     * Used for debugging. Prints a flat list of element names stored
     * in this DTD tree.
     */
   public void printElements()
   {
      if (m_elements == null) {
         log.info("Null Tree");
         return;
      }

      Iterator i = m_elements.values().iterator();

      PSDtdElement e;
      while (i.hasNext()) {
         e = (PSDtdElement) i.next();
         log.info(e.getName());
      }
   }



   /**
    *
    * Generates a list of Strings starting at the root
    * node in a top-down and left-right fashion.   The order of expansion
    * will be element followed by the element's attributes followed by
    * the element's content.  <p>When a recursive state is detected, the
    * name of the first element detected to be recursive will be added
    * to the catalog with the value <#RECURSION> appended to it.  No
    * further catalog of this leg of the tree will be attempted.<p>   The
    * catalog functionality of the DTD classes contains an upper limit
    * of <code>MAX_CATALOG_SIZE</code> as defined in this class and when
    * this limit is reached the value "TRUNCATED!" will be appended to the
    * list and no further processing will be attempted.<p>
    *
    * @param   separator   the element separator string, if null, the canonical
    * seperater will be used.
    *
    * @param   attribQualifier   the string used to identify an attribute entry. If
    * null, the canonical identifier will be used.
    *
    * @return List A list of strings containing the catalog info.
    *
    */
   public List getCatalog(String separator, String attribQualifier)
   {

      if (m_catalogList != null) {
         return m_catalogList;
      }

      // Start at the root!
      if (rootElement == null)
      {
         return null;
      }
      else
      {
         m_catalogList = new ArrayList();

         if (separator == null)
            separator = CANONICAL_PATH_SEP;

         if (attribQualifier == null)
            attribQualifier = CANONICAL_ATTRIBUTE_PREFIX;

         // Use a HashMap for a stack for recursion checks
         HashMap catStack = new HashMap();
         rootElement.catalog(catStack, m_catalogList, "", separator, attribQualifier);
      }

      return m_catalogList;
   }

   /**
    * @author   DVG
    *
    * @version 1.0
    *
    * Gets the occurrence setting of the given element within the given parent.
    * For example, if a <B>BookList</B> element can contain one or more
    * <B>Book</B> elements, calling this method with ("Book", "BookList") will
    * return <CODE>PSDtdNode.OCCURS_ATLEASTONCE</CODE>.
    *
    * @param   elementName   The element name
    * @param   parentName   The parent name
    *
    * @return   int   The occurrence type of element with the given name in
    * the parent element with the given parent name. If the element does
    * not occur within the parent, returns <CODE>PSDtdNode.OCCURS_UNKNOWN</CODE>.
    *
    * @todo: (ph) This method may not work correctly if the parentName appears
    * as a child of more than 1 element.
    */
   public int getOccurrenceSetting(String elementName, String parentName)
   {
      PSDtdElement parent;

      parent = (PSDtdElement) m_elements.get(parentName);
      if (parent != null)
      {
         PSDtdNode content = parent.getContent();
         if (content != null)
         {
         /* If element contains another element entry and the name is correct,
       return the content, if it's a nodelist containing the element, get
       the element's content setting in the list */
            if ((content instanceof PSDtdElementEntry) &&
               (((PSDtdElementEntry) content).getElement().getName().equals(elementName)))
            {
               return content.getOccurrenceType();
            }
            else if (content instanceof PSDtdNodeList)
            {
               for (int i = 0;
               i < ((PSDtdNodeList) content).getNumberOfNodes(); i++)
               {
                  PSDtdNode curNode = ((PSDtdNodeList) content).getNode(i);
                  if (curNode != null)
                  {
                     if ((curNode instanceof PSDtdElementEntry) &&
                        (((PSDtdElementEntry) curNode).getElement().getName().equals(elementName)))
                     {
                        return curNode.getOccurrenceType();
                     }
                  }
               }
            }
         }
      }

      return PSDtdNode.OCCURS_UNKNOWN;
   }

   /**
    * @author   DVG
    *
    * @version 1.0
    *
    * Gets the maximum occurrence setting of the given element within the given parent.
    * For example, if a <B>BookList</B> element can contain one or more
    * <B>Book</B> elements, calling this method with ("Book", "BookList") will
    * return <CODE>PSDtdNode.OCCURS_ATLEASTONCE</CODE>.  If, however,
      * <B>BookList</B> contains a nodelist which occurs at least once, and
    * <B>Book</B> appears in the nodelist, but is only set to occur once
    * itself, this method will still return <CODE>PSDtdNode.OCCURS_ATLEASTONCE</CODE>,
    * as opposed to getOccurrenceSetting which will return
    * <CODE>PSDtdNode.OCCURS_ONCE</CODE>
    *
    * @param   elementName   The element name
    * @param   parentName   The parent name
    *
    * @return   int   The occurrence type of element with the given name in
    * the parent element with the given parent name. If the element does
    * not occur within the parent, returns <CODE>PSDtdNode.OCCURS_UNKNOWN</CODE>.
    */
   public int getMaxOccurrenceSetting(String elementName, String parentName)
   {
      PSDtdElement parent;

      parent = (PSDtdElement) m_elements.get(parentName);
      if (parent != null)
      {
         PSDtdNode content = parent.getContent();
         if (content != null)
         {
         /* If element contains another element entry and the name is correct,
             return the content, if it's a nodelist containing the element, get
             the element's content setting in the list */
            if ((content instanceof PSDtdElementEntry) &&
               (((PSDtdElementEntry) content).getElement().getName().equals(elementName)))
            {
               return content.getOccurrenceType();
            }
            else if (content instanceof PSDtdNodeList)
            {
               /* Bug Id: Rx-99-10-0186 now search down through levels
                  of nodelists instead of only one nodelist level */
               PSDtdElementEntry contentEntry = (PSDtdElementEntry) content.acceptVisitor(this, elementName);
               if (contentEntry != null)
               {
                  return contentEntry.getMaxMergedOccurrenceSetting();
               }
            }
         }
      }

      return PSDtdNode.OCCURS_UNKNOWN;
   }

   /* PSDtdTreeVisitor implementation details for locating
      a node in a content model, used for maxOccurrence resolution,
      addresses Bug Id: Rx-99-10-0186 */
   public Object visit(PSDtdElementEntry node, Object data)
   {
      if (node.getElement().getName().equals(data))
         return node;
      else
         return null;
   }

   public Object visit(PSDtdNodeList node, Object data)
   {
      for (int i = 0;
      i < node.getNumberOfNodes(); i++)
      {
         PSDtdNode curNode = node.getNode(i);
         Object obj = curNode.acceptVisitor(this, data);
         if (obj != null)
            return obj;
      }
      return null;
   }

   public Object visit(PSDtdNode node, Object data)
   {
      return null;
   }

   public Object visit(PSDtdDataElement node, Object data)
   {
      return null;
   }




      /**
   *@return <code> true </code> if tree (attributes) were modified.
   *<code> false </code> if not
   */
   public boolean isTreeDirty()
   {
       return(m_dirty);
   }
   /**
   *sets the tree dirty flag ( attributes were modified )
   *
   *@param <code> true </code> if tree (attributes) were modified.
   *<code> false </code> if not
   */
   public void setTreeDirty(boolean bDirty)
   {
      m_dirty=bDirty;
   }


   /*********/



           /**
   *returns the attribute char on an string format
   *
   *@param the type: PSDtdNode.OCCURS_ANY ("*")
   *                 PSDtdNode.OCCURS_ATLEASTONCE ("+")
   *                   PSDtdNode.OCCURS_OPTIONAL ("?")
   *                 any other return " "
   *
   *@return the type string
   */
   private String getStringOccurence(int type)
   {
         String ret=new String();
         switch(type)
         {
            case PSDtdNode.OCCURS_ANY:
                   ret="*";
            break;
            case PSDtdNode.OCCURS_ATLEASTONCE:
             ret="+";
            break;

         case PSDtdNode.OCCURS_OPTIONAL:
             ret="?";
            break;
            default:
            break;
         }
      return(ret);
  }



 /**
  *resets/constructs all the arrays and the stack
  *
  */
  private void init()
  {
     elemtList=null;
     pcData=null;
     atList=null;
     m_stack=null;
     visitedMap=null;

     visitedMap=new HashMap();


     if( elemtList== null )
     {
        elemtList=new Vector();
       }
     if(  pcData ==null )
     {
          pcData=new Vector();
     }
     if( atList == null )
     {
         atList=new Vector();
     }
     if( m_stack == null )
     {
          m_stack = new Stack();
     }
  }

 /**
  *this function takes an string and stores, it searchs for duplicates, if is
  *do not add it
  *
  *@param str to be added
  */
  private void addStringToList(String str)
  {
     boolean addToList=true;

     int pos=str.indexOf("(#PCDATA)>");
     if( pos > 0)
     {
               int limit=pcData.size();
          if(limit == 0 )
          {
               pcData.add(str);
               elemtList.add(str);
          }
          else
          {
             String search=new String();
             for(int count=0;count<limit;count++)
             {
                 search=(String)pcData.get(count);
                 if( search.equals(str) )
                 {
                        addToList=false;
                        break;
                 }
             }
             if( addToList )
             {
                    pcData.add(str);
                    elemtList.add(str);
            }
          }
    }
    pos=str.indexOf("<!ELEMENT");
    if( pos == -1  )
    {
          int limit=atList.size();
               if(limit == 0 )
          {
               atList.add(str);
               elemtList.add(str);

          }
          else
          {
             String search=new String();
             for(int count=0;count<limit;count++)
             {
                 search=(String)atList.get(count);
                 if( search.equals(str) )
                 {
                        addToList=false;
                        break;
                 }
             }
             if( addToList )
             {
                  atList.add(str);
                  elemtList.add(str);
             }
          }
    }
    else
    {
         int limit=elemtList.size();
         if ( limit == 0)
         {
                elemtList.add(str);
         }
         else
         {
             String search=new String();
             for(int count=0;count<limit;count++)
             {
                 search=(String)elemtList.get(count);
                 if( search.equals(str) )
                 {
                    addToList=false;
                    break;
                 }
           }
            if( addToList == true )
            {
              elemtList.add(str);
            }

        }
    }


  }
  /**
  *add a string to the stack
  *
  *@param value string to be copy and added into the stack
   */
  private void push(String value)
  {
     String newStr=new String(value);
     m_stack.push(newStr);
  }
  /**
  *
  *@return the text stored in the stack, or the string invalid if stack is empty
  */
  private String pull()
  {
     String str=new String("invalid");
     if( m_stack.empty()== false )
     {
         str=(String)m_stack.pop();
     }
     return(str);
  }


  /**
  *process the content
  *
  *@param obj the content model
  *
  *@param rootName the root element name
  *
  *@param name the element name
  *
  *@param subName the content name
  */
   private  void processElement(Object obj,String rootName,String name,String subName)
  {
     if(subName == null)
        throw new IllegalArgumentException("subName cannot be null.");
     if( obj == null || subName.startsWith(rootName))
     {
        return;
     }
     else if( obj  instanceof PSDtdNodeList )
     {
         obj=processList((PSDtdNodeList)obj,rootName,name);
         if( obj  instanceof PSDtdNodeList )
         {
            name=getListParent((PSDtdNodeList)obj);
         }
         processElement(obj,rootName,name,subName);
     }
     else if ( obj instanceof PSDtdElementEntry )
     {
        String objName=getElementName((PSDtdElementEntry)obj);
        if( visitedMap.put(objName,(PSDtdElementEntry)obj) == null )
        {
           obj=insertElementEntry((PSDtdElementEntry)obj,rootName,objName,subName);
           processElement(obj,rootName,name,subName);
        }
     }


  }
  /**
  *@param subList the node list to extract the name
  *
  *@return the parent name of the list
  */
   private String getListParent(PSDtdNodeList subList)
  {
    String csRet=new String("invalid");
      if( subList != null )
    {
        PSDtdElementEntry entry=subList.getParentElement();
        if( entry != null )
        {
               PSDtdElement elem=entry.getElement();
               if( elem != null )
               {
                   csRet=elem.getName();
               }
        }
        else
        {
            Object obj=null;
            int limit=subList.getNumberOfNodes();
            PSDtdElementEntry element;
            PSDtdElementEntry parent=null;
            PSDtdElement elem=null;
            for( int count=0;count< limit;count++)
            {

                obj=subList.getNode(count);
                if( obj instanceof PSDtdElementEntry )
                {
                       element=(PSDtdElementEntry )obj;
                       if( element != null )
                       {
                         parent=element.getParentElement();
                         if(parent != null )
                         {
                             elem=parent.getElement();
                             if( elem != null )
                             {
                               csRet=elem.getName();
                               break;
                             }
                         }
                     }
                }
            }
        }

    }
    return(csRet);
  }
  /**
  *process the mix model ( a list with PCDATA being part of the list )
  *
  *@param subList the list to process
  *
  */
   private void processMixedModel(PSDtdNodeList subList)
  {
    if(subList != null )
    {
      String str=elType+getListParent(subList);
      Object obj=null;
      String sep=new String();
      String subName=new String();
         PSDtdElementEntry element;
      PSDtdElement elem=null;
      for( int count=0;count< subList.getNumberOfNodes();count++)
      {
         int type=subList.getType();
         obj=subList.getNode(count);
         if( type ==PSDtdNodeList.OPTIONLIST )
         {
             sep=" | ";
         }
         else
         {
              sep=", ";
         }
         if( obj instanceof PSDtdElementEntry )
         {
              element=(PSDtdElementEntry )obj;
              elem = element.getElement();
              if( elem != null )
              {
                processAttributes(elem);
                subName=elem.getName();
                str+=subName;
                if( count+1 < subList.getNumberOfNodes() )
                  str+=sep;
              }
        }
        else
            {
           str+=" (#PCDATA"+sep;
        }

     }
     str+=")";
     if( subList.getType() ==PSDtdNodeList.OPTIONLIST )
     {
         str+="*";
     }
     str+=">\r\n";
     addStringToList(str);
   }
  }
  /**
  *process a sublist ( a list inside a list )
  *
  *@param subList the list to be added into the main list
  *
  *@param rootName the name of the root element
  *
  *@param name the list owner name
  *
  *@return the content model
  *
  */

   private Object processSublist(PSDtdNodeList subList, String rootName,String name)
  {
     String  subName=new String();
     String path=new String();
     String str=new String();
     String sep=new String();
     String newstr=new String();
     PSDtdElementEntry element;
     PSDtdElement elem=null;
     Object obj=null;
     Vector prcList=new Vector();

     for( int count=0;count<subList.getNumberOfNodes();count++)
     {
          int type=subList.getType();
         if( type ==PSDtdNodeList.OPTIONLIST )
        {
             sep=" | ";
        }
        else
        {
              sep=", ";
        }

        obj=subList.getNode(count);
        if( obj instanceof PSDtdElementEntry )
        {
            element=(PSDtdElementEntry )obj;
            elem = element.getElement();
            if( elem != null )
            {
                processAttributes(elem);
                subName=elem.getName();
                subName+=getStringOccurence(element.getOccurrenceType());
                if( str.length() == 0 )
                {
                   str=name+" ("+subName;
                }
                else
                {
                   str+=subName;
                }
                  obj=elem.getContent();
                if( count+1 < subList.getNumberOfNodes())
                   str+=sep;

                 if( obj == null )
                 {
                    if( elem != null )
                    {
                        String strVal=elType+elem.getName()+" EMPTY>" + NEWLINE;
                          addStringToList(strVal);
                     }
                 }

               }
           }
           if (obj instanceof PSDtdDataElement )
           {

                if( elem != null )
                {
                  newstr=elType+elem.getName()+" (#PCDATA)>\r\n";
                  addStringToList(newstr);
                }
                else
                {
                  if( count == 0 )
                  {
                      processMixedModel(subList);
                  }
                  su=name;
                  return(elem);
                }
          }
          if (obj instanceof PSDtdNodeList)
          {
               PSDtdNodeList List=(PSDtdNodeList)obj;
               type=List.getType();
                        if( type == PSDtdNodeList.OPTIONLIST )
                {
                   obj=processSublist(List,rootName,str);
                   str=su;
                   su="";
                }
                else if( type == PSDtdNodeList.SEQUENCELIST )
                {
                    prcList.add(obj);
                }
         }

    }
    su=str+" )";

    int limit=prcList.size();
    if( limit > 0 )
    {
       push(su);
       for(int count=0;count< limit; count++)
       {
         Object tmpObj=prcList.get(count);
         if( tmpObj  instanceof PSDtdNodeList )
             {
                 PSDtdNodeList list=(PSDtdNodeList)tmpObj;
                 name=getListParent((PSDtdNodeList)tmpObj);
                 processElement(tmpObj,rootName,name,subName);
         }
       }
        su=pull();
    }
    return(obj);
  }

 /**
  *
  */
  private void processElementEntry(PSDtdElementEntry entry)
  {
      String name=getElementName(entry);
      String str=new String();

      str=elType+" ("+name+getStringOccurence(entry.getOccurrenceType())+" )"+">\r\n";
      addStringToList(str);
  }

  /**
  *process a node list
  *
  *@subList the list to be processed
  *
  *@param rootName the root element name
  *
  *@param name the list owner name
  *
  *@return the content model
  *
  */
   private Object  processList(PSDtdNodeList subList, String rootName,String name)
   {

     String  subName=new String();
     String path=new String();
     String str=new String();
       String sep=new String();

     PSDtdElementEntry element;
     PSDtdElement elem=null;
     Object obj=null;
     Object contentModel=null;
     Vector prcList=new Vector();
     boolean bprocessListmodel=false;

     //    if( name.length() == 0 )
   //  {
         name=getListParent(subList);
    // }



     str=elType+name+" (";

     for( int count=0;count<subList.getNumberOfNodes();count++)
     {
        int type=subList.getType();

        if( type ==PSDtdNodeList.OPTIONLIST )
        {
             sep=" | ";
        }
        else
        {
              sep=", ";
        }
        obj=subList.getNode(count);
        if (obj instanceof PSDtdDataElement )
        {
           if( count == 0 && subList.getNumberOfNodes() > 1 )
           {
                 str=elType+name+" ("+"#PCDATA |";
                 bprocessListmodel=true;
           }
        }
        if( obj instanceof PSDtdElementEntry )
        {
              element=(PSDtdElementEntry )obj;
              elem = element.getElement();
              if( elem != null )
              {
                processAttributes(elem);
                subName=elem.getName();
                subName+=getStringOccurence(element.getOccurrenceType());

                str+=subName;


                contentModel=elem.getContent();
                if( count+1 < subList.getNumberOfNodes())
                   str+=sep;
              }
              if( contentModel == null )
              {
                  if( elem != null )  // added it was missing
                  {
                        String strVal=elType+elem.getName()+" EMPTY>" + NEWLINE;
                          addStringToList(strVal);
                  }
              }
              if (contentModel instanceof PSDtdDataElement )
              {
                 String newstr=elType+elem.getName()+" (#PCDATA)>\r\n";
                 addStringToList(newstr);
              }
              if( contentModel instanceof PSDtdElementEntry )
              {
                    processElement(element,rootName,"",subName);
                    processElement(contentModel,rootName,"",subName);
              }
              if( contentModel  instanceof PSDtdNodeList )
              {

                 processElement(element,rootName,subName,"");

                PSDtdNodeList list=(PSDtdNodeList)contentModel;
                prcList.add(contentModel);

            }
       }

       if( obj  instanceof PSDtdNodeList )
       {
            PSDtdNodeList list=(PSDtdNodeList)obj;
            type=list.getType();
            if( type ==PSDtdNodeList.OPTIONLIST )
            {
                if( str.length() == 0 )
                {
                    str=elType+name+" (";
                }

               obj=processSublist((PSDtdNodeList)obj,rootName,str);
               str=su;
               if( count+1 < subList.getNumberOfNodes() && str.endsWith(sep)== false)
               {
                     str+=sep;
               }
               su="";
            }
            else if( type == PSDtdNodeList.SEQUENCELIST )
            {
                    prcList.add(obj);
            }
        }
     }
     str+=" )";

     if( subList.getType() ==PSDtdNodeList.OPTIONLIST && bprocessListmodel == true)
     {
         str+="*";
     }

     str+=">\r\n";



     addStringToList(str);

     int limit=prcList.size();
     if( limit > 0 )
     {
       for(int count=0;count< limit; count++)
       {
         Object tmpObj=prcList.get(count);
         if( tmpObj  instanceof PSDtdNodeList )
             {
                 PSDtdNodeList list=(PSDtdNodeList)tmpObj;
                 name=getListParent((PSDtdNodeList)tmpObj);
                 processElement(tmpObj,rootName,name,subName);
         }
       }
     }
     return(obj);
   }

   /**
   *process an element entry
   *
   *@param element the element to be processed
   *
   *@param rootName the root element name
   *
   *@param name the list owner name
   *
   *@param subname the content model name
   *
   *@return the content model
   *
   *
   */
   private Object insertElementEntry(PSDtdElementEntry element,
                                     String rootName,
                                     String name,
                                     String subName)
   {
      Object obj=null;
      String str=new String();

      PSDtdElement elem = element.getElement();
      if( elem != null )
      {
         processAttributes(elem);
         obj=elem.getContent();
         if (obj == null)
         {
            // add an empty element
            str = elType + name + " EMPTY>\r\n";
            addStringToList(str);
         }
         else if (obj instanceof PSDtdDataElement ||
            obj instanceof PSDtdElementEntry)
         {
            if(  obj instanceof PSDtdElementEntry)
            {
               PSDtdElementEntry ent = (PSDtdElementEntry)obj;

               //get the child of an object passed in
               PSDtdElement el = ent.getElement();

               //get the child's name
               String childName = el.getName();

               str = elType+name + " (" + childName +
                  getStringOccurence(ent.getOccurrenceType()) + " )" + ">\r\n";
            }
            else // it must be a PSDtdDataElement
            {
               str=elType+name+" (#PCDATA)>\r\n";
            }


            addStringToList(str);
         }
         else if (obj instanceof PSDtdNodeList )
         {
          obj=processList((PSDtdNodeList)obj,rootName,name);
         }
      }
      return(obj);
   }

  /**
   *process the notation, attlist for the element
   *
   *@param el the element to be processed
   *
   */
   private void processAttributes(PSDtdElement el)
   {
      PSDtdAttribute attrib=null;
      int ocurrence;
      List m_list;
      String csattribName;
      int type;
      String format=new String();
      String elementName=el.getName();
      int limit=el.getNumAttributes();
      if( limit > 0 )
      {
          for(int count=0;count< limit;count++)
          {
            attrib=el.getAttribute(count);
            csattribName=attrib.getName();

            ocurrence=attrib.getOccurrence();
                  m_list=attrib.getPossibleValues();

            type=attrib.getType();

            if( type == PSDtdAttribute.NOTATION  ||
                type == PSDtdAttribute.ENOTATION ||
                type == PSDtdAttribute.ENUMERATION  )
            {
               int size=m_list.size();
               Iterator i=m_list.iterator();

               if( type == PSDtdAttribute.NOTATION )
               {
                  format="<!NOTATION "+elementName+" "+csattribName+" ";
               }
               else if (type ==  PSDtdAttribute.ENOTATION )
               {
                    format="<!ATTLIST "+elementName+" "+csattribName+" NOTATION (";
               }
               else
               {
                    format="<!ATTLIST "+elementName+" "+csattribName+" (";
               }

               int elmCnt=0;
               while(i.hasNext())
               {
                   elmCnt++;
                   format+=(String)i.next();
                   if( elmCnt < size)
                   {
                       format+=" | ";
                   }
                      }
               if(    type == PSDtdAttribute.ENUMERATION)
               {
                 format += " )";

                 String defaultValue = attrib.getDefaultStringValue();
                 if (defaultValue != null &&
                     defaultValue.trim().length() > 0)
                    format += " " + "\"" + defaultValue + "\"";
               }
               if( type == PSDtdAttribute.ENOTATION )
               {
                 format+=" )"+attrib.getOcurrenceText();

               }
               if( type == PSDtdAttribute.NOTATION )
               {
                 format+=attrib.getOcurrenceText();
               }
               format+=">\r\n";
               addStringToList(format);

            }
            else
            {
              format="<!ATTLIST  "+elementName;
              format+=" "+csattribName+" "+attrib.getTypeText();
              format+=" "+attrib.getOcurrenceText();
              format+=">\r\n";
              addStringToList(format);
            }
         }
      }
   }

  /**
  *returns the element name
  *
  *@param entry the element to be processed
  *
  *@return the element name
  *
  */
  private String getElementName(PSDtdElementEntry entry)
  {
       PSDtdElement el=entry.getElement();
     String name =el.getName();
     return(name);
  }
  /**
  *process a element list, because of a single entry is returned
  *as an element entry
  *
  *@param entry the element to be processed
  *
  *@return the content model
  *
  */
  private Object processSingleEntryList(PSDtdElementEntry entry)
  {
     String str=new String();
       PSDtdElement el=entry.getElement();
     String name =el.getName();
     Object obj=el.getContent();
     if( obj instanceof PSDtdElementEntry )
     {
          String subName=getElementName((PSDtdElementEntry)obj);
          str=elType+name+" ("+subName+getStringOccurence(((PSDtdElementEntry)obj).getOccurrenceType())+" )"+">\r\n";
          addStringToList(str);
    }
    return(obj);
  }

   /**
    * Converts this tree to the proper format for textual display. If the flag
    * wrapInXml is <code>true</code>, the resulting text will be a well formed
    * XML document, otherwise, it will be a standard DTD w/ the first line
    * as an XML processing instruction that specifies the encoding of the chars.
    * The encoding specified in the header is the standard encoding used by the
    * server. If the String is written to a file or a byte array, this encoding
    * must be specified.
    *
    * @param wrapInXml If <code>true</code>, the resulting DTD is wrapped in
    * a DOCTYPE element so the resulting document is a well formed XML document.
    * Not used anymore. The DTD is never wrapped in DOCTYPE element anymore.
    * @return The text format of the DTD represented by this object.
    *
    * @see PSCharSets#rxStdEnc()
   **/
   public String toDTD(boolean wrapInXml)
   {
      int ival,iCount=0;
      PSDtdElementEntry e;
      PSDtdElementEntry eChild;

      init();

      String ret=new String();
      PSDtdElementEntry  elemRoot,entry;

      elemRoot =getRoot();
      String name,path;

      PSDtdElement el = elemRoot.getElement();

      String rootName=el.getName();
      visitedMap.put(rootName,elemRoot);
      Object obj=el.getContent();
      Object obj2=null;
      Object obj3=null;
      PSDtdNode node;
      PSDtdElementEntry  ee;
      PSDtdDataElement   elf;
      String subName;
      String subSubName;
      Object objNode=null;

      // first, check root element for its attributes and process them
      if ( obj != null )
      {
         processAttributes( el );
      }

      if( obj instanceof PSDtdNodeList )
      {
         ret=elType+rootName+getStringOccurence(elemRoot.getOccurrenceType())+" "+"(";
         PSDtdNodeList  nodeList=(PSDtdNodeList)obj;
         if( nodeList.getType() == PSDtdNodeList.SEQUENCELIST )
         {
            int limit=nodeList.getNumberOfNodes();
            for( int count=0;count<limit;count++)
            {
               ee=(PSDtdElementEntry )nodeList.getNode(count);
               el = ee.getElement();
               name=el.getName();
               visitedMap.put(name,ee);
               processAttributes(el);

               ret+=name+getStringOccurence(ee.getOccurrenceType());

               if( count+1 < limit )
                  ret+=", ";

               obj2=el.getContent();
               if( obj2 == null )
               {
                  String str=elType+name+" EMPTY>" + NEWLINE;
                  addStringToList(str);
               }
               else
               {
                  if (obj2 instanceof PSDtdDataElement )
                  {
                     String str=elType+name+" (#PCDATA)>" + NEWLINE;
                     addStringToList(str);
                  }
                  else
                  {
                     // if the object model is an entry this is a single
                     // entry node list
                     if( obj2 instanceof PSDtdElementEntry )
                     {
                         obj2=processSingleEntryList(ee);
                     }
                     processElement(obj2,rootName,name,"");
                  }
               }
            }
            ret+=" )>" + NEWLINE;
         }
         else
         {
            int limit=nodeList.getNumberOfNodes();
            for( int count=0;count<limit;count++)
            {
               objNode=nodeList.getNode(count);
               if( objNode instanceof PSDtdDataElement )
               {
                  ret+="#PCDATA";
               }
               if( objNode instanceof PSDtdElementEntry )
               {
                  ee=(PSDtdElementEntry )objNode;
                  el = ee.getElement();
                  name=el.getName();
                  processAttributes(el);
                  visitedMap.put(name,ee);
                  ret+=name+getStringOccurence(ee.getOccurrenceType());
                  obj2=el.getContent();
                  if( obj2 == null )
                  {
                     String str=elType+name+" EMPTY>" + NEWLINE;
                     addStringToList(str);
                  }
                  else
                  {
                     if (obj2 instanceof PSDtdDataElement )
                     {
                        String str=elType+name+" (#PCDATA)>" + NEWLINE;
                        addStringToList(str);
                     }
                     else
                     {
                        // if the object model is an entry this is a single
                        // entry node list
                        if( obj2 instanceof PSDtdElementEntry )
                        {
                           obj2=processSingleEntryList(ee);
                        }
                        processElement(obj2,rootName,name,"");
                     }
                  }
               }
               if( count+1 < limit )
                  ret+=" | ";

            }
            ret+=" )*>" + NEWLINE;
         }
         addStringToList(ret);
      }
      else if( obj instanceof PSDtdElementEntry )
      {
         entry=(PSDtdElementEntry)obj;
         el =entry.getElement();
         subSubName=el.getName();

         String str = elType + rootName + " ("
               + subSubName+getStringOccurence(entry.getOccurrenceType())
               + " )" + ">" + NEWLINE;
         addStringToList(str);

         processElement(entry,rootName,"","");
      }
      else if ( obj instanceof PSDtdDataElement )
      {
         addStringToList( elType + rootName + " (#PCDATA)>" + NEWLINE );
      }
      else
      {
         addStringToList( elType + rootName + " EMPTY>" + NEWLINE );
      }

      String csResultString=new String("<?xml version='1.0' encoding='" + PSCharSets.rxStdEnc()
            + "'?>" + NEWLINE);

      String csTmp=new String();
      int limit=elemtList.size();

      for(int count=limit-1;count >= 0; count--)
      {
         csTmp=(String)elemtList.get(count);
         csResultString+=csTmp;
      }

      return(csResultString);
  }

   /**
    * @author   chadloder
    *
    * @version 1.4 1999/05/28
    *
    * Prints the entire tree to System.out.
    */
   public void print() {
      if (rootElement != null)
         rootElement.print("");
   }

   /* Want to traverse all the elements?  Go key by key... */
   public Iterator elementKeyIterator()
   {
      if (m_elements == null)
         return null;
      else
         return m_elements.keySet().iterator();
   }

   // this is a workaround to the problem of the input stream being
   // closed by IBM's parser. We extend and no-op the close()
   // method
   private class NoCloseInputStream extends PSInputStreamAdapter
   {
      public NoCloseInputStream(InputStream in)
      {
         super(in);
      }

      public void close() throws IOException
      {
         // do nothing
      }
   }



   /** Only need one of these - placeholder for data */
   static private PSDtdDataElement ms_dataElement = new PSDtdDataElement();

   /** Root element for this tree */
   PSDtdElementEntry rootElement;

   /** HashMap for all elements */
   HashMap m_elements;

   /** Catalog list holder, for future calls, this will be cached */
   List m_catalogList;

   /** Need some sort of overflow condition for cataloging,
    DTDs can be very complex */
   static public final int MAX_CATALOG_SIZE = 1000;
  /** if <code> true </code> the repeats attributes were modified */
   boolean m_dirty=false;

   /** the element list */
  private Vector elemtList=null;
  /** start of the element string */
  private String elType=new String("<!ELEMENT ");
  /** the stack        */
  private Stack m_stack=null;
  /**  contains pcdata ( faster searchs)       */
  private Vector pcData=null;
  /**  contains attlist ( faster search)       */
  private Vector atList=null;
  /**   tmp global string       */
   private String su=new String();

  /**the hashmap to check for recursion */
  private HashMap visitedMap=null;


  /** newline */
   private static final String NEWLINE = "\r\n";

}
