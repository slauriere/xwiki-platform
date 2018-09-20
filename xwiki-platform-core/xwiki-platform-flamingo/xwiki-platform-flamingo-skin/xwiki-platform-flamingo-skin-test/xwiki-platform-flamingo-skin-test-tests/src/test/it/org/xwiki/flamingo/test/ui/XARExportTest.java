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
package org.xwiki.flamingo.test.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openqa.selenium.By;
import org.xwiki.flamingo.sking.test.po.ExportModal;
import org.xwiki.flamingo.sking.test.po.OtherFormatView;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.ui.AbstractTest;
import org.xwiki.test.ui.po.ViewPage;
import org.xwiki.tree.test.po.TreeElement;
import org.xwiki.tree.test.po.TreeNodeElement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verify that the XAR export features works fine.
 *
 * @version $Id$
 * @since 10.8
 */
public class XARExportTest extends AbstractTest
{
    @Test
    public void scenarioExportXAR() throws Exception
    {
        setupPages();
        exportXARAll();
        exportXARLotOfSelectedFiles();
        exportXARWithUnselect();
        exportXARWithUnselectMorePages();
        exportXARSelectOnePage();
    }

    private void setupPages() throws Exception
    {
        getUtil().loginAsSuperAdmin();

        // Create a space Foo
        getUtil().createPage("Foo", "WebHome", "Foo", "Foo");

        // Create 100 pages under that space (will be used for the test with lot of selected pages)
        for (int i = 0; i < 100; i++) {
            String name = "Foo_" + i;
            DocumentReference documentReference = new DocumentReference("xwiki", Arrays.asList("Foo", name), "WebHome");
            if (!getUtil().rest().exists(documentReference)) {
                getUtil().rest().savePage(documentReference);
            }
        }
    }

    /*
       Scenario: export a XAR after opening the export window and selecting "Other Format"
       Don't change anything in the tree of export.
     */
    private void exportXARAll() throws Exception
    {
        getUtil().loginAsSuperAdmin();
        ViewPage viewPage = getUtil().gotoPage("Foo", "WebHome");
        viewPage.clickMoreActionsSubMenuEntry("tmExport");

        ExportModal exportModal = new ExportModal();
        //assertTrue(exportModal.isDisplayed());

        OtherFormatView otherFormatView = exportModal.openOtherFormatView();
        assertTrue(otherFormatView.isTreeAvailable());
        assertTrue(otherFormatView.isExportAsXARButtonAvailable());

        TreeElement treeElement = otherFormatView.getTreeElement();
        List<TreeNodeElement> topLevelNodes = treeElement.getTopLevelNodes();
        assertEquals(1, topLevelNodes.size());

        otherFormatView.clickExportAsXARButton();

        String postURL = getUtil().getURL("Foo", "WebHome", "export", "format=xar&name=Foo.WebHome&pages=xwiki%3AFoo.%25");
        assertEquals(postURL, otherFormatView.getForm().getAttribute("action"));
        assertTrue(otherFormatView.getExcludingPagesField().getAttribute("value").isEmpty());
        assertTrue(otherFormatView.getIncludingPagesField().getAttribute("value").isEmpty());
        getUtil().forceGuestUser();
    }

    /*
        Scenario: Export a XAR after opening every pages in the pagination
        and selecting everything
     */
    public void exportXARLotOfSelectedFiles()
    {
        getUtil().loginAsSuperAdmin();
        ViewPage viewPage = getUtil().gotoPage("Foo", "WebHome");
        viewPage.clickMoreActionsSubMenuEntry("tmExport");

        ExportModal exportModal = new ExportModal();
        OtherFormatView otherFormatView = exportModal.openOtherFormatView();
        assertTrue(otherFormatView.isTreeAvailable());
        assertTrue(otherFormatView.isExportAsXARButtonAvailable());

        TreeElement treeElement = otherFormatView.getTreeElement();
        List<TreeNodeElement> topLevelNodes = treeElement.getTopLevelNodes();
        assertEquals(1, topLevelNodes.size());

        TreeNodeElement root = topLevelNodes.get(0);
        root.open().waitForIt();

        assertEquals(16, root.getChildren().size());

        // change the timeout as it might take time to load all nodes
        getDriver().setTimeout(20);

        TreeNodeElement lastNode = null;
        int size = 0;
        for (int i = 15; i < 100; i += 15) {
            size = root.getChildren().size();
            lastNode = root.getChildren().get(size - 1);

            String lastNodeLabel = lastNode.getLabel();
            lastNode.deselect();
            lastNode.select();

            getDriver().waitUntilElementDisappears(exportModal.getContainer(), By.linkText(lastNodeLabel));
        }

        assertEquals(100, root.getChildren().size());

        otherFormatView.clickExportAsXARButton();

        String postURL = getUtil().getURL("Foo", "WebHome", "export", "format=xar&name=Foo.WebHome&pages=xwiki%3AFoo.%25");
        assertEquals(postURL, otherFormatView.getForm().getAttribute("action"));
        assertTrue(otherFormatView.getExcludingPagesField().getAttribute("value").isEmpty());
        assertTrue(otherFormatView.getIncludingPagesField().getAttribute("value").isEmpty());

        getUtil().forceGuestUser();
    }

