package com.percussion.soln.p13n.tracking.web.taglib;


import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This tag completly <em>replaces</em> all profile segment weights with the segment weights provided
 * in the tracking request so that only the request segment weights are in the profile.
 * 
 * @author sbolton
 * @author adamgent
 *
 */
public class ClearTag implements Tag{
	/**
     * The log instance to use for this class, never <code>null</code>.
     */
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(ClearTag.class);
   
	private PageContext pageContext;
	private Tag parent;
	private String requestURI;
	
	private String segmentWeights;

	public static final String VISITOR_PROFILE_SESSION_ATTR = "perc_visitorProfile";
	public static final String VISITOR_PROFILE_ID_REQUEST_PARAM = "visitorProfileId";

	public ClearTag() {
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

			String responseText = TrackTagHelper.createWebBug(requestURI,"clear",segmentWeights);
			
			if (responseText.length() >0) {
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
