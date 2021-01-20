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
