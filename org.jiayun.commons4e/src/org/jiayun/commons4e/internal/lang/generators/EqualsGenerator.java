//$Id$
package org.jiayun.commons4e.internal.lang.generators;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.jiayun.commons4e.Commons4ePlugin;
import org.jiayun.commons4e.internal.ui.dialogs.FieldDialog;
import org.jiayun.commons4e.internal.util.JavaUtils;

/**
 * @author jiayun
 */
public final class EqualsGenerator implements ILangGenerator {

    private static final ILangGenerator instance = new EqualsGenerator();

    private EqualsGenerator() {
    }

    public static ILangGenerator getInstance() {
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jiayun.commons4e.internal.lang.generators.ILangGenerator#generate(org.eclipse.swt.widgets.Shell,
     *      org.eclipse.jdt.core.IType)
     */
    public void generate(Shell parentShell, IType objectClass) {

        IMethod existingMethod = objectClass.getMethod("equals",
                new String[] { "QObject;" });
        Set excludedMethods = new HashSet();
        if (existingMethod.exists()) {
            excludedMethods.add(existingMethod);
        }
        try {
            EqualsDialog dialog = new EqualsDialog(parentShell,
                    "Generate Equals Method", objectClass, JavaUtils
                            .getNonStaticFields(objectClass), excludedMethods);
            int returnCode = dialog.open();
            if (returnCode == Window.OK) {

                if (existingMethod.exists()) {
                    existingMethod.delete(true, null);
                }

                IField[] checkedFields = dialog.getCheckedFields();
                IJavaElement insertPosition = dialog.getElementPosition();
                boolean appendSuper = dialog.getAppendSuper();
                boolean generateComment = dialog.getGenerateComment();
                boolean compareReferences = dialog.getCompareReferences();

                generateEquals(parentShell, objectClass, checkedFields,
                        insertPosition, appendSuper, generateComment,
                        compareReferences);
            }

        } catch (CoreException e) {
            MessageDialog.openError(parentShell, "Method Generation Failed", e
                    .getMessage());
        }

    }

    private void generateEquals(final Shell parentShell,
            final IType objectClass, final IField[] checkedFields,
            final IJavaElement insertPosition, final boolean appendSuper,
            final boolean generateComment, final boolean compareReferences)
            throws PartInitException, JavaModelException {

        ICompilationUnit cu = objectClass.getCompilationUnit();
        IEditorPart javaEditor = JavaUI.openInEditor(cu);

        String source = createMethod(objectClass, checkedFields, appendSuper,
                generateComment, compareReferences);

        String lineDelim = JavaUtils.getLineDelimiterUsed(objectClass);
        int indent = JavaUtils.getIndentUsed(objectClass) + 1;

        TextEdit textEdit = ToolFactory.createCodeFormatter(null).format(
                CodeFormatter.K_CLASS_BODY_DECLARATIONS, source, 0,
                source.length(), indent, lineDelim);

        String formattedContent;
        if (textEdit != null) {
            Document document = new Document(source);
            try {
                textEdit.apply(document);
            } catch (BadLocationException e) {
                MessageDialog.openError(parentShell, "Error", e.getMessage());
            }
            formattedContent = document.get();
        } else {
            formattedContent = source;
        }

        objectClass.getCompilationUnit().createImport(
                "org.apache.commons.lang.builder.EqualsBuilder", null, null);
        IMethod created = objectClass.createMethod(formattedContent,
                insertPosition, true, null);

        JavaUI.revealInEditor(javaEditor, (IJavaElement) created);
    }

    private String createMethod(final IType objectClass,
            final IField[] checkedFields, final boolean appendSuper,
            final boolean generateComment, final boolean compareReferences) {

        StringBuffer content = new StringBuffer();
        if (generateComment) {
            content.append("/* (non-Javadoc)\n");
            content
                    .append(" * @see java.lang.Object#equals(java.lang.Object)\n");
            content.append(" */\n");
        }
        content.append("public boolean equals(final Object other) {\n");
        if (compareReferences) {
            content.append("if (this == other) return true;");
        }
        content.append("if ( !(other instanceof ");
        content.append(objectClass.getElementName());
        content.append(") ) return false;\n");
        content.append(objectClass.getElementName());
        content.append(" castOther = (");
        content.append(objectClass.getElementName());
        content.append(") other;\n");
        content.append("return new EqualsBuilder()");
        if (appendSuper) {
            content.append(".appendSuper(super.equals(other))");
        }
        for (int i = 0; i < checkedFields.length; i++) {
            content.append(".append(");
            content.append(checkedFields[i].getElementName());
            content.append(", castOther.");
            content.append(checkedFields[i].getElementName());
            content.append(")");
        }
        content.append(".isEquals();\n");
        content.append("}\n\n");

        return content.toString();
    }

    private static class EqualsDialog extends FieldDialog {

        private boolean compareReferences;

        private IDialogSettings settings;

        private static final String SETTINGS_SECTION = "EqualsDialog";

        private static final String SETTINGS_COMPARE_REFERENCES = "CompareReferences";

        public EqualsDialog(final Shell parentShell, final String dialogTitle,
                final IType objectClass, final IField[] fields,
                final Set excludedMethods) throws JavaModelException {

            super(parentShell, dialogTitle, objectClass, fields,
                    excludedMethods);

            IDialogSettings dialogSettings = Commons4ePlugin.getDefault()
                    .getDialogSettings();
            settings = dialogSettings.getSection(SETTINGS_SECTION);
            if (settings == null) {
                settings = dialogSettings.addNewSection(SETTINGS_SECTION);
            }

            compareReferences = settings
                    .getBoolean(SETTINGS_COMPARE_REFERENCES);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#close()
         */
        public boolean close() {
            settings.put(SETTINGS_COMPARE_REFERENCES, compareReferences);
            return super.close();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jiayun.commons4e.internal.ui.dialogs.FieldDialog#createOptionComposite(org.eclipse.swt.widgets.Composite)
         */
        protected Composite createOptionComposite(Composite composite) {
            Composite optionComposite = super.createOptionComposite(composite);
            addCompareReferencesOption(optionComposite);
            return optionComposite;
        }

        private Composite addCompareReferencesOption(final Composite composite) {

            Button button = new Button(composite, SWT.CHECK);
            button.setText("Compare object &references");
            button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

            button.addSelectionListener(new SelectionListener() {

                public void widgetSelected(SelectionEvent e) {
                    compareReferences = (((Button) e.widget).getSelection());
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                }
            });
            button.setSelection(compareReferences);

            return composite;
        }

        public boolean getCompareReferences() {
            return compareReferences;
        }
    }

}
