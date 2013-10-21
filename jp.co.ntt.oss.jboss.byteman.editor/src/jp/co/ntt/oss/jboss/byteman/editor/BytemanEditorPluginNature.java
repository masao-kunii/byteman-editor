package jp.co.ntt.oss.jboss.byteman.editor;

import java.util.ArrayList;
import java.util.Arrays;

import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The project nature for {@link BytemanRuleEditor}. It can configure or de-configure
 * a project with this nature.
 * @see BytemanEditorPluginBuilder
 */
public class BytemanEditorPluginNature implements IProjectNature {

	private IProject project;

	@Override
	public void configure() throws CoreException {
		try {
			IProjectDescription description = project.getDescription();
			ArrayList<ICommand> builders = new ArrayList<ICommand>(Arrays.asList(description.getBuildSpec()));
			ICommand command = description.newCommand();
			command.setBuilderName(BytemanEditorPlugin.BUILDER_ID);
			if(!builders.contains(command)) {
				builders.add(command);
				description.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
				project.setDescription(description, null);
			}
		} catch (CoreException e) {
			BytemanEditorPlugin.logException(e);
		}
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ArrayList<ICommand> builders = new ArrayList<ICommand>(Arrays.asList(description.getBuildSpec()));
		ICommand command = description.newCommand();
		command.setBuilderName(BytemanEditorPlugin.BUILDER_ID);
		if(builders.contains(command)) {
			builders.remove(command);
			description.setBuildSpec(builders.toArray(new ICommand[builders.size()]));
			project.setDescription(description, null);
		}
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
