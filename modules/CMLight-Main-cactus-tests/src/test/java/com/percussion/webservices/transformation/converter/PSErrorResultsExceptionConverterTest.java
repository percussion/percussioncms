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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webservices.transformation.converter;

import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.faults.PSErrorResultsFault;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSErrorResultsExceptionConverter} class.
 */
@Category(IntegrationTest.class)
public class PSErrorResultsExceptionConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   @Test
   public void testConversion() throws Exception
   {
      // create the source object
      IPSGuidManager manager = PSGuidManagerLocator.getGuidMgr();
      
      IPSGuid guid_1 = manager.createGuid(PSTypeEnum.SLOT);
      IPSGuid guid_2 = manager.createGuid(PSTypeEnum.SLOT);
      IPSGuid guid_3 = manager.createGuid(PSTypeEnum.SLOT);
      
      PSTemplateSlot slot = new PSTemplateSlot();
      slot.setGUID(guid_1);
      slot.setName("eiger");
      slot.setLabel("eigerLabel");
      
      PSErrorException error = new PSErrorException(
         IPSWebserviceErrors.OBJECT_NOT_FOUND, "message", "stack");
      PSLockErrorException lockError = new PSLockErrorException(
         IPSWebserviceErrors.OBJECT_NOT_FOUND, "message", "stack");
      lockError.setLocker("locker");
      lockError.setRemainingTime(1000);
      
      PSErrorResultsException source = new PSErrorResultsException();
      source.addResult(guid_1, slot);
      source.addError(guid_2, error);
      source.addError(guid_3, lockError);
      
      PSErrorResultsException target = 
         (PSErrorResultsException) roundTripConversion(
            PSErrorResultsException.class, 
            PSErrorResultsFault.class, 
            source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}

