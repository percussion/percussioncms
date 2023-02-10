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

import com.percussion.services.PSBaseServiceLocator;

public class PSFileStorageServiceLocator extends PSBaseServiceLocator {

    private static volatile IPSFileStorageService fss=null;
      
      public static IPSFileStorageService getFileStorageService()
      {
          if (fss==null)
          {
              synchronized (PSFileStorageServiceLocator.class)
              {
                  if (fss==null)
                  {
                      fss = (IPSFileStorageService) PSBaseServiceLocator.getBean(
                              FILESTORAGE_SERVICE_BEAN);
                  }
              }
          }
         return fss;
      }

      public static final String FILESTORAGE_SERVICE_BEAN = "sys_fileStorageService";
}
