/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.support;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;


/**
 */
public interface IImportItemSystemInfo {



	
	// Helper methods 
	/**
	 * Method getWorkflowName.
	 * @param id int
	 * @return String
	 */
	public String getWorkflowName(int id);
	/**
	 * Method getStateName.
	 * @param wfid int
	 * @param stateid int
	 * @return String
	 */
	public String getStateName(int wfid,int stateid);
	/**
	 * Method getSiteName.
	 * @param id int
	 * @return String
	 */
	public String getSiteName(int id);
	/**
	 * Method getFolderPath.
	 * @param id int
	 * @return String
	 */
	public String getFolderPath(int id);
	/**
	 * Method getSlotName.
	 * @param id int
	 * @return String
	 */
	public String getSlotName(int id);
	/**
	 * Method getTemplateName.
	 * @param id int
	 * @return String
	 */
	public String getTemplateName(int id);
	/**
	 * Method getContentTypeName.
	 * @param id long
	 * @return String
	 */
	public String getContentTypeName(long id);
	/**
	 * Method getCommunityName.
	 * @param id int
	 * @return String
	 */
	public String getCommunityName(int id);
	public int getCommunityId(String name);
	/**
	 * Method getItemDefinition.
	 * @param contentType String
	 * @return PSItemDefinition
	 * @throws PSInvalidContentTypeException
	 */
	public PSItemDefinition getItemDefinition(String contentType)  throws PSInvalidContentTypeException;
}
