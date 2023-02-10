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

/**
 * 
 */
package test.percussion.soln.p13n.tracking.web;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestWebServer {
    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private String response;
    private String requestBody;
    private int port;
    private AtomicInteger requestCount = new AtomicInteger(1);
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(TestWebServer.class);
    
    
    public TestWebServer(int port) {
        super();
        this.port = port;
    }

    public void start() throws Exception {
        isTrue(serverSocket == null);
        notNull(response);
        serverSocket = new ServerSocket(port);
        pool.submit( new Callable<ServerSocket>() {
            public ServerSocket call() throws Exception {
                while ( requestCount.decrementAndGet() >= 0 ) {
                    pool.execute(new TestRequest(serverSocket.accept()));
                }
                return serverSocket;
            }
        });
        notNull(serverSocket);
        
    }
    
    public void stop() throws Exception {
        pool.shutdown();
        serverSocket.close();
    }

    public String getResponse() {
        return response;
    }

    
    public void setResponse(String response) {
        this.response = response;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public class TestRequest implements Runnable{
        private Socket socket;
        
        private TestRequest(Socket socket) {
            super();
            this.socket = socket;
        }

        public void run()  {
            Reader br = null;
            try {
                StringWriter sw = new StringWriter();
                br = new InputStreamReader(socket.getInputStream());
                copy(br, sw);
                requestBody = sw.getBuffer().toString();
                sendResponse(new BufferedOutputStream(socket.getOutputStream()), 200, "text/json", response);
                
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            }
            finally {
               close(br);
               close(socket);               
            }

        }
    }
    
    private static void close(Reader stream) {
        if (stream == null) return;
        try {
            stream.close();
        } catch (IOException e) {
            log.error("Error closing stream", e);
        }
    }
    
    private static void close(Socket socket) {
        if (socket == null) return;
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error closing socket", e);
        }
    }

    private static void sendResponse(OutputStream out, int code, String contentType, String response) throws IOException {
        long contentLength = response.length();
        out.write(("HTTP/1.0 " + code + " OK\r\n" + 
                   "Date: " + new Date().toString() + "\r\n" +
                   "Server: TestWebServer/1.0\r\n" +
                   "Content-Type: " + contentType + "\r\n" +
                   "Expires: Thu, 01 Dec 2020 16:00:00 GMT\r\n" +
                   ((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "") +
                   "\r\n").getBytes());
        out.write(response.getBytes());
        out.flush();
        out.close();
    }
    
    private static long copy(Reader input, Writer output) throws IOException {
        boolean inHead = true;
        char[] buffer = new char[1024*4];
        int state = 0;
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer)) && inHead) {
            output.write(buffer, 0, n);
            count += n;
            for ( int j = 0; j < n; j++) {
                if (buffer[j] == '\r' && (state == 0 || state == 2)) {
                    ++state;
                }
                else if (buffer[j] == '\n' && (state == 1 || state == 3)) {
                    ++state;
                }
                else {
                    state = 0;
                }
                if (state == 4)
                    break;
            }
            if (state == 4)
                break;
            
        }
        return count;
    }

}