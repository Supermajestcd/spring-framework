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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.boot.cfgxml.internal.CfgXmlAccessServiceInitiator;
import org.hibernate.boot.cfgxml.internal.ConfigLoader;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.internal.DefaultSessionFactoryBuilderInitiator;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.bytecode.internal.BytecodeProviderInitiator;
import org.hibernate.bytecode.internal.ProxyFactoryFactoryInitiator;
import org.hibernate.cache.internal.RegionFactoryInitiator;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.config.internal.ConfigurationServiceInitiator;
import org.hibernate.engine.jdbc.batch.internal.BatchBuilderInitiator;
import org.hibernate.engine.jdbc.connections.internal.ConnectionProviderInitiator;
import org.hibernate.engine.jdbc.connections.internal.MultiTenantConnectionProviderInitiator;
import org.hibernate.engine.jdbc.cursor.internal.RefCursorSupportInitiator;
import org.hibernate.engine.jdbc.dialect.internal.DialectFactoryInitiator;
import org.hibernate.engine.jdbc.dialect.internal.DialectResolverInitiator;
import org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentInitiator;
import org.hibernate.engine.jdbc.internal.JdbcServicesInitiator;
import org.hibernate.engine.jndi.internal.JndiServiceInitiator;
import org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformInitiator;
import org.hibernate.engine.transaction.jta.platform.internal.JtaPlatformResolverInitiator;
import org.hibernate.event.internal.EntityCopyObserverFactoryInitiator;
import org.hibernate.hql.internal.QueryTranslatorFactoryInitiator;
import org.hibernate.id.factory.internal.MutableIdentifierGeneratorFactoryInitiator;
import org.hibernate.jmx.internal.JmxServiceInitiator;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.PersistenceUnitInfoDescriptor;
import org.hibernate.persister.internal.PersisterClassResolverInitiator;
import org.hibernate.persister.internal.PersisterFactoryInitiator;
import org.hibernate.property.access.internal.PropertyAccessStrategyResolverInitiator;
import org.hibernate.resource.beans.spi.ManagedBeanRegistryInitiator;
import org.hibernate.resource.transaction.internal.TransactionCoordinatorBuilderInitiator;
import org.hibernate.service.internal.SessionFactoryServiceRegistryFactoryInitiator;
import org.hibernate.tool.hbm2ddl.ImportSqlCommandExtractorInitiator;
import org.hibernate.tool.schema.internal.SchemaManagementToolInitiator;

import org.springframework.core.NativeDetector;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;

/**
 * Spring-specific subclass of the standard {@link HibernatePersistenceProvider}
 * from the {@code org.hibernate.jpa} package, adding support for
 * {@link SmartPersistenceUnitInfo#getManagedPackages()}.
 *
 * @author Juergen Hoeller
 * @author Joris Kuipers
 * @author Sebastien Deleuze
 * @since 4.1
 * @see Configuration#addPackage
 */
class SpringHibernateJpaPersistenceProvider extends HibernatePersistenceProvider {

