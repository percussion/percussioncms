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
package com.percussion.server;

import java.util.Properties;

/**
 * A process that runs during server startup
 * 
 * @author JaySeletz
 *
 */
public interface IPSStartupProcess
{

   /**
    * Do any startup work required.  
    * 
    * @param startupProps The startup properties, indicating values each process can use to
    * determine what work, if any, to perform.  Processes may modify these values, which are
    * then persisted for the next server startup.
    */
   void doStartupWork(Properties startupProps) throws Exception;
   
   /**
    * Set the manager on the process at init, process should call {@link IPSStartupProcessManager#addStartupProcess(IPSStartupProcess)}
    * @param mgr The mgr, not <code>null</code>.
    */
   void setStartupProcessManager(IPSStartupProcessManager mgr);

}
