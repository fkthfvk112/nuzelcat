package com.pet.cat.image.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.pet.cat.exception.BusinessException
import com.pet.cat.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ImageUploader(
    private val cloudinary: Cloudinary,

    @Value("\${spring.profiles.active}")
    private val activeProfile: String
) {
    private val log = LoggerFactory.getLogger(ImageUploader::class.java)

    /** 이미지 업로드 후 저장된 이미지 url 반환 */
    fun uploadImg(file: MultipartFile): String {
        val folderName = if ("local".equals(activeProfile, ignoreCase = true)) {
            "nuzel-cat-test"
        } else {
            "nuzel-cat-live"
        }

        val params = ObjectUtils.asMap(
            "use_filename", false,
            "unique_filename", true,
            "overwrite", true,
            "folder", folderName
        )

        return try {
            val uploadResult = cloudinary.uploader().upload(file.bytes, params)
            log.info("[saveImageToAPIserver] - saved repri Img success")
            uploadResult["secure_url"].toString()
        } catch (e: IOException) {
            log.error("[saveImageToAPIserver] - IOException", e)
            ""
        }
    }

    /** base64 문자열 업로드 */
    fun uploadImg(base64Str: String): String {
        log.info("[uploadImg] - 이미지 업로드")

        val folderName = if ("local".equals(activeProfile, ignoreCase = true)) {
            "nuzel-cat-test"
        } else {
            "nuzel-cat-live"
        }

        val params = ObjectUtils.asMap(
            "use_filename", false,
            "unique_filename", true,
            "overwrite", true,
            "folder", folderName
        )

        return try {
            val uploadResult = cloudinary.uploader().upload(base64Str as Any, params)
            log.info("[saveImageToAPIserver] - saved repri Img success")
            uploadResult["secure_url"].toString()
        } catch (e: IOException) {
            log.error("[saveImageToAPIserver] - IOException", e)
            ""
        }
    }

    /** temp 폴더에 업로드 */
    fun uploadImgToTemp(base64Img: String, domainName: String): String {
        log.info("[uploadImgToTemp] - 이미지 업로드")

        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        val fileName = "${domainName}_${dateStr}_$uuid"

        val folderName = if ("local".equals(activeProfile, ignoreCase = true)) {
            "nuzel-cat-test"
        } else {
            "nuzel-cat-live"
        }

        val params = ObjectUtils.asMap(
            "use_filename", true,
            "public_id", fileName,
            "overwrite", true,
            "unique_filename", false,
            "folder", "$folderName/temp"
        )

        return try {
            val uploadResult = cloudinary.uploader().upload(base64Img as Any, params)
            log.info("[uploadImgToTemp] - saved Img success")
            uploadResult["secure_url"].toString()
        } catch (e: IOException) {
            log.error("[uploadImgToTemp] - IOException", e)
            ""
        }
    }

    /** temp → real 폴더 이동 */
    fun mvFileToReal(base64Img: String?, domainName: String, domainId: Long): String? {
        log.info("[mvFileToReal] - 이동 시작: base64Img={}, domainName={}", base64Img, domainName)

        if (base64Img.isNullOrBlank() || !base64Img.contains("/upload/")) {
            log.error("[mvFileToReal] - 유효하지 않은 Cloudinary URL")
            return null
        }

        val folderName = if ("local".equals(activeProfile, ignoreCase = true)) {
            "nuzel-cat-test"
        } else {
            "nuzel-cat-live"
        }

        val fileName = createFileName(domainName, domainId)

        return try {
            // URL에서 public_id 추출
            val afterUpload = base64Img.split("/upload/")[1]
            val publicIdWithoutVersion = afterUpload.replaceFirst("^[^/]+/".toRegex(), "")
            val publicIdWithExtension = publicIdWithoutVersion.substring(0, publicIdWithoutVersion.lastIndexOf("."))
            val fromPublicId = publicIdWithExtension

            // 이동할 대상 public_id
            val toPublicId = "$folderName/$domainName/$fileName"

            val result = cloudinary.uploader().rename(
                fromPublicId,
                toPublicId,
                ObjectUtils.asMap("overwrite", true)
            )

            log.info("[mvFileToReal] - Move Success: new URL = {}", result["secure_url"])
            result["secure_url"].toString()
        } catch (e: Exception) {
            log.error("[mvFileToReal] - 파일 이동 실패", e)
            throw BusinessException(ErrorCode.IMAGE_UPLOAD_FAIL)
        }
    }

    /** ex) recipe_3_20250502_fsfsdfdsfdsfsdfsd */
    fun createFileName(domainName: String, domainId: Long): String {
        val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        return "${domainName}_${domainId}_${dateStr}_$uuid"
    }
}
