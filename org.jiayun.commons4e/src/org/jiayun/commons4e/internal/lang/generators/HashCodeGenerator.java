//$Id$
package org.jiayun.commons4e.internal.lang.generators;

import java.util.HashSet;
import java.util.Random;
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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.jiayun.commons4e.Commons4ePlugin;
import org.jiayun.commons4e.internal.ui.dialogs.FieldDialog;
import org.jiayun.commons4e.internal.util.JavaUtils;

/**
 * @author jiayun
 */
public final class HashCodeGenerator implements ILangGenerator {

    private static final ILangGenerator instance = new HashCodeGenerator();

    private HashCodeGenerator() {
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

        IMethod existingMethod = objectClass.getMethod("hashCode",
                new String[0]);
        Set excludedMethods = new HashSet();
        if (existingMethod.exists()) {
            excludedMethods.add(existingMethod);
        }
        try {
            HashCodeDialog dialog = new HashCodeDialog(parentShell,
                    "Generate HashCode Method", objectClass, JavaUtils
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
                IInitMultNumbers imNumbers = dialog.getInitMultNumbers();

                generateHashCode(parentShell, objectClass, checkedFields,
                        insertPosition, appendSuper, generateComment, imNumbers);
            }

        } catch (CoreException e) {
            MessageDialog.openError(parentShell, "Method Generation Failed", e
                    .getMessage());
        }

    }

    private void generateHashCode(final Shell parentShell,
            final IType objectClass, final IField[] checkedFields,
            final IJavaElement insertPosition, final boolean appendSuper,
            final boolean generateComment, final IInitMultNumbers imNumbers)
            throws PartInitException, JavaModelException {

        ICompilationUnit cu = objectClass.getCompilationUnit();
        IEditorPart javaEditor = JavaUI.openInEditor(cu);

        String source = createMethod(objectClass, checkedFields, appendSuper,
                generateComment, imNumbers);

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
                "org.apache.commons.lang.builder.HashCodeBuilder", null, null);
        IMethod created = objectClass.createMethod(formattedContent,
                insertPosition, true, null);

