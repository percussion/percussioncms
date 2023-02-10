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
