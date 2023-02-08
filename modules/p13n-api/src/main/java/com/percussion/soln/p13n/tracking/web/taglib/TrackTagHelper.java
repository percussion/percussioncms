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
