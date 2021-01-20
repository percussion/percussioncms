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
