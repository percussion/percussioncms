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

package com.percussion.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Vector;

/**
 * The CommentedProperties class is an extension of java.util.Properties
 * to allow retention of comment lines and blank (whitespace only) lines
 * in the properties file.
 *
 * adapted from  garydeng https://www.dreamincode.net/forums/topic/53734-java-code-to-modify-properties-file-and-preserve-comments/
 *
 * Written for Java version 1.4
 */
public class PSProperties extends java.util.Properties {

    private static final Logger log = LogManager.getLogger(PSProperties.class);

    /**
     * Constant for the name of the config file containing name/value pairs for
     * any module we install.
     */
    private static final String FILE_PROPS = "init.properties";

    /**
     * Use a Vector to keep a copy of lines that are a comment or 'blank'
     */
    public Vector lineData = new Vector(0, 1);

    /**
     * Use a Vector to keep a copy of lines containing a key, i.e. they are a property.
     */
    public Vector keyData = new Vector(0, 1);

    /**
     * Construct a collection of properties by loading them from the
     * specified properties file.
     *
     * @param fileName the name of the file to load the
     * settings from
     *
     * @exception IOException if an i/o error occurs
     *
     * @exception FileNotFoundException if fileName is not found
     */
    public PSProperties(java.lang.String fileName)
            throws IOException, FileNotFoundException
    {
        super();
        FileInputStream fis = new FileInputStream(fileName);
        try
        {
            load(fis);
        }
        finally
        {
            fis.close();
        }
    }

    /**
     * Construct an empty collection of properties.
     */
    public PSProperties()
    {
        super();
    }

    /**
     * Convenience routine to get a property's value as an int.
     *
     * @param key the key to retrieve
     *
     * @return the property value as an integer
     *
     * @exception NumberFormatException if the value cannot be converted
     */
    public int getInt(String key)
            throws NumberFormatException
    {
        String value = (String)get(key);
        if (value == null)
            throw new NumberFormatException("null cannot be used as an int value");

        return (new Integer(value)).intValue();
    }

    /**
     * Convenience routine to get a property's value as an int. If the
     * property does not exist, the specified default value is returned
     * instead.
     *
     * @param key the key to retrieve
     *
     * @param defaultValue the default value to return
     *
     * @return the property value as an integer
     *
     * @exception NumberFormatException if the value cannot be converted
     */
    public int getInt(String key, int defaultValue)
            throws NumberFormatException
    {
        String value = (String)get(key);
        if (value == null)
            return defaultValue;

        return (new Integer(value)).intValue();
    }

