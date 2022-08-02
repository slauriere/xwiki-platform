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
package org.xwiki.test.integration.junit5;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.test.integration.junit.StackTraceLogParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link StackTraceLogParser}.
 *
 * @version $Id$
 * @since 11.4RC1
 */
public class StackTraceLogParserTest
{
    @Test
    public void parseLogWithStackTrace() throws Exception
    {
        String log = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("stacktrace.txt"), "UTF-8");
        StackTraceLogParser parser = new StackTraceLogParser();
        List<String> results = parser.parse(log);
        assertEquals(47, results.size());
        assertEquals("2019-05-06 20:02:37,331 [Exec Stream Pumper] - 2019-05-06 20:02:37,330 "
                + "[http://localhost:8080/xwiki/webjars/wiki%3Axwiki/jstree/3.3.7/jstree.min.js] "
                    + "ERROR ebJarsResourceReferenceHandler - Failed to read resource [jstree/3.3.7/jstree.min.js]\n"
            + "2019-05-06 20:02:37,331 [Exec Stream Pumper] - org.xwiki.resource.ResourceReferenceHandlerException: "
                + "Failed to read resource [jstree/3.3.7/jstree.min.js]\n"
            + "2019-05-06 20:02:37,352 [Exec Stream Pumper] - Caused by: org.eclipse.jetty.io.EofException: null\n"
            + "2019-05-06 20:02:37,359 [Exec Stream Pumper] - Caused by: java.io.IOException: Broken pipe",
            results.get(45));
    }

    @Test
    public void parseWithInvalidStacktrace()
    {
        // Verify that it works if the third line is shorter or equal to the searched patterns.
        String log = "date - line1\n"
            + "date - line2\n"
            + "date - \tat \n"
            + "date - Caused by: \n"
            + "date - test";
        StackTraceLogParser parser = new StackTraceLogParser();
        List<String> results = parser.parse(log);

        // We validate we didn't recognize a stack trace (otherwise it would be 3 lines and not 5).
        assertEquals(5, results.size());
        assertEquals("date - line1", results.get(0));
        assertEquals("date - line2", results.get(1));
        assertEquals("date - \tat ", results.get(2));
        assertEquals("date - Caused by: ", results.get(3));
        assertEquals("date - test", results.get(4));
    }

    @Test
    public void parseWithLeadingStacktrace()
    {
        String log = "date - line1\n"
            + "date - line2\n"
            + "date - \tat x\n"
            + "date - Caused by: x\n"
            + "date - test";
        StackTraceLogParser parser = new StackTraceLogParser();
        List<String> results = parser.parse(log);

        assertEquals(2, results.size());
        assertEquals("date - line1\ndate - line2\ndate - Caused by: x", results.get(0));
        assertEquals("date - test", results.get(1));
    }

    @Test
    public void parseWithCapturedLogs()
    {
        String log = ""
            + "date [x] INFO  Class - STDOUT: date [main] ERROR OtherClass"
                + "- Configured permanent directory [/var/local/xwiki] could not be created. \n"
            + "date [x] INFO  Class - STDOUT: java.nio.file.AccessDeniedException: /var/local/xwiki\n"
            + "date [x] INFO  Class - STDOUT: \tat translateToIOException(UnixException.java:84)\n";
        StackTraceLogParser parser = new StackTraceLogParser();
        List<String> results = parser.parse(log);

        assertEquals(1, results.size());
        assertEquals("date [x] INFO  Class - STDOUT: date [main] ERROR OtherClass"
            + "- Configured permanent directory [/var/local/xwiki] could not be created. \n"
            + "date [x] INFO  Class - STDOUT: java.nio.file.AccessDeniedException: /var/local/xwiki", results.get(0));
    }

    @Test
    public void parseWithNoPrefixedLogs()
    {
        String log = ""
            + "WARN  - stacktrace\n"
            + "java.lang.Exception: exception\n"
            + "\tat doSomething(ValidateConsoleExtensionTest.java:94)";
        StackTraceLogParser parser = new StackTraceLogParser();
        List<String> results = parser.parse(log);

        assertEquals(1, results.size());
        assertEquals("WARN  - stacktrace\njava.lang.Exception: exception", results.get(0));

    }
}
