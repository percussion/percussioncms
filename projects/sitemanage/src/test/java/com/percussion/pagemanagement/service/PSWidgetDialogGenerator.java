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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.service;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSDisplayMapping;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUISet;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref.EnumValue;
import com.percussion.pagemanagement.data.PSWidgetDefinition.CssPref;
import com.percussion.pagemanagement.data.PSWidgetDefinition.UserPref;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.server.PSServer;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * When this unit test is run, three .java files may be generated for each widget currently registered in the running
 * server.  The files may include one for each of the tabs available when a page is open for edit (Content, Layout, 
 * Style), and will allow for read/write operations on the input fields of the respective dialogs.  These files will be
 * saved to a directory structure which matches that of the source filesystem, see {@link #WIDGET_DIALOGS_SRC_PATH}.
 * The root of this directory defaults to {@link #DEFAULT_SRC_DIR}.  For example, the following files will be generated
 * for the Rich Text widget:
 * <p>
 * <li>
 * RichTextWidgetContentTabDialog.java
 * <li>
 * RichTextWidgetLayoutTabDialog.java
 * <li>
 * RichTextWidgetStyleTabDialog.java
 * <p>
 * <br>
 * The classes are part of a corresponding sub-package (content, layout, style) under the
 * com.percussion.qa.framework.widgets.dialogs package.  They inherit from
 * com.percussion.qa.framework.widgets.dialogs.WidgetDialog.  By default, dialog classes will be generated for all
 * currently registered widgets.  To generate classes for a single widget, set the {@link #WIDGET_ID_PROP_NAME}
 * property in the {@link #WIDGET_DIALOG_GEN_PROPS} file under {install dir}/rxconfig/Server.  This property should be
 * set to the definition id of the widget.  To change the default source directory, set the {@link #SRC_DIR_PROP_NAME}
 * property in the same file.
 * 
 * @author peterfrontiero
 *
 */
@Category(IntegrationTest.class)
public class PSWidgetDialogGenerator extends PSServletTestCase
{
    @Override
    public void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        loadProperties();
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }    
    
    @Test
    public void testGenerate() throws Exception
    {
        List<String> ids = new ArrayList<String>();
        
        if (StringUtils.isNotBlank(widgetId))
        {
            ids.add(widgetId);
        }
        else
        {        
            for (PSWidgetSummary widgetSum : widgetService.findAll())
            {
                ids.add(widgetSum.getId());
            }
        }
        
        for (String id : ids)
        {
            PSWidgetDefinition widgetDefinition = widgetService.load(id);
            PSWidgetDialogGenerationInfo widgetGenerationInfo = createDialogs(widgetDefinition);
            createTests(widgetGenerationInfo);
        }
    }
      
    public IPSWidgetService getWidgetService()
    {
        return widgetService;
    }

    public void setWidgetService(IPSWidgetService widgetService)
    {
        this.widgetService = widgetService;
    }
    
    private PSWidgetDialogGenerationInfo createDialogs(PSWidgetDefinition widgetDef)
    {
        PSWidgetDialogGenerationInfo widgetDialogGenerationInfo = new PSWidgetDialogGenerationInfo(widgetDef);
        
        widgetDialogGenerationInfo.setLayoutDialogGenerated(createLayoutDialog(widgetDef));
        widgetDialogGenerationInfo.setStyleDialogGenerated(createStyleDialog(widgetDef));
        widgetDialogGenerationInfo.setContentDialogGenerated(createContentDialog(widgetDef));
        
        return widgetDialogGenerationInfo;
    }
    
    private void createTests(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        writeTestClassFile(widgetGenerationInfo.getWidgetDefinition(), generateWidgetSmokeTest(widgetGenerationInfo));
    }
    
    /**
     * 
     * @param widgetDef
     * @return <code>true</code> if the layout dialog class file was created. <code>false</code> otherwise.
     */
    private boolean createLayoutDialog(PSWidgetDefinition widgetDef)
    {
        return writeClassFile(getDialogClassName(widgetDef, LAYOUT_DIALOG_SUFFIX), generateLayoutDialog(widgetDef));
    }
    
    /**
     * 
     * @param widgetDef
     * @return <code>true</code> if the style dialog class file was created. <code>false</code> otherwise.
     */
    private boolean createStyleDialog(PSWidgetDefinition widgetDef)
    {
        return writeClassFile(getDialogClassName(widgetDef, STYLE_DIALOG_SUFFIX), generateStyleDialog(widgetDef));
    }
    
    /**
     * 
     * @param widgetDef
     * @return <code>true</code> if the content dialog class file was created. <code>false</code> otherwise.
     */
    private boolean createContentDialog(PSWidgetDefinition widgetDef)
    {
        return writeClassFile(getDialogClassName(widgetDef, CONTENT_DIALOG_SUFFIX), generateContentDialog(widgetDef));
    }
    
    private String generateLayoutDialog(PSWidgetDefinition widgetDef)
    {
        return generateDialog(getDialogClassName(widgetDef, LAYOUT_DIALOG_SUFFIX),
                generateLayoutMethods(widgetDef), LAYOUT_DIALOG_FOLDER_NAME);        
    }
    
    private String generateStyleDialog(PSWidgetDefinition widgetDef)
    {
        return generateDialog(getDialogClassName(widgetDef, STYLE_DIALOG_SUFFIX), generateStyleMethods(widgetDef),
                STYLE_DIALOG_FOLDER_NAME);               
    }
    
    private String generateContentDialog(PSWidgetDefinition widgetDef)
    {
        return generateDialog(getDialogClassName(widgetDef, CONTENT_DIALOG_SUFFIX), 
                generateContentMethods(widgetDef), CONTENT_DIALOG_FOLDER_NAME);
    }
    
    private String generateDialog(String className, String methods, String folderName)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        if (StringUtils.isNotBlank(methods))
        {
            strBuffer.append(generateClassHeader(className, folderName));
            strBuffer.append(methods);
            strBuffer.append(generateClassFooter());
        }
        
        return strBuffer.toString();            
    }
    
    private String generateWidgetSmokeTest(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        String methods = generateWidgetSmokeTestMethods(widgetGenerationInfo);
        
        if (StringUtils.isNotBlank(methods))
        {
            strBuffer.append(generateSmokeTestClassHeader(widgetGenerationInfo));
            strBuffer.append(methods);
            strBuffer.append(generateClassFooter());
        }
        
        return strBuffer.toString();       
    }
    
    private String generateWidgetSmokeTestMethods(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        strBuffer.append(generateWidgetTestCaseImplementationMethods(widgetGenerationInfo.getWidgetDefinition()));
        strBuffer.append(generateLayoutDialogTests(widgetGenerationInfo));
        strBuffer.append(generateContentDialogTests(widgetGenerationInfo));
        strBuffer.append(generateStyleDialogTests(widgetGenerationInfo));
        
        return strBuffer.toString();
    }
    
    private String generateWidgetTestCaseImplementationMethods(PSWidgetDefinition widgetDef)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        String className = getDialogClassName(widgetDef, TEST_SUFFIX);
        
        strBuffer.append("    @Override\n");
        strBuffer.append("    protected String getPagepath()\n");
        strBuffer.append("    {\n");
        strBuffer.append("        return \"Sites/" + className  + "/page1\";\n");
        strBuffer.append("    }\n");
        
        strBuffer.append("    \n");
        
        strBuffer.append("    @Override\n");
        strBuffer.append("    protected Widget createWidget()\n");
        strBuffer.append("    {\n");
        strBuffer.append("        return new Widget(\"content\", \"" + widgetDef.getId() + "\");\n");
        strBuffer.append("    }\n");
        
        strBuffer.append("    \n");
        
        return strBuffer.toString();
    }
    
    /**
     * 
     * @param comment
     * @param dialogClassName
     * @param methodPrefix
     * @param paramName
     * @param prefs Must be an array of AbstractUserPref instances. For example, UserPref or CssPref
     * objects.
     * @return
     */
    private String generateDialogTests(String comment, String dialogClassName, String methodPrefix, String paramName, Object[] widgetsAndCode)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        strBuffer.append("    /*\n");
        strBuffer.append("     * ");
        strBuffer.append(comment != null ? comment : StringUtils.EMPTY);
        strBuffer.append("\n");
        strBuffer.append("     */\n");
        
        strBuffer.append("    \n");
        
        strBuffer.append("    @Override\n");
        strBuffer.append("    protected Class<" + dialogClassName + "> get" + methodPrefix + "ConfigDialogClass()\n");
        strBuffer.append("    {\n");
        
        if (NON_EXISTENT_DIALOG_CLASS.equals(dialogClassName))
        {
            strBuffer.append("        // " + methodPrefix + " tab tests disabled.\n");
            strBuffer.append("        return null;\n");
        }
        else
        {
            strBuffer.append("        return " + dialogClassName + ".class;\n");
        }
        
        strBuffer.append("    }\n");
        
        strBuffer.append("    \n");
        
        List<String[]> widgetFieldsAndValues = (List<String[]>) widgetsAndCode[0];
        String makeChangesCode = (String) widgetsAndCode[1];
        
        strBuffer.append("    @Override\n");
        strBuffer.append("    protected void make" + methodPrefix + "TabChanges(" + dialogClassName + " " + paramName + ")\n");
        strBuffer.append("    {\n");
        strBuffer.append(makeChangesCode);
        strBuffer.append("    }\n");
        
        strBuffer.append("    \n");
        
        strBuffer.append("    @Override\n");
        strBuffer.append("    protected void assert" + methodPrefix + "TabChanges(" + dialogClassName + " " + paramName + ")\n");
        strBuffer.append("    {\n");
        strBuffer.append(generateAssertChanges(widgetFieldsAndValues, paramName));
        strBuffer.append("    }\n");
        
        strBuffer.append("    \n");
        
        return strBuffer.toString();
    }
    
    private String generateLayoutDialogTests(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        String paramName = "layoutConfigDialog";
        
        String layoutDialogClassName = NON_EXISTENT_DIALOG_CLASS;
        if (widgetGenerationInfo.isLayoutDialogGenerated())
            layoutDialogClassName = getDialogClassName(widgetGenerationInfo.getWidgetDefinition(), LAYOUT_DIALOG_SUFFIX);
        
        Object[] widgetsAndCode =
                generateMakeChanges(widgetGenerationInfo.getWidgetDefinition().getUserPref().toArray(new UserPref[0]),
                        paramName);
        
        return generateDialogTests("Layout tab tests",
                layoutDialogClassName, "Layout", paramName, widgetsAndCode);
    }

    private Object[] generateMakeChanges(AbstractUserPref[] prefs, String dialogVariableName)
    {
        List<String[]> fieldsInfo = new ArrayList<String[]>();
        for (Object obj : prefs)
        {
            AbstractUserPref userPref = (AbstractUserPref) obj;
            fieldsInfo.add(new String[] { userPref.getName(), userPref.getDatatype(), null });
        }
        
        return generateMakeChanges(fieldsInfo, dialogVariableName);
    }
    
    private String generateAssertChanges(List<String[]> widgetFieldsAndValues, String dialogVariableName)
    {
        StringBuffer strBuffer = new StringBuffer();
        String fieldName;
        String fieldValue;
        
        for (String[] fieldNameAndValue : widgetFieldsAndValues)
        {
            fieldName = fieldNameAndValue[0];
            fieldValue = fieldNameAndValue[1];
            
            strBuffer.append("        assertEquals");
            strBuffer.append("(\"" + fieldName + " value\", ");
            
            strBuffer.append(fieldValue);
            
            strBuffer.append(", " + dialogVariableName + ".");
            strBuffer.append(getWidgetFieldGetter(fieldName) + "()");
            
            if (StringUtils.equals(fieldValue, "true") || StringUtils.equals(fieldValue, "false"))
                strBuffer.append(".booleanValue()");
            
            strBuffer.append(");\n");
        }
        
        return strBuffer.toString();
    }

    private String generateContentDialogTests(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        String contentDialogClassName = NON_EXISTENT_DIALOG_CLASS;
        if (widgetGenerationInfo.isContentDialogGenerated())
            contentDialogClassName = getDialogClassName(widgetGenerationInfo.getWidgetDefinition(), CONTENT_DIALOG_SUFFIX);
        
        Object[] widgetsAndCode = generateContentTabMakeChanges(widgetGenerationInfo.getWidgetDefinition());
        
        return generateDialogTests("Content tab tests", contentDialogClassName,
                "Content", "contentConfigDialog", widgetsAndCode);
    }
    
    /**
     * 
     * @param widgetDef
     * @return An array of a size of 2. The first element is a List<String[]>. The inner
     * array of String has 3 elements with widget's field information: name, datatype and
     * control. The second element is a String with code for "make changes" method.
     */
    private Object[] generateContentTabMakeChanges(PSWidgetDefinition widgetDef)
    {
        PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
        String ctName = widgetDef.getWidgetPrefs().getContenttypeName();
        
        if (StringUtils.isEmpty(ctName))
            return new Object[] {new ArrayList<String[]>(), StringUtils.EMPTY};
        
        PSItemDefinition itemDef;
        try
        {
            long ctId = itemDefMgr.contentTypeNameToId(ctName);
            itemDef = itemDefMgr.getItemDef(ctId, -1);
        }
        catch (PSInvalidContentTypeException e1)
        {
            m_log.warn(e1);
            return new Object[] {new ArrayList<String[]>(), StringUtils.EMPTY};
        }
        
        PSContentEditorMapper ceMapper = itemDef.getContentEditorMapper();
        PSUIDefinition uiDef = ceMapper.getUIDefinition();
        PSDisplayMapper dispMapper = uiDef.getDisplayMapper();
        
        try
        {
            List<PSField> widgetFields = getWidgetFields(widgetDef);
            List<String[]> fieldsInfo = new ArrayList<String[]>();
            String control;
            for (PSField field : widgetFields)
            {
                control = getControl(field, dispMapper);
                if (control == null || isSupportedControl(control))
                    fieldsInfo.add(new String[] { field.getSubmitName(), field.getDataType(), control});
            }
            
            return generateMakeChanges(fieldsInfo, "contentConfigDialog");
        }
        catch (PSInvalidContentTypeException e)
        {
            m_log.warn(e);
        }
        
        return new Object[] { new ArrayList<String[]>(), StringUtils.EMPTY };
    }
    
    private Object[] generateMakeChanges(List<String[]> widgetFieldsInfo, String dialogVariableName)
    {
        StringBuffer strBuffer = new StringBuffer();
        List<String[]> widgetFieldsAndValues = new ArrayList<String[]>();
        String fieldName, fieldType, control, variableType;
        
        for (String[] widgetFieldInfo : widgetFieldsInfo)
        {
            fieldName = widgetFieldInfo[0];
            fieldType = widgetFieldInfo[1];
            control = widgetFieldInfo[2];
            
            strBuffer.append("        " + dialogVariableName + ".");
            strBuffer.append(getWidgetFieldSetter(fieldName));
            strBuffer.append("(");
            
            boolean isEnum = fieldType.equalsIgnoreCase("enum");
            variableType = getVariableType(fieldType, control);
            
            if (isEnum)
                variableType = "Enum";
            
            String fieldValue;
            if (variableType.equalsIgnoreCase("string"))
                fieldValue = "\"\"";
            else if (variableType.equalsIgnoreCase("boolean"))
                fieldValue = "false";
            else
                fieldValue = "null";
            
            strBuffer.append(fieldValue);
            widgetFieldsAndValues.add(new String[] { fieldName, fieldValue });
            
            strBuffer.append(");\n");
        }
        
        return new Object[] { widgetFieldsAndValues, strBuffer.toString() };
    }
    
    private List<PSField> getWidgetFields(PSWidgetDefinition widgetDef) throws PSInvalidContentTypeException
    {
        PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
        String ctName = widgetDef.getWidgetPrefs().getContenttypeName();
        
        List<PSField> fields = new ArrayList<PSField>();
        
        if (StringUtils.isEmpty(ctName))
            return fields;
        
        long ctId = itemDefMgr.contentTypeNameToId(ctName);
        PSItemDefinition itemDef = itemDefMgr.getItemDef(ctId, -1);
        PSContentEditorMapper ceMapper = itemDef.getContentEditorMapper();
        PSFieldSet fieldSet = ceMapper.getFieldSet();
        Iterator iter = fieldSet.getAll();
        
        while (iter.hasNext())
        {
            Object obj = iter.next();
            if (obj instanceof PSField)
            {
                PSField field = (PSField) obj;
                
                if (fieldIsIncluded(field))
                    fields.add(field);
            }
            else if (obj instanceof PSFieldSet)
            {
                PSFieldSet fs = (PSFieldSet) obj;
                Iterator fsIter = fs.getAll();
                while (fsIter.hasNext())
                {
                    PSField field = (PSField) fsIter.next();
                    
                    if (fieldIsIncluded(field))
                        fields.add(field);
                    
                    fields.add(field);
                }
            }
        }
        
        return fields;
    }
    
    private boolean fieldIsIncluded(PSField field)
    {
        return !StringUtils.startsWith(field.getSubmitName(), "sys");
    }
    
    private String getWidgetFieldSetter(String widgetFieldName)
    {
        return "set" + getMethodName(widgetFieldName);
    }
    
    private String getWidgetFieldGetter(String widgetFieldName)
    {
        return "get" + getMethodName(widgetFieldName);
    }
    
    private String generateStyleDialogTests(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        String paramName = "styleConfigDialog";
        
        String styleDialogClassName = NON_EXISTENT_DIALOG_CLASS;
        if (widgetGenerationInfo.isStyleDialogGenerated())
            styleDialogClassName = getDialogClassName(widgetGenerationInfo.getWidgetDefinition(), STYLE_DIALOG_SUFFIX);
        
        Object[] widgetsAndCode =
                generateMakeChanges(widgetGenerationInfo.getWidgetDefinition().getCssPref().toArray(new CssPref[0]),
                        paramName);
        
        return generateDialogTests("Style tab tests", styleDialogClassName, "Style", paramName, widgetsAndCode);
    }

    @SuppressWarnings("unchecked")
    private String generateContentMethods(PSWidgetDefinition widgetDef)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
        String ctName = widgetDef.getWidgetPrefs().getContenttypeName();
        if (StringUtils.isNotEmpty(ctName))
        {
            try
            {
                long ctId = itemDefMgr.contentTypeNameToId(ctName);
                PSItemDefinition itemDef = itemDefMgr.getItemDef(ctId, -1);
                PSContentEditorMapper ceMapper = itemDef.getContentEditorMapper();
                PSUIDefinition uiDef = ceMapper.getUIDefinition();
                PSDisplayMapper dispMapper = uiDef.getDisplayMapper();    
                PSFieldSet fieldSet = ceMapper.getFieldSet();
                Iterator iter = fieldSet.getAll();
                while (iter.hasNext())
                {
                    Object obj = iter.next();
                    if (obj instanceof PSField)
                    {
                        strBuffer.append(generateMethods((PSField) obj, dispMapper));
                    }
                    else if (obj instanceof PSFieldSet)
                    {
                        PSFieldSet fs = (PSFieldSet) obj;
                        Iterator fsIter = fs.getAll();
                        while (fsIter.hasNext())
                        {
                            PSField field = (PSField) fsIter.next();
                            strBuffer.append(generateMethods(field, dispMapper));
                        }
                    }
                }
            }
            catch (PSInvalidContentTypeException e)
            {
                m_log.warn(e);
            }
        }
              
        return strBuffer.toString();   
    }
    
    private String generateLayoutMethods(PSWidgetDefinition widgetDef)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        for (PSWidgetDefinition.UserPref userPref : widgetDef.getUserPref())
        {
            strBuffer.append(generateMethods(userPref));
        }
        
        return strBuffer.toString();
    }
    
    private String generateStyleMethods(PSWidgetDefinition widgetDef)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        for (PSWidgetDefinition.CssPref cssPref : widgetDef.getCssPref())
        {
            strBuffer.append(generateMethods(cssPref));
        }
        
        return strBuffer.toString();
    }
    
    private String generateMethods(AbstractUserPref userPref)
    {
        return generateMethods(userPref.getName(), userPref.getDisplayName(), userPref.getDatatype(), null, 
                userPref.getEnumValue());
    }
    
    private String generateMethods(PSField field, PSDisplayMapper mapper)
    {
        String name = field.getSubmitName();
        String label = getLabel(field, mapper);
        label = StringUtils.isNotBlank(label) ? label : name;
        
        return generateMethods(name, label, field.getDataType(), getControl(field, mapper), null);
    }
    
    private String getMethodName(String fieldSubmitName)
    {
        String methodName = "";
        
        String fieldName = StringUtils.replaceChars(fieldSubmitName, INVALID_METHOD_NAME_CHARS, "");
        String nameArr[] = StringUtils.split(fieldName, '_');
        for (String n : nameArr)
        {
            methodName += StringUtils.capitalize(n);            
        }
        
        return methodName;
    }
    
    /**
     * Generates the get/set methods for the specified dialog field.  If necessary, an enum will also be generated.
     * 
     * @param name of the field.
     * @param displayName the label displayed in the dialog for the field.
     * @param type the datatype of the field's value.
     * @param control the content editor control of the field.
     * @param enumValues list of enum values.
     * 
     * @return a java code snippet for the two methods.
     */
    private String generateMethods(String name, String displayName, String type, String control,
            List<EnumValue> enumValues)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        if (control == null || isSupportedControl(control))
        {
            String methodName = getMethodName(name);
                      
            String fieldName = StringUtils.replaceChars(name, INVALID_METHOD_NAME_CHARS, "");
            String variableName = StringUtils.remove(fieldName, '_').toLowerCase();
                     
            boolean isEnum = type.equalsIgnoreCase("enum");
            if (isEnum)
            {
                strBuffer.append(generateEnum(methodName, enumValues));
                strBuffer.append("\n\n");
            }
            
            String value = isEnum ? variableName + ".getLabel()" : variableName;
            String variableType = isEnum ? methodName : getVariableType(type, control);

            String controlParam = (control != null) ? "\"" + control + "\"" : "null";

            strBuffer.append("    /**\n");
            strBuffer.append("     * Get method for the field labeled '" + displayName + "'.\n");
            strBuffer.append("     */\n");
            strBuffer.append("    public " + variableType + " get" + methodName + "()\n");
            strBuffer.append("    {\n");
            
            if (isEnum)
            {
                strBuffer.append("        return " + variableType + ".getEnum((String) getField(\"" + name + "\", \""
                        + type + "\", " + controlParam + "));\n");
            }
            else
            {
                strBuffer.append("        return (" + variableType + ") getField(\"" + name + "\", \"" + type + "\", "
                        + controlParam + ");\n");
            }
            
            strBuffer.append("    }\n\n");

            strBuffer.append("    /**\n");
            strBuffer.append("     * Set method for the field labeled '" + displayName + "'.\n");
            strBuffer.append("     */\n");
            strBuffer.append("    public void set" + methodName + '(' + variableType + ' ' + variableName + ")\n");
            strBuffer.append("    {\n");
            strBuffer.append("        setField(\"" + name + "\", " + value + ", \"" + type + "\", "
                    + controlParam + ");\n");
            strBuffer.append("    }\n\n");
        }
              
        return strBuffer.toString();
    }
    
    private String generateCopyrightNote(String className)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        strBuffer.append("/******************************************************************************\n");
        strBuffer.append(" *\n");
        strBuffer.append(" * [ " + className + ".java ] NOTE: THIS CLASS WAS AUTOGENERATED.\n");
        strBuffer.append(" *\n");
        strBuffer.append(" * COPYRIGHT (c) 1999 - " + currentYear + " by Percussion Software, Inc., Woburn, MA USA.\n");
        strBuffer.append(" * All rights reserved. This material contains unpublished, copyrighted\n");
        strBuffer.append(" * work including confidential and proprietary information of Percussion.\n");
        strBuffer.append(" *\n");
        strBuffer.append(" ******************************************************************************/\n");
        strBuffer.append("\n");
        
        return strBuffer.toString();
    }
    
    /**
     * The class header includes the standard Percussion comment, as well as the package, import, and class
     * declarations, up to the opening '{'.
     * 
     * @param name of the class.
     * 
     * @return java code snippet for the header.
     */
    private String generateClassHeader(String name, String folderName)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        strBuffer.append(generateCopyrightNote(name));

        strBuffer.append("package com.percussion.qa.framework.widgets.dialogs." + folderName + ";\n");
        strBuffer.append("\n");
        strBuffer.append("import com.percussion.qa.framework.widgets.dialogs.WidgetDialog;\n");
        strBuffer.append("\n");
        strBuffer.append("public class " + name + " extends WidgetDialog\n");
        strBuffer.append("{\n");
           
        return strBuffer.toString();
    }
    
    private String getPlainWidgetName(PSWidgetDefinition widgetDef)
    {
        return StringUtils.deleteWhitespace(widgetDef.getWidgetPrefs().getTitle()).toLowerCase();
    }
    
    private String generateSmokeTestClassHeader(PSWidgetDialogGenerationInfo widgetGenerationInfo)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        PSWidgetDefinition widgetDef = widgetGenerationInfo.getWidgetDefinition();
        
        String className = getDialogClassName(widgetDef, TEST_SUFFIX);
        String plainWidgetName = getPlainWidgetName(widgetDef);
        
        String layoutDialogClassName = getDialogClassName(widgetDef, LAYOUT_DIALOG_SUFFIX);
        String contentDialogClassName = getDialogClassName(widgetDef, CONTENT_DIALOG_SUFFIX);
        String styleDialogClassName = getDialogClassName(widgetDef, STYLE_DIALOG_SUFFIX);
        
        strBuffer.append(generateCopyrightNote(className));
        
        strBuffer.append("package widgetsTestSuite." + plainWidgetName + "Widget;\n");
        strBuffer.append("\n");
        strBuffer.append("import com.percussion.qa.framework.WidgetTestCase;\n");
        strBuffer.append("import com.percussion.qa.framework.widgets.Widget;\n");
        
        if (!widgetGenerationInfo.isLayoutDialogGenerated() || !widgetGenerationInfo.isContentDialogGenerated() ||
                !widgetGenerationInfo.isStyleDialogGenerated())
            strBuffer.append("import com.percussion.qa.framework.widgets.dialogs.WidgetDialog;\n");
        
        if (widgetGenerationInfo.isLayoutDialogGenerated())
            strBuffer.append("import com.percussion.qa.framework.widgets.dialogs.layout." + layoutDialogClassName + ";\n");
        else
            layoutDialogClassName = NON_EXISTENT_DIALOG_CLASS;
        
        if (widgetGenerationInfo.isContentDialogGenerated())
            strBuffer.append("import com.percussion.qa.framework.widgets.dialogs.content." + contentDialogClassName + ";\n");
        else
            contentDialogClassName = NON_EXISTENT_DIALOG_CLASS;
        
        if (widgetGenerationInfo.isStyleDialogGenerated())
            strBuffer.append("import com.percussion.qa.framework.widgets.dialogs.style." + styleDialogClassName + ";\n");
        else
            styleDialogClassName = NON_EXISTENT_DIALOG_CLASS;
        
        strBuffer.append("\n");
        strBuffer.append("public class " + className + " extends WidgetTestCase<" + layoutDialogClassName + "," + contentDialogClassName + "," + styleDialogClassName + ">\n");
        strBuffer.append("{\n");
           
        return strBuffer.toString();
    }
    
    private String generateClassFooter()
    {
        return "}";
    }
    
    private void writeTestClassFile(PSWidgetDefinition widgetDef, String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return;
        }
        
        String className = getDialogClassName(widgetDef, TEST_SUFFIX);
        String plainWidgetName = getPlainWidgetName(widgetDef);
        
        String destDir = (StringUtils.isNotBlank(srcDir)) ? srcDir : DEFAULT_SRC_DIR;
        File widgetOutputDir = new File(destDir, WIDGET_DIALOGS_SMOKE_TESTS_SRC_PATH);
        
        String classDir = "widgetsTestSuite/" + plainWidgetName + "Widget";
        
        widgetOutputDir = new File(widgetOutputDir, classDir);
        
        writeJavaFile(widgetOutputDir, className, content);
    }
    
    /***
     * 
     * @param className
     * @param content
     * @return <code>true</code> if the file was written. <code>false</code> otherwise.
     */
    private boolean writeClassFile(String className, String content)
    {
        if (StringUtils.isEmpty(content))
        {
            return false;
        }
        
        String destDir = (StringUtils.isNotBlank(srcDir)) ? srcDir : DEFAULT_SRC_DIR;
        File widgetOutputDir = new File(destDir, WIDGET_DIALOGS_SRC_PATH);
        
        String classDir;
        if (className.endsWith(CONTENT_DIALOG_SUFFIX))
        {
            classDir = CONTENT_DIALOG_FOLDER_NAME;
        }
        else if (className.endsWith(LAYOUT_DIALOG_SUFFIX))
        {
            classDir = LAYOUT_DIALOG_FOLDER_NAME;
        }
        else
        {
            classDir = STYLE_DIALOG_FOLDER_NAME;
        }
        widgetOutputDir = new File(widgetOutputDir, classDir);
        
        writeJavaFile(widgetOutputDir, className, content);
        
        return true;
    }
    
    private void writeJavaFile(File destDir, String className, String fileContent)
    {
        if (!destDir.exists())
        {
            destDir.mkdirs();
        }
        
        File classFile = new File(destDir, className + ".java");
        
        FileWriter fWriter = null;
        try
        {
            fWriter = new FileWriter(classFile);
            fWriter.write(fileContent);           
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                if (fWriter != null)
                {
                    fWriter.close();
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private String getDialogClassName(PSWidgetDefinition widgetDef, String suffix)
    {
        return StringUtils.deleteWhitespace(widgetDef.getWidgetPrefs().getTitle()) + suffix;                
    }
    
    private String getVariableName(String name)
    {
        return StringUtils.deleteWhitespace(name.toLowerCase());
    }
    
    private String getVariableType(String type, String control)
    {
        return (type.equals("bool") || (control != null && control.equalsIgnoreCase(CHECKBOX_FIELD_CTRL))) ?
                "Boolean" : "String";
    }
    
    private String getLabel(PSField field, PSDisplayMapper mapper)
    {
        PSUISet uiSet = getUISet(field, mapper);
        
        return StringUtils.removeEnd(uiSet.getLabel().getText(), ":");
    }
    
    private String getControl(PSField field, PSDisplayMapper mapper)
    {
        PSUISet uiSet = getUISet(field, mapper);
        
        return uiSet.getControl().getName();
    }
    
    private PSUISet getUISet(PSField field, PSDisplayMapper mapper)
    {
        PSDisplayMapping mapping = mapper.getMapping(field.getSubmitName());
        
        return mapping.getUISet();
    }
    
    private boolean isSupportedControl(String control)
    {
        return ms_supportedCtrls.contains(control);
    }
    
    private String generateEnum(String name, List<EnumValue> prefValues)
    {
        StringBuffer strBuffer = new StringBuffer();
        
        strBuffer.append("    public enum " + name + "\n");
        strBuffer.append("    {\n");
        
        String values = "";
        for (EnumValue value : prefValues)
        {
            if (StringUtils.isNotEmpty(values))
            {
                values += ",\n";
            }
            
            String dispValue = value.getDisplayValue();
            String enumValue = StringUtils.replaceChars(dispValue, ' ', '_');
            enumValue = StringUtils.remove(enumValue, '\'');
            enumValue = StringUtils.remove(enumValue, '-');
            enumValue = StringUtils.remove(enumValue, '(');
            enumValue = StringUtils.remove(enumValue, ')');
                       
            values += "        " + enumValue + "(\"" + dispValue + "\")";
        }
        
        if (StringUtils.isNotEmpty(values))
        {
            values += ';';
        }
        
        strBuffer.append(values);
        strBuffer.append("\n\n");
        strBuffer.append("        private String label;\n");
        strBuffer.append("\n");
        strBuffer.append("        private " + name + "(String label)\n");
        strBuffer.append("        {\n");
        strBuffer.append("            this.label = label;\n");
        strBuffer.append("        }\n");
        strBuffer.append("\n");
        strBuffer.append("        public String getLabel()\n");
        strBuffer.append("        {\n");
        strBuffer.append("            return label;\n");
        strBuffer.append("        }\n");
        strBuffer.append("\n");
        strBuffer.append("        public static " + name + " getEnum(String label)\n");
        strBuffer.append("        {\n");
        strBuffer.append("            for (" + name + " v : values())\n");
        strBuffer.append("            {\n");
        strBuffer.append("                if (v.getLabel().equals(label))\n");
        strBuffer.append("                {\n");
        strBuffer.append("                    return v;\n");
        strBuffer.append("                }\n");
        strBuffer.append("            }\n");
        strBuffer.append("\n");
        strBuffer.append("            return null;\n");
        strBuffer.append("        }\n");
        strBuffer.append("    }");
          
        return strBuffer.toString();
    }
       
    /**
     * Loads the widget dialog generator properties.
     */
    private void loadProperties()
    {
        File propsFile = new File(PSServer.getRxConfigDir() + '/' + WIDGET_DIALOG_GEN_PROPS);
        
        try
        {
            PSProperties props = new PSProperties(propsFile.getAbsolutePath());
            srcDir = props.getProperty(SRC_DIR_PROP_NAME);
            widgetId = props.getProperty(WIDGET_ID_PROP_NAME);
        }
        catch (Exception e)
        {
            m_log.warn("Could not load properties file: " + e.getLocalizedMessage());                
        }
    }
    
    private static final Logger m_log = LogManager.getLogger(PSWidgetDialogGenerator.class);
    
    private static final String LAYOUT_DIALOG_SUFFIX = "WidgetLayoutDialog";
    private static final String STYLE_DIALOG_SUFFIX = "WidgetStyleDialog";
    private static final String CONTENT_DIALOG_SUFFIX = "WidgetContentDialog";
    private static final String TEST_SUFFIX = "WidgetSmokeTest";
    
    private static final String CONTENT_DIALOG_FOLDER_NAME = "content";
    private static final String LAYOUT_DIALOG_FOLDER_NAME = "layout";
    private static final String STYLE_DIALOG_FOLDER_NAME = "style";
    
    /**
     * This class is used to represent a dialog type that does not exist. For example,
     * for Blog List widget, this class is used when its Content dialog (which doesn't exist)
     * is required.
     */
    private static final String NON_EXISTENT_DIALOG_CLASS = "WidgetDialog";
    
    private static final String WIDGET_DIALOG_GEN_PROPS = "widgetDialogGenerator.properties";
    private static final String SRC_DIR_PROP_NAME = "srcDir";
    private static final String WIDGET_ID_PROP_NAME = "widgetId";
    private static final String DEFAULT_SRC_DIR = "C:\\widgetDialogFiles";
    
    private static final String WIDGET_DIALOGS_SRC_PATH = "/system/qaprojects/autotests/cubictests/src/main/java/com/percussion/qa/framework/widgets/dialogs";
    private static final String WIDGET_DIALOGS_SMOKE_TESTS_SRC_PATH = "/system/qaprojects/autotests/cubictests/src/test/java";
    
    private static final String CHECKBOX_FIELD_CTRL = "sys_SingleCheckBox";
    
    /**
     * String of characters not allowed in a method name.
     */
    private static final String INVALID_METHOD_NAME_CHARS = "~`!@#$%^&*()-+=[{]}|\\;:\'\",<.>/?";
        
    private static Set<String> ms_supportedCtrls = new HashSet<String>();
    
    static
    {
        ms_supportedCtrls.add("sys_EditBox");
        ms_supportedCtrls.add("sys_File");
        ms_supportedCtrls.add(CHECKBOX_FIELD_CTRL);
        ms_supportedCtrls.add("sys_tinymce");
        ms_supportedCtrls.add("sys_TextArea");
    }
    
    private IPSWidgetService widgetService;
    
    /**
     * Properties initialized in {@link #loadProperties()}.
     */
    private String srcDir;
    private String widgetId;
         
}
