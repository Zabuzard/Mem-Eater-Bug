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
	 * Loads the native attach library into the built path. It is used for
	 * attaching to a virtual machine.
	 */
	private void loadAttachLibrary() {
		System.loadLibrary(ATTACH_LIBRARY_NAME);
	}

	/**
	 * Injects a given agent jar-file into the hooked process. The hooked
	 * process also needs to be a jar-file.
	 * 
	 * @param pathToAgentJar
	 *            Path to the agent jar-file to inject. The agent jar-file needs
	 *            to specify an agentmain-method and must have the Agent-Class
	 *            key accordingly.
	 * @throws UnableToInjectException
	 *             If the operation was unable to inject the agent jar-file into
	 *             the target jar-file
	 */
	public void injectJarIntoJar(final String pathToAgentJar) throws UnableToInjectException {
		try {
			VirtualMachine vm = VirtualMachine.attach("" + mProcess.getPid());
			vm.loadAgent(pathToAgentJar);
			vm.detach();
		} catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException | IOException e) {
			throw new UnableToInjectException(ErrorMessages.UNABLE_TO_INJECT_JAR_INTO_JAR);
		}
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
			VirtualMachine vm = VirtualMachine.attach("" + mProcess.getPid());
			vm.loadAgentLibrary(pathToAgentLibrary);
			vm.detach();
		} catch (AttachNotSupportedException | AgentLoadException | AgentInitializationException | IOException e) {
			throw new UnableToInjectException(ErrorMessages.UNABLE_TO_INJECT_LIBRARY_INTO_JAR);
		}
	}
}
