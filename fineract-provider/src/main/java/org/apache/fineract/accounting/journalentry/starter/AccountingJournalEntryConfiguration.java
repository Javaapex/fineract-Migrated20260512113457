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
package org.apache.fineract.accounting.journalentry.starter;

import org.apache.fineract.accounting.closure.domain.GLClosureRepository;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorForLoanFactory;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorForSavingsFactory;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorForSharesFactory;
import org.apache.fineract.accounting.journalentry.service.AccountingProcessorHelper;
import org.apache.fineract.accounting.journalentry.service.CashBasedAccountingProcessorForClientTransactions;
import org.apache.fineract.accounting.journalentry.service.JournalEntryReadPlatformService;
import org.apache.fineract.accounting.journalentry.service.JournalEntryReadPlatformServiceImpl;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.rule.domain.AccountingRuleRepository;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.service.AccountingService;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMappingRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountingJournalEntryConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountingProcessorHelper.class)
    public AccountingProcessorHelper accountingProcessorHelper(JournalEntryRepository glJournalEntryRepository,
            ProductToGLAccountMappingRepository accountMappingRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository, GLClosureRepository closureRepository,
            GLAccountRepository glAccountRepository, OfficeRepository officeRepository,
            AccountTransfersReadPlatformService accountTransfersReadPlatformService, ChargeRepositoryWrapper chargeRepositoryWrapper,
            BusinessEventNotifierService businessEventNotifierService) {
        Objects.requireNonNull(businessEventNotifierService, "businessEventNotifierService cannot be null");
        Objects.requireNonNull(chargeRepositoryWrapper, "chargeRepositoryWrapper cannot be null");
        Objects.requireNonNull(accountTransfersReadPlatformService, "accountTransfersReadPlatformService cannot be null");
        Objects.requireNonNull(officeRepository, "officeRepository cannot be null");
        Objects.requireNonNull(glAccountRepository, "glAccountRepository cannot be null");
        Objects.requireNonNull(closureRepository, "closureRepository cannot be null");
        Objects.requireNonNull(financialActivityAccountRepository, "financialActivityAccountRepository cannot be null");
        Objects.requireNonNull(accountMappingRepository, "accountMappingRepository cannot be null");
        Objects.requireNonNull(glJournalEntryRepository, "glJournalEntryRepository cannot be null");
        return new AccountingProcessorHelper(glJournalEntryRepository, accountMappingRepository, financialActivityAccountRepository,
                closureRepository, glAccountRepository, officeRepository, accountTransfersReadPlatformService, chargeRepositoryWrapper,
                businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(JournalEntryReadPlatformService.class)
    public JournalEntryReadPlatformService journalEntryReadPlatformService(JdbcTemplate jdbcTemplate,
            GLAccountReadPlatformService glAccountReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            ColumnValidator columnValidator, FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        Objects.requireNonNull(sqlGenerator, "sqlGenerator cannot be null");
        Objects.requireNonNull(paginationHelper, "paginationHelper cannot be null");
        Objects.requireNonNull(financialActivityAccountRepositoryWrapper, "financialActivityAccountRepositoryWrapper cannot be null");
        Objects.requireNonNull(columnValidator, "columnValidator cannot be null");
        Objects.requireNonNull(officeReadPlatformService, "officeReadPlatformService cannot be null");
        Objects.requireNonNull(glAccountReadPlatformService, "glAccountReadPlatformService cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        return new JournalEntryReadPlatformServiceImpl(jdbcTemplate, glAccountReadPlatformService, officeReadPlatformService,
                columnValidator, financialActivityAccountRepositoryWrapper, paginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(JournalEntryWritePlatformService.class)
    public JournalEntryWritePlatformService journalEntryWritePlatformService(GLClosureRepository glClosureRepository,
            GLAccountRepository glAccountRepository, JournalEntryRepository glJournalEntryRepository,
            OfficeRepositoryWrapper officeRepositoryWrapper, AccountingProcessorForLoanFactory accountingProcessorForLoanFactory,
            AccountingProcessorForSavingsFactory accountingProcessorForSavingsFactory,
            AccountingProcessorForSharesFactory accountingProcessorForSharesFactory, AccountingProcessorHelper helper,
            JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer, AccountingRuleRepository accountingRuleRepository,
            GLAccountReadPlatformService glAccountReadPlatformService, OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository,
            PlatformSecurityContext context, PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            CashBasedAccountingProcessorForClientTransactions accountingProcessorForClientTransactions,
            ConfigurationReadPlatformService configurationReadPlatformService, AccountingService accountingService,
            ExternalAssetOwnerRepository externalAssetOwnerRepository,
            LoanAmortizationAllocationMappingRepository loanAmortizationAllocationMappingRepository,
            LoanTransactionRepository loanTransactionRepository) {
        Objects.requireNonNull(loanTransactionRepository, "loanTransactionRepository cannot be null");
        Objects.requireNonNull(loanAmortizationAllocationMappingRepository, "loanAmortizationAllocationMappingRepository cannot be null");
        Objects.requireNonNull(externalAssetOwnerRepository, "externalAssetOwnerRepository cannot be null");
        Objects.requireNonNull(accountingService, "accountingService cannot be null");
        Objects.requireNonNull(configurationReadPlatformService, "configurationReadPlatformService cannot be null");
        Objects.requireNonNull(accountingProcessorForClientTransactions, "accountingProcessorForClientTransactions cannot be null");
        Objects.requireNonNull(paymentDetailWritePlatformService, "paymentDetailWritePlatformService cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(organisationCurrencyRepository, "organisationCurrencyRepository cannot be null");
        Objects.requireNonNull(accountingRuleRepository, "accountingRuleRepository cannot be null");
        Objects.requireNonNull(fromApiJsonDeserializer, "fromApiJsonDeserializer cannot be null");
        Objects.requireNonNull(helper, "helper cannot be null");
        Objects.requireNonNull(accountingProcessorForSharesFactory, "accountingProcessorForSharesFactory cannot be null");
        Objects.requireNonNull(accountingProcessorForSavingsFactory, "accountingProcessorForSavingsFactory cannot be null");
        Objects.requireNonNull(accountingProcessorForLoanFactory, "accountingProcessorForLoanFactory cannot be null");
        Objects.requireNonNull(officeRepositoryWrapper, "officeRepositoryWrapper cannot be null");
        Objects.requireNonNull(glClosureRepository, "glClosureRepository cannot be null");
        return new JournalEntryWritePlatformServiceJpaRepositoryImpl(glClosureRepository, glAccountRepository, glJournalEntryRepository,
                officeRepositoryWrapper, accountingProcessorForLoanFactory, accountingProcessorForSavingsFactory,
                accountingProcessorForSharesFactory, helper, fromApiJsonDeserializer, accountingRuleRepository,
                glAccountReadPlatformService, organisationCurrencyRepository, context, paymentDetailWritePlatformService,
                financialActivityAccountRepositoryWrapper, accountingProcessorForClientTransactions, configurationReadPlatformService,
                accountingService, externalAssetOwnerRepository, loanAmortizationAllocationMappingRepository, loanTransactionRepository);
    }
}
