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

import com.percussion.design.catalog.PSCatalogException;
import com.percussion.server.PSConsole;
import com.percussion.util.PSCharSets;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used for generating DTD from a XML document.
 */
public class PSDtdGenerator
{
   private static final Logger log = LogManager.getLogger(PSDtdGenerator.class);
   public static void main(String[] args)
   {
      try
      {
         for (int i = 0; i < args.length; i++)
         {
            StringTokenizer tok = new StringTokenizer(args[i], ",;");
            ArrayList documents = new ArrayList(3);
            while (tok.hasMoreTokens())
            {
               Document doc = PSXmlDocumentBuilder.createXmlDocument(
                  new FileInputStream(new File(tok.nextToken())),
                  false);
               documents.add(doc);
            }
            PSDtdGenerator gen = new PSDtdGenerator();
            List fields = null;

            if (documents.size() == 1)
            {
               gen.generateDtd((Document)documents.get(0));
               PSDtdTree tree = PSDtdGenerator.generate((Document)documents.get(0));
               fields = tree.getCatalog("/", "@");
            }
            else
            {
               Document[] exemplars = new Document[documents.size()];
               exemplars = (Document[])documents.toArray(exemplars);
               gen.generateDtd(exemplars);

               PSDtdTree tree = PSDtdGenerator.generate(exemplars);
               fields = tree.getCatalog("/", "@");
            }

            if (fields != null)
            {
               log.info("\n\nFields for above DTD: ");
               for (Iterator k = fields.iterator(); k.hasNext(); )
               {
                  log.info(k.next().toString());
               }
            }
            else
            {
               log.info("Could not catalog fields.");
            }
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
   }

   /**
    * @author   chadloder
    *
    * Constructor. Does nothing.
    */
   public PSDtdGenerator()
   {
   }

   public static PSDtdTree generate(Document exemplar)
   {
      return generate(new Document[] { exemplar });
   }

   /**
    * Generates DTD from XML documents.
    * @param exemplars array of XML documents from which the DTD should be
    * generated. May not be <code>null</code> or empty.
    * @return the <code>PSDtdTree</code> representing the DTD for the input
    * XML documents. May be <code>null</code> if any error occurs during DTD
    * generation or parsing the generated DTD.
    */
   public static PSDtdTree generate(Document[] exemplars)
   {
      PSDtdGenerator gen = new PSDtdGenerator();
      PSDtdTree returnedDtd = null;

      gen.generateDtd(exemplars);

      // collect the DTD in a byte array
      ByteArrayOutputStream bout =
         new ByteArrayOutputStream(2048);

      try
      {
         gen.writeDtd(bout);
         byte[] ba = bout.toByteArray();

         // now parse the DTD with the parser
         PSDtdParser p = new PSDtdParser();
         XMLInputSource src = new XMLInputSource(null, null, null,
            new ByteArrayInputStream(ba),
            /*PSCharSets.rxStdEnc()*/ null);

         p.parseDtd(src);
         PSDtd dtd = p.getDtd();
         return new PSDtdTree(p);
      }
      catch (IOException e)
      {
         // shouldn't happen
         PSConsole.printMsg("IOException while generating DTD : ", e);
         return null;
      }
      catch (PSCatalogException e)
      {
         // shouldn't happen
         PSConsole.printMsg("PSCatalogException while generating DTD : ", e);
         return null;
      }
   }

   /**
    * Infers a DTD to which the given document will conform. The generated
    * DTD tends to be as restrictive as possible.
    *
    * The strategy used is:
    *
    *   <ul>
    *       <li>If an element contains both non-space character data and child elements, then it is
    *                                                                                               declared with mixed element content, permitting all child elements that are actually
    *                                                                                               encountered within instances of that parent element.</li>
    *       <li>If no significant character data is found in an element, it is assumed that the element
    *                                                                                               cannot contain character data.</li>
    *       <li>If an element contains child elements but no significant character data, then it is
    *                                                                                               declared as having element content. If the same child elements occur in every instance
    *                                                                                               of the parent and in a consistent sequence, then this sequence is reflected in the element
    *                                                                                               declaration: where child elements are repeated or trailing children (only) are omitted
    *                                                                                               in some instances of the parent element, this will result in a declaration that shows
    *                                                                                               the child element as being repeatable or optional or both. If no such consistency of
    *                                                                                               sequence can be detected, then a more general form of element
    *                                                                                               declaration is used in which all child elements may appear any number of times in any sequence.
    *       <li>If neither character data nor subordinate elements are found in an element, it is
    *                                                                                               assumed the element must always be empty.</li>
    *       <li>An attribute appearing in an element is assumed to be REQUIRED if it appears in every
    *                                                                                               occurrence of the element.</li>
    *       <li>NOT IMPLEMENTED: An attribute that has a distinct value every time it appears is assumed to be an
    *                                                                                               identifying (ID) attribute, provided that there are at least 10 instances of the element
    *                                                                                               in the input document.</li>
    *       <li>NOT IMPLEMENTED: An attribute is assumed to be an enumeration attribute if it has less than ten distinct
    *                                                                                               values, provided that the number of instances of the attribute is at least three times the
    *                                                                                               number of distinct values and at least ten. </li>
    *   </ul>
    *
    * @param exemplar A representative document.
    *
    * @return PSDtdTree a DTD to which the given document will confirm.
    */
   public void generateDtd(Document exemplar)
   {
      generateDtd(new Document[] { exemplar } );
   }

   /**
    * Infers a DTD to which all of the given documents will conform. The generated
    * DTD tends to be as restrictive as possible. All documents in the array must
    * have a root element with the same name.
    *
    * @param exemplar An array of representative documents.
    *
    * @return PSDtdTree a DTD to which the given document will confirm.
    *
    * @see #generateDtd(Document)
    */
   public void generateDtd(Document[] exemplars)
   {
      m_elMap = new HashMap();
      m_elements = new ArrayList();

      if (exemplars == null || exemplars.length == 0)
      {
         throw new IllegalArgumentException("No exemplar documents given");
      }

      Element root = exemplars[0].getDocumentElement();
      if (root == null)
      {
         throw new IllegalArgumentException("Document has no root");
      }
      String rootName = root.getNodeName();

      for (int i = 0; i < exemplars.length; i++)
      {
         root = exemplars[i].getDocumentElement();
         if (root == null)
         {
            throw new IllegalArgumentException("document " + i + " has null root.");
         }
         if (!root.getNodeName().equals(rootName))
            throw new IllegalArgumentException("Root element "
               + root.getNodeName() + " should be " + rootName);
         handleNode(root, -1, true, 1, 1);
      }
   }

   /**
    * @author   chadloder
    *
    * @version 1.2 1999/06/02
    *
    * Writes the generated DTD to the given OutputStream using the given encoding.
    *
    * @param   out The stream to which the DTD will be written.
    *
    * @throws   IllegalStateException If the DTD has not been generated yet.
    *
    */
   public void writeDtd(OutputStream out) throws IOException
   {
      writeDtd(out, null);
   }

   /**
    * @author   chadloder
    *
    * @version 1.2 1999/06/02
    *
    * Writes the generated DTD to the given OutputStream using the given encoding.
    *
    * @param   out The stream to which the DTD will be written.
    * @param   enc   The character encoding that will be used. If null, the default
    * character encoding will be used.
    *
    * @throws   IllegalStateException If the DTD has not been generated yet.
    *
    */
   public void writeDtd(OutputStream out, String enc) throws IOException
   {
      // go through each element and print out a <!ELEMENT entry for it
      // plus its child sequence information for all of the elements it
      // can contain and all the attributes it can have
      if (null == m_elMap)
         throw new IllegalStateException("DTD has not been generated yet.");

      // Xerces has problem with DTD parsing if the encoding specified is
      // Java encoding. Also we no longer use <!DOCTYPE in the DTD.
      if (null == enc)
         enc = PSCharSets.rxStdEnc();

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, enc));

      writer.write("<?xml version=\"1.0\" encoding=\"" + enc + "\"?>");
      writer.newLine();

      // for each element that is defined
      Iterator i = m_elements.iterator();
      int elementNumber = 0;
      while (i.hasNext())
      {
         ElementDefinition elDef = (ElementDefinition)i.next();
         String elName = elDef.getName();

         // we have added a dummy element for PCDATA to simplify
         // processing. we don't print out PCDATA's definition, instead
         // we only mark its presence as a child (see below)
         if (elName.equals("#PCDATA"))
            continue;

         TreeMap childDefs = elDef.getChildOccurrences();

         // EMPTY content
         if (childDefs.size() == 0)
         {
            if (elDef.hasCharacterContent())   // PCDATA content
            {
               // should never happen
               // writer.write("<!ELEMENT " + elName + " ( #PCDATA ) >");
               // writer.newLine();
            }
            else // EMPTY content
            {
               writer.write("<!ELEMENT " + elName + " EMPTY >");
               writer.newLine();
            }
         }
         else // has children, so either element content or mixed content
         {
            writer.write("<!ELEMENT " + elName + " ( ");
            List elSeqChildren = elDef.getSequencedChildren();
            final int size = elSeqChildren.size();
            final int sizeMinusOne = size - 1;
            for (int j = 0; j < size; j++)
            {
               ChildSequenceDefinition child
                  = (ChildSequenceDefinition)elSeqChildren.get(j);

               // print out that the element can contain one of these
               writer.write(child.getName());

               if (elDef.isSequenced())
               {
                  // there is no such thing as optional or repeatable PCDATA
                  // so only do the optional/repeatable annotations if the node
                  // is not a PCDATA node
                  if (!child.getName().equals("#PCDATA"))
                  {
                     if (child.isRepeatable())
                     {
                        if (child.isOptional())
                           writer.write('*');
                        else
                           writer.write('+');
                     }
                     else
                     {
                        if (child.isOptional())
                           writer.write('?');
                        // else neither repeatable nor optional, so no annotation
                     }
                  }

                  if (j < sizeMinusOne)
                     writer.write(", ");
               }
               else
               {
                  if (j < sizeMinusOne)
                     writer.write(" | ");
               }
            }

            if (elDef.isSequenced())
            {
               writer.write(" ) >");
            }
            else
            {
               writer.write(" )* >");
            }

            writer.newLine();
         }

         // now do the attributes
         Set attributeNames = elDef.getAttributeNames();
         Iterator j = attributeNames.iterator();

         // open the ATTLIST
         if (j.hasNext())
         {
            writer.write("<!ATTLIST " + elName + " ");
            writer.newLine();
         }

         while (j.hasNext())
         {
            String attrName = (String)j.next();
            AttributeDefinition attrDef = elDef.getAttribute(attrName);

            // if the attribute is always present, then treat it as required
            boolean isRequired = (attrDef.getOccurrences() == elDef.getOccurrences());

            // TODO: more tests on attribute

            writer.write("          " + attrName + " CDATA ");

            if (isRequired)
            {
               writer.write("#REQUIRED ");
            }
            else
            {
               writer.write("#IMPLIED ");
            }

            writer.newLine();

            if (!j.hasNext())
            {
               writer.write(">"); // close the ATTLIST
               writer.newLine();
            }
         }
      }
      writer.newLine();

      writer.flush();
   }

