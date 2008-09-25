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
package javax.faces.webapp.pdl;

import java.beans.BeanInfo;

import javax.faces.application.Resource;
import javax.faces.context.FacesContext;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2008-09-24 19:54:04 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public abstract class PageDeclarationLanguage
{
    public abstract BeanInfo getComponentMetadata(FacesContext context, Resource componentResource);
    
    public abstract Resource getScriptComponentResource(FacesContext context, Resource componentResource);
}
