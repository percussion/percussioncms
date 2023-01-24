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
package com.percussion.utils.jdbc;

/**
 * Simple concrete implementation of the {@link IPSConnectionInfo} interface.
 */
public class PSConnectionInfo implements IPSConnectionInfo
{
   /**
    * Construct from datasource name.
    * 
    * @param datasource The name, may be <code>null</code> or empty to indicate
    * the repository connection.
    */
   public PSConnectionInfo(String datasource)
   {
      m_datasource = datasource;
   }
   
   // see IPSConnectionInfo interface.
   public String getDataSource()
   {
      return m_datasource;
   }
   
   /**
    * The datasource name supplied during construction, immutable after that.
    */
   private String m_datasource;
}

