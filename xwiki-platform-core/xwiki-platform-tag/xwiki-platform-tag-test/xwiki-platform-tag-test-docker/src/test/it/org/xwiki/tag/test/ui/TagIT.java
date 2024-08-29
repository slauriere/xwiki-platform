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
package org.xwiki.tag.test.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.tag.test.po.AddTagsPane;
import org.xwiki.tag.test.po.TagPage;
import org.xwiki.tag.test.po.TaggablePage;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Several tests for adding and removing tags to/from a wiki page.
 *
 * @version $Id$
 * @since 15.3RC1
 * @since 14.10.8
 */
@UITest(properties = {
    "xwikiCfgPlugins=com.xpn.xwiki.plugin.tag.TagPlugin",
})
class TagIT
{
    private TagPage tagPage;

    @BeforeEach
    void setUp(TestUtils setup)
    {
        setup.loginAsSuperAdmin();
    }

    /**
     * Adds and removes a tag.
     */
    @Test
    @Order(1)
    void addRemoveTag(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String tag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(tag));
        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(tag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(tag));
        taggablePage.removeTag(tag);
        assertFalse(taggablePage.hasTag(tag));
    }

    /**
     * Open the add tag panel, cancel then open again the add tag panel and add a new tag.
     */
    @Test
    @Order(2)
    void cancelAddTag(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String firstTag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(firstTag));
        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(firstTag);
        addTagsPane.cancel();

        String secondTag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(secondTag));
        addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(secondTag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(secondTag));
        assertFalse(taggablePage.hasTag(firstTag));
    }

    /**
     * Add many tags and remove one of them.
     */
    @Test
    @Order(3)
    void addManyRemoveOneTag(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String firstTag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(firstTag));
        String secondTag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(secondTag));

        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(firstTag + "," + secondTag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(firstTag));
        assertTrue(taggablePage.hasTag(secondTag));

        assertTrue(taggablePage.removeTag(firstTag));
        assertTrue(taggablePage.hasTag(secondTag));
    }

    /**
     * Tests that a tag can't be added twice to the same page.
     */
    @Test
    @Order(4)
    void addExistingTag(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String tag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(tag));
        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(tag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(tag));

        addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(tag);
        assertFalse(addTagsPane.add());
        addTagsPane.cancel();
    }

    /**
     * Add a tag that contains the pipe character, which is used to separate stored tags.
     */
    @Test
    @Order(5)
    void testAddTagContainingPipe(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String tag = randomAlphanumeric(3) + "|" + randomAlphanumeric(3);
        assertFalse(taggablePage.hasTag(tag));
        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(tag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(tag));

        // Reload the page and test again.
        setup.gotoPage(testReference);
        taggablePage = new TaggablePage();
        assertTrue(taggablePage.hasTag(tag));
    }

    /**
     * @see <a href="https://jira.xwiki.org/browse/XWIKI-3843">XWIKI-3843</a>: Strip leading and trailing white
     *     spaces to tags when white space is not the separator
     */
    @Test
    @Order(6)
    void stripLeadingAndTrailingSpacesFromTags(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String firstTag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(firstTag));
        String secondTag = randomAlphanumeric(4);
        assertFalse(taggablePage.hasTag(secondTag));

        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags("   " + firstTag + " ,  " + secondTag + "    ");
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(firstTag));
        assertTrue(taggablePage.hasTag(secondTag));
    }

    /**
     * @see <a href="https://jira.xwiki.org/browse/XWIKI-6549">XWIKI-6549</a>: Prevent adding new tags that are
     *     equal ignoring case with existing tags
     */
    @Test
    @Order(7)
    void testTagCaseIsIgnored(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String firstTag = "taG1";
        assertFalse(taggablePage.hasTag(firstTag));
        // Second tag is same as first tag but with different uppercase/lowercase chars.
        String secondTag = "Tag1";
        assertFalse(taggablePage.hasTag(secondTag));
        String thirdTag = "tag3";
        assertFalse(taggablePage.hasTag(thirdTag));

        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(firstTag + "," + thirdTag + "," + secondTag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(firstTag));
        assertFalse(taggablePage.hasTag(secondTag));
        assertTrue(taggablePage.hasTag(thirdTag));
    }

    @Test
    @Order(8)
    void addAndRenameTagFromTagPage(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String tag = "MyTag";
        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(tag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(tag));
        this.tagPage = taggablePage.clickOnTag(tag);
        this.tagPage.clickRenameButton();
        this.tagPage.setNewTagName("MyTagRenamed");
        this.tagPage.clickConfirmRenameTagButton();
        assertTrue(this.tagPage.hasTagHighlight("MyTagRenamed"));
    }

    @Test
    @Order(9)
    void addAndDeleteTagFromTagPage(TestUtils setup, TestReference testReference)
    {
        TaggablePage taggablePage = resetTaggablePage(setup, testReference);
        String tag = "MyTagToBeDeleted";
        AddTagsPane addTagsPane = taggablePage.addTags();
        addTagsPane.setTags(tag);
        assertTrue(addTagsPane.add());
        assertTrue(taggablePage.hasTag(tag));
        this.tagPage = taggablePage.clickOnTag(tag);
        this.tagPage.clickDeleteButton();
        this.tagPage.clickConfirmDeleteTag();
        assertTrue(this.tagPage.hasConfirmationMessage(tag));
    }

    private TaggablePage resetTaggablePage(TestUtils setup, DocumentReference entityReference)
    {
        // Create a new test page.
        setup.deletePage(entityReference);
        setup.createPage(entityReference, "");
        return new TaggablePage();
    }
}
