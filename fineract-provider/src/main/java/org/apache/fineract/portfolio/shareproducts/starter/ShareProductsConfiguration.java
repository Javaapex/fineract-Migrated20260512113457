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
package org.apache.fineract.portfolio.shareproducts.starter;

import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.products.service.ShareProductReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductDividentPayOutDetailsRepositoryWrapper;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductRepositoryWrapper;
import org.apache.fineract.portfolio.shareproducts.serialization.ShareProductDataSerializer;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductCommandsServiceImpl;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDividendAssembler;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDividendReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDividendReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDropdownReadPlatformService;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductReadPlatformServiceImpl;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductWritePlatformService;
import org.apache.fineract.portfolio.shareproducts.service.ShareProductWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ShareProductsConfiguration {

    @Bean(value = "SHAREPRODUCT_COMMANDSERVICE")
    @ConditionalOnMissingBean(ShareProductCommandsServiceImpl.class)
    public ShareProductCommandsServiceImpl shareProductCommandsService(FromJsonHelper fromApiJsonHelper) {
        Objects.requireNonNull(fromApiJsonHelper, "fromApiJsonHelper cannot be null");
        return new ShareProductCommandsServiceImpl(fromApiJsonHelper);

    }

    @Bean
    @ConditionalOnMissingBean(ShareProductDividendAssembler.class)
    public ShareProductDividendAssembler shareProductDividendAssembler(ShareProductReadPlatformService shareProductReadPlatformService,
            ShareAccountReadPlatformService shareAccountReadPlatformService) {
        Objects.requireNonNull(shareAccountReadPlatformService, "shareAccountReadPlatformService cannot be null");
        Objects.requireNonNull(shareProductReadPlatformService, "shareProductReadPlatformService cannot be null");
        return new ShareProductDividendAssembler(shareProductReadPlatformService, shareAccountReadPlatformService);

    }

    @Bean
    @ConditionalOnMissingBean(ShareProductDividendReadPlatformService.class)
    public ShareProductDividendReadPlatformService shareProductDividendReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        Objects.requireNonNull(sqlGenerator, "sqlGenerator cannot be null");
        Objects.requireNonNull(paginationHelper, "paginationHelper cannot be null");
        Objects.requireNonNull(columnValidator, "columnValidator cannot be null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        return new ShareProductDividendReadPlatformServiceImpl(jdbcTemplate, columnValidator, paginationHelper, sqlGenerator);

    }

    @Bean
    @ConditionalOnMissingBean(ShareProductDropdownReadPlatformService.class)
    public ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService() {
        return new ShareProductDropdownReadPlatformServiceImpl();
    }

    @Bean(value = "shareReadPlatformService")
    @ConditionalOnMissingBean(ShareProductReadPlatformService.class)
    public ShareProductReadPlatformService shareProductReadPlatformService(JdbcTemplate jdbcTemplate,
            CurrencyReadPlatformService currencyReadPlatformService, ChargeReadPlatformService chargeReadPlatformService,
            ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService,
            AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
            PaginationHelper shareProductDataPaginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        Objects.requireNonNull(shareProductDataPaginationHelper, "shareProductDataPaginationHelper cannot be null");
        Objects.requireNonNull(accountMappingReadPlatformService, "accountMappingReadPlatformService cannot be null");
        Objects.requireNonNull(accountingDropdownReadPlatformService, "accountingDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(shareProductDropdownReadPlatformService, "shareProductDropdownReadPlatformService cannot be null");
        Objects.requireNonNull(chargeReadPlatformService, "chargeReadPlatformService cannot be null");
        Objects.requireNonNull(currencyReadPlatformService, "currencyReadPlatformService cannot be null");
        return new ShareProductReadPlatformServiceImpl(jdbcTemplate, currencyReadPlatformService, chargeReadPlatformService,
                shareProductDropdownReadPlatformService, accountingDropdownReadPlatformService, accountMappingReadPlatformService,
                shareProductDataPaginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(ShareProductWritePlatformService.class)
    public ShareProductWritePlatformService shareProductWritePlatformService(ShareProductRepositoryWrapper repository,
            ShareProductDataSerializer serializer, FromJsonHelper fromApiJsonHelper,
            ShareProductDividentPayOutDetailsRepositoryWrapper shareProductDividentPayOutDetailsRepository,
            ShareProductDividendAssembler shareProductDividendAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            BusinessEventNotifierService businessEventNotifierService) {
        Objects.requireNonNull(businessEventNotifierService, "businessEventNotifierService cannot be null");
        Objects.requireNonNull(accountMappingWritePlatformService, "accountMappingWritePlatformService cannot be null");
        Objects.requireNonNull(shareProductDividendAssembler, "shareProductDividendAssembler cannot be null");
        Objects.requireNonNull(shareProductDividentPayOutDetailsRepository, "shareProductDividentPayOutDetailsRepository cannot be null");
        Objects.requireNonNull(serializer, "serializer cannot be null");
        Objects.requireNonNull(repository, "repository cannot be null");
        return new ShareProductWritePlatformServiceJpaRepositoryImpl(repository, serializer, fromApiJsonHelper,
                shareProductDividentPayOutDetailsRepository, shareProductDividendAssembler, accountMappingWritePlatformService,
                businessEventNotifierService);
    }

}
