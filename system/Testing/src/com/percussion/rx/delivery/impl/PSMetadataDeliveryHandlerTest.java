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
package com.percussion.rx.delivery.impl;

import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.impl.PSBaseDeliveryHandler.Item;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PSMetadataDeliveryHandlerTest extends TestCase
{
    /**
     * Enable or disable the test that requires a live delivery server
     */
    private boolean isRunTest = false;

    private String LOCATION = "/MySite/Stuff/test.html";

    private long JOB_ID = 100L;

    private static final long PUB_SERVER_ID = 1000L;
    
    private static final int DELIVERY_CONTEXT = 1;

    private PSMetadataDeliveryHandler h = new PSMetadataDeliveryHandler();

    public void testCreateFullPath()
    {
        String baseUrl = "http://h1:2034/f1/f2";
        String location = "/f3/f4/foo.html";
        String fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/f1/f2/f3/f4/foo.html");

        baseUrl = "http://h1:2034/f1/f2/";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/f1/f2/f3/f4/foo.html");

        baseUrl = "http://h1/f1/f2";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/f1/f2/f3/f4/foo.html");

        baseUrl = "http://h1:2034/f1/f2";
        location = "f3/f4/foo.html";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/f1/f2/f3/f4/foo.html");

        location = "/foo.html";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/f1/f2/foo.html");

        location = "foo.html";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/f1/f2/foo.html");

        baseUrl = "http://h1:203";
        location = "foo.html";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/foo.html");

        baseUrl = "http://h1:203/";
        location = "foo.html";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/foo.html");

        baseUrl = "http://h1";
        location = "foo.html";
        fpath = h.createFullPath(baseUrl, location);
        assertEquals(fpath, "/h1/foo.html");
    }

    private PSPurgableTempFile file;

    @Before
    public void setUp() throws Exception
    {
        if (!isRunTest)
            return;

        PSDeliveryInfo deliveryServer = new PSDeliveryInfo("http://localhost:9980", "ps_manager", "newpassword");
        deliveryServer.setAdminUrl("https://localhost:8443");
        deliveryServer.setAllowSelfSignedCertificate(true);

        h.setDeliveryServer(deliveryServer);
        h.prepareForDelivery(JOB_ID);

        file = getMetadataFile();
        // file = getFileWithoutMetadata();
    }

    private PSPurgableTempFile getMetadataFile() throws IOException
    {
        PSPurgableTempFile f = new PSPurgableTempFile("test-", ".txt", null);
        FileUtils.writeStringToFile(f, "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "lang=\"en\" xml:lang=\"en\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\r\n"
                + "   <head><link rel=\"schema.DC\" href=\"http://purl.org/dc/elements/1.1/\" >"
                + "<title property=\"dc:title\">Home</title>\r\n"
                + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\"/>\r\n"
                + "    <meta name=\"description\" property=\"dc:description\" content=\"blah\" />\r\n"
                + "        <meta property=\"type\" content=\"page\"/>\r\n"
                + "        <meta property=\"source\" content=\"template name\"/>\r\n"
                + "        <meta property=\"created\" content=\"2011-01-21T09:36:05\"/>\r\n"
                + "        <meta property=\"alternative\" content=\"page link name\"/>" + "</head>\r\n"
                + "   <body>\r\n" + "   <p>Body content here</p>\r\n" + "   </body>\r\n" + "</html>", "UTF-8");

        return f;
    }

    private PSPurgableTempFile getFileWithoutMetadata() throws IOException
    {
        PSPurgableTempFile f = new PSPurgableTempFile("test-", ".txt", null);
        FileUtils.writeStringToFile(f, "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                + "lang=\"en\" xml:lang=\"en\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\r\n"
                + "   <head><link rel=\"schema.DC\" href=\"http://purl.org/dc/elements/1.1/\" >"
                + "<title property=\"dc:title\">Home</title>\r\n"
                + "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\"/>\r\n" + "</head>\r\n"
                + "   <body>\r\n" + "   <p>Body content here</p>\r\n" + "   </body>\r\n" + "</html>", "UTF-8");

        return f;
    }

    @After
    public void tearDown() throws Exception
    {
        if (!isRunTest)
            return;

        h.releaseForDelivery(JOB_ID);
    }

    @Test
    public void testDoDelivery() throws Exception
    {
        if (!isRunTest)
            return;

        Item item = createItem(file, "text/html", false);
        IPSDeliveryResult dr = h.doDelivery(item, JOB_ID, LOCATION);
        System.out.println(dr.getFailureMessage());
        assertEquals(Outcome.DELIVERED, dr.getOutcome());
    }

    @Test
    public void testDoDeliveryFailureBadEndpoint() throws Exception
    {
        if (!isRunTest)
            return;

        h.releaseForDelivery(JOB_ID);
        h.setDeliveryServer(new PSDeliveryInfo("http://blah:100"));
        h.prepareForDelivery(JOB_ID);
        Item item = createItem(file, "text/html", false);
        IPSDeliveryResult dr = h.doDelivery(item, JOB_ID, LOCATION);

        assertEquals(Outcome.FAILED, dr.getOutcome());
    }

    @Test
    public void testDoRemoval()
    {
        if (!isRunTest)
            return;

        Item item = createItem(file, "txt/html", false);
        IPSDeliveryResult dr = h.doRemoval(item, JOB_ID, LOCATION);
        System.out.println(dr.getFailureMessage());
        assertEquals(Outcome.DELIVERED, dr.getOutcome());
    }

    public Item createItem(PSPurgableTempFile file, String mimeType, boolean removal)
    {
        IPSGuid fakeGuid = new PSGuid();
        return h.createItemForTest(fakeGuid, file, mimeType, 100L, removal, JOB_ID, PUB_SERVER_ID, DELIVERY_CONTEXT);
    }

}
