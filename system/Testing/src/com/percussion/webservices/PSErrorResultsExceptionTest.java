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
package com.percussion.webservices;

import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link PSErrorResultsException} class.
 */
@Category(IntegrationTest.class)
public class PSErrorResultsExceptionTest
{
   /**
    * Test all contracts. 
    */
   @Test
   public void testContracts() throws Exception
   {
      IPSGuidManager manager = PSGuidManagerLocator.getGuidMgr();
      IPSGuid guid_1 = manager.createGuid(PSTypeEnum.SLOT);
      IPSGuid guid_2 = manager.createGuid(PSTypeEnum.SLOT);
      IPSGuid guid_3 = manager.createGuid(PSTypeEnum.SLOT);
      
      PSTemplateSlot result_1 = new PSTemplateSlot();
      result_1.setName("slot");
      PSErrorException error_1 = new PSErrorException(1, "message", "stack");
      PSLockErrorException error_2 = new PSLockErrorException(2, "message", 
         "stack");
      error_2.setLocker("locker");
      error_2.setRemainingTime(10000);
      
      PSErrorResultsException exception = new PSErrorResultsException();
      assertTrue(exception.getIds() != null && 
         exception.getIds().isEmpty());
      assertTrue(exception.getResults() != null && 
         exception.getResults().isEmpty());
      assertTrue(exception.getErrors() != null && 
         exception.getErrors().isEmpty());

      try
      {
         exception.addError(null, error_1);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         exception.addError(guid_1, null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         exception.addResult(null, result_1);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         exception.addResult(guid_1, null);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      try
      {
         exception.removeResult(guid_1);
         assertFalse("Should have thrown exception", false);
      }
      catch (IllegalArgumentException e)
      {
         // expected exception
         assertTrue(true);
      }
      
      exception.addError(guid_1, error_1);
      exception.addError(guid_2, error_2);
      exception.addResult(guid_3, result_1);
      assertTrue(exception.getIds() != null && 
         exception.getIds().size() == 3);
      assertTrue(exception.getResults() != null && 
         exception.getResults().size() == 1);
      assertTrue(exception.getErrors() != null && 
         exception.getErrors().size() == 2);
      
      PSErrorResultsException exception2 = new PSErrorResultsException();
      exception2.addError(guid_1, error_1);
      exception2.addError(guid_2, error_2);
      exception2.addResult(guid_3, result_1);
      
      assertTrue(exception.equals(exception2));
      
      exception2.removeResult(guid_3);
      exception2.removeResult(guid_2);
      
      assertFalse(exception.equals(exception2));
   }
}

