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
package com.percussion.services.contentmgr.impl.query;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.percussion.services.contentmgr.data.PSQuery;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeComparison;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeConjunction;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeIdentifier;
import com.percussion.services.contentmgr.impl.query.nodes.PSQueryNodeValue;
import com.percussion.services.contentmgr.impl.query.visitors.PSQueryNodePrinter;
import com.percussion.testing.PSTestCompare;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

public class PSQueryParserTest extends TestCase
{
   static String example = "select * from rx:press_release "
         + "where rx:type <> 3 order by rx:title asc";

   public void testSqlSimpleExpression() throws TokenStreamException,
         RecognitionException
   {
      Reader reader = new StringReader(example);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();
      assertEquals(1, q.getSortFields().size());
      assertEquals(1, q.getTypeConstraints().size());
      assertEquals(1, q.getProjection().size());
      assertNotNull(q.getWhere());

      assertEquals("rx:press_release", q.getTypeConstraints().iterator().next()
            .getName());
      assertEquals("rx:title", q.getSortFields().iterator().next().getFirst()
            .getName());
      assertEquals(PSQuery.SortOrder.ASC, q.getSortFields().iterator().next()
            .getSecond());
      IPSQueryNode clause = q.getWhere();
      assertTrue(clause instanceof PSQueryNodeComparison);
      PSQueryNodeComparison c = (PSQueryNodeComparison) clause;
      assertEquals("id(rx:type)", c.getLeft().toString());
      assertEquals(IPSQueryNode.Op.NE, c.getOp());
      assertEquals(new PSQueryNodeValue(3L), c.getRight());
   }

   static String example2 = "select rx:title, rx:count, rx:url from rx:press_release "
         + "where rx:count > 3";

   public void testSqlComplexProjection() throws RecognitionException,
         TokenStreamException
   {
      Reader reader = new StringReader(example2);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();
      assertEquals(1, q.getTypeConstraints().size());
      assertEquals(3, q.getProjection().size());
      assertNotNull(q.getWhere());

      assertEquals("rx:title", q.getProjection().get(0).getName());
      assertEquals("rx:count", q.getProjection().get(1).getName());
      assertEquals("rx:url", q.getProjection().get(2).getName());

      IPSQueryNode clause = q.getWhere();
      assertTrue(clause instanceof PSQueryNodeComparison);
      PSQueryNodeComparison c = (PSQueryNodeComparison) clause;
      assertEquals("id(rx:count)", c.getLeft().toString());
      assertEquals(IPSQueryNode.Op.GT, c.getOp());
      assertEquals(new PSQueryNodeValue(3L), c.getRight());
   }

   static String example3 = "select * from rx:generic,rx:press_release,rx:brief "
         + "where rx:contentStartDate < '10/3/99' and rx:title like 'john%smith'";

   public void testSqlMoreStuff() throws RecognitionException,
         TokenStreamException
   {
      Reader reader = new StringReader(example3);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();
      assertEquals(3, q.getTypeConstraints().size());
      assertEquals(1, q.getProjection().size());
      assertNotNull(q.getWhere());

      assertEquals("rx:generic", q.getTypeConstraints().get(0).getName());
      assertEquals("rx:press_release", q.getTypeConstraints().get(1).getName());
      assertEquals("rx:brief", q.getTypeConstraints().get(2).getName());

      IPSQueryNode clause = q.getWhere();
      assertTrue(clause instanceof PSQueryNodeConjunction);
      PSQueryNodeConjunction c = (PSQueryNodeConjunction) clause;
      IPSQueryNode left = c.getLeft();
      IPSQueryNode right = c.getRight();
      assertEquals(IPSQueryNode.Op.AND, c.getOp());

      assertTrue(left instanceof PSQueryNodeComparison);
      assertTrue(right instanceof PSQueryNodeComparison);

      PSQueryNodeComparison leftc = (PSQueryNodeComparison) left;
      PSQueryNodeComparison rightc = (PSQueryNodeComparison) right;

      assertEquals("id(rx:contentStartDate)", leftc.getLeft().toString());
      assertEquals(IPSQueryNode.Op.LT, leftc.getOp());
      assertEquals(new PSQueryNodeValue("10/3/99"), leftc.getRight());

      assertEquals("id(rx:title)", rightc.getLeft().toString());
      assertEquals(IPSQueryNode.Op.LIKE, rightc.getOp());
      assertEquals(new PSQueryNodeValue("john%smith"), rightc.getRight());
   }

   static String example4 = "select * from nt:root "
         + "where rx:a > 3 and rx:b < 4.5 or rx:c = -1 and rx:d < -.5";

   public void testSqlNumeric() throws Exception
   {
      Reader reader = new StringReader(example4);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();

      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());

