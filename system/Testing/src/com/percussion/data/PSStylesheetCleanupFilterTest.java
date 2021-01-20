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
package com.percussion.data;

import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayInputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;

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
      ByteArrayInputStream bis = null;
      try
      {
         instance = new PSStylesheetCleanupFilterTest();
         bis = new ByteArrayInputStream(FILTER_XML.getBytes("utf8"));
         Document doc = PSXmlDocumentBuilder.createXmlDocument(bis, false);
         instance.m_filter =
             PSStylesheetCleanupFilter.getInstance();
         instance.m_filter.fromXml(doc.getDocumentElement());
        
      }
      finally
      {
         if(bis != null)
            bis.close();
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