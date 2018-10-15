package com.app.ariadne.tumaps.util.analyzers;

interface BuildingDestinationIdAnalyzer {
    boolean isValidDestinationId(String id);
    int getLevelFromId(String id);
}
