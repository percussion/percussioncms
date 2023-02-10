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
package com.percussion.pagemanagement.web.service;

import static org.junit.Assert.*;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetPackageInfo;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoRequest;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoResult;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSRestTestCase;
import com.percussion.share.test.PSObjectRestClient.DataValidationRestClientException;
import com.percussion.share.validation.PSValidationErrors;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSWidgetServiceTest extends PSRestTestCase<PSWidgetServiceTest.PSWidgetRestClient> {

    @Test
    public void testFindAll() throws Exception {
        List<PSWidgetSummary> widgets = restClient.getAll();
        //We should have atleast one widget shipped.
        assertTrue(widgets.size() > 0);
        PSWidgetSummary w = widgets.get(0);
        assertNotNull(w);
    }

    @Test
    public void testFind() throws Exception
    {
        PSWidgetSummary widget = restClient.get("percRawHtml");
        assertEquals("percRawHtmlAsset", widget.getName());
    }

    @Test
    public void testValidateWidgetItem() throws Exception
    {
        PSWidgetItem widgetItem = new PSWidgetItem();
        try {
            restClient.validateWidgetItem(widgetItem);
            fail("Should be invalid");
        }
        catch (DataValidationRestClientException e) {
            log.debug(e.getResponseBody());
        }
    }
    
    @Test
    public void testWidgetPackageInfo() throws Exception
    {
        PSWidgetPackageInfoRequest request = new PSWidgetPackageInfoRequest();
        List<String> names = request.getWidgetNames();
        names.add("percRawHtml");
        names.add("nosuchwidget");
        names.add("percRichText");
        
        PSWidgetPackageInfoResult response = restClient.findWidgetPackageInfo(request);
        assertNotNull(response);
        List<PSWidgetPackageInfo> infoList = response.getPackageInfoList();
        assertEquals(2, infoList.size());
        
        PSWidgetPackageInfo info = infoList.get(0);
        assertEquals("percRawHtml", info.getWidgetName());
        assertEquals("http://www.percussion.com", info.getProviderUrl());
        assertEquals("1.0.4", info.getVersion());
        
        info = infoList.get(1);
        assertEquals("percRichText", info.getWidgetName());
        assertEquals("http://www.percussion.com", info.getProviderUrl());
        assertEquals("1.0.4", info.getVersion());
    }

    public static class PSWidgetRestClient extends PSDataServiceRestClient<PSWidgetSummary> {

        public PSWidgetRestClient(String url) {
            super(PSWidgetSummary.class, url, "/Rhythmyx/services/pagemanagement/widget/");
        }

        public PSValidationErrors validateWidgetItem(PSWidgetItem item) {
            return postObjectToPath(concatPath(getPath(), "validate/item"), item, PSValidationErrors.class);
        }
        
        public PSWidgetPackageInfoResult findWidgetPackageInfo(PSWidgetPackageInfoRequest request)
        {
            return postObjectToPath(concatPath(getPath(), "packageinfo"), request, PSWidgetPackageInfoResult.class);
        }

    }


    @Override
    protected PSWidgetRestClient getRestClient(String baseUrl)
    {
        return new PSWidgetRestClient(baseUrl);

    }


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSWidgetServiceTest.class);

}
