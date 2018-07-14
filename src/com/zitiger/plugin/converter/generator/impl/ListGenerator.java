package com.zitiger.plugin.converter.generator.impl;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTypesUtil;
import com.zitiger.plugin.converter.exception.ConverterException;
import com.zitiger.plugin.converter.util.StringUtils;
import org.jetbrains.annotations.NotNull;

public class ListGenerator extends SingleGenerator {

    @Override
    void generateCode(PsiClass psiClass, PsiMethod psiMethod) throws ConverterException {

        if(null == psiMethod.getBody()){
            throw new ConverterException("Method body is null");
        }

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        String returnGenericClassName = getGenericReturnPsiClass(psiMethod).getQualifiedName();
        String paramGenericClassName = getGenericParamPsiClass(psiMethod).getQualifiedName();
        String camelParamGenericClassName = StringUtils.toCamelCase(paramGenericClassName);
        String paramVariableName = psiMethod.getParameterList().getParameters()[0].getName();

        // match single converter according to param class and return class
        PsiMethod singleMethod = findSingleConvertMethod(psiMethod);
        if (null == singleMethod) {

            // create single convert method
            singleMethod = createSingleConvertMethod(psiClass, paramGenericClassName, returnGenericClassName);
        }

        String singleConvertMethodName = singleMethod.getName();
        String returnListName = StringUtils.toCamelCase(returnGenericClassName) + "List";

        psiMethod.getBody().add(createPsiStatement(psiClass, "List<" + returnGenericClassName + "> " + returnListName + "= new java.util.ArrayList<>();"));
        psiMethod.getBody()
                .add(createPsiStatement(psiClass, "for (" + paramGenericClassName + " " + camelParamGenericClassName + " : " + paramVariableName + ")"));

        PsiBlockStatement blockStatement = (PsiBlockStatement) elementFactory.createStatementFromText("{}", psiClass);
        blockStatement.getCodeBlock().add(createPsiStatement(psiClass, returnListName + ".add(" + singleConvertMethodName + "(" + camelParamGenericClassName + "));"));
        psiMethod.getBody().add(blockStatement);

        psiMethod.getBody().add(createPsiStatement(psiClass, "return " + returnListName + ";"));

        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(psiMethod);
    }

    @NotNull
    private PsiStatement createPsiStatement(PsiClass psiClass, String codeLine) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        return elementFactory.createStatementFromText(codeLine, psiClass);
    }

    private PsiMethod createSingleConvertMethod(PsiClass psiClass, String paramGenericClassName, String returnGenericClassName) throws ConverterException {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());

        StringBuilder sbSingleConvertMethod = new StringBuilder("private static ");
        sbSingleConvertMethod.append(returnGenericClassName);
        sbSingleConvertMethod.append(" to" + returnGenericClassName + "(");
        sbSingleConvertMethod.append(paramGenericClassName);
        sbSingleConvertMethod.append(" ");
        sbSingleConvertMethod.append(StringUtils.toCamelCase(paramGenericClassName));
        sbSingleConvertMethod.append(") {\n");
        sbSingleConvertMethod.append("}\n");

        PsiMethod singleConvertMethod = elementFactory.createMethodFromText(sbSingleConvertMethod.toString(), psiClass);
        PsiElement method = psiClass.add(singleConvertMethod);
        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(method);

        super.generateCode(psiClass, (PsiMethod) method);

        return (PsiMethod) method;

    }

    private PsiMethod findSingleConvertMethod(PsiMethod psiMethod) throws ConverterException {
        PsiClass paramPsiClass = getGenericParamPsiClass(psiMethod);
        PsiClass returnPsiClass = getGenericReturnPsiClass(psiMethod);

        PsiClass psiClass = (PsiClass) psiMethod.getParent();
        PsiMethod[] psiMethodList = psiClass.getMethods();

        for (PsiMethod classMethod : psiMethodList) {

            if(classMethod.getParameterList().getParametersCount() != 1 || classMethod.getReturnType() == null){
                continue;
            }

            PsiClass methodParamPsiClass = getParamPsiClass(classMethod);
            PsiClass methodReturnPsiClass = getReturnPsiClass(classMethod);

            if (paramPsiClass.equals(methodParamPsiClass) && returnPsiClass.equals(methodReturnPsiClass)) {
                return classMethod;
            }
        }

        return null;
    }

    // get the generic type from method param
    private PsiClass getGenericParamPsiClass(PsiMethod method) throws ConverterException {

        PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length == 0) {
            throw new ConverterException("The method does not have any parameter");
        }

        PsiType paramPsiType = ((PsiClassType) parameters[0].getType()).getParameters()[0];
        return PsiTypesUtil.getPsiClass(paramPsiType);
    }

    // get the generic type from method return
    private PsiClass getGenericReturnPsiClass(PsiMethod method) throws ConverterException {

        final PsiType returnType = method.getReturnType();
        if (null == returnType) {
            throw new ConverterException("Can not find the return of method");
        }


        if (PsiType.VOID.equals(returnType)) {
            throw new ConverterException("The return of method is void");
        }


        PsiType returnPsiType = ((PsiClassType) returnType).getParameters()[0];
        return PsiTypesUtil.getPsiClass(returnPsiType);
    }

}
