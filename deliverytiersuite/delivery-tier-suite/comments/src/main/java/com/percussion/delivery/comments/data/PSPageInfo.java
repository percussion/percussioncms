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

package com.percussion.delivery.comments.data;

public class PSPageInfo {
   
	private String pagePath;
	private String approvalState;
	private long commentCount;
	private boolean viewed;
	
	
	public PSPageInfo(String pagePath, String approvalState, long commentCount, boolean viewed)
	{
		this.pagePath = pagePath;
		this.approvalState = approvalState;
		this.commentCount = commentCount;
		this.viewed = viewed;
	}
	
	public String getPagePath()
	{
		return pagePath;
	}
	public String getApprovalState()
	{
		return approvalState;
	}
	public long getCommentCount()
	{
		return commentCount;
	}

	public boolean isViewed() {
		return viewed;
	}
	
	
	
}
