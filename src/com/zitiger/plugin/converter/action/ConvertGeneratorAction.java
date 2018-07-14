package com.zitiger.plugin.converter.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.zitiger.plugin.converter.exception.ConverterException;
import com.zitiger.plugin.converter.generator.ConverterGenerator;
import com.zitiger.plugin.converter.generator.impl.ListGenerator;
import com.zitiger.plugin.converter.generator.impl.SingleGenerator;

public class ConvertGeneratorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiMethod method = getPsiMethodFromContext(e);

        try {
            if (null != method) {
                getGenerator(method).generate(method);
            }
        } catch (ConverterException ex) {
            Messages.showErrorDialog(ex.getMessage(), "Converter Plugin");

        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiMethod psiMethod = getPsiMethodFromContext(e);
        e.getPresentation().setEnabled(psiMethod != null);
    }

    private PsiMethod getPsiMethodFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, PsiMethod.class);
    }

    private ConverterGenerator getGenerator(PsiMethod method) throws ConverterException {
        PsiTypeElement returnType = method.getReturnTypeElement();
        if(null == returnType){
            throw new ConverterException("This method does not have return type");
        }

        String type = returnType.getText();
        if (type.contains("List")) {
            return new ListGenerator();
        } else {
            return new SingleGenerator();
        }
    }

}
