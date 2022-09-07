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
package org.xwiki.image.lightbox.test.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.flamingo.skin.test.po.AttachmentsViewPage;
import org.xwiki.image.lightbox.test.po.ImagePopover;
import org.xwiki.image.lightbox.test.po.Lightbox;
import org.xwiki.image.lightbox.test.po.LightboxPage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.rest.model.jaxb.Object;
import org.xwiki.test.docker.junit5.TestConfiguration;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional tests for the image lightbox.
 * 
 * @version $Id$
 * @since 14.1RC1
 */
@UITest(properties = {
    // Add the FileUploadPlugin which is needed by the test to upload attachment files
    "xwikiCfgPlugins=com.xpn.xwiki.plugin.fileupload.FileUploadPlugin"})
class LightboxIT
{
    private static final DocumentReference LIGHTBOX_CONFIGURATION_REFERENCE =
        new DocumentReference("xwiki", Arrays.asList("XWiki", "Lightbox"), "LightboxConfiguration");

    private static final String LIGHTBOX_CONFIGURATION_CLASSNAME = "XWiki.Lightbox.LightboxConfigurationClass";

    private static final List<String> IMAGES = Arrays.asList("image1.png", "image2.png", "missingImage.png");

    public static final String USER_NAME = "JohnDoe";

    @BeforeAll
    void beforeAll(TestUtils testUtils)
    {
        testUtils.createUserAndLogin(USER_NAME, "pa$$word");
    }

    @Test
    @Order(1)
    void disabledLightbox(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, false);

