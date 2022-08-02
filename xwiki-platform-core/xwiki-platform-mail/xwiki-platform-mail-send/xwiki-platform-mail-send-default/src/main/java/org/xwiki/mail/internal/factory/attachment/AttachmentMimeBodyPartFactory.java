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
package org.xwiki.mail.internal.factory.attachment;

import java.io.File;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.FileUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.environment.Environment;
import org.xwiki.mail.internal.factory.AbstractMimeBodyPartFactory;

import com.xpn.xwiki.api.Attachment;

/**
 * Creates an attachment Body Part from an {@link Attachment} object. This will be added to a Multi Part message.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Named("xwiki/attachment")
@Singleton
public class AttachmentMimeBodyPartFactory extends AbstractMimeBodyPartFactory<Attachment> implements Initializable
{
    /**
     * Header name for storing temporary files used to hold attachment contents. These files are deleted in the
     * Preparation thread, when the messages are serialized before sending.
     */
    public static final String TMP_ATTACHMENT_LOCATION_FILE_HEADER = "X-TmpFile";

    @Inject
    private Environment environment;

    private File temporaryDirectory;

    @Override
    public void initialize()
    {
        this.temporaryDirectory = new File(this.environment.getTemporaryDirectory(), "mail");
        this.temporaryDirectory.mkdirs();
    }

    @Override
    public MimeBodyPart create(Attachment attachment, Map<String, Object> parameters) throws MessagingException
    {
        // Create the attachment part of the email
        MimeBodyPart attachmentPart = new MimeBodyPart();

        // Save the attachment to a temporary file on the file system and wrap it in a Java Mail Data Source.
        // Note that we copy the attachment to a file instead of using directly the attachment data because the
        // attachment could be removed before the mail is sent and the mail would point to some non-existing data.
        DataSource source = createTemporaryAttachmentDataSource(attachment, attachmentPart);
        attachmentPart.setDataHandler(new DataHandler(source));

        attachmentPart.setHeader("Content-Type", attachment.getMimeType());

        // Add a content-id so that we can uniquely reference this attachment. This is used for example to
        // display the attachment inline in some mail HTML content.
        // Note: According to http://tools.ietf.org/html/rfc2392 the id must be enclosed in angle brackets.
        attachmentPart.setHeader("Content-ID", "<" + attachment.getFilename() + ">");

        attachmentPart.setFileName(attachment.getFilename());

        // Handle headers passed as parameter
        addHeaders(attachmentPart, parameters);

        return attachmentPart;
    }

    private DataSource createTemporaryAttachmentDataSource(Attachment attachment, MimeBodyPart attachmentPart)
        throws MessagingException
    {
        File temporaryAttachmentFile;
        try {
            temporaryAttachmentFile = File.createTempFile("attachment", ".tmp", this.temporaryDirectory);
            FileUtils.copyInputStreamToFile(attachment.getContentInputStream(), temporaryAttachmentFile);

            // Add a header with the location of the temporary file so that it can be removed when no longer needed so
            // that it doesn't stay lying around. This is done in the Mail Preparation Thread.
            attachmentPart.setHeader(TMP_ATTACHMENT_LOCATION_FILE_HEADER, temporaryAttachmentFile.getAbsolutePath());
        } catch (Exception e) {
            throw new MessagingException(
                String.format("Failed to save attachment [%s] to the file system", attachment.getFilename()), e);
        }

        return new FileDataSource(temporaryAttachmentFile);
    }
}
