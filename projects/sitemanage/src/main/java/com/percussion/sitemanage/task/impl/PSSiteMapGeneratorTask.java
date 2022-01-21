package com.percussion.sitemanage.task.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Post Edition task for generating a sitemap when publishing is concluded.
 */
public class PSSiteMapGeneratorTask implements IPSEditionTask {

    private static final Logger log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);

    IPSPublisherService publisherService;
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

        List<IPSEditionContentList> contentLists = publisherService.loadEditionContentLists(edition.getGUID());

        if(!success){
            log.warn("Skipping sitemap generation for failed job: {}", jobId);
            return;
        }

        //Detect if this is a "normal" edition - publish now, auto publish, and incremental should always be skipped.
        if(contentLists != null && ! contentLists.isEmpty()){
            for(IPSEditionContentList ecl : contentLists){
                IPSContentList cl = publisherService.loadContentList(ecl.getContentListId());
                if(cl.getType() == IPSContentList.Type.INCREMENTAL){
                    //If this is an incremental edition we want to skip generation
                    log.warn("Skipping sitemap generation for incremental edition. ");
                    return;
                }
                if(cl.getEditionType().equals(PSEditionType.AUTOMATIC)){
                    log.warn("Skipping sitemap generation for automatic edition.");
                    return;
                }
                if(cl.getGenerator().equalsIgnoreCase("Java/global/percussion/system/sys_SelectedItemsGenerator")){
                    log.warn("Skipping sitemap generation for Publish Now edition.");
                    return;
                }
            }
        }

        //Get the list of published items.
        Iterable<IPSPubItemStatus> it =  publisherService.findPubItemStatusForJobIterable(jobId);

        while(it.iterator().hasNext()){
            IPSPubItemStatus s = it.iterator().next();

            if(s.getStatus().equals(IPSSiteItem.Status.SUCCESS)){
                s.getOperation()
            }else{

            }
        }
    }

    /**
     * Discover when the extension can be used.
     *
     * @return the type as specified in {@link TaskType}.
     */
    @Override
    public TaskType getType() {
        return null;
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
