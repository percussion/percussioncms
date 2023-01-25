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

package com.percussion.preinstall;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InputStreamLineBuffer{

    private static final Logger log = LogManager.getLogger(InputStreamLineBuffer.class);

    private InputStream inputStream;
    private ConcurrentLinkedQueue<String> lines;
    private long lastTimeModified;
    private Thread inputCatcher;
    private boolean isAlive;

    public InputStreamLineBuffer(InputStream is){
        inputStream = is;
        lines = new ConcurrentLinkedQueue<>();
        lastTimeModified = System.currentTimeMillis();
        isAlive = false;
        inputCatcher = new Thread(new Runnable(){
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder(100);
                int b;
                try{
                    while ((b = inputStream.read()) != -1){
                        // read one char
                        if((char)b == '\n'){
                            // new Line -> add to queue
                            lines.offer(sb.toString());
                            sb.setLength(0); // reset StringBuilder
                            lastTimeModified = System.currentTimeMillis();
                        }
                        else sb.append((char)b); // append char to stringbuilder
                    }
                } catch (IOException e){
                    log.error(PSExceptionUtils.getMessageForLog(e));
                    log.debug(PSExceptionUtils.getDebugMessageForLog(e));
                } finally {
                    isAlive = false;
                }
            }});
    }
    // is the input reader thread alive
    public boolean isAlive(){
        return isAlive;
    }
    // start the input reader thread
    public void start(){
        isAlive = true;
        inputCatcher.start();
    }
    // has Queue some lines
    public boolean hasNext(){
        return lines.size() > 0;
    }
    // get next line from Queue
    public String getNext(){
        return lines.poll();
    }
    // how much time has elapsed since last line was read
    public long timeElapsed(){
        return (System.currentTimeMillis() - lastTimeModified);
    }
}
