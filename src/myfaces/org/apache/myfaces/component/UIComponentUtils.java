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

import net.sourceforge.myfaces.MyFacesConfig;
import net.sourceforge.myfaces.convert.Converter;
import net.sourceforge.myfaces.convert.ConverterException;
import net.sourceforge.myfaces.convert.ConverterUtils;
import net.sourceforge.myfaces.convert.impl.StringArrayConverter;
import net.sourceforge.myfaces.renderkit.html.state.StateRenderer;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.Message;
import javax.faces.context.MessageResources;
import javax.faces.context.MessageResourcesFactory;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class UIComponentUtils
{
    private UIComponentUtils() {}

    public static boolean getBooleanAttribute(UIComponent uiComponent,
                                              String attribute,
                                              boolean defaultValue)
    {
        Boolean b = (Boolean)uiComponent.getAttribute(attribute);
        if (b == null)
        {
            return defaultValue;
        }
        else
        {
            return b.booleanValue();
        }
    }

    public static void setBooleanAttribute(UIComponent uiComponent,
                                           String attribute,
                                           boolean value)
    {
        uiComponent.setAttribute(attribute, value ? Boolean.TRUE : Boolean.FALSE);
    }


    public static void setComponentValue(UIComponent uiComponent,
                                         Object newValue)
    {
        uiComponent.setValue(newValue);
        uiComponent.setAttribute(CommonComponentAttributes.STRING_VALUE_ATTR, null);
        uiComponent.setValid(true);
    }


    public static void convertAndSetValue(FacesContext facesContext,
                                          UIComponent uiComponent,
                                          String newValue,
                                          boolean addErrorMessageOnFail)
    {
        Converter conv = ConverterUtils.findConverter(facesContext, uiComponent);
        if (conv == null)
        {
            //default to StringConverter
            conv = ConverterUtils.getConverter(facesContext.getServletContext(), String.class);
        }

        convertAndSetValue(facesContext, uiComponent, newValue, conv, addErrorMessageOnFail);
    }

    public static void convertAndSetValue(FacesContext facesContext,
                                          UIComponent uiComponent,
                                          String[] newValues,
                                          boolean addErrorMessageOnFail)
    {
        Converter conv = ConverterUtils.findConverter(facesContext, uiComponent);
        if (conv == null)
        {
            //default to StringConverter
            conv = ConverterUtils.getConverter(facesContext.getServletContext(), String.class);
        }

        if (conv instanceof StringArrayConverter)
        {
            setComponentValue(uiComponent, newValues);
        }
        else
        {
            String s = StringArrayConverter.getAsString(newValues);
            convertAndSetValue(facesContext, uiComponent, s, conv, addErrorMessageOnFail);
        }
    }



    public static void convertAndSetValue(FacesContext facesContext,
                                          UIComponent uiComponent,
                                          String newValue,
                                          Converter converter,
                                          boolean addErrorMessageOnFail)
        throws FacesException
    {
        try
        {
            Object objValue = converter.getAsObject(facesContext, uiComponent, newValue);
            setComponentValue(uiComponent, objValue);
        }
        catch (ConverterException e)
        {
            uiComponent.setValue(null);
            uiComponent.setAttribute(CommonComponentAttributes.STRING_VALUE_ATTR, newValue);
            uiComponent.setValid(false);
            if (addErrorMessageOnFail)
            {
                addConversionErrorMessage(facesContext, uiComponent, e.getMessageId());
            }
            else
            {
                throw new FacesException("Error converting value of component " + uiComponent.getClientId(facesContext) + " from String to Object: Converter Exception.", e);
            }
        }
    }


    protected static void addConversionErrorMessage(FacesContext facesContext,
                                                    UIComponent comp,
                                                    String messageId)
    {
        MessageResourcesFactory msgResFactory = (MessageResourcesFactory)FactoryFinder.getFactory(FactoryFinder.MESSAGE_RESOURCES_FACTORY);
        MessageResources msgRes = msgResFactory.getMessageResources(MessageResourcesFactory.FACES_IMPL_MESSAGES);
        //TODO: Find a label (= UIOuput with LabelRenderer) for the component and add it as a MessageFormat parameter
        Message msg = msgRes.getMessage(facesContext, messageId);
        facesContext.addMessage(comp, msg);
    }


    /*
    protected UIComponent findLabelComponent(FacesContext facesContext,
                                             UIComponent forComponent)
    {
        Tree tree = facesContext.getResponseTree();
        for (Iterator it = TreeUtils.treeIterator(tree); it.hasNext();)
        {
            UIComponent comp = (UIComponent)it.next();
            if (comp.getComponentType().equals(UIOutput.TYPE) &&
                comp.getRendererType().equals(LabelRenderer.TYPE))
            {
                String forAttr = (String)comp.getAttribute(LabelRendererAttributes.FOR_ATTR);
                if (forAttr != null && forAttr.equals(forComponent.getCompoundId()))
                {
                    return comp;
                }
            }
        }
        return null;
    }
    */


    public static void setTransient(UIComponent uiComponent, boolean b)
    {
        uiComponent.setAttribute(StateRenderer.TRANSIENT_ATTR,
                                 b ? Boolean.TRUE : Boolean.FALSE);
    }


    public static boolean isTransient(UIComponent uiComponent)
    {
        Boolean trans = (Boolean)uiComponent.getAttribute(StateRenderer.TRANSIENT_ATTR);
        if (trans == null)
        {
            return MyFacesConfig.isComponentsTransientByDefault();
        }
        else
        {
            return trans.booleanValue();
        }
    }


    /*
    public static void convertAndSetAttribute(FacesContext facesContext,
                                              UIComponent comp,
                                              String attrName,
                                              String strValue,
                                              boolean deserializeIfNoConverter)
        throws FacesException
    {
        if (strValue != null)
        {
            Converter conv = null;
            Class clazz = findOutAttrClassByUIComponent(comp, attrName);

            if (clazz != null)
            {
                conv = ConverterUtils.findConverter(facesContext.getServletContext(), clazz);
            }

            Object objValue;
            if (conv != null)
            {
                try
                {
                    objValue = conv.getAsObject(facesContext,
                                                facesContext.getResponseTree().getRoot(), //dummy UIComponent
                                                strValue);
                }
                catch (ConverterException e)
                {
                    throw new FacesException("Error setting attribute '" + attrName + "' of component " + comp.getCompoundId() + ": Converter exception!", e);
                }
            }
            else if (deserializeIfNoConverter)
            {
                LogUtil.getLogger().finer("No converter found for attribute '" + attrName + "' of component " + comp.getCompoundId() + ": Trying to deserialize.");
                objValue = ConverterUtils.deserialize(strValue);
            }
            else
            {
                LogUtil.getLogger().fine("No converter found for attribute '" + attrName + "' of component " + comp.getCompoundId() + ": Assuming String class.");
                objValue = strValue;
            }

            comp.setAttribute(attrName, objValue);
        }
    }
    */

    /*
    protected static Class findOutAttrClassByUIComponent(UIComponent comp, String attrName)
    {
        Class c = null;
        try
        {
            c = BeanUtils.getBeanPropertyType(comp, attrName);
        }
        catch (IllegalArgumentException e)
        {
            c = null;
        }

        if (c != null)
        {
            LogUtil.getLogger().finest("Found out class of attribute '" + attrName + "' of component " + comp.getCompoundId() + ": " + c.getName());
        }

        return c;
    }
    */

    /*
    protected static Class findOutAttrClassByTag(UIComponent comp, Tag tag, String attrName)
    {
        Class c = null;
        try
        {
            BeanMethod beanMethod = BeanUtils.getBeanPropertySetMethod(tag,
                                                                       attrName,
                                                                       Object.class);
            if (beanMethod != null && beanMethod.getMethod() != null)
            {
                c = beanMethod.getMethod().getParameterTypes()[0];
            }
        }
        catch (IllegalArgumentException e)
        {
            c = null;
        }

        if (c != null)
        {
            LogUtil.getLogger().finest("Found out class of attribute '" + attrName + "' of component " + comp.getCompoundId() + " (by Tag): " + c.getName());
        }

        return c;
    }
    */



    public static List[] getListeners(UIComponent uiComponent)
    {
        if (uiComponent instanceof UICommand)
        {
            return ((UICommand)uiComponent).getListeners();
        }
        else if (uiComponent instanceof UIInput)
        {
            return ((UIInput)uiComponent).getListeners();
        }
        else if (uiComponent instanceof javax.faces.component.UICommand ||
                 uiComponent instanceof javax.faces.component.UIInput)
        {
            //HACK to get protected field "listeners" from the given UICommand or UIInput:
            //TODO: try to convince Sun of making listeners public or give us a method to access them
            try
            {
                Field f = null;
                Class c = uiComponent.getClass();
                while (f == null && c != null && !c.equals(Object.class))
                {
                    try
                    {
                        f = c.getDeclaredField("listeners");
                    }
                    catch (NoSuchFieldException e)
                    {
                    }
                    c = c.getSuperclass();
                }

                if (f == null)
                {
                    throw new RuntimeException(new NoSuchFieldException());
                }

                List[] theListeners;
                if (f.isAccessible())
                {
                    theListeners = (List[])f.get(uiComponent);
                }
                else
                {
                    final Field finalF = f;
                    AccessController.doPrivileged(
                        new PrivilegedAction()
                        {
                            public Object run()
                            {
                                finalF.setAccessible(true);
                                return null;
                            }
                        });
                    theListeners = (List[])f.get(uiComponent);
                    f.setAccessible(false);
                }

                return theListeners;
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return null;
        }
    }


}
