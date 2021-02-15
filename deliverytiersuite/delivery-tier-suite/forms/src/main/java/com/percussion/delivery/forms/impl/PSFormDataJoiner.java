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

package com.percussion.delivery.forms.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.percussion.delivery.forms.data.IPSFormData;

/**
 * Merges the content of 0 or more PSFormData objects into a single CSV file,
 * with a single header and a row for every form data object.
 * Handles the case where different forms have different fields by putting
 * empty data for each row's cell if that row did not originally have that
 * field's data. The output rows are in the same order as the input rows.
 * 
 * @author miltonpividori
 * 
 */
public class PSFormDataJoiner
{

    private static final String FORM_NAME_FIELD = "Form name";

    private static final String CREATE_DATE_FIELD = "Create date";

    /**
     * Adds all columns in a form header into a Set of column names.
     * 
     * @param columnNames A Set object where form headers will be added. Assumed
     * not <code>null</code>.
     * @param formHeader Array with form's header (column names). Assumed no
     * <code>null</code>.
     */
    private void addColumnNames(Set<CaselessString> columnNames, Set<String> formHeader)
    {
        for (String header : formHeader)
        {
            columnNames.add(new CaselessString(header));
        }
    }

    /**
     * Prepares the final header to be finally written to a CSV file.
     * 
     * @param headerColumns The header with all the fields of every form, except
     * the 'form name' and 'create date'. Assumed not <code>null</code>.
     * @return A CaselessString list with all the header columns given in the
     * argument, plus the 'form name' and 'create date' fields. Never
     * <code>null<code>, never empty.
     */
    private List<CaselessString> prepareHeader(SortedSet<CaselessString> headerColumns)
    {
        List<CaselessString> finalHeader = new ArrayList<>(headerColumns);

        // Add "form name" and "create date" fields at the beginning
        // of the header list
        finalHeader.add(0, new CaselessString(CREATE_DATE_FIELD));
        finalHeader.add(0, new CaselessString(FORM_NAME_FIELD));

        return finalHeader;
    }

    /**
     * Takes a PSFormData object and returns a Map with all its keys (field
     * names) normalized, using the CaselessString class. It also adds the 'form
     * name' and 'create date' columns.
     * 
     * @param formData A PSFormData object to process. Assumed not
     * <code>null</code>.
     * @return A new Map with the same information as currently present in
     * 'formData', but using a CaselessString object as the key (field
     * names). Never <code>null</code>, never empty.
     */
    private Map<CaselessString, String> processCsvRow(IPSFormData formData)
    {
        Map<String, String> formDataFields = formData.getFields();

        Map<CaselessString, String> processedFormDataFields = new HashMap<>();

        for (String key : formDataFields.keySet())
            processedFormDataFields.put(new CaselessString(key), formDataFields.get(key));

        processedFormDataFields.put(new CaselessString(FORM_NAME_FIELD), formData.getName());
        processedFormDataFields.put(new CaselessString(CREATE_DATE_FIELD), formData.getCreateDate().toString());

        return processedFormDataFields;
    }

    /**
     * Takes a list of PSFormData objects and process them. It creates a unique
     * set of fields and returns the processing result.
     * 
     * @param formsList List of PSFormData objects to be processed. Assumed not
     * <code>null</code>.
     * @return Parsing result. It has the final header in alpha order and the
     * data as a list of maps. Never <code>null</code>, maybe empty.
     * 
     * @throws IOException
     */
    private FormDataProcessingResult parseCSVData(List<IPSFormData> formsList) throws IOException
    {
        SortedSet<CaselessString> finalHeaderSet = new TreeSet<>();
        List<Map<CaselessString, String>> currentDataList = new ArrayList<>();

        for (IPSFormData aForm : formsList)
        {
            addColumnNames(finalHeaderSet, aForm.getFieldNames());

            // TODO The Map returned by PSFormData is rebuilded here
            currentDataList.add(processCsvRow(aForm));
        }

        // add every row with all columns
        Map<String, String> aFinalCsvRow;
        List<Map<String, String>> finalCsvRowList = new ArrayList<>();

        // Final header is based on 'finalHeaderSet'. It has the 'form name' and
        // 'create date' at the beginning.
        List<CaselessString> finalHeader = this.prepareHeader(finalHeaderSet);

        for (Map<CaselessString, String> aCsvRow : currentDataList)
        {
            aFinalCsvRow = new HashMap<>();

            for (CaselessString aHeader : finalHeader)
            {
                // if this row has no data for aHeader, add a blank value
                if (aCsvRow.containsKey(aHeader))
                    aFinalCsvRow.put(aHeader.toString(), aCsvRow.get(aHeader));
                else
                    aFinalCsvRow.put(aHeader.toString(), StringUtils.EMPTY);
            }

            finalCsvRowList.add(aFinalCsvRow);
        }

        return new FormDataProcessingResult(finalHeader, finalCsvRowList);
    }

