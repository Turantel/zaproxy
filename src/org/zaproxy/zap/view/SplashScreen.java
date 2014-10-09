/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright the ZAP Dev team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextArea;

public class SplashScreen extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private final static char NEWLINE = '\n';
	private boolean close = false;
	
	private JScrollPane scrollPane = null;
	private ZapTextArea txtOutput = null;
	private Stack<String> stack = new Stack<String>();
	private static Thread thread = null;
	private List<Image> icons;
	
	public void run() {
		thread = Thread.currentThread();
		
		setSize(420, 460);
		setLocationRelativeTo(null);
		setUndecorated(true);
		setTitle(Constant.PROGRAM_NAME);
		setIconImages(loadIconImages());
		
		JPanel panel = new JPanel();
		
		panel.setPreferredSize(new Dimension(420, 460));
		panel.setLayout(new GridBagLayout());
		panel.setBackground(Color.white);
		panel.setBorder(BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		
		JLabel lblVersion = new JLabel();
		JLabel lblProgramName = new JLabel();
		JLabel lblLogo = new JLabel();

		lblProgramName.setText(Constant.PROGRAM_NAME);
		lblProgramName.setFont(new Font("Default", Font.BOLD, 36));
		lblProgramName.setVisible(true);
		lblProgramName.setName("lblProgramName");
		
		lblVersion.setText(Constant.PROGRAM_VERSION);
		lblVersion.setFont(new Font("Default", Font.PLAIN, 18));
		lblVersion.setName("lblVersion");
		lblVersion.setBackground(Color.white);

		lblLogo.setText("");
		lblLogo.setIcon(new ImageIcon(AboutPanel.class.getResource("/resource/zap64x64.png")));
		lblLogo.setName("lblLogo");

		panel.add(lblProgramName, 
				LayoutHelper.getGBC(1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, new Insets(2, 2, 2, 2)));
		panel.add(lblVersion, 
				LayoutHelper.getGBC(1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, new Insets(0, 5, 0, 5)));
		
		GridBagConstraints logoLayout = 
				LayoutHelper.getGBC(0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, new Insets(5, 15, 5, 15));
		logoLayout.gridheight = 2;
		panel.add(lblLogo, logoLayout);
		
		panel.add(getJScrollPane(), LayoutHelper.getGBC(0, 2, 2, 1.0, 1.0));
		
		this.add(panel);

		this.pack();
		setVisible(true);

        Logger.getRootLogger().addAppender(new SplashOutputWriter());
		
		try {
			// Show INFO and ERROR log messages until the UI is ready
			while (! close) {
				try {
					if (stack.isEmpty()) {
						Thread.sleep(100);
					} else {
						EventQueue.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								while (!stack.isEmpty()) {
									getTxtOutput().append(stack.pop());
									JScrollBar vertical = getJScrollPane().getVerticalScrollBar();
									vertical.setValue( vertical.getMaximum() );
								}
							}
						});
					}
				} catch (InterruptedException e) {
					// New message to display
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		dispose();
	}
	
	private List<Image> loadIconImages() {
		if (icons == null) {
			icons = new ArrayList<>(4);
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap16x16.png")));
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap32x32.png")));
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap48x48.png")));
			icons.add(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/resource/zap64x64.png")));
		}
		return icons;
	}

	private JScrollPane getJScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTxtOutput());
		}
		return scrollPane;
	}

	private ZapTextArea getTxtOutput() {
		if (txtOutput == null) {
			txtOutput = new ZapTextArea();
			txtOutput.setEditable(false);
			txtOutput.setLineWrap(true);
			txtOutput.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			txtOutput.setName("");
			
			// Dont use appendMsg as the interupt wont be handled at this stage
			stack.push(Constant.messages.getString("start.splash.start"));
		}
		return txtOutput;
	}

	public void close() {
		close = true;
	}
	
	public void appendMsg(final String msg) {
		stack.push(msg);
		thread.interrupt();
	}
	
	/* This can apparently cause lockups, probably due to a bug in log4j1
	 * Log4j2 might be ok, but upgrading is really painful :/

	public void appendMsg(final String msg) {
		if (EventQueue.isDispatchThread()) {
			getTxtOutput().append(msg);
			JScrollBar vertical = getJScrollPane().getVerticalScrollBar();
			vertical.setValue( vertical.getMaximum() );
			return;
		}
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					getTxtOutput().append(msg);
					JScrollBar vertical = getJScrollPane().getVerticalScrollBar();
					vertical.setValue( vertical.getMaximum() );
				}
			});
		} catch (Exception e) {
			// Ignore
		}
	}
	*/
	
	private class SplashOutputWriter extends WriterAppender {
		@Override
		public void append(LoggingEvent event) {
			if (event.getLevel().equals(Level.INFO)) {
				String renderedmessage = event.getRenderedMessage();
				if (renderedmessage != null) {
					appendMsg(new StringBuilder("INFO: ").append(renderedmessage).append(NEWLINE).toString());
				}
			} else if (event.getLevel().equals(Level.ERROR)) {
				String renderedmessage = event.getRenderedMessage();
				if (renderedmessage != null) {
					appendMsg(new StringBuilder("ERROR: ").append(renderedmessage).append(NEWLINE).toString());
				}
			}
		}
	}
}