/*
 * Created on 2004/7/28
 */
package org.jiayun.commons4e.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.jiayun.commons4e.Commons4ePlugin;
import org.jiayun.commons4e.internal.util.JavaUtils;

/*
 * This class contains some code from
 *      org.eclipse.jdt.internal.ui.dialogs.SourceActionDialog 
 */
/**
 * @author jiayun
 */
public class FieldDialog extends Dialog {

    private String title;

    private IType objectClass;

    private CheckboxTableViewer fieldViewer;

    private IField[] fields;

    private IField[] checkedFields;

    private List insertPositions;

    private List insertPositionLabels;

    private int currentPositionIndex;

    private boolean appendSuper;

    private boolean generateComment;

    private IDialogSettings settings;

    private static final String SETTINGS_SECTION = "FieldDialog";

    private static final String SETTINGS_INSERT_POSITION = "InsertPosition";

    private static final String SETTINGS_APPEND_SUPER = "AppendSuper";

    private static final String SETTINGS_GENERATE_COMMENT = "GenerateComment";

    public FieldDialog(Shell parentShell, String dialogTitle,
            IType objectClass, IField[] fields) throws JavaModelException {
        super(parentShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        this.title = dialogTitle;
        this.objectClass = objectClass;
        this.fields = fields;

        IDialogSettings dialogSettings = Commons4ePlugin.getDefault()
                .getDialogSettings();
        settings = dialogSettings.getSection(SETTINGS_SECTION);
        if (settings == null) {
            settings = dialogSettings.addNewSection(SETTINGS_SECTION);
        }
        
        try {
            currentPositionIndex = settings.getInt(SETTINGS_INSERT_POSITION);
        } catch (NumberFormatException e) {
            currentPositionIndex = 0;
        }
        appendSuper = settings.getBoolean(SETTINGS_APPEND_SUPER);
        generateComment = settings.getBoolean(SETTINGS_GENERATE_COMMENT);

        insertPositions = new ArrayList();
        insertPositionLabels = new ArrayList();

        IJavaElement[] members = objectClass.getChildren();
        IMethod[] methods = objectClass.getMethods();

        insertPositions.add(methods.length > 0 ? methods[0] : null); // first
        insertPositions.add(null); // last

        insertPositionLabels.add("First method");
        insertPositionLabels.add("Last method");

        for (int i = 0; i < methods.length; i++) {
            IMethod curr = methods[i];
            String methodLabel = JavaUtils.getMethodLabel(curr);
            insertPositionLabels.add("After " + methodLabel);
            insertPositions.add(findSibling(curr, members));
        }
        insertPositions.add(null);
    }

    private IJavaElement findSibling(final IMethod curr,
            final IJavaElement[] members) throws JavaModelException {
        IJavaElement res = null;
        int methodStart = curr.getSourceRange().getOffset();
        for (int i = members.length - 1; i >= 0; i--) {
            IMember member = (IMember) members[i];
            if (methodStart >= member.getSourceRange().getOffset()) { return res; }
            res = member;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        List list = Arrays.asList(fieldViewer.getCheckedElements());
        checkedFields = (IField[]) list.toArray(new IField[list.size()]);
        
        if (currentPositionIndex == 0 || currentPositionIndex == 1) {
            settings.put(SETTINGS_INSERT_POSITION, currentPositionIndex);
        }
        settings.put(SETTINGS_APPEND_SUPER, appendSuper);
        settings.put(SETTINGS_GENERATE_COMMENT, generateComment);

        return super.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.numColumns = 2;

        Label fieldSelectionLabel = new Label(composite, SWT.LEFT);
        fieldSelectionLabel
                .setText("&Select fields to use in the generated method:");
        GridData data = new GridData();
        data.horizontalSpan = 2;
        fieldSelectionLabel.setLayoutData(data);

        Composite fieldComposite = createFieldComposite(composite);
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 350;
        data.heightHint = 250;
        data.verticalSpan = 2;
        fieldComposite.setLayoutData(data);

        Button selectAllButton = new Button(composite, SWT.PUSH);
        selectAllButton.setText("Select &All");
        selectAllButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                fieldViewer.setAllChecked(true);
            }
        });
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING);
        data.widthHint = 150;
        selectAllButton.setLayoutData(data);

        Button deselectAllButton = new Button(composite, SWT.PUSH);
        deselectAllButton.setText("&Deselect All");
        deselectAllButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                fieldViewer.setAllChecked(false);
            }
        });
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING);
        deselectAllButton.setLayoutData(data);

        Composite optionComposite = createOptionComposite(composite);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        optionComposite.setLayoutData(data);
        addAppendSuperOption(optionComposite);

        Composite commentComposite = createCommentSelection(composite);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        commentComposite.setLayoutData(data);

        return composite;
    }

    private Composite createFieldComposite(final Composite composite) {
        Composite fieldComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        fieldComposite.setLayout(layout);

        fieldViewer = CheckboxTableViewer.newCheckList(fieldComposite, SWT.TOP
                | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;

        fieldViewer.getTable().setLayoutData(data);

        fieldViewer.setLabelProvider(new JavaElementLabelProvider());
        fieldViewer.setContentProvider(new ArrayContentProvider());
        fieldViewer.setInput(fields);
        return fieldComposite;
    }

    protected Composite createOptionComposite(final Composite composite) {
        Composite optionComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        optionComposite.setLayout(layout);

        addPositionChoices(optionComposite);

        return optionComposite;
    }

    private Composite addPositionChoices(final Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        label.setText("&Insertion point:");

        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        label.setLayoutData(data);

        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        combo.setItems((String[]) insertPositionLabels
                .toArray(new String[insertPositionLabels.size()]));
        combo.select(currentPositionIndex);

        data = new GridData(GridData.FILL_HORIZONTAL);
        combo.setLayoutData(data);
        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                currentPositionIndex = combo.getSelectionIndex();
            }
        });

        return composite;
    }

    private Composite addAppendSuperOption(final Composite composite) {

        Button appendButton = new Button(composite, SWT.CHECK);
        appendButton.setText("A&ppend super");
        appendButton
                .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        appendButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                appendSuper = (((Button) e.widget).getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        appendButton.setSelection(appendSuper);

        return composite;
    }

    protected Composite createCommentSelection(final Composite composite) {
        Composite commentComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        commentComposite.setLayout(layout);

        Button commentButton = new Button(commentComposite, SWT.CHECK);
        commentButton.setText("Generate method &comment");
        commentButton
                .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        commentButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                generateComment = (((Button) e.widget).getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        commentButton.setSelection(generateComment);

        return commentComposite;
    }

    public IField[] getCheckedFields() {
        return checkedFields;
    }

    /*
     * Determine where in the file to enter the newly created methods.
     */
    public IJavaElement getElementPosition() {
        return (IJavaElement) insertPositions.get(currentPositionIndex);
    }

    public boolean getAppendSuper() {
        return appendSuper;
    }

    public boolean getGenerateComment() {
        return generateComment;
    }
}
