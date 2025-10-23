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
import com.pet.cat.post.entity.PostTagEntity
import com.pet.cat.post.entity.PostViewEntity
import com.pet.cat.post.repository.PostLikeRepository
import com.pet.cat.post.repository.PostRepository
import com.pet.cat.post.repository.PostTagRepository
import com.pet.cat.post.repository.PostViewRepository
import com.pet.cat.post.service.Interface.IPostService
import com.pet.cat.utils.dto.CRUDStateEnum
import com.pet.cat.utils.dto.CommonResult
import com.pet.cat.visitor.dto.CurrentVisitorDto
import com.pet.cat.visitor.entity.VisitorEntity
import com.pet.cat.visitor.enums.ActionEnum
import com.pet.cat.visitor.repository.VisitorRepository
import com.pet.cat.visitor.service.Interface.IDailyActionService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime



@Service
class PostService(
    private val imageUploader: ImageUploader,
    private val postRepository: PostRepository,
    private val visitorRepository: VisitorRepository,
    private val postViewRepository: PostViewRepository,
    private val postLikeRepository: PostLikeRepository,
    private val dailyActionService: IDailyActionService,
    private val postTagRepository: PostTagRepository,
) : IPostService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun createPost(
        request: PostCreateRequest,
        visitor: CurrentVisitorDto?
    ): CommonResult<CRUDStateEnum, Long?> {

        // 하루 최대 게시글 생성 양 제한(사진 API 과도한 요청 방지)
        val todayPostCnt = dailyActionService.getTodayActionCnt(ActionEnum.POST_CREATE)
        if (todayPostCnt >= 500) {
            throw BusinessException(ErrorCode.TODAY_POST_LIMIT)
        }

        val post = PostEntity(
            poster = visitor?.id?.let { VisitorEntity(id = it) },
            title = request.title,
            catName = request.catName,
            authorNickname = request.authorNickname,
            description = request.description,
            locationRegion = request.locationRegion,
            pw = request.authorPassword
        )

        // 병렬 업로드 (ForkJoinPool commonPool 사용)
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

        // 태그 저장
        request.tags?.forEach {
            postTagRepository.save(PostTagEntity(
                post = savedPost,
                tag = it
            ))
        }

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
        tags: List<String>?,
        sortDir: String,
        exceptPostId: String?,
        fromYMD:String?,
        toYMD:String?,
        pageable: Pageable
    ): Page<ImageCardDTO> {

        // 1) 공백/빈 문자열은 null 취급
        val cleanTitle   = title?.trim()?.takeIf { it.isNotEmpty() }
        val cleanCatName = catName?.trim()?.takeIf { it.isNotEmpty() }

        // 2) 태그 필터 스위치 + IN () 구문 오류 방지용 더미
        val applyTag = if (!tags.isNullOrEmpty()) 1 else 0
        val safeTags = if (applyTag == 1) tags!! else listOf("__DUMMY__")

        // 3) sortDir 방어 (허용: asc | desc | score_asc | score_desc)
        val allowedSort = setOf("asc", "desc", "score_asc", "score_desc")
        val cleanSortDir = if (sortDir in allowedSort) sortDir else "desc"

        return postRepository.findImageCards(
            title     = cleanTitle,
            catName   = cleanCatName,
            tags      = safeTags,
            applyTag  = applyTag,
            sortDir   = cleanSortDir,
            fromYMD   = fromYMD,
            toYMD     = toYMD,
            exceptId  = exceptPostId,
            pageable  = pageable
        ).map { row ->
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
        val todayStart = LocalDate.now().atStartOfDay()
        val tomorrowStart = todayStart.plusDays(1)

        val count = postViewRepository.countTodayView(postId, visitorEntity.id!!, todayStart, tomorrowStart)
        if (count == 0L) {
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

        // 오늘 이미 좋아요 했는지 확인
        val alreadyLikedToday = postLikeRepository.existsTodayLike(postId, visitorEntity.id!!)
        if(alreadyLikedToday >= 1){
            log.error("[addLikeCnt] - aleady like! visitor id : {}, post id : {}", visitorEntity.id, postId)
            throw BusinessException(ErrorCode.TODAY_LIKE_DONE)
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

    @Transactional
    override fun deletePost(postId: Long, pw: String): CommonResult<CRUDStateEnum, Unit> {
        // 1. 게시글 조회
        val post = postRepository.findById(postId)
            .orElseThrow { throw BusinessException(ErrorCode.NOT_EXIST_POST) }

        // 2. 비밀번호가 세팅되지 않는 경우 삭제 불가
        if(post.pw.isNullOrBlank()){
            throw BusinessException(ErrorCode.NOT_DELETABLE_POST)
        }

        // 3. 이미 삭제된 경우
        if (post.isDel) {
            throw BusinessException(ErrorCode.DELETED_POST)
        }

        // 4. 비밀번호 검증
        if (post.pw.isNullOrBlank() || post.pw != pw) {
            throw BusinessException(ErrorCode.POST_PW_INCORRECT)
        }

        // 5. 삭제 처리 (soft delete)
        post.isDel = true
        post.deletedAt = LocalDateTime.now()
        postRepository.save(post)

        // 6. 성공 응답
        return CommonResult(
            data = Unit,
            message = "게시글이 삭제되었습니다.",
            state = CRUDStateEnum.DELETE_SUCCESS
        )
    }
}
