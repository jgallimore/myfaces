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

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;


/**
 * see Javadoc of JSF Specification
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UIComponentBase
        extends UIComponent
{
    private _ComponentAttributesMap _attributesMap = null;
    private Map _valueBindingMap = null;
    private List _childrenList = null;
    private Map _facetMap = null;
    private List _facesListeners = null;
    private String _clientId = null;
    private String _id = null;
    private UIComponent _parent = null;
    private boolean _transient = false;

    public UIComponentBase()
    {
    }

    public Map getAttributes()
    {
        if (_attributesMap == null)
        {
            _attributesMap = new _ComponentAttributesMap(this);
        }
        return _attributesMap;
    }

    public ValueBinding getValueBinding(String name)
    {
        if (name == null) throw new NullPointerException("name");
        if (_valueBindingMap == null)
        {
            return null;
        }
        else
        {
            return (ValueBinding)_valueBindingMap.get(name);
        }
    }

    public void setValueBinding(String name,
                                ValueBinding binding)
    {
        if (name == null) throw new NullPointerException("name");
        if (_valueBindingMap == null)
        {
            _valueBindingMap = new HashMap();
        }
        _valueBindingMap.put(name, binding);
    }

    /**
     * TODO: Can we be sure that the id was already set?
     * @param context
     * @return
     */
    public String getClientId(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (_clientId != null) return _clientId;
        UIComponent namingContainer = _ComponentUtils.findParentNamingContainer(this, false);
        if (namingContainer != null)
        {
            _clientId = namingContainer.getClientId(context) + NamingContainer.SEPARATOR_CHAR + getId();
        }
        else
        {
            _clientId = getId();
        }
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            _clientId = renderer.convertClientId(context, _clientId);
        }
        return _clientId;
    }

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        //TODO: check id according to javadoc
        _id = id;
        _clientId = null;
    }

    public UIComponent getParent()
    {
        return _parent;
    }

    public void setParent(UIComponent parent)
    {
        _parent = parent;
    }

    public boolean getRendersChildren()
    {
        Renderer renderer = getRenderer(getFacesContext());
        if (renderer != null)
        {
            return renderer.getRendersChildren();
        }
        else
        {
            return false;
        }
    }

    public List getChildren()
    {
        if (_childrenList == null)
        {
            _childrenList = new _ComponentChildrenList(this);
        }
        return _childrenList;
    }

    public int getChildCount()
    {
        return _childrenList == null ? 0 : _childrenList.size();
    }


    /**
     * @param expr
     * @return
     */
    public UIComponent findComponent(String expr)
    {
        if (expr == null) throw new NullPointerException("expr");
        if (expr.length() == 0) throw new IllegalArgumentException("empty expr"); //TODO: not specified!

        UIComponent findBase;
        if (expr.charAt(0) == NamingContainer.SEPARATOR_CHAR)
        {
            findBase = _ComponentUtils.getRootComponent(this);
            expr = expr.substring(1);
        }
        else
        {
            if (this instanceof NamingContainer)
            {
                findBase = this;
            }
            else
            {
                findBase = _ComponentUtils.findParentNamingContainer(this, true /* root if not found */);
            }
        }

        int separator = expr.indexOf(NamingContainer.SEPARATOR_CHAR);
        if (separator == -1)
        {
            return _ComponentUtils.findComponent(findBase, expr);
        }
        else
        {
            findBase = _ComponentUtils.findComponent(findBase, expr.substring(0, separator));
            if (findBase == null)
            {
                return null;
            }
            else
            {
                return findBase.findComponent(expr.substring(separator + 1));
            }
        }
    }


    public Map getFacets()
    {
        if (_facetMap == null)
        {
            _facetMap = new _ComponentFacetMap(this);
        }
        return _facetMap;
    }

    public UIComponent getFacet(String name)
    {
        return _facetMap == null ? null : (UIComponent)_facetMap.get(name);
    }

    public Iterator getFacetsAndChildren()
    {
        return new _FacetsAndChildrenIterator(_facetMap, _childrenList);
    }

    public void broadcast(FacesEvent event)
            throws AbortProcessingException
    {
        if (event == null) throw new NullPointerException("event");
        if (_facesListeners == null) return;
        for (Iterator it = _facesListeners.iterator(); it.hasNext(); )
        {
            FacesListener facesListener = (FacesListener)it.next();
            if (event.isAppropriateListener(facesListener))
            {
                event.processListener(facesListener);
            }
        }
    }

    public void decode(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.decode(context, this);
        }
    }

    public void encodeBegin(FacesContext context)
            throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.encodeBegin(context, this);
        }
    }

    public void encodeChildren(FacesContext context)
            throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.encodeChildren(context, this);
        }
    }

    public void encodeEnd(FacesContext context)
            throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.encodeEnd(context, this);
        }
    }

    protected void addFacesListener(FacesListener listener)
    {
        if (listener == null) throw new NullPointerException("listener");
        if (_facesListeners == null)
        {
            _facesListeners = new ArrayList();
        }
        _facesListeners.add(listener);
    }

    protected FacesListener[] getFacesListeners(Class clazz)
    {
        if (_facesListeners == null)
        {
            return (FacesListener[])Array.newInstance(clazz, 0);
        }
        List lst = null;
        for (Iterator it = _facesListeners.iterator(); it.hasNext(); )
        {
            FacesListener facesListener = (FacesListener)it.next();
            if (clazz.isAssignableFrom(facesListener.getClass()))
            {
                if (lst == null) lst = new ArrayList();
                lst.add(facesListener);
            }
        }
        if (lst == null)
        {
            return (FacesListener[])Array.newInstance(clazz, 0);
        }
        else
        {
            return (FacesListener[])lst.toArray((FacesListener[])Array.newInstance(clazz, lst.size()));
        }
    }

    protected void removeFacesListener(FacesListener listener)
    {
        if (_facesListeners != null)
        {
            _facesListeners.remove(listener);
        }
    }

    public void queueEvent(FacesEvent event)
    {
        if (event == null) throw new NullPointerException("event");
        UIComponent parent = getParent();
        if (parent == null)
        {
            throw new IllegalStateException("component is not a descendant of a UIViewRoot");
        }
        parent.queueEvent(event);
    }

    public void processDecodes(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processDecodes(context);
        }
        try
        {
            decode(context);
        }
        catch (RuntimeException e)
        {
            context.renderResponse();
            throw e;
        }
    }

    public void processValidators(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processValidators(context);
        }
    }

    public void processUpdates(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processUpdates(context);
        }
    }

    public Object processSaveState(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (isTransient()) return null;
        Map facetMap = null;
        for (Iterator it = getFacets().entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)it.next();
            if (facetMap == null) facetMap = new HashMap();
            facetMap.put(entry.getKey(),
                         ((UIComponent)entry.getValue()).processSaveState(context));
        }
        List childrenList = null;
        if (getChildCount() > 0)
        {
            for (Iterator it = getChildren().iterator(); it.hasNext(); )
            {
                UIComponent child = (UIComponent)it.next();
                if (childrenList == null) childrenList = new ArrayList(getChildCount());
                childrenList.add(child.processSaveState(context));
            }
        }
        return new Object[] {saveState(context),
                             facetMap,
                             childrenList};
    }

    public void processRestoreState(FacesContext context,
                                    Object state)
    {
        if (context == null) throw new NullPointerException("context");
        Object myState = ((Object[])state)[0];
        Map facetMap = (Map)((Object[])state)[1];
        List childrenList = (List)((Object[])state)[2];
        for (Iterator it = getFacets().entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)it.next();
            Object facetState = facetMap.get(entry.getKey());
            ((UIComponent)entry.getValue()).processRestoreState(context, facetState);
        }
        if (getChildCount() > 0)
        {
            int idx = 0;
            for (Iterator it = getChildren().iterator(); it.hasNext(); )
            {
                UIComponent child = (UIComponent)it.next();
                Object childState = childrenList.get(idx++);
                child.processRestoreState(context, childState);
            }
        }
        restoreState(context, myState);
    }

    protected FacesContext getFacesContext()
    {
        return FacesContext.getCurrentInstance();
    }

    protected Renderer getRenderer(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        String rendererType = getRendererType();
        if (rendererType == null) return null;
        String renderKitId = context.getViewRoot().getRenderKitId();
        RenderKitFactory rkf = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = rkf.getRenderKit(context, renderKitId);
        return renderKit.getRenderer(getFamily(), rendererType);
    }

    public boolean isTransient()
    {
        return _transient;
    }

    public void setTransient(boolean transientFlag)
    {
        _transient = transientFlag;
    }


    public static Object saveAttachedState(FacesContext context,
                                           Object attachedObject)
    {
        if (attachedObject == null) return null;
        if (attachedObject instanceof List)
        {
            ArrayList lst = new ArrayList(((List)attachedObject).size());
            for (Iterator it = ((List)attachedObject).iterator(); it.hasNext(); )
            {
                lst.add(saveAttachedState(context, it.next()));
            }
            return new _AttachedListStateWrapper(lst);
        }
        else if (attachedObject instanceof StateHolder)
        {
            if (((StateHolder)attachedObject).isTransient())
            {
                return null;
            }
            else
            {
                return new _AttachedStateWrapper(attachedObject.getClass(),
                                                 ((StateHolder)attachedObject).saveState(context));
            }
        }
        else if (attachedObject instanceof Serializable)
        {
            return attachedObject;
        }
        else
        {
            return new _AttachedStateWrapper(attachedObject.getClass(), null);
        }
    }

    public static Object restoreAttachedState(FacesContext context,
                                              Object stateObj)
            throws IllegalStateException
    {
        if (context == null) throw new NullPointerException("context");
        if (stateObj == null) return null;
        if (stateObj instanceof _AttachedListStateWrapper)
        {
            List lst = ((_AttachedListStateWrapper)stateObj).getWrappedStateList();
            List restoredList = new ArrayList(lst.size());
            for (Iterator it = lst.iterator(); it.hasNext(); )
            {
                restoredList.add(restoreAttachedState(context, it.next()));
            }
            return restoredList;
        }
        else if (stateObj instanceof _AttachedStateWrapper)
        {
            Class clazz = ((_AttachedStateWrapper)stateObj).getClazz();
            Object restoredObject = null;
            try
            {
                restoredObject = clazz.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            if (restoredObject instanceof StateHolder)
            {
                Object wrappedState = ((_AttachedStateWrapper)stateObj).getWrappedStateObject();
                ((StateHolder)restoredObject).restoreState(context, wrappedState);
            }
            return restoredObject;
        }
        else
        {
            return stateObj;
        }
    }


    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[7];
        values[0] = _id;
        values[1] = _rendered;
        values[2] = _rendererType;
        values[3] = _clientId;
        values[4] = saveAttributesMap();
        values[5] = saveAttachedState(context, _facesListeners);
        values[6] = saveValueBindingMap(context);
        return ((Object) (values));
    }

    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        _id = (String)values[0];
        _rendered = (Boolean)values[1];
        _rendererType = (String)values[2];
        _clientId = (String)values[3];
        restoreAttributesMap(values[4]);
        _facesListeners = (List)restoreAttachedState(context, values[5]);
        restoreValueBindingMap(context, values[6]);
    }


    private Object saveAttributesMap()
    {
        if (_attributesMap != null)
        {
            return _attributesMap.getUnderlyingMap();
        }
        else
        {
            return null;
        }
    }

    private void restoreAttributesMap(Object stateObj)
    {
        if (stateObj != null)
        {
            _attributesMap = new _ComponentAttributesMap(this, (Map)stateObj);
        }
        else
        {
            _attributesMap = null;
        }
    }

    private Object saveValueBindingMap(FacesContext context)
    {
        if (_valueBindingMap != null)
        {
            int initCapacity = (_valueBindingMap.size() * 4 + 3) / 3;
            HashMap stateMap = new HashMap(initCapacity);
            for (Iterator it = _valueBindingMap.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)it.next();
                stateMap.put(entry.getKey(),
                             saveAttachedState(context, entry.getValue()));
            }
            return stateMap;
        }
        else
        {
            return null;
        }
    }

    private void restoreValueBindingMap(FacesContext context, Object stateObj)
    {
        if (stateObj != null)
        {
            Map stateMap = (Map)stateObj;
            int initCapacity = (stateMap.size() * 4 + 3) / 3;
            _valueBindingMap = new HashMap(initCapacity);
            for (Iterator it = stateMap.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)it.next();
                _valueBindingMap.put(entry.getKey(),
                                     restoreAttachedState(context, entry.getValue()));
            }
        }
        else
        {
            _valueBindingMap = null;
        }
    }


    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    private static final boolean DEFAULT_RENDERED = true;

    private Boolean _rendered = null;
    private String _rendererType = null;



    public void setRendered(boolean rendered)
    {
        _rendered = Boolean.valueOf(rendered);
    }

    public boolean isRendered()
    {
        if (_rendered != null) return _rendered.booleanValue();
        ValueBinding vb = getValueBinding("rendered");
        Boolean v = vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
        return v != null ? v.booleanValue() : DEFAULT_RENDERED;
    }

    public void setRendererType(String rendererType)
    {
        _rendererType = rendererType;
    }

    public String getRendererType()
    {
        if (_rendererType != null) return _rendererType;
        ValueBinding vb = getValueBinding("rendererType");
        return vb != null ? (String)vb.getValue(getFacesContext()) : null;
    }


    //------------------ GENERATED CODE END ---------------------------------------
}
