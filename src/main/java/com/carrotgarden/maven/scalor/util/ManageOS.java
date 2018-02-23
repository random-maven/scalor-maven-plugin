package com.carrotgarden.maven.scalor.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;

public interface ManageOS {

	// http://www.gnu.org/software/libc/manual/html_node/Running-a-Command.html
	// http://www.gnu.org/software/libc/manual/html_node/Creating-a-Process.html

	// https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/util/jna/GNUCLibrary.java
	// https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/lifecycle/UnixLifecycle.java

	interface NIX_C extends Library {

		int F_GETFD = 1;
		int F_SETFD = 2;
		int F_GETPATH = 12;

		int FD_CLOEXEC = 1;

		int PATH_MAX = 4096;

		int fcntl(int fd, int cmd);

		int fcntl(int fd, int cmd, int arg);

		int fcntl(int fd, int cmd, byte[] array);

		int getdtablesize();

		int fork();

		int vfork();

		// absolute file
		int execv(String file, StringArray args);

		// search path for file
		int execvp(String file, StringArray args);

		int kill(int pid, int signum);

		int setsid();

		int setuid(short newuid);

		int setgid(short newgid);

		int umask(int mask);

		int getpid();

		int getppid();

		int chdir(String dir);

		int setenv(String name, String value);

		int unsetenv(String name);

		void perror(String msg);

		String strerror(int errno);

		int system(String cmd);

		int sysctl(int[] mib, int nameLen, Pointer oldp, IntByReference oldlenp, Pointer newp, IntByReference newlen);

		int sysctlnametomib(String name, Pointer mibp, IntByReference size);

	}

	static final NIX_C LIB_C = (NIX_C) Native.loadLibrary("c", NIX_C.class);

	interface NIX_prctl extends Library {

		int prctl(int arg1, String arg2, long arg3, long arg4, long arg5);

	}

	// close all files upon exec, except stdin, stdout, stderr
	static void closeFiles() {
		int size = LIB_C.getdtablesize();
		for (int fd = 3; fd < size; fd++) { // above std*
			int flags = LIB_C.fcntl(fd, NIX_C.F_GETFD);
			if (flags < 0) {
				continue; // ignore errors
			}
			LIB_C.fcntl(fd, NIX_C.F_SETFD, flags | NIX_C.FD_CLOEXEC);
		}
	}

	static void resolvePath(int fd) {
		byte[] filePath = new byte[NIX_C.PATH_MAX];
		if (LIB_C.fcntl(fd, NIX_C.F_GETPATH, filePath) != -1) {

		} else {

		}
	}

	// all files used by jvm
	// static void processFiles() {
	// int size = LIB_C.getdtablesize();
	// for (int fd = 3; fd < size; fd++) { // above std*
	// int flags = LIB_C.fcntl(fd, NIX_C.F_GETFD);
	// if (flags < 0) {
	// continue; // ignore errors
	// }
	// }
	// }

	static int invokeProcess(String[] command) {
		String exec = command[0];
		StringArray args = new StringArray(command);
		closeFiles();
		int result = LIB_C.execv(exec, args);
		return result;
	}

	// static final NIX_prctl LIB_CAP = (NIX_prctl) Native.loadLibrary("cap",
	// NIX_prctl.class);

}
