/*
 * MyFaces - the free JSF implementation
 * Copyright (C) 2003, 2004  The MyFaces Team (http://myfaces.sourceforge.net)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package javax.faces.component;

import java.util.Iterator;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIForm
        extends UIComponentBase
{
    //private static final Log log = LogFactory.getLog(UIForm.class);

    private boolean _submitted;

    public boolean isSubmitted()
    {
        return _submitted;
    }

    public void setSubmitted(boolean submitted)
    {
        _submitted = submitted;
    }

    public void processDecodes(javax.faces.context.FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        decode(context);
        if (isSubmitted())
        {
            for (Iterator it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent)it.next();
                child.processDecodes(context);
            }
        }
    }

    public void processValidators(javax.faces.context.FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (isSubmitted())
        {
            for (Iterator it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent)it.next();
                child.processValidators(context);
            }
        }
    }

    public void processUpdates(javax.faces.context.FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (isSubmitted())
        {
            for (Iterator it = getChildren().iterator(); it.hasNext();)
            {
                UIComponent child = (UIComponent)it.next();
                child.processUpdates(context);
            }
        }
    }

    public Object saveState(javax.faces.context.FacesContext context)
    {
        _submitted = false;
        return super.saveState(context);
    }

    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    public static final String COMPONENT_TYPE = "javax.faces.Form";
    public static final String COMPONENT_FAMILY = "javax.faces.Form";
    private static final String DEFAULT_RENDERER_TYPE = "javax.faces.Form";


    public UIForm()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }


    //------------------ GENERATED CODE END ---------------------------------------
}