    /**
     * Loads init.properties file from the system if there is one and searches
     * for the location of the properties. If there is no such a file
     * it loads the default properties file.
     *
     * @param entry a name of the entry that reperesents module's name/value
     * pair (for example, "designer_config_base_dir' is the name of workbench's pair)
     * @param property a name of the file that contains module's properties
     * (for example, 'designer.properties' for workbench, 'server.properties' for server)
     * @param dir a constant for the directory containing module's configs.
     * (for example, 'rxconfig/Workbench' for the workbench)
     *
     * @return file a valid file if no exception is thrown, otherwise
     * <CODE>null</NULL>.
     */
    public static File getConfig(String entry, String property, String dir)
    {
        File file = null;
        try
        {
            File propFile = new File(FILE_PROPS);

//if init file exists load it
            if(propFile.exists() && propFile.isFile())
            {
                PSProperties prop = new PSProperties(FILE_PROPS);
                String str = prop.getProperty(entry);
                file = new File(str, property);
            }
//if there is no init.properties file use the default
            else
            {
                file = new File(dir, property);
            }

        }
        catch(FileNotFoundException e)
        {
            log.error("Util", e);
        }
        catch(IOException e)
        {
            log.error("Util", e);
        }
        return file;
    }
    /**
     * Load properties from the specified InputStream.
     * Overload the load method in Properties so we can keep comment and blank lines.
     * @param   inStream   The InputStream to read.
     */
    public void load(InputStream inStream) throws IOException
    {
        // The spec says that the file must be encoded using ISO-8859-1.
        try(BufferedReader reader =
                new BufferedReader(new InputStreamReader(inStream, "ISO-8859-1"))) {

            String line;

            while ((line = reader.readLine()) != null) {
                char c = 0;
                int pos = 0;
                // Leading whitespaces must be deleted first.
                while (pos < line.length()
                        && Character.isWhitespace(c = line.charAt(pos))) {
                    pos++;
                }

                // If empty line or begins with a comment character, save this line
                // in lineData and save a "" in keyData.
                if ((line.length() - pos) == 0
                        || line.charAt(pos) == '#' || line.charAt(pos) == '!') {
                    lineData.add(line);
                    keyData.add("");
                    continue;
                }

                // The characters up to the next Whitespace, ':', or '='
                // describe the key.  But look for escape sequences.
                // Try to short-circuit when there is no escape char.
                int start = pos;
                boolean needsEscape = line.indexOf('\\', pos) != -1;
                StringBuilder key = needsEscape ? new StringBuilder() : null;

                while (pos < line.length()
                        && !Character.isWhitespace(c = line.charAt(pos++))
                        && c != '=' && c != ':') {
                    if (needsEscape && c == '\\') {
                        if (pos == line.length()) {
                            // The line continues on the next line.  If there
                            // is no next line, just treat it as a key with an
                            // empty value.
                            line = reader.readLine();
                            if (line == null)
                                line = "";
                            pos = 0;
                            while (pos < line.length()
                                    && Character.isWhitespace(c = line.charAt(pos)))
                                pos++;
                        } else {
                            c = line.charAt(pos++);
                            switch (c) {
                                case 'n':
                                    key.append('\n');
                                    break;
                                case 't':
                                    key.append('\t');
                                    break;
                                case 'r':
                                    key.append('\r');
                                    break;
                                case 'u':
                                    if (pos + 4 <= line.length()) {
                                        char uni = (char) Integer.parseInt
                                                (line.substring(pos, pos + 4), 16);
                                        key.append(uni);
                                        pos += 4;
                                    }   // else throw exception?
                                    break;
                                default:
                                    key.append(c);
                                    break;
                            }
                        }
                    } else if (needsEscape)
                        key.append(c);
                }

                boolean isDelim = (c == ':' || c == '=');

                String keyString;
                if (needsEscape)
                    keyString = key.toString();
                else if (isDelim || Character.isWhitespace(c))
                    keyString = line.substring(start, pos - 1);
                else
                    keyString = line.substring(start, pos);

                while (pos < line.length()
                        && Character.isWhitespace(c = line.charAt(pos)))
                    pos++;

                if (!isDelim && (c == ':' || c == '=')) {
                    pos++;
                    while (pos < line.length()
                            && Character.isWhitespace(c = line.charAt(pos)))
                        pos++;
                }

                // Short-circuit if no escape chars found.
                if (!needsEscape) {
                    put(keyString, line.substring(pos));
                    continue;
                }

                // Escape char found so iterate through the rest of the line.
                StringBuilder element = new StringBuilder(line.length() - pos);
                while (pos < line.length()) {
                    c = line.charAt(pos++);
                    if (c == '\\') {
                        if (pos == line.length()) {
                            // The line continues on the next line.
                            line = reader.readLine();

                            // We might have seen a backslash at the end of
                            // the file.  The JDK ignores the backslash in
                            // this case, so we follow for compatibility.
                            if (line == null)
                                break;

                            pos = 0;
                            while (pos < line.length()
                                    && Character.isWhitespace(c = line.charAt(pos)))
                                pos++;
                            element.ensureCapacity(line.length() - pos +
                                    element.length());
                        } else {
                            c = line.charAt(pos++);
                            switch (c) {
                                case 'n':
                                    element.append('\n');
                                    break;
                                case 't':
                                    element.append('\t');
                                    break;
                                case 'r':
                                    element.append('\r');
                                    break;
                                case 'u':
                                    if (pos + 4 <= line.length()) {
                                        char uni = (char) Integer.parseInt
                                                (line.substring(pos, pos + 4), 16);
                                        element.append(uni);
                                        pos += 4;
                                    }   // else throw exception?
                                    break;
                                default:
                                    element.append(c);
                                    break;
                            }
                        }
                    } else
                        element.append(c);
                }
                put(keyString, element.toString());
            }
        }
    }

