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
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.tree.TreeFilter;
import org.xwiki.tree.TreeNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DocumentTreeNode}.
 * 
 * @version $Id$
 * @since 9.11RC1
 */
@ComponentTest
public class DocumentTreeNodeTest
{
    @InjectMockComponents
    private DocumentTreeNode documentTreeNode;

    @MockComponent
    private EntityReferenceSerializer<String> defaultEntityReferenceSerializer;

    @MockComponent
    @Named("local")
    private EntityReferenceSerializer<String> localEntityReferenceSerializer;

    @MockComponent
    private EntityReferenceProvider defaultEntityReferenceProvider;

    @MockComponent
    private ContextualAuthorizationManager authorization;

    @MockComponent
    private LocalizationContext localizationContext;

    @MockComponent
    @Named("childPage/nestedPages")
    private QueryFilter childPageFilter;

    @MockComponent
    @Named("documentReferenceResolver/nestedPages")
    private QueryFilter documentReferenceResolverFilter;

    @MockComponent
    private QueryManager queryManager;

    @MockComponent
    @Named("translations")
    private TreeNode translationsTreeNode;

    @MockComponent
    @Named("attachments")
    private TreeNode attachmentsTreeNode;

    @MockComponent
    @Named("classProperties")
    private TreeNode classPropertiesTreeNode;

    @MockComponent
    @Named("objects")
    private TreeNode objectsTreeNode;

    @Mock
    @Named("nestedPagesOrderedByName")
    private Query nestedPagesOrderedByName;

    @MockComponent
    @Named("entityTreeNodeId")
    private Converter<EntityReference> entityTreeNodeIdConverter;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @MockComponent
    @Named("test")
    private TreeFilter filter;

    private DocumentReference documentReference =
        new DocumentReference("wiki", Arrays.asList("Path", "To", "Page"), "WebHome");

    private DocumentReference terminalDocumentReference = new DocumentReference("wiki", "Some", "Page");

    @BeforeComponent
    public void configure(MockitoComponentManager componentManager) throws Exception
    {
        when(this.contextComponentManagerProvider.get()).thenReturn(componentManager);
    }

    @BeforeEach
    public void before() throws Exception
    {
        when(this.defaultEntityReferenceProvider.getDefaultReference(EntityType.DOCUMENT))
            .thenReturn(new EntityReference("WebHome", EntityType.DOCUMENT));

        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.WebHome"))
            .thenReturn(this.documentReference);
        when(this.entityTreeNodeIdConverter.convert(String.class, this.documentReference.getParent()))
            .thenReturn("space:wiki:Path.To.Page");
        when(this.defaultEntityReferenceSerializer.serialize(this.documentReference))
            .thenReturn("wiki:Path.To.Page.WebHome");
        when(this.localEntityReferenceSerializer.serialize(this.documentReference.getParent()))
            .thenReturn("Path.To.Page");

        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Some.Page"))
            .thenReturn(this.terminalDocumentReference);
        when(this.entityTreeNodeIdConverter.convert(String.class, this.terminalDocumentReference.getParent()))
            .thenReturn("space:wiki:Some");
        when(this.defaultEntityReferenceSerializer.serialize(this.terminalDocumentReference))
            .thenReturn("wiki:Some.Page");

        when(this.queryManager.getNamedQuery("nestedPagesOrderedByName")).thenReturn(this.nestedPagesOrderedByName);
        when(this.nestedPagesOrderedByName.addFilter(any(QueryFilter.class))).thenReturn(this.nestedPagesOrderedByName);
    }

