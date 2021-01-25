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
package com.percussion.utils.string;

import org.junit.Test;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Unit tests for string utilities.
 */
public class PSStringUtilsTest
{
   
   /**
    * @throws Exception
    */
   @Test
   public void testIndexOfIgnoringCase() throws Exception
   {
      String testString = "abc def ghi";
      String testString2 = "A";
      
      int r;
      
      r = PSStringUtils.indexOfIgnoringCase(testString2, "a", 0);
      assertEquals(0, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "a", 0);
      assertEquals(0, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "a", 1);
      assertEquals(-1, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "b", 1);
      assertEquals(1, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "h", 1);
      assertEquals(9, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "i", 1);
      assertEquals(10, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "GHI", 1);
      assertEquals(8, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "b", 1);
      assertEquals(1, r);
      
      r = PSStringUtils.indexOfIgnoringCase(testString, "AbC", 0);
      assertEquals(0, r);
   }
   
   /**
    * @throws Exception
    */
   @Test
   public void testFolderRootPath() throws Exception
   {
      String testPathA = "/f1/f2";

      assertEquals(testPathA, PSFolderStringUtils
            .getFolderRootPathFromPattern(testPathA));
   }

   /**
    * @throws Exception
    */
   @Test
   public void testFolderRootPath2() throws Exception
   {
      String testPathA = "/f1/%";

      assertEquals("/f1", PSFolderStringUtils
            .getFolderRootPathFromPattern(testPathA));
   }

   /**
    * @throws Exception
    */
   @Test
   public void testFolderRootPath3() throws Exception
   {
      String testPathA = "/%/f2";

      assertEquals("/", PSFolderStringUtils
            .getFolderRootPathFromPattern(testPathA));
   }

   /**
    * @throws Exception
    */
   @Test
   public void testMatch() throws Exception
   {
      Pattern arr[] = PSFolderStringUtils.getFolderPatterns("/f1/%;/f2/%");
      String setA[] = new String[]
      {"/f1/a"};
      assertTrue(PSFolderStringUtils.oneMatched(setA, arr));

      String setB[] = new String[]
      {"/f2/a"};
      assertTrue(PSFolderStringUtils.oneMatched(setB, arr));

      String setC[] = new String[]
      {"/f3/a"};
      assertFalse(PSFolderStringUtils.oneMatched(setC, arr));
   }

   @Test
   public void testCompressWhitespace()
   {
      String a = "   b   c \n\t\fd e   \t f  ";
      String c = PSStringUtils.compressWhitespace(a);
      assertEquals(" b c d e f ", c);

      a = "fee fie     foo fum";
      c = PSStringUtils.compressWhitespace(a);
      assertEquals("fee fie foo fum", c);
   }

   /**
    * Test to make sure that we don't support regular expression, but take
    * the special characters of regular expression literally from the 
    * specified path/pattern.
    * 
    * @throws Exception
    */
   @Test
   public void testNotSupportRegExpression() throws Exception
   {
      // we are not support regular expression
      String path1 = "//Sites/EnterpriseInvestments/Images/People";
      String pattern1 = "//Sites/[A-Z]nterpriseInvestments/Images/People";
      String pattern2 = "//Sites/[C-E]%/Images/People";
      String pattern3 = "//Sites/EnterpriseInvestments/Images/Peopl.";
      String pattern4 = "//Sites/EnterpriseInvestments/Images/Peopl?";

      checkPattern(path1, pattern1, false);
      checkPattern(path1, pattern2, false);
      checkPattern(path1, pattern3, false);
      checkPattern(path1, pattern4, false);
      
      // we take the special character of RegEx literally
      String path2 = "//Sites/[EC]nterpriseInvestments/Images/People";
      String patt2 = "//Sites/[EC]%";
      String path3 = "//Sites/[EC]nterpriseInvestments/Images/Peopl?";
      String patt3 = "//Sites/[EC]%/Peopl?";
      
      checkPattern(path2, patt2, true);
      checkPattern(path3, patt3, true);
   }
   
   /**
    * Validates the given path and pattern.
    * 
    * @param path the tested path, assumed not <code>null</code> or empty.
    * @param pattern the tested pattern, assumed not <code>null</code> or empty.
    * @param bValid expected matching result.
    */
   private void checkPattern(String path, String pattern, boolean bValid)
   {
      String paths[] = new String[]{path};
      Pattern patterns[] = PSFolderStringUtils.getFolderPatterns(pattern);
      if (bValid)
         assertTrue(PSFolderStringUtils.oneMatched(paths, patterns));
      else
         assertFalse(PSFolderStringUtils.oneMatched(paths, patterns));      
   }

