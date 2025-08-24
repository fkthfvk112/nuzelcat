package com.pet.cat.post.service

import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.image.service.ImageUploader
import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.entity.PostEntity
import com.pet.cat.post.repository.PostRepository
import com.pet.cat.post.service.Interface.IPostService
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException


@Service
class PostService(
    private val imageUploader: ImageUploader,
    private val postRepository: PostRepository
) : IPostService {
    override fun createPost(
        request: PostCreateRequest,
        visitor: CurrentVisitorDto?
    ): CommonResult<CRUDStateEnum, Long?> {

        val post = PostEntity(
            poster = visitor?.id?.let { VisitorEntity(id = it) },
            title = request.title,
            catName = request.catName,
            authorNickname = request.authorNickname,
            description = request.description,
            locationRegion = request.locationRegion,
            pw = request.pw
        )

        // ✅ 병렬 업로드 (ForkJoinPool commonPool 사용)
        val uploadedUrls = request.base64ImgList
            .parallelStream()
            .map { base64 -> imageUploader.uploadImg(base64) }
            .toList()

        // 업로드된 URL → ImageEntity 변환 후 Post에 추가
        uploadedUrls.forEachIndexed { index, url ->
            if (url.isNotBlank()) {
                val imageEntity = ImageEntity(
                    storagePath = url,
                    imageOrder = index + 1,
                    imageSort = "cat"
                )
                post.addImage(imageEntity)
            }
        }

        val savedPost = postRepository.save(post)

        return CommonResult(
            data = savedPost.id,
            message = "포스트 생성 성공",
            state = CRUDStateEnum.CREATE_SUCCESS
        )
    }

    private fun splitConcatToList(concat: String?): List<String> =
        concat?.takeIf { it.isNotBlank() }
            ?.split("||")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

    @Transactional(readOnly = true)
    override fun getPostDetail(postId: Long): PostDetailDto {
        val row = postRepository.findDetailByPostId(postId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found")

        return PostDetailDto(
            postId      = row.getPostId(),
            images      = splitConcatToList(row.getImagesConcat()),
            title       = row.getTitle(),
            catName     = row.getCatName(),
            author      = row.getAuthor(),
            description = row.getDescription(),
            tags        = splitConcatToList(row.getTagsConcat()),
            likeCnt     = row.getLikeCnt(),
            viewCnt     = row.getViewCnt(),
            createdAt   = row.getCreatedAt()
        )
    }
}
