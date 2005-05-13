//$Id$
package org.jiayun.commons4e.internal.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jiayun.commons4e.Commons4ePlugin;

/**
 * @author jiayun
 */
public class Commons4eBasePreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private BooleanFieldEditor cacheHashCode;

    private StringFieldEditor hashCodeField;

    private BooleanFieldEditor cacheToString;

    private StringFieldEditor toStringField;

    public Commons4eBasePreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(Commons4ePlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        cacheHashCode = new BooleanFieldEditor(
                PreferenceConstants.CACHE_HASHCODE,
                "Cache &hashCode when all selected fields are final",
                getFieldEditorParent());
        addField(cacheHashCode);

        hashCodeField = new StringFieldEditor(
                PreferenceConstants.HASHCODE_CACHING_FIELD,
                "Hash&Code caching field", getFieldEditorParent());
        hashCodeField.setEmptyStringAllowed(false);
        addField(hashCodeField);

        cacheToString = new BooleanFieldEditor(
                PreferenceConstants.CACHE_TOSTRING,
                "Cache &toString when all selected fields are final",
                getFieldEditorParent());
        addField(cacheToString);

        toStringField = new StringFieldEditor(
                PreferenceConstants.TOSTRING_CACHING_FIELD,
                "To&String caching field", getFieldEditorParent());
        toStringField.setEmptyStringAllowed(false);
        addField(toStringField);
    }

    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            if (event.getSource() == cacheHashCode) {
                hashCodeField.setEnabled(cacheHashCode.getBooleanValue(),
                        getFieldEditorParent());
                if (!cacheHashCode.getBooleanValue()
                        && !hashCodeField.isValid()) {
                    hashCodeField.loadDefault();
                }
            }

            if (event.getSource() == cacheToString) {
                toStringField.setEnabled(cacheToString.getBooleanValue(),
                        getFieldEditorParent());
                if (!cacheToString.getBooleanValue()
                        && !toStringField.isValid()) {
                    toStringField.loadDefault();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }
}
