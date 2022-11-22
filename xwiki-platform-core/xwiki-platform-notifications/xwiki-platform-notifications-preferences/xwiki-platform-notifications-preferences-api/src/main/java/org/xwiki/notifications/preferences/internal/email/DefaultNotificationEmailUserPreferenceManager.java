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
package org.xwiki.notifications.preferences.internal.email;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.notifications.preferences.NotificationEmailInterval;
import org.xwiki.notifications.preferences.email.NotificationEmailDiffType;
import org.xwiki.notifications.preferences.email.NotificationEmailUserPreferenceManager;
import org.xwiki.text.StringUtils;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

/**
 * Default implementation for {@link NotificationEmailUserPreferenceManager}.
 *
 * @version $Id$
 * @since 9.11RC1
 */
@Component
@Singleton
public class DefaultNotificationEmailUserPreferenceManager implements NotificationEmailUserPreferenceManager
{
    private static final String WIKI_SPACE = "XWiki";

    private static final String NOTIFICATIONS = "Notifications";

    private static final String CODE = "Code";

    private static final LocalDocumentReference EMAIL_PREFERENCES_CLASS = new LocalDocumentReference(
        Arrays.asList(WIKI_SPACE, NOTIFICATIONS, CODE), "NotificationEmailPreferenceClass"
    );

    private static final LocalDocumentReference GLOBAL_PREFERENCES = new LocalDocumentReference(
        Arrays.asList(WIKI_SPACE, NOTIFICATIONS, CODE), "NotificationAdministration"
    );

    private static final String DIFF_TYPE = "diffType";

    private static final String INTERVAL = "interval";

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Inject
    private DocumentReferenceResolver<String> referenceResolver;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private Logger logger;

    @Override
    public NotificationEmailDiffType getDiffType()
    {
        return getDiffType(documentAccessBridge.getCurrentUserReference());
    }

    @Override
    public NotificationEmailDiffType getDiffType(DocumentReference userReference)
    {
        return getStaticListPropertyPreference(DIFF_TYPE, NotificationEmailDiffType.class,
            NotificationEmailDiffType.STANDARD, userReference);
    }

    @Override
    public NotificationEmailInterval getInterval()
    {
        return getInterval(documentAccessBridge.getCurrentUserReference());
    }

    @Override
    public NotificationEmailInterval getInterval(DocumentReference userReference)
    {
        return getStaticListPropertyPreference(INTERVAL, NotificationEmailInterval.class,
            NotificationEmailInterval.DAILY, userReference);
    }

    private <T extends Enum<T>> T getStaticListPropertyPreference(String propertyName,
        Class<T> propertyEnum, T propertyDefaultValue, DocumentReference user)
    {
        try {
            // Get the config of the user
            DocumentReference emailClassReference = new DocumentReference(EMAIL_PREFERENCES_CLASS,
                user.getWikiReference());
            Object value = documentAccessBridge.getProperty(user, emailClassReference, propertyName);
            if (value != null && StringUtils.isNotBlank((String) value)) {
                return Enum.valueOf(propertyEnum, ((String) value).toUpperCase());
            }

            // Get the config of the wiki
            DocumentReference xwikiPref =
                new DocumentReference(GLOBAL_PREFERENCES, user.getWikiReference());
            value = documentAccessBridge.getProperty(xwikiPref, emailClassReference, propertyName);
            if (value != null && StringUtils.isNotBlank((String) value)) {
                return Enum.valueOf(propertyEnum, ((String) value).toUpperCase());
            }

            // Get the config of the main wiki
            WikiReference mainWiki = new WikiReference(wikiDescriptorManager.getMainWikiId());
            if (!user.getWikiReference().equals(mainWiki)) {
                xwikiPref = new DocumentReference(GLOBAL_PREFERENCES, mainWiki);
                emailClassReference = new DocumentReference(EMAIL_PREFERENCES_CLASS, mainWiki);
                value = documentAccessBridge.getProperty(xwikiPref, emailClassReference, propertyName);
                if (value != null && StringUtils.isNotBlank((String) value)) {
                    return Enum.valueOf(propertyEnum, ((String) value).toUpperCase());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to get the email property [{}] for the user [{}].", propertyName, user, e);
        }

        // Fallback to the default value
        return propertyDefaultValue;
    }
}
