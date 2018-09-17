package jdd.so.service;

import java.io.FileInputStream;
import java.util.Properties;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.sobotics.redunda.PingService;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import jdd.so.CloseVoteFinder;
import jdd.so.bot.ChatBot;

public class SOCVFinderServiceWrapper implements WrapperListener {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SOCVFinderServiceWrapper.class);

	static {
		PropertyConfigurator.configure("ini/log4j.properties");
	}

	private ChatBot cb;

	/*---------------------------------------------------------------
	 * Constructors
	 *-------------------------------------------------------------*/
	/**
	 * Creates an instance of a WrapperSimpleApp.
	 * 
	 * @param The
	 *            full list of arguments passed to the JVM.
	 */
	protected SOCVFinderServiceWrapper(String args[]) {

		// Initialize the WrapperManager class on startup by referencing it.
		@SuppressWarnings("unused")
		Class<WrapperManager> wmClass = WrapperManager.class;

		// Start the application. If the JVM was launched from the native
		// Wrapper then the application will wait for the native Wrapper to
		// call the application's start method. Otherwise the start method
		// will be called immediately.
		WrapperManager.start(this, args);

		// This thread ends, the WrapperManager will start the application after
		// the Wrapper has
		// been properly initialized by calling the start method above.
	}

	public static void main(String[] args) {
		new SOCVFinderServiceWrapper(args);
	}

	/*---------------------------------------------------------------
	 * WrapperListener Methods
	 *-------------------------------------------------------------*/
	/**
	 * The start method is called when the WrapperManager is signalled by the
	 * native wrapper code that it can start its application. This method call
	 * is expected to return, so a new thread should be launched if necessary.
	 * If there are any problems, then an Integer should be returned, set to the
	 * desired exit code. If the application should continue, return null.
	 */
	@Override
	public Integer start(String[] args) {
		if (logger.isInfoEnabled()) {
			logger.info("start(String[]) - start");
		}

		// Load AI interface
		AIMLProcessor.extension = new PCAIMLProcessorExtension();
		MagicStrings.root_path = System.getProperty("user.dir");
		MagicStrings.default_bot_name = ChatBot.BOT_NAME;

		// Load properties file an instance the CloseVoteFinder

		// Start the bot
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("ini/SOCVService.properties"));
			CloseVoteFinder.initInstance(properties);
			
			//Redunda service
			if (logger.isInfoEnabled()) {
				logger.info("Starting redunda ping service");
			}
			PingService redunda = new PingService("b2f12d074632a1d9b2f55c3955326cf2c44b6d0f2210717bb467b18006161f91", CloseVoteFinder.VERSION);
			redunda.start();
			
			cb = new ChatBot(properties, null);
			SOLoginThread login = new SOLoginThread(); // takes to much time
			login.start();
			if (logger.isDebugEnabled()) {
				logger.debug("start(String[]) - Start completed");
			}
		} catch (Throwable e) {
			logger.error("start service", e);
			CloseVoteFinder.getInstance().shutDown();
			cb.close();
			return 1;
		}

		if (logger.isInfoEnabled()) {
			logger.info("start(String[]) - end");
		}
		return null;
	}

	/**
	 * Called when the application is shutting down.
	 */
	@Override
	public int stop(int exitCode) {
		if (logger.isInfoEnabled()) {
			logger.info("stop() - exitCode:" + exitCode);
		}
		try {
			if (cb != null) {
				cb.close();
			}
			CloseVoteFinder.getInstance().shutDown();
		} catch (Throwable e) {
			logger.error("stop(int)", e);
		}
		if (logger.isInfoEnabled()) {
			logger.info("stop() end");
		}
		return exitCode;
	}

	/**
	 * Called whenever the native wrapper code traps a system control signal
	 * against the Java process. It is up to the callback to take any actions
	 * necessary. Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT,
	 * WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or
	 * WRAPPER_CTRL_SHUTDOWN_EVENT
	 */
	@Override
	public void controlEvent(int event) {
		if ((event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT) && WrapperManager.isLaunchedAsService()) {
			// Ignore
			if (logger.isInfoEnabled()) {
				logger.info("ServiceWrapper: controlEvent(" + event + ") Ignored");
			}

		} else {
			if (logger.isInfoEnabled()) {
				logger.info("ServiceWrapper: controlEvent(" + event + ") Stopping");
			}
			WrapperManager.stop(0);
			// Will not get here.
		}
	}

	private class SOLoginThread extends Thread {
		/**
		 * Logger for this class
		 */
		private final Logger logger = Logger.getLogger(SOLoginThread.class);

		@Override
		public void run() {
			if (logger.isDebugEnabled()) {
				logger.debug("start(String[]) - Start login");
			}
			cb.loginIn();
			logger.info("start(String[]) - Join rooms - START");
			
			cb.joinRooms();
			
			logger.info("start(String[]) - Join rooms - END");

			cb.startDupeHunter();
			
			logger.info("start(String[]) - dupe hunter - END");
		}

	}

}
