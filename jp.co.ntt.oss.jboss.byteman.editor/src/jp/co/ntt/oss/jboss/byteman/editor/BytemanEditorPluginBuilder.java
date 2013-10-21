package jp.co.ntt.oss.jboss.byteman.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jp.co.ntt.oss.jboss.byteman.editor.editor.BytemanRuleEditor;
import jp.co.ntt.oss.jboss.byteman.editor.util.IOUtils;
import jp.co.ntt.oss.jboss.byteman.editor.validator.BytemanRuleValidator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The project builder for {@link BytemanRuleEditor}.
 * @see BytemanEditorPluginNature
 */
public class BytemanEditorPluginBuilder extends IncrementalProjectBuilder {

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		switch (kind) {
		case FULL_BUILD:
		{
			fullBuild(monitor);
			break;
		}
		case INCREMENTAL_BUILD:
		case AUTO_BUILD:
		{
			incrementalBuild(getDelta(getProject()), monitor);
			break;
		}
		case CLEAN_BUILD:
		{
			cleanBuild(monitor);
			break;
		}
		default:
			break;
		}
		return null;
	}

	protected void fullBuild(IProgressMonitor monitor) {
		class FullBuildVisitor implements IResourceVisitor {

			@Override
			public boolean visit(IResource resource) throws CoreException {
				buildRuleFile(resource);
				return true;
			}
		}

		try {
			getProject().accept(new FullBuildVisitor());
		} catch (CoreException e) {
			BytemanEditorPlugin.logException(e);
		}
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		class IncrementalBuildVistor implements IResourceDeltaVisitor {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();

				switch(delta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
				{
					buildRuleFile(resource);
					break;
				}
				case IResourceDelta.REMOVED:
				{
					break;
				}
				}
				return true;
			}
		}
		delta.accept(new IncrementalBuildVistor());
	}

	protected void cleanBuild(IProgressMonitor monitor) {
		class CleanBuildVisitor implements IResourceVisitor {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if(resource instanceof IFile && resource.getName().endsWith(".btm")) {
					BytemanRuleValidator.deleteMarker(resource);
				}
				return true;
			}
		}
		try {
			getProject().accept(new CleanBuildVisitor());
		} catch (CoreException e) {
			BytemanEditorPlugin.logException(e);
		}
	}

	public static boolean isValidFile(IResource resource) {
		File file = new File(resource.getRawLocationURI());
		if(!file.exists()) {
			return false;
		}
		IJavaProject project = JavaCore.create(resource.getProject());
		if(project != null) {
			List<IPath> outputDirs = new ArrayList<IPath>();
			try {
				outputDirs.add(project.getOutputLocation());
				for(IPackageFragmentRoot root : project.getAllPackageFragmentRoots()) {
					IPath outputPath = root.getRawClasspathEntry().getOutputLocation();
					if(outputPath != null) {
						outputDirs.add(outputPath);
					}
				}
				for(IPath path : outputDirs) {
					if(path.isPrefixOf(resource.getFullPath())) {
						return false;
					}
				}
			} catch (JavaModelException e) {
				return false;
			}
		}
		return true;
	}

	private List<IResource> getRuleFiles(IResource resource) throws CoreException {
		List<IResource> ruleFiles = new ArrayList<IResource>();

		if(resource instanceof IProject || resource instanceof IFolder) {
			for(IResource child : ((IContainer) resource).members()) {
				ruleFiles.addAll(getRuleFiles(child));
			}
		} else if(resource instanceof IFile && resource.getName().endsWith(".btm") && isValidFile(resource)) {
			ruleFiles.add(resource);
		}

		return ruleFiles;
	}

	private void buildRuleFile(IResource resource) throws CoreException {
		List<IResource> ruleFiles = new ArrayList<IResource>();
		if(resource instanceof IFile) {
			String filename = resource.getName();
			if(filename.endsWith(".java") || filename.endsWith(".classpath")) {
				ruleFiles.addAll(getRuleFiles(resource.getProject()));
			} else if(filename.endsWith(".btm") && isValidFile(resource)) {
				ruleFiles.add(resource);
			}

			for(IResource ruleFile : ruleFiles) {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(((IFile)ruleFile).getContents(), ((IFile)ruleFile).getCharset()));
					StringBuilder source = new StringBuilder();
					String line;
					while((line = reader.readLine()) != null) {
						source.append(line).append("\n");
					}
					BytemanRuleValidator.validate(ruleFile, source.toString());
				} catch (CoreException ex) {
					BytemanEditorPlugin.logException(ex);
				} catch (IOException ex) {
					BytemanEditorPlugin.logException(ex);
				} finally {
					IOUtils.closeQuietly(reader);
				}
			}
		}
	}
}
