/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.xmldom;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.html.TestPSHtmlCleanerProperties;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequestValidationException;
import com.percussion.testing.PSMockRequestContext;
import com.percussion.util.PSPurgableTempFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the text cleanup extension.
 */
public class TestPSXDTextCleanup {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void testStringCleanup() throws PSExtensionProcessingException, PSAuthorizationException, PSRequestValidationException, PSParameterMismatchException {

        PSXdTextCleanup psXdTextCleanup = new PSXdTextCleanup();


        Object[] params = new Object[]{
                "postBody", //fieldName
                "html-cleaner.properties", // cleaner properties config
                null, //server tags config file
                StandardCharsets.UTF_8.name(), //encoding
                "yes", //disable inline links
                "yes", //use pretty print
        };

        PSMockRequestContext context  = new PSMockRequestContext();

        context.setParameter("postBody","<div class='rxbodyfield'><p>test</p></div>");
        psXdTextCleanup.preProcessRequest(params,context);

        assertNotNull(context.getParameter("postBody"));
        assertEquals("<div class=\"rxbodyfield\"><p>test</p></div>",context.getParameter("postBody"));

        context.setParameter("postBody","<div class='rxbodyfield'><p>test</p></div><div class='rxbodyfield'><b>from 2nd div</b></div>");
        psXdTextCleanup.preProcessRequest(params,context);
        assertEquals("<div class=\"rxbodyfield\"><p>test</p><b>from 2nd div</b></div>",context.getParameter("postBody"));

        //Test some unicode content
        context.setParameter("postBody","<div class='rxbodyfield'><p>test</p></div><div class='rxbodyfield'><b>from 2nd div 😀</b></div>");
        psXdTextCleanup.preProcessRequest(params,context);
        assertEquals("<div class=\"rxbodyfield\"><p>test</p><b>from 2nd div 😀</b></div>",context.getParameter("postBody"));

    }

   @Test
    public void testFileSource() throws IOException, PSExtensionProcessingException, PSAuthorizationException, PSRequestValidationException, PSParameterMismatchException {

       PSXdTextCleanup psXdTextCleanup = new PSXdTextCleanup();

       String text = new Scanner(Objects.requireNonNull(TestPSHtmlCleanerProperties.class.getResourceAsStream("/com/percussion/xmldom/testdocument.html")), "UTF-8").useDelimiter("\\A").next();
       Object[] params = new Object[]{
               "postBody", //fieldName
               "html-cleaner.properties", // cleaner properties config
               null, //server tags config file
               StandardCharsets.UTF_8.name(), //encoding
               "yes", //disable inline links
               "yes", //use pretty print
       };

       PSPurgableTempFile tempFile = new PSPurgableTempFile("test","html",temporaryFolder.getRoot());
        tempFile.setSourceFileName("testdocument.html");
        tempFile.setSourceContentType("text/html");
        try(PrintWriter writer = new PrintWriter(tempFile)){
            writer.print(text);
        }

       PSMockRequestContext context  = new PSMockRequestContext();

       context.setParameter("postBody",tempFile);
       psXdTextCleanup.preProcessRequest(params,context);

       assertNotNull(context.getParameter("postBody"));
       String newText = new Scanner(Objects.requireNonNull(tempFile), "UTF-8").useDelimiter("\\A").next();
       System.out.println(newText);
       assertEquals(text,newText);
    }

    @Before
    public void setup() throws IOException {
        temporaryFolder.create();
    }

    @After
    public void teardown(){
        temporaryFolder.delete();
    }
}
