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
package org.xwiki.platform.date.script;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ocpsoft.prettytime.PrettyTime;
import org.xwiki.component.annotation.Component;
import org.xwiki.localization.LocalizationContext;
import org.xwiki.script.service.ScriptService;

/**
 * Script service to display dates in a pretty way.
 *
 * @version $Id$
 * @since 10.5RC1
 * @since 9.11.5
 */
@Component
@Singleton
@Named("date")
public class DateScriptService implements ScriptService
{
    @Inject
    private LocalizationContext localizationContext;

    /**
     * Display a date relatively to the current date and using the locale of the current user.
     * @param dateToDisplay the date to display
     * @return a localized string displaying the date like "12 minutes ago"
     */
    public String displayTimeAgo(Date dateToDisplay)
    {
        return displayTimeAgo(dateToDisplay, localizationContext.getCurrentLocale());
    }

    /**
     * Display a date relatively to the current date and using the given locale.
     * @param dateToDisplay the date to display
     * @param locale the locale to use
     * @return a localized string displaying the date like "12 minutes ago"
     */
    public String displayTimeAgo(Date dateToDisplay, Locale locale)
    {
        PrettyTime p = new PrettyTime(locale);
        return p.format(dateToDisplay);
    }
}
