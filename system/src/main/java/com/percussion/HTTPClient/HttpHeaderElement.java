/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.HTTPClient;


/**
 * This class holds a description of an http header element. It is used
 * by {@link Util#parseHeader(java.lang.String) Util.parseHeader}.
 *
 * @see Util#parseHeader(java.lang.String)
 * @see Util#getElement(java.util.Vector, java.lang.String)
 * @see Util#assembleHeader(java.util.Vector)
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
public class HttpHeaderElement
{
    /** element name */
    private String name;

    /** element value */
    private String value;

    /** element parameters */
    private NVPair[] parameters;


    // Constructors

    /**
     * Construct an element with the given name. The value and parameters
     * are set to null. This can be used when a dummy element is constructed
     * for comparison or retrieval purposes.
     *
     * @param name   the name of the element
     */
    public HttpHeaderElement(String name)
    {
	this.name  = name;
	this.value = null;
	parameters = new NVPair[0];
    }


    /**
     * @param name   the first token in the element
     * @param value  the value part, or null
     * @param params the parameters
     */
    public HttpHeaderElement(String name, String value, NVPair[] params)
    {
	this.name  = name;
	this.value = value;
	if (params != null)
	{
	    parameters = new NVPair[params.length];
	    System.arraycopy(params, 0, parameters, 0, params.length);
	}
	else
	    parameters = new NVPair[0];
    }


    // Methods

    /**
     * @return the name
     */
    public String getName()
    {
	return name;
    }


    /**
     * @return the value
     */
    public String getValue()
    {
	return value;
    }


    /**
     * @return the parameters
     */
    public NVPair[] getParams()
    {
	return parameters;
    }


    /**
     * Two elements are equal if they have the same name. The comparison is
     * <em>case-insensitive</em>.
     *
     * @param obj the object to compare with
     * @return true if <var>obj</var> is an HttpHeaderElement with the same
     *         name as this element.
     */
    public boolean equals(Object obj)
    {
	if ((obj != null) && (obj instanceof HttpHeaderElement))
	{
	    String other = ((HttpHeaderElement) obj).name;
	    return name.equalsIgnoreCase(other);
	}

	return false;
    }


    /**
     * @return a string containing the HttpHeaderElement formatted as it
     *         would appear in a header
     */
    public String toString()
    {
	StringBuilder buf = new StringBuilder();
	appendTo(buf);
	return buf.toString();
    }


    /**
     * Append this header element to the given buffer. This is basically a
     * more efficient version of <code>toString()</code> for assembling
     * multiple elements.
     *
     * @param buf the StringBuilder to append this header to
     * @see #toString()
     */
    public void appendTo(StringBuilder buf)
    {
	buf.append(name);

	if (value != null)
	{
	    if (Util.needsQuoting(value))
	    {
		buf.append("=\"");
		buf.append(Util.quoteString(value, "\\\""));
		buf.append('"');
	    }
	    else
	    {
		buf.append('=');
		buf.append(value);
	    }
	}

	for (int idx=0; idx<parameters.length; idx++)
	{
	    buf.append(";");
	    buf.append(parameters[idx].getName());
	    String pval = parameters[idx].getValue();
	    if (pval != null)
	    {
		if (Util.needsQuoting(pval))
		{
		    buf.append("=\"");
		    buf.append(Util.quoteString(pval, "\\\""));
		    buf.append('"');
		}
		else
		{
		    buf.append('=');
		    buf.append(pval);
		}
	    }
	}
    }
}
