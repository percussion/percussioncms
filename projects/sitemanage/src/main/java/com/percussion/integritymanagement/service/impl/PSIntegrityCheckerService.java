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

package com.percussion.integritymanagement.service.impl;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.service.impl.PSAssetService;
import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.integritymanagement.data.PSIntegrityStatus.Status;
import com.percussion.integritymanagement.data.PSIntegrityTask;
import com.percussion.integritymanagement.data.PSIntegrityTask.TaskStatus;
import com.percussion.integritymanagement.data.PSIntegrityTaskProperty;
import com.percussion.integritymanagement.service.IPSIntegrityCheckerService;
import com.percussion.itemmanagement.service.impl.PSItemWorkflowService;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pubserver.impl.PSPubServerService;
import com.percussion.rx.delivery.impl.PSAmazonS3DeliveryHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.service.IPSSiteDataService.PublishType;
import com.percussion.user.service.impl.PSUserService;
import com.percussion.utils.PSDTSStatusProvider;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.service.impl.PSUtilityService;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.PSWebserviceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("integrityCheckerService")
public class PSIntegrityCheckerService implements IPSIntegrityCheckerService
{

    @Autowired
    private PSIntegrityCheckerDao integrityDao;

    @Autowired
    private PSDTSStatusProvider dtsStatusProvider;
    
    @Autowired 
    private PSAssetService assetService;
    
    @Autowired 
    private PSItemWorkflowService itemWorkflowService;
    
    @Autowired
    private PSUserService userService;
    
    @Autowired
    private PSUtilityService utilityService;
    
    @Autowired
    private IPSSiteManager siteMgr;
    
    @Autowired
    private PSPubServerService pubServerService;
    
