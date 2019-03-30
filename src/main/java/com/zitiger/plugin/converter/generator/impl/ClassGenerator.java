package com.zitiger.plugin.converter.generator.impl;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.zitiger.plugin.converter.exception.ConverterException;
import com.zitiger.plugin.converter.util.StringUtils;

public class ClassGenerator extends ListGenerator {

    public void generateCode(PsiClass psiClass, PsiClass fromPsiClass, PsiClass toPsiClass) throws ConverterException {

        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {

            @Override
            protected void run() {
                try {
                    createPrivateConstruct(psiClass);

                    createConvertMethod(psiClass, fromPsiClass, toPsiClass);
                    createConvertMethod(psiClass, toPsiClass, fromPsiClass);
                } catch (ConverterException e) {
                    Messages.showErrorDialog(e.getMessage(), "Converter Plugin");
                }
            }
        }.execute();
    }

    private void createConvertMethod(PsiClass psiClass, PsiClass fromPsiClass, PsiClass toPsiClass) throws ConverterException {

        String methodName = "to" + toPsiClass.getName() + "List";

        PsiMethod[] psiMethodList = psiClass.getMethods();
        for (PsiMethod psiMethod : psiMethodList) {
            if (methodName.equals(psiMethod.getName())) {
                return;
            }
        }

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());

        StringBuilder sbSingleConvertMethod = new StringBuilder("public static java.util.List<");
        sbSingleConvertMethod.append(toPsiClass.getQualifiedName());
        sbSingleConvertMethod.append("> " + methodName + "(List<");
        sbSingleConvertMethod.append(fromPsiClass.getQualifiedName());
        sbSingleConvertMethod.append("> ");
        sbSingleConvertMethod.append(StringUtils.toCamelCase(fromPsiClass.getName()));
        sbSingleConvertMethod.append("List) {\n");
        sbSingleConvertMethod.append("}\n");

        PsiMethod singleConvertMethod = elementFactory.createMethodFromText(sbSingleConvertMethod.toString(), psiClass);
        PsiElement method = psiClass.add(singleConvertMethod);
        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);

        super.generateCode(psiClass, (PsiMethod) method);

    }

    private void createPrivateConstruct(PsiClass psiClass) {
        if (psiClass.getConstructors().length > 0) {
            return;
        }

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());

        String className = psiClass.getName();
        String sbSingleConvertMethod = "private  " + className + "(){\n        // 无需实现\n}";

        PsiMethod singleConvertMethod = elementFactory.createMethodFromText(sbSingleConvertMethod, psiClass);
        PsiElement method = psiClass.add(singleConvertMethod);
        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);
    }
}
