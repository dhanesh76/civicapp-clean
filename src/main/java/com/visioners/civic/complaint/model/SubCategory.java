package com.visioners.civic.complaint.model;

public enum SubCategory {

    // ---- Roads ----
    POTHOLES(Category.ROADS),
    OBSTACLES(Category.ROADS),
    CRACKS(Category.ROADS),
    WATERLOGGING(Category.ROADS),
    ROADS_OTHERS(Category.ROADS),

    // ---- Street Lights ----
    NOT_WORKING(Category.STREET_LIGHTS),
    FLICKERING(Category.STREET_LIGHTS),
    BROKEN_POLE(Category.STREET_LIGHTS),
    WIRES_EXPOSED(Category.STREET_LIGHTS),
    STREET_LIGHTS_OTHERS(Category.STREET_LIGHTS),

    // ---- Garbage ----
    UNCOLLECTED_GARBAGE(Category.GARBAGE),
    OVERFLOWING_BINS(Category.GARBAGE),
    ILLEGAL_DUMP(Category.GARBAGE),
    DEAD_ANIMALS(Category.GARBAGE),
    GARBAGE_OTHERS(Category.GARBAGE),

    // ---- Water Supply ----
    NO_WATER_SUPPLY(Category.WATER_SUPPLY),
    BROKEN_PIPELINE(Category.WATER_SUPPLY),
    CONTAMINATED_WATER(Category.WATER_SUPPLY),
    WATER_SUPPLY_OTHERS(Category.WATER_SUPPLY),

    // ---- Drainage ----
    BLOCKED_DRAINS(Category.DRAINAGE),
    OPEN_SEWAGE(Category.DRAINAGE),
    BAD_ODOR(Category.DRAINAGE),
    OVERFLOWING_MANHOLES(Category.DRAINAGE),
    DRAINAGE_OTHERS(Category.DRAINAGE),

    // ---- Public Safety ----
    ELECTRICAL_HAZARD(Category.PUBLIC_SAFETY),
    DAMAGED_PUBLIC_PROPERTY(Category.PUBLIC_SAFETY),
    PUBLIC_SAFETY_OTHERS(Category.PUBLIC_SAFETY),

    // ---- Parks ----
    UNCLEAN_PARKS(Category.PARKS_PUBLIC_SPACES),
    DAMAGED_EQUIPMENT(Category.PARKS_PUBLIC_SPACES),
    OVERGROWN_VEGETATION(Category.PARKS_PUBLIC_SPACES),
    PARKS_PUBLIC_SPACES_OTHERS(Category.PARKS_PUBLIC_SPACES),

    // ---- Pollution ----
    BURNING_WASTE(Category.POLLUTION),
    DUST_SMOKE_ISSUES(Category.POLLUTION),
    POLLUTION_OTHERS(Category.POLLUTION);

    private final Category category;

    SubCategory(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }
}
