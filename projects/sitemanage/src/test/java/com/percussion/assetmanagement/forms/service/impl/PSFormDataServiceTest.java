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

package com.percussion.assetmanagement.forms.service.impl;

import com.percussion.assetmanagement.forms.data.PSFormSummary;
import com.percussion.assetmanagement.forms.service.IPSFormDataService;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.test.PSServletTestCase;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JLS: Disabled test as it is unreliable, and since there is a similar test in the Delivery tier project, this test
 * is mostly redundant, and there are no other sitemanage unit tests for rest services that pass through to the
 * DTS.
 *
 */
//public class PSFormDataServiceTest extends PSServletTestCase
public class PSFormDataServiceTest extends TestCase
{

    private static final String SERVER_URL = "http://localhost:9980";

    private static final String SERVER_SECURE_URL = "https://localhost:8443";

    private static final String SERVER_USER = "ps_manager";

    private static final String SERVER_PASSWORD = "newpassword";

    private static final Boolean SERVER_ALLOW_CERTIFICATES = Boolean.TRUE;

    private static final String TESTFORM1 = "testform1";

    private static final String TESTFORM2 = "testform2";

    private static final String TESTFORM3 = "testform3";

    private IPSFormDataService formService;
    
    /**
     * Placeholder dummy test in place while all other tests are disabled
     * 
     * @throws Exception
     */
    @Test
    public void testNothing() throws Exception
    {
        assertTrue(true);
    }

