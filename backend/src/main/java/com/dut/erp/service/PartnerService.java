package com.dut.erp.service;

import com.dut.erp.dto.request.CreatePartnerRequest;
import com.dut.erp.dto.request.UpdateArchiveStatusRequest;
import com.dut.erp.dto.request.UpdatePartnerRequest;
import com.dut.erp.dto.response.PartnerResponse;
import java.util.List;
import java.util.UUID;

public interface PartnerService {

  /**
   * Creates a new partner within the given organization.
   *
   * @param organizationId the UUID of the organization
   * @param request the partner creation details
   * @return the created PartnerResponse
   */
  PartnerResponse createPartner(UUID organizationId, CreatePartnerRequest request);

  /**
   * Retrieves all partners belonging to the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @return list of PartnerResponse objects
   */
  List<PartnerResponse> getPartners(UUID organizationId);

  /**
   * Retrieves a single partner by ID within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner
   * @return the PartnerResponse
   */
  PartnerResponse getPartnerById(UUID organizationId, UUID partnerId);

  /**
   * Updates an existing partner.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner to update
   * @param request the update request containing new partner details
   * @return the updated PartnerResponse
   */
  PartnerResponse updatePartner(UUID organizationId, UUID partnerId, UpdatePartnerRequest request);

  /**
   * Updates the archive status of a partner (archive / unarchive).
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner
   * @param request the archive status update request
   * @return the updated PartnerResponse
   */
  PartnerResponse updatePartnerArchiveStatus(
      UUID organizationId, UUID partnerId, UpdateArchiveStatusRequest request);

  /**
   * Deletes a partner by its ID within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner to delete
   */
  void deletePartner(UUID organizationId, UUID partnerId);
}
