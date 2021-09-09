package com.ados.cheerdiary.model

import java.util.*

data class DashboardMissionDTO(
    var scheduleDTO: ScheduleDTO? = null,
    var scheduleProgressDTO: ScheduleProgressDTO? = null
) {}

data class ScheduleDTO(
    var isSelected: Boolean = false,
    var docName: String? = null,
    var order: Long? = 0L,
    var title: String? = null,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var purpose: String? = null,
    var action: ACTION? = null,
    var appDTO: AppDTO? = null,
    var url: String? = null,
    var cycle: CYCLE? = CYCLE.DAY,
    var count: Long? = 0L,
    var isAlarm: Boolean? = false,
    var alarmDTO: AlarmDTO = AlarmDTO(),
    var isFanClub: Boolean? = false
) {
    enum class ACTION {
        APP, URL
    }

    enum class CYCLE {
        DAY, WEEK, MONTH, PERIOD
    }
}

data class AppDTO(
    var isSelected: Boolean = false,
    val packageName: String? = null,
    val appName: String? = null,
    val iconImage: String? = null,
    val order: Int? = 0
) {}

data class AlarmDTO(
    var alarmDate: Date? = null,
    var alarmHour: Int? = 0,
    var alarmMinute: Int? = 0,
    var dayOfWeek: MutableMap<String,Boolean> = mutableMapOf()
) {
    init {
        clearDayOfWeek()
    }

    fun clearDayOfWeek() {
        (1..7).forEach { dayOfWeek[it.toString()] = false }
    }

    fun everyDayOfWeek() {
        (1..7).forEach { dayOfWeek[it.toString()] = true }
    }
}

data class ScheduleProgressDTO(
    var docName: String? = null, // 날짜형식 (일: "20210907", 주:"2021090920210912" , 월: "202109", 기간: "210909210930"")
    var count: Long? = 0L
) {}