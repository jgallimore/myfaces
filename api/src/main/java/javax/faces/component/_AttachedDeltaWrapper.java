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
package javax.faces.component;

import java.io.Serializable;

/**
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
class _AttachedDeltaWrapper implements Serializable
{
    private static final long serialVersionUID = 4732389964367986402L;

    private Object _wrappedStateObject;

    /**
     * @param clazz
     *            null means wrappedStateObject is a List of state objects
     * @param wrappedStateObject
     */
    public _AttachedDeltaWrapper(Class<?> clazz, Object wrappedStateObject)
    {
        if (wrappedStateObject != null && !(wrappedStateObject instanceof Serializable))
        {
            throw new IllegalArgumentException("Attached state for Object of type " + clazz + " (Class "
                    + wrappedStateObject.getClass().getName() + ") is not serializable");
        }
        _wrappedStateObject = wrappedStateObject;
    }

    public Object getWrappedStateObject()
    {
        return _wrappedStateObject;
    }
}
