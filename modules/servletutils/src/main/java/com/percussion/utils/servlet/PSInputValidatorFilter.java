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
package com.percussion.utils.servlet;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Logger;

/**
 * The input validator filter "intercepts" a pre-defined set of input parameters
 * and validates them, making sure they comply to their restriction type.
 * If a single parameter value or parameter name 
 * fails validation then a response will be sent to the client of status 422. This is to
 * help prevent spoofing the url for malicious purposes such as inserting HTML in the request parameters. 
 * <p>
 * The validator is enabled through the property {@link #VALIDATOR_ENABLE_PROP_NAME}.
 * <p>
 * The validator also has a property specifying a URL to a custom configuration resource called: 
 * {@link #VALIDATOR_CONFIG_RESOURCE_PROP_NAME}.
 * The format of the custom configuration resource is a property file whose property name
 * is the parameter name and the value is either a CSV of {@link RestrictType} or a quoted regex.
 * <p>
 * The configuration properties of enabling and location of configuration 
 * mentioned above are retrieved first from the following places (higher up takes precedence.)
 * <ol>
 * <li>System Properties</li>
 * <li>Filter Init Parameters in web.xml</li>
 * <li>Servlet Context init parameters from web.xml or context.xml (Tomcat).</li>
 * </ol> 
 * 
 * @author erikserating
 * @author wesleyhirsch
 * @author adamgent
 */
public class PSInputValidatorFilter implements Filter
{

    public static final int RESPONSE_ERROR_STATUS = 422;

    protected static final boolean VALIDATOR_ENABLE_DEFAULT_PARAM_VALUE = false;

    public static final String VALIDATOR_ENABLE_PROP_NAME = "PSInputValidatorFilter.enable";

    public static final String VALIDATOR_CONFIG_RESOURCE_PROP_NAME = "PSInputValidatorFilter.configResource";

   /**
     * log to use, never <code>null</code>.
     */


    static Logger log = Logger.getLogger(PSInputValidatorFilter.class.getName());


    /**
     * Map of params that need to be validated. The key is the parameter name
     * the value is the restriction type to validate against.
     */
    private static final Map<String, String[]> restrictionProps = new HashMap<>();
    
    /**
     * Restriction type enumeration. Defines types that a parameter should be
     * restricted to.
     */
    private enum RestrictType
    {
        // Only allow true, false, yes, or no
        BOOLEAN(Pattern.compile("yes|no|true|false", Pattern.CASE_INSENSITIVE)), 
        // Only allow valid guid/uuid values, so either 334 or 2-10-334
        GUID(Pattern.compile("^([-]?\\d+[-]{1}\\d+[-]{1}\\d+)|(\\d+)$")), 
        NOCONTROLCODE(Pattern.compile("[^\\x00\\x0a\\x0d]*")), // Do not allow control codes x00, x0a, or x0d
        NOLTGT(Pattern.compile("[^<>]*")), // Do not allow less than(<) or greater than (>)
        NOQUOTES(Pattern.compile("[^\"']*")), // Do not allow quotes or apostrophes
        // Only allow a numeric, i.e. 10 or 4.5
        NUMERIC(Pattern.compile("(((-|\\+)?[0-9]+(\\.[0-9]+)?)+|[.]?[0-9]+)")); 
        
        private Pattern pattern;

        RestrictType(Pattern p)
        {
            this.pattern = p;
        }

        public Pattern getPattern()
        {
            return this.pattern;
        }
    }

    private boolean isEnabled = false;

    public PSInputValidatorFilter()
    {
    }
    
    /**
     * 
     * @param request the original request, whose parameters may need
     * to be cleansed. Assumed not <code>null</code>.
     * 
     * @return the cleansed request if the cleansed feature is enabled;
     * otherwise it is the specified request, not <code>null</code>.
     */
    private boolean handleParamValidation(HttpServletRequest request, HttpServletResponse response)
    {
       ParamError badParam = this.validateRequest(request);
       if(badParam == null)
       {
          return true;
       }
       else
       {
          log.warning("IP: " + request.getRemoteAddr() + " Bad Parameter \"" + badParam.getParameterName() + 
                "\" with value: " + badParam.getParameterValue() + " isIllegalParameterName: " 
                + badParam.isIllegalParameterName());
          modifyResponse(response, badParam);
          return false;
       }      
    }

    private void modifyResponse(HttpServletResponse response,
          ParamError badParam)
    {
       try
       {
          String pname = badParam.isIllegalParameterName() ? "" : badParam.getParameterName();
          response.sendError(RESPONSE_ERROR_STATUS, "Unprocessable value for parameter \"" + pname + "\"");
       }
       catch (IOException e)
       {
          // We're already stopping the request, the error response is just a formality.
          // It doesn't matter if we can't print to a client.
          log.log(Level.SEVERE,e.getLocalizedMessage(), e);
       }
    }