   @Test
   public void testListToString()
   {
      List<String> test = new ArrayList<String>();
      assertEquals("", PSStringUtils.listToString(test, ","));

      test.add("a");
      assertEquals("a", PSStringUtils.listToString(test, ","));

      test.add("bb");
      assertEquals("a bb", PSStringUtils.listToString(test, " "));

      test.add("c c");
      assertEquals("a:bb:c c", PSStringUtils.listToString(test, ":"));
      assertEquals("a bb c c", PSStringUtils.listToString(test, " "));
   }

   @Test
   public void testStripQuotes()
   {
      String rval = PSStringUtils.stripQuotes("");
      assertEquals("", rval);

      rval = PSStringUtils.stripQuotes("'abc'");
      assertEquals("abc", rval);

      rval = PSStringUtils.stripQuotes("\"abc\"");
      assertEquals("abc", rval);

      rval = PSStringUtils.stripQuotes("\"abc'");
      assertEquals("\"abc'", rval);

      rval = PSStringUtils.stripQuotes("'abc");
      assertEquals("'abc", rval);

      rval = PSStringUtils.stripQuotes("abc'");
      assertEquals("abc'", rval);

      rval = PSStringUtils.stripQuotes("'a\"");
      assertEquals("'a\"", rval);

      rval = PSStringUtils.stripQuotes("''");
      assertEquals("", rval);

      rval = PSStringUtils.stripQuotes("\"\"");
      assertEquals("", rval);
   }

   @Test
   public void testToCamelCase()
   {
      assertEquals(null, PSStringUtils.toCamelCase(null));
      assertEquals("", PSStringUtils.toCamelCase(""));
      assertEquals("foo", PSStringUtils.toCamelCase("foo"));
      assertEquals("foo", PSStringUtils.toCamelCase("Foo"));
      assertEquals("foo", PSStringUtils.toCamelCase("fOo"));
      assertEquals("foo", PSStringUtils.toCamelCase("FOO"));
      assertEquals("foobar", PSStringUtils.toCamelCase("fooBar"));
      assertEquals("fooBar", PSStringUtils.toCamelCase("foo_bar"));
      assertEquals("fooBar", PSStringUtils.toCamelCase("Foo_Bar"));
      assertEquals("fooBar", PSStringUtils.toCamelCase("FOO_BAR"));
      assertEquals("fooBar", PSStringUtils.toCamelCase("fOO_bAr"));
      assertEquals("foobardoh", PSStringUtils.toCamelCase("fooBarDoh"));
      assertEquals("fooBarDoh", PSStringUtils.toCamelCase("foo_bar_doh"));
      assertEquals("fooBarDoh", PSStringUtils.toCamelCase("fOo_BaR_doH"));
      assertEquals("fooBarDoh", PSStringUtils.toCamelCase("FOO_BAR_DOH"));
      assertEquals("fo,oB.arDoh!", PSStringUtils.toCamelCase("FO,O_B.AR_DOH!"));
   }

   /**
    * @throws Exception
    */
   @Test
   public void testRemoveNonId() throws Exception
   {
      assertEquals("abc", PSStringUtils.replaceNonIdChars("abc"));
      assertEquals("_abc_", PSStringUtils.replaceNonIdChars(" abc "));
      assertEquals("_", PSStringUtils.replaceNonIdChars(" "));
      assertEquals("abc", PSStringUtils.replaceNonIdChars("(abc)"));
      assertEquals("123xyz", PSStringUtils.replaceNonIdChars("123xyz"));
      assertEquals("abc", PSStringUtils.replaceNonIdChars("a';:b.,><c"));
   }

   @Test
   public void testMatchLeftSubstring()
   {
      String paths[] =
      {"aaa", "abb", "abc", "bab"};

      assertEquals("aaa", PSStringUtils.findMatchingLeftSubstring("a", paths));
      assertEquals("abb", PSStringUtils.findMatchingLeftSubstring("ab", paths));
      assertEquals("abc", PSStringUtils.findMatchingLeftSubstring("abc", paths));
      assertEquals("bab", PSStringUtils.findMatchingLeftSubstring("b", paths));
      assertEquals("aaa", PSStringUtils.findMatchingLeftSubstring("aa", paths));
   }

   /**
    * Test the path abbreviator.
    */
   @Test
   public void testPathAbbreviator()
   {
      Font x = new Font("Arial", Font.PLAIN, 12);
      Dimension dim = new Dimension(30, 12);
      String result = PSStringUtils.abbreviatePath(
            "//aaa/bbb/ccc.xyz", dim, x);
      assertEquals(".../ccc.xyz", result);
      
      dim = new Dimension(120, 12);
      result = PSStringUtils.abbreviatePath(
            "//aaa/bbb/ccc.xyz", dim, x);
      assertEquals("//aaa/bbb/ccc.xyz", result);
      
      dim = new Dimension(150, 12);
      result = PSStringUtils.abbreviatePath(
            "//aaaaa/bbbbb/ccccc/ddddd/eeeee.xyz", dim, x);
      assertEquals("//aaaaa/.../ddddd/eeeee.xyz", result);     
      
      dim = new Dimension(200, 12);
      result = PSStringUtils.abbreviatePath(
            "//aaaaa/bbbbb/ccccc/ddddd/eeeee.xyz", dim, x);
      assertEquals("//aaaaa/bbbbb/.../ddddd/eeeee.xyz", result);        
      
   }
   
