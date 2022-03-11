/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
