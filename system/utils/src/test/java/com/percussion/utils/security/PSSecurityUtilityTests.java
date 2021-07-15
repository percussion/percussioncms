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

package com.percussion.utils.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PSSecurityUtilityTests {

    @Test
    public void testValidSQLObjectName(){
        assertNotEquals("<script>alert('');</script>",
                PSSecurityUtility.removeInvalidSQLObjectNameCharacters("<script>alert('');</script>"));

        assertNotEquals("IN VALID;",
                PSSecurityUtility.removeInvalidSQLObjectNameCharacters("IN VALID;"));

        assertEquals("VALID",
                PSSecurityUtility.removeInvalidSQLObjectNameCharacters("VALID"));

        assertEquals("VAL_ID",
                PSSecurityUtility.removeInvalidSQLObjectNameCharacters("VAL_ID"));


        assertEquals("VAL1_ID",
                PSSecurityUtility.removeInvalidSQLObjectNameCharacters("VAL1_ID"));

        //TODO:  Fix the regex to work with unicode characters
        //assertEquals("Њuni",
                //PSSecurityUtility.removeInvalidSQLObjectNameCharacters("Њuni"));

    }

    @Test
    public void testValidNumericId(){
        assertTrue(PSSecurityUtility.isValidNumericId("1234"));
        assertFalse(PSSecurityUtility.isValidNumericId("<script>alert('12345');</script>"));
    }

    @Test
    public void testValidGuidId(){
        assertTrue(PSSecurityUtility.isValidGuidId("1234-99-89894"));
        assertFalse(PSSecurityUtility.isValidGuidId("<script>alert('123-4444-45');</script>"));
    }

    @Test
    public void testValidCMSPath(){
        assertTrue(PSSecurityUtility.isValidCMSPathString("/Sites/HWMB/%22%3e%3cscript%3ealert%28595%29%3c%2fscript%3e"));
        assertTrue(PSSecurityUtility.isValidCMSPathString("/Sites/www.mysite.com/mypage.html"));
        assertFalse(PSSecurityUtility.isValidCMSPathString(
                "/Sites/HWMB/<script>alert(595);</script>"));
    }

}
