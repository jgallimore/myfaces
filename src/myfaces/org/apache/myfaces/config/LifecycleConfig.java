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
package net.sourceforge.myfaces.config;

import javax.faces.event.PhaseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


/**
 * @author Anton Koinov (latest modification by $Author$)
 * @author Thomas Spiegl*
 * @version $Revision$ $Date$
 */
public class LifecycleConfig
    implements Config
{
    //~ Instance fields ----------------------------------------------------------------------------

    private ArrayList _phaseListeners;

    //~ Methods ------------------------------------------------------------------------------------

    public void addPhaseListenerClasses(Iterator phaseListenerClasses)
    {
        while (phaseListenerClasses.hasNext())
        {
            _phaseListeners.add(phaseListenerClasses.next());
        }
    }

    public void addPhaseListener(PhaseListener phaseListener)
    {
            if (_phaseListeners == null)
            {
                _phaseListeners = new ArrayList();
            }
            _phaseListeners.add(phaseListener);
    }

    public Iterator getPhaseListenerClasses()
    {
        if (_phaseListeners == null)
        {
            return Collections.EMPTY_LIST.iterator();
        }
        return _phaseListeners.iterator();
    }
}
