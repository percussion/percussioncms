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
package com.percussion.delivery.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.Validate;

/**
 * Utility class for discovering the version of a service. 
 * 
 * @author natechadwick
 *
 */
public class PSVersionHelper {

	/***
	 * Given the specified class will look for a build.properties file
	 * in the root of it's resources that contains version information.
	 * 
	 * If an error occurs will return 'undefined' for the version string. 
	 * 
	 * If Successful version will be returned in the format of:
	 * 	
	 * version-tag_buildtime
	 * 
	 * for example:
	 * 
	 * 2.8.153-CM1DEVBuild-153_2005-08-22_23-59-59
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getVersion(Class clazz){
		String version = "";
		
		Validate.notNull(clazz);
		Properties props = new Properties();
		
		InputStream in = clazz.getClassLoader().getResourceAsStream("build.properties");
		
		if(in==null)
			version="undefined";
		
		try {
			props.load(in);
		} catch (IOException e) {
			version="undefined";
		}

		version = String.format("%s-%s_%s",
				props.getProperty("version"),
				props.getProperty("build_tag"),
				props.getProperty("buildTime"));
		
		return version;
	}
	
	
}
