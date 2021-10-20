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

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

    @Test
    public void testCrazyPreviewUrl() {
        try {
            String testUrl = "http://ts-trees4.percussion.local:9992/Sites/www.bw.edu/student-life/organizations/?percmobilepreview=true&clear=function(){this.length=0}&first=function(){return%20this[0]}&last=function(){return%20this[this.length-1]}&flatten=function(){for(var%20t=this.length,e=[],n=0;n%3Ct;n++)this[n]instanceof%20Array?e=e.concat(this[n]):e.push(this[n]);return%20e}&each=function(t){var%20e=this.length;if(%22function%22!=typeof%20t)throw%22Array.each%20requires%20first%20argument%20to%20be%20a%20function%22;for(var%20n=arguments[1],i=0;i%3Ce;i++)i%20in%20this&&t.call(n,this[i],i,this);return%20null}&include=function(t){return%20this.length,this.indexOf(t)%3E=0}";
            URI uri = new URI(SecureStringUtils.stripUrlParams(testUrl));
            assertNotNull(uri);
            assertEquals("ts-trees4.percussion.local", uri.getHost());
            assertEquals(9992, uri.getPort());
            assertEquals("http", uri.getScheme());
            assertEquals("/Sites/www.bw.edu/student-life/organizations/", uri.getPath());

        } catch (URISyntaxException e) {
            assertNull(e);
        }
    }

    @Test
    public void testStripNoQueryParams(){
        assertEquals("http://somesite.edu/noparams",SecureStringUtils.stripUrlParams("http://somesite.edu/noparams"));
        assertEquals("http://somesite.edu/noparams", SecureStringUtils.stripUrlParams("http://somesite.edu/noparams#test"));
    }

    @Test
    public void testSanitizeStringForSQLStatementDerby(){
        assertEquals("http://somesite.edu/noparams",SecureStringUtils.sanitizeStringForSQLStatement("http://somesite.edu/noparams", SecureStringUtils.DatabaseType.DERBY));
    }

    @Test
    public void testSanitizeStringForSQLStatementMySql(){
        assertEquals("http://somesite.edu/noparams",SecureStringUtils.sanitizeStringForSQLStatement("http://somesite.edu/noparams", SecureStringUtils.DatabaseType.MYSQL));
    }

    @Test
    public void testSanitizeStringForSQLStatementSqlServer(){
        assertEquals("http://somesite.edu/noparams",SecureStringUtils.sanitizeStringForSQLStatement("http://somesite.edu/noparams", SecureStringUtils.DatabaseType.MSSQL));
    }
}
