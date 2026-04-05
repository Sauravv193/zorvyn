package com.financeapp.mapper;

import com.financeapp.dto.response.FinancialRecordResponse;
import com.financeapp.entity.FinancialRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinancialRecordMapper {

    @Mapping(target = "createdBy", expression = "java(record.getCreatedBy().getUsername())")
    FinancialRecordResponse toResponse(FinancialRecord record);

    List<FinancialRecordResponse> toResponseList(List<FinancialRecord> records);
}