    /**
     * Writes a CSV file with given the header and data.
     * 
     * @param formDataProcessingResult Result of processing forms data. Assumed
     * not <code>null</code>.
     * @return CSV produced according with the parsing result. It's produced
     * according to Excel rules. Never <code>null</code>, maybe empty.
     * @throws Exception
     */
    private String writeCSV(FormDataProcessingResult formDataProcessingResult) throws IOException
    {
        if (formDataProcessingResult.isEmpty())
            return StringUtils.EMPTY;

        StringWriter finalResult = new StringWriter();
        ICsvMapWriter csvWriter = new CsvMapWriter(finalResult, CsvPreference.EXCEL_PREFERENCE);

        // write header
        csvWriter.writeHeader(formDataProcessingResult.getHeader());

        // write data
        for (Map<String, String> aRow : formDataProcessingResult.getData())
            csvWriter.write(aRow, formDataProcessingResult.getHeader());

        csvWriter.close();

        return finalResult.toString();
    }

    /**
     * Joins an array with forms data given as PSFormData objects and returns a
     * merged CSV.
     * 
     * @param forms List of PSFormData objects. Assumed not <code>null</code>.
     * @return A CSV with all forms data merged. It has the columns of all forms
     * given in the 'forms' argument, in ascending alpha order. Two
     * columns are added: 'Form name' and 'Create date'. Never
     * <code>null</code>, may be empty.
     * @throws Exception
     */
    public String generateCsv(List<IPSFormData> forms) throws IOException
    {
        if (forms == null || forms.size() == 0)
            return StringUtils.EMPTY;

        FormDataProcessingResult parsingResult = parseCSVData(forms);

        return writeCSV(parsingResult);
    }
    
    /**
     * Generate the email body containing the fields and values including form name and create date
     * 
     * @param form The form to process, not <code>null</code>.
     */
    public String generateEmailBody(IPSFormData form)
    {
        Validate.notNull(form);
        
        SortedSet<CaselessString> finalHeaderSet = new TreeSet<>();
        addColumnNames(finalHeaderSet, form.getFieldNames());
        List<CaselessString> finalHeader = prepareHeader(finalHeaderSet);
        Map<CaselessString, String> fieldMap = processCsvRow(form);
        
        StringBuilder bodyBuilder = new StringBuilder();
        for (CaselessString cString : finalHeader)
        {
        	// this first line is so the generated e-mail disregards the empty honeypot field
        	if(cString.string.equals("topyenoh"))
        		continue;
            bodyBuilder.append(cString.string);
            bodyBuilder.append(": ");
            String value = fieldMap.get(cString);
            bodyBuilder.append(value == null ? "" : value);
            bodyBuilder.append("\r\n");
        }
        
        return bodyBuilder.toString();
    }

    /**
     * A caseless representation of a String object.
     * 
     * @author miltonpividori
     * 
     */
    final class CaselessString implements Comparable<CaselessString>
    {
        /**
         * Original string.
         */
        private final String string;

        /**
         * Normalized string used for comparisons.
         */
        private final String normalizedString;

        public CaselessString(String string)
        {
            this.string = string;
            normalizedString = string.toUpperCase();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof CaselessString))
                return false;

            return ((CaselessString) obj).normalizedString.equals(normalizedString);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return normalizedString.hashCode();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.String#compareTo()
         */
        // @Override
        public int compareTo(CaselessString o)
        {
            return normalizedString.compareTo(o.normalizedString);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return string;
        }
    }

    /**
     * Represents the processing result of the forms data. It has the final
     * header and the data.
     * 
     * @author miltonpividori
     * 
     */
    class FormDataProcessingResult
    {
        private String[] header;

        private List<Map<String, String>> data;

        /**
         * Creates an empty ParsingResult.
         */
        public FormDataProcessingResult()
        {

        }

        /**
         * Creates a ParsingResult with the given header and data.
         * 
         * @param header Header of the parsing result.
         * @param data Data list of parsing result. It's a list of Maps. Each key/value pair
         * represents a CSV row, with the field as the key and its value as the Map's value.
         */
        public FormDataProcessingResult(List<CaselessString> header, List<Map<String, String>> data)
        {
            this.header = new String[header.size()];
            for (int i = 0; i < header.size(); i++)
                this.header[i] = header.get(i).toString();

            this.data = data;
        }

        /**
         * @return the header
         */
        public String[] getHeader()
        {
            return header;
        }

        /**
         * @return Data list of parsing result. It's a list of Maps. Each key/value pair
         * represents a CSV row, with the field as the key and its value as the Map's value.
         */
        public List<Map<String, String>> getData()
        {
            return data;
        }

        /**
         * Checks if the ParsingResult is empty.
         * 
         * @return Returns true if the ParsingResult object has no header and no
         *         data. False otherwise.
         */
        public boolean isEmpty()
        {
            return header == null && data == null;
        }
    }
}
