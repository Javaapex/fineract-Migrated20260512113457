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
package org.apache.fineract.organisation.teller.starter;

import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.teller.domain.CashierRepository;
import org.apache.fineract.organisation.teller.domain.CashierTransactionRepository;
import org.apache.fineract.organisation.teller.domain.TellerRepositoryWrapper;
import org.apache.fineract.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformService;
import org.apache.fineract.organisation.teller.service.TellerManagementReadPlatformServiceImpl;
import org.apache.fineract.organisation.teller.service.TellerWritePlatformService;
import org.apache.fineract.organisation.teller.service.TellerWritePlatformServiceJpaImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationTellerConfiguration {

    @Bean
    @ConditionalOnMissingBean(TellerManagementReadPlatformService.class)
    public TellerManagementReadPlatformService tellerManagementReadPlatformService(JdbcTemplate jdbcTemplate,
            PlatformSecurityContext context, OfficeReadPlatformService officeReadPlatformService, StaffReadService staffReadPlatformService,
            CurrencyReadPlatformService currencyReadPlatformService, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationHelper paginationHelper, SqlValidator sqlValidator) {
        Objects.requireNonNull(sqlValidator, "sqlValidator cannot be null");
        Objects.requireNonNull(paginationHelper, "paginationHelper cannot be null");
        Objects.requireNonNull(sqlGenerator, "sqlGenerator cannot be null");
        Objects.requireNonNull(currencyReadPlatformService, "currencyReadPlatformService cannot be null");
        Objects.requireNonNull(staffReadPlatformService, "staffReadPlatformService cannot be null");
        Objects.requireNonNull(officeReadPlatformService, "officeReadPlatformService cannot be null");
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        return new TellerManagementReadPlatformServiceImpl(jdbcTemplate, context, officeReadPlatformService, staffReadPlatformService,
                currencyReadPlatformService, sqlGenerator, paginationHelper, sqlValidator);
    }

    @Bean
    @ConditionalOnMissingBean(TellerWritePlatformService.class)
    public TellerWritePlatformService tellerWritePlatformService(PlatformSecurityContext context,
            TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer, TellerRepositoryWrapper tellerRepositoryWrapper,
            OfficeRepositoryWrapper officeRepositoryWrapper, StaffRepository staffRepository, CashierRepository cashierRepository,
            CashierTransactionRepository cashierTxnRepository, JournalEntryRepository glJournalEntryRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            CashierTransactionDataValidator cashierTransactionDataValidator) {
        Objects.requireNonNull(cashierTransactionDataValidator, "cashierTransactionDataValidator cannot be null");
        Objects.requireNonNull(financialActivityAccountRepositoryWrapper, "financialActivityAccountRepositoryWrapper cannot be null");
        Objects.requireNonNull(glJournalEntryRepository, "glJournalEntryRepository cannot be null");
        Objects.requireNonNull(cashierTxnRepository, "cashierTxnRepository cannot be null");
        Objects.requireNonNull(cashierRepository, "cashierRepository cannot be null");
        Objects.requireNonNull(staffRepository, "staffRepository cannot be null");
        Objects.requireNonNull(officeRepositoryWrapper, "officeRepositoryWrapper cannot be null");
        Objects.requireNonNull(tellerRepositoryWrapper, "tellerRepositoryWrapper cannot be null");
        Objects.requireNonNull(fromApiJsonDeserializer, "fromApiJsonDeserializer cannot be null");
        return new TellerWritePlatformServiceJpaImpl(context, fromApiJsonDeserializer, tellerRepositoryWrapper, officeRepositoryWrapper,
                staffRepository, cashierRepository, cashierTxnRepository, glJournalEntryRepository,
                financialActivityAccountRepositoryWrapper, cashierTransactionDataValidator);
    }
}
