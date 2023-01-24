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

package com.percussion.data.jdbc;



/**
 * The PSXmlDriverMetaData class implements driver level catalog
 * support for the XML driver.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXmlDriverMetaData extends PSFileSystemDriverMetaData {

   /**
    * Construnct an XML driver meta data object.
    */
   public PSXmlDriverMetaData()
   {
      super();
   }

   /* getServers is not implemented here as the File System driver
    * covers the XML driver's needs
    */
}

