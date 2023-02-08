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
package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

/**
 * @author DavidBenua
 *
 */
@XmlRootElement(name="SitePublishLogDetailsRequest")
@JsonRootName("SitePublishLogDetailsRequest")
public class PSSitePublishLogDetailsRequest extends PSAbstractDataObject {
   private long jobid; 
   private int skipCount;
   private boolean showOnlyFailures;
/**
 * @return the jobid
 */
public long getJobid() {
	return jobid;
}
/**
 * @param jobid the jobid to set
 */
public void setJobid(long jobid) {
	this.jobid = jobid;
}
/**
 * @return the skipCount
 */
public int getSkipCount() {
	return skipCount;
}
/**
 * @param skipCount the skipCount to set
 */
public void setSkipCount(int skipCount) {
	this.skipCount = skipCount;
}
/**
 * @return the showOnlyFailures
 */
public boolean isShowOnlyFailures() {
	return showOnlyFailures;
}
/**
 * @param showOnlyFailures the showOnlyFailures to set
 */
public void setShowOnlyFailures(boolean showOnlyFailures) {
	this.showOnlyFailures = showOnlyFailures;
} 
   
   
}
