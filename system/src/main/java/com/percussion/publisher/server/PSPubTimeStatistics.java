/******************************************************************************
 *
 * [ PSPubTimeStatistics.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.publisher.server;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.sql.ResultSet;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the exit that is used by sys_report/PubTimeStatistics.
 * This exit creates XML representation from the SQL result set which is
 * returned from sys_report/PubTimeStatistics resource.
 */
public class PSPubTimeStatistics implements IPSResultDocumentProcessor
{
    /**
     * Implementation of the method required by the interface
     * IPSResultDocumentProcessor. The returned XML conforms the following DTD
     * <pre>
     *    &lt;!ELEMENT Publishing_Time_Statistics (Publishing_Time_Statistic* )>
     *    &lt;!ELEMENT Publishing_Time_Statistic (Publish_Status_Id,
     *          Content_Type, Variant, Status, Average Mimimum,
     *          Maximum, Sub_Total )>
     *    &lt;!ELEMENT Sub_Total (#PCDATA)>
     *    &lt;!ATTLIST Sub_Total type CDATA #REQUIRED>
     *    &lt;!ELEMENT Number_Items (#PCDATA)>
     *    &lt;!ATTLIST Number_Items type CDATA #REQUIRED>
     *    &lt;!ELEMENT Maximum (#PCDATA)>
     *    &lt;!ATTLIST Maximum type CDATA #REQUIRED>
     *    &lt;!ELEMENT Mimimum (#PCDATA)>
     *    &lt;!ATTLIST Mimimum type CDATA #REQUIRED>
     *    &lt;!ELEMENT Average (#PCDATA)>
     *    &lt;!ATTLIST Average type CDATA #REQUIRED>
     *    &lt;!ELEMENT Status (#PCDATA)>
     *    &lt;!ELEMENT Variant (#PCDATA)>
     *    &lt;!ELEMENT Content_Type (#PCDATA)>
     *    &lt;!ELEMENT Publish_Status_Id (#PCDATA)>
     * </pre>
     *
     * @param params
     *         the parameter values supplied with the request in the appropriate
     *         order. There is one optional parameter to specify the maximum
     *         rows (from the SQL result set) included in the result document.
     *         Defaults to 65000 (-1 is unlimited).
     *
     * @param request
     *           the request context object
     *
     * @param resDoc
     *           the result XML document, can be <code>null</code>
     *
     * @return the processed document, which contains the statistics of the
     *         publishing time in the format described above.
     *
     * @throws PSParameterMismatchException
     *            if a call to setParamValues was never made, or the runtime
     *            parameters specified in that call are incorrect for the usage
     *            of this extension.
     *
     * @throws PSExtensionProcessingException
     *            if any other exception occurs which prevents the proper
     *            handling of this request.
     *
     */
    public Document processResultDocument(Object[] params,
                                          IPSRequestContext request, Document resDoc)
            throws PSParameterMismatchException, PSExtensionProcessingException
    {
        if (request == null)
            throw new IllegalArgumentException("request must not be null");
        int maxRows = 65000;

        if (params != null && params.length > 0 && params[0] != null)
        {
            String sMaxRows = params[0].toString();
            try
            {
                if (sMaxRows.trim().length() > 0)
                {
                    maxRows = Integer.parseInt(sMaxRows);
                    if (maxRows < 0)
                        maxRows = Integer.MAX_VALUE;
                }
                else
                {
                    ms_log.debug("The maxRows is default to " + maxRows);
                }
            }
            catch (NumberFormatException e)
            {
                // ignore the exception, log the error and take the default value.
                ms_log.warn("Failed to get the maxRows from value of '" + sMaxRows
                        + "', the maxRows is default to " + maxRows);
            }
        }

        resDoc = PSXmlDocumentBuilder.createXmlDocument();
        Element root = PSXmlDocumentBuilder.createRoot(resDoc, EL_PUB_STATISTICS);

        // optional parameter pubstatusid=#,#,#

        IPSInternalRequest ir = request.getInternalRequest(RESOURCE_NAME, null,
                true);

        if (ir == null)
            throw new PSExtensionProcessingException(
                    IPSCmsErrors.REQUIRED_RESOURCE_MISSING,
                    RESOURCE_NAME);
        try
        {
            ResultSet rs = ir.getResultSet();

            String pubstatusId, contentTypeName, variant, pubstatus,
                    minElapse, maxElapse, sumElapse, numItems;
            int avgElapse;
            Element statisticEl, numberEl;
            int rowCount = 0;

            while (rs.next() && rowCount < maxRows)
            {
                // retrieve the data from current row
                pubstatusId = rs.getString(1);
                contentTypeName = rs.getString(2);
                variant = rs.getString(3);
                pubstatus = rs.getString(4);
                avgElapse = rs.getInt(5); // round the avg to milli seconds
                minElapse = rs.getString(6);
                maxElapse = rs.getString(7);
                sumElapse = rs.getString(8);
                numItems = rs.getString(9);

                statisticEl = resDoc.createElement(EL_PUB_STATISTIC);
                PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_PUB_STATUS_ID, pubstatusId);
                PSXmlDocumentBuilder.addElement(resDoc, statisticEl, EL_CT_NAME,
                        contentTypeName);
                PSXmlDocumentBuilder.addElement(resDoc, statisticEl, EL_VARIANT,
                        variant);
                PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_PUBSTATUS, pubstatus);

                numberEl = PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_AVG, String.valueOf(avgElapse));
                numberEl.setAttribute(ATTR_TYPE, NUM_VALUE);