   protected void handleNode(Node node, int seqInParent,
      boolean firstSiblingWithThisName, int groupNumber, int numberInGroup)
   {
      boolean isTextNode = false;
      String nodeName = node.getNodeName();
      if (node.getNodeType() == Node.TEXT_NODE)
      {
         isTextNode = true;
         nodeName = "#PCDATA";
      }

      ElementDefinition elDef = (ElementDefinition)m_elMap.get(nodeName);

      if (elDef == null)
      {
         // this is the first time we have ever seen this element anywhere
         elDef = new ElementDefinition(nodeName);
         m_elMap.put(nodeName, elDef);
         m_elements.add(elDef);
      }

      elDef.incrementOccurrences();

      NamedNodeMap attrs = node.getAttributes();
      if (attrs != null)
      {
         for (int i = 0; i < attrs.getLength(); i++)
         {
            Node att = attrs.item(i);

            String attName = att.getNodeName();
            String attVal = att.getNodeValue();

            AttributeDefinition attDef = elDef.getAttribute(attName);
            if (attDef == null)
            {
               attDef = new AttributeDefinition(attName);
               elDef.addAttribute(attDef);
            }
            attDef.addValue(attVal);
            attDef.incrementOccurrences();
         }
      }

      if (seqInParent != -1) // not root?
      {
         Node parent = node.getParentNode();
         String parentName = parent.getNodeName();

         // get the sequence information for this element as it occurs in the parent
         ElementDefinition parentElDef = (ElementDefinition)m_elMap.get(parentName);
         TreeMap parentChildren = parentElDef.getChildOccurrences();
         List parentChildSequence = parentElDef.getSequencedChildren();
         ChildSequenceDefinition elSeqDef = (ChildSequenceDefinition)parentChildren.get(nodeName);

         if (isTextNode)
         {
            parentElDef.setHasCharacterContent(true);
         }

         if (elSeqDef == null)
         {
            // this is the first time that we have seen this element under this parent
            // so add it to the parent's child sequence information

            elSeqDef = new ChildSequenceDefinition(nodeName);
            elSeqDef.setPosition(seqInParent);
            parentChildren.put(nodeName, elSeqDef);
            parentChildSequence.add(elSeqDef);

            // if this is not the first occurrence of the element's parent, but this is
            // the first time we have seen the element, then this element is optional
            if (parentElDef.getOccurrences() != 1)
            {
               elSeqDef.setOptional(true);
            }
         }
         else // we have seen this element under this parent before
         {
            // if this element has occurred at a different position under this
            // parent before, then the parent's child list cannot be sequenced
            if (elSeqDef.getPosition() != seqInParent)
            {
               if (numberInGroup < 1)
                  parentElDef.setSequenced(false);
            }
         }

         if (!firstSiblingWithThisName)
         {
            elSeqDef.setRepeatable(true);
         }
      }


      // now do all the child elements recursively
      NodeList elChildren = node.getChildNodes();
      HashMap map = new HashMap(elChildren.getLength());
      boolean firstOfName = false;
      int numChildren = elChildren.getLength();

      String thisName = "", lastName = "";
      int numInGroup = 0;
      int groupNum = 0;

      for (int i = 0; i < elChildren.getLength(); i++)
      {
         Node n = elChildren.item(i);
         thisName = n.getNodeName();

         if (Node.TEXT_NODE == n.getNodeType())
         {
            thisName = "#PCDATA";
            // see if we really have text or if it's just whitespace
            String nodeValue = n.getNodeValue().trim();
            if (nodeValue.length() == 0)
            {
               thisName = lastName;
               continue;
            }
         }
         else if (Node.ELEMENT_NODE != n.getNodeType())
         {
            continue;
         }

         if (thisName.equals(lastName))
         {
            numInGroup++;
         }
         else
         {
            numInGroup = 0; // start new group
            groupNum++;
         }

         // first time we've seen this guy in this sequence ?
         if (map.get(thisName) == null)
         {
            firstOfName = true;
            map.put(thisName, Boolean.TRUE);
         }
         else
         {
            firstOfName = false;
         }

         // recurse for this child
         handleNode(n, i, firstOfName, groupNum, numInGroup);

         lastName = thisName;
      }

      // If this is a sequence, then check to see if all the expected
      // children were present. For those that weren't, make them
      // optional.
      if (elDef.isSequenced())
      {
         List childSeq = elDef.getSequencedChildren();
         for (int i = 0; i < childSeq.size(); i++)
         {
            ChildSequenceDefinition cDef =
               (ChildSequenceDefinition)childSeq.get(i);
            if (map.get(cDef.getName()) == null)
               cDef.setOptional(true);
         }
      }
   }

