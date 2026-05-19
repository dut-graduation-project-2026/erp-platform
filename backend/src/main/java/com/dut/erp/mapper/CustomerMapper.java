package com.dut.erp.mapper;

import com.dut.erp.dto.request.CreateCustomerContactRequest;
import com.dut.erp.dto.request.CreateCustomerRequest;
import com.dut.erp.dto.request.UpdateCustomerContactRequest;
import com.dut.erp.dto.request.UpdateCustomerRequest;
import com.dut.erp.dto.response.CustomerBaseResponse;
import com.dut.erp.dto.response.CustomerContactResponse;
import com.dut.erp.dto.response.CustomerResponse;
import com.dut.erp.entity.Customer;
import com.dut.erp.entity.CustomerContact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {UserMapper.class})
public interface CustomerMapper {

  @Mapping(source = "customerType", target = "type")
  CustomerBaseResponse toCustomerBaseResponse(Customer entity);

  @Mapping(source = "customerType", target = "type")
  CustomerResponse toCustomerResponse(Customer entity);

  CustomerContactResponse toCustomerContactResponse(CustomerContact entity);

  @Mapping(source = "type", target = "customerType")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "organization", ignore = true)
  @Mapping(target = "contacts", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  Customer toCustomer(CreateCustomerRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  CustomerContact toCustomerContact(CreateCustomerContactRequest request);

  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  CustomerContact toCustomerContact(UpdateCustomerContactRequest request);

  @Mapping(source = "type", target = "customerType")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "code", ignore = true)
  @Mapping(target = "organization", ignore = true)
  @Mapping(target = "contacts", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateCustomerFromRequest(UpdateCustomerRequest request, @MappingTarget Customer entity);

  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  void updateCustomerContactFromRequest(UpdateCustomerContactRequest request, @MappingTarget CustomerContact entity);
}
