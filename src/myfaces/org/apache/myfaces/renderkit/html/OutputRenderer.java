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

import net.sourceforge.myfaces.renderkit.html.util.HTMLEncoder;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

/**
 * TODO: description
 * @author Manfred Geiler
 * @version $Revision$ $Date$
 */
public class OutputRenderer
        extends HTMLRenderer
{
    public static final String TYPE = "OutputRenderer";
    public String getRendererType()
    {
        return TYPE;
    }

    public void encodeEnd(FacesContext facescontext, UIComponent uicomponent)
            throws IOException
    {
        ResponseWriter writer = facescontext.getResponseWriter();
        writer.write(HTMLEncoder.encode(getStringValue(facescontext, uicomponent), false, false));
    }

    public boolean supportsComponentType(String s)
    {
        return s.equals(UIOutput.TYPE);
    }

    public boolean supportsComponentType(UIComponent uicomponent)
    {
        return uicomponent instanceof UIOutput;
    }
}
