package com.visioners.civic.reopen.dto;

import lombok.Value;

/** Response returned to user when reopen is accepted */
@Value
public class ReopenCreatedResponse {
    String reopenId;
    String detail;
}
