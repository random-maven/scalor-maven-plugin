package com.carrotgarden.maven.scalor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;
import org.eclipse.m2e.importer.internal.MavenProjectConfigurator;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.m2e.tests.common.JobHelpers;
import org.eclipse.m2e.tests.common.JobHelpers.IJobMatcher;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportJob;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Verify companion Eclipse plugin setup, Scala project import and
 * configuration.
 */
@SuppressWarnings("restriction")
public class Scalor_01_Test extends AbstractMavenProjectTestCase {

	File testerDir;
	File testerMasterDir;
	File testerModuleDir;

	File sourceDir;
	File sourceMasterDir;
	File sourceModuleDir;

	File targetDir;
	File targetMasterDir;
	File targetModuleDir;

	static void removeMeta(File baseDir) throws Exception {
		new File(baseDir, ".project").delete();
		new File(baseDir, ".classpath").delete();
		FileUtils.deleteDirectory(new File(baseDir, ".settings"));
	}

	static void assertEqualsText(File sourceFile, File targetFile) throws Exception {
		assertTrue("Source file exists", sourceFile.exists());
		assertTrue("Target file exists", targetFile.exists());
		String sourceText = FileUtils.readFileToString(sourceFile, "UTF-8");
		String targetText = FileUtils.readFileToString(targetFile, "UTF-8");
		// String difference = StringUtils.difference(sourceText, targetText);
		String assertText = sourceFile + " vs " + targetFile + "\n" //
				+ "--- sourceText ---\n" + sourceText //
				+ "--- targetText ---\n" + targetText //
		// + "--- difference ---\n" + difference //
		;
		assertTrue(assertText, sourceText.equals(targetText));
	}

	static void assertEqualsProps(File sourceFile, File targetFile, String[] nameList) throws Exception {
		assertTrue("Source file exists", sourceFile.exists());
		assertTrue("Target file exists", targetFile.exists());
		String assertText = sourceFile + " vs " + targetFile;
		Properties sourceProps = new Properties();
		sourceProps.load(new FileInputStream(sourceFile));
		Properties targetProps = new Properties();
		targetProps.load(new FileInputStream(targetFile));
		for (String name : nameList) {
			String source = sourceProps.getProperty(name);
			String target = targetProps.getProperty(name);
			assertEquals(assertText + " @ " + name, source, target);
		}
	}

	static Job findJob(String name) {
		Job[] list = Job.getJobManager().find(null);
		for (Job job : list) {
			if (job.getName().equals(name)) {
				return job;
			}
		}
		return null;
	}

	static void log(String text) {
		System.err.println("XXX " + text);
	}

	static void awaitIdle() throws Exception {
		log("start@idle");
		int count = 0;
		int limit = 3;
		IJobManager manager = Job.getJobManager();
		while (true) {
			if (manager.isIdle()) {
				count += 1;
			} else {
				count = 0;
			}
			if (count >= limit) {
				break;
			} else {
				Thread.sleep(1000);
			}
		}
		log("finish@idle");
	}

	static void awaitWork(String title, Job job) throws Exception {
		log("enter@" + title);
		while (true) {
			if (job.getState() == Job.RUNNING) {
				log("start@" + title);
				break;
			} else {
				Thread.sleep(100);
			}
		}
		job.join();
		log("finish@" + title);
	}

	@Before
	public void setUp() throws Exception {
		String masterDir = "master-1";
		String moduleDir = "module-1";
		// keep outside current workspace
		testerDir = Files.createTempDirectory("scalor-test-m2e@" + System.currentTimeMillis() + "@").toFile();
		testerMasterDir = new File(testerDir, masterDir);
		testerModuleDir = new File(testerMasterDir, moduleDir);
		//
		sourceDir = new File("resources/source");
		sourceMasterDir = new File(sourceDir, masterDir);
		sourceModuleDir = new File(sourceMasterDir, moduleDir);
		//
		targetDir = new File("resources/target");
		targetMasterDir = new File(targetDir, masterDir);
		targetModuleDir = new File(targetMasterDir, moduleDir);
		//
		FileUtils.deleteDirectory(testerDir);
		testerMasterDir.mkdirs();
		copyDir(sourceMasterDir, testerMasterDir);
		//
		removeMeta(testerMasterDir);
		removeMeta(testerModuleDir);
		//
		assertTrue("Tester master has pom.xml", new File(testerMasterDir, "pom.xml").exists());
		assertFalse("Tester master has .project", new File(testerMasterDir, ".project").exists());
		//
		assertTrue("Tester module has pom.xml", new File(testerModuleDir, "pom.xml").exists());
		assertFalse("Tester module has .project", new File(testerModuleDir, ".project").exists());
	}

