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
package net.sourceforge.myfaces.webapp;

import net.sourceforge.myfaces.util.logging.LogUtil;

import javax.faces.FactoryFinder;
import javax.faces.lifecycle.ApplicationHandler;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;

public class DefaultServletContextListener
        implements ServletContextListener
{
    public DefaultServletContextListener()
    {
    }

    public void contextInitialized(ServletContextEvent e)
    {
        try
        {
            //Set logging level
            LogUtil.getLogger().setLevel(Level.FINE);

            ApplicationHandler handler = new DefaultApplicationHandler();
            LifecycleFactory factory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
            lifecycle.setApplicationHandler(handler);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        LogUtil.getLogger().info("ServletContext '" + e.getServletContext().getRealPath("/") + "' initialized.");
    }

    public void contextDestroyed(ServletContextEvent e)
    {
    }

}