    /**
     * Represents an error for a parameter name, value pair.
     * 
     * @author adamgent
     */
    private static class ParamError
    {
        private String parameterName;

        private String parameterValue;

        private boolean illegalParameterName;

        public ParamError(String parameterName, String parameterValue)
        {
            super();
            this.parameterName = parameterName;
            this.parameterValue = parameterValue;
        }

        public String getParameterName()
        {
            return parameterName;
        }

        public String getParameterValue()
        {
            return parameterValue;
        }

        public boolean isIllegalParameterName()
        {
            return illegalParameterName;
        }

        public void setIllegalParameterName(boolean badParameterName)
        {
            this.illegalParameterName = badParameterName;
        }
    }
    /**
     * Validates and cleanses the 'known' request input parameters. Invalid
     * input parameter values will be stripped and a warning message will be
     * logged.
     * 
     * @param request assumed not <code>null</code>.
     * @return the first found error<code>null</code>.
     */
    public ParamError validateRequest(HttpServletRequest request)
    {
        Map<?,?> originalParams = request.getParameterMap();
        for (Object key : originalParams.keySet())
        {
           String k = (String) key;
            String[] val = (String[]) originalParams.get(key);
            if (! isParamNameValid(k) ) {
               ParamError e = new ParamError(k, val[0]);
               e.setIllegalParameterName(true);
               return e;
            }
            if (restrictionProps.containsKey(k))
            {
               ParamError e = validateParameter(k, val[0]);
               if (e != null) return e;
            }

        }
        return null;
    }    
   
    private void doLoadCustomProps(String propsFilePath)
    {
        // Load custom restrictions file
        if (isBlank(propsFilePath))
        {
            log.log(Level.CONFIG,"Custom properties file not specified.");
            return;
        }
        try
        {
            URL url = new URL(propsFilePath);
            log.info("Loading custom properties: " + url.toString());
            InputStream is = url.openStream();
            this.doLoadProperties(is); // It will be closed by loadProperties.
        }
        catch (IOException e)
        {
            String message = "Properties file could not be found for: " + propsFilePath;
            log.log(Level.SEVERE,message, e);
        }

    }
    /**
     * Load the properties for the input filter that contains field restrictions. Properties will overwrite
     * any existing properties at the time of this call.
     * @param props the inputstream to the properties file, there is
     * no need to close the stream as this method will handle that. Cannot be <code>null</code>.
     */
    private void doLoadProperties(InputStream is)
    {
       notNull(is, "Input stream cannot be null.");
        try
        {
            Properties props = new Properties();
            props.load(is);
            for (Object key : props.keySet())
            {
                String[] vals = parsePropertyValues((String) props.get(key));
                if(vals.length > 0)
                   restrictionProps.put((String) key, vals);
            }
        }
        catch (Exception e)
        {
            log.log(Level.SEVERE,"Error loading properties", e);
        }
        finally
        {
            close(is);
        }
    }

   private void close(InputStream is)
   {
      if (is != null)
      {
          try
          {
              is.close();
          }
          catch (IOException ignore)
          {
          }
      }
   }
    
    /**
     * Helper method to parse property values.
     * @param rawVal may be <code>null</code> or empty.
     * @return split and trimmed values, never <code>null</code>, may
     * be empty.
     */
    private static String[] parsePropertyValues(String rawVal)
    {
        String[] results = splitByComma(rawVal);
        for (int i = 0; i < results.length; i++)
        {
            results[i] = results[i].trim();
        }
        return results;
    }

