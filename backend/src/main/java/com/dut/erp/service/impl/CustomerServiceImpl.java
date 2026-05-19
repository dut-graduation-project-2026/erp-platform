package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateCustomerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateCustomerRequest;
import com.dut.erp.dto.response.CustomerBaseResponse;
import com.dut.erp.dto.response.CustomerResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.Customer;
import com.dut.erp.entity.CustomerContact;
import com.dut.erp.entity.Organization;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.CustomerMapper;
import com.dut.erp.repository.CustomerRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.CustomerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final OrganizationRepository organizationRepository;
  private final CustomerMapper customerMapper;

  @Override
  public PagedEntityResponse<CustomerBaseResponse> getCustomers(
      UUID organizationId, String query, PaginationRequest paginationRequest) {
    log.info(
        "Fetching customers for organization {} with query '{}', pagination: page={}, limit={}",
        organizationId,
        query,
        paginationRequest.page(),
        paginationRequest.limit());

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("updatedAt"), SortField.asc("createdAt"), SortField.asc("name")));

    Page<Customer> customerPage;
    if (query != null && !query.trim().isEmpty()) {
      customerPage =
          customerRepository.searchByOrganizationIdAndQuery(organizationId, query.trim(), pageable);
    } else {
      customerPage = customerRepository.findAllByOrganizationId(organizationId, pageable);
    }

    Page<CustomerBaseResponse> responsePage =
        customerPage.map(customerMapper::toCustomerBaseResponse);

    return PagedEntityResponse.from(responsePage);
  }

  @Override
  public CustomerResponse getCustomerById(UUID id, UUID organizationId) {
    log.info("Fetching customer {} for organization {}", id, organizationId);
    Customer customer =
        customerRepository
            .findByIdAndOrganizationIdWithContacts(id, organizationId)
            .orElseThrow(
                () -> {
                  log.warn("Customer {} not found for organization {}", id, organizationId);
                  return new ResourceNotFoundException("Customer not found with id: " + id);
                });

    return customerMapper.toCustomerResponse(customer);
  }

  @Override
  @Transactional
  public CustomerResponse createCustomer(UUID organizationId, CreateCustomerRequest request) {
    log.info("Creating customer code {} in organization {}", request.code(), organizationId);

    Organization organization =
        organizationRepository
            .findById(organizationId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Organization not found with id: " + organizationId));

    customerRepository
        .findByCodeAndOrganizationId(request.code(), organizationId)
        .ifPresent(
            c -> {
              throw new ResourceAlreadyExistsException(
                  "Customer code already exists: " + request.code());
            });

    if (request.contacts() != null) {
      long primaryCount =
          request.contacts().stream().filter(c -> Boolean.TRUE.equals(c.isPrimary())).count();
      if (primaryCount > 1) {
        throw new BadRequestException("At most one contact can be marked as primary");
      }
    }

    Customer customer = customerMapper.toCustomer(request);
    customer.setOrganization(organization);

    if (request.contacts() != null) {
      for (var contactReq : request.contacts()) {
        CustomerContact contact = customerMapper.toCustomerContact(contactReq);
        contact.setCustomer(customer);
        customer.getContacts().add(contact);
      }
    }

    customer = customerRepository.save(customer);
    log.info("Created customer {} with ID {}", customer.getCode(), customer.getId());

    return customerMapper.toCustomerResponse(customer);
  }

  @Override
  @Transactional
  public CustomerResponse updateCustomer(
      UUID id, UUID organizationId, UpdateCustomerRequest request) {
    log.info("Updating customer ID {} in organization {}", id, organizationId);

    Customer customer =
        customerRepository
            .findByIdAndOrganizationIdWithContacts(id, organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

    customerMapper.updateCustomerFromRequest(request, customer);

    if (request.contacts() != null) {
      long primaryCount =
          request.contacts().stream().filter(c -> Boolean.TRUE.equals(c.isPrimary())).count();
      if (primaryCount > 1) {
        throw new BadRequestException("At most one contact can be marked as primary");
      }

      Map<UUID, CustomerContact> existingContacts =
          customer.getContacts().stream()
              .filter(c -> c.getId() != null)
              .collect(Collectors.toMap(CustomerContact::getId, Function.identity()));

      List<CustomerContact> updatedContacts = new ArrayList<>();

      for (var contactReq : request.contacts()) {
        if (contactReq.id() != null && existingContacts.containsKey(contactReq.id())) {
          CustomerContact existing = existingContacts.get(contactReq.id());
          customerMapper.updateCustomerContactFromRequest(contactReq, existing);
          updatedContacts.add(existing);
        } else {
          CustomerContact newContact = customerMapper.toCustomerContact(contactReq);
          newContact.setCustomer(customer);
          updatedContacts.add(newContact);
        }
      }

      customer.getContacts().clear();
      customer.getContacts().addAll(updatedContacts);
    } else {
      customer.getContacts().clear();
    }

    customer = customerRepository.save(customer);
    log.info("Updated customer ID {}", customer.getId());

    return customerMapper.toCustomerResponse(customer);
  }

  @Override
  @Transactional
  public void deleteCustomer(UUID id, UUID organizationId) {
    log.info("Deleting customer ID {} in organization {}", id, organizationId);

    Customer customer =
        customerRepository
            .findByIdAndOrganizationIdWithContacts(id, organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

    customerRepository.delete(customer);
    log.info("Deleted customer ID {}", id);
  }
}
