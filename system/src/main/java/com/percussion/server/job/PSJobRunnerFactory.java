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

package com.percussion.server.job;


/**
 * Class for creating classes implementing PSJobRunner based on the type and 
 * category of job.  Types are paired with class names in the 
 * {@link PSJobHandlerConfiguration}.  Any class that will be created by this 
 * factory must not be obfuscated and must be added to the config file used to 
 * initialize the {@link PSJobHandlerConfiguration} supplied during construction.
 */
public class PSJobRunnerFactory 
{
   /**
    * Construct the factory, providing the config.
    * 
    * @param config The config providing all information required to initialize
    * each type of job.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>config</code> is 
    * <code>null</code>.
    */
   public PSJobRunnerFactory(PSJobHandlerConfiguration config)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
         
      m_config = config;
   }

   /**
    * Returns an instance of PSJobRunner based on the job type.
    * 
    * @param category The category of this job.  May not be <code>null</code> or 
    * empty, and must be an existing category.
    * @param jobType A string which identifies a single job type within a 
    * category.  May not be <code>null</code> or empty, and must be am
    * existing job type within the specified category.
    * 
    * @return an instance of the correct class derived from PSJobRunner, never
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSJobException if there is an error instantiating the JobRunner.
    */
   public PSJobRunner getJobRunner(String category, String jobType) 
      throws PSJobException 
   {
      if (category == null || category.trim().length() == 0)
         throw new IllegalArgumentException(
            "category may not be null or empty");
            
      if (jobType == null || jobType.trim().length() == 0)
         throw new IllegalArgumentException("jobType may not be null or empty");
      
      PSJobRunner runner = null;
      
      String className = m_config.getJobClassName(category, jobType);
      
      try
      {
         runner = (PSJobRunner)Class.forName(className).newInstance();
      }
      catch (ClassNotFoundException e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSJobException(IPSJobErrors.FACTORY_GET_RUNNER, args);
      }
      catch (InstantiationException e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSJobException(IPSJobErrors.FACTORY_GET_RUNNER, args);
      }
      catch (IllegalAccessException e)
      {
         Object[] args = {className, e.getLocalizedMessage()};
         throw new PSJobException(IPSJobErrors.FACTORY_GET_RUNNER, args);
      }
      
      return runner;
   }

   /**
    * Get the config used by this factory.
    * 
    * @return The config, never <code>null</code>.
    */
   public PSJobHandlerConfiguration getConfig()
   {
      return m_config;
   }

   /**
    * Provides InitParams for all jobs and for the handler and maps job types
    * to their respective classes.  Initialized during ctor, never 
    * <code>null</code> or modified after that.
    */
   private static PSJobHandlerConfiguration m_config = null;

}
