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
package com.percussion.util;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for the PSBaseHttpUtils class
 */
public class PSBaseHttpUtilsTest extends TestCase
{
   public PSBaseHttpUtilsTest(String name)
   {
      super(name);
   }

   public void testRemoveQueryParam()
   {
      String[][] vectors = 
      {
         //url, expected result, param name
         {"a", "a", "x" },
         {"a?", "a?", "x" },
         {"a?m=n", "a?m=n", "x" },
         {"a?x=7", "a", "x" },
         {"a?m=n&x=7", "a?m=n", "x" },
         {"a?m=n&x=7&l=3", "a?m=n&l=3", "X" },
      };
      for (String[] vector : vectors)
      {
         String result = PSBaseHttpUtils.removeQueryParam(vector[0], vector[2]);
         assertEquals(result, vector[1]);
      }
   }
   
   public void testAddQueryParams()
   {
      String[] paths = 
      {
         "a",
         "a?",
         "a?b=",
         "a?b=c",
         "a?b=c&"
      };
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("z", "");
      params.put("x", "y");
      params.put("m", "n");
      for (String path : paths)
      {
         String result = PSBaseHttpUtils.addQueryParams(path, params, false);
         System.out.println(result);
         assertEquals(PSBaseHttpUtils.parseHttpPath(result), "a");
         Map<String, Object> resultParams = PSBaseHttpUtils
               .parseQueryParamsString(result);
         resultParams.remove("b");
         assertEquals(params, resultParams);
      }
      
      params.clear();
      params.put("?x", "y&");
      String result = PSBaseHttpUtils.addQueryParams("a%3f", params, true);
      //the hex digits from encoding must be upper case
      assertEquals(result, "a%3f?%3Fx=y%26");
           
      List<String> values = new ArrayList<String>();
      values.add("1");
      values.add("2");
      values.add("3");
      params.put("l", values);
      result = PSBaseHttpUtils.addQueryParams("a", params, true);
      System.out.println(result);
      assertEquals(params, PSBaseHttpUtils.parseQueryParamsString(result));
   }
   
   public void testParseHttpPath()
   {
      String[][] testData = 
      {
            //test, result
            {"", ""},
            {"a", "a"},
            {"a?", "a"},
            {"a?b", "a"},
      };
      for (String[] data : testData)
      {
         String path = PSBaseHttpUtils.parseHttpPath(data[0]);
         assertEquals(path, data[1]);
      }
   }
   
   @SuppressWarnings("unchecked")
   public void testParseQueryParams() throws Exception
   {
      Map<String, Object> results;
      
      //basic case
      results = PSBaseHttpUtils.parseQueryParamsString("");
      assertNotNull(results);
      assertEquals(results.size(), 0);
      
      results = PSBaseHttpUtils.parseQueryParamsString("?");
      assertNotNull(results);
      assertEquals(results.size(), 0);
      
      results = PSBaseHttpUtils.parseQueryParamsString("a%26c=");
      assertNotNull(results);
      assertEquals(results.size(), 1);
      assertEquals(StringUtils.EMPTY, results.get("a&c"));
      
      results = PSBaseHttpUtils.parseQueryParamsString("abc=&d%3df=");
      assertNotNull(results);
      assertEquals(results.size(), 2);
      assertEquals(StringUtils.EMPTY, results.get("abc"));
      assertEquals(StringUtils.EMPTY, results.get("d=f"));
      
      results = PSBaseHttpUtils.parseQueryParamsString("abc=12&def=34");
      assertNotNull(results);
      assertEquals(results.size(), 2);
      assertEquals("12", results.get("abc"));
      assertEquals("34", results.get("def"));
      
      results = PSBaseHttpUtils.parseQueryParamsString("?abc=12&def=34");
      assertNotNull(results);
      assertEquals(results.size(), 2);
      assertEquals("12", results.get("abc"));
      assertEquals("34", results.get("def"));
      
      results = PSBaseHttpUtils.parseQueryParamsString("foo?abc=12&def=34");
      assertNotNull(results);
      assertEquals(results.size(), 2);
      assertEquals("12", results.get("abc"));
      assertEquals("34", results.get("def"));
      
      results = PSBaseHttpUtils.parseQueryParamsString("abc=&def=44&abc=56");
      assertNotNull(results);
      assertEquals(results.size(), 2);
      List<String> vals = (List<String>) results.get("abc");
      assertEquals(2, vals.size());
      assertEquals(StringUtils.EMPTY, vals.get(0));
      assertEquals("56", vals.get(1));
      assertEquals("44", results.get("def"));
      
      String p1 = "a&o=";
      String v1 = "fo*?o b&ar";
      String param1 = URLEncoder.encode(p1, "UTF8");
      String value1 = URLEncoder.encode(v1, "UTF8");
      results = PSBaseHttpUtils.parseQueryParamsString(param1 + "=" + value1 + "&"
            + value1 + "=" + param1);
      assertEquals(2, results.size());
      assertEquals(v1, results.get(p1));
      assertEquals(p1, results.get(v1));
   }
   
