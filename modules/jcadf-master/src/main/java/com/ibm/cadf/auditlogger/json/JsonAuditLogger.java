/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.auditlogger.json;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.cadf.auditlogger.AuditLogger;
import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.util.Constants;

public class JsonAuditLogger extends AuditLogger
{

    private static JsonAuditLogger instance;

    public static JsonAuditLogger getInstance()
    {

        if (instance == null)
        {
            instance = new JsonAuditLogger();
        }

        return instance;
    }

    @Override
    public String getOutputFilePath()
    {
        String outputFilePath = super.getOutputFilePath();
        return outputFilePath == null ? Constants.JSON_AUDIT_FILES_NAME : outputFilePath;
    }

    @Override
    public void writeLog(Event auditEvent) throws CADFException
    {

        BufferedWriter writer = null;
        FileWriter fw = null;
        FileReader fr = null;
        try
        {
            String filePath = getOutputFilePath();
            fw = new FileWriter(filePath, true);
            fr = new FileReader(filePath);
            writer = new BufferedWriter(fw);
            String JsonSeperator = ",";
            if (fr.read() > 0)
            {
                writer.write(JsonSeperator);
            }
            GsonBuilder builder = new GsonBuilder();
            builder.disableHtmlEscaping();
            Gson gson = builder.create();
            writer.write(gson.toJson(auditEvent));
            writer.write("\r \n");
            writer.flush();
        }
        catch (IOException e)
        {
            throw new CADFException(e);
        }

        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                    fr.close();
                }
                catch (IOException e)
                {
                    throw new CADFException(e);
                }
            }
        }
    }
}
