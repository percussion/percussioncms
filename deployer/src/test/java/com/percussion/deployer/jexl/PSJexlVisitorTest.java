/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
