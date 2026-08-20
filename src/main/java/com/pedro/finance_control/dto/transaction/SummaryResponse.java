package com.pedro.finance_control.dto.transaction;

import java.math.BigDecimal;

public record SummaryResponse (BigDecimal receita,
                               BigDecimal despesa,
                               BigDecimal balance){
}