   private class AttributeDefinition
   {
      public AttributeDefinition(String name)
      {
         m_name = name;
         m_occurrences = 0;
         m_values = new TreeMap();
      }

      public String getName()
      {
         return m_name;
      }

      /**
       * @author   chadloder
       *
       * @version 1.0 1999/6/1
       *
       * Increments the number of occurrences of this element.
       */
      public void incrementOccurrences()
      {
         m_occurrences++;
      }

      public int getOccurrences()
      {
         return m_occurrences;
      }

      /**
       * @author   chadloder
       *
       * @version 1.0 1999/6/1
       *
       * Adds a possible value that this attribute can have. Duplicate values
       * will not be added.
       *
       * @param   value
       *
       */
      public void addValue(String value)
      {
         m_values.put(value, Boolean.TRUE);
      }

      /**
       * @author   chadloder
       *
       * @version 1.0 1999/6/1
       *
       *
       * @return   int The number of possible values that this attribute can
       * have.
       */
      public int getNumValues()
      {
         return m_values.size();
      }

      /**
       * @author   unascribed
       *
       * @version 1.0 1999/6/1
       *
       *
       * @return   Iterator An iterator over the possible values that this
       * attribute can have.
       */
      public Iterator getValuesIterator()
      {
         return m_values.values().iterator();
      }

