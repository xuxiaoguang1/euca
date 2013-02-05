package com.eucalyptus.webui.client.view;

import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;

public class DeviceMeasure {
    
    public static class Size {
        
        public int width;
        public int height;
        
        private Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
    }
    
    public static int getHTMLWidth(String html) {
        return getHTMLSize(html).width;
    }
    
    public static int getMaxHTMLWidth(List<String> list) {
        Document document = Document.get();
        Element element = document.createElement("div");
        element.getStyle().setPosition(Position.ABSOLUTE);
        element.getStyle().setLeft(-1000, Unit.PX);
        element.getStyle().setTop(-1000, Unit.PX);
        document.getBody().appendChild(element);
        int width = 0;
        for (String text : list) {
            element.setInnerHTML(text);
            width = Math.max(width, element.getOffsetWidth());
        }
        document.getBody().removeChild(element);
        return width;
    }
    
    public static int getTotalHTMLWidth(List<String> list) {
        Document document = Document.get();
        Element element = document.createElement("div");
        element.getStyle().setPosition(Position.ABSOLUTE);
        element.getStyle().setLeft(-1000, Unit.PX);
        element.getStyle().setTop(-1000, Unit.PX);
        document.getBody().appendChild(element);
        int width = 0;
        for (String text : list) {
            element.setInnerHTML(text);
            width += element.getOffsetWidth();
        }
        document.getBody().removeChild(element);
        return width;
    }
    
    public static Size getHTMLSize(String html) {
        Document document = Document.get();
        Element element = document.createElement("div");
        element.getStyle().setPosition(Position.ABSOLUTE);
        element.getStyle().setLeft(-1000, Unit.PX);
        element.getStyle().setTop(-1000, Unit.PX);
        document.getBody().appendChild(element);
        element.setInnerHTML(html);
        int width = element.getOffsetWidth();
        int height = element.getOffsetHeight();
        document.getBody().removeChild(element);
        return new Size(width, height);
    }
    
}
