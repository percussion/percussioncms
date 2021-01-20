/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.web.service;

import static org.junit.Assert.*;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log log = LogFactory.getLog(PSWidgetServiceTest.class);

}
