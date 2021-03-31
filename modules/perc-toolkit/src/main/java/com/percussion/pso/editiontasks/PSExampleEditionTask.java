package com.percussion.pso.editiontasks;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.sitemgr.IPSSite;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class PSExampleEditionTask implements IPSEditionTask {
	
	private static Logger logger = Logger.getLogger(PSExampleEditionTask.class);

	public void init(IPSExtensionDef def, File codeRoot) {
		// No initialization required
	}

	public TaskType getType() {
		return TaskType.POSTEDITION;
	}

	public void perform(IPSEdition edition, IPSSite site, Date starTime, Date endTime,
			long jobId, long duration, boolean success, Map<String, String> params,
			IPSEditionTaskStatusCallback status) throws Exception {
		
		logger.info("*********PSExampleEditionTaskStarting*********");
		
		Iterable<IPSPubItemStatus> jobPages = status.getIterableJobStatus();
		String siteBaseURL = StringUtils.removeEnd(site.getBaseUrl(), "/");
		String url = "";
		
		for(IPSPubItemStatus page : jobPages) {
			
			url = page.getLocation();

			if (page.getStatus().equals(IPSSiteItem.Status.SUCCESS)) {
				logger.info(siteBaseURL + url);
				// here the URL can be sent to any service/end point using
				// commons HTTP client, etc.
			}
		} // end for loop
		
	} // end method perform
	
}
