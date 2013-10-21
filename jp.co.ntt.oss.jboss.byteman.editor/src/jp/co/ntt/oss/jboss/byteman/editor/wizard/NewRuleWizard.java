package jp.co.ntt.oss.jboss.byteman.editor.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPluginImages;
import jp.co.ntt.oss.jboss.byteman.editor.action.template.AbstractTemplateHandler.RuleTemplate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * Wizard for creating a rule file from rule template.
 *
 */
public class NewRuleWizard extends BasicNewResourceWizard {

	private WizardNewFileCreationPage mainPage;

	private String ruleTemplate;

	public NewRuleWizard(String ruleTemplate) {
		super();
		this.ruleTemplate = ruleTemplate;
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new NewRuleFileCreationPage(getSelection(), ruleTemplate);
		mainPage.setTitle(BytemanEditorPlugin.getResourceString("wizard.newRule.pageTitle"));
		mainPage.setDescription(BytemanEditorPlugin.getResourceString("wizard.newRule.description"));
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(BytemanEditorPlugin.getResourceString("wizard.newRule.title"));
		setNeedsProgressMonitor(true);
	}

	@Override
	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(BytemanEditorPluginImages.DESC_NEWFILE_WIZARD);
	}

	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
        if (file == null || !file.exists()) {
			return false;
		}
        selectAndReveal(file);
        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
			BytemanEditorPlugin.logException(e);
        	return false;
        }

        return true;
	}

	private static class NewRuleFileCreationPage extends WizardNewFileCreationPage {

		private String ruleTemplate;

		public NewRuleFileCreationPage(IStructuredSelection selection, String ruleTemplate) {
			super("newRulePage1", selection);
			this.ruleTemplate = ruleTemplate;
		}

		@Override
		protected InputStream getInitialContents() {
			return new ByteArrayInputStream(ruleTemplate.getBytes());
		}
	}

	public static void openWizardDialog(IWorkbenchSite site, RuleTemplate ruleTemplate, IProject project) {
		NewRuleWizard wizard = new NewRuleWizard(ruleTemplate.toString());
		wizard.init(site.getWorkbenchWindow().getWorkbench(), new StructuredSelection(project));
		WizardDialog dialog = new WizardDialog(site.getShell(), wizard);
		dialog.setHelpAvailable(false);
		dialog.create();
		((WizardNewFileCreationPage) dialog.getCurrentPage()).setFileName("rule.btm");
		dialog.open();
	}

}
