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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.jexl.PSOFolderTools;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;

@RunWith(JMock.class)
public class PSOFolderToolsTest {

    Mockery context = new JUnit4Mockery();
    IPSContentWs contentWs;
    IPSGuidManager guidManager;
    IPSGuid guid;
    IPSAssemblyItem assemblyItem;
    PSOFolderTools tools;
    
    @Before
    public void setUp() throws Exception {
        contentWs = context.mock(IPSContentWs.class);
        guidManager = context.mock(IPSGuidManager.class);
        guid = context.mock(IPSGuid.class);
        assemblyItem = context.mock(IPSAssemblyItem.class);
        tools = new PSOFolderTools();
        tools.setContentWs(contentWs);
        tools.setGuidManager(guidManager);
    }

    @Test
    @Ignore
    public void shouldGetFirstParentFolderPathFromGuid() throws Exception {
        context.checking(new Expectations() {{ 
            one(contentWs).findFolderPaths(guid);
            will(returnValue(new String[] {"a","b","c"}));
        }});
        assertEquals("a",tools.getParentFolderPath(guid));
                
    }

    @Test
    @Ignore
    public void shouldGetParentFolderPathFromAssemblyItem() throws Exception {
        final PSFolder folder = new PSFolder("test",1,1,1,"description");
        folder.setFolderPath("testPath");
        context.checking(new Expectations() {{ 
            one(assemblyItem).getFolderId();
            will(returnValue(1));
            
            one(guidManager).makeGuid(new PSLocator(1,-1));
            will(returnValue(guid));
            
            one(contentWs).loadFolders(Arrays.asList(guid));
            will(returnValue(Arrays.asList(folder)));
        }});
        
        assertEquals("testPath",tools.getParentFolderPath(assemblyItem));
    }
}
