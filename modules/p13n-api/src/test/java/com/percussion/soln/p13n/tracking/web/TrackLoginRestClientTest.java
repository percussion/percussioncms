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

package test.percussion.soln.p13n.tracking.web;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;
import com.percussion.soln.p13n.tracking.web.TrackLoginRestClient;


public class TrackLoginRestClientTest extends TrackRestClientTest {
    protected TrackLoginRestClient loginClient = new TrackLoginRestClient();
    Map<String, Integer> w = new HashMap<String, Integer>();
    {
        w.put("foo", 100);
        w.put("bar", 200);
        client = loginClient;
    }
    
    @Test
    public void testLoginInitial() throws Exception {

        VisitorTrackingResponse r = loginClient.login("agent", w);
        assertNotNull(r);
        assertRequestBody(
                "GET /soln-p13n/track/track?userId=agent&actionName=login&segmentWeights%5Bfoo%5D=100&segmentWeights%5Bbar%5D=200 HTTP/1.1" +
        		"User-Agent: Jakarta Commons-HttpClient/3.1" +
        		"Host: localhost:9990");
    }
    
    @Test
    public void testLoginWithProfileId() throws Exception {
        loginClient.login("100", "agent", w);
        assertRequestBody(
                "GET /soln-p13n/track/track?userId=agent&visitorProfileId=100&actionName=login&segmentWeights%5Bfoo%5D=100&segmentWeights%5Bbar%5D=200 HTTP/1.1" +
        		"User-Agent: Jakarta Commons-HttpClient/3.1" +
        		"Host: localhost:9990");
    }
    
    @Test
    public void testLoginWithServlet() throws Exception {
        loginClient.login(servletRequest, servletResponse, "agent", w);
        assertRequestBody(
                "GET /soln-p13n/track/track?address=127.0.0.1&hostname=localhost&locale=en&userId=agent&actionName=login&segmentWeights%5Bfoo%5D=100&segmentWeights%5Bbar%5D=200 HTTP/1.1" +
        		"User-Agent: Jakarta Commons-HttpClient/3.1" +
        		"Host: localhost:9990");
    }
    
}