	@Override
	@SuppressWarnings("rawtypes")
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
		final List<String> mergedClassesAndPackages = new ArrayList<>(info.getManagedClassNames());
		if (info instanceof SmartPersistenceUnitInfo) {
			mergedClassesAndPackages.addAll(((SmartPersistenceUnitInfo) info).getManagedPackages());
		}
		return new SpringEntityManagerFactoryBuilderImpl(
				new PersistenceUnitInfoDescriptor(info) {
					@Override
					public List<String> getManagedClassNames() {
						return mergedClassesAndPackages;
					}
				}, properties).build();
	}

	@SuppressWarnings("rawtypes")
	private static List<StandardServiceInitiator> buildInitiatorList() {

		final ArrayList<StandardServiceInitiator> serviceInitiators = new ArrayList<>();

		serviceInitiators.add( DefaultSessionFactoryBuilderInitiator.INSTANCE );

		if (NativeDetector.inNativeImage()) {
			serviceInitiators.add( NoneBytecodeProviderInitiator.INSTANCE );
		}
		else {
			serviceInitiators.add( BytecodeProviderInitiator.INSTANCE );
		}

		serviceInitiators.add( ProxyFactoryFactoryInitiator.INSTANCE );

		serviceInitiators.add( CfgXmlAccessServiceInitiator.INSTANCE );
		serviceInitiators.add( ConfigurationServiceInitiator.INSTANCE );
		serviceInitiators.add( PropertyAccessStrategyResolverInitiator.INSTANCE );

		// TODO Potentially require exclusion because it is not available in Hibernate 6
		// serviceInitiators.add( ImportSqlCommandExtractorInitiator.INSTANCE );
		serviceInitiators.add( SchemaManagementToolInitiator.INSTANCE );

		serviceInitiators.add( JdbcEnvironmentInitiator.INSTANCE );
		serviceInitiators.add( JndiServiceInitiator.INSTANCE );
		// TODO Potentially require exclusion because it is not available in Hibernate 6
		// serviceInitiators.add( JmxServiceInitiator.INSTANCE );

		serviceInitiators.add( PersisterClassResolverInitiator.INSTANCE );
		serviceInitiators.add( PersisterFactoryInitiator.INSTANCE );

		serviceInitiators.add( ConnectionProviderInitiator.INSTANCE );
		serviceInitiators.add( MultiTenantConnectionProviderInitiator.INSTANCE );
		serviceInitiators.add( DialectResolverInitiator.INSTANCE );
		serviceInitiators.add( DialectFactoryInitiator.INSTANCE );
		serviceInitiators.add( BatchBuilderInitiator.INSTANCE );
		serviceInitiators.add( JdbcServicesInitiator.INSTANCE );
		serviceInitiators.add( RefCursorSupportInitiator.INSTANCE );

		serviceInitiators.add( QueryTranslatorFactoryInitiator.INSTANCE );
		serviceInitiators.add( MutableIdentifierGeneratorFactoryInitiator.INSTANCE);

		serviceInitiators.add( JtaPlatformResolverInitiator.INSTANCE );
		serviceInitiators.add( JtaPlatformInitiator.INSTANCE );

		serviceInitiators.add( SessionFactoryServiceRegistryFactoryInitiator.INSTANCE );

		serviceInitiators.add( RegionFactoryInitiator.INSTANCE );

		serviceInitiators.add( TransactionCoordinatorBuilderInitiator.INSTANCE );

		serviceInitiators.add( ManagedBeanRegistryInitiator.INSTANCE );
		serviceInitiators.add( EntityCopyObserverFactoryInitiator.INSTANCE );

		serviceInitiators.trimToSize();

		return Collections.unmodifiableList( serviceInitiators );
	}

	static class SpringEntityManagerFactoryBuilderImpl extends EntityManagerFactoryBuilderImpl {

		@SuppressWarnings("rawtypes")
		public SpringEntityManagerFactoryBuilderImpl(PersistenceUnitInfoDescriptor persistenceUnitInfoDescriptor, Map properties) {
			super(persistenceUnitInfoDescriptor, properties);
		}

		@Override
		protected StandardServiceRegistryBuilder getStandardServiceRegistryBuilder(BootstrapServiceRegistry bsr) {
			return new SpringServiceRegistryBuilder(bsr);
		}
	}

	static class SpringServiceRegistryBuilder extends StandardServiceRegistryBuilder {

		@SuppressWarnings("rawtypes")
		public SpringServiceRegistryBuilder(BootstrapServiceRegistry bootstrapServiceRegistry) {
			super(bootstrapServiceRegistry,
					new HashMap(),
					new ConfigLoader(bootstrapServiceRegistry),
					new LoadedConfig(null) {
						@Override
						protected void addConfigurationValues(Map configurationValues) {
							// here, do nothing
						}
					},
					buildInitiatorList());
		}
	}
}
