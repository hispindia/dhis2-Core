/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.webapi.controller.tracker.export;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hisp.dhis.common.AssignedUserSelectionMode;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.UID;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.webapi.controller.event.webrequest.PagingAndSortingCriteriaAdapter;
import org.hisp.dhis.webapi.controller.tracker.view.Attribute;
import org.hisp.dhis.webapi.webdomain.EndDateTime;
import org.hisp.dhis.webapi.webdomain.StartDateTime;

/**
 * Represents query parameters sent to {@link TrackerTrackedEntitiesExportController}.
 *
 * @author Giuseppe Nespolino
 */
@Data
@NoArgsConstructor
class TrackerTrackedEntityCriteria extends PagingAndSortingCriteriaAdapter {
  private String query;

  /** Comma separated list of attribute UIDs */
  @OpenApi.Property({UID[].class, Attribute.class})
  private String attribute;

  private String filter;

  /** Semicolon-delimited list of Organizational Unit UIDs */
  private String orgUnit;

  /** Selection mode for the specified organisation units, default is ACCESSIBLE. */
  private OrganisationUnitSelectionMode ouMode;

  /** a Program UID for which instances in the response must be enrolled in. */
  private String program;

  /** The {@see ProgramStatus} of the Tracked Entity Instance in the given program. */
  private ProgramStatus programStatus;

  /**
   * Indicates whether the Tracked Entity Instance is marked for follow up for the specified
   * Program.
   */
  private Boolean followUp;

  /** Start date for last updated. */
  private StartDateTime updatedAfter;

  /** End date for last updated. */
  private EndDateTime updatedBefore;

  /** The last updated duration filter. */
  private String updatedWithin;

  /** The given Program start date. */
  private StartDateTime enrollmentEnrolledAfter;

  /** The given Program end date. */
  private EndDateTime enrollmentEnrolledBefore;

  /** Start date for incident in the given program. */
  private StartDateTime enrollmentOccurredAfter;

  /** End date for incident in the given program. */
  private EndDateTime enrollmentOccurredBefore;

  /** Only returns Tracked Entity Instances of this type. */
  private String trackedEntityType;

  /** Semicolon-delimited list of Tracked Entity Instance UIDs */
  private String trackedEntity;

  /** Selection mode for user assignment of events. */
  private AssignedUserSelectionMode assignedUserMode;

  /** Semicolon-delimited list of user UIDs to filter based on events assigned to the users. */
  private String assignedUser;

  /** Program Stage UID, used for filtering TEIs based on the selected Program Stage */
  private String programStage;

  /** Status of any events in the specified program. */
  private EventStatus eventStatus;

  /** Start date for Event for the given Program. */
  private StartDateTime eventOccurredAfter;

  /** End date for Event for the given Program. */
  private EndDateTime eventOccurredBefore;

  /** Indicates whether not to include meta data in the response. */
  private boolean skipMeta;

  /** Indicates whether to include soft-deleted elements */
  private boolean includeDeleted;

  /** Indicates whether to include all TEI attributes */
  private boolean includeAllAttributes;

  /**
   * Potential Duplicate value for TEI. If null, we don't check whether a TEI is a
   * potentialDuplicate or not
   */
  private Boolean potentialDuplicate;

  @Override
  public boolean isLegacy() {
    return false;
  }
}