    /**
     * Helper method to split a string by a comma delimiter, except
     * for commas inside of quotes.
     * @param val may be <code>null</code> or empty.
     * @return string array of original string split be commas, never <code>null</code> may be
     * empty.
     */
    private static String[] splitByComma(String val)
    {
        if(val == null)
            return new String[]{};
        List<String> list = new ArrayList<>();
        StringReader sr = new StringReader(val);
        int current = -1;
        StringBuilder buff = new StringBuilder();
        try
        {
            boolean inQuotes = false;
            while ((current = sr.read()) != -1)
            {
                char c = (char) current;
                if (c == ',' && !inQuotes)
                {
                    if (buff.length() > 0)
                        list.add(buff.toString());
                    buff.setLength(0);
                    continue;
                }
                else if (c == '"')
                {
                    inQuotes = !inQuotes;
                }
                buff.append(c);

            }
            if (buff.length() > 0)
                list.add(buff.toString());
        }
        catch (IOException e)
        {
             log.log(Level.SEVERE,e.getLocalizedMessage(), e); // Highly doubtful this would ever get hit for a string reader
        }
        return list.toArray(new String[]
        {});

    }
    /**
     * Runs all restriction checks on the property value to determine if it is considered a
     * valid input value.
     * @param key param name, assumed not <code>null</code>.
     * @param value may be <code>null</code> or empty.
     * @return <code>null</code> if valid or parameter error if invalid.
     */
    private ParamError validateParameter(String key, String value)
    {

        if (restrictionProps.containsKey(key))
        {
            String[] restrictions = restrictionProps.get(key);
            for (String restrict : restrictions)
            {
                Pattern pattern = null;
                boolean hasRestrictType = false;
                //Does restrict type exist in the enum?
                for(RestrictType t : RestrictType.values())
                {
                   if(t.toString().equals(restrict))
                   {
                       hasRestrictType = true;
                       break;
                   }
                }
                
                if (hasRestrictType)
                {
                    RestrictType type = RestrictType.valueOf(restrict);
                    pattern = type.getPattern();
                    if (pattern == null)
                    {
                        String err = "Missing regex pattern for restriction type: " + restrict;
                        log.log(Level.SEVERE,err);
                        throw new RuntimeException(err);
                    }
                }
                else
                {
                    // check for enclosing quotes to be sure this is regex
                    if (restrict.startsWith("\"") && restrict.endsWith("\""))
                    {
                        String ptn = "";
                        try
                        {
                            //Strip enclosing quotes
                            ptn = restrict.substring(1, restrict.length() - 1);
                            pattern = Pattern.compile(ptn);
                        }
                        catch (PatternSyntaxException e)
                        {
                            log.log(Level.SEVERE,"Invalid Regular Expression, skipping restriction: " + ptn);
                            continue;
                        }
                    }
                    else
                    {
                       log.log(Level.SEVERE,"Expression not properly enclosed in quotes. Skipping.");
                       continue;
                    }
                }
                Matcher m = pattern.matcher(value);
                if (!m.matches())
                    return new ParamError(key, value);

            }
        }
        return null;
    }
    
    /**
     * Helper method to validate parameter name.
     * 
     * @param name assumed not <code>null</code>.
     * @return <code>true</code> if the name is valid.
     */
    private boolean isParamNameValid(String name)
    {
      Pattern[] pats = new Pattern[]
      {RestrictType.NOLTGT.getPattern(), RestrictType.NOQUOTES.getPattern(),
            RestrictType.NOQUOTES.getPattern()};
        for (Pattern p : pats)
        {
            Matcher m = p.matcher(name);
            if (!m.matches())
                return false;
        }
        return true;
    }    
    
   public void destroy()
   {
      
   }

   public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain filterChain) throws IOException, ServletException
   {
      // as long as this is an HTTP request, filter the request (we don't
      // wrap the response object)
      if (isEnabled && request instanceof HttpServletRequest)
      {
         HttpServletRequest httpReq = (HttpServletRequest) request;
         HttpServletResponse httpResp = (HttpServletResponse) response;
         
         // If this returns false, then we've already errored out the response.
         // We specifically don't want it to go further down the chain, so we do nothing.
         if(handleParamValidation(httpReq, httpResp))
            filterChain.doFilter(request, response);
      }
      else
      {
         filterChain.doFilter(request, response);
      }
   }

    /**
     * The config properties are retrieved in the following order
     * <ol>
     * <li>System Properties</li>
     * <li>Filter Init Parameters in web.xml</li>
     * <li>Servlet Context init parameters from web.xml or context.xml (Tomcat).
     * </li>
     * <li>default value</li>
     * </ol>
     * 
     * @param filterConfig not <code>null</code>
     * @param propName not <code>null</code>
     * @param defaultValue maybe <code>null</code>
     * @return maybe <code>null</code>
     */
    private String getProperty(FilterConfig filterConfig, String propName, String defaultValue)
    {
        String property = System.getProperty(propName, null);
        if (property == null)
            property = filterConfig.getInitParameter(propName);
        if (property == null)
            property = filterConfig.getServletContext().getInitParameter(propName);
        if (property == null)
            property = defaultValue;
        return property;
    }
   
    public void init(FilterConfig filterConfig) throws ServletException
    {
        String propsFilePath = getProperty(filterConfig, VALIDATOR_CONFIG_RESOURCE_PROP_NAME, null);
        String isEnabledString = getProperty(filterConfig, VALIDATOR_ENABLE_PROP_NAME, ""
                + VALIDATOR_ENABLE_DEFAULT_PARAM_VALUE);

        if ("true".equals(isEnabledString))
            isEnabled = true;
        InputStream is = this.getClass().getResourceAsStream(getClass().getSimpleName() + ".properties");
        notNull(is, "properties file should not be missing");
        doLoadProperties(is);
        doLoadCustomProps(propsFilePath);
        if (isEnabled)
            log.info("Request Validation is enabled");
    }

}
