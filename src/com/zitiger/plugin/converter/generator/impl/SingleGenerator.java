package com.zitiger.plugin.converter.generator.impl;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.zitiger.plugin.converter.exception.ConverterException;
import com.zitiger.plugin.converter.model.FieldMappingResult;
import com.zitiger.plugin.converter.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SingleGenerator extends AbstractGenerator {

    @Override
    void generateCode(PsiClass psiClass, PsiMethod psiMethod) throws ConverterException {

        if (psiMethod.isConstructor()) {
            throw new ConverterException("Method is the constructor");
        }

        if (null == psiMethod.getBody()) {
            throw new ConverterException("Method body is null");
        }

        PsiClass fromClass = getParamPsiClass(psiMethod);
        PsiClass toClass = getReturnPsiClass(psiMethod);

        // camel
        String toName = StringUtils.toCamelCase(toClass.getName());

        List<String> statementList = new ArrayList<>();
        statementList.add(buildMethodSignature(toClass, toName));

        FieldMappingResult mappingResult = new FieldMappingResult();
        processToFields(mappingResult, fromClass, toClass);
        processFromFields(mappingResult, fromClass);

        String fromName = psiMethod.getParameterList().getParameters()[0].getName();

        statementList.addAll(writeMappedFields(fromName, toName, mappingResult));
        statementList.addAll(writeNotMappedFields(mappingResult.getNotMappedToFieldList(), "TO"));
        statementList.addAll(writeNotMappedFields(mappingResult.getNotMappedFromFieldList(), "FROM"));
        statementList.add("return " + toName + ";");

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiCodeBlock codeBlock = elementFactory.createCodeBlockFromText("{" + String.join("\n", statementList) + "}", psiClass);

        for (int i = 1; i < codeBlock.getChildren().length - 1; i++) {
            PsiElement psiElement = codeBlock.getChildren()[i];
            psiMethod.getBody().add(psiElement);
        }

        JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(psiMethod);
    }

    private void processToFields(FieldMappingResult mappingResult, PsiClass from, PsiClass to) {
        for (PsiField toField : to.getFields()) {
            String toFieldName = toField.getName();
            if (toFieldName != null && !toField.getModifierList().hasExplicitModifier("static")) {
                PsiMethod toSetter = findSetter(to, toFieldName);
                PsiMethod fromGetter = findGetter(from, toFieldName);
                if (toSetter != null && fromGetter != null && isMatchingFieldType(toField, fromGetter)) {
                    mappingResult.addMappedField(toSetter, fromGetter);
                } else {
                    mappingResult.addNotMappedToField(toFieldName);
                }
            }
        }
    }

    private void processFromFields(FieldMappingResult mappingResult, PsiClass from) {
        for (PsiField fromField : from.getFields()) {
            String fromFieldName = fromField.getName();
            if (fromFieldName != null && !fromField.getModifierList().hasExplicitModifier("static")) {
                PsiMethod fromGetter = findGetter(from, fromFieldName);
                if (fromGetter == null || !mappingResult.getMappedFieldMap().containsValue(fromGetter)) {
                    mappingResult.addNotMappedFromField(fromFieldName);
                }
            }
        }
    }

    private String buildMethodSignature(PsiClass to, String toName) {
        StringBuilder builder = new StringBuilder("");

        builder.append(to.getQualifiedName()).append(" " + toName + " = new ").append(to.getQualifiedName()).append("();");
        return builder.toString();
    }

    private List<String> writeMappedFields(String fromName, String toName, FieldMappingResult mappingResult) {
        List<String> builder = new ArrayList<>();
        for (PsiMethod toSetter : mappingResult.getMappedFieldMap().keySet()) {
            builder.add(toName + "." + toSetter.getName() + "(" + fromName + "." + (mappingResult.getMappedFieldMap().get(toSetter).getName()) + "());");
        }
        return builder;
    }

    private List<String> writeNotMappedFields(List<String> notMappedFields, String sourceType) {
        List<String> builder = new ArrayList<>();
        if (!notMappedFields.isEmpty()) {
            builder.add("// Not mapped " + sourceType + " fields: ");
        }
        for (String notMappedField : notMappedFields) {
            builder.add("// " + notMappedField + "");
        }
        return builder;
    }

    private PsiMethod findSetter(PsiClass psiClass, String fieldName) {
        PsiMethod[] setters = psiClass.findMethodsByName("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), false);
        if (setters.length == 1) {
            return setters[0];
        }
        return null;
    }

    private PsiMethod findGetter(PsiClass psiClass, String fieldName) {
        String methodSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        PsiMethod[] getters = psiClass.findMethodsByName("get" + methodSuffix, false);
        if (getters.length > 0) {
            return getters[0];
        }
        getters = psiClass.findMethodsByName("is" + methodSuffix, false);
        if (getters.length > 0) {
            return getters[0];
        }
        return null;
    }

    private boolean isMatchingFieldType(PsiField toField, PsiMethod fromGetter) {
        PsiType fromGetterReturnType = fromGetter.getReturnType();
        PsiType toFieldType = toField.getType();
        return fromGetterReturnType != null && toFieldType.isAssignableFrom(fromGetterReturnType);
    }
}