      PSTestCompare
            .assertEqualIgnoringWhitespace(
                  "{ { rx:a GT 3 AND rx:b LT 4.5 } OR { rx:c EQ -1 AND rx:d LT -0.5 } } ",
                  pp);
   }

   static String example5 = "select * from nt:root "
         + "where not rx:a > :avar and jcr:contains(rx:b,'foo')";

   public void testSqlNot() throws Exception
   {
      Reader reader = new StringReader(example5);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();

      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ { NOT rx:a GT :avar } AND jcr:contains([id(rx:b), foo]) }\n", pp);
   }

   static String example6 = "select * from nt:root where not rx:a > rx:b";

   public void testSqlTwoProperties() throws Exception
   {
      Reader reader = new StringReader(example6);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();

      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace("{ NOT rx:a GT rx:b }\n", pp);
   }

   static String example7 = "select * from nt:root "
         + "where not rx:a is null and rx:b is not null";

   public void testSqlNullTest() throws Exception
   {
      Reader reader = new StringReader(example7);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();

      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ { NOT rx:a EQ <<null>> } AND rx:b NE <<null>> }", pp);
   }

   static String example8 = "select * from nt:root "
         + "where (not rx:a is null and rx:b is not null) and "
         + "(rx:c < 3 or (rx:d > 5 and rx:e = 10))";

   public void testSqlNestedExpressions() throws Exception
   {
      Reader reader = new StringReader(example8);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();

      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ { { NOT  rx:a EQ <<null>> } AND  rx:b NE <<null>> } AND"
                  + " { rx:c LT 3 OR { rx:d GT 5 AND  rx:e EQ 10 } } }", pp);
   }

   static String example9 = "select rx:displaytitle from rx:generic "
         + "where rx:sys_contentStartDate > '10/1/99' and "
         + " jcr:path like '//Sites/EnterpriseInvestments/%'";

   public void testSqlPathExpression() throws Exception
   {
      Reader reader = new StringReader(example9);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();

      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ rx:sys_contentStartDate GT 10/1/99 AND"
                  + "  jcr:path LIKE //Sites/EnterpriseInvestments/% }", pp);
   }
   
   static String example10 = "select * from rx:press_release "
      + "where jcr:path like '/sites/enterpriseinvestments/%' and ( "
      + "jcr:primaryType = 'rx:press_release' and "
      + "rx:type <> 3 or rx:bar = 'foo' ) order by rx:title asc";
   
   public void testSqlComplex() throws Exception
   {
      Reader reader = new StringReader(example10);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ jcr:path LIKE /sites/enterpriseinvestments/% AND " +
            "{ { jcr:primaryType EQ rx:press_release AND rx:type NE 3 } " +
            "OR rx:bar EQ foo } }", pp);
   }
   
   static String example11 = "select rx:sys_contentid, rx:sys_folderid " +
         "from rx:file,rx:image,rx:navimage " +
         "where jcr:path like \'//Sites/EnterpriseInvestments%\' " +
         "and not jcr:path like \'//Sites/EnterpriseInvestments/Images/CreditCard%\'";
   
   public void testSqlCompoundNot() throws Exception
   {
      Reader reader = new StringReader(example11);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere()); 
      PSTestCompare.assertEqualIgnoringWhitespace("{ " +
            "jcr:path LIKE //Sites/EnterpriseInvestments% AND {" + 
            "  NOT jcr:path LIKE //Sites/EnterpriseInvestments/Images/CreditCard%" + 
            "  } } ", pp);
   }
   
   static String example12 = "select * from rx:generic,rx:press_release,rx:brief "
         + "where rx:contentStartDate < '10/3/99' and rx:title like 'john%áéíóúÁÉÍÓÚñöü'";

   public void testSqlSpecialCharacters() throws RecognitionException,
         TokenStreamException
   {
      Reader reader = new StringReader(example12);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);
      PSQuery q = parser.start_rule();
      assertEquals(3, q.getTypeConstraints().size());
      assertEquals(1, q.getProjection().size());
      assertNotNull(q.getWhere());

      assertEquals("rx:generic", q.getTypeConstraints().get(0).getName());
      assertEquals("rx:press_release", q.getTypeConstraints().get(1).getName());
      assertEquals("rx:brief", q.getTypeConstraints().get(2).getName());

      IPSQueryNode clause = q.getWhere();
      assertTrue(clause instanceof PSQueryNodeConjunction);
      PSQueryNodeConjunction c = (PSQueryNodeConjunction) clause;
      IPSQueryNode left = c.getLeft();
      IPSQueryNode right = c.getRight();
      assertEquals(IPSQueryNode.Op.AND, c.getOp());

      assertTrue(left instanceof PSQueryNodeComparison);
      assertTrue(right instanceof PSQueryNodeComparison);

      PSQueryNodeComparison leftc = (PSQueryNodeComparison) left;
      PSQueryNodeComparison rightc = (PSQueryNodeComparison) right;

      assertEquals("id(rx:contentStartDate)", leftc.getLeft().toString());
      assertEquals(IPSQueryNode.Op.LT, leftc.getOp());
      assertEquals(new PSQueryNodeValue("10/3/99"), leftc.getRight());

      assertEquals("id(rx:title)", rightc.getLeft().toString());
      assertEquals(IPSQueryNode.Op.LIKE, rightc.getOp());
      assertEquals(new PSQueryNodeValue("john%áéíóúÁÉÍÓÚñöü"), rightc.getRight());
   }

   static String xpath1 = "//foo/bar";

   public void testXpathSimple() throws Exception
   {
      Reader reader = new StringReader(xpath1);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace("jcr:path LIKE /%/foo/bar\n",
            pp);

      List<PSQueryNodeIdentifier> types = q.getTypeConstraints();
      assertTrue(types.size() > 0);
      PSQueryNodeIdentifier type = types.get(0);
      assertTrue(type.getName().equals("nt:base"));
   }

   static String xpath2 = "/jcr:root/foo/bar[@rx:a > :var]";

   public void testXpathAttrs() throws Exception
   {
      Reader reader = new StringReader(xpath2);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      assertNotNull(q.getWhere());
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ jcr:path LIKE //foo/bar AND rx:a GT :var }", pp);
   }

   static String xpath3 = "/jcr:root/foo/bar/element(*, rx:generic)";

   public void testXpathElements() throws Exception
   {
      Reader reader = new StringReader(xpath3);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String rep = q.getWhere().toString();
      assertEquals("qn-compare(id(jcr:path),LIKE,//foo/bar/%)", rep);
      String pr = q.getProjection().toString();
      assertEquals("[id(*)]", pr);
      String types = q.getTypeConstraints().toString();
      assertEquals("[id(rx:generic)]", types);
   }

   static String xpath4 = "//element(n, *)/@rx:x order by @rx:b descending";

   public void testXpathSortOrder() throws Exception
   {
      Reader reader = new StringReader(xpath4);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String rep = q.getWhere().toString();
      assertEquals("qn-compare(id(jcr:path),LIKE,/%/n)", rep);
      String pr = q.getProjection().toString();
      assertEquals("[id(rx:x)]", pr);
      String types = q.getTypeConstraints().toString();
      assertEquals("[id(*)]", types);
      assertEquals("rx:b", q.getSortFields().iterator().next().getFirst()
            .getName());
      assertEquals(PSQuery.SortOrder.DSC, q.getSortFields().iterator().next()
            .getSecond());

   }

   static String xpath5 = "(@rx:x | @rx:y)";

   public void testXpathJustProjection() throws Exception
   {
      Reader reader = new StringReader(xpath5);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      assertNull(q.getWhere());
      String proj = q.getProjection().toString();
      PSTestCompare.assertEqualIgnoringWhitespace(proj, "[id(rx:x), id(rx:y)]");
   }

   static String xpath6 = "[@rx:x < 6]";

   public void testXpathJustWhere() throws Exception
   {
      Reader reader = new StringReader(xpath6);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace("rx:x LT 6\n", pp);
   }

   static String xpath7 = "[(@rx:x < 7 and @rx:y >= 9) or @rx:z = 3 and (@rx:a = @rx:b or @rx:a = @rx:z)]";

   public void testXpathJustComplex() throws Exception
   {
      Reader reader = new StringReader(xpath7);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ { rx:x LT 7 AND rx:y GE 9 } OR { rx:z EQ 3 AND " +
            "{ rx:a EQ rx:b OR rx:a EQ rx:z } } }", pp);
   }

   static String xpath8 = "[@rx:x < 6](@rx:a)";

   public void testXpathProjectionAndWhere() throws Exception
   {
      Reader reader = new StringReader(xpath8);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace("rx:x LT 6\n", pp);
      String proj = q.getProjection().toString();
      PSTestCompare.assertEqualIgnoringWhitespace("[id(rx:a)]", proj);
   }

   static String xpath9 = "[@rx:x < 6](@rx:a | @rx:b | @rx:c)";

   public void testXpathLongerProjection() throws Exception
   {
      Reader reader = new StringReader(xpath9);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace("rx:x LT 6\n", pp);
      String proj = q.getProjection().toString();
      PSTestCompare.assertEqualIgnoringWhitespace(
            "[id(rx:a), id(rx:b), id(rx:c)]", proj);
   }

   static String xpath10 = "/sites/enterpriseinvestments/element(*,rx:press_release)"
         + "[@jcr:primaryType = 'rx:press_release'"
         + " and @rx:type != 3"
         + " or @rx:bar = 'foo'] order by @rx:title ascending";

   public void testXpathComplex() throws Exception
   {
      Reader reader = new StringReader(xpath10);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);
      PSQuery q = parser.start_rule();
      String pp = PSQueryNodePrinter.prettyPrint(q.getWhere());
      PSTestCompare.assertEqualIgnoringWhitespace(
            "{ jcr:path LIKE /sites/enterpriseinvestments/% AND " +
            "{ { jcr:primaryType EQ rx:press_release AND rx:type NE 3 } " +
            "OR rx:bar EQ foo } }", pp);
   }
}
