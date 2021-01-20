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
package com.percussion.design.objectstore;

import com.percussion.design.objectstore.server.PSValidatorAdapter;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Unit tests for the PSConditional class
 */
public class PSConditionalTest extends TestCase
{
   public PSConditionalTest(String name)
   {
      super(name);
   }

   public void testToFromXml() throws Exception
   {
      // create a PSConditional with a binary op and a value
      {
         PSTextLiteral var = new PSTextLiteral("foo");
         PSTextLiteral val = new PSTextLiteral("bar");
         PSConditional cond = new PSConditional(var, "=", val, "AND");
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element condEl = cond.toXml(doc);
         PSConditional fromCond = new PSConditional();
         fromCond.fromXml(condEl, null, null);
         assertEquals(cond, fromCond);
      }

      // create a PSConditional with a unary op and no value
      {
         PSTextLiteral var = new PSTextLiteral("foo");
         PSConditional cond = new PSConditional(var, "IS NULL", null, "AND");
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element condEl = cond.toXml(doc);
         PSConditional fromCond = new PSConditional();
         fromCond.fromXml(condEl, null, null);
         assertEquals(cond, fromCond);
      }

   }

   public void testValueValidation() throws Exception
   {
      PSValidatorAdapter validator = new PSValidatorAdapter(null);

      // create a PSConditional with a binary op and a value
      {
         PSTextLiteral var = new PSTextLiteral("foo");
         PSTextLiteral val = new PSTextLiteral("bar");
         PSConditional cond = new PSConditional(var, "=", val, "AND");
         cond.validate(validator);
      }

      // create a PSConditional with a unary op and no value
      {
         PSTextLiteral var = new PSTextLiteral("foo");
         PSConditional cond = new PSConditional(var, "IS NULL", null, "AND");
         cond.validate(validator);
      }

      // create a PSConditional with a binary op but no value
      {
         PSTextLiteral var = new PSTextLiteral("foo");
         PSConditional cond = new PSConditional(var, "=", null, "AND");
         boolean didThrow = false;
         try
         {
            cond.validate(validator);
         }
         catch (PSValidationException e)
         {
            didThrow = true;
         }
         assertTrue("Caught binary op with no value?", didThrow);
      }
   }

   public void testUdfCall() throws Exception
   {
      /*
      // create PSUdfExit object
      String className = "com.percussion.exit.PSJavaScriptUdfExitHandler";
      String udfName = "MyUdfMethod";
      String body = "return 123456789";
      PSUdfExit udfExit = new PSUdfExit(className, udfName, body);
      
      // create PSExtensionParamDef object
      PSExtensionParamDef paramDef = new PSExtensionParamDef("paramName", "String");
      String clsName = "com.percussion.design.objectstore.PSExtensionParamDef";
      PSCollection paramDefCollect = new PSCollection(clsName);
      paramDefCollect.add(paramDef);
      udfExit.setParamDefs(paramDefCollect);
      udfExit.setApplicationContext("MyUdfTest");
      udfExit.setDescription("Always return 123456789");
      udfExit.setVersion("");
      
      // create PSExitParamValue object(s) and PSExitParamValue[]
      PSBackEndTable beTable = new PSBackEndTable("myTableAlias");
      // Note: we cannot call beTable.setXXX methods at here
      PSBackEndColumn beColumn = new PSBackEndColumn(beTable, "psudoColumn");
      PSExitParamValue paramValueOne = new PSExitParamValue(beColumn);
      PSExitParamValue[] paramArray  = new PSExitParamValue[1];
      paramArray[0] = paramValueOne;
      
      // create PSUdfCall object
      PSUdfCall udfCall = new PSUdfCall(udfExit, paramArray);
      
      // create PSConditional object
      PSConditional condOne = new PSConditional(beColumn, "=", udfCall, "AND");
      PSConditional condTwo = new PSConditional(udfCall, "=", beColumn, "AND");
      
      // see whether udfCall is stored/retrieved successfully by using "get" methods
      PSUdfCall variable = (PSUdfCall)condTwo.getVariable();
      PSUdfCall value    = (PSUdfCall)condOne.getValue();
      assertEquals(udfCall, variable);
      assertEquals(udfCall, value);
      
      // see whether udfCall is stored/retrieved successfully by using to/fromXml methods
      PSApplication application = new PSApplication("psudoApp");
      String udfClassName = "com.percussion.design.objectstore.PSUdfExit";
      PSCollection udfCollect = new PSCollection(udfClassName);
      udfCollect.add(udfExit);
      application.setApplicationUdfs(udfCollect);
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element condEl = condOne.toXml(doc);
      PSConditional fromCondOne = new PSConditional();
      fromCondOne.fromXml(condEl, application, null);
      if (condOne.equals(fromCondOne)){  // convenient to debug
         String isSame = "same";
      }
      assertEquals(condOne, fromCondOne);
      
      condEl = condTwo.toXml(doc);
      PSConditional fromCondTwo = new PSConditional();
      fromCondTwo.fromXml(condEl, application, null);
      if (condTwo.equals(fromCondTwo)){  // convenient to debug
         String isSame = "same";
      }
      assertEquals(condTwo, fromCondTwo);
   */
   }

