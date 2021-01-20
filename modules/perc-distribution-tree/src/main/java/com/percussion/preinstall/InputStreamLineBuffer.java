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

package com.percussion.preinstall;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InputStreamLineBuffer{
    private InputStream inputStream;
    private ConcurrentLinkedQueue<String> lines;
    private long lastTimeModified;
    private Thread inputCatcher;
    private boolean isAlive;

    public InputStreamLineBuffer(InputStream is){
        inputStream = is;
        lines = new ConcurrentLinkedQueue<String>();
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
                    e.printStackTrace();
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