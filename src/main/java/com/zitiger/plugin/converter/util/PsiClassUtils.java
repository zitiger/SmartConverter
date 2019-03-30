package com.zitiger.plugin.converter.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

import java.util.HashSet;
import java.util.Set;

/**
 * @author linglh
 * @version 2.12.0 on 2018/8/12
 */
public class PsiClassUtils {

    public static PsiClass findClassByName(String className, PsiClass containingClass, Project project) {
        PsiClass[] classes = PsiShortNamesCache.getInstance(project).getClassesByName(className,
                GlobalSearchScope.projectScope(project));
        if (classes.length == 0) {
            return null;
        }
        if (classes.length == 1) {
            return classes[0];
        }
        PsiJavaFile javaFile = (PsiJavaFile) containingClass.getContainingFile();
        PsiImportList importList = javaFile.getImportList();
        PsiImportStatement[] statements = importList.getImportStatements();
        Set<String> importedPackageSet = new HashSet<>();
        for (PsiImportStatement psiImportStatement : statements) {
            importedPackageSet.add(psiImportStatement.getQualifiedName());
        }
        for (PsiClass targetClass : classes) {
            PsiJavaFile targetClassContainingFile = (PsiJavaFile) targetClass.getContainingFile();
            String packageName = targetClassContainingFile.getPackageName();
            if (importedPackageSet.contains(packageName + "." + targetClass.getName())) {
                return targetClass;
            }
        }
        return null;
    }
}
