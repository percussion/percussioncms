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
package com.percussion.rx.config.impl.spring;

import com.percussion.rx.config.IPSBeanProperties;

import java.io.File;

/**
 * This interface exposed some methods for internal use, such as unit test.
 *
 * @author YuBingChen
 */
public interface IPSBeanPropertiesInternal extends IPSBeanProperties
{
   /**
    * Gets the properties file. This is not exposed through the interface, but
    * can be called directly, e.g., from unit test.
    * 
    * @return the properties file, never <code>null</code>.
    */
   File getPropertiesFile();
   

}
