/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aot.graalvm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import com.oracle.svm.core.annotate.AutomaticFeature;

import org.graalvm.nativeimage.hosted.Feature;

/**
 * GraalVM {@link Feature} that substitutes field values that match a certain pattern
 * with constants without causing build-time initialization.
 *
 * @author Sebastien Deleuze
 * @since 6.0
 */
@AutomaticFeature
class ConstantFieldFeature implements Feature {

	// TODO Should we keep those patterns or use an annotation?
	private static Pattern[] patterns = {
			Pattern.compile(Pattern.quote("org.springframework.core.NativeDetector#imageCode")),
			Pattern.compile(Pattern.quote("org.springframework.") + ".*#.*Present"),
			Pattern.compile(Pattern.quote("org.springframework.") + ".*#.*PRESENT"),
			Pattern.compile(Pattern.quote("reactor.") + ".*#.*Available")
	};

	private final ThrowawayClassLoader throwawayClassLoader = new ThrowawayClassLoader(ConstantFieldFeature.class.getClassLoader());

	@Override
	public void beforeAnalysis(BeforeAnalysisAccess access) {
		access.registerSubtypeReachabilityHandler(this::iterateFields, Object.class);
	}

	/* This method is invoked for every type that is reachable. */
	private void iterateFields(DuringAnalysisAccess access, Class<?> subtype) {
		try {
			for (Field field : subtype.getDeclaredFields()) {
				String fieldIdentifier = field.getDeclaringClass().getName() + "#" + field.getName();
				for (Pattern pattern : patterns) {
					int modifiers = field.getModifiers();
					if (pattern.matcher(fieldIdentifier).matches() &&
							Modifier.isStatic(modifiers) &&
							Modifier.isFinal(modifiers) &&
							!field.isEnumConstant()
					) {
						System.out.println("Field " + fieldIdentifier + " set to false at build time");
						access.registerFieldValueTransformer(field, (receiver, originalValue) -> provideFieldValue(field));
					}
				}
			}
		} catch (NoClassDefFoundError ex) {
			// Skip classes that have not all their field types in the classpath
		}
	}

	/* This method is invoked when the field value is written to the image heap or the field is constant folded. */
	private Object provideFieldValue(Field field) {
		try {
			Class<?> throwawayClass = this.throwawayClassLoader.loadClass(field.getDeclaringClass().getName());
			Field throwawayField = throwawayClass.getDeclaredField(field.getName());
			throwawayField.setAccessible(true);
			return throwawayField.get(null);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to read value from " + field.getDeclaringClass().getName() + "." + field.getName(), ex);
		}
	}

}
