/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.jexl;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.jexl.PSOMapTools;

public class PSOMapToolsTest {

    PSOMapTools mapTools;
    Map<String, Object> customM;
    Map<String, Object> defaultM;
    Map<String, Object> actual;
    Map<String, Object> expected;
    
    @Before
    public void setUp() {
        mapTools = new PSOMapTools();
        expected = new HashMap<String, Object>();
        
        customM = new HashMap<String, Object>();
        defaultM = new HashMap<String, Object>();
        customM.put("a", 1L);
        customM.put("b", 0L);
        customM.put("e", 5L);
        
        defaultM.put("a", 1L);
        defaultM.put("b", 2L);
        defaultM.put("c", 3L);
        defaultM.put("d", 4L);
    }
    
    @Test
    public void shouldLoadPropertiesFile() throws Exception {

            InputStream stream = this.getClass().getResourceAsStream("/com/percussion/pso/jexl/test.properties");
            File f = File.createTempFile("test", ".properties");
            f.deleteOnExit();

            FileOutputStream out = new FileOutputStream(f);
            IOUtils.copy(stream, out);
            out.close();

            Properties p = mapTools.loadPropertiesFile(f.getCanonicalPath());
            assertEquals("1", p.get("b"));

            f.delete();
    }
    
    @Test
    public void shouldExtend() {
        expected.put("a", 1L);
        expected.put("b", 0L);
        expected.put("c", 3L);
        expected.put("d", 4L);
        expected.put("e", 5L);
        
        actual = mapTools.overlay(defaultM, customM);
        assertEquals(expected, actual);
    }
    
    @Test
    public void overlayShouldReturnEqualsToDefaultOnNullForCustom() {
        actual = mapTools.overlay(defaultM, null);
        assertEquals(defaultM, actual);
        assertNotSame(defaultM, actual);
    }
    
    @Test
    public void overlayShouldReturnEqualsToCustomOnNullForDefault() {
        actual = mapTools.overlay(null, customM);
        assertEquals(customM, actual);
        assertNotSame(customM, actual);
    }
    
    @Test
    public void shouldCreateMapFromLists() {
        List<String> keys = asList("a","b","e");
        List<Long> values = asList(1L,0L,5L, 100000L);
        // The last value should be ignored
        actual = mapTools.create(keys, values);
        assertEquals(customM, actual);
    }
    
    @Test
    public void shouldCreateMap() {
        assertNotNull(mapTools.create());
    }
    
    @Test
    public void shouldGetFirstDefined() {
        Object actual = mapTools.getFirstDefined(customM, "d,r,e,b", null);
        assertEquals(5L, actual);
    }
}
