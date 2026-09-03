package com.tiagofsps.hunting_fishing_store.common.seeding;

public interface Seeder {
    void seed();
    
    default int getOrder() {
        return 0;
    }
}
