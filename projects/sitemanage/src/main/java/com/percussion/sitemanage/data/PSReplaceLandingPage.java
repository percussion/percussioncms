/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.percussion.share.data.PSAbstractDataObject;

/**
 * This class contains information for replacing the landing page of a (site) section.
 * 
 * @author yubingchen
 */
@XmlRootElement(name="ReplaceLandingPage")
public class PSReplaceLandingPage extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;

    private String sectionId;
    
    private String newLandingPageId;
    
    private String newLandingPageName;

    private String newLandingPageFromState;
    
    private String newLandingPageToState;
    
    private String oldLandingPageName;
 
    private String oldLandingPageFromState;
    
    private String oldLandingPageToState;
    
    /**
     * Gets the ID of the site section.
     * 
     * @return the ID of the section, not blank for a valid request/response data.
     */
    public String getSectionId()
    {
        return sectionId;
    }
    
    /**
     * Sets the ID of the site section.
     * 
     * @param id the ID of the site section, not blank for a valid request/response data.
     */
    public void setSectionId(String id)
    {
        sectionId = id;
    }
    
    /**
     * Gets the ID of the new landing page.
     * 
     * @return the ID of the new landing page, not blank for a valid request/response data.
     */
    public String getNewLandingPageId()
    {
        return newLandingPageId;
    }
    
    /**
     * Sets the ID of the new landing page.
     * 
     * @param id the ID of the new landing page, not blank for a valid request/response data.
     */
    public void setNewLandingPageId(String id)
    {
        newLandingPageId = id;
    }
    
    /**
     * Gets the name of the new landing page.
     * 
     * @return the name of the new landing page, it may be blank for a request data, 
     * but not blank for a response data.
     */
    public String getNewLandingPageName()
    {
        return newLandingPageName;
    }
    
    /**
     * Sets the name of the new landing page.
     * 
     * @param name the name of the new landing page, it may be blank for a request data,
     * but not blank for a response data.
     */
    public void setNewLandingPageName(String name)
    {
        newLandingPageName = name;
    }
    
    /**
     * Gets the workflow state of the new landing page before the replacement operation.
     * 
     * @return the workflow state, not blank for a valid response.
     */
    public String getNewLandingPageFromState()
    {
        return newLandingPageFromState;
    }
    
    /**
     * Sets the workflow state of the new landing page before the replacement operation.
     * 
     * @param state the new workflow state, not blank for a valid response.
     */
    public void setNewLandingPageFromState(String state)
    {
        newLandingPageFromState = state;
    }
    
    /**
     * Gets the workflow state of the new landing page after the replacement operation.
     * 
     * @return the workflow state, not blank for a valid response.
     */
    public String getNewLandingPageToState()
    {
        return newLandingPageToState;
    }
    
    /**
     * Sets the workflow state of the new landing page after the replacement operation.
     * 
     * @param state the new workflow state, not blank for a valid response.
     */
    public void setNewLandingPageToState(String state)
    {
        newLandingPageToState = state;
    }
    
    /**
     * Sets the name of the Old landing page.
     * 
     * @return the name of the old landing page, it may be blank for a request data,
     * but not blank for a response data.
     */
    public String getOldLandingPageName()
    {
        return oldLandingPageName;
    }
    
    /**
     * Sets the name of old landing page.
     * 
     * @param name the name of the old landing page, it may be blank for a resort;
     * but not blank for a response data.
     */
    public void setOldLandingPageName(String name)
    {
        oldLandingPageName = name;
    }
    
    /**
     * Gets the workflow state of the old landing page before the replacement operation.
     * 
     * @return the workflow state, not blank for a valid response.
     */
    public String getOldLandingPageFromState()
    {
        return oldLandingPageFromState;
    }
    
    /**
     * Sets the workflow state of the old landing page before the replacement operation.
     * 
     * @param state the new workflow state, not blank for a valid response.
     */
    public void setOldLandingPageFromState(String state)
    {
        oldLandingPageFromState = state;
    }
    
    /**
     * Gets the workflow state of the old landing page after the replacement operation.
     * 
     * @return the workflow state, not blank for a valid response.
     */
    public String getOldLandingPageToState()
    {
        return oldLandingPageToState;
    }

    /**
     * Sets the workflow state of the old landing page after the replacement operation.
     * 
     * @param state the new workflow state, not blank for a valid response.
     */
    public void setOldLandingPageToState(String state)
    {
        oldLandingPageToState = state;
    }    
}