    /*
        Scenario: Export a XAR after unselecting some pages
     */
    public void exportXARWithUnselect()
    {
        getUtil().loginAsSuperAdmin();
        ViewPage viewPage = getUtil().gotoPage("Foo", "WebHome");
        viewPage.clickMoreActionsSubMenuEntry("tmExport");

        ExportModal exportModal = new ExportModal();
        OtherFormatView otherFormatView = exportModal.openOtherFormatView();
        assertTrue(otherFormatView.isTreeAvailable());
        assertTrue(otherFormatView.isExportAsXARButtonAvailable());

        TreeElement treeElement = otherFormatView.getTreeElement();
        List<TreeNodeElement> topLevelNodes = treeElement.getTopLevelNodes();
        assertEquals(1, topLevelNodes.size());

        TreeNodeElement root = topLevelNodes.get(0);
        root = root.open().waitForIt();

        assertEquals(16, root.getChildren().size());

        TreeNodeElement node5 = root.getChildren().get(5);
        TreeNodeElement node11 = root.getChildren().get(11);

        // nodes are ordered using alphabetical order:
        // 1 -> Foo_1
        // 2 -> Foo_10
        // ...
        // 5 -> Foo_13

        node5.deselect();

        // 11 -> Foo_19
        node11.deselect();

        // 12 -> Foo_2
        // 13 -> Foo_20

        otherFormatView.clickExportAsXARButton();

        String postURL = getUtil().getURL("Foo", "WebHome", "export", "format=xar&name=Foo.WebHome&pages=xwiki%3AFoo.%25");
        assertEquals(postURL, otherFormatView.getForm().getAttribute("action"));

        String excludingPages = "xwiki%3AFoo.Foo_13.WebHome&xwiki%3AFoo.Foo_19.WebHome";
        assertEquals(excludingPages, otherFormatView.getExcludingPagesField().getAttribute("value"));

        assertTrue(otherFormatView.getIncludingPagesField().getAttribute("value").isEmpty());

        getUtil().forceGuestUser();
    }

    /*
        Scenario: Export a XAR after unselecting some pages and the "More pages" node
     */
    public void exportXARWithUnselectMorePages()
    {
        getUtil().loginAsSuperAdmin();
        ViewPage viewPage = getUtil().gotoPage("Foo", "WebHome");
        viewPage.clickMoreActionsSubMenuEntry("tmExport");

        ExportModal exportModal = new ExportModal();
        OtherFormatView otherFormatView = exportModal.openOtherFormatView();
        assertTrue(otherFormatView.isTreeAvailable());
        assertTrue(otherFormatView.isExportAsXARButtonAvailable());

        TreeElement treeElement = otherFormatView.getTreeElement();
        List<TreeNodeElement> topLevelNodes = treeElement.getTopLevelNodes();
        assertEquals(1, topLevelNodes.size());

        TreeNodeElement root = topLevelNodes.get(0);
        root = root.open().waitForIt();

        assertEquals(16, root.getChildren().size());

        TreeNodeElement node5 = root.getChildren().get(5);
        TreeNodeElement node11 = root.getChildren().get(11);
        TreeNodeElement nodeMoreElement = root.getChildren().get(15);

        // nodes are ordered using alphabetical order:
        // 1 -> Foo_1
        // 2 -> Foo_10
        // ...
        // 5 -> Foo_13

        node5.deselect();

        // 11 -> Foo_19
        node11.deselect();

        // 12 -> Foo_2
        // 13 -> Foo_20

        nodeMoreElement.deselect();



        otherFormatView.clickExportAsXARButton();

        String postURL = getUtil().getURL("Foo", "WebHome", "export", "format=xar&name=Foo.WebHome");
        assertEquals(postURL, otherFormatView.getForm().getAttribute("action"));

        String includingPages = "xwiki%3AFoo.WebHome&xwiki%3AFoo.WebPreferences&xwiki%3AFoo.Foo_0.WebHome&xwiki%3AFoo.Foo_1.WebHome"
            + "&xwiki%3AFoo.Foo_10.WebHome&xwiki%3AFoo.Foo_11.WebHome&xwiki%3AFoo.Foo_12.WebHome"
            + "&xwiki%3AFoo.Foo_14.WebHome&xwiki%3AFoo.Foo_15.WebHome&xwiki%3AFoo.Foo_16.WebHome"
            + "&xwiki%3AFoo.Foo_17.WebHome&xwiki%3AFoo.Foo_18.WebHome&xwiki%3AFoo.Foo_2.WebHome"
            + "&xwiki%3AFoo.Foo_20.WebHome&xwiki%3AFoo.Foo_21.WebHome";
        assertEquals(includingPages, otherFormatView.getIncludingPagesField().getAttribute("value"));
        String excludingPages = "xwiki%3AFoo.Foo_13.WebHome&xwiki%3AFoo.Foo_19.WebHome";
        assertEquals(excludingPages, otherFormatView.getExcludingPagesField().getAttribute("value"));

        getUtil().forceGuestUser();
    }

