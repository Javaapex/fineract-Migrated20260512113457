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
package org.apache.fineract.portfolio.delinquency.starter;

import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketMappingsRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceHelper;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyWritePlatformServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainService;
import org.apache.fineract.portfolio.delinquency.service.LoanDelinquencyDomainServiceImpl;
import org.apache.fineract.portfolio.delinquency.service.PossibleNextRepaymentCalculationServiceDiscovery;
import org.apache.fineract.portfolio.delinquency.spi.DelinquencyBucketUsageChecker;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyActionParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyBucketParseAndValidator;
import org.apache.fineract.portfolio.delinquency.validator.DelinquencyRangeParseAndValidator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionReadService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelinquencyConfiguration {

    @Bean
    @ConditionalOnMissingBean(DelinquencyReadPlatformService.class)
    public DelinquencyReadPlatformService delinquencyReadPlatformService(DelinquencyRangeRepository repositoryRange,
            DelinquencyBucketRepository repositoryBucket, LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory,
            DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository, DelinquencyRangeMapper mapperRange,
            DelinquencyBucketMapper mapperBucket, LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory, LoanRepository loanRepository,
            LoanDelinquencyDomainService loanDelinquencyDomainService,
            LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag,
            LoanDelinquencyActionRepository loanDelinquencyActionRepository,
            DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper, ConfigurationDomainService configurationDomainService,
            LoanTransactionRepository loanTransactionRepository,
            PossibleNextRepaymentCalculationServiceDiscovery possibleNextRepaymentCalculationService) {
        Objects.requireNonNull(possibleNextRepaymentCalculationService, "possibleNextRepaymentCalculationService cannot be null");
        Objects.requireNonNull(loanTransactionRepository, "loanTransactionRepository cannot be null");
        Objects.requireNonNull(configurationDomainService, "configurationDomainService cannot be null");
        Objects.requireNonNull(delinquencyEffectivePauseHelper, "delinquencyEffectivePauseHelper cannot be null");
        Objects.requireNonNull(loanDelinquencyActionRepository, "loanDelinquencyActionRepository cannot be null");
        Objects.requireNonNull(repositoryLoanInstallmentDelinquencyTag, "repositoryLoanInstallmentDelinquencyTag cannot be null");
        Objects.requireNonNull(loanDelinquencyDomainService, "loanDelinquencyDomainService cannot be null");
        Objects.requireNonNull(loanRepository, "loanRepository cannot be null");
        Objects.requireNonNull(mapperLoanDelinquencyTagHistory, "mapperLoanDelinquencyTagHistory cannot be null");
        Objects.requireNonNull(mapperBucket, "mapperBucket cannot be null");
        Objects.requireNonNull(mapperRange, "mapperRange cannot be null");
        Objects.requireNonNull(minimumPaymentPeriodAndRuleRepository, "minimumPaymentPeriodAndRuleRepository cannot be null");
        Objects.requireNonNull(repositoryLoanDelinquencyTagHistory, "repositoryLoanDelinquencyTagHistory cannot be null");
        Objects.requireNonNull(repositoryBucket, "repositoryBucket cannot be null");
        Objects.requireNonNull(repositoryRange, "repositoryRange cannot be null");
        return new DelinquencyReadPlatformServiceImpl(repositoryRange, repositoryBucket, minimumPaymentPeriodAndRuleRepository,
                repositoryLoanDelinquencyTagHistory, mapperRange, mapperBucket, mapperLoanDelinquencyTagHistory, loanRepository,
                loanDelinquencyDomainService, repositoryLoanInstallmentDelinquencyTag, loanDelinquencyActionRepository,
                delinquencyEffectivePauseHelper, configurationDomainService, loanTransactionRepository,
                possibleNextRepaymentCalculationService);
    }

    @Bean
    @ConditionalOnMissingBean(DelinquencyWritePlatformService.class)
    public DelinquencyWritePlatformService delinquencyWritePlatformService(DelinquencyBucketParseAndValidator dataValidatorBucket,
            DelinquencyRangeParseAndValidator dataValidatorRange, DelinquencyRangeRepository repositoryRange,
            DelinquencyBucketRepository repositoryBucket, DelinquencyBucketMappingsRepository repositoryBucketMappings,
            LoanDelinquencyTagHistoryRepository loanDelinquencyTagRepository, LoanRepositoryWrapper loanRepository,
            LoanProductRepository loanProductRepository, BusinessEventNotifierService businessEventNotifierService,
            LoanDelinquencyDomainService loanDelinquencyDomainService,
            LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository,
            DelinquencyReadPlatformService delinquencyReadPlatformService, LoanDelinquencyActionRepository loanDelinquencyActionRepository,
            DelinquencyActionParseAndValidator delinquencyActionParseAndValidator,
            DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper,
            DelinquencyWritePlatformServiceHelper delinquencyWritePlatformServiceHelper,
            DelinquencyMinimumPaymentPeriodAndRuleRepository delinquencyMinimumPaymentPeriodAndRuleRepository,
            final List<DelinquencyBucketUsageChecker> delinquencyBucketUsageCheckers) {
        Objects.requireNonNull(delinquencyMinimumPaymentPeriodAndRuleRepository, "delinquencyMinimumPaymentPeriodAndRuleRepository cannot be null");
        Objects.requireNonNull(delinquencyWritePlatformServiceHelper, "delinquencyWritePlatformServiceHelper cannot be null");
        Objects.requireNonNull(delinquencyActionParseAndValidator, "delinquencyActionParseAndValidator cannot be null");
        Objects.requireNonNull(delinquencyReadPlatformService, "delinquencyReadPlatformService cannot be null");
        Objects.requireNonNull(loanInstallmentDelinquencyTagRepository, "loanInstallmentDelinquencyTagRepository cannot be null");
        Objects.requireNonNull(businessEventNotifierService, "businessEventNotifierService cannot be null");
        Objects.requireNonNull(loanProductRepository, "loanProductRepository cannot be null");
        Objects.requireNonNull(loanDelinquencyTagRepository, "loanDelinquencyTagRepository cannot be null");
        Objects.requireNonNull(repositoryBucketMappings, "repositoryBucketMappings cannot be null");
        Objects.requireNonNull(dataValidatorRange, "dataValidatorRange cannot be null");
        Objects.requireNonNull(dataValidatorBucket, "dataValidatorBucket cannot be null");
        return new DelinquencyWritePlatformServiceImpl(dataValidatorBucket, dataValidatorRange, repositoryRange, repositoryBucket,
                repositoryBucketMappings, loanDelinquencyTagRepository, loanRepository, loanProductRepository, loanDelinquencyDomainService,
                loanInstallmentDelinquencyTagRepository, delinquencyReadPlatformService, loanDelinquencyActionRepository,
                delinquencyActionParseAndValidator, delinquencyEffectivePauseHelper, businessEventNotifierService,
                delinquencyWritePlatformServiceHelper, delinquencyMinimumPaymentPeriodAndRuleRepository, delinquencyBucketUsageCheckers);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDelinquencyDomainService.class)
    public LoanDelinquencyDomainService loanDelinquencyDomainService(DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper,
            LoanTransactionReadService loanTransactionReadService) {
        Objects.requireNonNull(loanTransactionReadService, "loanTransactionReadService cannot be null");
        return new LoanDelinquencyDomainServiceImpl(delinquencyEffectivePauseHelper, loanTransactionReadService);
    }
}
