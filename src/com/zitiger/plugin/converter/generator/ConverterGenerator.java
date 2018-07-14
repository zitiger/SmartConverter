package com.zitiger.plugin.converter.generator;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public interface ConverterGenerator {

    void generate(@NotNull PsiMethod element);
}
