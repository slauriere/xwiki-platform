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
package org.xwiki.index.tree.internal.nestedpages;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.localization.LocalizationContext;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceProvider;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.properties.converter.Converter;
import org.xwiki.query.Query;
import org.xwiki.query.QueryFilter;
import org.xwiki.query.QueryManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.tree.TreeFilter;
import org.xwiki.user.CurrentUserReference;
import org.xwiki.user.UserProperties;
import org.xwiki.user.UserPropertiesResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WikiTreeNode}.
 * 
 * @version $Id$
 */
@ComponentTest
public class WikiTreeNodeTest
{
    @InjectMockComponents
    private WikiTreeNode wikiTreeNode;

    @MockComponent
    @Named("entityTreeNodeId")
    private Converter<EntityReference> entityTreeNodeIdConverter;

    @MockComponent
    @Named("local")
    private EntityReferenceSerializer<String> localEntityReferenceSerializer;

    @MockComponent
    private EntityReferenceProvider defaultEntityReferenceProvider;

    @MockComponent
    private LocalizationContext localizationContext;

    @MockComponent
    private UserPropertiesResolver userPropertiesResolver;

    @MockComponent
    @Named("topLevelPage/nestedPages")
    private QueryFilter topLevelPageFilter;

    @MockComponent
    @Named("hiddenPage/nestedPages")
    private QueryFilter hiddenPageFilter;

    @MockComponent
    @Named("excludedSpace/nestedPages")
    private QueryFilter excludedSpaceFilter;

    @MockComponent
    private QueryManager queryManager;

    @Mock
    private Query query;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @MockComponent
    @Named("test")
    private TreeFilter filter;

