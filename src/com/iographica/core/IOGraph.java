package com.iographica.core;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.iographica.events.IEventDispatcher;
import com.iographica.events.IEventHandler;
import com.iographica.events.IOEvent;
import com.iographica.gui.ControlPanel;
import com.iographica.gui.FrontPanel;
import com.iographica.gui.IOGraphMenu;
import com.iographica.gui.IOGraphTrayIcon;
import com.iographica.gui.MainFrame;
import com.iographica.gui.SecondaryControls;
import com.iographica.gui.TimerPanel;
import com.iographica.gui.WelcomePanel;
import com.iographica.tracker.TrackManager;
import com.iographica.utils.debug.gui.DebugConsole;
import com.iographica.utils.debug.gui.GraphicProfiler;

public class IOGraph implements IEventHandler, IEventDispatcher {
	private static IOGraph _instance = null;

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		new IOGraph();
	}

	private MainFrame _mainFrame;
	private TrackManager _trackManager;
	private ControlPanel _controlPanel;
	private SnapshotManager _snapshotManager;
	private TimerPanel _timerPanel;
	private SecondaryControls _secondaryControls;
	private PanelSwaper _panelSwaper;
	private TrackingTimer _trackingTimer;
	private FrontPanel _frontPanel;
	private WelcomePanel _welcomePanel;
	private IOGraphMenu _menu;
	private IOGraphTrayIcon _trayIcon;
	private UpdateChecker _updateChecker;
	private ArrayList<IEventHandler> _eventHandlers;
	private boolean _snapShotTaken;
	private boolean _isSnapshotMultimonitored;

	public IOGraph() {
		ImageIO.setUseCache(false);
		// TODO: Show recording status on icon.
		_instance = this;
		Data.getPrefs();
		Data.isOSX = System.getProperty("os.name").toLowerCase().indexOf("mac") != -1;
		Data.isTrayGUI = Data.isOSX && SystemTray.isSupported(); 
		_mainFrame = new MainFrame();
		Data.mainFrame = _mainFrame;
		_updateChecker = new UpdateChecker();
		GraphicsDevice s = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (!Data.isTrayGUI) {
			_mainFrame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					exitCheck();
				}
			});
		}
		_trackingTimer = new TrackingTimer();
		_trackManager = new TrackManager();
		_mainFrame.get_outputPanel().add(_trackManager, 0);

		_snapshotManager = new SnapshotManager(_mainFrame);
		_snapshotManager.addEventHandler(_trackManager);

		_secondaryControls = new SecondaryControls();
		_secondaryControls.setOpaque(false);
		_mainFrame.get_bottomPanel().add(_secondaryControls);
		_secondaryControls.setLocation(Data.MAIN_FRAME_WIDTH - _secondaryControls.getWidth(), 0);

		_controlPanel = new ControlPanel();
		_controlPanel.addEventHandler(this);
		_controlPanel.setOpaque(false);
		_mainFrame.get_bottomPanel().add(_controlPanel);
		_controlPanel.setLocation(0, Data.PANEL_HEIGHT);

		_frontPanel = new FrontPanel();
		_frontPanel.setOpaque(false);
		_mainFrame.get_bottomPanel().add(_frontPanel);

		_timerPanel = new TimerPanel(Data.TEXT_COLOR);
		_timerPanel.setOpaque(false);
		_frontPanel.add(_timerPanel);

		_welcomePanel = new WelcomePanel(Data.TEXT_COLOR);
		_welcomePanel.setOpaque(false);
		_frontPanel.add(_welcomePanel);
		
		_menu = new IOGraphMenu();
		_trayIcon = new IOGraphTrayIcon(_mainFrame);

		_mainFrame.setLocation((int) ((s.getDisplayMode().getWidth() - _mainFrame.getWidth()) * .5), (int) ((s.getDisplayMode().getHeight() - _mainFrame.getHeight()) * .5));
		_mainFrame.pack();
		_trackManager.setup();
		_mainFrame.pack();
		_mainFrame.setMenuBar(_menu);
		_mainFrame.setVisible(true);

		_mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowDeactivated(java.awt.event.WindowEvent e) {
				_secondaryControls.focusLost();
			}

			public void windowActivated(java.awt.event.WindowEvent e) {
				_secondaryControls.focusGained();
			}
		});

		_panelSwaper = new PanelSwaper(_frontPanel, _controlPanel);
		_updateChecker.addEventHandler(_menu);
		_updateChecker.addEventHandler(_trayIcon);
		_menu.addEventHandler(this);
		_menu.addEventHandler(_menu);
		_menu.addEventHandler(_secondaryControls);
		_menu.addEventHandler(_snapshotManager);
		_menu.addEventHandler(_updateChecker);
		_menu.addEventHandler(_trackManager);
		_menu.addEventHandler(_trackingTimer);
		_menu.addEventHandler(_timerPanel);
		_menu.addEventHandler(_welcomePanel);
		_menu.addEventHandler(_controlPanel);
		_trayIcon.addEventHandler(this);
		_trayIcon.addEventHandler(_panelSwaper);
		_trayIcon.addEventHandler(_secondaryControls);
		_trayIcon.addEventHandler(_trackManager);
		_trayIcon.addEventHandler(_trackingTimer);
		_trayIcon.addEventHandler(_timerPanel);
		_trayIcon.addEventHandler(_welcomePanel);
		_trayIcon.addEventHandler(_trayIcon);
		_snapshotManager.addEventHandler(_menu);
		_controlPanel.addEventHandler(this);
		_controlPanel.addEventHandler(_controlPanel);
		_controlPanel.addEventHandler(_menu);
		_controlPanel.addEventHandler(_trayIcon);
		_controlPanel.addEventHandler(_snapshotManager);
		_controlPanel.addEventHandler(_trackManager);
		_controlPanel.addEventHandler(_timerPanel);
		_controlPanel.addEventHandler(_trackingTimer);
		_secondaryControls.addEventHandler(this);
		_secondaryControls.addEventHandler(_panelSwaper);
		_secondaryControls.addEventHandler(_trackManager);
		_secondaryControls.addEventHandler(_trayIcon);
		_trackManager.addEventHandler(_trackManager);
		_trackManager.addEventHandler(_secondaryControls);
		_trackManager.addEventHandler(_trackingTimer);
		_trackManager.addEventHandler(_timerPanel);
		_trackManager.addEventHandler(_menu);
		_trackManager.addEventHandler(_trayIcon);
		_trackManager.addEventHandler(_welcomePanel);
		_timerPanel.addEventHandler(_trackingTimer);
		_timerPanel.addEventHandler(_timerPanel);
		_timerPanel.addEventHandler(_trackManager);
		_timerPanel.addEventHandler(_welcomePanel);
		_timerPanel.addEventHandler(_menu);
		_timerPanel.addEventHandler(_secondaryControls);
		_timerPanel.addEventHandler(_trayIcon);
		_trackingTimer.addEventHandler(_timerPanel);
		_trackingTimer.addEventHandler(_trackManager);
		_trackingTimer.addEventHandler(this);
		this.addEventHandler(_trackingTimer);
		this.addEventHandler(_timerPanel);
		this.addEventHandler(_welcomePanel);
		this.addEventHandler(_trackManager);
		this.addEventHandler(_snapshotManager);
		this.addEventHandler(_controlPanel);
		this.addEventHandler(_menu);
		this.addEventHandler(this);
		
		if (Data.DEBUG) {
			DebugConsole console;
			console = new DebugConsole();
			console.setVisible(true);
			System.getProperties().list(System.out);
			GraphicProfiler profiler = new GraphicProfiler();
			profiler.setVisible(true);
			_trackManager.addEventHandler(profiler);
		}
		_updateChecker.check();
	}

	protected void exitCheck() {
		System.out.println("exitCheck");
		if (Data.trackingTime < 60000) System.exit(0);
		String header = "Wait! Wait! Wait!";
		int timeTreshold = 60000 * 30;
		String message = "Do you really wanna exit and lose all your data?";
		if (Data.trackingTime > timeTreshold) message += "\nAre you sure? After " + Data.time + " of laborious tracking?";
		int mt = Data.trackingTime > timeTreshold ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE;
		int cd = JOptionPane.showConfirmDialog(null, message, header, JOptionPane.YES_NO_OPTION, mt, IOGraph.getIcon("DialogIcon.png"));
		if (cd == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	public void debugExit() {
		_trackManager.debugExit();
		_trackManager = null;
		_controlPanel = null;
		_snapshotManager = null;
		_timerPanel = null;
		_secondaryControls = null;
		_panelSwaper = null;
		_trackingTimer = null;
		_frontPanel = null;
		_welcomePanel = null;
		_menu = null;
		Data.mainFrame = null;
		Data.mouseTrackRecording = false;
		_mainFrame.dispose();
		_mainFrame = null;
		_instance = null;
		System.gc();
	}

	public void onEvent(IOEvent event) {
		switch (event.type) {
		case Data.GET_URL:
			WebSurfer.get(Data.WEBSITE_URL);
			break;
		case Data.MULTI_MONITOR_USAGE_CHANGED:
			_mainFrame.pack();
			break;
		case Data.SYSTEM_QUIT_REQUESTED:
			exitCheck();
			break;
		case Data.CHECK_FOR_UPDATES:
			_updateChecker.check();
			break;
		case Data.IGNORE_MOUSE_STOPS_CHANGED:
			Data.prefs.putBoolean(Data.IGNORE_MOUSE_STOPS, !Data.prefs.getBoolean(Data.IGNORE_MOUSE_STOPS, false));
			break;
		case Data.MULTI_MONITOR_USAGE_CHANGING_REQUEST:
			changeMultiMonitorUsage();
			break;
		case Data.COLORFUL_SCHEME_USAGE_CHANGING_REQUEST:
			changeColorfulSchemeUsage();
			break;
		case Data.BACKGROUND_USAGE_CHANGE_REQUEST:
			changeBackgroundUsage();
			break;
		default:
			break;
		}
	}

	private void changeBackgroundUsage() {
		Data.useScreenshot = !Data.useScreenshot;
		if (!Data.useScreenshot) {
			_isSnapshotMultimonitored = Data.prefs.getBoolean(Data.USE_MULTIPLE_MONITORS, true);
		} else {
			if (_isSnapshotMultimonitored != Data.prefs.getBoolean(Data.USE_MULTIPLE_MONITORS, true)) _snapShotTaken = false;
		}
		if (!_snapShotTaken) {
			_snapShotTaken = true;
			dispatchEvent(Data.UPDATE_BACKGROUND);
		}
		dispatchEvent(Data.BACKGROUND_USAGE_CHANGED);
	}

	private void changeColorfulSchemeUsage() {
		if (Data.trackingTime > 0) {
			Data.preventControlsHiding = true;
			String header = "Color Scheme Switch Confirmation";
			int timeTreshold = 60000 * 30;
			String message = "We need to reset the tracking by switching\nbetween color schemas.\nDo you really wanna start from scratch?";
			if (Data.trackingTime > timeTreshold) message += "\nAre you sure? After " + Data.time + " of laborious tracking?";
			int mt = Data.trackingTime > timeTreshold ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE;
			int cd = JOptionPane.showConfirmDialog(null, message, header, JOptionPane.YES_NO_OPTION, mt, getIcon("DialogIcon.png"));
			Data.preventControlsHiding = false;
			if (cd != JOptionPane.YES_OPTION) return;
		}
		Data.prefs.putBoolean(Data.USE_COLOR_SCHEME, Data.requestedColorfulSchemeUsage);
		dispatchEvent(Data.COLOR_SCHEME_CHANGED);
	}

	private void changeMultiMonitorUsage() {
		if (Data.trackingTime > 0) {
			Data.preventControlsHiding = true;
			String header = "Switch Confirmation";
			int timeTreshold = 60000 * 30;
			String message = "We need to reset the tracking by switching\nbetween single/multiple monitors.\nDo you really wanna start from scratch?";
			if (Data.trackingTime > timeTreshold) message += "\nAre you sure? After " + Data.time + " of laborious tracking?";
			int mt = Data.trackingTime > timeTreshold ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE;
			int cd = JOptionPane.showConfirmDialog(null, message, header, JOptionPane.YES_NO_OPTION, mt, getIcon("DialogIcon.png"));
			Data.preventControlsHiding = false;
			if (cd != JOptionPane.YES_OPTION) return;
		}
		Data.prefs.putBoolean(Data.USE_MULTIPLE_MONITORS, Data.requestedMultiMonitorUsage);
		if (Data.useScreenshot) dispatchEvent(Data.UPDATE_BACKGROUND);
		dispatchEvent(Data.MULTI_MONITOR_USAGE_CHANGED);
	}

	static public ImageIcon getIcon(String filename) {
		URL url = _instance.getClass().getResource(Data.RESOURCE_DIRECTORY + filename);
		ImageIcon i;
		try {
			i = new ImageIcon(url);
		} catch (Exception e) {
			i = null;
			System.err.println("Can't load ImageIcon from " + filename + ".");
		}
		return i;
	}

	static public BufferedImage getBufferedImage(String filename) {
		BufferedImage img = null;
		try {
			URL url = _instance.getClass().getResource(Data.RESOURCE_DIRECTORY + filename);
			img = ImageIO.read(url);
		} catch (IOException e) {
			System.out.println("IOGraph.getBufferedImage(): " + e);
		}
		return img;
	}

	public static IOGraph getInstance() {
		return _instance;
	}

	public static boolean resetConfirmation() {
		String header = "Reset confirmation";
		int timeTreshold = 60000 * 30;
		String message = "Do you really wanna start from scratch?";
		if (Data.trackingTime > timeTreshold) message += "\nAre you sure? After " + Data.time + " of laborious tracking?";
		int mt = Data.trackingTime > timeTreshold ? JOptionPane.WARNING_MESSAGE : JOptionPane.QUESTION_MESSAGE;
		ImageIcon icon = null;
		icon = getIcon("DialogIcon.png");
		int cd;
		cd = JOptionPane.showConfirmDialog(null, message, header, JOptionPane.YES_NO_OPTION, mt, icon);
		if (cd == JOptionPane.YES_OPTION) {
			return true;
		}
		return false;
	}

	public void addEventHandler(IEventHandler handler) {
		if (_eventHandlers == null) {
			_eventHandlers = new ArrayList<IEventHandler>();
		}
		for (IEventHandler handler2 : _eventHandlers)
			if (handler2.equals(handler)) return;
		_eventHandlers.add(handler);
	}

	private void dispatchEvent(int type) {
		if (_eventHandlers != null) {
			final IOEvent event = new IOEvent(type, this);
			for (IEventHandler handler : _eventHandlers) {
				handler.onEvent(event);
			}
		}
	}
}