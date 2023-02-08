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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import com.percussion.soln.p13n.tracking.VisitorProfile;
import com.percussion.soln.p13n.tracking.web.TrackLoginRestClient;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;

/**
 *  This tag is used to handle login for user registration systems such as portals or e-commerce sites 
 * where user registration is required.
 * <p>
 * This is done by merging the current anonymous profile with an existing profile of the given userId.
 * If the current profile is not anonymous and has the same userId as the request then nothing will happen.
 * <p>
 * @author adamgent
 *
 */
public class LoginTag implements Tag{

	private PageContext pageContext;
	private Tag parent;
	private String requestURI;
	private String userName;
	private String segmentWeights;
	private String label;

	public LoginTag() {
		super();
	}

	public int doStartTag() throws javax.servlet.jsp.JspTagException
	{
		return SKIP_BODY;
	}

	public int doEndTag() throws javax.servlet.jsp.JspTagException
	{

		HttpSession session = pageContext.getSession();

		VisitorProfile profile = VisitorTrackingWebUtils.getVisitorProfileFromSession(session);
		//String profileId="";
		String profileUserName = null;
		if (profile != null) {
			//	profileId = String.valueOf(profile.getId());
			profileUserName = profile.getUserId();
		}


		if ((userName != null && userName.length() > 0) &&
				!(profileUserName!= null && profileUserName.equals(userName)) ) {
			HttpServletRequest request = (HttpServletRequest) ((this.getPageContext()).getRequest());
			HttpServletResponse response = (HttpServletResponse) ((this.getPageContext()).getResponse());

			TrackLoginRestClient lc = new TrackLoginRestClient();
			lc.setTrackingURI(requestURI);
			lc.login(request, response, userName, TrackTagHelper.parseSegmentWeights(segmentWeights));

		}
		
		return EVAL_PAGE;
	}


	public void release() {}

	public void setPageContext(final javax.servlet.jsp.PageContext pageContext)
	{
		this.pageContext=pageContext;
	}


	public void setParent(final javax.servlet.jsp.tagext.Tag parent)
	{
		this.parent=parent;
	}

	public javax.servlet.jsp.tagext.Tag getParent()
	{
		return parent;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSegmentWeights() {
		return segmentWeights;
	}

	public void setSegmentWeights(String segmentWeights) {
		this.segmentWeights = segmentWeights;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public PageContext getPageContext() {
		return pageContext;
	}

}
