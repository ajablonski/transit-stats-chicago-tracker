package com.github.ajablonski.shared.model

import java.time.LocalDateTime

case class Bus(destination: String,
               tripId: String,
               blockId: String,
               timestamp: LocalDateTime,
               vehicleId: Long,
               latitude: Double,
               longitude: Double) {}
