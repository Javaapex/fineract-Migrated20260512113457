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
package org.apache.fineract.infrastructure.campaigns.sms.data;

import java.time.LocalDate;

@SuppressWarnings("unused")
public record SmsCampaignTimeLine(LocalDate submittedOnDate, String submittedByUsername, LocalDate activatedOnDate,
        String activatedByUsername, LocalDate closedOnDate, String closedByUsername) {
        Objects.requireNonNull(closedByUsername, "closedByUsername cannot be null");
        Objects.requireNonNull(closedOnDate, "closedOnDate cannot be null");
        Objects.requireNonNull(activatedByUsername, "activatedByUsername cannot be null");
        Objects.requireNonNull(activatedOnDate, "activatedOnDate cannot be null");
        Objects.requireNonNull(submittedByUsername, "submittedByUsername cannot be null");
        Objects.requireNonNull(submittedOnDate, "submittedOnDate cannot be null");

}
