package com.pet.cat.exception

class BusinessException : RuntimeException {
    val errorCode: ErrorCode

    constructor(message: String, errorCode: ErrorCode) : super(message) {
        this.errorCode = errorCode
    }

    constructor(errorCode: ErrorCode) : super(errorCode.message) {
        this.errorCode = errorCode
    }
}