    /*
        Scenario: Export a XAR after clicking select none and selecting one page
     */
    public void exportXARSelectOnePage()
    {
        getUtil().loginAsSuperAdmin();
        ViewPage viewPage = getUtil().gotoPage("Foo", "WebHome");
        viewPage.clickMoreActionsSubMenuEntry("tmExport");

        ExportModal exportModal = new ExportModal();
        OtherFormatView otherFormatView = exportModal.openOtherFormatView();
        assertTrue(otherFormatView.isTreeAvailable());
        assertTrue(otherFormatView.isExportAsXARButtonAvailable());

        TreeElement treeElement = otherFormatView.getTreeElement();
        List<TreeNodeElement> topLevelNodes = treeElement.getTopLevelNodes();
        assertEquals(1, topLevelNodes.size());

        TreeNodeElement root = topLevelNodes.get(0);
        root = root.open().waitForIt();

        assertEquals(16, root.getChildren().size());

        TreeNodeElement node5 = root.getChildren().get(5);
        TreeNodeElement node11 = root.getChildren().get(11);
        TreeNodeElement nodeMoreElement = root.getChildren().get(15);

        otherFormatView.getSelectNoneLink().click();

        // nodes are ordered using alphabetical order:
        // 1 -> Foo_1
        // 2 -> Foo_10
        // ...
        // 5 -> Foo_13

        node5.select();

        // 11 -> Foo_19
        node11.select();

        // 12 -> Foo_2
        // 13 -> Foo_20



        otherFormatView.clickExportAsXARButton();

        String postURL = getUtil().getURL("Foo", "WebHome", "export", "format=xar&name=Foo.WebHome");
        assertEquals(postURL, otherFormatView.getForm().getAttribute("action"));

        String includingPages = "xwiki%3AFoo.Foo_13.WebHome&xwiki%3AFoo.Foo_19.WebHome";

        String excludingPages = "xwiki%3AFoo.Foo_0.WebHome&xwiki%3AFoo.Foo_1.WebHome"
            + "&xwiki%3AFoo.Foo_10.WebHome&xwiki%3AFoo.Foo_11.WebHome&xwiki%3AFoo.Foo_12.WebHome"
            + "&xwiki%3AFoo.Foo_14.WebHome&xwiki%3AFoo.Foo_15.WebHome&xwiki%3AFoo.Foo_16.WebHome"
            + "&xwiki%3AFoo.Foo_17.WebHome&xwiki%3AFoo.Foo_18.WebHome&xwiki%3AFoo.Foo_2.WebHome"
            + "&xwiki%3AFoo.Foo_20.WebHome&xwiki%3AFoo.Foo_21.WebHome";

        assertEquals(includingPages, otherFormatView.getIncludingPagesField().getAttribute("value"));
        assertEquals(excludingPages, otherFormatView.getExcludingPagesField().getAttribute("value"));

        getUtil().forceGuestUser();
    }
}
