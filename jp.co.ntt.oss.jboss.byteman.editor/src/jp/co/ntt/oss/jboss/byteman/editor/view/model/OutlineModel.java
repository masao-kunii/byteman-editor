package jp.co.ntt.oss.jboss.byteman.editor.view.model;

import jp.co.ntt.oss.jboss.byteman.editor.view.BytemanRuleOutlinePage;

/**
 * This interface extends {@link Model} for the {@link BytemanRuleOutlinePage}.
 *
 */

public interface OutlineModel extends Model {
	public int getLineNumber();
	public int getLineOfOffset();
	public int getLength();
}
