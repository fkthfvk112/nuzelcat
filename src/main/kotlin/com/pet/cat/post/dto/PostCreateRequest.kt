package com.pet.cat.post.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PostCreateRequest(

    /** 제목 (필수, 1~50자) */
    @field:NotBlank(message = "제목은 반드시 입력해야 합니다.")
    @field:Size(min = 1, max = 50, message = "제목은 1자 이상 50자 이하여야 합니다.")
    val title: String? = null,

    /** 고양이 이름 (비필수, 최대 40자) */
    @field:Size(max = 40, message = "고양이 이름은 최대 40자까지 입력할 수 있습니다.")
    val catName: String? = null,

    /** 작성자 닉네임 (비필수, 최대 40자) */
    @field:Size(max = 40, message = "작성자명은 최대 40자까지 입력할 수 있습니다.")
    val authorNickname: String? = null,

    /** 설명 (비필수, 최대 250자) */
    @field:Size(max = 250, message = "설명은 250자 이하여야 합니다.")
    val description: String? = null,

    /** 게시글 비밀번호 (비필수, 6~20자) */
    @field:Size(min = 6, max = 20, message = "게시글 비밀번호는 6자 이상 20자 이하여야 합니다.")
    val authorPassword: String? = null,

    /** 태그 (비필수, 최대 3개, 각 태그는 최대 50자) */
    val tags: List<@Size(max = 50, message = "태그는 최대 50자까지 가능합니다.") String>? = emptyList(),

    val locationRegion: String? = null,

    /** 업로드 이미지 (필수, 최소 1장, 최대 3장) */
    @field:Size(min = 1, max = 3, message = "사진은 최소 1장 이상, 최대 3장까지 업로드할 수 있습니다.")
    val base64ImgList: List<String> = emptyList()
)
