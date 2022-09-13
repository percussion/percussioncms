package com.percussion.soln.p13n.tracking.web.taglib;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TrackTagHelper {
	/**
	 * The log instance to use for this class, never <code>null</code>.
	 */
	private static final Log log = LogFactory.getLog(TrackTagHelper.class);

	

	protected static Map<String, Integer> parseSegmentWeights(String segmentWeights) {
        Map<String, Integer> weights = new HashMap<String, Integer>();

        if (segmentWeights != null) {
            String[] str = segmentWeights.split(",");
            for (int i = 0; i < str.length; i++) {
                String segmentName;
                String segmentValue;
                if (str[i].contains(":")) {
                    String[] nv = str[i].split(":");
                    segmentName = nv[0];
                    segmentValue = nv[1];
                } else {
                    segmentName = str[i];
                    segmentValue = "1";
                }
                try {
                    weights.put(segmentName, Integer.parseInt(segmentValue));
                } catch (NumberFormatException e) {
                    String error = "One of the segment weights is not an integer for segment: " + segmentName + " skipping it.";
                    log.error(error);
                }
            }
        }
        return weights;

    }
	
	protected static String createWebBug(String requestURI, String action,
			String segmentWeights) {
		String webBugLink = requestURI + ".gif?actionName=" + action;
		String webBugHtml = "";
		if (segmentWeights != null && segmentWeights.length()>0) {
			String[] str = segmentWeights.split(",");

			for (int i = 0; i < str.length; i++) {
				String segmentName;
				String segmentValue;
				if (str[i].contains(":")) {
					String[] weights = str[i].split(":");
					segmentName = weights[0];
					segmentValue = weights[1];
				} else {
					segmentName = str[i];
					segmentValue = "1";
				}
				webBugLink += "&segmentWeights[" + segmentName + "]="
						+ segmentValue;
			}
	}
			
				webBugHtml = "<img height=\"1\" width=\"1\" src=\""
						+ webBugLink + "\">";
		
		return webBugHtml;
	}
	

}
