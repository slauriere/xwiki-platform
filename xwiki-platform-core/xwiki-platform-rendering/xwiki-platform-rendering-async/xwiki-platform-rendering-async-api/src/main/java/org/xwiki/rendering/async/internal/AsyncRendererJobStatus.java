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
package org.xwiki.rendering.async.internal;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.job.AbstractJobStatus;
import org.xwiki.job.annotation.Serializable;
import org.xwiki.logging.LoggerManager;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.ObservationManager;
import org.xwiki.rendering.async.internal.DefaultAsyncContext.RightEntry;

/**
 * The status of the {@link AsyncRendererJob}.
 * 
 * @version $Id$
 * @since 10.10RC1
 */
// TODO: we might want to decide to isolate asynchronous renderer log at some point
@Serializable(false)
public class AsyncRendererJobStatus extends AbstractJobStatus<AsyncRendererJobRequest>
{
    /**
     * The type of the job.
     */
    public static final String JOBTYPE = "asyncrenderer";

    private AsyncRendererResult result;

    private Set<EntityReference> references;

    private Set<Type> roleTypes;

    private Set<ComponentRole<?>> roles;

    private Set<RightEntry> rights;

    private Map<String, Collection<Object>> uses;

    private boolean async;

    private Set<String> clients = ConcurrentHashMap.newKeySet();

    /**
     * @param request the request provided when started the job
     * @param observationManager the observation manager component
     * @param loggerManager the logger manager component
     */
    public AsyncRendererJobStatus(AsyncRendererJobRequest request, ObservationManager observationManager,
        LoggerManager loggerManager)
    {
        super(JOBTYPE, request, null, observationManager, loggerManager);

        // We are not ready to isolate asynchronous renderer, plus it's not stored right now so the log would be lost.
        setIsolated(false);

        this.async = true;
    }

    /**
     * @param request the request
     * @param result the result of the renderer execution
     */
    public AsyncRendererJobStatus(AsyncRendererJobRequest request, AsyncRendererResult result)
    {
        super(JOBTYPE, request, null, null, null);

        this.async = false;

        setResult(result);

        setState(State.FINISHED);
        setEndDate(new Date());
    }

    /**
     * @param request the request
     * @param result the result of the renderer execution
     * @param references the involved references
     * @param roleTypes the involved components types
     * @param roles the involved components
     * @param rights
     * @since 11.8RC1
     */
    AsyncRendererJobStatus(AsyncRendererJobRequest request, AsyncRendererResult result, Set<EntityReference> references,
        Set<Type> roleTypes, Set<ComponentRole<?>> roles, Set<RightEntry> rights, Map<String, Collection<Object>> uses)
    {
        super(JOBTYPE, request, null, null, null);

        this.async = false;

        setResult(result);
        setReferences(references);
        setRoleTypes(roleTypes);
        setRoles(roles);
        setRights(rights);
        setUses(uses);

        setState(State.FINISHED);
        setEndDate(new Date());
    }

    /**
     * @return true if this status is associated to an asynchronous execution
     * @since 10.10
     */
    public boolean isAsync()
    {
        return async;
    }

    /**
     * @return the result of the execution
     */
    public AsyncRendererResult getResult()
    {
        return this.result;
    }

    /**
     * @param result the result of the renderer execution
     */
    void setResult(AsyncRendererResult result)
    {
        this.result = result;
    }

    /**
     * @return the references
     */
    public Set<EntityReference> getReferences()
    {
        return this.references != null ? this.references : Collections.emptySet();
    }

    /**
     * @param references the references to invalidate the cache
     * @since 10.10
     */
    void setReferences(Set<EntityReference> references)
    {
        if (references != null) {
            this.references = Collections.unmodifiableSet(references);
        }
    }

    /**
     * @return the types of the components to invalidate the cache
     */
    public Set<Type> getRoleTypes()
    {
        return this.roleTypes != null ? this.roleTypes : Collections.emptySet();
    }

    /**
     * @param roleTypes the types of the components to invalidate the cache
     */
    void setRoleTypes(Set<Type> roleTypes)
    {
        this.roleTypes = roleTypes;
    }

    /**
     * @return the components to invalidate the cache
     */
    public Set<ComponentRole<?>> getRoles()
    {
        return this.roles != null ? this.roles : Collections.emptySet();
    }

    /**
     * @param roles the components to invalidate the cache
     */
    void setRoles(Set<ComponentRole<?>> roles)
    {
        if (roles != null) {
            this.roles = Collections.unmodifiableSet(roles);
        }
    }

    /**
     * @return the right checks to invalidate the cache
     * @since 11.8RC1
     */
    public Set<RightEntry> getRights()
    {
        return this.rights != null ? this.rights : Collections.emptySet();
    }

    /**
     * @param rights the right checks to invalidate the cache
     * @since 11.8RC1
     */
    void setRights(Set<RightEntry> rights)
    {
        if (rights != null) {
            this.rights = Collections.unmodifiableSet(rights);
        }
    }

    /**
     * @return the custom uses values
     */
    public Map<String, Collection<Object>> getUses()
    {
        return this.uses;
    }

    /**
     * @param uses the custom uses values
     */
    public void setUses(Map<String, Collection<Object>> uses)
    {
        this.uses = uses;
    }

    /**
     * @param client the identifier of the client to associated to this status
     * @since 10.11.5
     * @since 11.3RC1
     */
    public void addClient(String client)
    {
        this.clients.add(client);
    }

    /**
     * @return the clients
     * @since 10.11.5
     * @since 11.3RC1
     */
    public Set<String> getClients()
    {
        return this.clients;
    }

    /**
     * Remove stuff which are not required in the cache (to spare some memory).
     */
    void dispose()
    {
        getRequest().setContext(null);
        getRequest().setRenderer(null);
    }
}
