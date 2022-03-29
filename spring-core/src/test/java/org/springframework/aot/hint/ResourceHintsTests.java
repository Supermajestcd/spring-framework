/*
 * Copyright 2002-2021 the original author or authors.
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

package org.springframework.aot.hint;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.ResourceHintsTests.Nested.Inner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ResourceHints}.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 */
class ResourceHintsTests {

	private final ResourceHints resourceHints = new ResourceHints();

	@Test
	void registerType() {
		this.resourceHints.registerType(String.class, ResourceHintsTests.class);
		assertThat(this.resourceHints.resourcePatterns()).singleElement().satisfies(
				patternOf("java/lang/String.class", RuntimeHintCondition.of(ResourceHintsTests.class)));
	}

	@Test
	void registerTypeWithNestedType() {
		this.resourceHints.registerType(TypeReference.of(Nested.class), RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourcePatterns()).singleElement().satisfies(
				patternOf("org/springframework/aot/hint/ResourceHintsTests$Nested.class", RuntimeHintCondition.of(ResourceHintsTests.class)));
	}

	@Test
	void registerTypeWithInnerNestedType() {
		this.resourceHints.registerType(TypeReference.of(Inner.class), RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourcePatterns()).singleElement().satisfies(
				patternOf("org/springframework/aot/hint/ResourceHintsTests$Nested$Inner.class", RuntimeHintCondition.of(ResourceHintsTests.class)));
	}

	@Test
	void registerTypeSeveralTimesAddsOnlyOneEntry() {
		this.resourceHints.registerType(String.class, ResourceHintsTests.class);
		this.resourceHints.registerType(TypeReference.of(String.class), RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourcePatterns()).singleElement().satisfies(
				patternOf("java/lang/String.class", RuntimeHintCondition.of(ResourceHintsTests.class)));
	}

	@Test
	void registerExactMatch() {
		this.resourceHints.registerPattern("com/example/test.properties", RuntimeHintCondition.of(ResourceHintsTests.class));
		this.resourceHints.registerPattern("com/example/another.properties", RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourcePatterns())
				.anySatisfy(patternOf("com/example/test.properties", RuntimeHintCondition.of(ResourceHintsTests.class)))
				.anySatisfy(patternOf("com/example/another.properties", RuntimeHintCondition.of(ResourceHintsTests.class)))
				.hasSize(2);
	}

	@Test
	void registerPattern() {
		this.resourceHints.registerPattern("com/example/*.properties", RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourcePatterns()).singleElement().satisfies(
				patternOf("com/example/*.properties", RuntimeHintCondition.of(ResourceHintsTests.class)));
	}

	@Test
	void registerPatternWithIncludesAndExcludes() {
		this.resourceHints.registerPattern("com/example/*.properties", RuntimeHintCondition.of(ResourceHintsTests.class),
				resourceHint -> resourceHint.exclude("com/example/to-ignore.properties",
						RuntimeHintCondition.of(ResourceHintsTests.class)));
		assertThat(this.resourceHints.resourcePatterns()).singleElement().satisfies(patternOf(
				Map.of("com/example/*.properties", RuntimeHintCondition.of(ResourceHintsTests.class)),
				Map.of("com/example/to-ignore.properties", RuntimeHintCondition.of(ResourceHintsTests.class))));
	}

	@Test
	void registerResourceBundle() {
		this.resourceHints.registerResourceBundle("com.example.message", RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourceBundles()).singleElement()
				.satisfies(resourceBundle("com.example.message"));
	}

	@Test
	void registerResourceBundleSeveralTimesAddsOneEntry() {
		this.resourceHints.registerResourceBundle("com.example.message", RuntimeHintCondition.of(ResourceHintsTests.class))
				.registerResourceBundle("com.example.message", RuntimeHintCondition.of(ResourceHintsTests.class));
		assertThat(this.resourceHints.resourceBundles()).singleElement()
				.satisfies(resourceBundle("com.example.message"));
	}


	private Consumer<ResourcePatternHint> patternOf(String include, RuntimeHintCondition condition) {
		return patternOf(Collections.singletonMap(include, condition), Collections.emptyMap());
	}

	private Consumer<ResourceBundleHint> resourceBundle(String baseName) {
		return resourceBundleHint -> assertThat(resourceBundleHint.getBaseName()).isEqualTo(baseName);
	}

	private Consumer<ResourcePatternHint> patternOf(Map<String, RuntimeHintCondition> includes, Map<String, RuntimeHintCondition> excludes) {
		return pattern -> {
			assertThat(pattern.getIncludes()).containsExactlyEntriesOf(includes);
			assertThat(pattern.getExcludes()).containsExactlyEntriesOf(excludes);
		};
	}

	static class Nested {

		static class Inner {

		}
	}

}
