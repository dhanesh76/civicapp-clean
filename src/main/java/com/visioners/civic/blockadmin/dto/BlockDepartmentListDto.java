package com.visioners.civic.blockadmin.dto;

import java.util.List;
import java.util.Map;


public record BlockDepartmentListDto(
    Long blockId,
    List<Map<String, Object>> departments
) {}