	@After
	public void tearDown() throws Exception {
		// FileUtils.deleteDirectory(testerDir); // manual review
	}

	@Test
	public void testScalaProjectImport() throws Exception {

		log("test start");

		awaitIdle();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		Set<IProject> projectsInitial = new HashSet<>(Arrays.asList(workspaceRoot.getProjects()));

		SmartImportJob importJob = new SmartImportJob(testerMasterDir, Collections.emptySet(), true, true);

		Map<File, List<ProjectConfigurator>> proposals = importJob.getImportProposals(monitor);
		assertEquals("Expected 2 projects to import", 2, proposals.size());

		boolean hasConfigurator = false;
		for (ProjectConfigurator configurator : proposals.values().iterator().next()) {
			if (configurator instanceof MavenProjectConfigurator) {
				hasConfigurator = true;
			}
		}
		assertTrue("Configurator present", hasConfigurator);

		// start import
		String importName = "Import @" + System.currentTimeMillis();
		importJob.setDirectoriesToImport(proposals.keySet()); // accept proposals
		importJob.setName(importName);
		importJob.setPriority(10);// interact
		importJob.schedule(0); // start delay

		// await import
		awaitWork("import", importJob);

		// await config
		Job configJob = findJob(MavenProjectConfigurator.UPDATE_MAVEN_CONFIGURATION_JOB_NAME);
		awaitWork("config", configJob);

		awaitIdle();

		Set<IProject> projectsFinal = new HashSet<>(Arrays.asList(workspaceRoot.getProjects()));

		Set<IProject> projectsDifference = new HashSet<>();
		projectsDifference.addAll(projectsFinal);
		projectsDifference.removeAll(projectsInitial);
		assertEquals("Expected 2 projects", 2, projectsDifference.size());

		IProject moduleProject = null;
		for (IProject project : projectsDifference) {
			assertTrue("Project has expected location",
					project.getLocation().toFile().getCanonicalPath().startsWith(testerMasterDir.getCanonicalPath()));
			IMavenProjectFacade mavenFacade = MavenPlugin.getMavenProjectRegistry().getProject(project);
			assertNotNull("Project configured as Maven", mavenFacade);
			MavenProject mavenProject = mavenFacade.getMavenProject(monitor);
			String artifactId = mavenProject.getArtifactId();
			if (artifactId.equals("scalor-test-module-1")) {
				moduleProject = project;
			}
		}
		assertNotNull("Module project present", moduleProject);

		awaitIdle();

		// start update
		String updateName = "Update @" + System.currentTimeMillis();
		UpdateMavenProjectJob updateJob = new UpdateMavenProjectJob(new IProject[] { moduleProject });
		updateJob.setName(updateName);
		updateJob.setPriority(10);// interact
		updateJob.schedule(0); // start delay

		// await update
		awaitWork("update", updateJob);

		// await scalor
		// Job scalorJob = findJob("Scalor: update project settings for Scala IDE");
		// awaitWork("scalor", scalorJob);

		awaitIdle();

		// match ".project"
		String metaProject = ".project";
		File testerProject = new File(testerModuleDir, metaProject);
		File targetProject = new File(targetModuleDir, metaProject);
		assertEqualsText(testerProject, targetProject);

		// match ".classpath"
		String metaClasspath = ".classpath";
		File testerClasspath = new File(testerModuleDir, metaClasspath);
		File targetClasspath = new File(targetModuleDir, metaClasspath);
		// FIXME
		// assertEqualsText(testerClasspath, targetClasspath);

		// match ".settings/scala-ide"
		String metaScalaIDE = ".settings/org.scala-ide.sdt.core.prefs";
		File testerSettings = new File(testerModuleDir, metaScalaIDE);
		File targetSettings = new File(targetModuleDir, metaScalaIDE);
		String[] nameList = new String[] { //
				"//src/macro/java", //
				"//src/macro/scala", //
				"//src/main/java", //
				// "//src/main/resources", //
				"//src/main/scala", //
				"//src/test/java", //
				// "//src/test/resources", //
				"//src/test/scala", //
				"Xmaxerrs", //
				"compileorder", //
				"deprecation", //
				"encoding", //
				"feature", //
				"target", //
				"unchecked", //
				"scala.compiler.sourceLevel", //
				"useScopesCompiler", //
		};
		assertEqualsProps(testerSettings, targetSettings, nameList);

		// Thread.sleep(300 * 1000); // manual testing

	}

}
