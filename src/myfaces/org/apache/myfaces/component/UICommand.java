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
package net.sourceforge.myfaces.component;

import javax.faces.component.UIComponent;
import javax.faces.component.NamingContainer;
import javax.faces.context.FacesContext;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UICommand
    extends javax.faces.component.UICommand
    implements CommonComponentAttributes
{
    public static final String COMMAND_NAME_ATTR = "commandName";

    //MyFaces extensions
    public static final String COMMAND_REFERENCE_ATTR = "commandReference";

    private Map _parameters = null;


    public UICommand()
    {
        setValid(true);
    }

    public void setAttribute(String name, Object value)
    {
        if (name.equals(COMMAND_NAME_ATTR))
        {
            setAttribute(VALUE_ATTR, value);
        }
        else
        {
            super.setAttribute(name, value);
        }
    }

    public Object getAttribute(String name)
    {
        if (name.equals(COMMAND_NAME_ATTR))
        {
            return getAttribute(VALUE_ATTR);
        }
        else
        {
            return super.getAttribute(name);
        }
    }


    public String getCommandReference()
    {
        return (String)getAttribute(COMMAND_REFERENCE_ATTR);
    }

    public void setCommandReference(String commandReference)
    {
        setAttribute(COMMAND_REFERENCE_ATTR, commandReference);
    }


    public List[] getListeners()
    {
        return listeners;
    }






//------------------------------------------------------------------------------
// UIComponentHelper Delegation
// HACK: Delegation, because UIComponentBase does not support Facets properly.
//       (getClientId crashes, etc.)

    private UIComponentHelper _uiComponentHelper = new UIComponentHelper(this);

    public String getClientId(FacesContext context)
    {
        return _uiComponentHelper.getClientId(context);
    }

    public void addFacet(String facetName, UIComponent facet)
    {
        super.addFacet(facetName, facet);
        _uiComponentHelper.addFacet(facetName, facet);
    }

    public UIComponent getParent()
    {
        return _uiComponentHelper.getParent(super.getParent());
    }
//------------------------------------------------------------------------------
}
