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


package net.sourceforge.myfaces.custom.tree;

import net.sourceforge.myfaces.custom.tree.model.TreeModel;
import net.sourceforge.myfaces.custom.tree.model.TreePath;

import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;
import java.util.Iterator;


/**
 * Represents a single node of a three. A custom html link component representing the expand/collapse icon
 * is held as a facet named <code>expandCollapse</code>.
 *
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision$ $Date$
 *          $Log$
 *          Revision 1.1  2004/04/22 10:20:23  manolito
 *          tree component
 *
 */
public class HtmlTreeNode
        extends HtmlCommandLink
{

    private static final String DEFAULT_RENDERER_TYPE = "net.sourceforge.myfaces.HtmlTreeNode";

    public static final String COMPONENT_TYPE = "net.sourceforge.myfaces.HtmlTreeNode";
    public static final String EXPAND_COLLAPSE_FACET = "expandCollapse";

    public static final int OPEN = 0;
    public static final int OPEN_FIRST = 1;
    public static final int OPEN_LAST = 2;
    public static final int OPEN_SINGLE = 3;
    public static final int CLOSED = 10;
    public static final int CLOSED_FIRST = 11;
    public static final int CLOSED_LAST = 12;
    public static final int CLOSED_SINGLE = 13;
    public static final int CHILD = 20;
    public static final int CHILD_FIRST = 21;
    public static final int CHILD_LAST = 22;
    public static final int CHILD_SINGLE = 23;
    public static final int LINE = 30;
    public static final int EMPTY = 40;
    private static final int OFFSET = 10;

    private transient TreePath path;
    private int[] translatedPath;
    private transient Object userObject;
    private boolean expanded = false;
    private boolean selected = false;
    private int[] layout;


    public HtmlTreeNode()
    {
        setRendererType(DEFAULT_RENDERER_TYPE);
    }


    public int getLevel()
    {
        return layout.length - 1;
    }


    public int getMaxChildLevel()
    {
        if (getChildCount() == 0)
        {
            return getLevel();
        }

        int max = getLevel();
        for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
        {
            HtmlTreeNode child = (HtmlTreeNode)iterator.next();
            max = Math.max(max, child.getMaxChildLevel());
        }
        return max;
    }


    public TreePath getPath()
    {
        if (path == null)
        {
            if (translatedPath == null)
            {
                throw new IllegalStateException("No path and no translated path available");
            }
            path = translatePath(translatedPath, getTreeModel(FacesContext.getCurrentInstance()));
        }
        return path;
    }


    public void setPath(TreePath path)
    {
        this.path = path;
    }


    public Object getUserObject()
    {
        if (userObject == null)
        {
            userObject = getPath().getLastPathComponent();
        }
        return userObject;
    }


    public void setUserObject(Object userObject)
    {
        this.userObject = userObject;
        setValue(userObject.toString());
    }


    public boolean isExpanded()
    {
        return expanded;
    }


    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;

        if (expanded)
        {
            FacesContext context = FacesContext.getCurrentInstance();
            TreeModel model = getTreeModel(context);
            int childCount = model.getChildCount(getUserObject());

            for (int i = 0; i < childCount; i++)
            {
                Object child = model.getChild(getUserObject(), i);
                HtmlTreeNode node = (HtmlTreeNode)context.getApplication().createComponent(HtmlTreeNode.COMPONENT_TYPE);
                String id = context.getViewRoot().createUniqueId();
                node.setId(id);

                node.setPath(getPath().pathByAddingChild(child));
                node.setUserObject(child);
                int state = CHILD;
                int childChildCount = model.getChildCount(child);

                if (childChildCount == 0)
                {

                    if (childCount > 1)
                    {
                        if (i == 0)
                        {
                            state = CHILD;
                        }
                        else if (i == childCount - 1)
                        {
                            state = CHILD_LAST;
                        }
                    }
                    else
                    {
                        state = CHILD_LAST;
                    }
                }
                else
                {
                    if (childCount > 1)
                    {
                        if (i == 0)
                        {
                            state = CLOSED;
                        }
                        else if (i == childCount - 1)
                        {
                            state = CLOSED_LAST;
                        }
                        else
                        {
                            state = CLOSED;
                        }
                    }
                    else
                    {
                        state = CLOSED_LAST;
                    }

                }
                node.setLayout(layout, state);
                getChildren().add(node);
            }
            layout[layout.length - 1] -= OFFSET;
        }
        else
        {
            if (clearSelection())
            {
                // move selection from child to this node
                selected = true;
            }
            getChildren().clear();
            layout[layout.length - 1] += OFFSET;
        }

    }


    public void toggleExpanded()
    {
        setExpanded(!expanded);
    }


    public boolean isSelected()
    {
        return selected;
    }


    public void setSelected(boolean selected)
    {
        getTree().getRootNode().clearSelection();
        this.selected = selected;
    }


    public void toggleSelected()
    {
        setSelected(!selected);
    }


    public boolean clearSelection()
    {
        if (isSelected())
        {
            selected = false;
            return true;
        }
        for (Iterator iterator = getChildren().iterator(); iterator.hasNext();)
        {
            HtmlTreeNode node = (HtmlTreeNode)iterator.next();
            if (node.clearSelection())
            {
                return true;
            }
        }
        return false;
    }


    public int[] getLayout()
    {
        return layout;
    }


    public void setLayout(int[] layout)
    {
        this.layout = layout;
    }


    public void setLayout(int[] parentLayout, int layout)
    {
        this.layout = new int[parentLayout.length + 1];
        this.layout[parentLayout.length] = layout;

        for (int i = 0; i < parentLayout.length; i++)
        {
            int state = parentLayout[i];
            if (state == OPEN || state == OPEN_FIRST || state == CLOSED || state == CLOSED_FIRST || state == CHILD || state == CHILD_FIRST || state == LINE)
            {
                this.layout[i] = LINE;
            }
            else
            {
                this.layout[i] = EMPTY;
            }
        }
    }


    public HtmlTreeImageCommandLink getExpandCollapseCommand(FacesContext context)
    {
        HtmlTreeImageCommandLink command = (HtmlTreeImageCommandLink)getFacet(EXPAND_COLLAPSE_FACET);

        if (command == null)
        {
            command = (HtmlTreeImageCommandLink)context.getApplication().createComponent(HtmlTreeImageCommandLink.COMPONENT_TYPE);
            String id = context.getViewRoot().createUniqueId();
            command.setId(id);
            getFacets().put(EXPAND_COLLAPSE_FACET, command);
        }
        return command;
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[5];
        values[0] = super.saveState(context);
        values[1] = Boolean.valueOf(expanded);
        values[2] = layout;
        values[3] = translatePath(getPath(), getTreeModel(context));
        values[4] = Boolean.valueOf(selected);
        return ((Object)(values));
    }


    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        super.restoreState(context, values[0]);
        expanded = ((Boolean)values[1]).booleanValue();
        layout = (int[])values[2];
        translatedPath = (int[])values[3];
        selected = ((Boolean)values[4]).booleanValue();
    }


    protected int[] translatePath(TreePath treePath, TreeModel model)
    {
        Object[] path = treePath.getPath();
        int[] translated = new int[path.length - 1];

        Object parent = path[0];

        for (int i = 1; i < path.length; i++)
        {
            Object element = path[i];
            translated[i - 1] = model.getIndexOfChild(parent, element);
            parent = element;
        }
        return translated;
    }


    protected TreePath translatePath(int[] path, TreeModel model)
    {
        Object[] translated = new Object[path.length + 1];
        Object parent = model.getRoot();

        translated[0] = parent;

        for (int i = 0; i < path.length; i++)
        {
            int index = path[i];
            translated[i + 1] = model.getChild(parent, index);
            parent = translated[i + 1];
        }
        return new TreePath(translated);
    }


    protected TreeModel getTreeModel(FacesContext context)
    {
        return getTree().getModel(context);
    }


    protected HtmlTree getTree()
    {
        if (getParent() instanceof HtmlTree)
        {
            return (HtmlTree)getParent();
        }
        return ((HtmlTreeNode)getParent()).getTree();
    }


    public boolean isLeaf(FacesContext context)
    {
        return getTreeModel(context).isLeaf(getUserObject());
    }
}
