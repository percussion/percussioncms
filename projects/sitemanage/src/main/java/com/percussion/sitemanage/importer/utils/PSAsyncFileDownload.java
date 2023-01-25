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

package com.percussion.sitemanage.importer.utils;

import com.percussion.queue.impl.PSPageImportQueue;
import com.percussion.server.PSRequest;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class PSAsyncFileDownload
{

    
    // ================= Begin Main Class ==========================================
    private boolean m_complete = false;

    private static final Logger m_log = LogManager.getLogger(PSPageImportQueue.class);

    private List<PSPair<Boolean, String>> results = new ArrayList<>();

    private List<PSFileDownloadJob> jobs = new ArrayList<>();

    private Integer MAX_THREADS = 6;

    private Map<String, Object> m_requestMap;

    public boolean hasCompleted()
    {
        return m_complete;
    }


    public PSAsyncFileDownload(Map<String, Object> requestMap)
    {
        this.m_requestMap = requestMap;
    }
    
    public void addDownload(String filePath, String url, boolean createAsset)
    {
        PSFileDownloadJob job = new PSFileDownloadJob(filePath, url, createAsset);
        jobs.add(job);
    }

    public void download()
    {

        this.setRequestInfo(this.m_requestMap);
        Iterator<PSFileDownloadJob> i = jobs.iterator();
        ArrayList<PSFileDownLoadJobRunner> runningJobs = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        while (i.hasNext())
        {
            if (runningJobs.size() < MAX_THREADS)
            {
            	PSFileDownloadJob job = i.next();
                final Map<String, Object> requestInfoMap = PSRequestInfo.copyRequestInfoMap();
                PSRequest request = (PSRequest) requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
                requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());
                PSFileDownLoadJobRunner download = new PSFileDownLoadJobRunner(job, requestInfoMap);
                Thread t = new Thread(download);
                t.setDaemon(true);
                t.start();
                runningJobs.add(download);
                threads.add(t);
                
                if(runningJobs.size() == MAX_THREADS || !i.hasNext())
                {
                    for (Thread thread : threads)
                    {
                        try
                        {
                            thread.join();
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                    threads.clear();
                    
                    for (PSFileDownLoadJobRunner runningJob : runningJobs )
                    {
                        results.addAll(runningJob.getResults());
                    }
                    runningJobs.clear();
                }
                    
            }
            else
            {
            	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
				}
            }
            
        }    
    }

    public void setRequestInfo(Map<String, Object> requestInfoMap)
    {
        if (PSRequestInfo.isInited())
        {
            PSRequestInfo.resetRequestInfo();
        }
        PSRequestInfo.initRequestInfo(requestInfoMap);
    }

    public List<PSPair<Boolean, String>> getResults()
    {
        return results;
    }
}
