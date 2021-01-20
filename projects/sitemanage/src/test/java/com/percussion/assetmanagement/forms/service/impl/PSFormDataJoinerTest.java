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

package com.percussion.assetmanagement.forms.service.impl;

import static org.junit.Assert.assertEquals;

import com.percussion.assetmanagement.forms.service.impl.PSFormDataJoiner;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;


public class PSFormDataJoinerTest
{
    private PSFormDataJoiner formDataJoiner;
    
    // form1
    private static final String FORM1_COLUMNS =
        "Form name,Create date,field1,field2";
    
    private static final String FORM1_DATA =
        "testform1,12/22/2010 14:28:00,value1,value2";
    
    // form1 with different columns than form1
    private static final String FORM1_2_COLUMNS =
        "Form name,Create date,field3,field4";
    
    private static final String FORM1_2_DATA =
        "testform1,12/18/2010 20:28:00,value33,value44\n" +
        "testform1,12/19/2010 22:27:00,value331,value441";
    
    // form1 with shared columns names with form1
    private static final String FORM1_3_COLUMNS =
        "Form name,Create date,field2,field3";
    
    private static final String FORM1_3_DATA =
        "testform1,12/18/2010 20:28:00,value22,value33\n" +
        "testform1,12/19/2010 22:27:00,value221,value331";
    
    // form2 (same columns as form1)
    private static final String FORM2_COLUMNS =
        FORM1_COLUMNS;
    
    private static final String FORM2_DATA =
        "testform2,12/15/2010 20:28:00,value3,value4\n" +
        "testform2,12/16/2010 22:28:00,value31,value41";
    
    @Before
    public void setUp() throws Exception
    {
        this.formDataJoiner = new PSFormDataJoiner();
    }
    
    @Test
    public void testJoinFormData_EmptyData() throws Exception
    {
        assertEquals("joined data should be empty",
                "",
                this.formDataJoiner.joinFormData(new String[] { "" }));
    }
    
    @Test
    public void testJoinFormData_EmptyData2() throws Exception
    {
        assertEquals("joined data should be empty", 
                "",
                this.formDataJoiner.joinFormData(new String[0]));
    }
    
    @Test
    public void testJoinFormData_EmptyData3() throws Exception
    {
        assertEquals("joined data should be empty", 
                "",
                this.formDataJoiner.joinFormData((String[])null));
    }
    
