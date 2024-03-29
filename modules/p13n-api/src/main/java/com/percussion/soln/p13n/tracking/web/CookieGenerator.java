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

package com.percussion.soln.p13n.tracking.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class for cookie generation, carrying cookie descriptor settings
 * as bean properties and being able to add and remove cookie to/from a
 * given response.
 *
 * <p>Can serve as base class for components that generate specific cookies,
 * like CookieLocaleResolcer and CookieThemeResolver.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see #addCookie
 * @see #removeCookie
 */
public class CookieGenerator {

	/**
	 * Default path that cookies will be visible to: "/", i.e. the entire server.
	 */
	public static final String DEFAULT_COOKIE_PATH = "/";

	/**
	 * Default maximum age of cookies: maximum integer value, i.e. forever.
	 */
	public static final int DEFAULT_COOKIE_MAX_AGE = Integer.MAX_VALUE;


	protected final Log logger = LogFactory.getLog(getClass());

	private String cookieName;

	private String cookieDomain;

	private String cookiePath = DEFAULT_COOKIE_PATH;

	private int cookieMaxAge = DEFAULT_COOKIE_MAX_AGE;

	private boolean cookieSecure = false;


	/**
	 * Use the given name for cookies created by this generator.
	 * @param cookieName 
	 */
	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	/**
	 * Return the given name for cookies created by this generator.
	 * @return maybe <code>null</code>.
	 */
	public String getCookieName() {
		return cookieName;
	}

	/**
	 * Use the given domain for cookies created by this generator.
	 * The cookie is only visible to servers in this domain.
	 * @param cookieDomain maybe <code>null</code>.
	 */
	public void setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
	}

	/**
	 * Return the domain for cookies created by this generator, if any.
	 * @return maybe <code>null</code>.
	 */
	public String getCookieDomain() {
		return cookieDomain;
	}

	/**
	 * Use the given path for cookies created by this generator.
	 * The cookie is only visible to URLs in this path and below.
	 * @param cookiePath maybe <code>null</code>.
	 */
	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	/**
	 * Return the path for cookies created by this generator.
	 * @return maybe <code>null</code>.
	 */
	public String getCookiePath() {
		return cookiePath;
	}

	/**
	 * Use the given maximum age (in seconds) for cookies created by this generator.
	 * Useful special value: -1 ... not persistent, deleted when client shuts down
	 * @param cookieMaxAge
	 */
	public void setCookieMaxAge(int cookieMaxAge) {
		this.cookieMaxAge = cookieMaxAge;
	}

	/**
	 * Return the maximum age for cookies created by this generator.
	 * @return -1 not is special
	 */
	public int getCookieMaxAge() {
		return cookieMaxAge;
	}

	/**
	 * Set whether the cookie should only be sent using a secure protocol,
	 * such as HTTPS (SSL). This is an indication to the receiving browser,
	 * not processed by the HTTP server itself. Default is "false".
	 * @param cookieSecure Default is "false".
	 */
	public void setCookieSecure(boolean cookieSecure) {
		this.cookieSecure = cookieSecure;
	}

	/**
	 * Return whether the cookie should only be sent using a secure protocol,
	 * such as HTTPS (SSL).
	 * @return default is false.
	 */
	public boolean isCookieSecure() {
		return cookieSecure;
	}


	/**
	 * Add a cookie with the given value to the response,
	 * using the cookie descriptor settings of this generator.
	 * <p>Delegates to <code>createCookie</code> for cookie creation.
	 * @param response the HTTP response to add the cookie to
	 * @param cookieValue the value of the cookie to add
	 * @see #setCookieName
	 * @see #setCookieDomain
	 * @see #setCookiePath
	 * @see #setCookieMaxAge
	 * @see #createCookie
	 */
	public void addCookie(HttpServletResponse response, String cookieValue) {
		Cookie cookie = createCookie(cookieValue);
		cookie.setMaxAge(getCookieMaxAge());
		if (isCookieSecure()) {
			cookie.setSecure(true);
		}
		response.addCookie(cookie);
		if (logger.isDebugEnabled()) {
			logger.debug("Added cookie with name [" + getCookieName() + "] and value [" + cookieValue + "]");
		}
	}

	/**
	 * Remove the cookie that this generator describes from the response.
	 * Will generate a cookie with empty value and max age 0.
	 * <p>Delegates to <code>createCookie</code> for cookie creation.
	 * @param response the HTTP response to remove the cookie from
	 * @see #setCookieName
	 * @see #setCookieDomain
	 * @see #setCookiePath
	 * @see #createCookie
	 */
	public void removeCookie(HttpServletResponse response) {
		Cookie cookie = createCookie("");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		if (logger.isDebugEnabled()) {
			logger.debug("Removed cookie with name [" + getCookieName() + "]");
		}
	}

	/**
	 * Create a cookie with the given value, using the cookie descriptor
	 * settings of this generator (except for "cookieMaxAge").
	 * @param cookieValue the value of the cookie to crate
	 * @return the cookie
	 * @see #setCookieName
	 * @see #setCookieDomain
	 * @see #setCookiePath
	 */
	protected Cookie createCookie(String cookieValue) {
		Cookie cookie = new Cookie(getCookieName(), cookieValue);
		if (getCookieDomain() != null) {
			cookie.setDomain(getCookieDomain());
		}
		cookie.setPath(getCookiePath());
		return cookie;
	}

}
