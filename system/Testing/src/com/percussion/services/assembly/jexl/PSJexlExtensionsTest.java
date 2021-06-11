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
package com.percussion.services.assembly.jexl;

import com.percussion.services.assembly.data.PSAssemblyWorkItem;
import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.utils.jsr170.PSProperty;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.Test;

import javax.jcr.Property;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test jexl extensions
 * 
 * @author dougrand
 */
public class PSJexlExtensionsTest
{

   private static final Logger log = LogManager.getLogger(PSJexlExtensionsTest.class);
   /**
    * Velocity engine eis initialized in the static block, never
    * <code>null</code> afterward
    */
   public static VelocityEngine ms_engine = null;

   static
   {
      ms_engine = new VelocityEngine();
      try
      {
         ms_engine.init();
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Get a precomputed velocity context with the jexl extensions registered.
    * 
    * @return the context, never <code>null</code>
    */
   public VelocityContext getContext()
   {
      VelocityContext rval = new VelocityContext();
      Map<String, Object> sys = new HashMap<String, Object>();
      rval.put("sys", sys);

      sys.put("string", new PSStringUtils());
      sys.put("db", new PSDbUtils());
      sys.put("doc", new PSDocumentUtils());
      sys.put("guid", new PSGuidUtils());
      sys.put("codec", new PSCodecUtils());
      sys.put("link", new PSLinkUtils());
      sys.put("cond", new PSCondUtils());
      return rval;
   }

   /**
    * Run the velocity engine. We use this here to run most of the tests
    * 
    * @param ctx the context, from {@link #getContext()}
    * @param template the template to run, never <code>null</code> or empty
    * @return the result
    * @throws ParseErrorException
    * @throws MethodInvocationException
    * @throws ResourceNotFoundException
    * @throws IOException
    */
   public String run(VelocityContext ctx, String template)
         throws ParseErrorException, MethodInvocationException,
         ResourceNotFoundException, IOException
   {
      if (StringUtils.isBlank(template))
      {
         throw new IllegalArgumentException("template may not be null or empty");
      }
      StringWriter out = new StringWriter();
      ms_engine.evaluate(ctx, out, "Velo", template);
      return out.toString();
   }

   /**
    * Perform the test by running the template and comparing the actual and
    * expected results
    * 
    * @param ctx the context, never <code>null</code>
    * @param inputtemplate the input template, never <code>null</code> or
    *           empty
    * @param expectedoutput the expected output
    * @throws ParseErrorException
    * @throws MethodInvocationException
    * @throws ResourceNotFoundException
    * @throws IOException
    */

   public void doTest(VelocityContext ctx, String inputtemplate,
         String expectedoutput) throws ParseErrorException,
         MethodInvocationException, ResourceNotFoundException, IOException
   {
      String out = run(ctx, inputtemplate);
      assertEquals(expectedoutput, out);
   }

   /**
    * @throws Exception
    */
   @Test
   public void testStringUtils() throws Exception
   {
      VelocityContext ctx = getContext();
      doTest(ctx, "$sys.string.extractNumber(\"44\")", "44");
      doTest(ctx, "$sys.string.equalNumbers(null,0)", "true");
      doTest(ctx, "$sys.string.extractFieldName(\"a:foo\")", "foo");
      doTest(ctx, "$sys.string.stringToMap(\"aa=bb&c=d\")", "{aa=bb, c=d}");
      doTest(ctx, "$sys.string.stringToMap(\"c=d\")", "{c=d}");
      doTest(ctx, "$sys.string.stripSpaces(\" a    b   c      \")", "a b c");
   }

   /**
    * @throws Exception
    */
   public void testCombineMap() throws Exception
   {
      PSAssemblerUtils a = new PSAssemblerUtils();

      Map<String, String[]> input = new HashMap<String, String[]>();
      String extra = "b=1&c=2&c=3&d=4";
      Map<String, Object> output;

      input.put("a", new String[]
      {"0", "1"});
      input.put("b", new String[]
      {"0", "1"});
      input.put("e", new String[]
      {"5"});

      output = a.combine(input, extra);

      Object vals;

      vals = output.get("a");
      checkEquality(vals, new String[]
      {"0", "1"});
      vals = output.get("b");
      checkEquality(vals, new String[]
      {"1"});
      vals = output.get("c");
      checkEquality(vals, new String[]
      {"2", "3"});
      vals = output.get("d");
      checkEquality(vals, new String[]
      {"4"});
      vals = output.get("e");
      checkEquality(vals, new String[]
      {"5"});
      
      // Test error detection
      try
      {
         a.combine(input, "a=b=c");
         throw new Exception();
      }
      catch(Exception e)
      {
         // Good!
      }
      
      try
      {
         a.combine(input, "d");
        throw new Exception();
      }
      catch(Exception e)
      {
         // Good!
      }
   }

   /**
    * Check objects for equality. If the objects are arrays, then they are
    * checked element by element for equality
    * 
    * @param a the first object, may be <code>null</code>
    * @param b the second object, may be <code>null</code>
    */
   void checkEquality(Object a, Object b)
   {
      if (a == null && b == null)
         return;

     assertTrue(a == null || b == null);


     assertTrue(a.getClass().equals(b.getClass()));


      if (a.getClass().isArray())
      {
         Object[] aarr = (Object[]) a;
         Object[] barr = (Object[]) b;

         assertTrue(aarr.length != barr.length);


         for (int i = 0; i < aarr.length; i++)
         {
            checkEquality(aarr[i], barr[i]);
         }
      }
      else
      {
         assertEquals(a, b);
      }
   }

   /**
    * @throws Exception
    */
   @Test
   public void testBodyPageBreakUtils() throws Exception
   {
      PSContentNode parent = new PSContentNode(null, "parent", null, null,
            null, null);
      PSPaginateUtils pager = new PSPaginateUtils();
      
      final String content = "<div class=\"rxbodyfield\">"
         + "adipiscing elit. <![CDATA[ <hello>\n&<?pageBreak?> ]]><?test abc?><!-- comment text --><em>Praesent ullamcorper.</em> Pellentesque <h2><font>elementum<?pageBreak?>"
         + "turpis id justo. <h1 attr=\"foo\"><font>D<em>u</em>is</font> adipiscing</h1>, orci non placerat <em>congue,<?pageBreak?>"
         + "malesuada est</em> et diam. Nunc sit amet lacus sit</font></h2> amet lacus varius<em><?pageBreak ?>"
         + "volutpat.</em> Sed molestie pharetra sem. Sed dictum. Fusce <em>elementum.<?pageBreak  ?>"
         + "</em>Etiam non orci non felis &lt;foo&gt; feugiat lobortis.<?pageBreak ?>"
         + "abc <foo attrib1='x\"y\"' attrib2=\"&amp;&lt;&apos;&quot;\"></foo>  <br></br> <br/> <br />"
         + "<h1/> <h2/> <textarea/>"
         + "&apos;&amp;yyy&apos;"
         + "</div>";
      Property p = new PSProperty("content", parent, content);
      int pageCount = pager.fieldContentPageCount(p).intValue();
      final int expectedPageCount = 6;
      assertEquals(expectedPageCount, pageCount);
      
      String[] testResults = 
      {
         "<div class=\"rxbodyfield\">"
            + "adipiscing elit. <![CDATA[ <hello>\n&<?pageBreak?> ]]><?test abc?><!-- comment text --><em>Praesent ullamcorper.</em> Pellentesque <h2><font>elementum</font></h2></div>",
         
         "<div class=\"rxbodyfield\">"
            + "<h2><font>turpis id justo. <h1 attr=\"foo\"><font>D<em>u</em>is</font> adipiscing</h1>, orci non placerat <em>congue,</em></font></h2></div>",
         
         "<div class=\"rxbodyfield\">"
            + "<h2><font><em>malesuada est</em> et diam. Nunc sit amet lacus sit</font></h2> amet lacus varius<em></em></div>",
               
         "<div class=\"rxbodyfield\">"
            + "<em>volutpat.</em> Sed molestie pharetra sem. Sed dictum. Fusce <em>elementum.</em></div>",

         "<div class=\"rxbodyfield\">"
            + "<em></em>Etiam non orci non felis &lt;foo> feugiat lobortis.</div>",
            
         "<div class=\"rxbodyfield\">"
            + "abc <foo attrib1=\"x&quot;y&quot;\" attrib2=\"&amp;&lt;'&quot;\" />  <br /> <br /> <br />"
            + "<h1></h1> <h2></h2> <textarea></textarea>"
            + "'&amp;yyy'</div>"
      };
      
      for (int i = 1; i <= pageCount; i++)
      {
         String page = pager.getFieldPage(p, i);
         //verify the returned doc is valid xml
         PSXmlDocumentBuilder.createXmlDocument(new StringReader(page), false);
         String s = pager.getFieldPage(p, i);
         assertEquals(testResults[i-1], s);
      }
      
      final String empty = "<?pageBreak?>a<?pageBreak?>bb<?pageBreak?>";
      p = new PSProperty("content", parent, empty);
      assertEquals(4,pager.fieldContentPageCount(p));
      assertEquals("", pager.getFieldPage(p, 1));
      assertEquals("a", pager.getFieldPage(p, 2));
      assertEquals("bb", pager.getFieldPage(p, 3));
      assertEquals("", pager.getFieldPage(p, 4));
      //should be no error if requesting extra pages
      assertEquals("", pager.getFieldPage(p, 5));
      
      p = new PSProperty("content", parent, "");
      assertEquals(0,pager.fieldContentPageCount(p));
      assertEquals("", pager.getFieldPage(p, 1));
      
      final String shortstr = "how now brown cow";
      p = new PSProperty("content", parent, shortstr);
      assertEquals(1,pager.fieldContentPageCount(p));      
      assertEquals(shortstr, pager.getFieldPage(p, 1));
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void testCodecUtils() throws Exception
   {
      VelocityContext ctx = getContext();
      String input = run(ctx, "$sys.codec.base64Encoder(\"\u00E2\u00E3\u00E4\u00E5\");");
      //      "4uPk5Q==");
      doTest(ctx, "$sys.codec.base64Decoder(\"" + input + "\")",
            "\u00E2\u00E3\u00E4\u00E5");
      doTest(ctx, "$sys.codec.decodeFromXml(\"&#160;\'&lt;&gt;&amp;\")",
            "\u00A0\'<>&");
      doTest(ctx, "$sys.codec.escapeForXml(\"\u00A0\'<>&\")",
            "&#160;&apos;&lt;&gt;&amp;");
   }

   /**
    * 
    */
   public static final String testHtmlDoc = "<html><head>"
         + "<title>Test title</title>"
         + "</head><body><p>test body</p></body></html>";

   /**
    * @throws Exception
    */
   @Test
   public void testDocUtils() throws Exception
   {
      VelocityContext ctx = getContext();
      ctx.put("doc", testHtmlDoc);
      PSAssemblyWorkItem item = new PSAssemblyWorkItem();
      item.setResultData(testHtmlDoc.getBytes());
      item.setMimeType("text/html");
      ctx.put("result", item);

      doTest(ctx, "$sys.doc.extractBody($result)", "<p>test body</p>");
      doTest(ctx, "$sys.doc.extractBody($doc)", "<p>test body</p>");
      String out = run(ctx, "$sys.doc.getDocument('https://www.percussion.com')");
      assertTrue(out.length() > 0);
   }

   /**
    * @throws Exception
    */
   @Test
   public void testLinkUtils() throws Exception
   {
      VelocityContext ctx = getContext();
      ctx.put("url", "http://www.google.com/foo/bar");
      ctx.put("url2", "http://www.google.com/foo/bar?param1=value1");

      doTest(ctx, "$sys.link.addParams($url,'p1','v1')",
            "http://www.google.com/foo/bar?p1=v1");
      doTest(ctx, "$sys.link.addParams($url2,'p2','v2')",
            "http://www.google.com/foo/bar?param1=value1&p2=v2");
      doTest(ctx, "$sys.link.addParams($url,'p1','v1','p2','v2')",
            "http://www.google.com/foo/bar?p1=v1&p2=v2");
      doTest(ctx, "$sys.link.addParams($url,'p1','v1','p2','v2','p3','v3')",
            "http://www.google.com/foo/bar?p1=v1&p2=v2&p3=v3");
      doTest(
            ctx,
            "$sys.link.addParams($url,'p1','v1','p2','v2','p3','v3','p4','v4')",
            "http://www.google.com/foo/bar?p1=v1&p2=v2&p3=v3&p4=v4");
      doTest(ctx,
            "$sys.link.addParams($url,'p1','v1','p2','v2','p3','v3','p4','v4',"
                  + "'p5','v5')",
            "http://www.google.com/foo/bar?p1=v1&p2=v2&p3=v3&p4=v4&p5=v5");
      // Try nesting
      doTest(ctx, "$sys.link.addParams("
            + "$sys.link.addParams($url,'p1','v1','p2','v2'),'p3','v3'" + ")",
            "http://www.google.com/foo/bar?p1=v1&p2=v2&p3=v3");

   }

   /**
    * @throws Exception
    */
   @Test
   public void testCondUtils() throws Exception
   {
      VelocityContext ctx = getContext();

      ctx.put("a", true);
      ctx.put("b", 2);
      ctx.put("c", 3);
      ctx.put("f", false);

      doTest(ctx, "$sys.cond.choose($a,$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose($f,$b,$c)", "3");
      doTest(ctx, "#set($x=$b>1)\n$sys.cond.choose($x,$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose('y',$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose('t',$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose('yes',$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose('true',$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose('1',$b,$c)", "2");
      doTest(ctx, "$sys.cond.choose('0',$b,$c)", "3");
      doTest(ctx, "$sys.cond.choose('0',\"abc\",\"def\")", "def");
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void testMapList() throws Exception
   {
      List<Map<String,Object>> input = new ArrayList<Map<String,Object>>();
      
      input.add(getMapListEntry("x", new Integer(1)));
      input.add(getMapListEntry("x", new Integer(2)));
      input.add(getMapListEntry("x", "3"));
      input.add(getMapListEntry("y", "4"));
      
      PSAssemblerUtils autils = new PSAssemblerUtils();
      
      List<String> result = autils.mapValues(input, "x");
      assertEquals("[1, 2, 3, ]", result.toString());
   }

   /**
    * Create a single element map
    * @param key the key to store the object under, assumed never <code>null</code> or empty
    * @param object the object, assumed never <code>null</code>
    * @return the map, never <code>null</code>
    */
   private Map<String, Object> getMapListEntry(String key, Object object)
   {
      Map<String, Object> rval = new HashMap<String, Object>();
      rval.put(key, object);
      return rval;
   }
}
