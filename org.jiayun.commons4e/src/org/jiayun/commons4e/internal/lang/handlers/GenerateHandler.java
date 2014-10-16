/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.jiayun.commons4e.internal.lang.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jiayun.commons4e.internal.lang.generators.LangGenerators;

/**
 * Handler that determine which generation should be performed depending on the event commandId. It also ensures that
 * the currently selected object in the editor is a class in order to be able to perform the generation. The
 * {@link GenerateHandler#generate(String, ISelection, ICompilationUnit, Shell)} method is extracted from the
 * {@link IEditorActionDelegate} written by jiayun previously in the plugin.
 * 
 * @author jiayun, maudrain
 */
public class GenerateHandler extends AbstractHandler {

    /**
     * @see IHandler#execute(ExecutionEvent)
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell parentShell = HandlerUtil.getActiveShell(event);
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
        ICompilationUnit compilationUnit = manager.getWorkingCopy(editor
                .getEditorInput());
        generate(event.getCommand().getId(), currentSelection, compilationUnit,
                parentShell);
        return null;
    }

    private void generate(String commandId, ISelection iSelection,
            ICompilationUnit compilationUnit, Shell parentShell) {
        IType objectClass = null;
        try {
            ITextSelection selection = (ITextSelection) iSelection;
            IJavaElement element = compilationUnit.getElementAt(selection
                    .getOffset());
            if (element != null) {
                objectClass = (IType) element.getAncestor(IJavaElement.TYPE);
            }
        } catch (JavaModelException e) {
            MessageDialog.openError(parentShell, "Error", e.getMessage());
        }

        if (objectClass == null) {
            objectClass = compilationUnit.findPrimaryType();
        }

        try {
            if (objectClass == null || !objectClass.isClass()) {
                MessageDialog
                        .openInformation(parentShell, "Method Generation",
                                "Cursor not in a class, or no class has the same name with the Java file.");
            } else {
                LangGenerators.getGenerator(commandId).generate(parentShell,
                        objectClass);
            }
        } catch (JavaModelException e) {
            MessageDialog.openError(parentShell, "Error", e.getMessage());
        }
    }

}
