/**
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
package net.sourceforge.myfaces.taglib.html;

import net.sourceforge.myfaces.renderkit.JSFAttr;
import net.sourceforge.myfaces.renderkit.html.HTML;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlGraphicImage;


/**
 * DOCUMENT ME!
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Martin Marinschek
 * @version $Revision$ $Date$
 */
public class HtmlGraphicImageTag
    extends HtmlComponentTag
{
    public String getComponentType()
    {
        return HtmlGraphicImage.COMPONENT_TYPE;
    }

    public String getDefaultRendererType()
    {
        return "javax.faces.Image";
    }

    // UIComponent attributes --> already implemented in MyfacesComponentTag

    // user role attributes --> already implemented in MyfacesComponentTag

    // HTML universal attributes --> already implemented in HtmlComponentTag

    // HTML event handler attributes --> already implemented in HtmlComponentTag

    // HTML img attributes relevant for graphic-image
    private String _align;  //FIXME: not in API, HTML 4.0 transitional attribute and not in strict... what to do?
    private String _alt;
    private String _border; //FIXME: not in API, HTML 4.0 transitional attribute and not in strict... what to do!
    private String _height;
    private String _hspace; //FIXME: not in API, HTML 4.0 transitional attribute and not in strict... what to do!
    private String _ismap;
    private String _longdesc;
    private String _onblur;
    private String _onchange;
    private String _onfocus;
    private String _usemap;
    private String _vspace; //FIXME: not in API, HTML 4.0 transitional attribute and not in strict... what to do!
    private String _width;

    //UIGraphic attributes
    private String _url;

    // HtmlGraphicImage attributes
    //none so far

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setStringProperty(component, HTML.ALIGN_ATTR, _align);
        setStringProperty(component, HTML.ALT_ATTR, _alt);
        setStringProperty(component, HTML.BORDER_ATTR, _border);
        setStringProperty(component, HTML.HEIGHT_ATTR, _height);
        setStringProperty(component, HTML.HSPACE_ATTR, _hspace);
        setBooleanProperty(component, HTML.ISMAP_ATTR, _ismap);
        setStringProperty(component, HTML.LONGDESC_ATTR, _longdesc);
        setStringProperty(component, HTML.ONBLUR_ATTR, _onblur);
        setStringProperty(component, HTML.ONCHANGE_ATTR, _onchange);
        setStringProperty(component, HTML.ONFOCUS_ATTR, _onfocus);
        setStringProperty(component, HTML.USEMAP_ATTR, _usemap);
        setStringProperty(component, HTML.VSPACE_ATTR, _vspace);
        setStringProperty(component, HTML.WIDTH_ATTR, _width);

        setStringProperty(component, JSFAttr.URL_ATTR, _url);
   }

    public void setAlign(String align)
    {
        _align = align;
    }

    public void setAlt(String alt)
    {
        _alt = alt;
    }

    public void setBorder(String border)
    {
        _border = border;
    }

    public void setHeight(String height)
    {
        _height = height;
    }

    public void setHspace(String hspace)
    {
        _hspace = hspace;
    }

    public void setIsmap(String ismap)
    {
        _ismap = ismap;
    }

    public void setLongdesc(String longdesc)
    {
        _longdesc = longdesc;
    }

    public void setOnblur(String onblur)
    {
        _onblur = onblur;
    }

    public void setOnchange(String onchange)
    {
        _onchange = onchange;
    }

    public void setOnfocus(String onfocus)
    {
        _onfocus = onfocus;
    }

    public void setUsemap(String usemap)
    {
        _usemap = usemap;
    }

    public void setVspace(String vspace)
    {
        _vspace = vspace;
    }

    public void setWidth(String width)
    {
        _width = width;
    }

    public void setUrl(String url)
    {
        _url = url;
    }
}
