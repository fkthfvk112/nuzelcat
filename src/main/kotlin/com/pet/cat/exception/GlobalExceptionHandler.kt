package com.pet.cat.exception

import com.pet.cat.message.dto.EmailRequest
import com.pet.cat.message.service.EmailSender
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.io.BufferedReader
import java.io.IOException

@ControllerAdvice
class GlobalExceptionHandler(
    private val emailSender: EmailSender
) {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // @Valid 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        log.error("handleMethodArgumentNotValidException", e)
        val errorCode = ErrorCode.INVALID_INPUT_VALUE

        val response = ErrorResponse(
            status = errorCode.status,
            code = errorCode.code,
            error = "MethodArgumentNotValidException",
            message = errorCode.message
        )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.error("BusinessException", e)
        val errorCode = e.errorCode

        val response = ErrorResponse(
            status = errorCode.status,
            code = errorCode.code,
            error = "BusinessException",
            message = errorCode.message
        )

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    /** 잘못된 URL 요청 (404) */
    @ExceptionHandler(NoHandlerFoundException::class)
    protected fun handleNoHandlerFoundException(e: NoHandlerFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            code = "NOT_FOUND",
            error = "Resource Not Found",
            message = "요청한 리소스를 찾을 수 없습니다."
        )
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    /** PathVariable 누락 */
    @ExceptionHandler(MissingPathVariableException::class)
    protected fun handleMissingPathVariableException(e: MissingPathVariableException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "MISSING_PATH_VARIABLE",
            error = "Bad Request",
            message = "경로 변수 값이 누락되었습니다."
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    /** 지원하지 않는 Content-Type */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    protected fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            code = "UNSUPPORTED_MEDIA_TYPE",
            error = "Unsupported Media Type",
            message = "지원하지 않는 요청 형식입니다."
        )
        return ResponseEntity(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    }

    /** 잘못된 파라미터 타입 */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "TYPE_MISMATCH",
            error = "Bad Request",
            message = "잘못된 파라미터 타입입니다."
        )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
//    @Throws(MessagingException::class, IOException::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.error("handleException", e)
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR

        val requestMethod = request.method
        val requestUrl = request.requestURL.toString()
        val queryParams = request.queryString
        val requestBody = StringBuilder().apply {
            request.reader?.let { reader: BufferedReader ->
                reader.useLines { lines -> lines.forEach { append(it) } }
            }
        }

        val message = buildString {
            append("<div>Request Method : $requestMethod</div>")
            append("<div>Request URL : $requestUrl</div>")
            append("<div>Request Params : $queryParams</div>")
            append("<div>Request Body : $requestBody</div>")
            append("<div>Error Status : ${errorCode.status}</div>")
            append("<div>Error Code : ${errorCode.code}</div>")
            append("<div style='margin:0.2rem; padding:0.2rem; margin-top:1rem; background-color:#e1e1e1; word-wrap: break-word;'>$e</div>")
        }

        val serverHost = request.serverName
        if (!serverHost.equals("localhost", ignoreCase = true)) {
            emailSender.sendHtmlMessageAsync(
                EmailRequest(
                    emailTitle = "에러 보고",
                    emailAddress = "wjdwl545@naver.com",
                    emailContent = message
                )
            )
        }

        val response = ErrorResponse(
            status = errorCode.status,
            code = errorCode.code,
            error = "Exception",
            message = errorCode.message
        )

        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
