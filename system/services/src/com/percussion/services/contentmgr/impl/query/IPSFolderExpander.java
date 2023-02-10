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
package com.percussion.services.contentmgr.impl.query;

import com.percussion.utils.guid.IPSGuid;

import java.util.List;

import javax.jcr.query.InvalidQueryException;


/**
 * Implement this interface to expand a folder path to a list of folder
 * guids. Defined as a separate interface primarily for testing purposes,
 * although extra decoupling can be handy.
 * 
 * @author dougrand
 */
public interface IPSFolderExpander
{
   /**
    * Expand the given folder path. The path is a slash separated folder
    * path using the '%' character as a wildcard. The path does not have to
    * be to a valid folder path.
    * @param path the path, never <code>null</code> or empty
    * @return zero or more guids that each identify a folder
    * @throws InvalidQueryException if a path is invalid.
    */
   List<IPSGuid> expandPath(String path) throws InvalidQueryException;
}