   public void testReadStatusLine() throws Exception
   {
      String[] testStrings = new String[]
      {
         "GET /foo/bar/baz HTTP/1.0\r\nNEXTLINE",
         "GET \uC3BC\uC3BC\uC3BC\uC3BC HTTP1.0\r\nNEXTLINE",
         "GET \uC3BC\uC3BC\uC3BC\uC3BC HTTP1.0\rNEXTLINE",
         "GET \uC3BC\uC3BC\uC3BC\uC3BC HTTP1.0 \rNEXTLINE"
      };

      String[] parseStrings = new String[]
      {
         "GET /foo/bar/baz HTTP/1.0",
         "GET \uC3BC\uC3BC\uC3BC\uC3BC HTTP1.0",
         "GET \uC3BC\uC3BC\uC3BC\uC3BC HTTP1.0",
         "GET \uC3BC\uC3BC\uC3BC\uC3BC HTTP1.0 "
      };

      for (int i = 0; i < testStrings.length; i++)
      {
         ByteArrayInputStream in = 
            new ByteArrayInputStream(testStrings[i].getBytes("UTF-8"));

         PSInputStreamReader rdr = new PSInputStreamReader(in);

         String line = PSBaseHttpUtils.readStatusLine(rdr);
         assertEquals("UTF-8", parseStrings[i], line);

         String nextLine = rdr.readLine("UTF-8");
         assertEquals("UTF-8", "NEXTLINE", nextLine);
      }
   }

   @SuppressWarnings("unchecked")
   public void testParseContentType() throws Exception
   {
      HashMap params = new HashMap();
      String contentType = "application/x-www-form-urlencoded";

      String parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals(contentType, parseType);
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "text/html;foo=bar;bar=baz;baz=car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertTrue("bar".equals(params.remove("foo")));
      assertTrue("baz".equals(params.remove("bar")));
      assertTrue("car".equals(params.remove("baz")));
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "text/html;charset=US-ASCII;bar=baz; foo = car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertTrue("US-ASCII".equals(params.remove("charset")));
      assertTrue("baz".equals(params.remove("bar")));
      assertTrue("car".equals(params.remove("foo")));
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "text/html;";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "text/html ;";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "text/html  ;  ";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "  text/html ;   charset = US-ASCII; bar = baz; foo =   car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertTrue("US-ASCII".equals(params.remove("charset")));
      assertTrue("baz".equals(params.remove("bar")));
      assertTrue("car".equals(params.remove("foo")));
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "  text/html ;   charset = \"US-ASCII\"; bar = baz; foo =   car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertEquals("\"US-ASCII\"", params.remove("charset"));
      assertTrue("baz".equals(params.remove("bar")));
      assertTrue("car".equals(params.remove("foo")));
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "  text/html ;   charset = \"US-ASCII\"; bar =  \" baz   \"; foo =   car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertTrue("\"US-ASCII\"".equals(params.remove("charset")));
      assertEquals("\" baz   \"", params.remove("bar"));
      assertTrue("car".equals(params.remove("foo")));
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "  text/html ;   charset = \"US-;ASCII\"; bar =  \" baz   \"; foo =   car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertTrue("\"US-;ASCII\"".equals(params.remove("charset")));
      assertEquals("\" baz   \"", params.remove("bar"));
      assertTrue("car".equals(params.remove("foo")));
      assertEquals(0, params.size());

      params = new HashMap();
      contentType = "  text/html ;   charset = \"US-;ASCII\"; bar =  \" ba;;z   \"; foo =   car";
      parseType = PSBaseHttpUtils.parseContentType(contentType, params);
      assertEquals("text/html", parseType);
      assertTrue("\"US-;ASCII\"".equals(params.remove("charset")));
      assertEquals("\" ba;;z   \"", params.remove("bar"));
      assertTrue("car".equals(params.remove("foo")));
      assertEquals(0, params.size());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      return suite;
   }
}
