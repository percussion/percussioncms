package percussion.soln.rss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat;
import com.percussion.soln.rss.RssJexl;



@RunWith(JMock.class)
public class RssJexlTest {
    
    Mockery context = new JUnit4Mockery();
    Node node;
    Map<String, String> values = new HashMap<String, String>();
    List<IPSAssemblyTemplate> foundTemplates = new ArrayList<IPSAssemblyTemplate>();
    String contentType = "myContentType";
    
    RssJexl rss = new RssJexl() {

        @Override
        protected String getValue(Node node, String titleFields) {
            return values.get(titleFields);
        }

        @Override
        protected Collection<IPSAssemblyTemplate> findAllTemplates() throws PSAssemblyException {
            return foundTemplates;
        }

        @Override
        protected String getContentType(Node node) {
            return contentType;
        }
        
    };
    {
        values.put("body", "body");
        values.put("title", "title");
    }
    
    @Test
    public void testCreateEntryForNode() throws Exception {
        SyndFeed feed = rss.createFeed();
        feed.setTitle("stuff");
        feed.setDescription("desc");
        feed.setLink("http://crap");
        List<SyndEntry>entries = rss.createEntries();
        SyndEntry entry = rss.createEntry(node, "title", "body");
        entries.add(entry);
        feed.setEntries(entries);
        String output = rss.getRss(feed);
        assertNotNull(output);
    }
    

    @Test
    public void testFindEntryTemplates() throws Exception {
        
        
        expectTemplate("adam", "crap", "crap", Snippet);
        expectTemplate("rss entry", "crap", "crap", Page);
        expectTemplate("stuff rss_elntry poop", "text/xml", "adsffasdfd myContenttype asdfasdffsadf", Snippet);
        expectTemplate("stuff rss asdfasdf Entry poop", "text/xml", "adsffasdfd myContenttype asdfasdffsadf", Snippet);
        expectTemplate("adam", "crap", "crap", Page);
        
        String template = rss.findEntryTemplate(node);
        
        assertNotNull(template);
        assertEquals("stuff rss asdfasdf Entry poop", template);
        
    }
    
    private int nameId = 0;
    public IPSAssemblyTemplate expectTemplate(final String templateName, 
            final String mimeType, 
            final String description, 
            final OutputFormat format) {
        final IPSAssemblyTemplate t = context.mock(IPSAssemblyTemplate.class, templateName + "-" + ++nameId);
        context.checking(new Expectations() {{
            allowing(t).getName();
            will(returnValue(templateName));
            one(t).getMimeType();
            will(returnValue(mimeType));
            one(t).getDescription();
            will(returnValue(description));
            one(t).getOutputFormat();
            will(returnValue(format));
        }});
        foundTemplates.add(t);
        return t;
    }

}
