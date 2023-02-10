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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Class to run performance tests against the PSMetadataDeliveryHandler and the
 * remote Extractor REST Service.
 * 
 * @author miltonpividori
 */
public class PSMetadataDeliveryHandlerPerformanceTest extends TestCase
{
    public void testDummy()
    {   
    }
    
    /**
     * A valid directory where test pages reside.
     */
    //private static final String PAGES_DIRECTORY = "C:/Customer/HHS/DHHS_PublishPages";
    private static final String PAGES_DIRECTORY = "C:\\Users\\Milton\\Documents\\pages"; 
   
    //private static final String DELIVERY_URL = "http://terminator:7080";
    private static final String DELIVERY_URL = "http://localhost:9970";
    
//    private static final String DELIVERY_SECURED_URL = "https://terminator:7443";
    private static final String DELIVERY_SECURED_URL = "https://localhost:8443";

    private static final String DELIVERY_PASSWORD = "newpassword";

    private static final String DELIVERY_USER = "ps_manager";
    
    private static final long JOB_ID = 100L;
    
    private static final long PUB_SERVER_ID = 1000L;
    
    private static final int DELIVERY_CONTEXT = 1;
    
    
    private PSMetadataDeliveryHandler metadataDeliveryHandler = new PSMetadataDeliveryHandler();
    
    @Before
    public void setUp() throws Exception
    {
        PSDeliveryInfo deliveryServer =
            new PSDeliveryInfo(DELIVERY_URL, DELIVERY_USER, DELIVERY_PASSWORD);
        deliveryServer.setAdminUrl(DELIVERY_SECURED_URL);
        deliveryServer.setAllowSelfSignedCertificate(true);

        metadataDeliveryHandler.setOperationTimeout(60000);
        metadataDeliveryHandler.setConnectionTimeout(60000);
        
        metadataDeliveryHandler.setDeliveryServer(deliveryServer);
        metadataDeliveryHandler.prepareForDelivery(JOB_ID);
    }
    
    @After
    public void tearDown() 
    {
       metadataDeliveryHandler.releaseForDelivery(JOB_ID);
    }

    
    public Item createItem(PSPurgableTempFile file, String mimeType, boolean removal)
    {
        IPSGuid fakeGuid = new PSGuid();
        
        return metadataDeliveryHandler
            .createItemForTest(fakeGuid, file, mimeType, 100L, removal, JOB_ID, PUB_SERVER_ID, DELIVERY_CONTEXT);
    }
    
    /**
     * Returns an IOFileFilter object to list files in the directory where pages
     * reside. It looks for file with .htm and .html extensions, and also without
     * extension.
     * 
     * @return A IOFileFilter object with filter settings.
     *
    private IOFileFilter getFileFilters()
    {
        Collection<IOFileFilter> filters = new ArrayList<IOFileFilter>();

        IOFileFilter directories = FileFilterUtils.directoryFileFilter();
        filters.add(FileFilterUtils.and(directories, HiddenFileFilter.VISIBLE));

        // Add filter for each file extension.
        for (String fileExtension : new String[] { "htm", "html"})
        {
            filters.add(FileFilterUtils.suffixFileFilter(fileExtension, IOCase.INSENSITIVE));
        }

        // Catch files with no extensions too
        filters.add(FileFilterUtils.asFileFilter(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return (pathname != null && !pathname.getName().contains("."));
            }
        }));

        return FileFilterUtils.or((IOFileFilter[]) filters.toArray(new IOFileFilter[0]));
    }
    
    @Test
    public void testPerformance() throws Exception
    {
        File directoryWithPages = new File(PAGES_DIRECTORY);
        
        if (!directoryWithPages.exists())
            return;
        
        System.out.println("Searching for files in directory: " +
                directoryWithPages.getAbsolutePath());
        
        Collection<File> pagesToProcess =
            FileUtils.listFiles(directoryWithPages, getFileFilters(),
                    FileFilterUtils.trueFileFilter());
        
        // Send each page to the metadata delivery handler, and through it,
        // to the remote extractor REST service.
        for (File aPage : pagesToProcess)
        {
            System.out.println("Page: " + aPage.getAbsolutePath());
            PSPurgableTempFile f = new PSPurgableTempFile("test-", ".txt", null);
            FileUtils.writeStringToFile(f, FileUtils.readFileToString(aPage));
            
            Item item = createItem(f, "text/html", false);
            
            IPSDeliveryResult dr = null;
            try
            {
                //System.out.println("  Doing delivery");
                String location = "/" + directoryWithPages.toURI().relativize(aPage.toURI()).getPath();
                dr = metadataDeliveryHandler.doDelivery(item, JOB_ID, location);
            }
            catch (Exception e)
            {
                System.out.println("  Exception thrown: " + e.getMessage());
                e.printStackTrace();
                continue;
            }
            
            if (dr.getOutcome() == Outcome.FAILED)
               System.out.println("  ERROR: " + dr.getFailureMessage());
            
            //assertEquals(Outcome.DELIVERED, dr.getOutcome());
        }
    }
    
    public static void main(String[] args)
    {
       PSMetadataDeliveryHandlerPerformanceTest perf = new PSMetadataDeliveryHandlerPerformanceTest();
       try
       {
          perf.setUp();
          perf.testPerformance();
          perf.tearDown();
          
          System.out.println();
          System.out.println("Finished");
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
    }
    */
}