    @Override
    public synchronized String start(final IntegrityTaskType type) throws PSDataServiceException {
        ms_log.info("Started integrity checker.");
        validateUsage();
        PSIntegrityStatus status = getRunningStatus();
        request = PSWebserviceUtils.getRequest();
        request = request.cloneRequest();
        if(status == null){
            status = new PSIntegrityStatus();
            final String token = UUID.randomUUID().toString();
            status.setToken(token);
            status.setStartTime(new Date());
            status.setStatus(Status.RUNNING);
            integrityDao.save(status);
            Runnable r = new Runnable() {
                public void run() {
                    try{
                        if (PSRequestInfo.isInited()) {
                            PSRequestInfo.resetRequestInfo();
                        }
                        PSRequestInfo.initRequestInfo(request.getServletRequest());
                        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, request);
                        runTasks(type);
                    }
                    catch(Exception e){
                        ms_log.info("Error starting the integration tasks", e);
                    }
                }
            };
            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(r);        
        }
        return status.getToken();
    }

    private void runTasks(IntegrityTaskType type) throws IPSGenericDao.SaveException {
        ms_log.info("Started running the tasks.");
        PSIntegrityStatus status = getRunningStatus();
        if (status != null)
        {
            Set<PSIntegrityTask> tasks = new HashSet<>();
            Status topSt = Status.SUCCESS;
            if (type.equals(IntegrityTaskType.all) || type.equals(IntegrityTaskType.cm1))
            {
                try{
                    tasks.addAll(getCmsTasks(status.getToken()));
                }
                catch(Exception e){
                    topSt = Status.FAILED;
                    ms_log.error("Error occurred running cms tasks", e);
                }
            }
            if (type.equals(IntegrityTaskType.all) || type.equals(IntegrityTaskType.dts))
            {
                try{
                    tasks.addAll(getDtsTasks(status.getToken()));
                }
                catch(Exception e){
                    topSt = Status.FAILED;
                    ms_log.error("Error occurred running dts tasks", e);
                }
            }
            // Roll up the failure status, if any of the tasks fail then we
            // should mark the top status as failed.
            if (topSt.equals(Status.SUCCESS))
            {
                for (PSIntegrityTask task : tasks)
                {
                    if (task.getStatus().equals(TaskStatus.FAILED))
                    {
                        topSt = Status.FAILED;
                        break;
                    }
                }
            }
            status.setStatus(topSt);
            status.setTasks(tasks);
            status.setEndTime(new Date());
            integrityDao.save(status);
        }
    }
    
    private Set<PSIntegrityTask> getDtsTasks(String token){
        Set<PSIntegrityTask> dtsTasks = new HashSet<>();
        Map<String,PSPair<TaskStatus, String>> dtsStatus = dtsStatusProvider.getDTSStatusReport();
        for (String name : dtsStatus.keySet())
        {
            PSIntegrityTask task = new PSIntegrityTask();
            task.setType(IntegrityTaskType.dts.toString());
            task.setName(name);
            task.setStatus(dtsStatus.get(name).getFirst());
            task.setMessage(dtsStatus.get(name).getSecond());
            task.setToken(token);
            dtsTasks.add(task);
        }
        return dtsTasks;
    }
    
    private Set<PSIntegrityTask> getCmsTasks(String token){
        Set<PSIntegrityTask> cmsTasks = new HashSet<>();
        Map<String,PSPair<TaskStatus, String>> cm1Status = runImageTasks(token);
        for (String name : cm1Status.keySet())
        {
            PSIntegrityTask task = new PSIntegrityTask();
            task.setType(IntegrityTaskType.cm1.toString());
            task.setName(name);
            task.setStatus(cm1Status.get(name).getFirst());
            task.setMessage(cm1Status.get(name).getSecond());
            task.setToken(token);
            cmsTasks.add(task);
        }
        cmsTasks.addAll(runImagePublishTasks(token));
        return cmsTasks;
    }

    @Override
    public void stop() throws PSDataServiceException {
        validateUsage();
        PSIntegrityStatus status = getRunningStatus();
        if(status != null){
            status.setStatus(Status.CANCELLED);
            status.setEndTime(new Date());
            integrityDao.save(status);
        }
    }
    
    private PSIntegrityStatus getRunningStatus() throws IPSGenericDao.SaveException {
        PSIntegrityStatus result = null;
        List<PSIntegrityStatus> statuses = integrityDao.find(Status.RUNNING);
        if(statuses.size()==1){
            result = statuses.get(0);
        }
        else if(statuses.size()>1){
            for (int i=1; i<statuses.size(); i++)
            {
                PSIntegrityStatus status = statuses.get(i);
                status.setStatus(Status.CANCELLED);
                integrityDao.save(status);
            }
        }
        return result;
    }
    
    @Override
    public PSIntegrityStatus getStatus(String token) throws PSDataServiceException {
        validateUsage();
        return integrityDao.find(token);
    }

    @Override
    public List<PSIntegrityStatus> getHistory() throws PSDataServiceException {
        validateUsage();
        return getHistory(null);
    }

    @Override
    public List<PSIntegrityStatus> getHistory(Status status) throws PSDataServiceException {
        validateUsage();
        return integrityDao.find(status);
    }

    @Override
    public void delete(String token) throws PSDataServiceException {
        validateUsage();
        PSIntegrityStatus status = integrityDao.find(token);
        if(status != null){
            integrityDao.delete(status);
        }
    }
    
    /**
     * Helper method to run the image tasks
     * @param token assumed not <code>null</code>
     * @return Map<String, PSPair<TaskStatus, String>> map of task name and pair of status and message.
     */
    private Map<String, PSPair<TaskStatus, String>> runImageTasks(String token) {
        Map<String, PSPair<TaskStatus, String>> result = new HashMap<>();
        PSAsset percussionImage = null;
        try(InputStream in = new FileInputStream(PSServer.getRxDir().getAbsolutePath() + PSAmazonS3DeliveryHandler.PERC_TEST_IMG_DIR
                    + PSAmazonS3DeliveryHandler.PERC_TEST_IMG)){
            PSAbstractAssetRequest ar = new PSBinaryAssetRequest(PSAssetPathItemService.ASSET_ROOT + "/uploads",
                    AssetType.IMAGE, PSAmazonS3DeliveryHandler.generateTestImageKey(token), "image/jpeg",
                    in);

            percussionImage = assetService.createAsset(ar);

            PSPair<TaskStatus, String> imgCreate = new PSPair<>(TaskStatus.SUCCESS,"");
            result.put(IMAGE_CREATE_TASK, imgCreate);
        } catch (Exception e) {
            PSPair<TaskStatus, String> imgCreate = new PSPair<>(TaskStatus.FAILED, e.getMessage());
            result.put(IMAGE_CREATE_TASK, imgCreate);
            //Return the result here
            return result;
        }

        //Approve Image Asset
        try {
            itemWorkflowService.performApproveTransition(percussionImage.getId(), false, "");

            PSPair<TaskStatus, String> imgApprove = new PSPair<>(TaskStatus.SUCCESS,"");
            result.put(IMAGE_APPROVE_TASK, imgApprove);
        } catch (Exception e) {
            PSPair<TaskStatus, String> imgApprove = new PSPair<>(TaskStatus.FAILED, e.getMessage());
            result.put(IMAGE_APPROVE_TASK, imgApprove);
        }
        
        //Delete Image from cm1
        try {
            assetService.delete(percussionImage.getId());
            
            PSPair<TaskStatus, String> imgDelete = new PSPair<>(TaskStatus.SUCCESS, "");
            result.put(IMAGE_DELETE_TASK, imgDelete);
        } catch (Exception e) {
            PSPair<TaskStatus, String> imgDelete = new PSPair<>(TaskStatus.FAILED, e.getMessage());
            result.put(IMAGE_DELETE_TASK, imgDelete);
        }

        return result;
    }
    
    private void validateUsage() throws PSDataServiceException {
        if(!userService.isAdminUser(userService.getCurrentUser().getName())){
            throw new RuntimeException("You are not authorized to use " + PSIntegrityCheckerService.class.getName() + " API.");
        }
        if(!utilityService.isSaaSEnvironment())
            throw new RuntimeException("The " + PSIntegrityCheckerService.class.getName() + " API is not supported in your environment.");
    }
    
    /**
     * Helper method to run the image tasks
     * @param token assumed not <code>null</code>
     * @return Map<String, PSPair<TaskStatus, String>> map of task name and pair of status and message.
     */
    private Set<PSIntegrityTask> runImagePublishTasks(String token)
    {
        Set<PSIntegrityTask> result = new HashSet<>();
        try
        {
            PSAmazonS3DeliveryHandler delHandler = new PSAmazonS3DeliveryHandler();
            // get all sites
            List<IPSSite> sites = siteMgr.findAllSites();
            for (IPSSite site : sites)
            {
                // get pubserver for each site
                PSPubServer pubServer = pubServerService.getDefaultPubServer(site.getGUID());
                if(pubServer == null)
                    continue;
                String pubType = pubServer.getPublishType();
                if(!(equalsIgnoreCase(pubType, PublishType.amazon_s3.toString())
                || equalsIgnoreCase(pubType, PublishType.amazon_s3_only.toString()))){
                    continue;
                }
                Set<PSIntegrityTaskProperty> taskProps = new HashSet<>();
                taskProps.add(new PSIntegrityTaskProperty("sitename", site.getName()));
                try{
                    PSPair<Boolean, String> pub = delHandler.publishTestImage(pubServer, site, token);
                    PSIntegrityTask task = new PSIntegrityTask();
                    task.setName(IMAGE_PUBLISH_TASK + ":" + site.getName());
                    task.setToken(token);
                    task.setStatus(pub.getFirst() ? TaskStatus.SUCCESS : TaskStatus.FAILED);
                    task.setMessage(pub.getSecond());
                    task.setType(IntegrityTaskType.cm1.toString());
                    task.setTaskProperties(taskProps);
                    result.add(task);
                }
                catch(Exception e){
                    PSIntegrityTask task = createErrorTask(IMAGE_PUBLISH_TASK + ":" + site.getName(),token,IntegrityTaskType.cm1.toString(),e); 
                    task.setTaskProperties(taskProps);
                    result.add(task);
                }
            }
        }
        catch (Exception e)
        {
            PSIntegrityTask task = createErrorTask(IMAGE_PUBLISH_TASK,token,IntegrityTaskType.cm1.toString(),e); 
            result.add(task);
        }
        // call
        return result;
    }
    private PSIntegrityTask createErrorTask(String name, String token, String type, Exception e)
    {
        PSIntegrityTask task = new PSIntegrityTask();
        task.setName(name);
        task.setToken(token);
        task.setStatus(TaskStatus.FAILED);
        task.setMessage(e.getLocalizedMessage());
        task.setType(type);
        return task;
        
    }
    private PSRequest request;
    private static final String IMAGE_CREATE_TASK = "Image-Create";
    private static final String IMAGE_DELETE_TASK = "Image-Delete";
    private static final String IMAGE_APPROVE_TASK = "Image-Approve";
    private static final String IMAGE_PUBLISH_TASK = "Image-Publish";
    private static final Logger ms_log = LogManager.getLogger(PSIntegrityCheckerService.class);


}
