/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.service;

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref.EnumValue;
import com.percussion.pagemanagement.data.PSWidgetDefinition.UserPref;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.impl.PSWidgetUserPropertiesValidator;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSValidationException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Scenario description: 
 * @author adamgent, Nov 24, 2009
 */
@RunWith(JMock.class)
public class PSWidgetServiceValidationTest
{

    Mockery context = new JUnit4Mockery();

    PSWidgetUserPropertiesValidator validator;

    private IPSWidgetService widgetService;

    private PSWidgetDefinition definition = new PSWidgetDefinition();
    
    private PSWidgetItem widgetItem = new PSWidgetItem();
    
    private UserPref numberPref;
    private UserPref stringPref;
    private UserPref listPref;
    private UserPref enumPref;
    private UserPref boolPref;
    
    
    @Before
    public void setUp() throws Exception
    {
        widgetService = context.mock(IPSWidgetService.class);
        validator = new PSWidgetUserPropertiesValidator(widgetService);
        widgetItem.setDefinitionId("wid");
        numberPref = create("number", "number", true);
        stringPref = create("string", "string", true);
        listPref = create("list", "list", true);
        enumPref = create("enum", "enum", true);
        EnumValue a = new EnumValue();
        a.setValue("a");
        EnumValue b = new EnumValue();
        b.setValue("b");
        EnumValue c = new EnumValue();
        c.setValue("c");
        enumPref.getEnumValue().addAll(asList(a,b,c));
        boolPref = create("bool", "bool", true);
        
        
        
    }
    

    
    @Test
    public void shouldValidateWidgetId() throws Exception
    {
        expectDefinition("wid");
        widgetItem.setId("123");
        validator.validate(widgetItem);
        
        expectDefinition("wid");
        widgetItem.setId("2");
        validator.validate(widgetItem);
    }
    
    @Test(expected=PSValidationException.class)
    @Ignore //TODO: Fix Me
    public void shouldValidateWidgetIdAndFailOnNonNumeric() throws Exception
    {
        widgetItem.setId("Blah");
        validator.validate(widgetItem);
    }
    
    @Test
    public void shouldValidateNumberOk() throws Exception
    {
        assertPropertyValid("number", 200, numberPref);
    }

    @Test
    public void shouldValidateNumberAndFail() throws Exception
    {
        assertPropertyInvalid("number", "blah", numberPref);
    }
    

    @Test
    public void shouldValidateList() throws Exception
    {
        assertPropertyValid("list", asList("a", "b"), listPref);
    }
    
    @Test
    public void shouldValidateListAndFail() throws Exception
    {
        assertPropertyInvalid("list", "blah", listPref);
    }
    
    @Test
    public void shouldValidateString() throws Exception
    {
        assertPropertyValid("string", "oK", stringPref);
    }
    
    @Test
    public void shouldValidateStringAndFail() throws Exception
    {
        assertPropertyInvalid("string", 100, stringPref);
    }

    @Test
    public void shouldValidateBool() throws Exception
    {
        assertPropertyValid("bool", true, boolPref);
    }
    
    @Test
    public void shouldValidateBoolAndFail() throws Exception
    {
        assertPropertyInvalid("bool", 100, boolPref);
    }

    @Test
    public void shouldValidateEnum() throws Exception
    {
        assertPropertyValid("enum", "a", enumPref);
        assertPropertyValid("enum", "b", enumPref);
        assertPropertyValid("enum", "c", enumPref);
    }
    
    @Test
    public void shouldValidateEnumAndFail() throws Exception
    {
        assertPropertyInvalid("enum", "d", enumPref);
    }
    
    private PSPropertiesValidationException validate(String field, Object value, UserPref userPref) throws PSDataServiceException {
        definition.getUserPref().add(userPref);
        expectDefinition("wid");
        widgetItem.getProperties().put(field, value);
        return validator.validate(widgetItem);
    }
    
    private void assertPropertyValid(String field, Object value, UserPref userPref) throws PSDataServiceException {
        PSPropertiesValidationException e = validate(field,value,userPref);
        assertFalse(e.hasErrors());
    }
    
    private void assertPropertyInvalid(String field, Object value, UserPref userPref) throws PSDataServiceException {
        PSPropertiesValidationException e = validate(field, value, userPref);
        assertTrue("should have errors", e.hasErrors());
        assertEquals("should equal value", value, e.getFieldValue(field));
    }
    
    
    private UserPref create(String name, String dataType, Boolean required) 
    {
        UserPref up = new UserPref();
        up.setName(name);
        up.setDatatype(dataType);
        up.setRequired("" + required);
        return up;
    }
    
    private void expectDefinition(final String id) throws PSDataServiceException {
        context.checking(new Expectations() {{
            one(widgetService).load(with(any(String.class)));
            will(returnValue(definition));;
        }});
    }
}
