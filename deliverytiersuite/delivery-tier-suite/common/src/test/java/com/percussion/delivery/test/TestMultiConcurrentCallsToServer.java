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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.test;

import com.percussion.error.PSExceptionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Performs simulation of multiple clients making calls to DTS
 * @author Santosh Dhariwal
 *
 */
@Ignore
public class TestMultiConcurrentCallsToServer {

    private static final Logger log = LogManager.getLogger(TestMultiConcurrentCallsToServer.class);
    private static String deliveryServerUrl = "http://localhost:9980/perc-metadata-services/metadata/indexedDirectories";

        public void makeConcurrentClientRequests(){

            ExecutorService executor = Executors.newFixedThreadPool(150);
            List<Future<String>> list = new ArrayList<Future<String>>();

            for(int i=0; i< 200; i++){
                HttpClient  httpClient = new HttpClient();
                Callable<String> callable = new ThreadLocalRunner(i+1,httpClient);
                Future<String> future = executor.submit(callable);
                list.add(future);
            }
            for(Future<String> fut : list){
                try {
                    log.info("{} :: {}", new Date(), fut.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(e);
                    Assert.assertFalse(true);
                    Thread.currentThread().interrupt();
                }
            }
            //shut down the executor service now
            executor.shutdown();
        }

        public String makeRestRequest(int num,HttpClient httpClient){
            try
            {
                GetMethod get = new GetMethod(deliveryServerUrl);
                get.setRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
                get.setRequestHeader("Accept", MediaType.APPLICATION_JSON);

                try
                {
                    httpClient.executeMethod(get);
                    String resp =  get.getResponseBodyAsString();

                    Assert.assertEquals(200, get.getStatusCode());
                    String returnVal = "Request Was :" + num + " : "  + get.getStatusCode();
                    return returnVal;
                }
                finally
                {
                    get.releaseConnection();
                }


            }
            catch (Exception e)
            {
                log.error(PSExceptionUtils.getMessageForLog(e));
                log.debug(e);
                Assert.assertFalse(true);
            }
            return "ERROR";
        }


    class ThreadLocalRunner implements Callable<String> {
        private int num;
        private HttpClient httpClient;

        @Override
        public String call() throws Exception {
            return makeRestRequest(num,httpClient);
        }

        public ThreadLocalRunner(int num,HttpClient httpClient)
        {
            this.num = num;
            this.httpClient = httpClient;

        }


    }
}


