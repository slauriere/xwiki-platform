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
package org.xwiki.query.internal;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.query.Query;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.user.CurrentUserReference;
import org.xwiki.user.UserProperties;
import org.xwiki.user.UserPropertiesResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HiddenDocumentFilter}
 *
 * @version $Id$
 */
@ComponentTest
public class HiddenDocumentFilterTest
{
    @InjectMockComponents
    private HiddenDocumentFilter filter;

    @MockComponent
    private UserPropertiesResolver userPropertiesResolver;

    @BeforeComponent
    public void before()
    {
        UserProperties userProperties = mock(UserProperties.class);
        when(userProperties.displayHiddenDocuments()).thenReturn(false);
        when(this.userPropertiesResolver.resolve(CurrentUserReference.INSTANCE)).thenReturn(userProperties);
    }

    @Test
    void filterHQLStatementWithDoNotDisplayHiddenDocumentsInTheUserPreferences()
    {
        assertEquals(
            "select doc.fullName from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null) and (1=1)",
            filter.filterStatement("select doc.fullName from XWikiDocument doc where 1=1", Query.HQL));
    }

    @Test
    void filterHQLStatementWithDisplayHiddenDocumentsInTheUserPreferences()
    {
        // We need to do it that way since the expectation must be set in #configure() and the expectation sets the
        // displayHiddenDocuments property to true
        ReflectionUtils.setFieldValue(this.filter, "displayHiddenDocuments", true);

        // Insertions of distinct
        assertEquals("select doc.fullName from XWikiDocument doc where 1=1",
            filter.filterStatement("select doc.fullName from XWikiDocument doc where 1=1", Query.HQL));
    }

    @Test
    void filterIncorrectHQLStatement()
    {
        // Insertions of distinct
        assertEquals("select doc.fullName from XWikiDocument mydoc where 1=1",
            filter.filterStatement("select doc.fullName from XWikiDocument mydoc where 1=1", Query.HQL));
    }

    @Test
    void filterXWQLStatement()
    {
        assertEquals("select doc.fullName from XWikiDocument doc where 1=1",
            filter.filterStatement("select doc.fullName from XWikiDocument doc where 1=1", Query.XWQL));
    }

    @Test
    void filterHQLStatementWithWhereAndOrderBy()
    {
        // Insertions of distinct
        assertEquals("select doc.name from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null) and "
            + "(1=1) order by doc.name",
            filter.filterStatement("select doc.name from XWikiDocument doc where 1=1 order by doc.name",
                Query.HQL));
    }

    @Test
    void filterHQLStatementWithWhereAndGroupBy()
    {
        // Insertions of distinct
        assertEquals("select doc.name from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null) and "
            + "(1=1) group by doc.name",
            filter.filterStatement("select doc.name from XWikiDocument doc where 1=1 group by doc.name",
                Query.HQL));
    }

    @Test
    void filterHQLStatementWithWhereAndOrderByAndGroupBy()
    {
        // Insertions of distinct
        assertEquals("select doc.name from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null) and "
            + "(1=1) order by doc.name group by doc.name",
            filter.filterStatement("select doc.name from XWikiDocument doc where 1=1 order by doc.name group by "
                + "doc.name", Query.HQL));
    }

    @Test
    void filterHQLStatementWithoutWhere()
    {
        // Insertions of distinct
        assertEquals("select doc.name from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null)",
            filter.filterStatement("select doc.name from XWikiDocument doc", Query.HQL));
    }

    @Test
    void filterHQLStatementWithoutWhereWithOrderBy()
    {
        // Insertions of distinct
        assertEquals("select doc.name from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null) order by "
            + "doc.name asc",
            filter.filterStatement("select doc.name from XWikiDocument doc order by doc.name asc", Query.HQL));
    }

    @Test
    void filterHQLStatementWithoutWhereWithGroupBy()
    {
        // Insertions of distinct
        assertEquals(
            "select doc.web, doc.name from XWikiDocument doc where (doc.hidden <> true or doc.hidden is null) " +
                "group by doc.web",
            filter.filterStatement("select doc.web, doc.name from XWikiDocument doc group by doc.web", Query.HQL));
    }

    @Test
    void filterResults()
    {
        List list = mock(List.class);
        assertSame(list, filter.filterResults(list));
    }
}
