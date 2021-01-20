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

import com.percussion.delivery.forms.data.IPSFormData;
import com.percussion.delivery.forms.data.PSFormData;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * 
 * @author miltonpividori
 *
 */
public class PSFormDataJoinerTest
{

    private PSFormDataJoiner formDataJoiner;
    
    /**
     * Helper method to generate PSFormData for test cases.
     * 
     * @param data An array consisting of form names and their
     * values for each field. Odd positions have the form name,
     * and even ones have a String array with values.
     * @return List of PSFormData created as from the given list of data.
     */
    private List<IPSFormData> generateSampleData(Object[] data)
    {
        List<IPSFormData> result = new ArrayList<IPSFormData>();
        PSFormData formData;
        String[] fieldValues;
        Map<String,String[]> props;
        
        for (int i=0; i<data.length; i+=2)
        {
            fieldValues = (String[]) data[i+1];
            props = new HashMap<String, String[]>();
            
            for (int j=0; j<fieldValues.length; j+=2)
            {
                props.put(fieldValues[j], new String[] { fieldValues[j+1] });
            }
            
            formData = new PSFormData(data[i].toString(), props);
            
            result.add(formData);
        }
        
        return result;
    }
    
    @Before
    public void setUp()
    {
        formDataJoiner = new PSFormDataJoiner();
    }
    
    @Test
    public void testGenerateCsv_NoForms() throws Exception
    {
        // TODO This test case must be validated. Should the joiner
        // return an empty string when it receives an empty list of
        // PSFormData? Before refactoring, it did it.
        assertEquals("output is empty",
                StringUtils.EMPTY,
                formDataJoiner.generateCsv(new ArrayList<IPSFormData>()));
    }
    
    @Test
    public void testGenerateCsv_SingleForm() throws Exception
    {
        // PSFormData is immutable as constructed, so an expected creation date cannot
        // be compared
        List<IPSFormData> data = generateSampleData(
                new Object[] {
                    "testform1",
                    new String[] {
                        "field1", "value1"
                    }
                }
            );
        
        Date created = data.get(0).getCreateDate();
        assertEquals("output CSV is correctly generated",
                join("Form name,Create date,field1",
                        "testform1," + created.toString() + ",value1"),
                formDataJoiner.generateCsv(data));
    }
    
