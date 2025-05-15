package com.example.providercomparison.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.domain.Persistable;

@Data
@Table("servusspeed_product")
public class ServusSpeedProductEntity implements Persistable<String> {

    @Id
    private String productId;

    /* --- all other columns (unchanged) --- */
    private String provider;
    private String connectionType;
    private Integer speed;
    private Integer speedLimitFrom;
    private Integer contractDuration;
    private Integer maxAge;

    private Integer discountedMonthlyCost;
    private Integer monthlyCost;
    private Integer monthlyCostAfter24m;
    private Integer monthlyDiscountValue;
    private Integer maxDiscount;
    private Boolean installationService;
    private Boolean tvIncluded;
    private String tvBrand;

    /* DB default will fill this – do NOT set before first save */
    private java.time.LocalDateTime lastUpdated;

    /* ---------------------------------------------------------------- */
    /** Flag that tells Spring-Data whether to INSERT or UPDATE. */
    @Transient
    private boolean newRow = true;

    @Override public String getId()     { return productId; }
    @Override public boolean isNew()    { return newRow; }
}