    @Test
    public void testJoinFormData_SingleData() throws Exception
    {
        assertEquals("joined data should be the same",
                    join(FORM1_COLUMNS, FORM1_DATA),
                this.formDataJoiner.joinFormData(
                        new String[] {
                                join(FORM1_COLUMNS, FORM1_DATA)
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_SingleData_FieldValueWithComma() throws Exception
    {
        assertEquals("joined data should be the same",
                    join("Form name,Create date,field1,field2",
                            "testform1,12/22/2010 14:28:00,\"value1, with comma\",value2"),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join("Form name,Create date,field1,field2",
                                "testform1,12/22/2010 14:28:00,\"value1, with comma\",value2") 
                        }));
    }
    
    @Test
    public void testJoinFormData_MultipleData_FormsWithSameColumns() throws Exception
    {
        assertEquals("returned csv file",
                    join(FORM1_COLUMNS,
                            FORM1_DATA,
                            FORM2_DATA),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join(FORM1_COLUMNS, FORM1_DATA),
                            join(FORM2_COLUMNS, FORM2_DATA)
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_SomeFieldValuesHaveCommas() throws Exception
    {
        assertEquals("returned csv file",
                    join("Form name,Create date,field1,field2",
                            "testform1,12/22/2010 14:28:00,\"value1, with comma\",value2",
                            "testform2,12/15/2010 20:28:00,value3,value4",
                            "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\""),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join("Form name,Create date,field1,field2",
                                "testform1,12/22/2010 14:28:00,\"value1, with comma\",value2" + "\n"),
                            join("Form name,Create date,field1,field2",
                                "testform2,12/15/2010 20:28:00,value3,value4",
                                "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\"")
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_DoubleQuotesAtTheBegining() throws Exception
    {
        assertEquals("returned csv file",
                    join("Form name,Create date,field1,field2",
                            "testform1,12/22/2010 14:28:00,\"\"\"value1, with double quote\",value2",
                            "testform2,12/15/2010 20:28:00,value3,value4",
                            "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\""),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join("Form name,Create date,field1,field2",
                                "testform1,12/22/2010 14:28:00,\"\"\"value1, with double quote\",value2"),
                            join("Form name,Create date,field1,field2",
                                "testform2,12/15/2010 20:28:00,value3,value4",
                                "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\"")
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_DoubleQuotesAtTheMiddle() throws Exception
    {
        assertEquals("returned csv file",
                    join("Form name,Create date,field1,field2",
                            "testform1,12/22/2010 14:28:00,\"value1, \"\"with\"\" double quote\",value2",
                            "testform2,12/15/2010 20:28:00,value3,value4",
                            "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\""),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join("Form name,Create date,field1,field2",
                                "testform1,12/22/2010 14:28:00,\"value1, \"\"with\"\" double quote\",value2"),
                            join("Form name,Create date,field1,field2",
                                "testform2,12/15/2010 20:28:00,value3,value4",
                                "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\"")
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_DoubleQuotesAtTheEnd() throws Exception
    {
        assertEquals("returned csv file",
                    join("Form name,Create date,field1,field2",
                            "testform1,12/22/2010 14:28:00,\"value1, with double quote\"\"\",value2",
                            "testform2,12/15/2010 20:28:00,value3,value4",
                            "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\""),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join("Form name,Create date,field1,field2",
                                "testform1,12/22/2010 14:28:00,\"value1, with double quote\"\"\",value2"),
                            join("Form name,Create date,field1,field2",
                                "testform2,12/15/2010 20:28:00,value3,value4",
                                "testform2,12/16/2010 22:28:00,value31,\"value41, with comma\"")
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_FormsWithDifferentColumns_AllColumnsAreUnique() throws Exception
    {
        assertEquals("returned csv file",
                    // join form1's columns with form3's ones
                    join("Form name,Create date,field1,field2,field3,field4",
                            "testform1,12/22/2010 14:28:00,value1,value2,,",
                            "testform1,12/18/2010 20:28:00,,,value33,value44",
                            "testform1,12/19/2010 22:27:00,,,value331,value441"),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join(FORM1_COLUMNS, FORM1_DATA),
                            join(FORM1_2_COLUMNS, FORM1_2_DATA)
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_InversedSubmissionOrder() throws Exception
    {
        assertEquals("returned csv file",
                // join form1's columns with form3's ones
                join("Form name,Create date,field1,field2,field3,field4",
                        "testform1,12/18/2010 20:28:00,,,value33,value44",
                        "testform1,12/19/2010 22:27:00,,,value331,value441",
                        "testform1,12/22/2010 14:28:00,value1,value2,,"),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join(FORM1_2_COLUMNS, FORM1_2_DATA),
                            join(FORM1_COLUMNS, FORM1_DATA)
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_FormsWithDifferentColumns_SomeColumnsShared() throws Exception
    {
        assertEquals("returned csv file",
                // join form1's columns with form3's ones
                join("Form name,Create date,field1,field2,field3",
                        "testform1,12/22/2010 14:28:00,value1,value2,",
                        "testform1,12/18/2010 20:28:00,,value22,value33",
                        "testform1,12/19/2010 22:27:00,,value221,value331"),
                this.formDataJoiner.joinFormData(
                        new String[] {
                            join(FORM1_COLUMNS, FORM1_DATA),
                            join(FORM1_3_COLUMNS, FORM1_3_DATA)
                        }
                ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_SomeColumnsDifferByCaseOnly() throws Exception
    {
        assertEquals("returned csv file",
                join("Form name,Create date,field1,Field2,field3",
                        "testform1,12/22/2010 14:28:00,value1,value2,",
                        "testform2,12/15/2010 20:28:00,,value2,value3",
                        "testform2,12/16/2010 22:28:00,,value21,value31"),
            this.formDataJoiner.joinFormData(
                    new String[] {
                        join("Form name,Create date,field1,Field2",
                            "testform1,12/22/2010 14:28:00,value1,value2"),
                        join("Form name,Create date,FIELD2,field3",
                            "testform2,12/15/2010 20:28:00,value2,value3",
                            "testform2,12/16/2010 22:28:00,value21,value31")
                    }
            ));
    }
    
    @Test
    public void testJoinFormData_MultipleData_SomeColumnsDifferByCaseOnly2() throws Exception
    {
        assertEquals("returned csv file",
                join("Form name,Create date,field1,Field2,FIELD3",
                        "testform1,12/22/2010 14:28:00,value1,value2,",
                        "testform1,12/22/2010 14:28:00,value1,value2,",
                        "testform2,12/15/2010 20:28:00,,value2,value3",
                        "testform2,12/16/2010 22:28:00,,value21,value31"),
            this.formDataJoiner.joinFormData(
                    new String[] {
                        join("Form name,Create date,field1,Field2",
                            "testform1,12/22/2010 14:28:00,value1,value2"),
                        join("Form name,Create date,field1,FiELd2",
                            "testform1,12/22/2010 14:28:00,value1,value2"),
                        join("Form name,Create date,field2,FIELD3",
                            "testform2,12/15/2010 20:28:00,value2,value3"),
                        join("Form name,Create date,FIELD2,field3",
                            "testform2,12/16/2010 22:28:00,value21,value31")
                    }
            ));
    }
    
    private String join(String... data)
    {
        return StringUtils.join(data, "\n") + "\n";
    }
}
