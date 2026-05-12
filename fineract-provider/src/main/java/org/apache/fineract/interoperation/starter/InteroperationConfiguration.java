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
package org.apache.fineract.interoperation.starter;

import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.interoperation.domain.InteropIdentifierRepository;
import org.apache.fineract.interoperation.serialization.InteropDataValidator;
import org.apache.fineract.interoperation.service.InteropService;
import org.apache.fineract.interoperation.service.InteropServiceImpl;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class InteroperationConfiguration {

    @Bean
    @ConditionalOnMissingBean(InteropService.class)
    public InteropService interopService(PlatformSecurityContext securityContext, InteropDataValidator interopDataValidator,
            SavingsAccountRepository savingsAccountRepository, SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            ApplicationCurrencyRepository applicationCurrencyRepository, NoteRepository noteRepository,
            PaymentTypeRepository paymentTypeRepository, InteropIdentifierRepository identifierRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, SavingsHelper savingsHelper,
            SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            SavingsAccountDomainService savingsAccountService, ConfigurationDomainService configurationDomainService,
            JdbcTemplate jdbcTemplate, PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer, DatabaseSpecificSQLGenerator sqlGenerator) {
        Objects.requireNonNull(sqlGenerator, "sqlGenerator cannot be null");
        Objects.requireNonNull(toApiJsonSerializer, "toApiJsonSerializer cannot be null");
        Objects.requireNonNull(commandsSourceWritePlatformService, "commandsSourceWritePlatformService cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        Objects.requireNonNull(configurationDomainService, "configurationDomainService cannot be null");
        Objects.requireNonNull(savingsAccountService, "savingsAccountService cannot be null");
        Objects.requireNonNull(savingsAccountTransactionSummaryWrapper, "savingsAccountTransactionSummaryWrapper cannot be null");
        Objects.requireNonNull(savingsHelper, "savingsHelper cannot be null");
        Objects.requireNonNull(loanRepositoryWrapper, "loanRepositoryWrapper cannot be null");
        Objects.requireNonNull(identifierRepository, "identifierRepository cannot be null");
        Objects.requireNonNull(paymentTypeRepository, "paymentTypeRepository cannot be null");
        Objects.requireNonNull(noteRepository, "noteRepository cannot be null");
        Objects.requireNonNull(applicationCurrencyRepository, "applicationCurrencyRepository cannot be null");
        Objects.requireNonNull(savingsAccountTransactionRepository, "savingsAccountTransactionRepository cannot be null");
        Objects.requireNonNull(savingsAccountRepository, "savingsAccountRepository cannot be null");
        Objects.requireNonNull(interopDataValidator, "interopDataValidator cannot be null");
        Objects.requireNonNull(securityContext, "securityContext cannot be null");
        return new InteropServiceImpl(securityContext, interopDataValidator, savingsAccountRepository, savingsAccountTransactionRepository,
                applicationCurrencyRepository, noteRepository, paymentTypeRepository, identifierRepository, loanRepositoryWrapper,
                savingsHelper, savingsAccountTransactionSummaryWrapper, savingsAccountService, configurationDomainService, jdbcTemplate,
                commandsSourceWritePlatformService, toApiJsonSerializer, sqlGenerator);
    }
}