    @Test
    public void testGenerateCsv_SingleForm_FieldValueWithComma() throws Exception
    {
        List<IPSFormData> formData = 
                generateSampleData(
                        new Object[] {
                            "testform1",
                            new String[] {
                                "field1", "value1, with comma"
                            }
                        }
                    );
        assertEquals("output CSV is correctly generated",
                join("Form name,Create date,field1",
                        "testform1," + formData.get(0).getCreateDate().toString() + ",\"value1, with comma\""),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_FormsWithSameColumns() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value1_1",
                            "field2", "value2_1"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value1_2",
                            "field2", "value2_2"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2",
                        "testform1,{0},value1,value2",
                        "testform2,{0},value1_1,value2_1",
                        "testform2,{0},value1_2,value2_2"),
                formDataJoiner.generateCsv(
                        formData
                ));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_SomeFieldValuesHaveCommas() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1, with comma",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value1_1",
                            "field2", "value2_1"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value1_2",
                            "field2", "value2_2, with comma"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2",
                        "testform1,{0},\"value1, with comma\",value2",
                        "testform2,{0},value1_1,value2_1",
                        "testform2,{0},value1_2,\"value2_2, with comma\""),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_DoubleQuotesAtTheBegining() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "\"value1, with double quote",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value3",
                            "field2", "value4"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value31",
                            "field2", "value41, with comma"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2",
                        "testform1,{0},\"\"\"value1, with double quote\",value2",
                        "testform2,{0},value3,value4",
                        "testform2,{0},value31,\"value41, with comma\""),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_DoubleQuotesAtTheMiddle() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1 \"with\" double quote",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value3",
                            "field2", "value4"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value31",
                            "field2", "value41 \"with\" comma"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2",
                        "testform1,{0},\"value1 \"\"with\"\" double quote\",value2",
                        "testform2,{0},value3,value4",
                        "testform2,{0},value31,\"value41 \"\"with\"\" comma\""),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_DoubleQuotesAtTheEnd() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1 with double quote\"",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value3",
                            "field2", "value4"
                        },
                        
                        "testform2",
                        new String[] {
                            "field1", "value31",
                            "field2", "value41 with comma\""
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2",
                        "testform1,{0},\"value1 with double quote\"\"\",value2",
                        "testform2,{0},value3,value4",
                        "testform2,{0},value31,\"value41 with comma\"\"\""),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_AllColumnsAreUnique() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field3", "value3",
                            "field4", "value4"
                        },
                        
                        "testform2",
                        new String[] {
                            "field3", "value31",
                            "field4", "value41"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2,field3,field4",
                        "testform1,{0},value1,value2,,",
                        "testform2,{0},,,value3,value4",
                        "testform2,{0},,,value31,value41"),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_InversedSubmissionOrder() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform2",
                        new String[] {
                            "field3", "value3",
                            "field4", "value4"
                        },
                        
                        "testform2",
                        new String[] {
                            "field3", "value31",
                            "field4", "value41"
                        },
                        
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "field2", "value2"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2,field3,field4",
                        "testform2,{0},,,value3,value4",
                        "testform2,{0},,,value31,value41",
                        "testform1,{0},value1,value2,,"),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_SomeColumnsShared() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field2", "value21",
                            "field3", "value31"
                        },
                        
                        "testform2",
                        new String[] {
                            "field2", "value22",
                            "field3", "value32"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2,field3",
                        "testform1,{0},value1,value2,",
                        "testform2,{0},,value21,value31",
                        "testform2,{0},,value22,value32"),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_SameForm_SomeColumnsShared() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "field2", "value2"
                        },
                        
                        "testform1",
                        new String[] {
                            "field2", "value21",
                            "field3", "value31"
                        },
                        
                        "testform1",
                        new String[] {
                            "field2", "value22",
                            "field3", "value32"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,field2,field3",
                        "testform1,{0},value1,value2,",
                        "testform1,{0},,value21,value31",
                        "testform1,{0},,value22,value32"),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_SomeColumnsDifferByCaseOnly() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "Field2", "value2"
                        },
                        
                        "testform2",
                        new String[] {
                            "field2", "value21",
                            "field3", "value31"
                        },
                        
                        "testform2",
                        new String[] {
                            "FIELD2", "value22",
                            "field3", "value32"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,Field2,field3",
                        "testform1,{0},value1,value2,",
                        "testform2,{0},,value21,value31",
                        "testform2,{0},,value22,value32"),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateCsv_MultipleData_SomeColumnsDifferByCaseOnly2() throws Exception
    {
        List<IPSFormData> formData = generateSampleData(
                new Object[] {
                        "testform1",
                        new String[] {
                            "field1", "value1",
                            "Field2", "value2"
                        },
                        
                        "testform1",
                        new String[] {
                            "field1", "value11",
                            "FiELd2", "value21"
                        },
                        
                        "testform2",
                        new String[] {
                            "field2", "value21",
                            "FIELD3", "value31"
                        },
                        
                        "testform2",
                        new String[] {
                            "FIELD2", "value22",
                            "field3", "value32"
                        }
                    }
                );
        assertEquals("output CSV is correctly generated",
                join(formData, "Form name,Create date,field1,Field2,FIELD3",
                        "testform1,{0},value1,value2,",
                        "testform1,{0},value11,value21,",
                        "testform2,{0},,value21,value31",
                        "testform2,{0},,value22,value32"),
                formDataJoiner.generateCsv(formData));
    }
    
    @Test
    public void testGenerateEmailForm() throws Exception
    {
        // PSFormData is immutable as constructed, so an expected creation date cannot
        // be compared
        List<IPSFormData> data = generateSampleData(
                new Object[] {
                    "testform1",
                    new String[] {
                        "field2", "value2",
                        "field1", "value1"                        
                    }
                }
            );
        
        Date created = data.get(0).getCreateDate();
        assertEquals("EmailBody is correctly generated",
                getEmailBody("Form name", "testform1", "Create date", created.toString(),"field1", "value1",
                        "field2","value2"),
                formDataJoiner.generateEmailBody(data.get(0)));
    }
    
    private String join(String... data)
    {
        String lineSeparator = "\n";
        return StringUtils.join(data, lineSeparator) + lineSeparator;
    }
    
    private String getEmailBody(String... data)
    {
        String body = "";
        for (int i=0; i<data.length; i+=2)
        {
            body += data[i] + ": " + data[i+1] + "\r\n";
        }
        
        return body;
    }
    
    /**
     * Join the supplied data, using supplied formdata to fill in expected created date.
     * 
     * @param formData List of formdata, length is one less than length of data array.
     * @param data Array of expected form data to join, first entry is field names followed by list of 
     * expected form data with single message format param for the created date.
     * 
     * @return The joined form data strings with the correct created dates inserted.
     */
    private String join(List<IPSFormData> formData, String... data)
    {
        String[] newData = new String[data.length];
        
        for (int i = 0; i < data.length; i++)
        {
            // skip first entry, contains field names not data
            if (i == 0)
            {
                newData[i] = data[i];
                continue;
            }
            
            newData[i] = MessageFormat.format(data[i], formData.get(i-1).getCreateDate().toString());
        }
        
        String lineSeparator = "\n";
        return StringUtils.join(newData, lineSeparator) + lineSeparator;
    }
}
