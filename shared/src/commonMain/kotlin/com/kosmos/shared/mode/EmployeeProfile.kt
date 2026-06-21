package com.kosmos.shared.mode

data class EmployeeProfile(
    val homeLabel: String? = null,
    val homeLat: Double? = null,
    val homeLon: Double? = null,
    val workLabel: String? = null,
    val workLat: Double? = null,
    val workLon: Double? = null,
) {
    val hasHome: Boolean get() = homeLat != null && homeLon != null
    val hasWork: Boolean get() = workLat != null && workLon != null
    val isComplete: Boolean get() = hasWork
}
