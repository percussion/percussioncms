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
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
                log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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


