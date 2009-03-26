/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.context;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionEvent;
import javax.faces.event.ExceptionEventContext;
import javax.faces.event.SystemEvent;
import javax.servlet.ServletException;

/**
 * DOCUMENT ME!
 * 
 * @author (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-09-24 18:31:37 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class ExceptionHandlerImpl extends ExceptionHandler
{
    private static final String UNHANDLED_QUEUE = "unhandled" + ExceptionHandlerImpl.class.getName();
    private static final String HANDLED_QUEUE = "handled" + ExceptionHandlerImpl.class.getName();

    private static final Queue<ExceptionEvent> EMPTY_QUEUE = new ArrayDeque<ExceptionEvent>(0);

    @Override
    public ExceptionEvent getHandledExceptionEvent()
    {
        return getUnhandledQueue(false).poll();
    }

    @Override
    public Iterable<ExceptionEvent> getHandledExceptionEvents()
    {
        return getHandledQueue(false);
    }

    @Override
    public Throwable getRootCause(Throwable t)
    {
        if ((t instanceof FacesException) || (t instanceof ELException))
        {
            Throwable cause = t.getCause();
            if (cause != null)
            {
                return getRootCause(cause);
            }
        }

        return t;
    }

    @Override
    public Iterable<ExceptionEvent> getUnhandledExceptionEvents()
    {
        return getUnhandledQueue(false);
    }

    @Override
    public void handle() throws FacesException
    {
        /*
         * The default implementation must take the first ExceptionEvent queued from a call to
         * processEvent(javax.faces.event.SystemEvent), unwrap it with a call to getRootCause(java.lang.Throwable),
         * re-wrap it in a ServletException and re-throw it. The default implementation must take special action in the
         * following cases.
         * 
         * If an unchecked Exception occurs as a result of calling a method annotated with PreDestroy on a managed bean,
         * the Exception must be logged and swallowed.
         * 
         * If the Exception originates inside the ELContextListener.removeElContextListener, the Exception must be
         * logged and swallowed.
         */
        ExceptionEvent event = getHandledExceptionEvent();
        if (event != null)
        {
            getHandledQueue(true).add(event);

            ExceptionEventContext exceptionContext = (ExceptionEventContext) event.getSource();

            // FIXME This method must throw ServletException
            throw new FacesException(new ServletException(getRootCause(exceptionContext.getException())));
        }
    }

    @Override
    public boolean isListenerForSource(Object source)
    {
        return source instanceof ExceptionEvent;
    }

    @Override
    public void processEvent(SystemEvent exceptionEvent)
    {
        getUnhandledQueue(true).add((ExceptionEvent) exceptionEvent);
    }

    private Queue<ExceptionEvent> getHandledQueue(boolean create)
    {
        return getQueue(HANDLED_QUEUE, create);
    }

    private Queue<ExceptionEvent> getUnhandledQueue(boolean create)
    {
        return getQueue(UNHANDLED_QUEUE, create);
    }

    @SuppressWarnings("unchecked")
    private Queue<ExceptionEvent> getQueue(String queueName, boolean create)
    {
        assert queueName != null;

        FacesContext context = FacesContext.getCurrentInstance();

        Map<Object, Object> attributes = context.getAttributes();

        Queue<ExceptionEvent> queue = (Queue<ExceptionEvent>) attributes.get(queueName);

        if (queue == null)
        {
            if (create)
            {
                queue = new ArrayDeque<ExceptionEvent>(1);

                attributes.put(queueName, queue);
            }
            else
            {
                queue = EMPTY_QUEUE;
            }
        }

        return queue;
    }

}