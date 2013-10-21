package jp.co.ntt.oss.jboss.byteman.editor.wizard;

import jp.co.ntt.oss.jboss.byteman.editor.BytemanEditorPlugin;
import jp.co.ntt.oss.jboss.byteman.editor.util.StringUtils;
import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentsStore;
import jp.co.ntt.oss.jboss.byteman.editor.view.model.AgentModel.Agent;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for registering an agent to {@link BytemanAgentView}.
 *
 */
public class AgentWizardPage extends WizardPage {

	private static final int MAX_PORT = 65535;

	private Text addressText;

	private Text portText;

	private String initialAddress;
	private String initialPort;

	private String defaultPort = "9091";

	public AgentWizardPage() {
		this(null);
	}

	public AgentWizardPage(Agent agent) {
		super("Agent Wizard Page");
		setTitle(BytemanEditorPlugin.getResourceString("wizard.agent.pageTitle"));
		if(agent != null) {
			setDescription(BytemanEditorPlugin.getResourceString("wizard.agent.edit.description"));
			initialAddress = agent.getAddress();
			initialPort = Integer.toString(agent.getPort());
		} else {
			setDescription(BytemanEditorPlugin.getResourceString("wizard.agent.new.description"));
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, NONE);
		composite.setLayout(new GridLayout(2, false));

		new Label(composite, NONE).setText(BytemanEditorPlugin.getResourceString("page.agentView.address") + ": ");
		addressText = new Text(composite, SWT.BORDER);
		addressText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if(initialAddress != null) {
			addressText.setText(initialAddress);
		}
		addressText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				doValidate();
			}
		});

		new Label(composite, NONE).setText(BytemanEditorPlugin.getResourceString("page.agentView.port") + ": ");
		portText = new Text(composite, SWT.BORDER);
		if(initialPort != null) {
			portText.setText(initialPort);
		} else{
			portText.setText(defaultPort);
		}
		portText.setTextLimit(5);
		portText.setLayoutData(new GridData(40, SWT.DEFAULT));
		portText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				for(int i = 0; i < e.text.length(); i++) {
					if(!Character.isDigit(e.text.charAt(i))) {
						e.doit = false;
						return;
					}
				}
			}
		});
		portText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				doValidate();
			}
		});

		doValidate();
		setControl(composite);
	}

	private void doValidate() {
		if(StringUtils.isEmpty(addressText.getText())) {
			setErrorMessage(BytemanEditorPlugin.getResourceString(
								"error.empty", BytemanEditorPlugin.getResourceString("page.agentView.address")));
			setPageComplete(false);
			return;
		}
		if(StringUtils.isEmpty(portText.getText())) {
			setErrorMessage(BytemanEditorPlugin.getResourceString(
								"error.empty", BytemanEditorPlugin.getResourceString("page.agentView.port")));
			setPageComplete(false);
			return;
		}
		if(Integer.parseInt(portText.getText()) > MAX_PORT) {
			setErrorMessage(BytemanEditorPlugin.getResourceString("error.agent.portMaxOver"));
			setPageComplete(false);
			return;
		}
		if((!addressText.getText().equals(initialAddress) || !portText.getText().equals(initialPort))
				&& AgentsStore.exists(addressText.getText(), Integer.parseInt(portText.getText()))) {
			setErrorMessage(BytemanEditorPlugin.getResourceString(
								"error.dupliateAgent", addressText.getText() + ":" + portText.getText()));
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	public String getAddress() {
		return addressText.getText();
	}

	public int getPort() {
		return Integer.parseInt(portText.getText());
	}
}
