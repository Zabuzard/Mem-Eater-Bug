package de.zabuza.memeaterbug.injection;

import java.io.IOException;

import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import de.zabuza.memeaterbug.exceptions.UnableToInjectException;
import de.zabuza.memeaterbug.locale.ErrorMessages;
import de.zabuza.memeaterbug.winapi.Process;
import de.zabuza.memeaterbug.winapi.jna.util.PsapiUtil;

/**
 * Provides various methods for injecting code into a given process.
 * 
 * @author Zabuza
 *
 */
public final class Injector {

	/**
	 * Masks that separates the passed arguments.
	 */
	public static final String ARG_SEPARATOR = ",";

	/**
	 * Name of the attach library that is used for attaching to a virtual
	 * machine.
	 */
	private static final String ATTACH_LIBRARY_NAME = "attach";

	/**
	 * The process this object belongs to.
	 */
	private final Process mProcess;

	/**
	 * Creates a new object that is able to inject code into the given process.
	 * 
	 * @param processId
	 *            Id of the process to inject into
	 */
	public Injector(final int processId) {
		this(processId, null);
	}

	/**
	 * Creates a new object that is able to inject code into the given process.
	 * 
	 * @param processId
	 *            Id of the process to inject into
	 * @param processHandle
	 *            An optional previously created handle object that must
	 *            correspond to the same process that is specified by processId.
	 *            Using <tt>null</tt> results in the creation of a default
	 *            handle, that has all access rights.
	 */
	public Injector(final int processId, final HANDLE processHandle) {
		mProcess = PsapiUtil.getProcessById(processId);
		if (processHandle != null) {
			mProcess.setHandle(processHandle);
		}
		loadAttachLibrary();
	}

	/**
	 * Injects a given agent jar-file into the hooked process. The hooked
	 * process also needs to be a jar-file. The agent jar-File should use the
	 * {@link Injection} agent. It will automatically create and start an
	 * instance of the given custom Thread object. Thus this method immediately
	 * returns once the thread has started.
	 * 
	 * @param pathToAgentJar
	 *            Path to the agent jar-file to inject. The agent jar-file needs
	 *            to specify an agentmain-method and must have the Agent-Class
	 *            key accordingly. A good solution is to use the
	 *            {@link Injection} agent for that and provide a custom Thread
	 *            which gets automatically called by {@link Injection}.
	 * @param threadClassName
	 *            The full class name of the thread object that gets
	 *            automatically started by the injection agent, assuming
	 *            {@link Injection} gets used.
	 * @throws UnableToInjectException
	 *             If the operation was unable to inject the agent jar-file into
	 *             the target jar-file
	 */
	public void injectJarIntoJar(final String pathToAgentJar, final String threadClassName)
			throws UnableToInjectException {
		injectJarIntoJar(pathToAgentJar, threadClassName, null);
	}

	/**
	 * Injects a given agent jar-file into the hooked process. The hooked
	 * process also needs to be a jar-file. The agent jar-File should use the
	 * {@link Injection} agent. It will automatically create and start an
	 * instance of the given custom Thread object. Thus this method immediately
	 * returns once the thread has started. If the given Thread provides a
	 * constructor that accepts a String-array, additional arguments can be
	 * passed.
	 * 
	 * @param pathToAgentJar
	 *            Path to the agent jar-file to inject. The agent jar-file needs
	 *            to specify an agentmain-method and must have the Agent-Class
	 *            key accordingly. A good solution is to use the
	 *            {@link Injection} agent for that and provide a custom Thread
	 *            which gets automatically called by {@link Injection}.
	 * @param threadClassName
	 *            The full class name of the thread object that gets
	 *            automatically started by the injection agent, assuming
	 *            {@link Injection} gets used. The injector will pass the given
	 *            additional arguments if the Thread object has a constructor
	 *            that accepts a String-array, else the default constructor gets
	 *            used.
	 * @param additionalArgs
	 *            Additional arguments to pass to the jar-File. They get
	 *            separated by {@link #ARG_SEPARATOR}.
	 * @throws UnableToInjectException
	 *             If the operation was unable to inject the agent jar-file into
	 *             the target jar-file
	 */
	public void injectJarIntoJar(final String pathToAgentJar, final String threadClassName,
			final String[] additionalArgs) throws UnableToInjectException {
		try {
			StringBuilder argsToPass = new StringBuilder();
			if (threadClassName != null && threadClassName.length() > 0) {
				argsToPass.append(threadClassName).append(ARG_SEPARATOR);
			}
			String processIdAsString = String.valueOf(mProcess.getPid());
			argsToPass.append(processIdAsString);
			if (additionalArgs != null && additionalArgs.length > 0) {
				for (String addtionalArg : additionalArgs) {
					argsToPass.append(ARG_SEPARATOR).append(addtionalArg);
				}
			}

			VirtualMachine vm = VirtualMachine.attach(processIdAsString);
			vm.loadAgent(pathToAgentJar, argsToPass.toString());
			vm.detach();
		} catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException | IOException e) {
			throw new UnableToInjectException(ErrorMessages.UNABLE_TO_INJECT_JAR_INTO_JAR);
		}
	}

	/**
	 * Injects a given agent jar-file into the hooked process. The hooked
	 * process also needs to be a jar-file. The method returns once the
	 * agentmain-method of the injected agent finishes.
	 * 
	 * @param pathToAgentJar
	 *            Path to the agent jar-file to inject. The agent jar-file needs
	 *            to specify an agentmain-method and must have the Agent-Class
	 *            key accordingly.
	 * @param additionalArgs
	 *            Additional arguments to pass to the jar-File. They get
	 *            separated by {@link #ARG_SEPARATOR}.
	 * @throws UnableToInjectException
	 *             If the operation was unable to inject the agent jar-file into
	 *             the target jar-file
	 */
	public void injectJarIntoJar(final String pathToAgentJar, final String[] additionalArgs)
			throws UnableToInjectException {
		injectJarIntoJar(pathToAgentJar, null, additionalArgs);
	}

	/**
	 * Injects a given agent library into the hooked process. The hooked process
	 * needs to be a jar-file.
	 * 
	 * @param pathToAgentLibrary
	 *            Path to the agent library to inject. The agent library needs
	 *            to specify all needed agent-methods via the native agent
	 *            interface.
	 * @throws UnableToInjectException
	 *             If the operation was unable to inject the agent library into
	 *             the target jar-file
	 */
	public void injectLibraryIntoJar(final String pathToAgentLibrary) throws UnableToInjectException {
		try {
			String processIdAsString = String.valueOf(mProcess.getPid());
			VirtualMachine vm = VirtualMachine.attach(processIdAsString);
			vm.loadAgentLibrary(pathToAgentLibrary);
			vm.detach();
		} catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException | IOException e) {
			throw new UnableToInjectException(ErrorMessages.UNABLE_TO_INJECT_LIBRARY_INTO_JAR);
		}
	}

	/**
	 * Loads the native attach library into the built path. It is used for
	 * attaching to a virtual machine.
	 */
	private void loadAttachLibrary() {
		System.loadLibrary(ATTACH_LIBRARY_NAME);
	}
}
