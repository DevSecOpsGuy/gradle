/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.artifacts.transform;

import com.google.common.collect.ImmutableList;
import org.gradle.api.artifacts.transform.TransformInvocationException;

import java.io.File;

public class DefaultTransformerInvoker implements TransformerInvoker {
    private final TransformedFileCache transformedFileCache;
    private final ArtifactTransformListener artifactTransformListener;

    public DefaultTransformerInvoker(TransformedFileCache transformedFileCache, ArtifactTransformListener artifactTransformListener) {
        this.transformedFileCache = transformedFileCache;
        this.artifactTransformListener = artifactTransformListener;
    }

    @Override
    public void invoke(TransformerInvocation invocation) {
        File primaryInput = invocation.getPrimaryInput();
        Transformer transformer = invocation.getTransformer();
        boolean hasCachedResult = hasCachedResult(primaryInput, transformer);
        TransformationSubject subjectBeingTransformed = invocation.getSubjectBeingTransformed();
        if (!hasCachedResult) {
            artifactTransformListener.beforeTransformation(transformer, subjectBeingTransformed);
        }
        try {
            ImmutableList<File> result = ImmutableList.copyOf(transformedFileCache.getResult(primaryInput, transformer));
            invocation.success(result);
        } catch (Throwable t) {
            invocation.failure(new TransformInvocationException(primaryInput.getAbsoluteFile(), transformer.getImplementationClass(), t));
        } finally {
            if (!hasCachedResult) {
                artifactTransformListener.afterTransformation(transformer, subjectBeingTransformed);
            }
        }
    }

    @Override
    public boolean hasCachedResult(File input, Transformer transformer) {
        return transformedFileCache.contains(input.getAbsoluteFile(), transformer.getInputsHash());
    }
}
