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

package com.percussion.sitemanage.importer.utils;

import com.percussion.sitemanage.importer.PSLink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class LinkExtractorHarness {

	private static final String COMMA = ",";
	private static final String CR = "\n";
	private static final String CRLF = "\r\n";

	private static final Log log = LogFactory
			.getLog(LinkExtractorHarness.class);

	private LinkExtractorHarness() {
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(final String[] args) throws IOException {
		@SuppressWarnings("unchecked")
		final List<String> urls = FileUtils.readLines(new File(args[0]));
		final File f = new File(args[1]);
		final BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		final Iterator<String> urlIterator = urls.iterator();
		while (urlIterator.hasNext()) {
			try {
				final String site = urlIterator.next();
				System.out.print("Evaluating:" + site + CR);
				Document doc = Jsoup.connect(site)
					      .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
					      .referrer("http://www.google.com")
					      .get();
				final List<PSLink> links = PSLinkExtractor
						.getLinksForDocument(doc, null, null, site);
				final Iterator<PSLink> linkIterator = links.iterator();
				while (linkIterator.hasNext()) {
					final PSLink link = linkIterator.next();
					writer.write(link.getLinkText() + COMMA
							+ link.getLinkPath() + COMMA
							+ link.getAbsoluteLink() + COMMA
							+ link.getPageName() + CRLF);
				}
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}
			
		}
		writer.close();
	}
}
