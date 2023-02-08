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
package com.percussion.services.notification.filemonitor.impl;

import java.io.File;

/**
 * Class used to implement a File Listener for the File Monitor Notification 
 * Service. An example implementation for now.
 *
 */
public class PSFileListener extends PSBaseListener implements IPSFileListener {

    /**
     * Constructor
     */
    public PSFileListener() {
        super();
    }

    public void onStart(Object monitoredResource) {
        // On startup
        if (monitoredResource instanceof File) {
            File resource = (File) monitoredResource;
            if (resource.isDirectory()) {
                System.out.println("Start to monitor " + resource.getAbsolutePath());
                /*File[] files = resource.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File f = (File) files[i];
                    onAdd(f);
                }*/
            }
        }
    }

    public void onStop(Object notMonitoredResource) {

    }

    public void onAdd(Object newResource) {
        if (newResource instanceof File) {
            File file = (File) newResource;
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath() + " is added");
            }
        }
    }

    public void onChange(Object changedResource) {
        if (changedResource instanceof File) {
            File file = (File) changedResource;
            if (file.isFile()) {
                System.out.println(file.getAbsolutePath() + " is changed");
            }

        }
    }

    public void onDelete(Object deletedResource) {
        if (deletedResource instanceof String) {
            String deletedFile = (String) deletedResource;
            System.out.println(deletedFile + " is deleted");
        }
    }
}

