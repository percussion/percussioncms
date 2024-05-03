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

package com.percussion.sitemanage.task.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.cms.IPSConstants;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pathmanagement.data.PSGenerateSiteMapOptions;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.rx.delivery.impl.PSLocalDeliveryManager;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSRevisions;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.data.PSItemPublishingHistory;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Post Edition task for generating a sitemap when publishing is concluded.
 */
public class PSSiteMapGeneratorTask implements IPSEditionTask {

    private static final Logger log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);
    public static final String EXTENSION_NAME="Java/global/percussion/task/perc_SitemapGeneratorTask";

    @Autowired
    IPSPublisherService publisherService;

    @Autowired
    IPSPubServerService pubServerService;

    /**
     * Perform the task, either before or after the edition is run, depending on
     * the registration.
     * <h3>Implementation notes</h3>
     * Note for each parameter whether the parameter is
     * available given a usage.
     * <p>
     * Post edition tasks may also wish to retrieve
     * status information from the service and change behavior according to
     * whether a particular item published successfully or not.
     *
     * @param edition   the edition description, never <code>null</code>
     * @param site      the site description, never <code>null</code>
     * @param startTime the time when the edition started to run, this is the
     *                  time at which the job was spawned, which is to say the initial
     *                  time before the first task is called, never <code>null</code>.
     * @param endTime   the time when the job completed, before the first post
     *                  task is invoked. This time is only available to post tasks and
     *                  will be <code>null</code> for pre edition tasks.
     * @param jobId     the job id.
     * @param duration  the length of time that the edition ran in seconds, from
     *                  the first moment <i>after</i> the pre tasks completed to the
     *                  moment just before the first post edition tasks started.
     *                  Supplied as <code>0</code> to pre edition tasks.
     * @param success   if <code>true</code> then the edition was successful,
     *                  which means that all items published without error. If
     *                  <code>false</code> then some or all items failed and the
     *                  status callback or other service calls must be used to
     *                  determine what failures existed. Undefined for pre edition
     *                  tasks.
     * @param params    registered parameters for the task, may be empty or
     *                  <code>null</code> for tasks that don't require parameters.
     * @param status    this is <code>null</code> for pre tasks, but post tasks
     *                  can use this to obtain status information about the job.
     * @throws Exception if the task fails for any reason an exception should
     *                   be thrown that details the reason for the failure. The
     *                   exception will be caught by the job code and recorded as part
     *                   of the edition task.
     */
    @Override
    public void perform(IPSEdition edition, IPSSite site, Date startTime, Date endTime, long jobId, long duration, boolean success, Map<String, String> params, IPSEditionTaskStatusCallback status) throws Exception {

        if(site.isGenerateSitemap()) {

            ObjectMapper mapper = new ObjectMapper();
            String jsonString = site.getGenerateSiteMapOptions();
            PSGenerateSiteMapOptions psGenerateSiteMapOptions = null;
            try {
                psGenerateSiteMapOptions = mapper.readValue(jsonString, PSGenerateSiteMapOptions.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            long count = 0;
            List<IPSEditionContentList> contentLists = publisherService.loadEditionContentLists(edition.getGUID());

            //Detect if this is a "normal" edition - publish now, auto publish, and incremental should always be skipped.
            if (contentLists != null && !contentLists.isEmpty()) {
                for (IPSEditionContentList ecl : contentLists) {
                    IPSContentList cl = publisherService.loadContentList(ecl.getContentListId());
                    if (cl.getType() == IPSContentList.Type.INCREMENTAL) {
                        //If this is an incremental edition we want to skip generation
                        log.warn("Skipping sitemap generation for incremental edition. ");
                        return;
                    }

                    String generator = cl.getGenerator();
                    if (generator != null && generator.equalsIgnoreCase("Java/global/percussion/system/sys_SelectedItemsGenerator")) {
                        log.warn("Skipping sitemap generation for Publish Now edition.");
                        return;
                    }
                }
            }

            File siteMapDir = new File(getSiteMapDirForJob(jobId));
            siteMapDir.mkdirs();
            WebSitemapGenerator wsg = WebSitemapGenerator.builder(site.getBaseUrl(),
                    siteMapDir).build();

            String excludeImage = null;
            if (psGenerateSiteMapOptions !=null){
                excludeImage=  psGenerateSiteMapOptions.getGenerateSitemapExcludeImage();
            }

            IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
            IPSGuidManager ipsGuidManager = PSGuidManagerLocator.getGuidMgr();

            for (IPSPubItemStatus s : status.getIterableJobStatus()) {
                //bypassing assets bases on user preference (set in site navigation -> site preference)
                if(s.getLocation().startsWith("/Assets")){
                    if (excludeImage!=null && excludeImage.equals("true")  ) {
                        continue;
                    }
                }

                PSLocator psLocator = new PSLocator(s.getContentId());
                psLocator.setRevision(s.getRevisionId());

                IPSGuid ipsGuid = ipsGuidManager.makeGuid(psLocator);
                List<IPSGuid> guids = (List<IPSGuid>) ipsGuid;
                List<javax.jcr.Node> nodeList =  contentMgr.findItemsByGUID(guids,null);

                if (s.getStatus().equals(IPSSiteItem.Status.SUCCESS) &&
                        s.getOperation().equals(IPSSiteItem.Operation.PUBLISH)) {
                    addToSiteMap(wsg, site, s);
                    count++;
                } else {
                    if (isPriorVersionLiveV2(s) && s.getOperation().equals(IPSSiteItem.Operation.PUBLISH)) {
                        //a previous version was live - so even tho this one failed we should include it in the sitemap
                        addToSiteMap(wsg, site, s);
                        count++;
                    }
                    log.debug("Not including failed item {} in sitemap", s.getLocation());
                }
            }

            wsg.write();
            wsg.writeSitemapsWithIndex();
            log.info("Wrote {} entries to generated sitemap.", count);
        }
    }

    public static String getSiteMapDirForJob(long jobId){
        return PSServer.getRxDir() + File.separator + PSLocalDeliveryManager.DEFAULT_TMP_DIR + File.separator + jobId +
                File.separator + "sitemaps";
    }
    protected static void addToSiteMap(WebSitemapGenerator wsg, IPSSite site, IPSPubItemStatus s){
        String locURL =getCanonicalLocation(site, s.getLocation());
        if (locURL.startsWith("/"))
            locURL = locURL.substring(1);
        wsg.addUrl(site.getBaseUrl() + locURL);
    }

    protected static String getCanonicalLocation(IPSSite site, String location){
        if(site.isCanonical() &&
                site.getCanonicalDist().equalsIgnoreCase("sections") &&
                location.endsWith(site.getDefaultDocument())) {
                return location.substring(0, location.lastIndexOf(site.getDefaultDocument()));
        }
        return location;
    }

    protected static boolean isPriorVersionLiveV2(IPSPubItemStatus status){

        IPSPublisherService pubSvc = PSPublisherServiceLocator.getPublisherService();

        List<PSItemPublishingHistory> history = pubSvc.findItemPublishingHistory(new PSLegacyGuid(status.getContentId(), status.getRevisionId()));

        if(history == null || history.isEmpty()) {
            return false;
        }
        history.sort(Comparator.comparing(PSItemPublishingHistory::getPublishedDate).reversed());
        for(PSItemPublishingHistory h : history){
            if(h.getStatus().equalsIgnoreCase(IPSSiteItem.Status.SUCCESS.name())
            && h.getLocation().equalsIgnoreCase(status.getLocation())
            && h.getOperation().equalsIgnoreCase(IPSSiteItem.Operation.PUBLISH.name())){
                return  true;
            }else if(
                    h.getStatus().equalsIgnoreCase(IPSSiteItem.Status.SUCCESS.name())
                            && h.getLocation().equalsIgnoreCase(status.getLocation())
                            && h.getOperation().equalsIgnoreCase(IPSSiteItem.Operation.UNPUBLISH.name())
            ){
                return false;
            }
        }
        return true;
    }


    protected static boolean isPriorVersionLive(IPSPubItemStatus status){

        IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
        IPSContentWs ws = PSContentWsLocator.getContentWebservice();
        List<PSRevisions> revs = ws.findRevisions(Collections.singletonList(new PSLegacyGuid(status.getContentId(), status.getRevisionId())));
        if(revs != null && !revs.isEmpty()) {
            PSRevisions r = revs.get(0);
            List<PSContentStatusHistory> history = r.getRevisions();
            if(history!=null && !history.isEmpty()) {
                history.sort(Comparator.comparing(PSContentStatusHistory::getRevision).reversed());

                for (PSContentStatusHistory csh : history) {

                    IPSWorkflowService wfsvc = PSWorkflowServiceLocator.getWorkflowService();
                    IPSGuid wfGuid = guidMgr.makeGuid(csh.getWorkflowId(), PSTypeEnum.WORKFLOW);
                    IPSGuid stateGuid = guidMgr.makeGuid(csh.getStateId(), PSTypeEnum.WORKFLOW_STATE);

                    PSState state = wfsvc.loadWorkflowState(stateGuid, wfGuid);
                    //Archive state
                    if(state.getContentValidValue().equalsIgnoreCase("u")){
                        return false;
                    }
                    if(state.isPublishable() &&
                        (state.getName().equalsIgnoreCase("live")
                                ||
                                state.getName().equalsIgnoreCase("public")
                            ||
                                state.getName().equalsIgnoreCase("publish"))){
                        return true;
                        }
                    }
            }
        }
        return false;
    }
    /**
     * Discover when the extension can be used.
     *
     * @return the type as specified in {@link TaskType}.
     */
    @Override
    public TaskType getType() {
        return TaskType.POSTEDITION;
    }

    /**
     * Initializes this extension.
     * <p>
     * Note that the extension will have permission to read
     * and write any files or directiors under <CODE>codeRoot</CODE>
     * (recursively). The extension will not have permissions for
     * any other files or directories.
     *
     * @param def      The extension def, which contains configuration
     *                 info and initialization params.
     * @param codeRoot The root directory where this extension
     *                 should install and look for any files relating to itself. The
     *                 subdirectory structure under codeRoot is left up to the
     *                 extension implementation. Must not be <CODE>null</CODE>.
     * @throws PSExtensionException     If the codeRoot does not exist,
     *                                  or is not accessible. Also thrown for any other initialization
     *                                  errors that will prohibit this extension from doing its job
     *                                  correctly, such as invalid or missing properties.
     * @throws IllegalArgumentException If any param is invalid.
     */
    @Override
    public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
}
