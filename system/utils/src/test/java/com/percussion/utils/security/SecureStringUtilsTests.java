/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.utils.security;

import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;
import com.percussion.security.SecureStringUtils;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
}