      private String m_name;
      private int m_occurrences;
      private TreeMap m_values;
      private boolean m_allValuesAreValidNames;
      private boolean m_allValuesAreValidNMTokens;
   }

   private class ElementDefinition
   {
      public ElementDefinition(String name)
      {
         m_name = name;
         m_occurrences = 0;
         m_hasCharacterContent = false;
         m_isSequenced = true;
         m_childOccurrences = new TreeMap();
         m_childSequence = new ArrayList();
         m_attributes = new TreeMap();
      }

      public String getName()
      {
         return m_name;
      }

      /**
       * @author   chadloder
       *
       * @version 1.0 1999/6/1
       *
       * Increments the number of occurrences of this element.
       */
      public void incrementOccurrences()
      {
         m_occurrences++;
      }

      public int getOccurrences()
      {
         return m_occurrences;
      }

      public void addAttribute(AttributeDefinition attr)
      {
         m_attributes.put(attr.getName(), attr);
      }

      public AttributeDefinition getAttribute(String name)
      {
         return (AttributeDefinition)m_attributes.get(name);
      }

      public Set getAttributeNames()
      {
         return m_attributes.keySet();
      }

      public TreeMap getChildOccurrences()
      {
         return m_childOccurrences;
      }

      public boolean isSequenced()
      {
         return m_isSequenced;
      }

