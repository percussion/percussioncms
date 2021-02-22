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

package com.percussion.category.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.percussion.server.PSRequest;
import com.percussion.user.service.IPSUserService;

public class PSCategoryLockInfo {
	
	private static Log log = LogFactory.getLog(PSCategoryLockInfo.class);
	private static final String LOCKINFOFILE =  "lock_info.json";
	
	public static void writeLockInfoToFile(IPSUserService userService, String date) throws PSDataServiceException {
		
		JSONObject lockInfo = new JSONObject();
		File file = new File(LOCKINFOFILE);

		String userName = userService.getCurrentUser().getName();
		String sessionId = PSRequest.getContextForRequest().getUserSessionId();
		try (FileOutputStream os = new FileOutputStream(file)){
			lockInfo.put("userName", userName);
			lockInfo.put("sessionId", sessionId);
			lockInfo.put("creationDate", date);
 		

			os.write(lockInfo.toString().getBytes(StandardCharsets.UTF_8));
			
			log.debug("Created file that has user information who has locked the Categories Tab.");
			
		} catch (FileNotFoundException e) {
			log.error("File not found exception - PSCategoryService.writeLockInfoToFile()", e);
		} catch (JSONException e) {
			log.error("Json exception with the Json Object - PSCategoryService.writeLockInfoToFile()", e);
		} catch (IOException e) {
			log.error("IO exception with FileWriter - PSCategoryService.writeLockInfoToFile()", e);
		}
	}
	
	public static boolean isFileLocked() {
		
		JSONObject jsonObject = getLockInfo();
			if(jsonObject != null)
				return true;	
		return false;
	}
	
	@SuppressWarnings("unused")
	public static JSONObject getLockInfo() {
		
		File file = new File(LOCKINFOFILE);
		FileInputStream is = null;
		byte[] lockInfo = new byte[1000];
		JSONObject jsonObject = null;
		
		if(file.exists()){
			
			try {
				is = new FileInputStream(file);
				is.read(lockInfo);

				if(lockInfo == null) {
					
					is.close();
					return null;
				}

				jsonObject = new JSONObject(new String(lockInfo,"UTF-8"));
			} catch (FileNotFoundException e) {
				log.error("File not found with FileReader - PSCategoryService.getLockInfo()", e);
			} catch (IOException e) {
				log.error("IO exception with FileReader - PSCategoryService.getLockInfo()", e);
			} catch (JSONException e) {
				log.error("Json exception with the Json Object - PSCategoryService.getLockInfo()", e);
			} finally {
				try {
					//FB: NP_GUARANTEED_DEREF_ON_EXCEPTION_PATH NC 1-16-16
					if(is!=null){
						is.close();
					}
					is= null;
				} catch (IOException e) {
					log.error("IO exception while closing the FileReader - PSCategoryService.getLockInfo()", e);
				}
			}
		}
		
		return jsonObject;
	}
	
	public static void removeLockInfo() {
		
		JSONObject jsonObject = getLockInfo();
		
    	if(jsonObject != null) {
    		File file = new File(LOCKINFOFILE);
    
    		if(file.exists()) {
    			file.setWritable(true);
    			if(file.delete())
    				log.debug("File containing the user information who locked the Categories Tab has been deleted successfully.");
    		}
    	}
		
	}
}
