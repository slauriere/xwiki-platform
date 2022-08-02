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
package org.xwiki.rendering.async.internal.block;

import org.xwiki.rendering.RenderingException;
import org.xwiki.rendering.async.internal.AsyncRenderer;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Block based asynchronous renderer.
 * 
 * @version $Id$
 * @since 10.10RC1
 */
public interface BlockAsyncRenderer extends AsyncRenderer
{
    @Override
    BlockAsyncRendererResult render(boolean async, boolean cached) throws RenderingException;

    /**
     * @return true if the renderer come from an inline context
     */
    boolean isInline();

    /**
     * @return the syntax in which to render the result
     */
    Syntax getTargetSyntax();
}
