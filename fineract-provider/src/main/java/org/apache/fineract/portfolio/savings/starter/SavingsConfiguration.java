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
package org.apache.fineract.portfolio.savings.starter;

import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.DataTableValidator;
import org.apache.fineract.infrastructure.dataqueries.service.DatatableReadService;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.dataqueries.service.GenericDataService;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestIncentiveDropdownReadService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartAssembler;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartDropdownReadService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.apache.fineract.portfolio.savings.data.DepositAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.DepositProductDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsProductDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.DepositAccountDomainService;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.DepositProductAssembler;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccountRepository;
import org.apache.fineract.portfolio.savings.domain.FixedDepositProductRepository;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccountRepository;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositProductRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.SavingsProductAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.service.DepositAccountInterestRateChartReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountInterestRateChartReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.DepositAccountOnHoldTransactionReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountOnHoldTransactionReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.DepositAccountPreMatureCalculationPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountPreMatureCalculationPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.DepositApplicationProcessWritePlatformService;
import org.apache.fineract.portfolio.savings.service.DepositApplicationProcessWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.DepositsDropdownReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositsDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.FixedDepositProductWritePlatformService;
import org.apache.fineract.portfolio.savings.service.FixedDepositProductWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.GSIMReadPlatformService;
import org.apache.fineract.portfolio.savings.service.GSIMReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.GroupSavingsIndividualMonitoringWritePlatformService;
import org.apache.fineract.portfolio.savings.service.GroupSavingsIndividualMonitoringWritePlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.RecurringDepositProductWritePlatformService;
import org.apache.fineract.portfolio.savings.service.RecurringDepositProductWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.SavingsAccountApplicationTransitionApiJsonValidator;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.SavingsAccountDomainService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountInterestPostingService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountInterestPostingServiceImpl;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.SavingsAccountTemplateReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountTemplateReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.SavingsApplicationProcessWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.SavingsDropdownReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformServiceImpl;
import org.apache.fineract.portfolio.savings.service.SavingsProductWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.portfolio.savings.service.SavingsSchedularInterestPoster;
import org.apache.fineract.portfolio.savings.service.SavingsSchedularInterestPosterTask;
import org.apache.fineract.portfolio.savings.service.search.SavingsAccountTransactionSearchService;
import org.apache.fineract.portfolio.savings.service.search.SavingsAccountTransactionsSearchServiceImpl;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SavingsConfiguration {

    @Bean
    @ConditionalOnMissingBean(SavingsAccountTransactionSearchService.class)
    public SavingsAccountTransactionSearchService savingsAccountTransactionSearchService(PlatformSecurityContext context,
            GenericDataService genericDataService, DatabaseSpecificSQLGenerator sqlGenerator, DatatableReadService datatableService,
            DataTableValidator dataTableValidator, JdbcTemplate jdbcTemplate, SearchUtil searchUtil) {
        Objects.requireNonNull(searchUtil, "searchUtil cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        Objects.requireNonNull(dataTableValidator, "dataTableValidator cannot be null");
        Objects.requireNonNull(datatableService, "datatableService cannot be null");
        Objects.requireNonNull(sqlGenerator, "sqlGenerator cannot be null");
        Objects.requireNonNull(genericDataService, "genericDataService cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        return new SavingsAccountTransactionsSearchServiceImpl(context, genericDataService, sqlGenerator, datatableService,
                dataTableValidator, jdbcTemplate, searchUtil);
    }

    @Bean
    public DepositAccountInterestRateChartReadPlatformServiceImpl.DepositAccountInterestRateChartExtractor depositAccountInterestRateChartExtractor(
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new DepositAccountInterestRateChartReadPlatformServiceImpl.DepositAccountInterestRateChartExtractor(sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountInterestRateChartReadPlatformService.class)
    public DepositAccountInterestRateChartReadPlatformService depositAccountInterestRateChartReadService(PlatformSecurityContext context,
            JdbcTemplate jdbcTemplate,
            DepositAccountInterestRateChartReadPlatformServiceImpl.DepositAccountInterestRateChartExtractor chartExtractor,
            InterestRateChartDropdownReadService chartDropdownReadPlatformService,
            InterestIncentiveDropdownReadService interestIncentiveDropdownReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService) {
        Objects.requireNonNull(codeValueReadPlatformService, "codeValueReadPlatformService cannot be null");
        Objects.requireNonNull(interestIncentiveDropdownReadPlatformService, "interestIncentiveDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(chartDropdownReadPlatformService, "chartDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(chartExtractor, "chartExtractor cannot be null");
        return new DepositAccountInterestRateChartReadPlatformServiceImpl(context, jdbcTemplate, chartExtractor,
                chartDropdownReadPlatformService, interestIncentiveDropdownReadPlatformService, codeValueReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountOnHoldTransactionReadPlatformService.class)
    public DepositAccountOnHoldTransactionReadPlatformService depositAccountOnHoldTransactionReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        Objects.requireNonNull(paginationHelper, "paginationHelper cannot be null");
        Objects.requireNonNull(columnValidator, "columnValidator cannot be null");
        return new DepositAccountOnHoldTransactionReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, columnValidator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountPreMatureCalculationPlatformService.class)
    public DepositAccountPreMatureCalculationPlatformService depositAccountPreMatureCalculationPlatformService(
            FromJsonHelper fromJsonHelper, DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            DepositAccountAssembler depositAccountAssembler, SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            ConfigurationDomainService configurationDomainService, PaymentTypeReadService paymentTypeReadPlatformService) {
        Objects.requireNonNull(paymentTypeReadPlatformService, "paymentTypeReadPlatformService cannot be null");
        Objects.requireNonNull(configurationDomainService, "configurationDomainService cannot be null");
        Objects.requireNonNull(savingsAccountReadPlatformService, "savingsAccountReadPlatformService cannot be null");
        Objects.requireNonNull(depositAccountAssembler, "depositAccountAssembler cannot be null");
        Objects.requireNonNull(depositAccountTransactionDataValidator, "depositAccountTransactionDataValidator cannot be null");
        Objects.requireNonNull(fromJsonHelper, "fromJsonHelper cannot be null");
        return new DepositAccountPreMatureCalculationPlatformServiceImpl(fromJsonHelper, depositAccountTransactionDataValidator,
                depositAccountAssembler, savingsAccountReadPlatformService, configurationDomainService, paymentTypeReadPlatformService);

    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountReadPlatformService.class)
    public DepositAccountReadPlatformService depositAccountReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            DepositAccountInterestRateChartReadPlatformService chartReadPlatformService,
            InterestRateChartReadService productChartReadPlatformService,
            PaginationParametersDataValidator paginationParametersDataValidator, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationHelper paginationHelper, ClientReadPlatformService clientReadPlatformService,
            GroupReadPlatformService groupReadPlatformService, DepositProductReadPlatformService depositProductReadPlatformService,
            SavingsDropdownReadPlatformService savingsDropdownReadPlatformService, ChargeReadPlatformService chargeReadPlatformService,
            StaffReadService staffReadPlatformService, DepositsDropdownReadPlatformService depositsDropdownReadPlatformService,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService, DropdownReadPlatformService dropdownReadPlatformService,
            CalendarReadPlatformService calendarReadPlatformService, PaymentTypeReadService paymentTypeReadPlatformService) {
        Objects.requireNonNull(calendarReadPlatformService, "calendarReadPlatformService cannot be null");
        Objects.requireNonNull(dropdownReadPlatformService, "dropdownReadPlatformService cannot be null");
        Objects.requireNonNull(depositsDropdownReadPlatformService, "depositsDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(staffReadPlatformService, "staffReadPlatformService cannot be null");
        Objects.requireNonNull(chargeReadPlatformService, "chargeReadPlatformService cannot be null");
        Objects.requireNonNull(savingsDropdownReadPlatformService, "savingsDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(depositProductReadPlatformService, "depositProductReadPlatformService cannot be null");
        Objects.requireNonNull(groupReadPlatformService, "groupReadPlatformService cannot be null");
        Objects.requireNonNull(clientReadPlatformService, "clientReadPlatformService cannot be null");
        Objects.requireNonNull(paginationParametersDataValidator, "paginationParametersDataValidator cannot be null");
        Objects.requireNonNull(productChartReadPlatformService, "productChartReadPlatformService cannot be null");
        Objects.requireNonNull(chartReadPlatformService, "chartReadPlatformService cannot be null");
        return new DepositAccountReadPlatformServiceImpl(context, jdbcTemplate, chartReadPlatformService, productChartReadPlatformService,
                paginationParametersDataValidator, sqlGenerator, paginationHelper, clientReadPlatformService, groupReadPlatformService,
                depositProductReadPlatformService, savingsDropdownReadPlatformService, chargeReadPlatformService, staffReadPlatformService,
                depositsDropdownReadPlatformService, savingsAccountReadPlatformService, dropdownReadPlatformService,
                calendarReadPlatformService, paymentTypeReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountWritePlatformService.class)
    public DepositAccountWritePlatformService depositAccountWritePlatformService(PlatformSecurityContext context,
            SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
            SavingsAccountTransactionRepository savingsAccountTransactionRepository, DepositAccountAssembler depositAccountAssembler,
            DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            SavingsAccountChargeDataValidator savingsAccountChargeDataValidator,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            JournalEntryWritePlatformService journalEntryWritePlatformService, DepositAccountDomainService depositAccountDomainService,
            NoteRepository noteRepository, AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            ChargeRepositoryWrapper chargeRepository, SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            DepositAccountReadPlatformService depositAccountReadPlatformService, CalendarInstanceRepository calendarInstanceRepository,
            ConfigurationDomainService configurationDomainService, HolidayRepositoryWrapper holidayRepository,
            WorkingDaysRepositoryWrapper workingDaysRepository,
            DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository

    ) {
        Objects.requireNonNull(depositAccountOnHoldTransactionRepository, "depositAccountOnHoldTransactionRepository cannot be null");
        Objects.requireNonNull(workingDaysRepository, "workingDaysRepository cannot be null");
        Objects.requireNonNull(holidayRepository, "holidayRepository cannot be null");
        Objects.requireNonNull(calendarInstanceRepository, "calendarInstanceRepository cannot be null");
        Objects.requireNonNull(depositAccountReadPlatformService, "depositAccountReadPlatformService cannot be null");
        Objects.requireNonNull(accountTransfersWritePlatformService, "accountTransfersWritePlatformService cannot be null");
        Objects.requireNonNull(accountAssociationsReadPlatformService, "accountAssociationsReadPlatformService cannot be null");
        Objects.requireNonNull(savingsAccountChargeRepository, "savingsAccountChargeRepository cannot be null");
        Objects.requireNonNull(chargeRepository, "chargeRepository cannot be null");
        Objects.requireNonNull(accountTransfersReadPlatformService, "accountTransfersReadPlatformService cannot be null");
        Objects.requireNonNull(noteRepository, "noteRepository cannot be null");
        Objects.requireNonNull(depositAccountDomainService, "depositAccountDomainService cannot be null");
        Objects.requireNonNull(journalEntryWritePlatformService, "journalEntryWritePlatformService cannot be null");
        Objects.requireNonNull(applicationCurrencyRepositoryWrapper, "applicationCurrencyRepositoryWrapper cannot be null");
        Objects.requireNonNull(paymentDetailWritePlatformService, "paymentDetailWritePlatformService cannot be null");
        Objects.requireNonNull(savingsAccountChargeDataValidator, "savingsAccountChargeDataValidator cannot be null");
        Objects.requireNonNull(savingsAccountTransactionRepository, "savingsAccountTransactionRepository cannot be null");
        Objects.requireNonNull(savingAccountRepositoryWrapper, "savingAccountRepositoryWrapper cannot be null");
        return new DepositAccountWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepositoryWrapper,
                savingsAccountTransactionRepository, depositAccountAssembler, depositAccountTransactionDataValidator,
                savingsAccountChargeDataValidator, paymentDetailWritePlatformService, applicationCurrencyRepositoryWrapper,
                journalEntryWritePlatformService, depositAccountDomainService, noteRepository, accountTransfersReadPlatformService,
                chargeRepository, savingsAccountChargeRepository, accountAssociationsReadPlatformService,
                accountTransfersWritePlatformService, depositAccountReadPlatformService, calendarInstanceRepository,
                configurationDomainService, holidayRepository, workingDaysRepository, depositAccountOnHoldTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(DepositApplicationProcessWritePlatformService.class)
    public DepositApplicationProcessWritePlatformService depositApplicationProcessWritePlatformService(PlatformSecurityContext context,
            SavingsAccountRepositoryWrapper savingAccountRepository, FixedDepositAccountRepository fixedDepositAccountRepository,
            RecurringDepositAccountRepository recurringDepositAccountRepository, DepositAccountAssembler depositAccountAssembler,
            DepositAccountDataValidator depositAccountDataValidator, AccountNumberGenerator accountNumberGenerator,
            ClientRepositoryWrapper clientRepository, GroupRepository groupRepository, SavingsProductRepository savingsProductRepository,
            NoteRepository noteRepository, StaffRepositoryWrapper staffRepository,
            SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator,
            SavingsAccountChargeAssembler savingsAccountChargeAssembler, AccountAssociationsRepository accountAssociationsRepository,
            FromJsonHelper fromJsonHelper, CalendarInstanceRepository calendarInstanceRepository,
            ConfigurationDomainService configurationDomainService, AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            BusinessEventNotifierService businessEventNotifierService) {
        Objects.requireNonNull(businessEventNotifierService, "businessEventNotifierService cannot be null");
        Objects.requireNonNull(accountNumberFormatRepository, "accountNumberFormatRepository cannot be null");
        Objects.requireNonNull(accountAssociationsRepository, "accountAssociationsRepository cannot be null");
        Objects.requireNonNull(savingsAccountChargeAssembler, "savingsAccountChargeAssembler cannot be null");
        Objects.requireNonNull(savingsAccountApplicationTransitionApiJsonValidator, "savingsAccountApplicationTransitionApiJsonValidator cannot be null");
        Objects.requireNonNull(staffRepository, "staffRepository cannot be null");
        Objects.requireNonNull(savingsProductRepository, "savingsProductRepository cannot be null");
        Objects.requireNonNull(groupRepository, "groupRepository cannot be null");
        Objects.requireNonNull(clientRepository, "clientRepository cannot be null");
        Objects.requireNonNull(accountNumberGenerator, "accountNumberGenerator cannot be null");
        Objects.requireNonNull(depositAccountDataValidator, "depositAccountDataValidator cannot be null");
        Objects.requireNonNull(recurringDepositAccountRepository, "recurringDepositAccountRepository cannot be null");
        Objects.requireNonNull(fixedDepositAccountRepository, "fixedDepositAccountRepository cannot be null");
        return new DepositApplicationProcessWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepository,
                fixedDepositAccountRepository, recurringDepositAccountRepository, depositAccountAssembler, depositAccountDataValidator,
                accountNumberGenerator, clientRepository, groupRepository, savingsProductRepository, noteRepository, staffRepository,
                savingsAccountApplicationTransitionApiJsonValidator, savingsAccountChargeAssembler, accountAssociationsRepository,
                fromJsonHelper, calendarInstanceRepository, configurationDomainService, accountNumberFormatRepository,
                businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositProductReadPlatformService.class)
    public DepositProductReadPlatformService depositProductReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            InterestRateChartReadService interestRateChartReadPlatformService) {
        Objects.requireNonNull(interestRateChartReadPlatformService, "interestRateChartReadPlatformService cannot be null");
        return new DepositProductReadPlatformServiceImpl(context, jdbcTemplate, interestRateChartReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositsDropdownReadPlatformService.class)
    public DepositsDropdownReadPlatformService depositsDropdownReadPlatformService() {
        return new DepositsDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(FixedDepositProductWritePlatformService.class)
    public FixedDepositProductWritePlatformService fixedDepositProductWritePlatformService(PlatformSecurityContext context,
            FixedDepositProductRepository fixedDepositProductRepository, DepositProductDataValidator fromApiJsonDataValidator,
            DepositProductAssembler depositProductAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService, InterestRateChartAssembler chartAssembler) {
        Objects.requireNonNull(chartAssembler, "chartAssembler cannot be null");
        Objects.requireNonNull(accountMappingWritePlatformService, "accountMappingWritePlatformService cannot be null");
        Objects.requireNonNull(depositProductAssembler, "depositProductAssembler cannot be null");
        Objects.requireNonNull(fromApiJsonDataValidator, "fromApiJsonDataValidator cannot be null");
        Objects.requireNonNull(fixedDepositProductRepository, "fixedDepositProductRepository cannot be null");
        return new FixedDepositProductWritePlatformServiceJpaRepositoryImpl(context, fixedDepositProductRepository,
                fromApiJsonDataValidator, depositProductAssembler, accountMappingWritePlatformService, chartAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(GroupSavingsIndividualMonitoringWritePlatformService.class)
    public GroupSavingsIndividualMonitoringWritePlatformService groupSavingsIndividualMonitoringWritePlatformService(
            PlatformSecurityContext context, GSIMRepositoy gsimAccountRepository, LoanRepository loanRepository) {
        Objects.requireNonNull(loanRepository, "loanRepository cannot be null");
        Objects.requireNonNull(gsimAccountRepository, "gsimAccountRepository cannot be null");
        return new GroupSavingsIndividualMonitoringWritePlatformServiceImpl(context, gsimAccountRepository, loanRepository);
    }

    @Bean
    @ConditionalOnMissingBean(GSIMReadPlatformService.class)
    public GSIMReadPlatformService gsimReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            ColumnValidator columnValidator) {
        return new GSIMReadPlatformServiceImpl(jdbcTemplate, context, columnValidator);
    }

    @Bean
    @ConditionalOnMissingBean(RecurringDepositProductWritePlatformService.class)
    public RecurringDepositProductWritePlatformService recurringDepositProductWritePlatformService(PlatformSecurityContext context,
            RecurringDepositProductRepository recurringDepositProductRepository, DepositProductDataValidator fromApiJsonDataValidator,
            DepositProductAssembler depositProductAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService, InterestRateChartAssembler chartAssembler) {
        Objects.requireNonNull(recurringDepositProductRepository, "recurringDepositProductRepository cannot be null");
        return new RecurringDepositProductWritePlatformServiceJpaRepositoryImpl(context, recurringDepositProductRepository,
                fromApiJsonDataValidator, depositProductAssembler, accountMappingWritePlatformService, chartAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountApplicationTransitionApiJsonValidator.class)
    public SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator(
            FromJsonHelper fromApiJsonHelper) {
        Objects.requireNonNull(fromApiJsonHelper, "fromApiJsonHelper cannot be null");
        return new SavingsAccountApplicationTransitionApiJsonValidator(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountChargeReadPlatformService.class)
    public SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService(PlatformSecurityContext context,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, JdbcTemplate jdbcTemplate,
            DropdownReadPlatformService dropdownReadPlatformService, DatabaseSpecificSQLGenerator sqlGenerator) {
        Objects.requireNonNull(chargeDropdownReadPlatformService, "chargeDropdownReadPlatformService cannot be null");
        return new SavingsAccountChargeReadPlatformServiceImpl(context, chargeDropdownReadPlatformService, jdbcTemplate,
                dropdownReadPlatformService, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountInterestPostingService.class)
    public SavingsAccountInterestPostingService savingsAccountInterestPostingService(SavingsHelper savingsHelper) {
        Objects.requireNonNull(savingsHelper, "savingsHelper cannot be null");
        return new SavingsAccountInterestPostingServiceImpl(savingsHelper);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountReadPlatformService.class)
    public SavingsAccountReadPlatformService savingsAccountReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            SavingsAccountAssembler savingAccountAssembler, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator,
            SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper, ColumnValidator columnValidator,
            SavingsAccountTransactionRepository savingsAccountTransactionRepository) {
        Objects.requireNonNull(savingsAccountRepositoryWrapper, "savingsAccountRepositoryWrapper cannot be null");
        Objects.requireNonNull(savingAccountAssembler, "savingAccountAssembler cannot be null");
        return new SavingsAccountReadPlatformServiceImpl(context, jdbcTemplate, savingAccountAssembler, paginationHelper, columnValidator,
                sqlGenerator, savingsAccountRepositoryWrapper, savingsAccountTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountTemplateReadPlatformService.class)
    public SavingsAccountTemplateReadPlatformService savingsAccountTemplateReadPlatformService(PlatformSecurityContext context,
            JdbcTemplate jdbcTemplate, ClientReadPlatformService clientReadPlatformService,
            GroupReadPlatformService groupReadPlatformService, SavingsProductReadPlatformService savingProductReadPlatformService,
            StaffReadService staffReadPlatformService, SavingsDropdownReadPlatformService dropdownReadPlatformService,
            ChargeReadPlatformService chargeReadPlatformService, EntityDatatableChecksReadService entityDatatableChecksReadService,
            ColumnValidator columnValidator) {
        Objects.requireNonNull(entityDatatableChecksReadService, "entityDatatableChecksReadService cannot be null");
        Objects.requireNonNull(savingProductReadPlatformService, "savingProductReadPlatformService cannot be null");
        return new SavingsAccountTemplateReadPlatformServiceImpl(context, jdbcTemplate, clientReadPlatformService, groupReadPlatformService,
                savingProductReadPlatformService, staffReadPlatformService, dropdownReadPlatformService, chargeReadPlatformService,
                entityDatatableChecksReadService, columnValidator);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountWritePlatformService.class)
    public SavingsAccountWritePlatformService savingsAccountWritePlatformService(PlatformSecurityContext context,
            SavingsAccountDataValidator fromApiJsonDeserializer, SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
            StaffRepositoryWrapper staffRepository, SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            SavingsAccountAssembler savingAccountAssembler, SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator,
            SavingsAccountChargeDataValidator savingsAccountChargeDataValidator,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            JournalEntryWritePlatformService journalEntryWritePlatformService, SavingsAccountDomainService savingsAccountDomainService,
            NoteRepository noteRepository, AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, ChargeRepositoryWrapper chargeRepository,
            SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository, HolidayRepositoryWrapper holidayRepository,
            WorkingDaysRepositoryWrapper workingDaysRepository, ConfigurationDomainService configurationDomainService,
            DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, AppUserRepositoryWrapper appuserRepository,
            StandingInstructionRepository standingInstructionRepository, BusinessEventNotifierService businessEventNotifierService,
            GSIMRepositoy gsimRepository, SavingsAccountInterestPostingService savingsAccountInterestPostingService,
            ExternalIdFactory externalIdFactory, ErrorHandler errorHandler) {
        Objects.requireNonNull(errorHandler, "errorHandler cannot be null");
        Objects.requireNonNull(externalIdFactory, "externalIdFactory cannot be null");
        Objects.requireNonNull(savingsAccountInterestPostingService, "savingsAccountInterestPostingService cannot be null");
        Objects.requireNonNull(gsimRepository, "gsimRepository cannot be null");
        Objects.requireNonNull(standingInstructionRepository, "standingInstructionRepository cannot be null");
        Objects.requireNonNull(appuserRepository, "appuserRepository cannot be null");
        Objects.requireNonNull(entityDatatableChecksWritePlatformService, "entityDatatableChecksWritePlatformService cannot be null");
        Objects.requireNonNull(savingsAccountDomainService, "savingsAccountDomainService cannot be null");
        Objects.requireNonNull(savingsAccountTransactionDataValidator, "savingsAccountTransactionDataValidator cannot be null");
        Objects.requireNonNull(fromApiJsonDeserializer, "fromApiJsonDeserializer cannot be null");
        return new SavingsAccountWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, savingAccountRepositoryWrapper,
                staffRepository, savingsAccountTransactionRepository, savingAccountAssembler, savingsAccountTransactionDataValidator,
                savingsAccountChargeDataValidator, paymentDetailWritePlatformService, journalEntryWritePlatformService,
                savingsAccountDomainService, noteRepository, accountTransfersReadPlatformService, accountAssociationsReadPlatformService,
                chargeRepository, savingsAccountChargeRepository, holidayRepository, workingDaysRepository, configurationDomainService,
                depositAccountOnHoldTransactionRepository, entityDatatableChecksWritePlatformService, appuserRepository,
                standingInstructionRepository, businessEventNotifierService, gsimRepository, savingsAccountInterestPostingService,
                externalIdFactory, errorHandler);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsApplicationProcessWritePlatformService.class)
    public SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService(PlatformSecurityContext context,
            SavingsAccountRepositoryWrapper savingAccountRepository, SavingsAccountAssembler savingAccountAssembler,
            SavingsAccountDataValidator savingsAccountDataValidator, AccountNumberGenerator accountNumberGenerator,
            ClientRepositoryWrapper clientRepository, GroupRepository groupRepository, SavingsProductRepository savingsProductRepository,
            NoteRepository noteRepository, StaffRepositoryWrapper staffRepository,
            SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator,
            SavingsAccountChargeAssembler savingsAccountChargeAssembler, CommandProcessingService commandProcessingService,
            SavingsAccountDomainService savingsAccountDomainService, SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, BusinessEventNotifierService businessEventNotifierService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, GSIMRepositoy gsimRepository,
            GroupRepositoryWrapper groupRepositoryWrapper, GroupSavingsIndividualMonitoringWritePlatformService gsimWritePlatformService) {
        Objects.requireNonNull(gsimWritePlatformService, "gsimWritePlatformService cannot be null");
        Objects.requireNonNull(groupRepositoryWrapper, "groupRepositoryWrapper cannot be null");
        Objects.requireNonNull(savingsAccountWritePlatformService, "savingsAccountWritePlatformService cannot be null");
        Objects.requireNonNull(commandProcessingService, "commandProcessingService cannot be null");
        Objects.requireNonNull(savingsAccountDataValidator, "savingsAccountDataValidator cannot be null");
        return new SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepository, savingAccountAssembler,
                savingsAccountDataValidator, accountNumberGenerator, clientRepository, groupRepository, savingsProductRepository,
                noteRepository, staffRepository, savingsAccountApplicationTransitionApiJsonValidator, savingsAccountChargeAssembler,
                commandProcessingService, savingsAccountDomainService, savingsAccountWritePlatformService, accountNumberFormatRepository,
                businessEventNotifierService, entityDatatableChecksWritePlatformService, gsimRepository, groupRepositoryWrapper,
                gsimWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsDropdownReadPlatformService.class)
    public SavingsDropdownReadPlatformService savingsDropdownReadPlatformService() {
        return new SavingsDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(SavingsProductReadPlatformService.class)
    public SavingsProductReadPlatformService savingsProductReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            FineractEntityAccessUtil fineractEntityAccessUtil) {
        Objects.requireNonNull(fineractEntityAccessUtil, "fineractEntityAccessUtil cannot be null");
        return new SavingsProductReadPlatformServiceImpl(context, jdbcTemplate, fineractEntityAccessUtil);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsProductWritePlatformService.class)
    public SavingsProductWritePlatformService savingsProductWritePlatformService(PlatformSecurityContext context,
            SavingsProductRepository savingProductRepository, SavingsProductDataValidator fromApiJsonDataValidator,
            SavingsProductAssembler savingsProductAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            FineractEntityAccessUtil fineractEntityAccessUtil) {
        Objects.requireNonNull(savingsProductAssembler, "savingsProductAssembler cannot be null");
        Objects.requireNonNull(savingProductRepository, "savingProductRepository cannot be null");
        return new SavingsProductWritePlatformServiceJpaRepositoryImpl(context, savingProductRepository, fromApiJsonDataValidator,
                savingsProductAssembler, accountMappingWritePlatformService, fineractEntityAccessUtil);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean(SavingsSchedularInterestPoster.class)
    public SavingsSchedularInterestPoster savingsSchedularInterestPoster(
            SavingsAccountWritePlatformService savingsAccountWritePlatformService, JdbcTemplate jdbcTemplate,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService, PlatformSecurityContext platformSecurityContext

    ) {
        Objects.requireNonNull(platformSecurityContext, "platformSecurityContext cannot be null");
        return new SavingsSchedularInterestPoster(savingsAccountWritePlatformService, jdbcTemplate, savingsAccountReadPlatformService,
                platformSecurityContext);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean(SavingsSchedularInterestPosterTask.class)
    public SavingsSchedularInterestPosterTask savingsSchedularInterestPosterTask(SavingsSchedularInterestPoster interestPoster) {
        Objects.requireNonNull(interestPoster, "interestPoster cannot be null");
        return new SavingsSchedularInterestPosterTask(interestPoster);
    }
}
