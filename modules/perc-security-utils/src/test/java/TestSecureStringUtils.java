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

import com.percussion.security.SecureStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestSecureStringUtils {

    @Rule
    public TemporaryFolder tempFolder = TemporaryFolder.builder().build();

    private static final String[] resourcePaths = new String[] {
            "/Sites/",
            "/Assets/",
            "/rx_resources",
            "/sys_resources",
            "/cm/",
            "/web_resources",
            "/tmx/",
            "/services/",
            "/sessioncheck",
            "/webservices/",
            "/designwebservices/",
            "/content/",
            "/rest",
            "/v8",
            "/assembler/",
            "/contentlist",
            "/sitelist",
            "/login",
            "/logout",
            "/rxwebdav",
            "/ui/actionpage/panel",
            "/user/apps",
            "/publisher/",
            "/linkback/",
            "/servlet/",
            "/assembly/aa",
            "/contentui/aa",
            "/adf/",
            "/uploadAssetFile",
            "/textToImage/",
            "/Designer",
            "/Rhythmyx/"

    };

    @Before
    public void setup(){
        System.setProperty("rxdeploydir", tempFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void testRandomPassword(){
        String pwd = SecureStringUtils.generateRandomPassword();

        assertTrue(pwd!=null);
    }

    @Test
    public void testWildStrings(){

        String testPath = SecureStringUtils.cleanWildPath(resourcePaths,"/Rhythmyx/cm%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5cwindows%5cwin.ini","unit.test");
        assertNull(testPath);

        testPath = SecureStringUtils.cleanWildPath(resourcePaths,"/login","unit.test");
        assertEquals("/login",testPath);

        testPath = SecureStringUtils.cleanWildPath(resourcePaths,"/cm/../sys_resources/css/test.css","unit.test");
        assertEquals("/cm/../sys_resources/css/test.css",testPath);

        testPath = SecureStringUtils.cleanWildPath(resourcePaths,"/cm/../../sys_resources/css/test.css","unit.test");
        assertNull(testPath);

        testPath = SecureStringUtils.cleanWildPath(resourcePaths,"favicon.ico","unit.test");
        assertEquals("favicon.ico",testPath);

        testPath = SecureStringUtils.cleanWildPath(resourcePaths,"%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5c%2e%2e%5cwindows%5cwin.ini","unit.test");
        assertNull(testPath);

    }

    @Test
    public void testIsHTML(){
        assertFalse(SecureStringUtils.isXML("x"));
        assertTrue(SecureStringUtils.isXML("<div></div>"));
        assertTrue(SecureStringUtils.isXML("<a href='#'>x</a>"));
    }

    @Test
    public void testIsXML(){
        assertFalse(SecureStringUtils.isXML("x"));
        assertTrue(SecureStringUtils.isXML("<div></div>"));
        assertTrue(SecureStringUtils.isXML("<a href='#'>x</a>"));

    }


}
