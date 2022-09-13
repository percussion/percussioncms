package test.percussion.soln.p13n.delivery;

import static com.percussion.soln.p13n.delivery.web.DeliveryWebUtils.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;

//import static java.util.Arrays.*;
//import static org.hamcrest.CoreMatchers.*;
//import static org.junit.matchers.JUnitMatchers.*;

/**
 * Scenario description: 
 * @author adamgent, Apr 27, 2009
 */
public class DeliveryWebUtilsTest {
    
    
    String jsonRequest = "{\"listItem\":{\"contentId\":724,\"contentType\":\"\",\"id\":724," +
    		"\"properties\":{\"rx:soln_list_dateRangeStart\":\"\",\"rx:soln_list_titleContains\":\"\",\"rx:soln_list_maxResults\":\"\"," +
    		"\"rx:soln_list_type\":\"Auto\",\"rx:soln_list_dateRangeEnd\":\"\",\"rx:soln_list_snippet\":\"rffSnTitleLink\",\"rx:soln_p13n_filterMin\":\"\"," +
    		"\"rx:soln_p13n_filterMax\":\"\",\"rx:soln_list_jcrQuery\":\"\",\"rx:soln_list_contentType\":\"rffEvent\"},\"segmentIds\":[]," +
    		"\"snippetFilterIds\":[\"soln.p13n.filter.BestMatchScoring\",\"soln.p13n.filter.SortBasedOnScore\"],\"snippets\":[{\"contentId\":722,\"contentType\":\"\",\"id\":0," +
    		"\"properties\":{},\"rendering\":\"\\n    " +
    		"<span class=\\\"lead_snippet\\\">\\n\\t<a href=\\\"$pagelink\\\" class=\\\"titlelink\\\">\\n\\t  Chris' Birthday\\t</a>\\n    " +
    		"</span>\\n  \",\"segmentIds\":[\"718\"]}]},\"listItemId\":724,\"visitorProfile\":null}";
    
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testJsonToRequest() {
        DeliveryRequest request = jsonToRequest(jsonRequest);
        
        assertNotNull("request should not be null", request);
        assertNotNull("List item", request.getListItem());
        List<DeliverySnippetItem> snippets = request.getListItem().getSnippets();
        assertNotNull("snippets", snippets);
        assertEquals("snippets size", 1, snippets.size());
        assertEquals("is snippets", DeliverySnippetItem.class, snippets.get(0).getClass());
        assertEquals("missing property", "Auto", request.getListItem().getProperties().get("rx:soln_list_type"));
        

    }
    
    @Test
    public void testJsonToRequestEmpty() {
        String jsonRequest = "{\"listItem\" : {} }";
        DeliveryRequest request = jsonToRequest(jsonRequest);
        assertNotNull("list item should not be null", request);
    }


}
