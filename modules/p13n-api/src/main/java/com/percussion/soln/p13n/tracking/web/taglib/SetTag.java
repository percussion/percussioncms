package com.percussion.soln.p13n.tracking.web.taglib;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * Tag does a Union of the request segment weights and the profiles segment weights
 * where the request segment weights will replace the profile segment weights.
 * 
 * @author adamgent
 *
 */
public class SetTag implements Tag{
	
	private PageContext pageContext;
	private Tag parent;
	private String requestURI;
	
	private String segmentWeights;

	public static final String VISITOR_PROFILE_SESSION_ATTR = "perc_visitorProfile";
	public static final String VISITOR_PROFILE_ID_REQUEST_PARAM = "visitorProfileId";

	public SetTag() {
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
			String responseText = TrackTagHelper.createWebBug(requestURI,"set",segmentWeights);
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
