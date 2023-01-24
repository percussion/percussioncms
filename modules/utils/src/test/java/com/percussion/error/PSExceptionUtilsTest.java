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

package com.percussion.error;

import com.percussion.utils.tools.IPSUtilsConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PSExceptionUtilsTest {

    private static final Logger log = LogManager.getLogger(IPSUtilsConstants.UNIT_TEST_LOG);

    public class PSInnerClass{

        public void throwinnerexception() throws Exception {
            throw new Exception("Inner Test Root Cause");
        }
    }

    @Test
    public void testGetMessage(){
        try{
            try {
                new PSInnerClass().throwinnerexception();
            }catch(Exception e){
                throw new Exception(e);
            }
        }catch(Exception e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            assertNotNull(PSExceptionUtils.getMessageForLog(e));
        }
    }
    @Test
    public void testGetDebugMessage(){
        try{
            throw new Exception("test",new Exception("Test Chain"));
        }catch(Exception e) {
            log.error(PSExceptionUtils.getDebugMessageForLog(e));
            assertNotNull(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
}
