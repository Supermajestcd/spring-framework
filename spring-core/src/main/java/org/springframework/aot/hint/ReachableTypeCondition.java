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

import java.util.Objects;

/**
 * Reachable type condition that defines when a runtime hint should be applied.
 *
 * The related type should not be an annotation, see <a href="https://github.com/oracle/graal/issues/4015">related issue</a>.
 *
 * @author Sebastien Deleuze
 * @since 6.0
 */
public class ReachableTypeCondition implements RuntimeHintCondition {

	private final TypeReference reachableType;

	ReachableTypeCondition(TypeReference reachableType) {
		if (reachableType instanceof ReflectionTypeReference &&
				((ReflectionTypeReference) reachableType).getType().isAnnotation()) {
			throw new IllegalArgumentException("Reachable type condition should not be an annotation");
		}
		this.reachableType = reachableType;
	}

	@Override
	public TypeReference getReachableType() {
		return this.reachableType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ReachableTypeCondition condition = (ReachableTypeCondition) o;
		return this.reachableType.getCanonicalName().equals(condition.reachableType.getCanonicalName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.reachableType.getCanonicalName());
	}
}
