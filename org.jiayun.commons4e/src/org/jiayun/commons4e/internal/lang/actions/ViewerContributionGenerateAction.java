//$Id$
package org.jiayun.commons4e.internal.lang.actions;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jiayun.commons4e.internal.lang.generators.LangGenerators;

/**
 * @author jiayun
 */
public final class ViewerContributionGenerateAction implements
        IEditorActionDelegate {

    private Shell parentShell;

    private ITextEditor editor;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        editor = (ITextEditor) targetEditor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        parentShell = editor.getSite().getShell();
        IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
        ICompilationUnit cu = manager.getWorkingCopy(editor.getEditorInput());

        IType objectClass = null;
        try {
            ITextSelection selection = (ITextSelection) editor
                    .getSelectionProvider().getSelection();
            IJavaElement element = cu.getElementAt(selection.getOffset());
            if (element != null) {
                objectClass = (IType) element.getAncestor(IJavaElement.TYPE);
            }
        } catch (JavaModelException e) {
            MessageDialog.openError(parentShell, "Error", e.getMessage());
        }

        if (objectClass == null) {
            objectClass = cu.findPrimaryType();
        }

        try {
            if (objectClass == null || !objectClass.isClass()) {
                MessageDialog
                        .openInformation(parentShell, "Method Generation",
                                "Cursor not in a class, or no class has the same name with the Java file.");
            } else {
                LangGenerators.getGenerator(action.getId()).generate(
                        parentShell, objectClass);
            }
        } catch (JavaModelException e) {
            MessageDialog.openError(parentShell, "Error", e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

}