    /**
     * @see "XWIKI-14643: Missing page in breadcrumbs treeview when treeview is expanded"
     */
    @Test
    public void pagination() throws Exception
    {
        this.documentTreeNode.getProperties().put("hierarchyMode", "reference");
        this.documentTreeNode.getProperties().put("showTranslations", true);
        this.documentTreeNode.getProperties().put("showAttachments", true);
        this.documentTreeNode.getProperties().put("showClassProperties", true);
        this.documentTreeNode.getProperties().put("showObjects", true);
        this.documentTreeNode.getProperties().put("showAddDocument", true);

        when(this.authorization.hasAccess(Right.EDIT, documentReference.getParent())).thenReturn(true);
        when(this.translationsTreeNode.getChildCount("translations:wiki:Path.To.Page.WebHome")).thenReturn(1);
        when(this.attachmentsTreeNode.getChildCount("attachments:wiki:Path.To.Page.WebHome")).thenReturn(1);
        when(this.classPropertiesTreeNode.getChildCount("classProperties:wiki:Path.To.Page.WebHome")).thenReturn(1);
        when(this.objectsTreeNode.getChildCount("objects:wiki:Path.To.Page.WebHome")).thenReturn(1);

        assertEquals(
            Arrays.asList("translations:wiki:Path.To.Page.WebHome", "attachments:wiki:Path.To.Page.WebHome",
                "classProperties:wiki:Path.To.Page.WebHome"),
            this.documentTreeNode.getChildren("document:wiki:Path.To.Page.WebHome", 0, 3));

        verify(this.nestedPagesOrderedByName, never()).execute();

        DocumentReference alice = new DocumentReference("wiki", Arrays.asList("Path.To.Page"), "Alice");
        when(this.nestedPagesOrderedByName.execute()).thenReturn(Collections.singletonList(alice));
        when(this.entityTreeNodeIdConverter.convert(String.class, alice))
            .thenReturn("document:wiki:Path.To.Page.Alice");

        assertEquals(
            Arrays.asList("objects:wiki:Path.To.Page.WebHome", "addDocument:wiki:Path.To.Page.WebHome",
                "document:wiki:Path.To.Page.Alice"),
            this.documentTreeNode.getChildren("document:wiki:Path.To.Page.WebHome", 3, 3));

        verify(this.nestedPagesOrderedByName).setOffset(0);
        verify(this.nestedPagesOrderedByName).setLimit(1);

        DocumentReference bob = new DocumentReference("wiki", Arrays.asList("Path.To.Page"), "Bob");
        DocumentReference carol = new DocumentReference("wiki", Arrays.asList("Path.To.Page"), "Carol");
        when(this.nestedPagesOrderedByName.execute()).thenReturn(Arrays.asList(bob, carol));
        when(this.entityTreeNodeIdConverter.convert(String.class, bob)).thenReturn("document:wiki:Path.To.Page.Bob");
        when(this.entityTreeNodeIdConverter.convert(String.class, carol))
            .thenReturn("document:wiki:Path.To.Page.Carol");

        assertEquals(Arrays.asList("document:wiki:Path.To.Page.Bob", "document:wiki:Path.To.Page.Carol"),
            this.documentTreeNode.getChildren("document:wiki:Path.To.Page.WebHome", 6, 3));

        verify(this.nestedPagesOrderedByName).setOffset(1);
        verify(this.nestedPagesOrderedByName).setLimit(3);
    }

    @Test
    public void getChildDocuments() throws Exception
    {
        assertEquals(Collections.emptyList(),
            this.documentTreeNode.getChildDocuments(terminalDocumentReference, 0, 10));

        this.documentTreeNode.getProperties().put("showTerminalDocuments", false);
        Query queryNonTerminalPagesByName = mock(Query.class, "nonTerminalPagesOrderedByTitle");
        String statement = "select reference, 0 as terminal from XWikiSpace page order by lower(name), name";
        when(this.queryManager.createQuery(statement, Query.HQL)).thenReturn(queryNonTerminalPagesByName);
        when(queryNonTerminalPagesByName.addFilter(this.documentReferenceResolverFilter))
            .thenReturn(queryNonTerminalPagesByName);
        DocumentReference childReference = new DocumentReference("wiki", Arrays.asList("Path.To.Page"), "Alice");
        when(queryNonTerminalPagesByName.execute()).thenReturn(Collections.singletonList(childReference));

        assertEquals(Collections.singletonList(childReference),
            this.documentTreeNode.getChildDocuments(documentReference, 5, 3));

        verify(queryNonTerminalPagesByName).setWiki("wiki");
        verify(queryNonTerminalPagesByName).setOffset(5);
        verify(queryNonTerminalPagesByName).setLimit(3);
        verify(queryNonTerminalPagesByName).addFilter(this.childPageFilter);
        verify(queryNonTerminalPagesByName).bindValue("parent", "Path.To.Page");

        this.documentTreeNode.getProperties().put("orderBy", "title");
        Query queryNonTerminalPagesByTitle = mock(Query.class, "nonTerminalPagesOrderedByTitle");
        when(this.queryManager.getNamedQuery("nonTerminalPagesOrderedByTitle"))
            .thenReturn(queryNonTerminalPagesByTitle);
        childReference = new DocumentReference("wiki", Arrays.asList("Path.To.Page"), "Bob");
        when(queryNonTerminalPagesByTitle.addFilter(this.documentReferenceResolverFilter))
            .thenReturn(queryNonTerminalPagesByTitle);
        when(queryNonTerminalPagesByTitle.execute()).thenReturn(Collections.singletonList(childReference));
        when(this.localizationContext.getCurrentLocale()).thenReturn(Locale.GERMAN);

        assertEquals(Collections.singletonList(childReference),
            this.documentTreeNode.getChildDocuments(documentReference, 0, 5));

        verify(queryNonTerminalPagesByTitle).bindValue("locale", "de");
    }

