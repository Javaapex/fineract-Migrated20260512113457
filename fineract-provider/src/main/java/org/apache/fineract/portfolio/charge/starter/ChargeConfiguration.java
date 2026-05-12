/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.charge.starter;

import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainServiceJpa;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformServiceImpl;
import org.apache.fineract.portfolio.charge.service.ChargeWritePlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class ChargeConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChargeDropdownReadPlatformService.class)
    public ChargeDropdownReadPlatformService chargeDropdownReadPlatformService() {
        return new ChargeDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ChargeReadPlatformService.class)
    public ChargeReadPlatformService chargeReadPlatformService(CurrencyReadPlatformService currencyReadPlatformService,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, JdbcTemplate jdbcTemplate,
            DropdownReadPlatformService dropdownReadPlatformService, FineractEntityAccessUtil fineractEntityAccessUtil,
            AccountingDropdownReadPlatformService accountingDropdownReadPlatformService, TaxReadPlatformService taxReadPlatformService,
            ConfigurationDomainServiceJpa configurationDomainServiceJpa, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        Objects.requireNonNull(namedParameterJdbcTemplate, "namedParameterJdbcTemplate cannot be null");
        Objects.requireNonNull(configurationDomainServiceJpa, "configurationDomainServiceJpa cannot be null");
        Objects.requireNonNull(taxReadPlatformService, "taxReadPlatformService cannot be null");
        Objects.requireNonNull(accountingDropdownReadPlatformService, "accountingDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(fineractEntityAccessUtil, "fineractEntityAccessUtil cannot be null");
        Objects.requireNonNull(dropdownReadPlatformService, "dropdownReadPlatformService cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        Objects.requireNonNull(chargeDropdownReadPlatformService, "chargeDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(currencyReadPlatformService, "currencyReadPlatformService cannot be null");
        return new ChargeReadPlatformServiceImpl(currencyReadPlatformService, chargeDropdownReadPlatformService, jdbcTemplate,
                dropdownReadPlatformService, fineractEntityAccessUtil, accountingDropdownReadPlatformService, taxReadPlatformService,
                configurationDomainServiceJpa, namedParameterJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ChargeWritePlatformService.class)
    public ChargeWritePlatformService chargeWritePlatformService(PlatformSecurityContext context,
            ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer, ChargeRepository chargeRepository,
            LoanProductRepository loanProductRepository, JdbcTemplate jdbcTemplate, FineractEntityAccessUtil fineractEntityAccessUtil,
            GLAccountRepositoryWrapper glAccountRepository, TaxGroupRepositoryWrapper taxGroupRepository,
            PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper) {
        Objects.requireNonNull(paymentTyperepositoryWrapper, "paymentTyperepositoryWrapper cannot be null");
        Objects.requireNonNull(taxGroupRepository, "taxGroupRepository cannot be null");
        Objects.requireNonNull(glAccountRepository, "glAccountRepository cannot be null");
        Objects.requireNonNull(loanProductRepository, "loanProductRepository cannot be null");
        Objects.requireNonNull(chargeRepository, "chargeRepository cannot be null");
        Objects.requireNonNull(fromApiJsonDeserializer, "fromApiJsonDeserializer cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        return new ChargeWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, chargeRepository, loanProductRepository,
                jdbcTemplate, fineractEntityAccessUtil, glAccountRepository, taxGroupRepository, paymentTyperepositoryWrapper);
    }
}
