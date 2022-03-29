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

package org.springframework.aot.hint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A hint that describes resources that should be made available at runtime.
 *
 * <p>The patterns may be a simple path which has a one-to-one mapping to a
 * resource on the classpath, or alternatively may contain the special
 * {@code *} character to indicate a wildcard search.
 *
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @since 6.0
 */
public final class ResourcePatternHint {

	private final Map<String, RuntimeHintCondition> includes;

	private final Map<String, RuntimeHintCondition> excludes;


	private ResourcePatternHint(Builder builder) {
		this.includes = builder.includes;
		this.excludes = builder.excludes;
	}

	/**
	 * Return the include patterns to use to identify the resources to match.
	 * @return the include patterns
	 */
	public Map<String, RuntimeHintCondition> getIncludes() {
		return this.includes;
	}

	/**
	 * Return the exclude patterns to use to identify the resources to match.
	 * @return the exclude patterns
	 */
	public Map<String, RuntimeHintCondition> getExcludes() {
		return this.excludes;
	}


	/**
	 * Builder for {@link ResourcePatternHint}.
	 */
	public static class Builder {

		private final Map<String, RuntimeHintCondition> includes = new LinkedHashMap<>();

		private final Map<String, RuntimeHintCondition> excludes = new LinkedHashMap<>();


		/**
		 * Includes the resources matching the specified pattern.
		 * @param include the include pattern
		 * @param condition the condition that defines when the hint should apply
		 * @return {@code this}, to facilitate method chaining
		 */
		public Builder include(String include, RuntimeHintCondition condition) {
			this.includes.put(include, condition);
			return this;
		}

		/**
		 * Exclude resources matching the specified pattern.
		 * @param exclude the excludes pattern
		 * @param condition the condition that defines when the hint should apply
		 * @return {@code this}, to facilitate method chaining
		 */
		public Builder exclude(String exclude, RuntimeHintCondition condition) {
			this.excludes.put(exclude, condition);
			return this;
		}

		/**
		 * Creates a {@link ResourcePatternHint} based on the state of this
		 * builder.
		 * @return a resource pattern hint
		 */
		public ResourcePatternHint build() {
			return new ResourcePatternHint(this);
		}

	}
}
