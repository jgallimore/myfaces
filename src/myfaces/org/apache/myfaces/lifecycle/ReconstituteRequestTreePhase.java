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

import net.sourceforge.myfaces.renderkit.html.state.StateRenderer;
import net.sourceforge.myfaces.util.logging.LogUtil;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import javax.faces.tree.Tree;
import javax.faces.tree.TreeFactory;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * TODO: description
 * @author Manfred Geiler
 * @version $Revision$ $Date$
 */
public class ReconstituteRequestTreePhase
        extends AbstractPhase
{
    private Lifecycle _lifecycle;
    private TreeFactory _treeFactory;

    public ReconstituteRequestTreePhase(Lifecycle lifecycle)
    {
        _lifecycle = lifecycle;
        _treeFactory = (TreeFactory)FactoryFinder.getFactory(FactoryFinder.TREE_FACTORY);
    }

    public int execute(FacesContext facesContext)
            throws FacesException
    {
        //Create tree
        HttpServletRequest request = (HttpServletRequest)facesContext.getServletRequest();
        String treeId = request.getPathInfo();
        Tree requestTree = _treeFactory.getTree(facesContext.getServletContext(), treeId);
        facesContext.setRequestTree(requestTree);

        //Restore state
        RenderKitFactory rkFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = rkFactory.getRenderKit(facesContext.getResponseTree().getRenderKitId());

        Renderer renderer = null;
        try
        {
            renderer = renderKit.getRenderer(StateRenderer.TYPE);
        }
        catch (Exception e)
        {
            //No StateRenderer
        }

        if (renderer != null)
        {
            try
            {
                //LogUtil.logInfo("StateRenderer found, calling decode...");
                renderer.decode(facesContext, null);
            }
            catch (IOException e)
            {
                throw new FacesException("Error restoring state", e);
            }
        }

        return GOTO_NEXT;
    }

}
