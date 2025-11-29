package com.visioners.civic.reopen.model;

public enum ReopenStatus {
    OPEN,       // first reopen routed to department (if department exists)
    ASSIGNED,   // department assigned to worker
    PENDING,    // >=2 reopen -> BA decision required
    RESOLVED,   // worker resolved (awaiting dept approval)
    REJECTED,   // BA or dept rejected reopen
    CLOSED      // dept approved resolution (final)
}
