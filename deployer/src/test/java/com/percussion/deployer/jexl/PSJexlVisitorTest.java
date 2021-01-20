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
package com.percussion.deployer.jexl;

import com.percussion.utils.testing.UnitTest;
import org.apache.commons.jexl3.parser.ParserVisitor;
import org.apache.commons.jexl3.parser.SimpleNode;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 * @author vamsinukala
 * 
 */
// public class PrintJexlVisitor implements ParserVisitor
@Category(UnitTest.class)
public class PSJexlVisitorTest
{

   @Test
   public void testScript() throws Exception
   {
      String code = "while (x < 10) x = x + 1;";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createScriptNode(code);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      ((PSGetIDsJexlVisitor)visitor).printIDs();
      assertEquals(2, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testMultiLineScript() throws Exception
   {
      String code = "y = x * 12 + 44; y = y * 4;";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createScriptNode(code);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      ((PSGetIDsJexlVisitor)visitor).printIDs();
      assertEquals(3, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testForEach() throws Exception
   {
      String code = "for (item : list){ 1+1;}";
      PSGetIDsJexlVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,false);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(2, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testBlock() throws Exception
   {
      String code = "if (val > 100) { x = '200'; y = '300';}";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,false);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(3, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testExpression() throws Exception
   {
      String code = "x + 1;";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,false);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(1, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testOrExpression() throws Exception
   {
      String code = "x || y";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code, true);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(0, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testAndExpression() throws Exception
   {
      String code = "x&&y";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,true);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(0, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testMethod() throws Exception
   {
      String code = "$rx.codec.decodeFromXml(\"222\", \"301\",\"222\", 123);";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,false);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(4, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testBitwiseORExpression() throws Exception
   {
      String code = "2 | 1";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,true);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(2, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testBitwiseXorExpression() throws Exception
   {
      String code = "33 ^ 4";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,true);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(2, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

   @Test
   public void testBitwiseAndExpression() throws Exception
   {
      String code = "2&1";
      ParserVisitor visitor = new PSGetIDsJexlVisitor();
      PSJexlSimpleNode psExp = PSJexlParserUtils.createNewExpression(code,true);
      SimpleNode exp = psExp.getNode();
      exp.childrenAccept(visitor, exp);
      assertEquals(2, ((PSGetIDsJexlVisitor)visitor).getIds().size());
   }

}