   public void testEquals() throws Exception
   {
      // Create a number of situations with two PSCondition objects to see if 
      // hashCode is working correctly
      
      // Back end table
      PSBackEndTable table = new PSBackEndTable("foo");
      PSBackEndTable table2 = new PSBackEndTable("bar");
      doEqualsTest(new PSBackEndColumn(table, "x"), new PSBackEndColumn(table, "x"));
      doNotEqualsTest(new PSBackEndColumn(table2, "x"), new PSBackEndColumn(table, "x"));
      doNotEqualsTest(new PSBackEndColumn(table, "x"), new PSBackEndColumn(table, "y"));
      
      // Back end column
      PSConditional a = new PSConditional();
      a.setVariable(new PSBackEndColumn(table, "x"));
      a.setOperator("=");
      a.setValue(new PSBackEndColumn(table, "y"));
      PSConditional b = new PSConditional();
      b.setVariable(new PSBackEndColumn(table, "x"));
      b.setOperator("=");
      b.setValue(new PSBackEndColumn(table, "y"));      
      doEqualsTest(a, b);
      b.setValue(new PSBackEndColumn(table, "z"));
      doNotEqualsTest(a, b);
      
      // url request
      PSCollection params1 = new PSCollection(PSParam.class);
      params1.add(new PSParam("a", new PSTextLiteral("b")));
      PSCollection params2 = new PSCollection(PSParam.class);
      params2.add(new PSParam("b", new PSTextLiteral("b")));
      PSCollection params3 = new PSCollection(PSParam.class);
      params2.add(new PSParam("a", new PSTextLiteral("c")));
            
      PSUrlRequest req = new PSUrlRequest("name", "href", params1);
      PSUrlRequest req2 = new PSUrlRequest("name", "href", params1);
      PSUrlRequest req3 = new PSUrlRequest("name1", "href", params1);
      PSUrlRequest req4 = new PSUrlRequest("name", "href1", params1);
      PSUrlRequest req5 = new PSUrlRequest("name", "href1", params2);
      PSUrlRequest req6 = new PSUrlRequest("name", "href1", params3);
      doEqualsTest(req, req2);
      doNotEqualsTest(req, req3);
      doNotEqualsTest(req, req4);
      doNotEqualsTest(req, req5);
      doNotEqualsTest(req, req6);
      
      a.setValue(req);
      b.setValue(req2);
      doEqualsTest(a, b);
      b.setValue(req3);
      doNotEqualsTest(a, b);
      
      // PSExtensionCall
      PSExtensionCall call = new PSExtensionCall();
      call.setExtensionRef(new PSExtensionRef("cat", "handler", "ctx", "name"));
      PSExtensionCall call2 = new PSExtensionCall();
      call2.setExtensionRef(new PSExtensionRef("cat", "handler", "ctx", "name"));
      PSExtensionCall call4 = new PSExtensionCall();
      call4.setExtensionRef(new PSExtensionRef("cat", "handler2", "ctx", "name"));
      PSExtensionCall call5 = new PSExtensionCall();
      call5.setExtensionRef(new PSExtensionRef("cat", "handler", "ctx3", "name"));
      PSExtensionCall call6 = new PSExtensionCall();
      call6.setExtensionRef(new PSExtensionRef("cat", "handler", "ctx", "name4"));
                        
      doEqualsTest(call, call2);
      doNotEqualsTest(call, call4);
      doNotEqualsTest(call, call5);
      doNotEqualsTest(call, call6);   
      
      PSConditional c1 = create(call, "=", call2);
      PSConditional c2 = create(call, "=", call2);
      PSConditional c3 = create(call, "=", call4);
      doEqualsTest(c1, c2);
      doNotEqualsTest(c1, c3);  
      
      // PSDisplayFieldRef
      PSDisplayFieldRef ref = new PSDisplayFieldRef("ref");
      PSDisplayFieldRef ref1 = new PSDisplayFieldRef("ref");
      PSDisplayFieldRef ref2 = new PSDisplayFieldRef("ref2");
      
      doEqualsTest(ref, ref1);
      doNotEqualsTest(ref, ref2);
      
      c1 = create(ref, "=", ref1);
      c2 = create(ref, "=", ref1);
      c3 = create(ref, "=", ref2);
      
      doEqualsTest(c1, c2);
      doNotEqualsTest(c1, c3);
      
      // PSDisplayTestLiteral
      PSDisplayTextLiteral dtl1 = new PSDisplayTextLiteral("disp1", "val1");
      PSDisplayTextLiteral dtl2 = new PSDisplayTextLiteral("disp1", "val1");
      PSDisplayTextLiteral dtl3 = new PSDisplayTextLiteral("disp2", "val1");
      PSDisplayTextLiteral dtl4 = new PSDisplayTextLiteral("disp1", "val2");
      
      doEqualsTest(dtl1, dtl2);
      doNotEqualsTest(dtl1, dtl3);
      doNotEqualsTest(dtl1, dtl4);
      
      c1 = create(ref, "=", dtl1);
      c2 = create(ref, "=", dtl2);
      c3 = create(ref, "=", dtl3);

      doEqualsTest(c1, c2);
      doNotEqualsTest(c1, c3);      
      
      // PSNamedReplacementValue (PSCookie is a subclass that is not abstract)
      PSNamedReplacementValue val1 = new PSCookie("name");
      PSNamedReplacementValue val2 = new PSCookie("name");
      PSNamedReplacementValue val3 = new PSCookie("name2");
      
      doEqualsTest(val1, val2);
      doNotEqualsTest(val1, val3);
      
      c1 = create(ref, "=", val1);
      c2 = create(ref, "=", val2);
      c3 = create(ref, "=", val3);

      doEqualsTest(c1, c2);
      doNotEqualsTest(c1, c3);         
      
      // PSTextLiteral
      PSTextLiteral lit1 = new PSTextLiteral("literal");
      PSTextLiteral lit2 = new PSTextLiteral("literal");
      PSTextLiteral lit3 = new PSTextLiteral("literala");

      doEqualsTest(lit1, lit2);
      doNotEqualsTest(lit1, lit3);

      c1 = create(ref, "=", lit1);
      c2 = create(ref, "=", lit2);
      c3 = create(ref, "=", lit3);

      doEqualsTest(c1, c2);
      doNotEqualsTest(c1, c3);
   }
   
   public PSConditional create(IPSReplacementValue var, String op, IPSReplacementValue val)
   throws PSIllegalArgumentException
   {
      PSConditional rval = new PSConditional(var, op, val);
      
      return rval;
   }
   
   public void doEqualsTest(Object a, Object b)
   {
      assertTrue("The hash values for class " + a.getClass() + " were not equal when they should have been",
         a.hashCode() == b.hashCode());
         
      assertTrue("The values for class " + a.getClass() + " were not equal when they should have been",
         a.equals(b));         
   }

   public void doNotEqualsTest(Object a, Object b)
   {
      assertTrue("The hash values for class " + a.getClass() + " were equal when they shouldn't have been",
          a.hashCode() != b.hashCode());
          
      assertTrue("The values for class " + a.getClass() + " were equal when they shouldn't have been",
                a.equals(b) == false);                
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSConditionalTest("testValueValidation"));
      suite.addTest(new PSConditionalTest("testToFromXml"));
      suite.addTest(new PSConditionalTest("testUdfCall"));
      suite.addTest(new PSConditionalTest("testEquals"));
      
      return suite;
   }
}
