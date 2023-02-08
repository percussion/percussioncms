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

package com.percussion.pso.utils;

import org.apache.commons.lang.StringUtils;

public class PathCleanupUtils {

	public static String cleanupPathPart(String path,boolean forceLower,boolean includesExtension, boolean stripExtension, String prefix, String suffix, String forceExtension) {
		String ext=null;
	    path = path.replaceAll("[&]", " and ");
	    path = path.replaceAll("[^0-9a-zA-Z-_/ \\.\\\\]", "");
	 
	    
	    if (forceLower) path=path.toLowerCase();
		if (StringUtils.isNotEmpty(forceExtension)) {
			ext = forceExtension;
		}
		if (path.contains(".") && includesExtension) {
		   int extIndex = path.lastIndexOf(".");
		   if (!stripExtension && ext==null) {
			   ext=path.substring(extIndex+1,path.length());
			   ext = ext.replaceAll("[-_ \\.\\\\]+", "-");
			   if (forceLower) ext=ext.toLowerCase();
		   }
		   path=path.substring(0,extIndex);
		   path = path.replaceAll("[-_ \\.\\\\]+", "-");
		} else {
			path = path.replaceAll("[-_ \\.\\\\]+", "-");
		}
		
	
		
		path=prefix + path+ suffix;
		
	    if (ext!=null) {
	    	path+="."+ext;
	    }
		
	    return path;
	}
	
	public static String cleanupPathPart(String path,boolean forceLower,boolean includesExtension) {
		return cleanupPathPart(path,forceLower,includesExtension,false, "", "",  ""); 
	}
}