    @Test
    public void getChildrenByNameWithExclusions() throws Exception
    {
        this.documentTreeNode.getProperties().put("exclusions",
            new HashSet<>(Arrays.asList("document:wiki:Path.To.OtherPage", "document:wiki:Path.To.Page.Alice",
                "document:wiki:Path.WebHome", "document:wiki:Path.To.Page.Bob.WebHome")));

        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.OtherPage"))
            .thenReturn(new DocumentReference("wiki", Arrays.asList("Path", "To"), "OtherPage"));
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.WebHome"))
            .thenReturn(new DocumentReference("wiki", "Path", "WebHome"));

        DocumentReference alice = new DocumentReference("Alice", this.documentReference.getLastSpaceReference());
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.Alice"))
            .thenReturn(alice);
        when(this.localEntityReferenceSerializer.serialize(alice)).thenReturn("Path.To.Page.Alice");

        DocumentReference bob = new DocumentReference("wiki", Arrays.asList("Path", "To", "Page", "Bob"), "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.Bob.WebHome"))
            .thenReturn(bob);
        when(this.localEntityReferenceSerializer.serialize(bob.getParent())).thenReturn("Path.To.Page.Bob");

        this.documentTreeNode.getProperties().put("filters", Collections.singletonList("test"));
        when(this.entityTreeNodeIdConverter.convert(String.class, alice.getParent()))
            .thenReturn("space:wiki:Path.To.Page");
        when(this.filter.getChildExclusions("space:wiki:Path.To.Page")).thenReturn(new HashSet<>(
            Arrays.asList("document:wiki:Path.To.Page.John.WebHome", "document:wiki:Path.To.Page.Oliver")));

