package com.eucalyptus.webui.client.view;

import java.util.LinkedList;
import java.util.List;

import com.eucalyptus.webui.shared.message.ClientMessage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class DeviceColumnPopupPanel extends PopupPanel {
	
	private static DeviceColumnPopupPanelUiBinder uiBinder = GWT.create(DeviceColumnPopupPanelUiBinder.class);
	
	interface DeviceColumnPopupPanelUiBinder extends UiBinder<Widget, DeviceColumnPopupPanel> {
	}
	
	public interface Presenter {
	    
	    public void onValueChange(int column, boolean value);
	    
	}
	
	@UiField ScrollPanel panel;
	@UiField Tree tree;
	
	private Presenter presenter;
	
	public DeviceColumnPopupPanel(Presenter presenter) {
		super(true);		
		setWidget(uiBinder.createAndBindUi(this));
		
		this.presenter = presenter;
		this.addDomHandler(new KeyDownHandler() {

			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (isVisible() && event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					hide();
				}
			}
			
		}, KeyDownEvent.getType());
		
        tree.addOpenHandler(new OpenHandler<TreeItem>() {

            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                resize();
            }
            
        });
        
        tree.addCloseHandler(new CloseHandler<TreeItem>() {

            @Override
            public void onClose(CloseEvent<TreeItem> event) {
                resize();
            }
            
        });
        
        Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				if (isVisible()) {
					resize();
				}
			}
			
        });
	}
	
	public void popup(Widget widget) {
	    int x = widget.getAbsoluteLeft();
        int y = widget.getAbsoluteTop() + widget.getOffsetHeight();
        setPopupPosition(x, y);
        show();
        resize();
	}
	
	public void resize() {
	    DeviceMeasure.Size size = DeviceMeasure.getHTMLSize(tree.getElement().getInnerHTML());
	    int maxHeight = Math.max(100, Window.getClientHeight() - panel.getAbsoluteTop());
	    panel.setWidth(size.width + 30 + "px");
	    panel.setHeight(Math.min(size.height, maxHeight - 30) + 3 + "px");
	}
	
	public class Node {
	    
	    private List<Node> nodeList = new LinkedList<Node>();
	    
	    private ClientMessage msg;
	    private int column = -1;
	    private boolean value;
	    
	    private Node(ClientMessage msg) {
	        this.msg = msg;
	    }
	    
	    private Node(ClientMessage msg, int column, boolean value) {
	        this.msg = msg;
	        this.column = column;
	        this.value = value;
	    }
	    
	    public Node addNode(ClientMessage msg) {
	        Node node = new Node(msg);
	        nodeList.add(node);
	        return node;
	    }
	    
	    public Node addNode(ClientMessage msg, int column, boolean value) {
	        Node node = new Node(msg, column, value);
	        nodeList.add(node);
	        return node;
	    }
	    
	    private TreeItem getItem() {
	        String text = "";
	        if (msg != null) {
	            text = msg.getText();
	        }
	        TreeItem item;
	        if (column >= 0) {
	            final CheckBox checkbox = new CheckBox(text);
	            checkbox.setValue(value);
	            checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {
                        if (presenter != null) {
                            value = event.getValue();
                            presenter.onValueChange(column, value);
                        }
                    }
                    
	            });
	            item = new TreeItem(checkbox);
	        }
	        else {
	            item = new TreeItem(text);
	        }
	        for (Node node : nodeList) {
	            item.addItem(node.getItem());
	        }
	        return item;
	    }
	    
	}
	
	private List<Node> rootList = new LinkedList<Node>();
	
	public Node addNode(ClientMessage msg) {
	    Node node = new Node(msg);
	    rootList.add(node);
	    return node;
	}
	
	public void reload() {
        tree.clear();
	    if (!rootList.isEmpty()) {
    	    for (Node node : rootList) {
    	        tree.addItem(node.getItem());
    	    }
	    }
	}
	
}
