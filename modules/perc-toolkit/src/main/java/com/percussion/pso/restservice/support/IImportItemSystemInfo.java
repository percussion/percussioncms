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