    /*
    @Override
    protected void setUp()
    {
        this.formService = new PSFormDataService(new IPSDeliveryInfoService()
        {

            @Override
            public List<PSDeliveryInfo> findAll()
            {
                PSDeliveryInfo ds = new PSDeliveryInfo(SERVER_URL);
                ds.setUsername(SERVER_USER);
                ds.setPassword(SERVER_PASSWORD);
                ds.setAdminUrl(SERVER_SECURE_URL);
                ds.setAllowSelfSignedCertificate(SERVER_ALLOW_CERTIFICATES);
                ArrayList<String> services = new ArrayList<String>();
                services.add("perc-form-processor");
                ds.setAvailableServices(services);

                ArrayList<PSDeliveryInfo> list = new ArrayList<PSDeliveryInfo>();
                list.add(ds);

                return list;
            }

            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
        });

        this.removeAllSubmittedForms();
    }

    private void removeAllSubmittedForms()
    {
        this.formService.exportFormData(TESTFORM1);
        this.formService.clearFormData(TESTFORM1);

        this.formService.exportFormData(TESTFORM2);
        this.formService.clearFormData(TESTFORM2);

        this.formService.exportFormData(TESTFORM3);
        this.formService.clearFormData(TESTFORM3);
    }

    private void submitFormData(String firstName, String lastName, String middleName, String formName) throws Exception
    {

        submitFormData(formName, new String[][]
        {new String[]
        {"fname", firstName}, new String[]
        {"lname", lastName}, new String[]
        {"mname", middleName}});
    }

    private void submitFormData(String formName, String[][] keyValues) throws Exception
    {
        submitFormData(SERVER_URL, formName, keyValues);
    }

    private void submitFormData(String serverUrl, String formName, String[][] keyValues) throws Exception
    {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(serverUrl + "/perc-form-processor/form/");

        List<NameValuePair> values = new ArrayList<NameValuePair>();

        values.add(new NameValuePair("perc_formName", formName));

        for (String[] keyValue : keyValues)
        {
            values.add(new NameValuePair(keyValue[0], keyValue[1]));
        }

        try
        {
            postMethod.setRequestBody(values.toArray(new NameValuePair[0]));
            httpClient.executeMethod(postMethod);

            postMethod.getResponseBody();
        }
        finally
        {
            postMethod.releaseConnection();
        }
    }

    private int countIndexOf(String text, String search)
    {
        int count = 0;
        for (int fromIndex = 0; fromIndex > -1; count++)
            fromIndex = text.indexOf(search, fromIndex + ((count > 0) ? 1 : 0));
        return count - 1;
    }

    @Test
    public void testGetFormData_WithNoDeliveryServers() throws Exception
    {
        this.formService = new PSFormDataService(new IPSDeliveryInfoService()
        {
            @Override
            public List<PSDeliveryInfo> findAll()
            {
                return new ArrayList<PSDeliveryInfo>();
            }

            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
        });

        PSFormSummary form = formService.getFormData(TESTFORM1);
        assertNull("form summary is null", form);
    }

    @Test
    public void testGetFormData_FormDoesExist() throws Exception
    {
        this.submitFormData("firstNameValue", "lastNameValue", "middleNameValue", TESTFORM1);

        PSFormSummary formSummary = formService.getFormData(TESTFORM1);

        assertNotNull("form summary is not null", formSummary);
        assertEquals("form name", TESTFORM1, formSummary.getName());
    }

    @Test
    public void testGetAllFormData_NoDeliveryServers() throws Exception
    {
        this.formService = new PSFormDataService(new IPSDeliveryInfoService()
        {
            @Override
            public List<PSDeliveryInfo> findAll()
            {
                return new ArrayList<PSDeliveryInfo>();
            }

            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
        });

        List<PSFormSummary> list = formService.getAllFormData();
        assertNotNull("form data list not null", list);
        assertTrue("form data list empty", list.size() == 0);
    }

    @Test
    public void testGetAllFormData_NoFormSubmitted() throws Exception
    {
        this.removeAllSubmittedForms();
        List<PSFormSummary> list = formService.getAllFormData();

        assertNotNull("form data list not null", list);
        assertTrue("form data list empty", list.size() == 0);
    }

    @Test
    public void testGetAllFormData_SomeFormsSubmitted() throws Exception
    {
        this.removeAllSubmittedForms();
        this.submitFormData("firstNameValue", "lastNameValue", "middleNameValue", TESTFORM1);
        this.submitFormData("firstNameValue2", "lastNameValue2", "middleNameValue2", TESTFORM2);

        List<PSFormSummary> list = formService.getAllFormData();

        assertNotNull("form data list not null", list);
        assertEquals("form data list not empty", 2, list.size());

        // testform1
        assertEquals("testform1 name", TESTFORM1, list.get(0).getName());
    }

    @Test
    public void testExportFormData_WithNoDeliveryServers() throws Exception
    {
        this.formService = new PSFormDataService(new IPSDeliveryInfoService()
        {
            @Override
            public List<PSDeliveryInfo> findAll()
            {
                PSDeliveryInfo ds = new PSDeliveryInfo(SERVER_URL);
                ds.setUsername(SERVER_USER);
                ds.setPassword(SERVER_PASSWORD);
                ds.setAdminUrl(SERVER_SECURE_URL);
                ds.setAllowSelfSignedCertificate(SERVER_ALLOW_CERTIFICATES);
                ArrayList<String> services = new ArrayList<String>();
                services.add("perc-form-processor");
                ds.setAvailableServices(services);

                ArrayList<PSDeliveryInfo> list = new ArrayList<PSDeliveryInfo>();
                list.add(ds);

                return list;
            }

            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
        });

        // There will be a submitted form, but no delivery servers configured
        this.submitFormData("firstNameValue", "lastNameValue", "middleNameValue", TESTFORM1);

        String result = this.formService.exportFormData(TESTFORM1);

        assertTrue("export Form Data response not null", result != null);
    }

    @Test
    public void testExportFormData_WithOneFormSubmitted() throws Exception
    {
        this.submitFormData("firstNameValue", "lastNameValue", "middleNameValue", TESTFORM1);

        String result = this.formService.exportFormData(TESTFORM1);

        assertTrue("export Form Data response not null", result != null && !result.isEmpty());
        assertTrue("export Form Data first name", countIndexOf(result, "Form name,Create date,fname,lname,mname") == 1);
        assertTrue("export Form Data first name", countIndexOf(result, "firstNameValue") == 1);
        assertTrue("export Form Data last name", countIndexOf(result, "lastNameValue") == 1);
        assertTrue("export Form Data middle name", countIndexOf(result, "middleNameValue") == 1);
    }

    @Test
    public void testExportFormData_WithTwoFormsSubmitted_SameFormName_WithSameColumns() throws Exception
    {
        this.submitFormData("firstNameValue1", "lastNameValue1", "middleNameValue1", TESTFORM1);
        this.submitFormData("firstNameValue2", "lastNameValue2", "middleNameValue2", TESTFORM1);

        String result = this.formService.exportFormData(TESTFORM1);

        assertTrue("export Form Data response not null", result != null && !result.isEmpty());
        assertTrue("export Form Data first name", countIndexOf(result, "Form name,Create date,fname,lname,mname") == 1);

        assertTrue("export Form Data first name 1", countIndexOf(result, "firstNameValue1") == 1);
        assertTrue("export Form Data last name 1", countIndexOf(result, "lastNameValue1") == 1);
        assertTrue("export Form Data middle name 1", countIndexOf(result, "middleNameValue1") == 1);

        assertTrue("export Form Data first name 2", countIndexOf(result, "firstNameValue2") == 1);
        assertTrue("export Form Data last name 2", countIndexOf(result, "lastNameValue2") == 1);
        assertTrue("export Form Data middle name 2", countIndexOf(result, "middleNameValue2") == 1);
    }

    @Test
    public void testExportFormData_WithTwoFormsSubmitted_SameFormName_WithDifferentColumns() throws Exception
    {
        this.submitFormData(TESTFORM1, new String[][]
        {new String[]
        {"field1", "value1"}, new String[]
        {"field2", "value2"}});

        this.submitFormData(TESTFORM1, new String[][]
        {new String[]
        {"field3", "value3"}, new String[]
        {"field4", "value4"}});

        String result = this.formService.exportFormData(TESTFORM1);

        assertTrue("export Form Data response not null", result != null && !result.isEmpty());
        assertTrue("export Form Data first name",
                countIndexOf(result, "Form name,Create date,field1,field2,field3,field4") == 1);

        assertTrue("export Form Data field1", countIndexOf(result, "value1") == 1);
        assertTrue("export Form Data field2", countIndexOf(result, "value2") == 1);

        assertTrue("export Form Data field3", countIndexOf(result, "value3") == 1);
        assertTrue("export Form Data field4", countIndexOf(result, "value4") == 1);
    }

    @Test
    public void testClearFormData_WithNoDeliveryServers() throws Exception
    {
        this.formService = new PSFormDataService(new IPSDeliveryInfoService()
        {
            @Override
            public List<PSDeliveryInfo> findAll()
            {
                PSDeliveryInfo ds = new PSDeliveryInfo(SERVER_URL);
                ds.setUsername(SERVER_USER);
                ds.setPassword(SERVER_PASSWORD);
                ds.setAdminUrl(SERVER_SECURE_URL);
                ds.setAllowSelfSignedCertificate(SERVER_ALLOW_CERTIFICATES);
                ArrayList<String> services = new ArrayList<String>();
                services.add("perc-form-processor");
                ds.setAvailableServices(services);

                ArrayList<PSDeliveryInfo> list = new ArrayList<PSDeliveryInfo>();
                list.add(ds);

                return list;
            }

            @Override
            public PSDeliveryInfo findByService(String service)
            {
                List<PSDeliveryInfo> servers = findAll();
                return servers.isEmpty() ? null : servers.get(0);
            }
        });

        formService.clearFormData(TESTFORM1);
    }

    @Test
    public void testClearFormData_WithNoFormsSubmitted() throws Exception
    {
        formService.clearFormData(TESTFORM1);
    }

    @Test
    public void testClearFormData_WithFormSubmitted() throws Exception
    {
        this.submitFormData("firstNameValue1", "lastNameValue1", "middleNameValue1", TESTFORM1);

        String result = formService.exportFormData(TESTFORM1);
        assertTrue("exported form data not empty", result != null && result.length() > 0);

        formService.clearFormData(TESTFORM1);

        result = formService.exportFormData(TESTFORM1);
        assertTrue("exported form data is empty", countIndexOf(result, "firstNameValue1") == 0);
    }
    */
}
