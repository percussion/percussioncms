package test.percussion.soln.utilities.rx.jexl;


import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.soln.utilities.rx.jexl.DispatchTemplateUtil; 

@RunWith(JMock.class)
public class DispatchTemplateUtilTest {

    Mockery context = new JUnit4Mockery();
    DispatchTemplateUtil picker;
    Map<String,Object> templateMap;
    Map<String,Object> paramMap;
    IPSAssemblyItem asmItem;
    IPSAssemblyService mockAssemblyService;
    IPSAssemblyTemplate mockTemplate;
    
    Node mockNode;
    
    @Before
    public void setUp() throws Exception {
        picker = new DispatchTemplateUtil();
        
        templateMap = new HashMap<String, Object>();
        templateMap.put("a", "a_test");
        asmItem = context.mock(IPSAssemblyItem.class);
        mockNode = context.mock(Node.class);
        mockAssemblyService = context.mock(IPSAssemblyService.class);
        picker.setAssemblyService(mockAssemblyService);
        paramMap = new HashMap<String,Object>();
        mockTemplate = context.mock(IPSAssemblyTemplate.class); 
    }
    
    public void setupMockAssemblyItem(final String ct) throws Exception {
        final IPSNodeDefinition nd = context.mock(IPSNodeDefinition.class);
        context.checking( new Expectations () {{
            allowing(asmItem).getNode();
            will(returnValue(mockNode));
            
            allowing(mockNode).getDefinition();
            will(returnValue(nd));
            
            allowing(nd).getInternalName();
            will(returnValue(ct));
        }});
    }
    
    @Test
    public void shouldPickTemplate() throws Exception {
        templateMap.put("testCT", "testTemplate");
        setupMockAssemblyItem("testCT");
        assertEquals("testTemplate", picker.pickTemplate(asmItem, templateMap, "blah"));
    }
    
    @Test
    public void shouldPickDefaultTemplate() throws Exception {
        templateMap.put("testCT", "testTemplate");
        setupMockAssemblyItem("NOT_IN_MAP");
        assertEquals("blah", picker.pickTemplate(asmItem, templateMap, "blah"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void shouldFailOnNullAssemblyItem() throws Exception {
        templateMap.put("testCT", "testTemplate");
        picker.pickTemplate(null, templateMap, "blah");
    }
    
    @Test
    public void shouldGetTemplateFromLocationParams() throws Exception {
        paramMap.put(IPSHtmlParameters.SYS_VARIANTID, "301");
        context.checking( new Expectations () {{
            allowing(mockAssemblyService).loadUnmodifiableTemplate("301");
            will(returnValue(mockTemplate));
        }});
        assertNotNull(picker.getLocationSchemeTemplate(paramMap));
    }
    

}
