package com.zitiger.plugin.converter.action;

import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.zitiger.plugin.converter.util.ContextUtils;
import com.zitiger.plugin.converter.util.PsiClassUtils;
import com.zitiger.plugin.converter.exception.ConverterException;
import com.zitiger.plugin.converter.generator.ConverterGenerator;
import com.zitiger.plugin.converter.generator.impl.ClassGenerator;
import com.zitiger.plugin.converter.generator.impl.ListGenerator;
import com.zitiger.plugin.converter.generator.impl.MethodGenerator;

public class ConvertGeneratorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        PsiMethod method = ContextUtils.getPsiMethod(e);
        PsiClass psiClass = ContextUtils.getPsiClass(e);

        try {
            if (null != method) {
                getGenerator(method).generate(method);
            } else if (psiClass != null) {

                ClassGenerator classGenerator = new ClassGenerator();
                PsiClass[] psiClasses = getFromDialog(psiClass);

                if (psiClasses.length > 0) {
                    classGenerator.generateCode(psiClass, psiClasses[0], psiClasses[1]);
                }
            }
        } catch (ConverterException ex) {
            Messages.showErrorDialog(ex.getMessage(), "Converter Plugin");
        }
    }

    private PsiClass[] getFromDialog(PsiClass psiClass) {

        PsiClass[] psiClasses;

        PsiClass fromClass = getClassDialog(psiClass, "Choose From class");
        PsiClass toClass = getClassDialog(psiClass, "Choose To class");

        if (fromClass == null || toClass == null) {
            return new PsiClass[] {};
        } else {
            psiClasses = new PsiClass[2];
            psiClasses[0] = fromClass;
            psiClasses[1] = toClass;
        }

        //        ConverterDialog generateConverterDialog = new ConverterDialog(psiClass);
        //        generateConverterDialog.show();
        //        if (generateConverterDialog.isOK()) {
        //            PsiClass classFrom = generateConverterDialog.getConvertFromClass();
        //            PsiClass classTo = generateConverterDialog.getConvertToClass();
        //
        //            psiClasses[0] = classFrom;
        //            psiClasses[1] = classTo;
        //
        //            return psiClasses;
        //        }
        return psiClasses;
    }

    private PsiClass getClassDialog(PsiClass psiClass, String title) {
        TreeClassChooserFactory factory = TreeClassChooserFactory.getInstance(psiClass.getProject());

        Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
        GlobalSearchScope scope;
        if (module != null) {
            scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        } else {
            scope = GlobalSearchScope.allScope(psiClass.getProject());
        }

        TreeClassChooser chooser = factory.createInheritanceClassChooser(title, scope, null, null);
        chooser.showDialog();

        return chooser.getSelected();
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = ContextUtils.getPsiClass(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private ConverterGenerator getGenerator(PsiMethod method) throws ConverterException {
        PsiTypeElement returnType = method.getReturnTypeElement();
        if (null == returnType) {
            throw new ConverterException("This method does not have return type");
        }

        String type = returnType.getText();
        if (type.contains("List")) {
            return new ListGenerator();
        } else {
            return new MethodGenerator();
        }
    }

}
