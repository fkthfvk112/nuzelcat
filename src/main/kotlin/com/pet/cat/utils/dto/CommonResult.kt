package com.pet.cat.utils.dto

data class CommonResult<T, D> (
    val message:String?,
    val state:T,
    val data:D
)