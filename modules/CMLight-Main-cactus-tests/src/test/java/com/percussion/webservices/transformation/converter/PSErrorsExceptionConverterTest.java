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
package com.percussion.webservices.transformation.converter;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.faults.PSErrorsFault;
import org.junit.experimental.categories.Category;

/**
 * Unit tests for the {@link PSErrorsExceptionConverter} class.
 */
@Category(IntegrationTest.class)
public class PSErrorsExceptionConverterTest extends PSConverterTestBase
{
   /**
    * Tests the conversion from a server to a client object. 
    */
   public void testConversion() throws Exception
   {
      // create the source object
      IPSGuidManager manager = PSGuidManagerLocator.getGuidMgr();
      
      IPSGuid guid_1 = manager.createGuid(PSTypeEnum.SLOT);
      IPSGuid guid_2 = manager.createGuid(PSTypeEnum.SLOT);
      IPSGuid guid_3 = manager.createGuid(PSTypeEnum.SLOT);
      
      PSErrorException error = new PSErrorException(
         IPSWebserviceErrors.OBJECT_NOT_FOUND, "message", "stack");
      PSLockErrorException lockError = new PSLockErrorException(
         IPSWebserviceErrors.OBJECT_NOT_FOUND, "message", "stack");
      lockError.setLocker("locker");
      lockError.setRemainingTime(1000);
      
      PSErrorsException source = new PSErrorsException();
      source.addResult(guid_1);
      source.addError(guid_2, error);
      source.addError(guid_3, lockError);
      
      PSErrorsException target = 
         (PSErrorsException) roundTripConversion(
            PSErrorsException.class, 
            PSErrorsFault.class, 
            source);
      
      // verify the the round-trip object is equal to the source object
      assertTrue(source.equals(target));
   }
}