        testUtils.createPage(testReference, getSimpleImage(IMAGES.get(0)), "Disabled Lightbox");
        LightboxPage lightboxPage = new LightboxPage();
        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Optional<ImagePopover> imagePopover = lightboxPage.hoverImage(0);
        assertFalse(imagePopover.isPresent());
    }

    @Test
    @Order(2)
    void openImageWithoutDescription(TestUtils testUtils, TestReference testReference,
        TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        String lastUploadDate =
            lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        // Verify the image ID popover actions.
        Optional<ImagePopover> imagePopover = lightboxPage.hoverImage(0);
        assertTrue(imagePopover.isPresent());
        ImagePopover currentImagePopover = imagePopover.get();
        assertTrue(currentImagePopover.isImagePopoverDisplayed());

        WebElement imagePermalinkButton = currentImagePopover.getImagePermalinkButton();
        String[] elements = imagePermalinkButton.getAttribute("href").split("#");
        assertEquals("Iimage1.png", elements[elements.length - 1]);

        imagePermalinkButton.click();
        assertEquals(testUtils.getDriver().getCurrentUrl(), imagePermalinkButton.getAttribute("href"));
        assertEquals("Iimage1.png", currentImagePopover.getImageId());

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        assertEquals("0", lightbox.getSlideIndex());
        assertEquals(IMAGES.get(0), lightbox.getCaption());
        assertEquals("", lightbox.getTitle());
        assertEquals("Posted by JohnDoe", lightbox.getPublisher());
        assertEquals(lastUploadDate, lightbox.getDate());
        assertEquals("Iimage1.png", lightbox.getImageId());
    }

    @Test
    @Order(3)
    void openImageWithCaptionAndManuallyAddedId(TestUtils testUtils, TestReference testReference,
        TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getImageWithCaptionAndManuallyAddedId(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        String lastUploadDate =
            lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        // Verify the image ID popover actions.
        Optional<ImagePopover> imagePopover = lightboxPage.hoverImage(0);
        assertTrue(imagePopover.isPresent());
        ImagePopover currentImagePopover = imagePopover.get();
        assertTrue(currentImagePopover.isImagePopoverDisplayed());

        WebElement imagePermalinkButton = currentImagePopover.getImagePermalinkButton();
        String[] elements = imagePermalinkButton.getAttribute("href").split("#");
        assertEquals("manuallyAddedImageId", elements[elements.length - 1]);

        imagePermalinkButton.click();
        assertEquals(testUtils.getDriver().getCurrentUrl(), imagePermalinkButton.getAttribute("href"));
        assertEquals("manuallyAddedImageId", currentImagePopover.getImageId());

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        assertEquals("0", lightbox.getSlideIndex());
        assertEquals("Caption", lightbox.getCaption());
        assertEquals(IMAGES.get(0), lightbox.getTitle());
        assertEquals("Posted by JohnDoe", lightbox.getPublisher());
        assertEquals(lastUploadDate, lightbox.getDate());
        assertEquals("manuallyAddedImageId", lightbox.getImageId());
    }

    @Test
    @Order(4)
    void openImageWithAlt(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getImageWithAlt(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        String lastUploadDate =
            lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        assertEquals("0", lightbox.getSlideIndex());
        assertEquals("Alternative text", lightbox.getCaption());
        assertEquals("", lightbox.getTitle());
        assertEquals("Posted by JohnDoe", lightbox.getPublisher());
        assertEquals(lastUploadDate, lightbox.getDate());
        assertEquals("Iimage1.png", lightbox.getImageId());
    }

    @Test
    @Order(5)
    void openIconImage(TestUtils testUtils, TestReference testReference)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, "[[image:icon:accept]]");

        LightboxPage lightboxPage = new LightboxPage();
        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        assertEquals("0", lightbox.getSlideIndex());
        assertEquals("accept", lightbox.getCaption());
        assertEquals("", lightbox.getTitle());
        assertEquals("", lightbox.getPublisher());
        assertEquals("", lightbox.getDate());
        assertEquals("Iaccept", lightbox.getImageId());
    }

    @Test
    @Order(6)
    void clickLightboxEscape(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        lightbox.close();
        assertFalse(lightbox.isDisplayed());
    }

    @Test
    @Order(7)
    void navigateThroughImages(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)) + this.getSimpleImage(IMAGES.get(1)));
        LightboxPage lightboxPage = new LightboxPage();

        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));
        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(1));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        // Using arrows.
        lightbox.next(1);
        assertEquals("1", lightbox.getSlideIndex());
        lightbox.previous(0);
        assertEquals("0", lightbox.getSlideIndex());

        // Using thumbnails icons.
        lightbox.clickThumbnail(1);
        assertEquals("1", lightbox.getSlideIndex());
        lightbox.clickThumbnail(0);
        assertEquals("0", lightbox.getSlideIndex());
    }

    @Test
    @Order(8)
    void playSlideshow(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)) + this.getSimpleImage(IMAGES.get(1))
            + this.getSimpleImage(IMAGES.get(2)));
        LightboxPage lightboxPage = new LightboxPage();

        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));
        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(1));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        // Start auto play.
        lightbox.toggleSlideshow();
        assertTrue(lightbox.waitForSlide(1));
        assertTrue(lightbox.waitForSlide(2));
        assertTrue(lightbox.waitForSlide(0));

        // Stop auto play.
        lightbox.toggleSlideshow();
        assertFalse(lightbox.waitForSlide(1));
    }

    @Test
    @Order(9)
    void openMissingImage(TestUtils testUtils, TestReference testReference)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(2)));
        LightboxPage lightboxPage = new LightboxPage();

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());
        assertTrue(lightbox.isDisplayed());

        assertTrue(lightbox.isImageMissing());
        assertEquals("0", lightbox.getSlideIndex());
        assertEquals(IMAGES.get(2), lightbox.getCaption());
        assertEquals("", lightbox.getTitle());
        assertEquals("", lightbox.getDate());
    }

    @Test
    @Order(10)
    void openFullscreen(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        // Enter and exit fullscreen.
        assertTrue(lightbox.toggleFullscreen());
        assertFalse(lightbox.toggleFullscreen());
    }

    @Test
    @Order(11)
    void verifyDownload(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        // Verify the image popover download action.
        Optional<ImagePopover> imagePopover = lightboxPage.hoverImage(0);
        assertTrue(imagePopover.isPresent());
        ImagePopover currentImagePopover = imagePopover.get();
        assertTrue(currentImagePopover.isImagePopoverDisplayed());

        WebElement popoverDownload = currentImagePopover.getDownloadButton();
        assertEquals(lightboxPage.getImageElement(0).getAttribute("src"), popoverDownload.getAttribute("href"));
        assertEquals("image1.png", popoverDownload.getAttribute("download"));

        // Verify the image lightbox download action.
        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());

        WebElement slide = lightbox.getSlideElement();
        WebElement lightboxDownload = lightbox.getDownloadButton();
        assertEquals(slide.findElement(By.tagName("img")).getAttribute("src"), lightboxDownload.getAttribute("href"));
        assertEquals(IMAGES.get(0), lightboxDownload.getAttribute("download"));
    }

    @Test
    @Order(12)
    void verifyPartiallyDisabledLightbox(TestUtils testUtils, TestReference testReference)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getPartiallyDisabledLightboxContent());
        LightboxPage lightboxPage = new LightboxPage();

        Optional<ImagePopover> imagePopover = lightboxPage.hoverImage(0);
        assertFalse(imagePopover.isPresent());

        imagePopover = lightboxPage.hoverImage(1);
        assertTrue(imagePopover.isPresent());
    }

    @Test
    @Order(13)
    void openImageWithoutId(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
    {
        enableLightbox(testUtils, true);

        testUtils.createPage(testReference, this.getImageWithoutId(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();

        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Optional<ImagePopover> imagePopover = lightboxPage.hoverImage(0);
        assertTrue(imagePopover.isPresent());
        ImagePopover currentImagePopover = imagePopover.get();
        assertTrue(currentImagePopover.isImagePopoverDisplayed());
        assertFalse(currentImagePopover.getImagePermalinkButton().isDisplayed());
        assertFalse(currentImagePopover.getCopyImageIdButton().isDisplayed());

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());
        assertFalse(lightbox.getCopyImageIdButton().isDisplayed());
    }

    /**
     * Check that the date displayed inside the lightbox takes into account user's timezone.
     */
    @Test
    @Order(14)
    void setNewTimezone(TestUtils testUtils, TestReference testReference, TestConfiguration testConfiguration)
        throws Exception
    {
        enableLightbox(testUtils, true);

        setTimezone(testUtils, "Europe/Paris");
        testUtils.createPage(testReference, this.getSimpleImage(IMAGES.get(0)));
        LightboxPage lightboxPage = new LightboxPage();
        lightboxPage.attachFile(testConfiguration.getBrowser().getTestResourcesPath(), IMAGES.get(0));
        String lastUploadDate =
            new AttachmentsViewPage().openAttachmentsDocExtraPane().getDateOfLastUpload(IMAGES.get(0));

        // Make sure that the images are displayed.
        lightboxPage.reloadPage();

        Lightbox lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());
        assertEquals(lastUploadDate, lightbox.getDate());

        setTimezone(testUtils, "America/Barbados");
        lightboxPage.reloadPage();

        lastUploadDate = new AttachmentsViewPage().openAttachmentsDocExtraPane().getDateOfLastUpload(IMAGES.get(0));
        lightbox = lightboxPage.openLightboxAtImage(0);
        assertTrue(lightbox.isDisplayed());
        assertEquals(lastUploadDate, lightbox.getDate());
    }

    private void setTimezone(TestUtils testUtils, String timezoneValue) throws Exception
    {
        Object userObject = testUtils.rest().object(new LocalDocumentReference("XWiki", USER_NAME), "XWiki.XWikiUsers");
        userObject.withProperties(TestUtils.RestTestUtils.property("timezone", timezoneValue));
        testUtils.rest().update(userObject);
    }

    private void enableLightbox(TestUtils testUtils, boolean enable)
    {
        testUtils.updateObject(LIGHTBOX_CONFIGURATION_REFERENCE, LIGHTBOX_CONFIGURATION_CLASSNAME, 0,
            "isLightboxEnabled", enable ? "1" : "0");
    }

    private String getSimpleImage(String image)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[[image:");
        sb.append(image);
        sb.append("|| width=120 height=120]]\n\n");

        return sb.toString();
    }

    private String getImageWithCaptionAndManuallyAddedId(String image)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("{{figure}}\n[[image:");
        sb.append(image);
        sb.append("||width=120 height=120]]\n\n");
        sb.append("{{figureCaption}}{{id name=\"manuallyAddedImageId\"/}}\n");
        sb.append("Caption{{/figureCaption}}\n\n");
        sb.append("{{/figure}}\n\n");

        return sb.toString();
    }

    private String getImageWithoutId(String image)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("{{figure}}\n[[image:");
        sb.append(image);
        sb.append("||width=120 height=120]]\n\n");
        sb.append("{{figureCaption}}{{id name=''/}}\n");
        sb.append("Caption{{/figureCaption}}\n\n");
        sb.append("{{/figure}}\n\n");

        return sb.toString();
    }

    private String getImageWithAlt(String image)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("[[image:");
        sb.append(image);
        sb.append("||alt=\"Alternative text\" width=120 height=120]]\n\n");

        return sb.toString();
    }

    private String getPartiallyDisabledLightboxContent()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("{{html}}\n");
        sb.append("<div data-xwiki-lightbox='false'><img src='/xwiki/resources/icons/silk/accept.png'/></div>\n");
        sb.append("<div><img src='/xwiki/resources/icons/silk/accept.png'/></div>\n");
        sb.append("{{/html}}");

        return sb.toString();
    }
}
