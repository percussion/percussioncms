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