                numberEl = PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_MIN, minElapse);
                numberEl.setAttribute(ATTR_TYPE, NUM_VALUE);

                numberEl = PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_MAX, maxElapse);
                numberEl.setAttribute(ATTR_TYPE, NUM_VALUE);

                numberEl = PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_SUM, sumElapse);
                numberEl.setAttribute(ATTR_TYPE, NUM_VALUE);

                numberEl = PSXmlDocumentBuilder.addElement(resDoc, statisticEl,
                        EL_NUMITEMS, numItems);
                numberEl.setAttribute(ATTR_TYPE, NUM_VALUE);

                root.appendChild(statisticEl);
                rowCount++;
            }
        }
        catch (Exception e)
        {
            throw new PSExtensionProcessingException("PubTimeStatistics", e);
        }
        finally
        {
            ir.cleanUp();
        }

        return resDoc;
    }
    /**
     * Implementation of the method required by the interface IPSExtension.
     * Do nothing here.
     *
     * @param extensionDef The extension def, it is not used, may be
     *    <code>null</code>.
     *
     * @param file The root directory, which is not used, may be
     *    <code>null</code>.
     *
     * @throws PSExtensionException as defined by the interface, however, we do
     * not throw any exception here.
     *
     */
    public void init(IPSExtensionDef extensionDef, File file)
            throws PSExtensionException
    {
    }

    /**
     * Implementation of the method required by the interface
     * IPSResultDocumentProcessor.
     * <p>
     *
     * @return  always <code>false</code> since
     *          {@link #processResultDocument processResultDocument} never need
     *          to modify the XML-stylesheet processing instruction.
     */
    public boolean canModifyStyleSheet()
    {
        return false;
    }


    /**
     * The logger used for this class.
     */
    static private Logger ms_log = Logger.getLogger("PubTimeStatistics");

    /**
     * The resource name, used to query the statistics of the publishing time
     */
    private final static String RESOURCE_NAME = "sys_reports/PubTimeStatistics";

    /**
     * XML element and attribute names and values
     */
    private String EL_PUB_STATISTICS = "Publishing_Time_Statistics";
    private String EL_PUB_STATISTIC = "Publishing_Time_Statistic";
    private String EL_PUB_STATUS_ID = "Publish_Status_Id";
    private String EL_CT_NAME = "Content_Type";
    private String EL_VARIANT = "Variant";
    private String EL_PUBSTATUS = "Status";
    private String EL_AVG = "Average";
    private String EL_MIN = "Mimimum";
    private String EL_MAX = "Maximum";
    private String EL_SUM = "Sub_Total";
    private String EL_NUMITEMS = "Number_Items";
    private String ATTR_TYPE = "type";
    private String NUM_VALUE = "Number";
}
