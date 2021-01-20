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
package com.percussion.services.notification.filemonitor.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Monitors a directory for changes to any of the files contained within it.
 * This watcher wakes up periodically and compares the modification dates
 * of the files within the directory.
 * 
 * When a file change is detected, the appropriate messages are sent to 
 * listener classes (which implement IPSResourceWatcher). These listeners
 * are registered using methods of the parent PSAbstractResourceWatcher.
 */
public class PSDirectoryWatcher extends PSAbstractResourceWatcher {

    /**
     * The current map of files and their timestamps (String fileName => Long
     * lastMod)
     */
    private Map currentFiles = new HashMap();

    /**
     * The directory to watch.
     */
    private String directory;

    /**
     * The map of last recorded files and their timestamps (String fileName =>
     * Long lastMod)
     */
    private Map prevFiles = new HashMap();

    /**
     * Constructor that takes the directory to watch.
     *
     * @param directoryPath   the directory to watch
     * @param intervalSeconds The interval to use when monitoring this
     *                        directory.  I.e., ever x seconds, check this directory to see
     *                        what has changed.
     * @throws IllegalArgumentException if the argument does not map to a
     *                                  valid directory
     */
    public PSDirectoryWatcher(String directoryPath, int intervalSeconds)
            throws IllegalArgumentException {

        //Get the common thread interval stuff set up.
        super(intervalSeconds, directoryPath + " interval watcher.");

        //Check that it is indeed a directory.
        File theDirectory = new File(directoryPath);

        if (theDirectory != null && !theDirectory.isDirectory()) {

            //This is bad, so let the caller know
           //TODO Add Logging to this class
            //FB: UR_UNINIT_READ NC 1-17-16
           IllegalArgumentException e = new IllegalArgumentException("The path " + theDirectory +
                 " does not represent a valid directory.");
            throw(e);

        }

        //Else all is well so set this directory and the interval
        this.directory = directoryPath;

    }

    /**
     * Start the monitoring of this directory.
     */
    public void start() {

        //Since we're going to start monitoring, we want to take a snapshot of the
        //current directory to we have something to refer to when stuff changes.
        takeSnapshot();

        //And start the thread on the given interval
        super.start();

        //And notify the listeners that monitoring has started
        File theDirectory = new File(directory);
        monitoringStarted(theDirectory);
    }

    /**
     * Stop the monitoring of this directory.
     */
    public void stop() {

        //And start the thread on the given interval
        super.stop();

        //And notify the listeners that monitoring has started
        File theDirectory = new File(directory);
        monitoringStopped(theDirectory);
    }

    /**
     * Store the file names and the last modified timestamps of all the files
     * and directories that exist in the directory at this moment.
     */
    private void takeSnapshot() {

        //Set the last recorded snap shot to be the current list
        prevFiles.clear();
        prevFiles.putAll(currentFiles);

        //And get a new current state with all the files and directories
        currentFiles.clear();

        File theDirectory = new File(directory);
        File[] children = theDirectory.listFiles();

        //Store all the current files and their timestamps
        for (int i = 0; i < children.length; i++) {

            File file = children[i];
            currentFiles.put(file.getAbsolutePath(),
                    new Long(file.lastModified()));
        }

    }

    /**
     * Check this directory for any changes and fire the proper events.
     */
    protected void doInterval() {

        //Take a snapshot of the current state of the dir for comparisons
        takeSnapshot();

        //Iterate through the map of current files and compare
        //them for differences etc...
        Iterator currentIt = currentFiles.keySet().iterator();

        while (currentIt.hasNext()) {

            String fileName = (String) currentIt.next();
            Long lastModified = (Long) currentFiles.get(fileName);

            //If this file did not exist before, but it does now, then
            //it's been added
            if (!prevFiles.containsKey(fileName)) {
                //DirectorySnapshot.addFile(fileName);
                resourceAdded(new File(fileName));
            }
            //If this file did exist before
            else if (prevFiles.containsKey(fileName)) {

                Long prevModified = (Long) prevFiles.get(fileName);

                //If this file existed before and has been modified
                if (prevModified.compareTo(lastModified) != 0) {
                   
                   //System.out.println("Detected fileName: \"" + fileName + "\".");
                   
                    // 27 June 2006
                    // Need to check if the file are removed and added
                    // during the interval
                   /* if (!DirectorySnapshot.containsFile(fileName)) {
                        resourceAdded(new File(fileName));
                    } else {*/
                        resourceChanged(new File(fileName));
                    //}
                }
            }
        }

        //Now we need to iterate through the list of previous files and
        //see if any that existed before don't exist anymore
        Iterator prevIt = prevFiles.keySet().iterator();

        while (prevIt.hasNext()) {

            String fileName = (String) prevIt.next();

            //If this file did exist before, but it does not now, then
            //it's been deleted
            if (!currentFiles.containsKey(fileName)) {
               //DirectorySnapshot.removeFile(fileName);
                resourceDeleted(fileName);
            }
        }
    }

    /**
     * For testing only.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Monitor c:/temp every 5 seconds
        PSDirectoryWatcher dw = new PSDirectoryWatcher("c:/temp/", 5);
        dw.addListener(new PSFileListener());
        dw.start();
    }


}
