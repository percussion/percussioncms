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
        private Map<String, String> properties = new HashMap<String, String>();
        
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

