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
package com.percussion.delivery.metadata.data.impl;

import com.percussion.delivery.metadata.error.PSMalformedMetadataQueryException;
import junit.framework.TestCase;

/**
 * @author erikserating
 *
 */
public class PSCriteriaElementTest extends TestCase
{
   
   public void testParsingValidCriteria() throws Exception
   {
      
      PSCriteriaElement el = null;

      el = new PSCriteriaElement("perc:category IN ('/Categories/Cursos Cortos/Hoteleria, Turismo y Gastronomia/Espania', '/Categories/Cursos Cortos/Hoteleria, Turismo y Gastronomia/Francia', '/Categories/Cursos Cortos/Hoteleria, Turismo y Gastronomia/Peru')");
      assertEquals(el.getName(), "perc:category");
      assertEquals(el.getOperation(), "IN");
      assertEquals(el.getValue(), "'/Categories/Cursos Cortos/Hoteleria, Turismo y Gastronomia/Espania', '/Categories/Cursos Cortos/Hoteleria, Turismo y Gastronomia/Francia', '/Categories/Cursos Cortos/Hoteleria, Turismo y Gastronomia/Peru'");

      el = new PSCriteriaElement("folder='ABC'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "=");
      assertEquals(el.getValue(), "ABC");
      
      el = new PSCriteriaElement("folder = 'ABC'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "=");
      assertEquals(el.getValue(), "ABC");
      
      el = new PSCriteriaElement("folder ='ABC'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "=");
      assertEquals(el.getValue(), "ABC");
      
      el = new PSCriteriaElement("folder= 'ABC'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "=");
      assertEquals(el.getValue(), "ABC");
      
      el = new PSCriteriaElement("folder != 'ABC'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "!=");
      assertEquals(el.getValue(), "ABC");
      
      el = new PSCriteriaElement("folder!='ABC'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "!=");
      assertEquals(el.getValue(), "ABC");
      
      el = new PSCriteriaElement("folder LIKE 'ABC%'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "LIKE");
      assertEquals(el.getValue(), "ABC%");      
      
      el = new PSCriteriaElement("folder like 'ABC%'");
      assertEquals(el.getName(), "folder");
      assertEquals(el.getOperation(), "LIKE");
      assertEquals(el.getValue(), "ABC%");
      
      el = new PSCriteriaElement("perc:template IN ('A','B','C')");
      assertEquals(el.getName(), "perc:template");
      assertEquals(el.getOperation(), "IN");
      assertEquals(el.getValue(), "'A','B','C'");
      
      el = new PSCriteriaElement("perc:template IN (1,2,3)");
      assertEquals(el.getName(), "perc:template");
      assertEquals(el.getOperation(), "IN");
      assertEquals(el.getValue(), "1,2,3");
      
      el = new PSCriteriaElement("perc:template IN (1.8,2.9,3.0)");
      assertEquals(el.getName(), "perc:template");
      assertEquals(el.getOperation(), "IN");
      assertEquals(el.getValue(), "1.8,2.9,3.0");
            
      el = new PSCriteriaElement("somenumber<=23");
      assertEquals(el.getName(), "somenumber");
      assertEquals(el.getOperation(), "<=");
      assertEquals(el.getValue(), "23");
      
      el = new PSCriteriaElement("somenumber   >    5");
      assertEquals(el.getName(), "somenumber");
      assertEquals(el.getOperation(), ">");
      assertEquals(el.getValue(), "5");


   }
   
   public void testParsingMalformedCriteria() throws Exception
   {
     
      PSCriteriaElement el = null;
      
      try
      {
         el = new PSCriteriaElement("perc:template IN ('A,'B',C')");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      try
      {
         el = new PSCriteriaElement("perc:template IN () DELETE *");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      try
      {
         el = new PSCriteriaElement("folder like 'ABC%");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      try
      {
         el = new PSCriteriaElement("folder === '/foo'");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      try
      {
         el = new PSCriteriaElement("folder === 23'");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      try
      {
         el = new PSCriteriaElement("folder");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      try
      {
         el = new PSCriteriaElement(">= 'foo'");
         fail("Tested element should have thrown Malformed Exception.");
      }
      catch (PSMalformedMetadataQueryException ignore){}
      
      
   }
}
