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
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author miltonpividori
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-beans.xml"})
public class PSFormServiceTest extends PSBaseFormServiceTest
{


    @Test
    public void testSave_NullForm()
    {
        try
        {
            formService.save(null);
            fail("save didn't throw an exception with null form");
        }
        catch(IllegalArgumentException ex)
        {

        }
    }

    @Test
    public void testSave()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String[] multipleValuesField = new String[] {
                "multipleValuesField",
                StringUtils.join(new String[] { "first", "second", "third" },
                        FIELD_VALUES_SEPARATOR)
        };

        IPSFormData formData =
                generateFormData("testform1",
                        fieldValue1[0], fieldValue1[1],
                        fieldValue2[0], fieldValue2[1],
                        multipleValuesField[0], multipleValuesField[1]);

        formService.save(formData);

        List<IPSFormData> allForms = formService.findAllForms();

        assertEquals("form count", 1, allForms.size());
        assertEquals("form name", "testform1", allForms.get(0).getName());


        assertEquals("form fields count", 3, allForms.get(0).getFields().size());
        for (String fieldName : allForms.get(0).getFields().keySet())
        {
            String value = allForms.get(0).getFields().get(fieldName);
            value = value.replace(FIELD_VALUES_SEPARATOR,"");
            if (fieldName.equals(fieldValue1[0]))
            {
                assertEquals("field 1 value", fieldValue1[1], value);
            }
            else if (fieldName.equals(fieldValue2[0]))
            {
                assertEquals("field 2 value", fieldValue2[1], value);
            }
            else if (fieldName.equals(multipleValuesField[0]))
            {
                String mValue = multipleValuesField[1];
                value = value.replace("\\",FIELD_VALUES_SEPARATOR);

                assertEquals("multiple values field - count", mValue, value);

            }
            else
                fail("invalid field name");
        }




    }

    @Test
    public void testSave_InvalidForm()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String[] multipleValuesField = new String[] {
                "multipleValuesField",
                StringUtils.join(new String[] { "first", "second", "third" },
                        FIELD_VALUES_SEPARATOR)
        };

        IPSFormData formData =
                generateFormData("testfor!m1",
                        fieldValue1[0], fieldValue1[1],
                        fieldValue2[0], fieldValue2[1],
                        multipleValuesField[0], multipleValuesField[1]);
        try {
            formService.save(formData);
            fail("Invalid form name should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {

        }

    }

    @Test
    public void testFilterInvalidForm()
    {
        addInvalidFormToDb();
        try
        {
            List<IPSFormData> allForms = formService.findAllForms();
            for (IPSFormData form : allForms)
            {
                if (form.getName().startsWith(INVALID_FORM_NAME_PREFIX))
                    fail("Found invalid form " + form.getName() + " that should have been filtered by service");
            }
            List<String> formNames = formService.findDistinctFormNames();
            for (String formName : formNames)
            {
                if (formName.startsWith(INVALID_FORM_NAME_PREFIX))
                    fail("Found invalid form name" + formName + " that should have been filtered by service");
            }
        }
        finally
        {
            removeInvalidFormsFromDb();
        }
    }

    @Test
    public void testDelete_NonExistingForm()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        IPSFormData formData =
                generateFormData("testform1",
                        fieldValue1[0], fieldValue1[1],
                        fieldValue2[0], fieldValue2[1]);

        formService.delete(formData);
    }

    @Test
    public void testDelete()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        IPSFormData formData =
                generateFormData("testform1",
                        fieldValue1[0], fieldValue1[1],
                        fieldValue2[0], fieldValue2[1]);

        formService.save(formData);
        List<IPSFormData> allForms = formService.findAllForms();
        assertEquals("form count", 1, allForms.size());

        formService.delete(allForms.get(0));

        assertEquals("form count", 0, getAllForms().size());
    }

    @Test
    public void testMarkAsExported()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        List<IPSFormData> allForms = formService.findAllForms();

        formService.markAsExported(allForms.subList(0, 5));

        // Assert
        allForms = formService.findAllForms();
        assertEquals("form count", 10,allForms.size());

        for (int i = 0; i < 5; i++)
        {
            IPSFormData form = allForms.get(i);

            assertTrue("is exported", form.isExported() == 'y');
        }

        for (int i = 5; i < 10; i++)
        {
            IPSFormData form = allForms.get(i);

            assertTrue("is exported", form.isExported() == 'n');
        }
    }

    @Test
    public void testMarkAsExported_NonExistingForms()
    {
        Collection<IPSFormData> forms = new ArrayList<IPSFormData>();
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            PSFormData formData =
                    generateFormData("testform1" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            forms.add(formData);
        }

        formService.markAsExported(forms);
    }

    @Test
    public void testGetExportedFormCount_WithFormNameArgument()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + (i == 3 ? 2 : i),
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.markAsExported(getAllForms());

        // Assert
        String testFormName = "testform2";

        long count = formService.getExportedFormCount(testFormName);
        assertEquals("exported forms count", 2, count);

        count = formService.getExportedFormCount(testFormName.toUpperCase());
        assertEquals("exported forms count", 2, count);
    }

    @Test
    public void testGetExportedFormCount_FormNameWithSpecialCharacters()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + (i == 3 ? 2 : i),
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.markAsExported(getAllForms());

        // Assert
        long count = formService.getExportedFormCount("testform_");
        assertEquals("exported forms count", 0, count);
    }

    @Test
    public void testGetExportedFormCount_WithoutFormNameArgument()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.markAsExported(getAllForms().subList(0, 4));

        // Assert
        long count = formService.getExportedFormCount(null);
        assertEquals("exported forms count", 4, count);

        count = formService.getExportedFormCount(StringUtils.EMPTY);
        assertEquals("exported forms count", 4, count);
    }

    @Test
    public void testGetTotalFormCount_WithFormNameArgument()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + (i == 3 ? 2 : i),
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        String testFormName = "testform2";

        // Assert
        long count = formService.getTotalFormCount(testFormName);
        assertEquals("forms count", 2, count);

        count = formService.getTotalFormCount(testFormName.toUpperCase());
        assertEquals("forms count", 2, count);
    }

    @Test
    public void testGetTotalFormCount_FormNameWithSpecialCharacters()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        // Assert
        long count = formService.getTotalFormCount("testform_");
        assertEquals("forms count", 0, count);
    }

    @After
    // @Transactional
    public void tearDown()
    {
        List<IPSFormData> forms = formService.findAllForms();
        for(IPSFormData formData:forms){
            formService.delete(formData);
        }


    }

    @Test
    public void testGetTotalFormCount_WithoutFormNameArgument()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        // Assert
        long count = formService.getTotalFormCount(null);
        assertEquals("forms count", 10, count);

        count = formService.getTotalFormCount(StringUtils.EMPTY);
        assertEquals("forms count", 10, count);
    }

    @Test
    public void testDeleteExportedForms_FormDoesNotExist()
    {
        formService.deleteExportedForms("testform1");
    }

    @Test
    public void testDeleteExportedForms_FormExists_ButIsNotExported()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.deleteExportedForms("testform1");

        // Assert
        List<IPSFormData> forms = formService.findAllForms();
        assertEquals("forms count", 10, forms.size());
    }

    @Test
    public void testDeleteExportedForms_ValidFormName()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.markAsExported(getAllForms());

        String deletedForm = "testform2";
        formService.deleteExportedForms(deletedForm);

        // Assert
        List<IPSFormData> forms = formService.findAllForms();
        assertEquals("forms count", 9, forms.size());
        for (IPSFormData f : forms)
        {
            assertFalse("form name is not the removed one", f.getName().equals(deletedForm));
        }
    }

    @Test
    public void testDeleteExportedForms_DiffersFormNameOnlyInCasing()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.markAsExported(getAllForms());

        String deletedForm = "testform2";
        formService.deleteExportedForms(deletedForm.toUpperCase());

        // Assert
        List<IPSFormData> forms = formService.findAllForms();
        assertEquals("forms count", 9, forms.size());
        for (IPSFormData f : forms)
        {
            assertFalse("form name is not the removed one", f.getName().equals(deletedForm));
        }
    }

    @Test
    public void testDeleteExportedForms_FormNameWithSpecialCharacters()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        formService.markAsExported(getAllForms());

        String deletedForm = "testform_";
        formService.deleteExportedForms(deletedForm);

        // Assert
        List<IPSFormData> forms = formService.findAllForms();
        assertEquals("forms count", 10, forms.size());
    }

    @Test
    public void testFindFormsByName_WithFormNameArgument_CheckOrder() throws Exception
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<3; i++)
        {
            IPSFormData formData =
                    generateFormData("testform",
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);

            Thread.sleep(2000);
        }

        String testFormName = "testform";

        // Assert
        List<IPSFormData> forms = formService.findFormsByName(testFormName);
        checkFindFormsByNameResult(fieldValue1, fieldValue2, testFormName, forms);

        // Different case
        forms = formService.findFormsByName(testFormName.toUpperCase());
        checkFindFormsByNameResult(fieldValue1, fieldValue2, testFormName, forms);
    }

    private void checkFindFormsByNameResult(String[] fieldValue1, String[] fieldValue2,
                                            String testFormName, List<IPSFormData> forms)
    {
        assertNotNull("forms not null", forms);
        assertEquals("forms count", 3, forms.size());

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, cal.getActualMinimum(Calendar.YEAR));

        Calendar previousDate = cal;
        Calendar currentDate = Calendar.getInstance();

        for (IPSFormData aForm : forms)
        {
            assertEquals("forms name", testFormName, aForm.getName());
            assertEquals("forms fields count", 2, aForm.getFields().size());
            assertTrue("form field 1", aForm.getFields().containsKey(fieldValue1[0]));
            String filedValue1 = aForm.getFields().get(fieldValue1[0]);
            assertEquals("form field 1 value", fieldValue1[1], filedValue1.replace(FIELD_VALUES_SEPARATOR,"") );
            assertTrue("form field 2", aForm.getFields().containsKey(fieldValue1[0]));
            String filedValue2 = aForm.getFields().get(fieldValue2[0]);
            assertEquals("form field 2 value", fieldValue2[1], filedValue2.replace(FIELD_VALUES_SEPARATOR,""));

            // Make sure the comments are ascending sorted
            currentDate.setTime(aForm.getCreated());
            assertTrue("form created order", previousDate.compareTo(currentDate) < 0);
            previousDate.setTime(aForm.getCreated());
        }
    }

    @Test
    public void testFindFormsByName_FormNameWithSpecialCharacters()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        // Assert
        String testFormName = "testform_";

        List<IPSFormData> forms = formService.findFormsByName(testFormName);
        assertNotNull("forms not null", forms);
        assertEquals("forms count", 0, forms.size());
    }

    @Test
    public void testFindFormsByName_WithoutFormNameArgument()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        // Assert
        try
        {
            formService.findFormsByName(null);
            fail("exception not thrown");
        }
        catch (IllegalArgumentException e)
        {

        }
        List<IPSFormData> forms = formService.findFormsByName(StringUtils.EMPTY);
        assertNotNull("forms not null", forms);
        assertEquals("forms count", 0, forms.size());
    }

    @Test
    public void testFindAllForms()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        for (int i=0; i<10; i++)
        {
            IPSFormData formData =
                    generateFormData("testform" + i,
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        // Assert
        List<IPSFormData> forms = formService.findAllForms();
        assertNotNull("forms not null", forms);
        assertEquals("forms count", 10, forms.size());
    }

    @Test
    public void testFindDistinctFormNames()
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };
        String[] formNames = new String[] {
                "testform",
                "TESTFORM",
                "testForm",
                "anotherForm",
                "lastForm"
        };

        for (int i=0; i<10; i++)
        {

            IPSFormData formData =
                    generateFormData(formNames[i % 5],
                            fieldValue1[0], fieldValue1[1],
                            fieldValue2[0], fieldValue2[1]);

            formService.save(formData);
        }

        // Assert
        List<String> distinctFormNames = formService.findDistinctFormNames();
        assertNotNull("forms not null", distinctFormNames);
        assertEquals("forms count", 3, distinctFormNames.size());
    }

    @Test
    @Ignore("Test fails for no good reason - bad mock")
    public void testSendFormEmail() throws Exception
    {
        String[] fieldValue1 = new String[] { "field1", "value1" };
        String[] fieldValue2 = new String[] { "field2", "value2" };

        IPSFormData formData = generateFormData("testform", fieldValue2[0], fieldValue2[1], fieldValue1[0], fieldValue1[1]);

        String toList = "test1@percussion1.com,test2@percussion2.com, test3@percussion3.com";
        String subject = "testFormEmailData";

        formService.emailFormData(toList, subject, formData);
        validateEmailSent(fieldValue1, fieldValue2, toList, subject);
    }

    @Test
    public void testValidateFormName() throws Exception
    {

        String valid1="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
        String invalid1="quote'";
        String invalid2="test<1";
        String invalid3="test>1";

        assertTrue(formService.isValidFormName(valid1));
        assertFalse(formService.isValidFormName(invalid1));
        assertFalse(formService.isValidFormName(invalid2));
        assertFalse(formService.isValidFormName(invalid3));
    }

}
