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
package net.sourceforge.myfaces.lifecycle;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import java.io.IOException;

/**
 * TODO: description
 * @author Manfred Geiler
 * @version $Revision$ $Date$
 */
public class ApplyRequestValuesPhase
        extends AbstractPhase
{
    private Lifecycle _lifecycle;
    public ApplyRequestValuesPhase(Lifecycle lifecycle)
    {
        _lifecycle = lifecycle;
    }

    public int execute(FacesContext facescontext) throws FacesException
    {
        return execute(facescontext, _action);
    }

    private PhaseAction _action = new PhaseAction() {
        public int doActionForComponent(FacesContext facescontext, UIComponent uicomponent)
                throws FacesException
        {
            try
            {
                uicomponent.decode(facescontext);
            }
            catch (IOException e)
            {
                throw new FacesException(e);
            }
            return GOTO_NEXT;
        }
    };
}
