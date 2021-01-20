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

package com.percussion.sitemanage.importer.utils;

import com.percussion.queue.impl.PSPageImportQueue;
import com.percussion.server.PSRequest;
import com.percussion.sitemanage.importer.utils.PSFileDownLoadJobRunner;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class PSAsyncFileDownload
{

    
    // ================= Begin Main Class ==========================================
    private boolean m_complete = false;

    private Log m_log = LogFactory.getLog(PSPageImportQueue.class);

    private List<PSPair<Boolean, String>> results = new ArrayList<PSPair<Boolean, String>>();

    private List<PSFileDownloadJob> jobs = new ArrayList<PSFileDownloadJob>();

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
        ArrayList<PSFileDownLoadJobRunner> runningJobs = new ArrayList<PSFileDownLoadJobRunner>();
        List<Thread> threads = new ArrayList<Thread>();
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
