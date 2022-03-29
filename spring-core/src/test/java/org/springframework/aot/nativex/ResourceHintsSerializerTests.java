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

package org.springframework.aot.nativex;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.springframework.aot.hint.ResourceHints;
import org.springframework.aot.hint.RuntimeHintCondition;

/**
 * Tests for {@link ResourceHintsSerializer}.
 *
 * @author Sebastien Deleuze
 */
public class ResourceHintsSerializerTests {

	private final ResourceHintsSerializer serializer = new ResourceHintsSerializer();

	@Test
	void empty() throws JSONException {
		ResourceHints hints = new ResourceHints();
		assertEquals("{}", hints);
	}

	@Test
	void registerExactMatch() throws  JSONException {
		ResourceHints hints = new ResourceHints();
		hints.registerPattern("com/example/test.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class));
		hints.registerPattern("com/example/another.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class));
		assertEquals("""
				{
					"resources": {
						"includes": [
							{
								"pattern" : "\\\\Qcom/example/test.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							},
							{
								"pattern" : "\\\\Qcom/example/another.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							}
						]
					}
				}""", hints);
	}

	@Test
	void registerPattern() throws JSONException {
		ResourceHints hints = new ResourceHints();
		hints.registerPattern("com/example/*.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class));
		assertEquals("""
				{
					"resources": {
						"includes" : [
							{
								"pattern" : "\\\\Qcom/example/\\\\E.*\\\\Q.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							}
						]
					}
				}""", hints);
	}

	@Test
	void registerPatternWithIncludesAndExcludes() throws JSONException {
		ResourceHints hints = new ResourceHints();
		hints.registerPattern("com/example/*.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class),
				hint -> hint.exclude("com/example/to-ignore.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class)));
		hints.registerPattern("org/example/*.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class),
				hint -> hint.exclude("org/example/to-ignore.properties", RuntimeHintCondition.of(ResourceHintsSerializerTests.class)));
		assertEquals("""
				{
					"resources": {
						"includes": [
							{
								"pattern" : "\\\\Qcom/example/\\\\E.*\\\\Q.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							},
							{
								"pattern" : "\\\\Qorg/example/\\\\E.*\\\\Q.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							}
						],
						"excludes": [
							{
								"pattern" : "\\\\Qcom/example/to-ignore.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							},
							{
								"pattern" : "\\\\Qorg/example/to-ignore.properties\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							}
						]
					}
				}""", hints);
	}

	@Test
	void registerType() throws JSONException {
		ResourceHints hints = new ResourceHints();
		hints.registerType(String.class, ResourceHintsSerializerTests.class);
		assertEquals("""
				{
					"resources": {
						"includes" : [
							{
								"pattern" : "\\\\Qjava/lang/String.class\\\\E",
								"condition": { "typeReachable": "org.springframework.aot.nativex.ResourceHintsSerializerTests" }
							}
						]
					}
				}""", hints);
	}

	@Test
	void registerResourceBundle() throws JSONException {
		ResourceHints hints = new ResourceHints();
		hints.registerResourceBundle("com.example.message", RuntimeHintCondition.of(ResourceHintsSerializerTests.class));
		hints.registerResourceBundle("com.example.message2", RuntimeHintCondition.of(ResourceHintsSerializerTests.class));
		assertEquals("""
				{
					"bundles": [
						{ "name" : "com.example.message"},
						{ "name" : "com.example.message2"}
					]
				}""", hints);
	}

	private void assertEquals(String expectedString, ResourceHints hints) throws JSONException {
		JSONAssert.assertEquals(expectedString, serializer.serialize(hints), JSONCompareMode.LENIENT);
	}

}
