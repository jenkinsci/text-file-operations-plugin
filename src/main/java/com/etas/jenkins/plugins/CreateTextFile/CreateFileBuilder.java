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
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.IOException;

public class CreateFileBuilder extends Builder {

    private final String textFilePath;
    private final String textFileContent;
    private final String fileOption;
    private final boolean useWorkspace;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CreateFileBuilder(String textFilePath, String textFileContent,String fileOption,boolean useWorkspace){
        this.textFilePath = textFilePath;
        this.textFileContent =  textFileContent;
        this.fileOption = fileOption;
        this.useWorkspace = useWorkspace;
        
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getTextFilePath() {
        return textFilePath;
    }
    
    public String getTextFileContent(){
    	return textFileContent;
    }
    
    public String getFileOption(){
    	return fileOption;
    }
    
    public boolean getUseWorkspace(){
    	return useWorkspace;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	boolean result = false;
    	try {
    		
    		String tempFilePath = textFilePath;
    		if(this.useWorkspace && !(textFilePath.startsWith("${WORKSPACE}")||textFilePath.startsWith("$WORKSPACE")))
    		{
    			tempFilePath = "${WORKSPACE}" + System.getProperty("file.separator", "\\") + textFilePath;
    		}
			String resolvedFilePath = build.getEnvironment(listener).expand(tempFilePath);
			String resolvedContent = build.getEnvironment(listener).expand(textFileContent);
			
			result = launcher.getChannel().call(new CreateFileTask(resolvedFilePath, resolvedContent, fileOption,listener));
			
			
		} catch (Exception e) {

			listener.getLogger().println("Failed to create/update file.");
			listener.getLogger().println(e.getMessage());
			return false;
		} 
    	
        return result;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

       
        public FormValidation doCheckTextFilePath(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set file path.");

            return FormValidation.ok();
        }
        
        public FormValidation doCheckTextFileContent(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please enter the content.");

            return FormValidation.ok();
        }
        
        public FormValidation doCheckFileOption(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please select one option.");

            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Create/Update Text File";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
           
            save();
            return super.configure(req,formData);
        }
        
    }
}

