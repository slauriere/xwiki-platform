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
package org.xwiki.rendering.display.html.internal;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.displayer.HTMLDisplayer;
import org.xwiki.displayer.HTMLDisplayerException;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.template.Template;
import org.xwiki.template.TemplateManager;

/**
 * Default implementation of {@code HTMLDisplayer} using templates.
 *
 * @version $Id$
 * @since 10.11RC1
 */
@Component
@Singleton
public class DefaultTemplateHTMLDisplayer implements HTMLDisplayer<Object>
{
    /**
     * Folder containing the HTML Displayers velocity templates.
     */
    public static final String TEMPLATE_FOLDER = "html_displayer";

    /**
     * Name of the velocity variable containing the value, the mode and parameters of the displayer.
     */
    public static final String DISPLAYER_VELOCITY_NAME = "displayer";

    /**
     * Template extension (velocity).
     */
    public static final String TEMPLATE_EXTENSION = ".vm";

    @Inject
    protected TemplateManager templateManager;

    @Inject
    protected ScriptContextManager scriptContextManager;

    /**
     * {@inheritDoc}
     * <p>
     * Displays the value with the 'view' mode.
     */
    @Override
    public String display(Type type, Object value) throws HTMLDisplayerException
    {
        return display(type, value, Collections.emptyMap());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Displays the value with the 'view' mode.
     */
    @Override
    public String display(Type type, Object value, Map<String, String> parameters) throws HTMLDisplayerException
    {
        return display(type, value, parameters, "view");
    }

    @Override
    public String display(Type type, Object value, Map<String, String> parameters, String mode)
        throws HTMLDisplayerException
    {
        ScriptContext scriptContext = scriptContextManager.getCurrentScriptContext();
        try {
            Map<String, Object> displayer = new HashMap<>();
            displayer.put("type", type);
            displayer.put("value", value);
            displayer.put("parameters", parameters);
            displayer.put("mode", mode);
            scriptContext.setAttribute(DISPLAYER_VELOCITY_NAME, displayer, ScriptContext.ENGINE_SCOPE);

            Writer writer = new StringWriter();
            templateManager.render(getTemplate(type, value, mode), writer);

            return writer.toString();
        } catch (Exception e) {
            throw new HTMLDisplayerException("Couldn't render the template", e);
        } finally {
            scriptContext.removeAttribute(DISPLAYER_VELOCITY_NAME, ScriptContext.ENGINE_SCOPE);
        }
    }

    /**
     * Computes the template name.
     * <p>
     * The following names will be use in this priority order to find an existing template:
     * <ul>
     * <li>html_displayer/[type]/[mode].vm
     * <li>html_displayer/[type].vm
     * <li>html_displayer/[mode].vm
     * <li>html_displayer/default.vm
     * </ul>
     * Please note that the following special characters: &gt;, &lt;, ? and spaces will be replaced by "." in the path.
     *
     * @return the template name used to make the rendering
     */
    private Template getTemplate(Type type, Object value, String mode)
    {
        if (type != null || value == null) {
            return getTemplate(type, mode);
        } else {
            return getTemplate(value.getClass(), mode);
        }
    }

    private Template getTemplate(Type type, String mode)
    {
        Template template = null;
        for (String path : getTemplatePaths(type, mode)) {
            template = templateManager.getTemplate(TEMPLATE_FOLDER + '/' + path + TEMPLATE_EXTENSION);
            if (template != null) {
                break;
            }
        }

        return template;
    }

    private String cleanPath(String path)
    {
        return path.replaceAll("<", "(").replaceAll(">", ")").replaceAll("\\?", "_").replaceAll(" ", "");
    }

    private List<String> getTemplatePaths(Type type, String mode)
    {
        List<String> paths = new ArrayList<>();
        for (String typeName : getTypeNames(type)) {
            if (mode != null) {
                paths.add(cleanPath(typeName + '/' + mode));
            }
            paths.add(cleanPath(typeName));
        }
        if (mode != null) {
            paths.add(cleanPath(mode));
        }
        paths.add("default");
        return paths;
    }

    private List<String> getTypeNames(Type type)
    {
        List<String> typeNames = new ArrayList<>();
        if (type instanceof Class) {
            Class<?> aClass = (Class<?>) type;
            typeNames.add(aClass.getSimpleName().toLowerCase());
            if (aClass.isEnum()) {
                typeNames.add("enum");
            }
        } else if (type != null) {
            typeNames.add(ReflectionUtils.serializeType(type).toLowerCase());
        }
        return typeNames;
    }
}
