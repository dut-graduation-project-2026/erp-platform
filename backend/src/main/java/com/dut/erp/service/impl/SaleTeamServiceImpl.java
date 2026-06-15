package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateSaleTeamRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateSaleTeamRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleTeamBaseResponse;
import com.dut.erp.dto.response.SaleTeamResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.SaleTeam;
import com.dut.erp.entity.User;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.SaleTeamMapper;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.SaleTeamRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.SaleTeamService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleTeamServiceImpl implements SaleTeamService {

  private final OrganizationRepository organizationRepository;
  private final SaleTeamRepository saleTeamRepository;
  private final UserRepository userRepository;
  private final SaleTeamMapper saleTeamMapper;
  private final UserMapper userMapper;

  @Override
  public PagedEntityResponse<SaleTeamBaseResponse> getSaleTeamsWithFilterByOrganizationId(
      UUID organizationId, String search, boolean isArchived, PaginationRequest paginationRequest) {
    log.info("Fetching sale teams for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? saleTeamRepository.findIdsByOrganizationIdAndIsArchivedAndSearch(
                organizationId, isArchived, search, pageable)
            : saleTeamRepository.findIdsByOrganizationIdAndIsArchived(
                organizationId, isArchived, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, SaleTeam> saleTeamMap =
        saleTeamRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(SaleTeam::getId, Function.identity()));

    List<SaleTeamBaseResponse> responses =
        ids.getContent().stream()
            .map(saleTeamMap::get)
            .filter(Objects::nonNull)
            .map(saleTeamMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public SaleTeamResponse getSaleTeamById(UUID organizationId, UUID id) {
    log.info("Fetching sale team {} in organization {}", id, organizationId);
    SaleTeam saleTeam = findSaleTeamByIdAndOrganizationId(id, organizationId);
    return saleTeamMapper.toResponse(saleTeam);
  }

  @Override
  public List<SaleTeamResponse> getMySaleTeamsByOrganizationId(UUID organizationId, UUID userId) {
    log.info("Fetching my sale teams for user {} in organization {}", userId, organizationId);
    List<SaleTeam> saleTeams =
        saleTeamRepository.findAllByOrganizationIdAndMemberId(organizationId, userId);
    return saleTeams.stream().map(saleTeamMapper::toResponse).collect(Collectors.toList());
  }

  @Override
  public List<UserBaseResponse> getSaleTeamUsers(UUID organizationId, UUID id) {
    log.info("Fetching users of sale team {} in organization {}", id, organizationId);
    SaleTeam saleTeam = findSaleTeamByIdAndOrganizationId(id, organizationId);
    return saleTeam.getMembers().stream()
        .map(userMapper::toUserBaseResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public SaleTeamResponse createSaleTeam(UUID organizationId, CreateSaleTeamRequest request) {
    Organization organization = findOrganizationById(organizationId);

    if (saleTeamRepository.existsByOrganizationIdAndName(organizationId, request.name())) {
      throw new ResourceAlreadyExistsException(
          "Sale team name already exists within this organization");
    }

    User leader = findUserById(request.leaderId());

    Set<User> members = new HashSet<>();
    if (request.memberIds() != null && !request.memberIds().isEmpty()) {
      List<User> orgMembers =
          userRepository.findAllByIdInAndOrganizationId(request.memberIds(), organizationId);
      if (orgMembers.size() != request.memberIds().size()) {
        throw new BadRequestException("One or more members do not belong to the organization");
      }
      members.addAll(orgMembers);
    }
    if (leader != null) {
      members.add(leader);
    }

    SaleTeam saleTeam =
        SaleTeam.builder()
            .organization(organization)
            .name(request.name())
            .isArchived(false)
            .leader(leader)
            .members(members)
            .build();

    saleTeam = saleTeamRepository.save(saleTeam);
    log.info("Created sale team {} in organization {}", saleTeam.getId(), organizationId);
    return saleTeamMapper.toResponse(saleTeam);
  }

  @Override
  @Transactional
  public SaleTeamResponse updateSaleTeam(
      UUID organizationId, UUID id, UpdateSaleTeamRequest request) {
    SaleTeam saleTeam = findSaleTeamByIdAndOrganizationId(id, organizationId);

    if (saleTeamRepository.existsByOrganizationIdAndNameAndIdNot(
        organizationId, request.name(), id)) {
      throw new ResourceAlreadyExistsException(
          "Sale team name already exists within this organization");
    }

    if (request.leaderId() != null) {
      if (!userRepository.existsByIdAndOrganizationId(request.leaderId(), organizationId)) {
        throw new BadRequestException("Leader does not belong to the organization");
      }
      User leader = findUserById(request.leaderId());
      saleTeam.setLeader(leader);
    }

    Set<User> members = new HashSet<>();
    if (request.memberIds() != null && !request.memberIds().isEmpty()) {
      List<User> orgMembers =
          userRepository.findAllByIdInAndOrganizationId(request.memberIds(), organizationId);
      if (orgMembers.size() != request.memberIds().size()) {
        throw new BadRequestException("One or more members do not belong to the organization");
      }
      members.addAll(orgMembers);
    }

    if (saleTeam.getLeader() != null) {
      members.add(saleTeam.getLeader());
    }

    saleTeam.setName(request.name());
    saleTeam.getMembers().clear();
    saleTeam.getMembers().addAll(members);

    saleTeam = saleTeamRepository.save(saleTeam);
    log.info("Updated sale team {} in organization {}", id, organizationId);
    return saleTeamMapper.toResponse(saleTeam);
  }

  @Override
  @Transactional
  public SaleTeamResponse updateSaleTeamArchiveStatus(
      UUID organizationId, UUID id, boolean isArchived) {
    SaleTeam saleTeam = findSaleTeamByIdAndOrganizationId(id, organizationId);
    saleTeam.setArchived(isArchived);
    saleTeam = saleTeamRepository.save(saleTeam);
    log.info("Updated archive status for sale team {} in organization {}", id, organizationId);
    return saleTeamMapper.toResponse(saleTeam);
  }

  @Override
  @Transactional
  public void deleteSaleTeam(UUID organizationId, UUID id) {
    SaleTeam saleTeam = findSaleTeamByIdAndOrganizationId(id, organizationId);
    saleTeamRepository.delete(saleTeam);
    log.info("Deleted sale team {} from organization {}", id, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private User findUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
  }

  private SaleTeam findSaleTeamByIdAndOrganizationId(UUID id, UUID organizationId) {
    return saleTeamRepository
        .findByIdAndOrganizationId(id, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Sale team not found with id: " + id));
  }
}
