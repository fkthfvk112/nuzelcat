package com.pet.cat.post.service

import com.pet.cat.exception.BusinessException
import com.pet.cat.exception.ErrorCode
import com.pet.cat.image.entity.ImageEntity
import com.pet.cat.image.service.ImageUploader
import com.pet.cat.post.dto.ImageCardDTO
import com.pet.cat.post.dto.PostCreateRequest
import com.pet.cat.post.dto.PostDetailDto
import com.pet.cat.post.entity.PostEntity
import com.pet.cat.post.entity.PostLikeEntity
import com.pet.cat.post.entity.PostViewEntity
import com.pet.cat.post.repository.PostLikeRepository
import com.pet.cat.post.repository.PostRepository
import com.pet.cat.post.repository.PostViewRepository
import com.pet.cat.post.service.Interface.IPostService
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity
import com.pet.cat.visitor.repository.VisitorRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException


@Service
class PostService(
    private val imageUploader: ImageUploader,
    private val postRepository: PostRepository,
    private val visitorRepository: VisitorRepository,
    private val postViewRepository: PostViewRepository,
    private val postLikeRepository: PostLikeRepository
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

    @Transactional
    override fun getPostDetail(postId: Long, visitor: CurrentVisitorDto?): PostDetailDto {
        val row = postRepository.findDetailByPostId(postId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found")

        if(visitor?.id != null){
            addViewCnt(row.getPostId(), visitor)
        }

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

    @Transactional(readOnly = true)
    override fun getImageCardList(
        title: String?,
        catName: String?,
        tag: String?,
        sortDir: String,
        pageable: Pageable
    ): Page<ImageCardDTO> {
        return postRepository.findImageCards(title, catName, tag, sortDir, pageable)
            .map { row ->
                ImageCardDTO(
                    postId      = row.getPostId(),
                    repreImgUrl = row.getRepreImgUrl() ?: "",
                    imgCnt      = row.getImgCnt(),
                    createdAt   = row.getCreatedAt(),
                    title       = row.getTitle(),
                    catName     = row.getCatName(),
                    tags        = splitConcatToList(row.getTagsConcat()),
                    likeCnt     = row.getLikeCnt(),
                    viewCnt     = row.getViewCnt()
                )
            }
    }

    @Transactional
    override fun addViewCnt(
        postId: Long,
        visitor: CurrentVisitorDto
    ): CommonResult<CRUDStateEnum, Long?> {
        val post = postRepository.findById(postId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found") }

        val visitorEntity = visitorRepository.findById(visitor.id!!)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found") }

        // 오늘 이미 조회한 기록이 있는지 확인
        val alreadyViewedToday = postViewRepository.existsTodayView(postId, visitorEntity.id!!)
        if (!alreadyViewedToday) {
            postViewRepository.save(
                PostViewEntity(
                    post = post,
                    visitor = visitorEntity
                )
            )
        }

        // 현재 총 조회수 반환
        val viewCnt = postViewRepository.countByPostId(postId)

        return CommonResult(
            data = viewCnt,
            message = "조회수 갱신 성공",
            state = CRUDStateEnum.UPDATE_SUCCESS
        )
    }

    @Transactional
    override fun addLikeCnt(postId: Long, visitor: CurrentVisitorDto): CommonResult<CRUDStateEnum, Long?> {
        val post = postRepository.findById(postId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found") }

        val visitorEntity = visitorRepository.findById(visitor.id!!)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found") }

        addViewCnt(post.id!!, visitor)

        // 오늘 이미 좋아요 했는지 확인
        val alreadyLikedToday = postLikeRepository.existsTodayLike(postId, visitorEntity.id!!)
        if(alreadyLikedToday){
            throw BusinessException(ErrorCode.TODAY_LIKE_DONE);
        }

        postLikeRepository.save(
            PostLikeEntity(
                post = post,
                visitor = visitorEntity
            )
        )

        // 현재 총 좋아요 수 반환
        val likeCnt = postLikeRepository.countByPostId(postId)

        return CommonResult(
            data    = likeCnt,
            message = "좋아요 성공",
            state   = CRUDStateEnum.UPDATE_SUCCESS
        )
    }
}
