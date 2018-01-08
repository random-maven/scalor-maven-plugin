package com.carrotgarden.maven.scalor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.FileUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.wizards.datatransfer.SmartImportJob;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.importer.internal.MavenProjectConfigurator;
import org.eclipse.m2e.tests.common.AbstractMavenProjectTestCase;
import org.eclipse.m2e.tests.common.JobHelpers;
import org.eclipse.m2e.tests.common.JobHelpers.IJobMatcher;

@SuppressWarnings("restriction")
public class MavenImporterTest extends AbstractMavenProjectTestCase {

	private File projectDirectory;

	@Before
	public void setUp() throws IOException {

		projectDirectory = new File(Files.createTempDirectory("m2e-tests").toFile(), "example1");

		FileUtils.deleteDirectory(projectDirectory);

		projectDirectory.mkdirs();

		copyDir(new File("resources/examples/example1"), projectDirectory);

		new File(projectDirectory, ".project").delete();
		new File(projectDirectory, ".classpath").delete();
		new File(projectDirectory, "module1/.project").delete();
		new File(projectDirectory, "module1/.classpath").delete();
	}

	@After
	public void tearDown() throws IOException {

		FileUtils.deleteDirectory(projectDirectory);

	}

	@Test
	public void testBasicImport() throws Exception {

		SmartImportJob job = new SmartImportJob(projectDirectory, Collections.EMPTY_SET, true, true);

		Map<File, List<ProjectConfigurator>> proposals = job.getImportProposals(monitor);
		Assert.assertEquals("Expected 2 projects to import", 2, proposals.size());

		boolean hasConfigurator = false;
		for (ProjectConfigurator configurator : proposals.values().iterator().next()) {
			if (configurator instanceof MavenProjectConfigurator) {
				hasConfigurator = true;
			}
		}
		Assert.assertTrue("Maven configurator is present", hasConfigurator);

		// accept proposals
		job.setDirectoriesToImport(proposals.keySet());

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		Set<IProject> projectSetBefore = new HashSet<>(Arrays.asList(workspaceRoot.getProjects()));

		job.run(monitor);
		job.join();

		Set<IProject> projectSetAfter = new HashSet<>(Arrays.asList(workspaceRoot.getProjects()));
		projectSetAfter.removeAll(projectSetBefore);

		Assert.assertEquals("Expected 2 projects", 2, projectSetAfter.size());

		JobHelpers.waitForJobs(new IJobMatcher() {
			public boolean matches(Job job) {
				return MavenProjectConfigurator.UPDATE_MAVEN_CONFIGURATION_JOB_NAME.equals(job.getName());
			}
		}, 30_000);

		for (IProject project : projectSetAfter) {
			Assert.assertTrue(
					project.getLocation().toFile().getCanonicalPath().startsWith(projectDirectory.getCanonicalPath()));
			IMavenProjectFacade mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(project);
			Assert.assertNotNull("Project configured as Maven", mavenProject);
		}
	}

	@Test
	public void testRootWithoutPom() throws Exception {

		// important part here is the "getParentFile()"

		SmartImportJob job = new SmartImportJob(projectDirectory.getParentFile(), Collections.EMPTY_SET, true, true);

		Map<File, List<ProjectConfigurator>> proposals = job.getImportProposals(monitor);

		Assert.assertEquals("Expected 2 projects", 2, proposals.size());

		boolean hasConfigurator = false;

		for (ProjectConfigurator configurator : proposals.values().iterator().next()) {
			if (configurator instanceof MavenProjectConfigurator) {
				hasConfigurator = true;
			}
		}
		Assert.assertTrue("Maven configurator present", hasConfigurator);

		// accept proposals
		job.setDirectoriesToImport(proposals.keySet());

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		Set<IProject> projectSetBefore = new HashSet<>(Arrays.asList(workspaceRoot.getProjects()));

		job.run(monitor);
		job.join();

		Set<IProject> projectSetAfter = new HashSet<>(Arrays.asList(workspaceRoot.getProjects()));
		projectSetAfter.removeAll(projectSetBefore);

		Assert.assertEquals("Expected 2 projects", 2, projectSetAfter.size());

		JobHelpers.waitForJobs(new IJobMatcher() {
			public boolean matches(Job job) {
				return MavenProjectConfigurator.UPDATE_MAVEN_CONFIGURATION_JOB_NAME.equals(job.getName());
			}
		}, 30_000);

		for (IProject project : projectSetAfter) {
			Assert.assertTrue(
					project.getLocation().toFile().getCanonicalPath().startsWith(projectDirectory.getCanonicalPath()));
			IMavenProjectFacade mavenProject = MavenPlugin.getMavenProjectRegistry().getProject(project);
			Assert.assertNotNull("Project configured as Maven", mavenProject);
		}

	}

}
