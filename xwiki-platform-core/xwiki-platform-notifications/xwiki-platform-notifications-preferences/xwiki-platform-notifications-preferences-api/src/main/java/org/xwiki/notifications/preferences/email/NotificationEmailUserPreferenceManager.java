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
package org.xwiki.notifications.preferences.email;

import org.xwiki.component.annotation.Role;
import org.xwiki.notifications.preferences.NotificationEmailInterval;
import org.xwiki.stability.Unstable;
import org.xwiki.user.UserReference;

/**
 * Configuration for the emails for each user.
 *
 * @version $Id$
 * @since 9.11RC1
 */
@Role
public interface NotificationEmailUserPreferenceManager
{
    /**
     * @return the diff type configured for the current user
     */
    NotificationEmailDiffType getDiffType();

    /**
     * @param userReference reference of a user
     * @return the diff type configured for the given user
     * @since 14.10
     */
    @Unstable
    default NotificationEmailDiffType getDiffType(UserReference userReference)
    {
        return getDiffType();
    }

    /**
     * @return the notification interval configured for the current user
     * @since 14.10
     */
    @Unstable
    default NotificationEmailInterval getInterval()
    {
        return NotificationEmailInterval.DAILY;
    }

    /**
     * @param userReference reference of a user
     * @return the notification interval configured for the given user
     * @since 14.10
     */
    @Unstable
    default NotificationEmailInterval getInterval(UserReference userReference)
    {
        return this.getInterval();
    }
}
