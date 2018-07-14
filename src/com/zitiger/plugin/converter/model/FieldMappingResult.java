package com.zitiger.plugin.converter.model;

import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FieldMappingResult {

    /**
     * Key is a Setter method for To class field
     * Value is a Getter method for From class field
     */
    private final Map<PsiMethod, PsiMethod> mappedFieldMap = new HashMap<>();

    private final List<String> notMappedToFieldList = new ArrayList<>();
    private final List<String> notMappedFromFieldList = new ArrayList<>();

    public Map<PsiMethod, PsiMethod> getMappedFieldMap() {
        return mappedFieldMap;
    }

    public List<String> getNotMappedToFieldList() {
        return notMappedToFieldList;
    }

    public List<String> getNotMappedFromFieldList() {
        return notMappedFromFieldList;
    }

    public void addMappedField(PsiMethod toSetter, PsiMethod fromGetter) {
        mappedFieldMap.put(toSetter, fromGetter);
    }

    public void addNotMappedToField(String toField) {
        notMappedToFieldList.add(toField);
    }

    public void addNotMappedFromField(String fromField) {
        notMappedFromFieldList.add(fromField);
    }


}
