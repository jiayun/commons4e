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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
public final class ToStringGenerator implements ILangGenerator {

    private static final String STYLE_PREFIX = "org.apache.commons.lang.builder.ToStringStyle";

    private static final String DEFAULT_STYLE = STYLE_PREFIX + "."
            + "DEFAULT_STYLE";

    private static final String MULTI_LINE_STYLE = STYLE_PREFIX + "."
            + "MULTI_LINE_STYLE";

    private static final String NO_FIELD_NAMES_STYLE = STYLE_PREFIX + "."
            + "NO_FIELD_NAMES_STYLE";

    private static final String SIMPLE_STYLE = STYLE_PREFIX + "."
            + "SIMPLE_STYLE";

    private static final String[] STYLES = new String[] { DEFAULT_STYLE,
            MULTI_LINE_STYLE, NO_FIELD_NAMES_STYLE, SIMPLE_STYLE };

    private static final ILangGenerator instance = new ToStringGenerator();

    private ToStringGenerator() {
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

        IMethod existingMethod = objectClass.getMethod("toString",
                new String[0]);
        Set excludedMethods = new HashSet();
        if (existingMethod.exists()) {
            excludedMethods.add(existingMethod);
        }
        try {
            ToStringDialog dialog = new ToStringDialog(parentShell,
                    "Generate ToString Method", objectClass, JavaUtils
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
                String style = dialog.getToStringStyle();

                generateToString(parentShell, objectClass, checkedFields,
                        insertPosition, appendSuper, generateComment, style);
            }

        } catch (CoreException e) {
            MessageDialog.openError(parentShell, "Method Generation Failed", e
                    .getMessage());
        }

    }

    private void generateToString(final Shell parentShell,
            final IType objectClass, final IField[] checkedFields,
            final IJavaElement insertPosition, final boolean appendSuper,
            final boolean generateComment, final String style)
            throws PartInitException, JavaModelException {

        ICompilationUnit cu = objectClass.getCompilationUnit();
        IEditorPart javaEditor = JavaUI.openInEditor(cu);

        String styleConstant = getStyleConstantAndAddImport(style, objectClass);
        String source = createMethod(objectClass, checkedFields, appendSuper,
                generateComment, styleConstant);

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
                "org.apache.commons.lang.builder.ToStringBuilder", null, null);
        IMethod created = objectClass.createMethod(formattedContent,
                insertPosition, true, null);

        JavaUI.revealInEditor(javaEditor, (IJavaElement) created);
    }

    private String getStyleConstantAndAddImport(final String style,
            final IType objectClass) throws JavaModelException {

        String styleConstant = null;
        if (!style.equals(DEFAULT_STYLE) && !style.equals("")) {

            int lastDot = style.lastIndexOf('.');
            if (lastDot != -1 && lastDot != (style.length() - 1)) {

                String styleClass = style.substring(0, lastDot);
                if (styleClass.length() == 0) { return null; }

                int lastDot2 = styleClass.lastIndexOf('.');
                if (lastDot2 != (styleClass.length() - 1)) {

                    styleConstant = style.substring(lastDot2 + 1, style
                            .length());
                    if (lastDot2 != -1) {
                        objectClass.getCompilationUnit().createImport(
                                styleClass, null, null);
                    }
                }
            }
        }
        return styleConstant;
    }

    private String createMethod(final IType objectClass,
            final IField[] checkedFields, final boolean appendSuper,
            final boolean generateComment, final String styleConstant) {

        StringBuffer content = new StringBuffer();
        if (generateComment) {
            content.append("/* (non-Javadoc)\n");
            content.append(" * @see java.lang.Object#toString()\n");
            content.append(" */\n");
        }
        content.append("public String toString() {\n");
        if (styleConstant == null) {
            content.append("return new ToStringBuilder(this)");
        } else {
            content.append("return new ToStringBuilder(this, ");
            content.append(styleConstant);
            content.append(")");
        }
        if (appendSuper) {
            content.append(".appendSuper(super.toString())");
        }
        for (int i = 0; i < checkedFields.length; i++) {
            content.append(".append(\"");
            content.append(checkedFields[i].getElementName());
            content.append("\", ");
            content.append(checkedFields[i].getElementName());
            content.append(")");
        }
        content.append(".toString();\n");
        content.append("}\n\n");

        return content.toString();
    }

    private static class ToStringDialog extends FieldDialog {

        private Combo styleCombo;

        private String toStringStyle;

        private IDialogSettings settings;

        private static final String SETTINGS_SECTION = "ToStringDialog";

        private static final String SETTINGS_STYLE = "ToStringStyle";

        public ToStringDialog(final Shell parentShell,
                final String dialogTitle, final IType objectClass,
                final IField[] fields, final Set excludedMethods)
                throws JavaModelException {

            super(parentShell, dialogTitle, objectClass, fields,
                    excludedMethods);

            IDialogSettings dialogSettings = Commons4ePlugin.getDefault()
                    .getDialogSettings();
            settings = dialogSettings.getSection(SETTINGS_SECTION);
            if (settings == null) {
                settings = dialogSettings.addNewSection(SETTINGS_SECTION);
            }

            toStringStyle = settings.get(SETTINGS_STYLE);
            toStringStyle = toStringStyle == null ? DEFAULT_STYLE
                    : toStringStyle;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#close()
         */
        public boolean close() {
            toStringStyle = styleCombo.getText();
            settings.put(SETTINGS_STYLE, toStringStyle);
            return super.close();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jiayun.commons4e.internal.ui.dialogs.FieldDialog#createOptionComposite(org.eclipse.swt.widgets.Composite)
         */
        protected Composite createOptionComposite(Composite composite) {
            Composite optionComposite = super.createOptionComposite(composite);
            addStyleChoices(optionComposite);
            return optionComposite;
        }

        private Composite addStyleChoices(final Composite composite) {
            Label label = new Label(composite, SWT.NONE);
            label.setText("&ToString style:");

            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            label.setLayoutData(data);

            styleCombo = new Combo(composite, SWT.NONE);
            styleCombo.setItems(STYLES);
            styleCombo.setText(toStringStyle);

            data = new GridData(GridData.FILL_HORIZONTAL);
            styleCombo.setLayoutData(data);

            return composite;
        }

        public String getToStringStyle() {
            return toStringStyle;
        }
    }

}
