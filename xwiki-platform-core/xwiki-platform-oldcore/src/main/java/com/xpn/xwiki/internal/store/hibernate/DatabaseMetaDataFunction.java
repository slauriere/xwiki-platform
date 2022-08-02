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
package com.xpn.xwiki.internal.store.hibernate;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Function;

import org.hibernate.engine.spi.SessionImplementor;

/**
 * A functional interface used to manipulate a {@link DatabaseMetaData}.
 * 
 * @param <R> the type of the result of the function
 * @see Function
 * @version $Id$
 * @since 11.6RC1
 */
@FunctionalInterface
public interface DatabaseMetaDataFunction<R>
{
    /**
     * Applies this function to the given argument.
     *
     * @param metadata the metadata
     * @param session the Hibernate sessions
     * @return the function result
     * @throws SQLException when failing
     */
    R apply(DatabaseMetaData metadata, SessionImplementor session) throws SQLException;
}
