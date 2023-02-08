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
package com.percussion.sitemanage.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

import org.apache.tools.ant.BuildListener;

/**
 * 
 * Used to run Apache Ant scripts.
 * 
 * @author adamgent
 *
 */
public interface IPSAntService
{

    /**
     * Check to see if a script is running.
     * This is only really useful for reporting purposes
     * cannot be relied on for thread safety.
     * Consequently it maybe incorrect if multiple scripts of
     * the same name are running.
     * 
     * @param file never <code>null</code> or empty.
     * @return <code>true</code>
     */
    public boolean isRunning(String file);

    /**
     * Runs an ant script.
     * @param antScript never <code>null</code>.
     * @return never <code>null</code>.
     */
    public String runAnt(AntScript antScript);
    
    public static class AntScript {
        
        @NotEmpty
        private String file;
        private List<String> targets;
        private List<? extends BuildListener> listeners;
        
        @NotNull
        private boolean blocking = true;
        
        @NotNull
        private Map<String, String> properties = new HashMap<>();
        
        /**
         * The file name of the script.
         * @return never <code>null</code>.
         */
        public String getFile()
        {
            return file;
        }
        public void setFile(String file)
        {
            this.file = file;
        }
        public List<String> getTargets()
        {
            return targets;
        }
        public void setTargets(List<String> targets)
        {
            this.targets = targets;
        }
        public List<? extends BuildListener> getListeners()
        {
            return listeners;
        }
        public void setListeners(List<? extends BuildListener> listeners)
        {
            this.listeners = listeners;
        }
        /**
         * If <code>true</code> the call to {@link IPSAntService#runAnt(AntScript)}
         * will block and wait till the script is done.
         * Its recommend that if set to <code>false</code> to add {@link BuildListener}
         * to know when the build is complete.
         * @return never <code>null</code>.
         */
        public boolean isBlocking()
        {
            return blocking;
        }
        public void setBlocking(boolean blocking)
        {
            this.blocking = blocking;
        }
        /**
         * Properties that are passed on to the ant script.
         * @return never <code>null</code>.
         */
        public Map<String, String> getProperties()
        {
            return properties;
        }
        public void setProperties(Map<String, String> properties)
        {
            this.properties = properties;
        }
    }

}

