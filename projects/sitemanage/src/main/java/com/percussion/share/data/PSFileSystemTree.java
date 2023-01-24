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

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.data.PSFileSystemItem.PSFileSystemItemType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @deprecated Seems unused as of 8.0.2
 */
@Deprecated
public class PSFileSystemTree implements IPSTree {

	private static final Logger log = LogManager.getLogger(PSFileSystemTree.class);

	private IPSTreeNode<PSFileSystemItem> root;
	
	public PSFileSystemTree(File f)  {
		PSFileSystemTreeNode<PSFileSystemItem> rt = new PSFileSystemTreeNode<>();
		rt.setParent(null);
		try {
			rt.setValue(new PSFileSystemItem(f.getCanonicalPath(), PSFileSystemItemType.DIRECTORY));
		} catch (IOException e) {
			log.error(PSExceptionUtils.getMessageForLog(e));
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
		}
		this.root = rt;
		initTree();
	}

	@Override
	public IPSTreeNode<PSFileSystemItem> getRoot() {
		return root;
	}

	private void initTree(){
		File f = new File(root.getValue().getAbsolutePath());
	
		if(f.exists()){
			
		File[] files = f.listFiles();
		if(files != null) {
			for (int i = 0; i < files.length; i++) {
				try {
					PSFileSystemItem fi = null;
					if (files[i].isFile())
						fi = new PSFileSystemItem(files[i].getCanonicalPath(), PSFileSystemItem.PSFileSystemItemType.FILE);
					else
						fi = new PSFileSystemItem(files[i].getCanonicalPath(), PSFileSystemItem.PSFileSystemItemType.FILE);
				} catch (IOException e) {
					log.error(PSExceptionUtils.getMessageForLog(e));
					log.debug(PSExceptionUtils.getDebugMessageForLog(e));
				}
			}
		}
		

		}
	}
	
}
