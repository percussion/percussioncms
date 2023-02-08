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

package test.percussion.soln.p13n.delivery.web;

import static com.percussion.soln.p13n.delivery.web.DeliveryWebUtils.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.DeliverySnippetItem;


public class DeliveryWebUtilsTest {
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void testJsonToRequest() throws Exception {
        String json = "{\"listItem\":{\"contentId\":724,\"contentType\":\"\",\"id\":724," +
        "\"properties\":{\"rx:soln_list_dateRangeStart\":\"\",\"rx:soln_list_titleContains\":\"\"," +
        "\"rx:soln_list_maxResults\":\"\",\"rx:soln_list_type\":\"Auto\"," +
        "\"rx:soln_list_dateRangeEnd\":\"\",\"rx:soln_list_snippet\":\"rffSnTitleLink\"," +
        "\"rx:soln_p13n_filterMin\":\"\",\"rx:soln_p13n_filterMax\":\"\",\"rx:soln_list_jcrQuery\":\"\"," +
        "\"rx:soln_list_contentType\":\"rffEvent\"},\"segmentIds\":[]," +
        "\"snippetFilterIds\":[\"soln.p13n.filter.BestMatchScoring\",\"soln.p13n.filter.SortBasedOnScore\"]," +
        "\"snippets\":[{\"contentId\":722,\"contentType\":\"\",\"id\":0,\"properties\":{}," +
        "\"rendering\":\"\\n    <span class=\\\"lead_snippet\\\">\\n\\t<a href=\\\"$pagelink\\\" class=\\\"titlelink\\\">\\n\\t  Chris' Birthday\\t</a>\\n    </span>\\n  \"," +
        "\"segmentIds\":[\"718\"]}]},\"listItemId\":724,\"visitorProfile\":null}";
        
        DeliveryRequest actual = jsonToRequest(json);
        
        assertEquals("content id", 724L, actual.getListItem().getContentId());
        assertEquals("Properties", "Auto", actual.getListItem().getProperties().get("rx:soln_list_type"));
        assertEquals("Snippet filter ids", 
                asList("soln.p13n.filter.BestMatchScoring", "soln.p13n.filter.SortBasedOnScore"), actual.getListItem().getSnippetFilterIds());
    }
    
    @Test
    public void testRequestToJsonShouldHaveSegmentIds() throws Exception {
        DeliveryRequest request = new DeliveryRequest();
        DeliveryListItem list = new DeliveryListItem();
        list.setSegmentIds(new HashSet<String>(asList("1","2","3")));
        DeliverySnippetItem snip = new DeliverySnippetItem();
        snip.setSegmentIds(new HashSet<String>(asList("1","2")));
        list.setSnippets(asList(snip));
        request.setListItem(list);
        String json = requestToJson(request);
        String expected = "{\"listItem\":{\"contentId\":0,\"contentType\":\"\",\"id\":0,\"properties\":null,\"segmentIds\":[\"1\",\"2\",\"3\"]," +
        		"\"snippetFilterIds\":[],\"snippets\":[{\"contentId\":0,\"contentType\":\"\",\"id\":0," +
        		"\"properties\":null,\"rendering\":\"\",\"segmentIds\":[\"1\",\"2\"]}]},\"listItemId\":0,\"visitorProfile\":null}";
        assertEquals(expected,json);
    }

}
