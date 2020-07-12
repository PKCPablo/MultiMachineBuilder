/**
 * 
 */
package mmb.addon.loader;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

import mmb.addon.data.AddonInfo;
import mmb.addon.data.AddonState;
import mmb.data.contents.GameContents;
import mmb.data.files.ListFiles;
import mmb.debug.Debugger;
import mmb.addon.*;

import static mmb.ui.window.Loading.*;

/**
 * @author oskar
 *
 */
public class ModLoader {
	private static Debugger debug = new Debugger("MOD-LOADER");
	public static List<AddonLoader> runningLoaders = new ArrayList<AddonLoader>();
	public static List<Thread> runningFirstRunThreads = new ArrayList<Thread>();
	public static List<Thread> runningContentAddThreads = new ArrayList<Thread>();
	public static List<Thread> runningIntegrationThreads = new ArrayList<Thread>();
	public static int modCount = 0;
	public static List<String> toLoad = new ArrayList<String>();
	public static String[] external = new String[] {};
	
	public static void waitAllFirstRuns() {
		runningFirstRunThreads.forEach((Thread t) -> {try {
			t.join();
		} catch (InterruptedException e) {
			debug.pstm(e, "Stopping loading prematurely");
		}
		});
	}
	
	public static void waitAllContentRuns() {
		runningContentAddThreads.forEach((Thread t) -> {try {
			t.join();
		} catch (InterruptedException e) {
			debug.pstm(e, "Stopping loading prematurely");
		}
		});
	}
	
	public static void waitAllIntegrationRuns() {
		runningIntegrationThreads.forEach((Thread t) -> {try {
			t.join();
		} catch (InterruptedException e) {
			debug.pstm(e, "Stopping loading prematurely");
		}
		});
	}
	public static void waitAllLoaders() {
		runningLoaders.forEach((AddonLoader t) -> {try {
			t.runningOn.join();
		} catch (InterruptedException e) {
			debug.pstm(e, "Stopping loading prematurely");
		}
		});
	}
	
	public static void modloading(){
		try {
			external = new String(Files.readAllBytes(Paths.get("ext.txt"))).split("\n");
		} catch (IOException e1) {
			debug.pstm(e1, "Unable to load external mods:");
		}
		//Notify user
		state1("Initial load");
		debug.printl("Loading mods");
		debug.printl("Finding all files to load");
		
		// Find modfiles to load;
		try {
			walkDirectory(new File("mods/"));
		} catch (IOException e) {
			debug.pstm(e, "Couldn't load mods, the list may be incomplete");
		}
		modCount += external.length;
		for(int i = 0; i < external.length; i++) {
			toLoad.add(external[i]);
		}
		state1("Found "+ modCount + " mod files");
		debug.printl("Found "+ modCount + " mod files");
		toLoad.forEach((String p) -> {
			state2("Loading file: " + p);
			AddonLoader.load(p);
		});
		//Wait until all files load
		waitAllLoaders();
		
		//First runs. Similar process for all three stages
		GameContents.addons.forEach((AddonInfo ai) -> {
			if(ai.state == AddonState.ENABLE) {
				Thread thr = new Thread(() -> {
					if(ai.central == null) {
						debug.printl(ai.name + " is not a mod and will not be run");
						ai.state = AddonState.API;
					}else {
						debug.printl("Start 1st stage for " + ai.name);
						ai.central.firstOpen();
						debug.printl("End 1st stage for " + ai.name);
					}
					
				});
				runningFirstRunThreads.add(thr);
				thr.run();
			}
		});
		waitAllFirstRuns();
		
		//Content runs
		GameContents.addons.forEach((AddonInfo ai) -> {
			if(ai.state == AddonState.ENABLE) { 
				Thread thr = new Thread(() -> {
					debug.printl("Start 2nd stage for " + ai.name);
					ai.central.makeContent();
					debug.printl("End 2nd stage for " + ai.name);
				});
				runningContentAddThreads.add(thr);
				thr.run();
			}
		});
		waitAllContentRuns();
		
		//Integration runs
		GameContents.addons.forEach((AddonInfo ai) -> {
			if(ai.state == AddonState.ENABLE) {
				Thread thr = new Thread(() -> {
					debug.printl("Start 3rd stage for " + ai.name);
					ai.central.makeContent();
					debug.printl("End 3rd stage for " + ai.name);
				});
				runningIntegrationThreads.add(thr);
				thr.run();
			}
		});
		waitAllIntegrationRuns();
		
		summarizeMods();
		debug.printl("HOORAY, IT'S OVER!");
	}
	
	static void walkDirectory(File folder) throws IOException {
		if(folder.isDirectory()) {
			debug.printl("Directory: " + folder.getCanonicalPath());
		}else {
			debug.printl("File: " + folder.getCanonicalPath());
		}
		
		File[] modfiles = ListFiles.findFiles(folder);
		for(int i = 0; i < modfiles.length; i++) { //Copy over found modfiles
			try {
				debug.printl("File: " + modfiles[i].getCanonicalPath());
				toLoad.add(modfiles[i].getCanonicalPath());
				modCount++;
			} catch (Exception e) {
				debug.pstm(e, "Couldn't load file "+ modfiles[i].getCanonicalPath());
			}
		}
		File[] modpacks = ListFiles.findDirectories(folder);
		for(int i = 0; i < modpacks.length; i++) {
			walkDirectory(modpacks[i]);
		}
	}
	
	static void summarizeMods() {
		GameContents.addons.forEach((AddonInfo ai) -> {summarizeMod(ai);});
	}
	
	static void summarizeMod(AddonInfo ai) {
		debug.printl("=============================================MOD INFORMATION FOR " + ai.name + "=============================================");
		debug.printl("LOCATED AT " + ai.path);

		debug.printl(ai.state.toString());
		try {
			switch(ai.state) {
			case DEAD:
			case DISABLE:
			case ENABLE:
				debug.printl("RELEASED " + ai.mmbmod.release.toString());
				debug.printl("MADE BY " + ai.mmbmod.author);
				debug.printl("DESCRIPTION: " + ai.mmbmod.description);
			default:
				break;
			}
		} catch (Exception e) {
			debug.pstm(e, "Unable to get metadata for " + ai.name);
		}
	}
}