/**
 * MyFaces - the free JSF implementation
 * Copyright (C) 2002 Manfred Geiler, Thomas Spiegl
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
package net.sourceforge.myfaces.renderkit.html;

import net.sourceforge.myfaces.component.*;
import net.sourceforge.myfaces.renderkit.html.util.HTMLEncoder;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIComponent;
import javax.faces.component.SelectItem;
import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * TODO: description
 * @author Thomas Spiegl
 * @version $Revision$ $Date$
 */
public class SelectManyOptionRenderer
        extends AbstractSelectOptionRenderer
{
    public static final String TYPE = "SelectManyOptionRenderer";

    public String getRendererType()
    {
        return TYPE;
    }

    public boolean supportsComponentType(String s)
    {
        return s.equals(UISelectMany.TYPE);
    }

    public boolean supportsComponentType(UIComponent uicomponent)
    {
        return uicomponent instanceof UISelectOne;
    }

    public void encodeBegin(FacesContext facescontext, UIComponent uicomponent)
            throws IOException
    {
        if (uicomponent.getComponentType() != UISelectMany.TYPE)
        {
            throw new IllegalArgumentException("UIComponent must be of type " + UISelectMany.class.getName());
        }
    }

    public void encodeEnd(FacesContext facescontext, UIComponent uicomponent)
        throws IOException
    {
        super.encodeEnd(facescontext, uicomponent, true);
    }

}
