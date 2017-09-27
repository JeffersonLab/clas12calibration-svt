/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.calib.services.svt.utils;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

//import org.root.pad.EmbeddedCanvas;
//import org.root.basic.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvas;


public class CanvasViewPanel extends JPanel {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private		JTabbedPane tabbedPane;
    
    public CanvasViewPanel(){
        super();
        this.setLayout(new BorderLayout());
        this.initComponents();
    }	
    
    private void initComponents(){
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane,BorderLayout.CENTER);
    }
    
    public void addCanvasLayer(String name, EmbeddedCanvas panel){
        tabbedPane.addTab(name, panel);
    }
}