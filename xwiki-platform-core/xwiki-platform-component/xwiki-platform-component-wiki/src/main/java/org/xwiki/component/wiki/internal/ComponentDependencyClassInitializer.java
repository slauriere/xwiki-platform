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
package org.xwiki.component.wiki.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.doc.AbstractMandatoryClassInitializer;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Initialize wiki component dependency class.
 * 
 * @version $Id$
 * @since 11.4
 */
@Component
@Named(WikiComponentConstants.DEPENDENCY_CLASS)
@Singleton
public class ComponentDependencyClassInitializer extends AbstractMandatoryClassInitializer
    implements WikiComponentConstants
{
    /**
     * The default constructor.
     */
    public ComponentDependencyClassInitializer()
    {
        super(DEPENDENCY_CLASS_REFERENCE, "Wiki Component Dependency XWiki Class");
    }

    @Override
    protected void createClass(BaseClass xclass)
    {
        xclass.addTextField(COMPONENT_ROLE_TYPE_FIELD, "Dependency Role Type", 30);
        xclass.addTextField(COMPONENT_ROLE_HINT_FIELD, "Dependency Role Hint", 30);
        xclass.addTextField(DEPENDENCY_BINDING_NAME_FIELD, "Binding name", 30);
    }
}