    @BeforeEach
    public void before(MockitoComponentManager componentManager)
    {
        when(this.defaultEntityReferenceProvider.getDefaultReference(EntityType.DOCUMENT))
            .thenReturn(new EntityReference("WebHome", EntityType.DOCUMENT));
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "wiki:foo"))
            .thenReturn(new WikiReference("foo"));

        DocumentReference alice = new DocumentReference("bar", "A", "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:bar:A.WebHome")).thenReturn(alice);
        when(this.localEntityReferenceSerializer.serialize(alice.getParent())).thenReturn("A");

        DocumentReference bob = new DocumentReference("foo", "B", "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:foo:B.WebHome")).thenReturn(bob);
        when(this.localEntityReferenceSerializer.serialize(bob.getParent())).thenReturn("B");

        when(this.entityTreeNodeIdConverter.convert(String.class, new DocumentReference("foo", "C", "WebHome")))
            .thenReturn("document:foo:C.WebHome");

        when(this.query.addFilter(any(QueryFilter.class))).thenReturn(this.query);

        when(this.contextComponentManagerProvider.get()).thenReturn(componentManager);
    }

    @Test
    public void getParent()
    {
        assertEquals("farm:*", this.wikiTreeNode.getParent("wiki:foo"));
    }

    @Test
    public void getChildCount() throws Exception
    {
        // Filter hidden child nodes.
        this.wikiTreeNode.getProperties().put("filterHiddenDocuments", true);
        UserProperties userProperties = mock(UserProperties.class);
        when(userProperties.displayHiddenDocuments()).thenReturn(false);
        when(this.userPropertiesResolver.resolve(CurrentUserReference.INSTANCE)).thenReturn(userProperties);

        assertEquals(0, this.wikiTreeNode.getChildCount("something"));
        assertEquals(0, this.wikiTreeNode.getChildCount("some:thing"));

        when(this.queryManager.createQuery("select count(*) from XWikiSpace where parent is null and hidden <> true",
            Query.HQL)).thenReturn(this.query);
        when(query.execute()).thenReturn(Collections.singletonList(2L));

        assertEquals(2L, this.wikiTreeNode.getChildCount("wiki:foo"));

        verify(this.query).setWiki("foo");
        verify(this.query, never()).bindValue(anyString(), any());
    }

    @Test
    public void getChildCountWithExclusions() throws Exception
    {
        this.wikiTreeNode.getProperties().put("exclusions", new HashSet<>(
            Arrays.asList("document:bar:A.WebHome", "document:foo:B.WebHome", "document:foo:C.D", "space:foo:C")));

        DocumentReference denis = new DocumentReference("foo", "C", "D");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:foo:C.D")).thenReturn(denis);

        SpaceReference carol = new SpaceReference("foo", "C");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "space:foo:C")).thenReturn(carol);
        when(this.localEntityReferenceSerializer.serialize(carol)).thenReturn("C");

        this.wikiTreeNode.getProperties().put("filters", Collections.singletonList("test"));
        when(this.entityTreeNodeIdConverter.convert(String.class, new WikiReference("foo"))).thenReturn("wiki:foo");
        when(this.filter.getChildExclusions("wiki:foo")).thenReturn(Collections.singleton("document:foo:J.WebHome"));

        DocumentReference john = new DocumentReference("foo", "J", "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:foo:J.WebHome")).thenReturn(john);
        when(this.localEntityReferenceSerializer.serialize(john.getLastSpaceReference())).thenReturn("J");

        when(this.queryManager.createQuery(
            "select count(*) from XWikiSpace where parent is null " + "and reference not in (:excludedSpaces)",
            Query.HQL)).thenReturn(this.query);
        when(query.execute()).thenReturn(Collections.singletonList(2L));

        assertEquals(2L, this.wikiTreeNode.getChildCount("wiki:foo"));

        verify(this.query).setWiki("foo");
        verify(this.query).bindValue("excludedSpaces", new HashSet<String>(Arrays.asList("B", "C", "J")));
    }

    @Test
    public void getChildren() throws Exception
    {
        // Filter hidden child nodes.
        this.wikiTreeNode.getProperties().put("filterHiddenDocuments", true);
        UserProperties userProperties = mock(UserProperties.class);
        when(userProperties.displayHiddenDocuments()).thenReturn(false);
        when(this.userPropertiesResolver.resolve(CurrentUserReference.INSTANCE)).thenReturn(userProperties);

        String statement = "select reference, 0 as terminal from XWikiSpace page order by lower(name), name";
        when(this.queryManager.createQuery(statement, Query.HQL)).thenReturn(this.query);
        when(query.execute()).thenReturn(Collections.singletonList(new DocumentReference("foo", "C", "WebHome")));

        assertEquals(Collections.singletonList("document:foo:C.WebHome"),
            this.wikiTreeNode.getChildren("wiki:foo", 5, 10));

        verify(this.query).setWiki("foo");
        verify(this.query).setOffset(5);
        verify(this.query).setLimit(10);
        verify(this.query).addFilter(this.topLevelPageFilter);
        verify(this.query).addFilter(this.hiddenPageFilter);
    }

    @Test
    public void getChildrenByTitle() throws Exception
    {
        // Don't filter hidden child nodes.
        this.wikiTreeNode.getProperties().put("filterHiddenDocuments", true);
        UserProperties userProperties = mock(UserProperties.class);
        when(userProperties.displayHiddenDocuments()).thenReturn(false);
        when(this.userPropertiesResolver.resolve(CurrentUserReference.INSTANCE)).thenReturn(userProperties);

        this.wikiTreeNode.getProperties().put("orderBy", "title");
        this.wikiTreeNode.getProperties().put("exclusions",
            new HashSet<>(Arrays.asList("document:bar:A.WebHome", "document:foo:B.WebHome")));

        when(this.queryManager.getNamedQuery("nonTerminalPagesOrderedByTitle")).thenReturn(this.query);
        when(this.localizationContext.getCurrentLocale()).thenReturn(Locale.FRENCH);
        when(query.execute()).thenReturn(Collections.singletonList(new DocumentReference("foo", "C", "WebHome")));

        assertEquals(Collections.singletonList("document:foo:C.WebHome"),
            this.wikiTreeNode.getChildren("wiki:foo", 5, 10));

        verify(this.query).setWiki("foo");
        verify(this.query).setOffset(5);
        verify(this.query).setLimit(10);
        verify(this.query).bindValue("locale", "fr");
        verify(this.query).bindValue("excludedSpaces", Collections.singleton("B"));
        verify(this.query).addFilter(this.excludedSpaceFilter);
    }
}
