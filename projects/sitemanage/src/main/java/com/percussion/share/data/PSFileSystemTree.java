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

package com.percussion.share.data;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.percussion.share.data.PSFileSystemItem.PSFileSystemItemType;

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
			log.error(e.getMessage());
			log.debug(e);
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
					log.error(e.getMessage());
					log.debug(e);
				}
			}
		}
		

		}
	}
	
}
