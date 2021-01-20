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

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

/**
 * This is a unit test for the PSXmlTreeWalker class.
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXmlTreeWalkerTest extends TestCase
{
   public void testBookWalker()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "BookList");

      for (int i = 0; i < m_books.size(); i++)
      {
         ((Book)m_books.get(i)).addToDocument(doc, root);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      
      assertEquals("the root should be the initial current node",
         walker.getCurrent(), doc.getDocumentElement());

      int i = 0;
      for (Element el = walker.getNextElement("Book", true, true);
         el != null;
         el = walker.getNextElement("Book", true, true))
      {
         String fromCur = walker.getElementData("title", false);
         String fromRelCur = walker.getElementData("./title", false);
         assertEquals("title should match ./title",
            fromCur, fromRelCur);

         fromCur = walker.getElementData("isbn", false);
         fromRelCur = walker.getElementData("./isbn", false);
         assertEquals("isbn should match ./isbn",
            fromCur, fromRelCur);

         fromCur = walker.getElementData("author", false);
         fromRelCur = walker.getElementData("./author", false);
         assertEquals("author should match ./author",
            fromCur, fromRelCur);

         fromCur = walker.getElementData("author/@id", false);
         fromRelCur = walker.getElementData("./author/@id", false);
         assertEquals("author/@id should match ./author/@id",
            fromCur, fromRelCur);

         // if we're on the first run, then getting the data from the
         // parent should also equal the current data
         if (i == 0) {
            fromCur = walker.getElementData("title", false);
            fromRelCur = walker.getElementData("../Book/title", false);
            assertEquals("title should match ../Book/title",
               fromCur, fromRelCur);

            fromCur = walker.getElementData("isbn", false);
            fromRelCur = walker.getElementData("../Book/isbn", false);
            assertEquals("isbn should match ../Book/isbn",
               fromCur, fromRelCur);

            fromCur = walker.getElementData("author", false);
            fromRelCur = walker.getElementData("../Book/author", false);
            assertEquals("author should match ../Book/author",
               fromCur, fromRelCur);

            fromCur = walker.getElementData("author/@id", false);
            fromRelCur = walker.getElementData("../Book/author/@id", false);
            assertEquals("author/@id should match ../Book/author/@id",
               fromCur, fromRelCur);
         }

         i++;
      }

      assertEquals("Did we get all the books?", i, m_books.size());
   }

   public void setUp()
   {
      m_books = new java.util.ArrayList<Book>();
      m_books.add(new Book("The Power and the Glory", "123456789", "Graham Greene", "1"));
      m_books.add(new Book("Our Man in Havana", "223456789", "Graham Greene", "1"));
      m_books.add(new Book("The Man Within", "323456789", "Graham Greene", "1"));
      m_books.add(new Book("The Inheritors", "423456789", "William Golding", "2"));
      m_books.add(new Book("The Honourable Schoolboy", "523456789", "John le Carre", "3"));
      m_books.add(new Book("All Quiet on the Western Front", "623456789",
         "Erich Maria Remarque", "4"));
   }
   
   /**
    * This tests the static method getBaseElement for the following cases:
    * <table border="1">
    * <tr><th>currentBase</th><th>xmlField</th><th>expected result</th></tr>
    * <tr><td>root/foo/bar</td><td>root/foo/bar</td><td>root/foo/bar<td></tr>
    * <tr><td>root/foo/bar</td><td>root/foo/bar_1</td><td>root/foo<td></tr>
    * <tr><td>root/foo/bar_1</td><td>root/foo/bar</td><td>root/foo<td></tr>
    * <tr><td>null</td><td>root/foo/bar</td><td>root/foo<td></tr>
    * <tr><td>root/foo/bar</td><td>null</td><td>root/foo/bar<td></tr>
    * <tr><td>null</td><td>null</td><td>null<td></tr>
    * <tr><td>root/foo1/bar</td><td>root/foo/bar</td><td>root<td></tr>
    * <tr><td>root/foo/bar</td><td>root/foo1/bar</td><td>root<td></tr>
    * <tr><td>/root/foo/bar</td><td>/root/foo1/bar</td><td>/root<td></tr>
    * <tr><td>/root1/foo/bar</td><td>/root/foo/bar</td><td>null<td></tr>
    * <tr><td>/root/foo/bar</td><td>root/foo1/bar</td><td>null<td></tr>
    * <tr><td>root/foo/bar</td><td>/root/foo1/bar</td><td>null<td></tr>
    * </table>
    */
   public void testGetBaseElement()
   {
      String currentBase = "root/foo/bar";
      String xmlField = "root/foo/bar";
      String base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      String oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root/foo/bar"));
      assertTrue(base.equals(oldBase));
      
      // this case tests the fix for bug Rx-04-04-0026
      currentBase = "root/foo/bar";
      xmlField = "root/foo/bar_1";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root/foo"));
      assertTrue(!base.equals(oldBase));

      // this case tests the fix for bug Rx-04-04-0026
      currentBase = "root/foo/bar_1";
      xmlField = "root/foo/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root/foo"));
      assertTrue(!base.equals(oldBase));

      currentBase = null;
      xmlField = "root/foo/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root/foo"));
      assertTrue(base.equals(oldBase));

      currentBase = "root/foo/bar";
      xmlField = null;
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root/foo/bar"));
      assertTrue(base.equals(oldBase));

      currentBase = null;
      xmlField = null;
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      // this case throws a null pointer in the old implementation
      // oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base == null);

      currentBase = "root/foo1/bar";
      xmlField = "root/foo/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root"));
      assertTrue(base.equals(oldBase));

      currentBase = "root/foo/bar";
      xmlField = "root/foo1/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("root"));
      assertTrue(base.equals(oldBase));

      currentBase = "/root/foo/bar";
      xmlField = "/root/foo1/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base.equals("/root"));
      assertTrue(base.equals(oldBase));

      currentBase = "/root1/foo/bar";
      xmlField = "/root/foo/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base == null);
      assertTrue(base == oldBase);

      currentBase = "/root/foo/bar";
      xmlField = "root/foo1/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base == null);
      assertTrue(base == oldBase);

      currentBase = "root/foo/bar";
      xmlField = "/root/foo1/bar";
      base = PSXmlTreeWalker.getBaseElement(currentBase, xmlField);
      oldBase = getBaseElement(currentBase, xmlField);
      assertTrue(base == null);
      assertTrue(base == oldBase);
   }
   
   static String ms_expectedSer = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<BookList>\n" + 
        "   <Book>\n" + 
        "      <title>The Power and the Glory</title>\n" + 
        "      <isbn>123456789</isbn>\n" + 
        "      <author id=\"1\">Graham Greene</author>\n" + 
        "   </Book>\n" + 
        "   <Book>\n" + 
        "      <title>Our Man in Havana</title>\n" + 
        "      <isbn>223456789</isbn>\n" + 
        "      <author id=\"1\">Graham Greene</author>\n" + 
        "   </Book>\n" + 
        "   <Book>\n" + 
        "      <title>The Man Within</title>\n" + 
        "      <isbn>323456789</isbn>\n" + 
        "      <author id=\"1\">Graham Greene</author>\n" + 
        "   </Book>\n" + 
        "   <Book>\n" + 
        "      <title>The Inheritors</title>\n" + 
        "      <isbn>423456789</isbn>\n" + 
        "      <author id=\"2\">William Golding</author>\n" + 
        "   </Book>\n" + 
        "   <Book>\n" + 
        "      <title>The Honourable Schoolboy</title>\n" + 
        "      <isbn>523456789</isbn>\n" + 
        "      <author id=\"3\">John le Carre</author>\n" + 
        "   </Book>\n" + 
        "   <Book>\n" + 
        "      <title>All Quiet on the Western Front</title>\n" + 
        "      <isbn>623456789</isbn>\n" + 
        "      <author id=\"4\">Erich Maria Remarque</author>\n" + 
        "   </Book>\n" + 
        "</BookList>";
   
   /**
    * Test the serialization 
    * @throws IOException 
    */
   public void testSerialization() throws IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "BookList");

      for (int i = 0; i < m_books.size(); i++)
      {
         ((Book)m_books.get(i)).addToDocument(doc, root);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      
      StringWriter w = new StringWriter();
      walker.write(w);
      
      assertEquals(ms_expectedSer, w.toString());
   }

   /**
    * This was the old implementation for the getBaseElement method. This is
    * used to test the backwards compatibility.
    * See {@link PSXmlTreeWalker.getBaseElement(String, String)} for parameter
    * descriptions.
    */
   private String getBaseElement(String curBase, String xmlField)
   {
      int baseLen = (curBase == null) ? 0 : curBase.length();
      int fldLen = (xmlField == null) ? 0 : xmlField.length();

      if ((curBase == null) || (baseLen == 0)) {
         int pos = xmlField.lastIndexOf('/');
         if (pos != -1)
            return xmlField.substring(0, pos);
         else
            return xmlField;
      }
      else if ((xmlField == null) || (fldLen == 0))
         return curBase;

      char[] chars1 = curBase.toCharArray();

      char[] chars2 = xmlField.toCharArray();
      char ch;

      int pos = 0;
      int lastElementPos = 0;
      for (; (pos < baseLen) && (pos < fldLen); pos++)
      {
         ch = chars1[pos];
         if (ch != chars2[pos])
            break;
         else if (ch == '/')
            lastElementPos = pos;
      }

      /*  No bug Id!  Removed logic which stated
          that we have a match when baseLen == fldLen, which is not
          true!  This was evident when doing a POST with xml
          with two xml fields with the bases but with
          different element names of the same length! */
      if (pos == baseLen)  // don't truncate if it's an exact match
         return curBase;
      else if (pos == fldLen)    // same here, don't truncate
         return xmlField;

      if (lastElementPos <= 0)
         return null;

      return new String(chars1, 0, lastElementPos);
   }

   private java.util.List<Book> m_books;

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
         buf.append(m_title);
         buf.append("</title>\n");
         buf.append("\t<isbn>");
         buf.append(m_isbn);
         buf.append("</isbn>\n");
         buf.append("\t<author id=\"");
         buf.append(m_authorId);
         buf.append("\">");
         buf.append(m_author);
         buf.append("</author>\n</Book>");
         return buf.toString();
      }

      public Element addToDocument(Document doc, Element root)
      {
         Element book = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, "Book");

         PSXmlDocumentBuilder.addElement(doc, book, "title", m_title);

         PSXmlDocumentBuilder.addElement(doc, book, "isbn", m_isbn);

         Element author = PSXmlDocumentBuilder.addElement(
            doc, book, "author", m_author);
         author.setAttribute("id", m_authorId);

         return book;
      }

      private String m_title;
      private String m_isbn;
      private String m_author;
      private String m_authorId;
   }
}
