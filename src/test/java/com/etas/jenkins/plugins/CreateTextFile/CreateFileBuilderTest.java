/*
The MIT License (MIT)

Copyright (c) 2015 Sanketh P B

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.etas.jenkins.plugins.CreateTextFile;

import static org.junit.Assert.assertTrue;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

public class CreateFileBuilderTest {
	
	@Rule public JenkinsRule j = new JenkinsRule();
	
	@Test
	public void testCreateNewFile() throws InterruptedException, IOException, Exception {
	
		EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("FILE_NAME", "NewFile.txt");
        envVars.put("LINE1", "NewFile");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        
        CreateFileBuilder builder = new CreateFileBuilder("${WORKSPACE}"+ "/" +"${FILE_NAME}", "${LINE1}", "overWrite",false);
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String content = build.getWorkspace().child("NewFile.txt").readToString();
        assertTrue(content.contains("NewFile"));        
        
	}
	
	@Test
	public void testInsertAtBeginFile() throws InterruptedException, IOException, Exception {
		
		EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("FILE_NAME", "ExistingFile.txt");
        envVars.put("LINE2", "Line2");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        
        project.getBuildersList().add(new TestBuilder() {
			
			@Override
			public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
					BuildListener listener) throws InterruptedException, IOException {
				
				build.getWorkspace().child("ExistingFile.txt").write("LINE1", "UTF-8");
				
				return true;
				
			}
		});
        
        
        CreateFileBuilder builder = new CreateFileBuilder("${FILE_NAME}", "${LINE2}", "insertAtStart",true);
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        InputStream content = build.getWorkspace().child("ExistingFile.txt").read();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        String firstLine = reader.readLine();
        assertTrue(firstLine.equals("Line2"));   
        String fullContent =  build.getWorkspace().child("ExistingFile.txt").readToString();
        assertTrue(fullContent.contains("LINE1"));
	
	}
	
	@Test
	public void testAppendToEndFile() throws InterruptedException, IOException, Exception {
	
		EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("FILE_NAME", "ExistingFile.txt");
        envVars.put("LINE2", "Line2");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        
        project.getBuildersList().add(new TestBuilder() {
			
			@Override
			public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
					BuildListener listener) throws InterruptedException, IOException {
				
				build.getWorkspace().child("ExistingFile.txt").write("LINE1", "UTF-8");
				
				return true;
				
			}
		});
        
        
        CreateFileBuilder builder = new CreateFileBuilder("${WORKSPACE}"+ "/" +"${FILE_NAME}", "${LINE2}", "appendToEnd",true);
        project.getBuildersList().add(builder);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String filePath = build.getWorkspace().child("ExistingFile.txt").getRemote();

        String firstLine = (String)FileUtils.readLines(new File(filePath)).get(0);
        String secondLine = (String)FileUtils.readLines(new File(filePath)).get(2);
        assertTrue(firstLine.equals("LINE1"));
        assertTrue(secondLine.equals("Line2"));    
	}

}
