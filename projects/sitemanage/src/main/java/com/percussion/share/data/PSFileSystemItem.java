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

package com.percussion.share.data;

/***
 * Lightweight representation of an item on the file system.
 * 
 * @author natechadwick
 *
 */
public class PSFileSystemItem {

	public enum PSFileSystemItemType{
		FILE,
		DIRECTORY
	}
	
	private String absolutePath;
	private PSFileSystemItemType type;
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	public PSFileSystemItemType getType() {
		return type;
	}
	public void setType(PSFileSystemItemType type) {
		this.type = type;
	}
	
	public PSFileSystemItem(String path, PSFileSystemItemType type){
		this.absolutePath = path;
		this.type = type;
	}
}
