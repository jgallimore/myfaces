/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.custom.aliasbean;

import javax.faces.component.UIComponent;

import org.apache.myfaces.taglib.UIComponentTagBase;

/**
 * @author Sylvain Vieujot (latest modification by $Author$)
 * @version $Revision$ $Date$
 * $Log$
 * Revision 1.2  2004/11/23 04:46:40  svieujot
 * Add an ugly "permanent" tag to x:aliasBean to handle children events.
 *
 * Revision 1.1  2004/11/08 20:43:15  svieujot
 * Add an x:aliasBean component
 *
 */
public class AliasBeanTag extends UIComponentTagBase {
    
    private String _sourceBean;
    private String _alias;
    private String _permanent;
    
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setStringProperty(component, "sourceBean", _sourceBean);
        setStringProperty(component, "alias", _alias);
        setBooleanProperty(component, "permanent", _permanent);
    }
    
    public String getComponentType() {
        return AliasBean.COMPONENT_TYPE;
    }

    public String getRendererType() {
        return null;
    }
    
    public void setSourceBean(String sourceBean){
        _sourceBean = sourceBean;
    }
    
    public void setAlias(String alias){
        _alias = alias;
    }
    
    public void setPermanent(String permanent){
        _permanent = permanent;
    }
}