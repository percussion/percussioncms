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
package com.percussion.data;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

/**
 *  Test class used to test methods in <code>PSStylesheetCleanupFilter</code>
 */
@Category(IntegrationTest.class)
public class PSStylesheetCleanupFilterTest{

   public PSStylesheetCleanupFilterTest()
   {
   }
  
   @BeforeClass
   public static  void setupFilter() throws Exception
   {
      initFilter();  
   }

   @Test
   public void testIsNSDeclarationAllowed()
   {
      assertTrue(
         !m_filter.isNSDeclarationAllowed("jsp","http://java.sun.com/jsp"));
      assertTrue(
         m_filter.isNSDeclarationAllowed("", "http://www.w3.org/1999/xhtml"));
      assertTrue(
         !m_filter.isNSDeclarationAllowed("", "http://www.w3.org/1999/foo"));
   }

   @Test
   public void testIsNSAttributeAllowed()
   {
      assertTrue(m_filter.isNSAttributeAllowed("","foo"));
      assertTrue(m_filter.isNSAttributeAllowed("xml", "lang"));
      assertTrue(m_filter.isNSAttributeAllowed("xml", "space"));
      assertTrue(!m_filter.isNSAttributeAllowed("xml", "bar"));
      assertTrue(!m_filter.isNSAttributeAllowed("x", "foo"));
      assertTrue(m_filter.isNSAttributeAllowed("jsp", "page"));      
   }

   @Test
   public void testIsNSElementAllowed()
   {
      assertTrue(m_filter.isNSElementAllowed("","foo"));
      assertTrue(!m_filter.isNSElementAllowed("xml", "bar"));
      assertTrue(!m_filter.isNSElementAllowed("x", "foo"));
      assertTrue(m_filter.isNSElementAllowed("jsp", "include"));
   }
   

   /**
    * Creates the <code>PSStylesheetCleanupFilter<code> object to be used
    * by the tests.
    * @throws Exception on IO error
    */
   private static void initFilter() throws Exception
   {

      try(ByteArrayInputStream bis = new ByteArrayInputStream(FILTER_XML.getBytes(StandardCharsets.UTF_8)))
      {
         instance = new PSStylesheetCleanupFilterTest();
         Document doc = PSXmlDocumentBuilder.createXmlDocument(bis, false);
         instance.m_filter =
             PSStylesheetCleanupFilter.getInstance();
         instance.m_filter.fromXml(doc.getDocumentElement());
        
      }
   }

   static PSStylesheetCleanupFilterTest instance;
   /**
    * The filter that will be used by the tests. Initialized in
    * {@link #initFilter()} which is called by {@link #setUp()}.
    * Should not be <code>null</code> after that.
    */
   private PSStylesheetCleanupFilter m_filter;
   
   /**
    *  Test xml filter
    */
   private static final String FILTER_XML = 
      "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
      "<stylesheetCleanupFilter>" +
         "<allowedNamespace name=\"\" declAllowed=\"true \" declValue=\"*xhtml*\">" +
            "<allowedElement name=\"*\"/>" +
            "<allowedAttribute name=\"*\"/>" +
         "</allowedNamespace>" +
         "<allowedNamespace name=\"xml\" declAllowed=\"false \">" +
            "<allowedAttribute name=\"lang\"/>" +
            "<allowedAttribute name=\"space\"/>" +
         "</allowedNamespace>" +         
         "<allowedNamespace name=\"jsp\" declAllowed=\"false \">" +
            "<allowedElement name=\"*\"/>" +
            "<allowedAttribute name=\"*\"/>" +
         "</allowedNamespace>" +        
      "</stylesheetCleanupFilter>";

}
