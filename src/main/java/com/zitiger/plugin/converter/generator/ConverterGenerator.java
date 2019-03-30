package com.zitiger.plugin.converter.generator;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author zitiger
 * @version 0.13.0
 */
public interface ConverterGenerator {

    /**
     * Generate code
     * @param element method
     */
    void generate(@NotNull PsiMethod element);
}