   /**
    * Test user name validation.
    */
   @Test
   public void testUserNameValidation()
   {
      Character result = PSStringUtils.validateUserName("Jump Rabbit");
      assertEquals(null, result);

      //only invalid character
      assertEquals('*', PSStringUtils.validateUserName("Jump*Rabbit")
         .charValue());
      //multiple invalid characters, should return the first 
      assertEquals('?', PSStringUtils.validateUserName("Jump?*Rabbit")
         .charValue());
   }
   
   /**
    * Test url character validation.
    */
   @Test
   public void testUrlCharacterValidation()
   {
      char[] invalidUrlChars = PSStringUtils.INVALID_NAME_CHARS.toCharArray();
      
      for (int i=0; i < invalidUrlChars.length; i++)
      {
         assertTrue(
               PSStringUtils.containsInvalidNameChars(
                     "Test" + invalidUrlChars[i]));
      }
      
      assertFalse(PSStringUtils.containsInvalidNameChars("Test"));
   }
   
   /**
    * Test make valid for object name.
    */
   @Test
   public void testMakeValidForObjectName()
   {
      char[] invalidUrlChars = PSStringUtils.INVALID_NAME_CHARS.toCharArray();
      String invalid;
      String valid = "Test";
      
      for (int i=0; i < invalidUrlChars.length; i++)
      {
         invalid = "Test" + invalidUrlChars[i];
         assertTrue(PSStringUtils.containsInvalidNameChars(invalid));

         Character ch = PSStringUtils.validate(invalid,
               PSStringUtils.INVALID_NAME_CHARS);
         assertFalse(ch == null);
      }
      
      valid = "Test(1)-_$'.txt";
      assertFalse(PSStringUtils.containsInvalidNameChars(valid));

      Character ch = PSStringUtils.validate(valid,
            PSStringUtils.INVALID_NAME_CHARS);
      assertTrue(ch == null);
   }

   /**
    * Test make valid for Content Type name.
    */
   @Test
   public void testValidForContentTypeName()
   {
      char[] invalidUrlChars = PSStringUtils.INVALID_NAME_CHARS.toCharArray();
      String invalid;
      String valid = "Test123_";

      assertFalse(PSStringUtils.containsInvalidNameChars(valid));
      Character ch = PSStringUtils.validateContentTypeName(valid);
      assertTrue(ch == null);
      
      for (int i=0; i < invalidUrlChars.length; i++)
      {
         invalid = "Test" + invalidUrlChars[i];
         ch = PSStringUtils.validateContentTypeName(invalid);
         assertTrue(ch != null);
         
         valid = PSStringUtils.makeValidContentTypeName(invalid);
         ch = PSStringUtils.validateContentTypeName(valid);
         assertTrue(ch == null);
      }
   }

   @Test
   public void testNameStart()
   {
      char[] invalidNameStart = PSStringUtils.INVALID_NAME_START_CHARS.toCharArray();
      // test invalid name start
      for (char ch : invalidNameStart)
      {
         String name = ch + "test";
         assertFalse( PSStringUtils.validateNameStart(name));
      }
      
      // test valid name start
      for (char ch : invalidNameStart)
      {
         String name = "test" + ch;
         assertTrue( PSStringUtils.validateNameStart(name));
      }
   }

   @Test
   public void testNotBlank_noArgs()
   {
      PSStringUtils.notBlank("a");

      try {
         PSStringUtils.notBlank(null);
         fail();
      }
      catch (IllegalArgumentException expected) {}

      try {
         PSStringUtils.notBlank("");
         fail();
      }
      catch (IllegalArgumentException expected) {}

      try {
         PSStringUtils.notBlank(" \t\n");
         fail();
      }
      catch (IllegalArgumentException expected) {}
   }

   @Test
   public void testNotBlank_withMessage()
   {
      PSStringUtils.notBlank("a", "abc");
      PSStringUtils.notBlank("a", null);

      final String message = "Message 1";
      try {
         PSStringUtils.notBlank(null, message);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
         assertEquals(message, expected.getMessage());
      }

      try {
         PSStringUtils.notBlank(null, null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
         assertTrue(expected.getMessage().length() > 0);
      }
   }
}