        JavaUI.revealInEditor(javaEditor, (IJavaElement) created);
    }

    private String createMethod(final IType objectClass,
            final IField[] checkedFields, final boolean appendSuper,
            final boolean generateComment, final IInitMultNumbers imNumbers) {

        StringBuffer content = new StringBuffer();
        if (generateComment) {
            content.append("/* (non-Javadoc)\n");
            content.append(" * @see java.lang.Object#hashCode()\n");
            content.append(" */\n");
        }
        content.append("public int hashCode() {\n");
        content.append("return new HashCodeBuilder(");
        content.append(imNumbers.getValue());
        content.append(")");
        if (appendSuper) {
            content.append(".appendSuper(super.hashCode())");
        }
        for (int i = 0; i < checkedFields.length; i++) {
            content.append(".append(");
            content.append(checkedFields[i].getElementName());
            content.append(")");
        }
        content.append(".toHashCode();\n");
        content.append("}\n\n");

        return content.toString();
    }

    private static class HashCodeDialog extends FieldDialog {

        private Button imButtons[] = new Button[3];

        private Text initText;

        private Text multText;

        private IInitMultNumbers imNumbers[] = new IInitMultNumbers[] {
                new DefaultInitMultNumbers(), new RandomInitMultNumbers(),
                new CustomInitMultNumbers() };

        private int initMultType;

        private int initialNumber;

        private int multiplierNumber;

        private IDialogSettings settings;

        private static final String SETTINGS_SECTION = "HashCodeDialog";

        private static final String SETTINGS_INIT_MULT_TYPE = "InitMultType";

        private static final String SETTINGS_INITIAL_NUMBER = "InitialNumber";

        private static final String SETTINGS_MULTIPLIER_NUMBER = "MultiplierNumber";

        public HashCodeDialog(final Shell parentShell,
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

            try {
                initMultType = settings.getInt(SETTINGS_INIT_MULT_TYPE);
            } catch (NumberFormatException e) {
                initMultType = 0;
            }

            try {
                initialNumber = settings.getInt(SETTINGS_INITIAL_NUMBER);
            } catch (NumberFormatException e) {
                initialNumber = 17;
            }

            try {
                multiplierNumber = settings.getInt(SETTINGS_MULTIPLIER_NUMBER);
            } catch (NumberFormatException e) {
                multiplierNumber = 37;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#close()
         */
        public boolean close() {
            imNumbers[initMultType].setNumbers(initialNumber, multiplierNumber);
            settings.put(SETTINGS_INIT_MULT_TYPE, initMultType);
            settings.put(SETTINGS_INITIAL_NUMBER, initialNumber);
            settings.put(SETTINGS_MULTIPLIER_NUMBER, multiplierNumber);
            return super.close();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#create()
         */
        public void create() {
            super.create();

            imButtons[initMultType].setSelection(true);
            initText.setText(String.valueOf(initialNumber));
            multText.setText(String.valueOf(multiplierNumber));
            if (initMultType != 2) {
                initText.setEnabled(false);
                multText.setEnabled(false);
            }
            fieldViewer.getTable().setFocus();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jiayun.commons4e.internal.ui.dialogs.FieldDialog#createOptionComposite(org.eclipse.swt.widgets.Composite)
         */
        protected Composite createOptionComposite(Composite composite) {
            Composite optionComposite = super.createOptionComposite(composite);
            addInitialMultiplierOptions(optionComposite);
            return optionComposite;
        }

        private void addInitialMultiplierOptions(final Composite composite) {
            Group group = new Group(composite, SWT.NONE);
            group.setText("Initial and multiplier numbers");
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            GridLayout layout = new GridLayout(4, false);
            group.setLayout(layout);

            imButtons[0] = new Button(group, SWT.RADIO);
            imButtons[0].setText("D&efault");
            GridData data = new GridData();
            data.horizontalSpan = 4;
            imButtons[0].setLayoutData(data);

            imButtons[1] = new Button(group, SWT.RADIO);
            imButtons[1].setText("&Random generate");
            data = new GridData();
            data.horizontalSpan = 4;
            imButtons[1].setLayoutData(data);

            imButtons[2] = new Button(group, SWT.RADIO);
            imButtons[2].setText("C&ustom:");
            data = new GridData();
            data.horizontalSpan = 4;
            imButtons[2].setLayoutData(data);

            Label initLabel = new Label(group, SWT.NONE);
            initLabel.setText("Initial:");
            data = new GridData();
            data.horizontalIndent = 30;
            initLabel.setLayoutData(data);

            initText = new Text(group, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
            initText.addVerifyListener(new IntegerVerifyListener(initText));
            initText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    checkInput();
                }

            });
            data = new GridData(GridData.FILL_HORIZONTAL);
            initText.setLayoutData(data);

            Label multLabel = new Label(group, SWT.NONE);
            multLabel.setText("Multiplier:");
            data = new GridData();
            data.horizontalIndent = 20;
            multLabel.setLayoutData(data);

            multText = new Text(group, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
            multText.addVerifyListener(new IntegerVerifyListener(multText));
            multText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    checkInput();
                }

            });
            data = new GridData(GridData.FILL_HORIZONTAL);
            multText.setLayoutData(data);

            imButtons[0].addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    Button button = (Button) e.widget;
                    if (button.getSelection()) {
                        initMultType = 0;
                    }
                }
            });

            imButtons[1].addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    Button button = (Button) e.widget;
                    if (button.getSelection()) {
                        initMultType = 1;
                    }
                }
            });

            imButtons[2].addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    Button button = (Button) e.widget;
                    if (button.getSelection()) {
                        initMultType = 2;
                        initText.setEnabled(true);
                        multText.setEnabled(true);
                    } else {
                        initText.setEnabled(false);
                        multText.setEnabled(false);
                    }
                }
            });

        }

        private void checkInput() {
            String text = initText.getText();
            int init;
            try {
                init = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                showNotOddMessage("Initial number");
                return;
            }
            if (init % 2 == 0) {
                showNotOddMessage("Initial Number");
                return;
            }
            initialNumber = init;

            text = multText.getText();
            int mult;
            try {
                mult = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                showNotOddMessage("Multiplier number");
                return;
            }
            if (mult % 2 == 0) {
                showNotOddMessage("Multiplier Number");
                return;
            }
            multiplierNumber = mult;
            clearMessage();
        }

        private void showNotOddMessage(String title) {
            messageLabel.setImage(JFaceResources
                    .getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
            messageLabel.setText(title + " must be an odd number.");
            messageLabel.setVisible(true);
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        }

        private void clearMessage() {
            messageLabel.setImage(null);
            messageLabel.setText(null);
            messageLabel.setVisible(false);
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }

        public IInitMultNumbers getInitMultNumbers() {
            return imNumbers[initMultType];
        }

    }

    private static interface IInitMultNumbers {

        void setNumbers(int initial, int multiplier);

        String getValue();
    }

    private static class DefaultInitMultNumbers implements IInitMultNumbers {

        public void setNumbers(int initial, int multiplier) {
        }

        public String getValue() {
            return "";
        }

    }

    private static class RandomInitMultNumbers implements IInitMultNumbers {

        private static Random random = new Random();

        public void setNumbers(int initial, int multiplier) {
        }

        public String getValue() {

            int initial = random.nextInt();
            int multiplier = random.nextInt();

            initial = initial % 2 == 0 ? initial + 1 : initial;
            multiplier = multiplier % 2 == 0 ? multiplier + 1 : multiplier;

            return String.valueOf(initial) + ", " + String.valueOf(multiplier);
        }

    }

    private static class CustomInitMultNumbers implements IInitMultNumbers {

        int initial;

        int multiplier;

        public void setNumbers(int initial, int multiplier) {
            this.initial = initial;
            this.multiplier = multiplier;
        }

        public String getValue() {
            return String.valueOf(initial) + ", " + String.valueOf(multiplier);
        }

    }

    private static class IntegerVerifyListener implements VerifyListener {

        private Text inputText;

        public IntegerVerifyListener(Text inputText) {
            super();
            this.inputText = inputText;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.VerifyListener#verifyText(org.eclipse.swt.events.VerifyEvent)
         */
        public void verifyText(VerifyEvent e) {

            StringBuffer number = new StringBuffer(inputText.getText());
            number.insert(e.start, e.text);

            try {
                Integer.parseInt(number.toString());
            } catch (NumberFormatException nfe) {
                e.doit = false;
                return;
            }
        }

    }

}
