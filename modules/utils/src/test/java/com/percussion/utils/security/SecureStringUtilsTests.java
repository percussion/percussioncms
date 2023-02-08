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

package com.percussion.utils.security;

import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;
import com.percussion.security.SecureStringUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SecureStringUtilsTests {

    //TODO: Fix Me
    @Test
    @Ignore
    public void testHeaderValidation(){
        String cleaned = SecureStringUtils.stripAllLineBreaks("A\n" +
                "Multiline\n" +
                "Test9_8-p\n" +
                "\n" +
                "\n" +
                "\r" +
                "\r\n" +
                "\u000B    " +
                "\u000C " +
                "\u0085 " +
                "\u2028" +
                "\u2029" +
                "Test" +
                "%0d" +
                "%0a" +
                ""
        );

        assertEquals("A Multiline Test9_8-p  Test",cleaned);

        cleaned = SecureStringUtils.stripAllLineBreaks("This%20Is%20An%20ENCODED%0dSTRING%0aWITH_SOME%20");

        assertEquals("This%20Is%20An%20ENCODEDSTRINGWITH_SOME%20",cleaned);
    }


    @Test
    public void testSanitizeSQLTextValue(){

        assertEquals("\';DROP TABLE DUAL;\'",
                SecureStringUtils.sanitizeStringForSQLStatement("';DROP TABLE DUAL;'"));

        assertEquals("Mc\'Donald had a farm.",
                SecureStringUtils.sanitizeStringForSQLStatement("Mc'Donald had a farm."));

        assertEquals("Mc\'Donald had a farm.",
                SecureStringUtils.sanitizeStringForSQLStatement("Mc'Donald had a farm."));
        //Add additional atack patterns here

    }

    @Test
    public void reproUnicodeCompare() throws StringPrepParseException {
        String t1 = "ADM\u0131N";
        String t2 = "adm\u0049n";
        String t3 = "ADM\u0049N";

        assertEquals("ADMIN",t1.toUpperCase());

        String t1NFD = Normalizer2.getNFDInstance().normalize(t1.toUpperCase());
        System.out.println("NFD:" + t1NFD);
        assertEquals("ADMIN",t1NFD);

        String t1NFC = Normalizer2.getNFCInstance().normalize(t1.toUpperCase());
        System.out.println("NFC:" + t1NFC);
        assertEquals("ADMIN",t1NFC);

        String t1NFKCCasefold = Normalizer2.getNFKCCasefoldInstance().normalize(t1);
        System.out.println("NFKCCasefold:" + t1NFKCCasefold);
        assertEquals("ADMIN",t1NFKCCasefold.toUpperCase());

        //NFKD Normalizes the string correctly
        String t1NFKD = Normalizer2.getNFKDInstance().normalize(t1);
        System.out.println("NFKD:" + t1NFKD);
        assertNotEquals("ADMIN",t1NFKD);

        String c = StringPrep.getInstance(StringPrep.RFC4518_LDAP).prepare(t1,StringPrep.DEFAULT);
        System.out.println("Unicode:" + c);
        System.out.println("Plain:" + "ADMIN");

        assertEquals("ADMIN", c.toUpperCase());

    }

    @Test
    public void testCleanURLString(){

        String t1 = SecureStringUtils.stripNonHttpProtocols("HtTp://www.Percussion.com");
        assertEquals("HtTp://www.Percussion.com",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("HtTps://www.percussion.com");
        assertEquals("HtTps://www.percussion.com",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("HtTp://www.percussion.com");
        assertEquals("HtTp://www.percussion.com",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("jar://www.percussion.com/some.jar");
        assertEquals("",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("data://www.percussion.com/some.data");
        assertEquals("",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("file:///etc/passwd");
        assertEquals("",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("file://C\\:/Windows/Users/Administrator/somefile.txt");
        assertEquals("",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("https:///somefeed/i/rss");
        assertEquals("https:///somefeed/i/rss",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("http://somefeed/i/rss");
        assertEquals("http://somefeed/i/rss",t1);


        t1 = SecureStringUtils.stripNonHttpProtocols("http://somefeed/i/rss\r\n");
        assertEquals("http://somefeed/i/rss",t1);

        t1 = SecureStringUtils.stripNonHttpProtocols("https://www.nasa.gov/rss/dyn/breaking_news.rss");
        assertEquals("https://www.nasa.gov/rss/dyn/breaking_news.rss",t1);

    }

    @Test
    public void testValidSQLObjectName(){
        assertNotEquals("<script>alert('');</script>",
                SecureStringUtils.removeInvalidSQLObjectNameCharacters("<script>alert('');</script>"));

        assertNotEquals("IN VALID;",
                SecureStringUtils.removeInvalidSQLObjectNameCharacters("IN VALID;"));

        assertEquals("VALID",
                SecureStringUtils.removeInvalidSQLObjectNameCharacters("VALID"));

        assertEquals("VAL_ID",
                SecureStringUtils.removeInvalidSQLObjectNameCharacters("VAL_ID"));


        assertEquals("VAL1_ID",
                SecureStringUtils.removeInvalidSQLObjectNameCharacters("VAL1_ID"));

        //TODO:  Fix the regex to work with unicode characters
        //assertEquals("Њuni",
        //PSSecurityUtility.removeInvalidSQLObjectNameCharacters("Њuni"));

    }

    @Test
    public void testValidNumericId(){
        assertTrue(SecureStringUtils.isValidNumericId("1234"));
        assertFalse(SecureStringUtils.isValidNumericId("<script>alert('12345');</script>"));
    }

    @Test
    public void testValidGuidId(){
        assertTrue(SecureStringUtils.isValidGuidId("1234-99-89894"));
        assertFalse(SecureStringUtils.isValidGuidId("<script>alert('123-4444-45');</script>"));
    }

    @Test
    public void testValidCMSPath(){
        assertTrue(SecureStringUtils.isValidCMSPathString("/Sites/HWMB/%22%3e%3cscript%3ealert%28595%29%3c%2fscript%3e"));
        assertTrue(SecureStringUtils.isValidCMSPathString("/Sites/www.mysite.com/mypage.html"));
        assertFalse(SecureStringUtils.isValidCMSPathString(
                "/Sites/HWMB/<script>alert(595);</script>"));
    }

}
