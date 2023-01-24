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

package com.percussion.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.percussion.wrapper.JettyStartUtils.error;
import static com.percussion.wrapper.JettyStartUtils.info;
import static com.percussion.wrapper.JettyStartUtils.debug;

/**
 * Empties stream passed into it in a separate thread line by line and
 * prints the line just read to the console.
 * To launch the gobbler call {@link #start()}.
 * The idea is borrowed from
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html.
 */
class PSStreamGobbler extends Thread {

    /**
     * The stream to read.
     */
    private final InputStream m_in;
    private final String startupString;
    private final String name;
    private final Runnable startedCallback;
    private volatile boolean waitForStartString = false;

    /**
     * Creates new stream gobbler.
     *
     * @param in the stream to copy to the console.
     *           Assumed not <code>null</code>.
     */
    PSStreamGobbler(String name, InputStream in) {
        this(name, in, null, null);
    }

    public PSStreamGobbler(String name, InputStream in, String startupString, Runnable startedCallback) {
        this.name = name;
        this.waitForStartString = (startupString!=null);
        this.m_in = in;
        this.startupString = startupString;
        this.startedCallback = startedCallback;
    }

    /**
     * Reads the stream provided in the constructor and prints it line-by-line
     * to the console. The method finishes when the stream is empty.
     *
     * @see Runnable#run()
     */
    @Override
    public void run() {
        try {
            try(final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(m_in))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    info("{%s} %s", name, line);
                    // TODO: line.contains should probably be changed to a regex
                    // to detect INFO: bla bla Server startup in...
                    if (waitForStartString && line.contains(startupString)) {
                        this.startedCallback.run();
                        waitForStartString = false;
                    }
                }
            }
        } catch (IOException e) {
            error("Error writing to stream",e);
        }
    }


}
