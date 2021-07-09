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

import com.percussion.utils.tools.IPSUtilsConstants;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * This file contains tests for both the document builder
 * and the tree walker.
 */
public class PSXmlDocumentBuilderTest extends TestCase
{
   public PSXmlDocumentBuilderTest(String name)
   {
      super(name);
   }

   public void testCreateBooksWithDtd()
      throws IOException, org.xml.sax.SAXException
   {
      internalTestBooks(true);
   }

   public void testCreateBooksWithoutDtd()
      throws IOException, org.xml.sax.SAXException
   {
      internalTestBooks(false);
   }

   protected void internalTestBooks(boolean withDtd)
      throws IOException, org.xml.sax.SAXException
   {
      StringBuffer buf = new StringBuffer();
      
      if (withDtd)
         buf.append(ms_bookListDtd);
      
      buf.append("<BookList>\n");
      for (int i = 0; i < m_books.size(); i++)
      {
         buf.append(((Book)(m_books.get(i))).toXmlString());
      }
      buf.append("</BookList>");
      StringReader docStringReader = new StringReader(buf.toString());
      Document doc;
      if (withDtd)
         doc = PSXmlDocumentBuilder.createXmlDocument(docStringReader, true);
      else
         doc = PSXmlDocumentBuilder.createXmlDocument(docStringReader, false);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      
      assertEquals("the root should be the initial current node",
         walker.getCurrent(), doc.getDocumentElement());

      int i = 0;
      for (Element el = walker.getNextElement("Book", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         el != null;
         el = walker.getNextElement("Book", PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS))
      {
         String title = walker.getElementData("title", false);
         String isbn = walker.getElementData("isbn", false);
         
         Element authorEl = walker.getNextElement("author", PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         String author = walker.getElementData(authorEl);
         String authorId = authorEl.getAttribute("id");

         assertEquals(new Book(title, isbn, author, authorId),
            m_books.get(i++));

         // pop
         walker.setCurrent(el);
      }
      assertEquals("Did we get all the books?", i, m_books.size());

      performSerialization(doc);
   }

   public void performSerialization(Document doc)
      throws IOException, org.xml.sax.SAXException
   {
      // write it out, read it back in
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PSXmlDocumentBuilder.write(doc, out);
      byte[] bytes = out.toByteArray();
      ByteArrayInputStream in = new ByteArrayInputStream(bytes);

      Document serDoc = PSXmlDocumentBuilder.createXmlDocument(in, false);

      // then write out what we read in
      out = new ByteArrayOutputStream();
      PSXmlDocumentBuilder.write(serDoc, out);
      bytes = out.toByteArray();
      in = new ByteArrayInputStream(bytes);

      Document serDoc2 = PSXmlDocumentBuilder.createXmlDocument(in, false);

      assertDocEquals(serDoc, serDoc2);
   }
    
    public void testCopyTree() throws Exception
    {
       StringReader r = new StringReader("<document><a><b/></a></document>");
       Document d = PSXmlDocumentBuilder.createXmlDocument();
       Document td = PSXmlDocumentBuilder.createXmlDocument(r, false);
       d.appendChild(d.createElement("document"));
       NodeList nl = td.getDocumentElement().getElementsByTagName("a");
       Node n = PSXmlDocumentBuilder.copyTree(d, d.getDocumentElement(), nl.item(0), true);
       assertDocEquals(d, td);
       assertNotNull(n);
       assertNotSame(n, nl.item(0));
       // Test null handling
       n = PSXmlDocumentBuilder.copyTree(d, d.getDocumentElement(), null, false);
       assertNull(n);
    }

   // recursive document comparison
   public void assertDocEquals(Document a, Document b)
   {
      assertEquals(a.getDocumentElement(), b.getDocumentElement());
   }

   // recursive node comparison
   public void assertEquals(Node a, Node b)
   {
      if (a instanceof Element)
         ((Element)a).normalize();

      if (b instanceof Element)
         ((Element)b).normalize();

      assertEquals(a.getNodeType(), b.getNodeType());
      assertEquals(a.getNodeName(), b.getNodeName());

      NamedNodeMap Aattrs = a.getAttributes();
      NamedNodeMap Battrs = b.getAttributes();
      if (Aattrs == null || Battrs == null)
      {
         assertTrue(Aattrs == null);
         assertTrue(Battrs == null);
      }
      else
      {
         assertAttrs(Aattrs, Battrs);
      }

      String Aval = a.getNodeValue();
      String Bval = b.getNodeValue();

      if (Aval == null || Bval == null)
      {
         assertTrue(Aval == null);
         assertTrue(Bval == null);
      }
      else
      {
         assertEquals(Aval.trim(), Bval.trim());
      }

      NodeList Akids = a.getChildNodes();
      NodeList Bkids = b.getChildNodes();
      assertEquals(Akids.getLength(), Bkids.getLength());

      for (int i = 0; i < Akids.getLength(); i++)
      {
         assertEquals(Akids.item(i), Bkids.item(i));
      }
   }

   // attributes comparison
   public void assertAttrs(NamedNodeMap a, NamedNodeMap b)
   {
      // make a copy of all the A attributes, then ensure they
      // map 1:1 to the B attributes
      HashMap<String,String> aNodes = new HashMap<String,String>();
      for (int i = 0; i < a.getLength(); i++)
      {
         Attr att = (Attr)a.item(i);
         aNodes.put(att.getName(), att.getValue());
      }

      for (int i = 0; i < b.getLength(); i++)
      {
         Attr att = (Attr)b.item(i);
         String aVal = (String)aNodes.remove(att.getName());
         assertTrue(aVal != null);
         assertEquals(aVal, att.getValue());
      }

      assertEquals(aNodes.size(), 0);
   }

   public void setUp()
   {
      m_books = new ArrayList<Book>();
      m_books.add(new Book("\"The Power and the Glory\"", "12345'6789'", "&Graham \"Greene", "1"));
      m_books.add(new Book("Our Man in Havana", "223456<789>", "Graham Greene", "1"));
      m_books.add(new Book("The Man Within", "323456789", "Graham Greene", "1"));
      m_books.add(new Book("<The Inhe<<ri>tor>>s", "423&4567'\"89", "William Golding", "2"));
      m_books.add(new Book("The Honourable Schoolboy", "523456789", "John le Carre", "3"));
      m_books.add(new Book("All Quiet on the Western Front", "623456789",
         "Erich Maria Remarque", "4"));
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite(PSXmlDocumentBuilderTest.class);
      return suite;
   }

   private static final String ms_bookListDtd =
      "<?xml version=\"1.0\" encoding=\"" +
      IPSUtilsConstants.RX_STANDARD_ENC + "\" standalone=\"yes\" ?>\n" +
      "<!DOCTYPE BookList [\n" +
      "\t<!ELEMENT BookList (Book*)>\n" +
      "\t<!ELEMENT Book (title, isbn, author)>\n" +
      "\t<!ELEMENT title (#PCDATA)>\n" +
      "\t<!ELEMENT isbn (#PCDATA)>\n" +
      "\t<!ELEMENT author (#PCDATA)>\n" +
      "\t<!ATTLIST author id CDATA #REQUIRED>\n" +
      "]>";

   private ArrayList<Book> m_books;

   protected class Book implements Comparable
   {
      public Book(String title, String isbn, String author, String authorId)
      {
         m_title = title;
         m_isbn = isbn;
         m_author = author;
         m_authorId = authorId;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Book)) return false;
         Book book = (Book) o;
         return Objects.equals(m_title, book.m_title) &&
                 Objects.equals(m_isbn, book.m_isbn) &&
                 Objects.equals(m_author, book.m_author) &&
                 Objects.equals(m_authorId, book.m_authorId);
      }

      @Override
      public int hashCode() {
         return Objects.hash(m_title, m_isbn, m_author, m_authorId);
      }

      public int compareTo(Object o)
      {
         Book b = (Book)o;
         int compare = m_title.compareTo(b.m_title);
         if (compare != 0)
            return compare;
         compare = m_isbn.compareTo(b.m_isbn);
         if (compare != 0)
            return compare;
         compare = m_authorId.compareTo(b.m_authorId);
         if (compare != 0)
            return compare;
         compare = m_author.compareTo(b.m_author);
         if (compare != 0)
            return compare;
         return 0;
      }

      public String toXmlString()
      {
         StringBuffer buf = new StringBuffer("<Book>\n\t<title>");
         buf.append(PSXmlTreeWalker.convertToXmlEntities(m_title));
         buf.append("</title>\n");
         buf.append("\t<isbn>");
         buf.append(PSXmlTreeWalker.convertToXmlEntities(m_isbn));
         buf.append("</isbn>\n");
         buf.append("\t<author id=\"");
         buf.append(PSXmlTreeWalker.convertToXmlEntities(m_authorId));
         buf.append("\">");
         buf.append(PSXmlTreeWalker.convertToXmlEntities(m_author));
         buf.append("</author>\n</Book>");
         return buf.toString();
      }

      private String m_title;
      private String m_isbn;
      private String m_author;
      private String m_authorId;
   }
}
