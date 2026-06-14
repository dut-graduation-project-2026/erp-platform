package com.dut.erp.service;

import com.dut.erp.dto.request.CreateSaleTeamRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateSaleTeamRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleTeamBaseResponse;
import com.dut.erp.dto.response.SaleTeamResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import java.util.List;
import java.util.UUID;

public interface SaleTeamService {

  PagedEntityResponse<SaleTeamBaseResponse> getSaleTeamsWithFilterByOrganizationId(
      UUID organizationId, String search, boolean isArchived, PaginationRequest paginationRequest);

  SaleTeamResponse getSaleTeamById(UUID organizationId, UUID id);

  List<SaleTeamResponse> getMySaleTeamsByOrganizationId(UUID organizationId, UUID userId);

  List<UserBaseResponse> getSaleTeamUsers(UUID organizationId, UUID id);

  SaleTeamResponse createSaleTeam(UUID organizationId, CreateSaleTeamRequest request);

  SaleTeamResponse updateSaleTeam(UUID organizationId, UUID id, UpdateSaleTeamRequest request);

  SaleTeamResponse updateSaleTeamArchiveStatus(UUID organizationId, UUID id, boolean isArchived);

  void deleteSaleTeam(UUID organizationId, UUID id);
}
