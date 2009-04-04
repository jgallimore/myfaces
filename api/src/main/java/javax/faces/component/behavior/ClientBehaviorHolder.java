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
package javax.faces.component.behavior;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TODO: COMMENT
 * 
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 15:15:08 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public interface ClientBehaviorHolder
{
    public void addClientBehavior(String eventName, ClientBehavior behavior);
    
    public Map<String,List<ClientBehavior>> getClientBehaviors();
    
    public String getDefaultEventName();
    
    public Collection<String> getEventNames();
}
