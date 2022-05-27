package com.example.controller.data

data class ImageModel (var pathImage:String,var startTime: Long,var endTime:Long,var transition: Transition = Transition(0))