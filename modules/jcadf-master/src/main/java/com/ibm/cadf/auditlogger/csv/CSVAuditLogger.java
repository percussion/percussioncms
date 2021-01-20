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

package com.ibm.cadf.auditlogger.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cadf.auditlogger.AuditLogger;
import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.model.Measurement;
import com.ibm.cadf.util.Constants;

public class CSVAuditLogger extends AuditLogger
{

    private static CSVAuditLogger instance;

    private CSVAuditLogger()
    {
        writeFieldNames();
    }

    public static CSVAuditLogger getInstance()
    {

        if (instance == null)
        {
            instance = new CSVAuditLogger();
        }

        return instance;
    }

    public String getOutputFilePath()
    {
        String outputFilePath = super.getOutputFilePath();
        return outputFilePath == null ? Constants.CSV_AUDIT_FILES_NAME : outputFilePath;
    }

    @Override
    public void writeLog(Event auditEvent)
    {

        BufferedWriter writer = null;

        try
        {
            writer = new BufferedWriter(new FileWriter(getOutputFilePath(), true));
            String CSVSeperator = Constants.CSV_SEPERATOR;

            if (StringUtils.isNotEmpty(auditEvent.getId()))
                writer.write("" + auditEvent.getId());
            writer.write(CSVSeperator);
            if (auditEvent.getEventTime() != null)
                writer.write(auditEvent.getEventTime());
            writer.write(CSVSeperator);
            if (StringUtils.isNotEmpty(auditEvent.getAction()))
                writer.write(auditEvent.getAction());
            writer.write(CSVSeperator);
            if (auditEvent.getObserver() != null && auditEvent.getObserver().getName() != null)
                writer.write(auditEvent.getObserver().getName());
            writer.write(CSVSeperator);
            if (auditEvent.getInitiator() != null && auditEvent.getInitiator().getName() != null)
                writer.write(auditEvent.getInitiator().getName());
            writer.write(CSVSeperator);
            if (auditEvent.getTarget() != null && auditEvent.getTarget().getName() != null)
                writer.write(auditEvent.getTarget().getName());
            writer.write(CSVSeperator);
            if (auditEvent.getOutcome() != null)
                writer.write(auditEvent.getOutcome());
            writer.write(CSVSeperator);
            if (auditEvent.getMeasurements() != null && !auditEvent.getMeasurements().isEmpty())
            {
                writer.write("<");
                for (Measurement measurement : auditEvent.getMeasurements())
                {
                    if (measurement.getMetric() != null)
                    {
                        writer.write(measurement.getMetric().getMetricId() + " - " + measurement.getMetric().getName()
                                     + " " + measurement.getResult() + " : ");
                    }
                }
                writer.write(">");
            }
            writer.write("\r\n");
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
                }
                catch (IOException e)
                {

                }
            }
        }
    }

    private void writeFieldNames()
    {

        String outputFilePath = getOutputFilePath();
        if (new File(outputFilePath).exists())
            return;

        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(outputFilePath, true));

            writer.write("Id" + ",");
            writer.write("Timestamp" + ",");
            writer.write("Action" + ",");
            writer.write("Observer" + ",");
            writer.write("Initiator" + ",");
            writer.write("Target" + ",");
            writer.write("Outcome" + ",");
            writer.write("<Measurements>");
            writer.write("\r\n");

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
                }
                catch (IOException e)
                {
                    throw new CADFException(e);
                }
            }
        }
    }
}