    /**
     * Write the properties to the specified OutputStream.
     *
     * Overloads the store method in Properties so we can put back comment
     * and blank lines.
     *
     * @param out	The OutputStream to write to.
     * @param header Ignored, here for compatability w/ Properties.
     *
     * @exception IOException
     */
    public void store(OutputStream out, String header) throws IOException
    {
        // The spec says that the file must be encoded using ISO-8859-1.
        PrintWriter writer
                = new PrintWriter(new OutputStreamWriter(out, "ISO-8859-1"));

        // We ignore the header, because if we prepend a commented header
        // then read it back in it is now a comment, which will be saved
        // and then when we write again we would prepend Another header...

        String line;
        String key;
        StringBuilder s = new StringBuilder ();

        for (int i=0; i<lineData.size(); i++) {
            line = (String) lineData.get(i);
            key = (String) keyData.get(i);
            if((String) get(key)==null){
                continue;
            }
            if (key.length() > 0) {  // This is a 'property' line, so rebuild it
                formatForOutput (key, s, true);
                s.append ('=');
                formatForOutput ((String) get(key), s, false);
                writer.println (s);
            } else {  // was a blank or comment line, so just restore it
                writer.println (line);
            }
        }
        writer.flush ();
    }

    /**
     * Need this method from Properties because original code has StringBuilder,
     * which is an element of Java 1.5, used StringBuilder instead (because
     * this code was written for Java 1.4)
     *
     * @param str	- the string to format
     * @param buffer - buffer to hold the string
     * @param key	- true if str the key is formatted, false if the value is formatted
     */
    private void formatForOutput(String str, StringBuilder buffer, boolean key)
    {
        if (key) {
            buffer.setLength(0);
            buffer.ensureCapacity(str.length());
        } else
            buffer.ensureCapacity(buffer.length() + str.length());
        boolean head = true;
        int size = str.length();
        for (int i = 0; i < size; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\t':
                    buffer.append("\\t");
                    break;
                case ' ':
                    buffer.append(head ? "\\ " : " ");
                    break;
                case '\\':
                case '!':
                case '#':
                case '=':
                case ':':
                    buffer.append('\\').append(c);
                    break;
                default:
                    if (c < ' ' || c > '~') {
                        String hex = Integer.toHexString(c);
                        buffer.append("\\u0000".substring(0, 6 - hex.length()));
                        buffer.append(hex);
                    } else
                        buffer.append(c);
            }
            if (c != ' ')
                head = key;
        }
    }

    /**
     * Add a Property to the end of the CommentedProperties.
     *
     * @param   keyString	 The Property key.
     * @param   value		 The value of this Property.
     */
    @Override
    public synchronized Object put(Object keyString, Object value)
    {
        // add new entries at end but just update existing entries.
        if (!this.containsKey(keyString))
        {
            lineData.add("");
            keyData.add(keyString);
        }
        return super.put(keyString, value);
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        t.entrySet().forEach(e -> this.put(e.getKey(),e.getValue()));
    }

    /**
     * Add a comment or blank line or comment to the end of the CommentedProperties.
     *
     * @param   line The string to add to the end, make sure this is a comment
     *			   or a 'whitespace' line.
     */
    public void addLine(String line)
    {
        lineData.add(line);
        keyData.add("");
    }
}
