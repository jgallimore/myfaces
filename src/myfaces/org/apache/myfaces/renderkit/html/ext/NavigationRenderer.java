/**
 * MyFaces - the free JSF implementation
 * Copyright (C) 2003  The MyFaces Team (http://myfaces.sourceforge.net)
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
package net.sourceforge.myfaces.renderkit.html.ext;

import net.sourceforge.myfaces.component.UIComponentUtils;
import net.sourceforge.myfaces.component.ext.UINavigation;
import net.sourceforge.myfaces.renderkit.html.HTMLRenderer;
import net.sourceforge.myfaces.renderkit.html.util.HTMLEncoder;
import net.sourceforge.myfaces.renderkit.attr.ext.NavigationRendererAttributes;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class NavigationRenderer
    extends HTMLRenderer
    implements NavigationRendererAttributes
{
    public static final String GET_CHILDREN_FROM_REQUEST_ATTR
        = NavigationRenderer.class.getName() + ".GET_CHILDREN_FROM_REQUEST";

    public static final String TYPE = "Navigation";
    public String getRendererType()
    {
        return TYPE;
    }

    public boolean supportsComponentType(String s)
    {
        return s.equals(UINavigation.TYPE);
    }

    public boolean supportsComponentType(UIComponent uiComponent)
    {
        return uiComponent instanceof UINavigation;
    }


    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.write("<table border=\"0\">");
    }

    public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.write("</table>");
    }

    public void encodeChildren(FacesContext facesContext, UIComponent uiComponent)
        throws IOException
    {
        Boolean b = (Boolean)facesContext.getResponseTree().getRoot().getAttribute(GET_CHILDREN_FROM_REQUEST_ATTR);
        facesContext.getResponseTree().getRoot().setAttribute(GET_CHILDREN_FROM_REQUEST_ATTR, null);
        if (b != null && b.booleanValue())
        {
            //Remove all children:
            while(uiComponent.getChildCount() > 0)
            {
                uiComponent.removeChild(0);
            }

            //Add all children from same component in request
            UIComponent requestNav
                = facesContext.getRequestTree().getRoot()
                    .findComponent(uiComponent.getCompoundId());
            for (Iterator it = requestNav.getChildren(); it.hasNext();)
            {
                uiComponent.addChild((UIComponent)it.next());
            }
        }
        renderChildren(facesContext, 0, uiComponent.getChildren());
    }

    protected void renderChildren(FacesContext facesContext, int level, Iterator children)
        throws IOException
    {
        while(children.hasNext())
        {
            UIComponent child = (UIComponent)children.next();
            if (child.getRendererType().equals(NavigationItemRenderer.TYPE))
            {
                renderMenuItem(facesContext, level, child);
            }
            else
            {
                throw new FacesException("Unexpected component of renderer type " + child.getRendererType());
            }
        }
    }

    protected void renderMenuItem(FacesContext facesContext, int level, UIComponent item)
        throws IOException
    {
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.write("\n<tr><td>");
        for (int i = 0; i < level; i++)
        {
            writer.write("&nbsp;");
        }
        if (level > 0)
        {
            writer.write("<font size=\"-" + level + "\">");
        }

        item.encodeBegin(facesContext);
        item.encodeEnd(facesContext);

        if (level > 0)
        {
            writer.write("</font>");
        }
        writer.write("</td></tr>");

        if (UIComponentUtils.getBooleanAttribute(item,
                                                 UINavigation.UINavigationItem.OPEN_ATTR,
                                                 false))
        {
            renderChildren(facesContext, level + 1, item.getChildren());
        }

    }


}
