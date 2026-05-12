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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link ProjectedAmortizationScheduleCalculator}. Delegates to
 * {@link ProjectedAmortizationScheduleModel} methods.
 */
@Component
public final class DefaultProjectedAmortizationScheduleCalculator implements ProjectedAmortizationScheduleCalculator {

    @Override
    @NonNull
    public ProjectedAmortizationScheduleModel generateModel(@NonNull final BigDecimal originationFeeAmount,
            @NonNull final BigDecimal netDisbursementAmount, @NonNull final BigDecimal totalPaymentValue,
            @NonNull final BigDecimal periodPaymentRate, final int npvDayCount, @NonNull final LocalDate expectedDisbursementDate,
            @NonNull final MathContext mc, @NonNull final MonetaryCurrency currency) {
        Objects.requireNonNull(currency, "currency cannot be null");
        Objects.requireNonNull(mc, "mc cannot be null");
        Objects.requireNonNull(expectedDisbursementDate, "expectedDisbursementDate cannot be null");
        Objects.requireNonNull(periodPaymentRate, "periodPaymentRate cannot be null");
        Objects.requireNonNull(totalPaymentValue, "totalPaymentValue cannot be null");
        Objects.requireNonNull(netDisbursementAmount, "netDisbursementAmount cannot be null");
        Objects.requireNonNull(originationFeeAmount, "originationFeeAmount cannot be null");
        return ProjectedAmortizationScheduleModel.generate(originationFeeAmount, netDisbursementAmount, totalPaymentValue,
                periodPaymentRate, npvDayCount, expectedDisbursementDate, mc, currency);
    }

    @Override
    @NonNull
    public ProjectedAmortizationScheduleModel addDisbursement(@NonNull final ProjectedAmortizationScheduleModel model,
            @NonNull final BigDecimal newDiscountAmount, @NonNull final BigDecimal newNetAmount, @NonNull final LocalDate newStartDate) {
        Objects.requireNonNull(newStartDate, "newStartDate cannot be null");
        Objects.requireNonNull(newNetAmount, "newNetAmount cannot be null");
        Objects.requireNonNull(newDiscountAmount, "newDiscountAmount cannot be null");
        Objects.requireNonNull(model, "model cannot be null");
        return model.regenerate(newDiscountAmount, newNetAmount, newStartDate);
    }

    @Override
    public void applyPayment(@NonNull final ProjectedAmortizationScheduleModel model, @NonNull final LocalDate paymentDate,
            @NonNull final BigDecimal paymentAmount) {
        Objects.requireNonNull(paymentAmount, "paymentAmount cannot be null");
        Objects.requireNonNull(paymentDate, "paymentDate cannot be null");
        model.applyPayment(paymentDate, paymentAmount);
    }
}
