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

package com.percussion.services.filestorage;

import com.percussion.services.filestorage.data.PSHashedColumn;

import java.util.Set;

public interface IPSHashedFieldCataloger
{   
   public Set<PSHashedColumn> getServerHashedColumns();

   public void storeColumns(Set<PSHashedColumn> columns);
   
   public Set<PSHashedColumn> getStoredColumns();
   
   public Set<PSHashedColumn> validateColumns();
   
   public void addColumn(String table, String column);
   public void removeColumn(String table, String column);
    
}
