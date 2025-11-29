package com.visioners.civic.reopen.dto;

/** Small enum wrapper to avoid leaking internal enum package references in controllers */
public enum ReopenStatusDTO { OPEN, ASSIGNED, PENDING, RESOLVED, REJECTED, CLOSED }
