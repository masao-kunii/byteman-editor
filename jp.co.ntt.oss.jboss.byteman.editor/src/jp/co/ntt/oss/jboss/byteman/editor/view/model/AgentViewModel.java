package jp.co.ntt.oss.jboss.byteman.editor.view.model;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanAgentView;

/**
 * The base class of the {@link Model} for the {@link BytemanAgentView}.
 *
 */
public abstract class AgentViewModel implements Model {

	public Color getForeground() {
		return null;
	}

	public Font getFont() {
		return null;
	}

}
