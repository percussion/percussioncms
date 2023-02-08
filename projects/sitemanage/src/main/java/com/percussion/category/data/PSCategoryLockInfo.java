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

package com.percussion.category.data;


import com.percussion.server.PSRequest;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.service.IPSUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;



public class PSCategoryLockInfo {


	private static final Logger log = LogManager.getLogger(PSCategoryLockInfo.class);
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
			if(jsonObject != null) {
				return true;
			}
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
    			if(file.delete()) {
					log.debug("File containing the user information who locked the Categories Tab has been deleted successfully.");
				}
    		}
    	}
		
	}
}