      public void setSequenced(boolean sequenced)
      {
         m_isSequenced = sequenced;
      }

      public List getSequencedChildren()
      {
         return m_childSequence;
      }

      public boolean hasCharacterContent()
      {
         return m_hasCharacterContent;
      }

      public void setHasCharacterContent(boolean hasCharCont)
      {
         m_hasCharacterContent = hasCharCont;
      }

      private String m_name;
      private int m_occurrences;
      private boolean m_hasCharacterContent;
      private boolean m_isSequenced;
      private TreeMap m_childOccurrences;
      private TreeMap m_attributes;
      private List m_childSequence;
   }

   private class ChildSequenceDefinition
   {
      public ChildSequenceDefinition(String name)
      {
         m_name = name;
         m_isRepeatable = false;
         m_isOptional = false;
         m_position = -1;
      }

      public void setName(String name)
      {
         m_name = name;
      }

      public String getName()
      {
         return m_name;
      }

      public void setPosition(int pos)
      {
         m_position = pos;
      }

      public int getPosition()
      {
         return m_position;
      }

      public void setOptional(boolean optional)
      {
         m_isOptional = optional;
      }

      public boolean isOptional()
      {
         return m_isOptional;
      }

      public void setRepeatable(boolean repeatable)
      {
         m_isRepeatable = repeatable;
      }

      public boolean isRepeatable()
      {
         return m_isRepeatable;
      }

      private String m_name;
      private int m_position;
      private boolean m_isRepeatable;
      private boolean m_isOptional;
   }

   private Map m_elMap;
   private List m_elements; // in the order they were added
}
