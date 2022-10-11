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

package org.springframework.orm.jpa.vendor;

import java.util.Map;

import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * {@code StandardServiceInitiator<BytecodeProvider>} implementation instantiating a
 * {@link org.hibernate.bytecode.internal.none.BytecodeProviderImpl} instance for native
 * image use case.
 *
 * @author Sebastien Deleuze
 * @since 6.0
 */
public final class NoneBytecodeProviderInitiator implements StandardServiceInitiator<BytecodeProvider> {

	/**
	 * Singleton access.
	 */
	public static final StandardServiceInitiator<BytecodeProvider> INSTANCE = new NoneBytecodeProviderInitiator();

	@Override
	@SuppressWarnings("rawtypes")
	public BytecodeProvider initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		return new org.hibernate.bytecode.internal.none.BytecodeProviderImpl();
	}

	@Override
	public Class<BytecodeProvider> getServiceInitiated() {
		return BytecodeProvider.class;
	}

}