        DocumentReference john = new DocumentReference("wiki", Arrays.asList("Path", "To", "Page", "John"), "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.John.WebHome"))
            .thenReturn(john);
        when(this.localEntityReferenceSerializer.serialize(john.getLastSpaceReference()))
            .thenReturn("Path.To.Page.John");

        DocumentReference oliver = new DocumentReference("wiki", Arrays.asList("Path", "To", "Page"), "Oliver");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.Oliver"))
            .thenReturn(oliver);
        when(this.localEntityReferenceSerializer.serialize(oliver)).thenReturn("Path.To.Page.Oliver");

        DocumentReference child = new DocumentReference("Child", this.documentReference.getLastSpaceReference());
        when(this.nestedPagesOrderedByName.execute()).thenReturn(Collections.singletonList(child));
        when(this.entityTreeNodeIdConverter.convert(String.class, child))
            .thenReturn("document:wiki:Path.To.Page.Child");

        assertEquals(Collections.singletonList("document:wiki:Path.To.Page.Child"),
            this.documentTreeNode.getChildren("document:wiki:Path.To.Page.WebHome", 0, 5));

        verify(this.nestedPagesOrderedByName).bindValue("excludedDocuments",
            new HashSet<>(Arrays.asList("Path.To.Page.Alice", "Path.To.Page.Oliver")));
        verify(this.nestedPagesOrderedByName).bindValue("excludedSpaces",
            new HashSet<>(Arrays.asList("Path.To.Page.Bob", "Path.To.Page.John")));
    }

    @Test
    public void getChildCount() throws Exception
    {
        this.documentTreeNode.getProperties().put("exclusions",
            new HashSet<>(Arrays.asList("document:wiki:Path.To.Page.Alice", "document:wiki:Path.To.Page.Bob.WebHome")));

        DocumentReference alice = new DocumentReference("Alice", this.documentReference.getLastSpaceReference());
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.Alice"))
            .thenReturn(alice);
        when(this.localEntityReferenceSerializer.serialize(alice)).thenReturn("Path.To.Page.Alice");

        DocumentReference bob = new DocumentReference("wiki", Arrays.asList("Path", "To", "Page", "Bob"), "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page.Bob.WebHome"))
            .thenReturn(bob);
        when(this.localEntityReferenceSerializer.serialize(bob.getParent())).thenReturn("Path.To.Page.Bob");

        Query childSpacesQuery = mock(Query.class, "childSpaces");
        when(this.queryManager.createQuery(
            "select count(*) from XWikiSpace where parent = :parent " + "and reference not in (:excludedSpaces)",
            Query.HQL)).thenReturn(childSpacesQuery);
        when(childSpacesQuery.execute()).thenReturn(Collections.singletonList(2L));

        Query childTerminalPagesQuery = mock(Query.class, "childTerminalPages");
        when(this.queryManager.createQuery("where doc.translation = 0 and doc.space = :space and "
            + "doc.name <> :defaultDocName and doc.fullName not in (:excludedDocuments)", Query.HQL))
                .thenReturn(childTerminalPagesQuery);
        when(childTerminalPagesQuery.execute()).thenReturn(Collections.singletonList(3L));

        assertEquals(5L, this.documentTreeNode.getChildCount("document:wiki:Path.To.Page.WebHome"));

        verify(childSpacesQuery).setWiki("wiki");
        verify(childSpacesQuery).bindValue("parent", "Path.To.Page");
        verify(childSpacesQuery).bindValue("excludedSpaces", Collections.singleton("Path.To.Page.Bob"));

        verify(childTerminalPagesQuery).setWiki("wiki");
        verify(childTerminalPagesQuery).bindValue("space", "Path.To.Page");
        verify(childTerminalPagesQuery).bindValue("defaultDocName", "WebHome");
        verify(childTerminalPagesQuery).bindValue("excludedDocuments", Collections.singleton("Path.To.Page.Alice"));

        this.documentTreeNode.getProperties().put("showTerminalDocuments", false);

        assertEquals(2L, this.documentTreeNode.getChildCount("document:wiki:Path.To.Page.WebHome"));
    }

    @Test
    public void getPseudoChildCount()
    {
        when(this.translationsTreeNode.getChildCount("translations:wiki:Some.Page")).thenReturn(2);
        when(this.attachmentsTreeNode.getChildCount("attachments:wiki:Some.Page")).thenReturn(1);
        when(this.classPropertiesTreeNode.getChildCount("classProperties:wiki:Some.Page")).thenReturn(3);
        when(this.objectsTreeNode.getChildCount("objects:wiki:Some.Page")).thenReturn(5);

        assertEquals(0L, this.documentTreeNode.getChildCount("document:wiki:Some.Page"));

        this.documentTreeNode.getProperties().put("showTranslations", true);

        assertEquals(1L, this.documentTreeNode.getChildCount("document:wiki:Some.Page"));

        this.documentTreeNode.getProperties().put("showAttachments", true);

        assertEquals(2L, this.documentTreeNode.getChildCount("document:wiki:Some.Page"));

        this.documentTreeNode.getProperties().put("showClassProperties", true);

        assertEquals(3L, this.documentTreeNode.getChildCount("document:wiki:Some.Page"));

        this.documentTreeNode.getProperties().put("showObjects", true);

        assertEquals(4L, this.documentTreeNode.getChildCount("document:wiki:Some.Page"));

        when(this.attachmentsTreeNode.getChildCount("attachments:wiki:Some.Page")).thenReturn(0);

        assertEquals(3L, this.documentTreeNode.getChildCount("document:wiki:Some.Page"));
    }

    @Test
    public void getParentForTerminalPage()
    {
        DocumentReference documentReference = new DocumentReference("wiki", Arrays.asList("Path", "To"), "Page");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.Page"))
            .thenReturn(documentReference);

        when(this.entityTreeNodeIdConverter.convert(String.class,
            new DocumentReference("WebHome", documentReference.getLastSpaceReference())))
                .thenReturn("document:wiki:Path.To.WebHome");

        assertEquals("document:wiki:Path.To.WebHome", this.documentTreeNode.getParent("document:wiki:Path.To.Page"));
    }

    @Test
    public void getParentForNestedPage()
    {
        DocumentReference documentReference = new DocumentReference("wiki", Arrays.asList("Path", "To"), "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.To.WebHome"))
            .thenReturn(documentReference);

        when(this.entityTreeNodeIdConverter.convert(String.class, new DocumentReference("wiki", "Path", "WebHome")))
            .thenReturn("document:wiki:Path.WebHome");

        assertEquals("document:wiki:Path.WebHome", this.documentTreeNode.getParent("document:wiki:Path.To.WebHome"));
    }

    @Test
    public void getParentForTopLevelPage()
    {
        DocumentReference documentReference = new DocumentReference("wiki", "Path", "WebHome");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "document:wiki:Path.WebHome"))
            .thenReturn(documentReference);

        when(this.entityTreeNodeIdConverter.convert(String.class, new WikiReference("wiki"))).thenReturn("wiki:wiki");

        assertEquals("wiki:wiki", this.documentTreeNode.getParent("document:wiki:Path.WebHome"));
    }

    @Test
    public void getParentForInvalidNode()
    {
        SpaceReference spaceReference = new SpaceReference("wiki", "Path");
        when(this.entityTreeNodeIdConverter.convert(EntityReference.class, "space:wiki:Path"))
            .thenReturn(spaceReference);

        assertEquals(null, this.documentTreeNode.getParent("space:wiki:Path"));
    }
}
