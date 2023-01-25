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

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * Does the default tracking action.
 * @author adamgent
 *
 */
public class TrackTag implements Tag{
	
	private PageContext pageContext;
	private Tag parent;
	private String requestURI;
	
	private String segmentWeights;


	public static final String VISITOR_PROFILE_SESSION_ATTR = "perc_visitorProfile";
	public static final String VISITOR_PROFILE_ID_REQUEST_PARAM = "visitorProfileId";

	public TrackTag() {
		super();
	}

	public int doStartTag() throws javax.servlet.jsp.JspTagException
	{
		return SKIP_BODY;
	}

	public int doEndTag() throws javax.servlet.jsp.JspTagException
	{

		try
		{

			if (segmentWeights!=null && segmentWeights.length()>0) {
				String responseText = TrackTagHelper.createWebBug(requestURI,"update",segmentWeights);
				pageContext.getOut().write(responseText); 
			}
			
		}
		catch(java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
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

	
	public String getSegmentWeights() {
		return segmentWeights;
	}

	public void setSegmentWeights(String segmentWeights) {
		this.segmentWeights = segmentWeights;
	}

}
