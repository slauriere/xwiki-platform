/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.whatsnew.internal.xwikiblog;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;

/**
 * Clean the content of the RSS feed so that it's safe.
 * <p>
 * TODO: We should reuse some common code located elsewhere, such as HTMLRawBlockFilter currently located in Rendering
 * Transformation and which should be moved elsewhere.
 *
 * @version $Id$
 * @since 15.2RC1
 */
@Component(roles = RSSContentCleaner.class)
@Singleton
public class RSSContentCleaner
{
    /**
     * To clean the HTML content.
     */
    @Inject
    private HTMLCleaner htmlCleaner;

    /**
     * Cleans the content of an RSS item.
     *
     * @param source the source of the item
     * @return the cleaned and safe content as raw block
     */
    public String clean(String source)
    {
        HTMLCleanerConfiguration cleanerConfiguration = this.htmlCleaner.getDefaultConfiguration();
        Map<String, String> parameters = new HashMap<>(cleanerConfiguration.getParameters());

        // Just always use HTML 5 as this is what browsers parse.
        parameters.put(HTMLCleanerConfiguration.HTML_VERSION, "5");
        // Don't trust remote content.
        parameters.put(HTMLCleanerConfiguration.RESTRICTED, "true");

        cleanerConfiguration.setParameters(parameters);

        Document document = this.htmlCleaner.clean(new StringReader(source), cleanerConfiguration);

        // Remove the HTML envelope since this macro is only a fragment of a page which will already have an
        // HTML envelope when rendered. We remove it so that the HTML <head> tag isn't output.
        HTMLUtils.stripHTMLEnvelope(document);

        // Don't print the XML declaration nor the XHTML DocType.
        String cleanedContent = HTMLUtils.toString(document, true, true);
        // Don't print the top level html element (which is always present and at the same location
        // since it's been normalized by the HTML cleaner)
        // Note: we trim the first 7 characters since they correspond to a leading new line (generated by
        // XMLUtils.toString() since the doctype is printed on a line by itself followed by a new line) +
        // the 6 chars from "<html>".
        return  cleanedContent.substring(7, cleanedContent.length() - 8);
    }
}
