package com.pet.cat.seo.service

import com.pet.cat.exception.BusinessException
import com.pet.cat.exception.ErrorCode
import com.pet.cat.post.repository.PostRepository
import com.pet.cat.seo.service.ISeoService.ISeoService
import com.redfin.sitemapgenerator.WebSitemapGenerator
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

@Service
class SeoService(
    val postRepository:PostRepository
):ISeoService {
    val frontUrl = "https://www.nuzelpet.com"
    @Throws(IOException::class)
    override fun getSiteMap(): String? {
        val tempDir = File(System.getProperty("java.io.tmpdir"))

        // sitemap 생성
        val wsg = WebSitemapGenerator(frontUrl, tempDir)
        setPostUrlToWsg(wsg)
        setPostListUrlToWsg(wsg)

        wsg.write() // 기존에 사이트맵 파일 있을 시 덮어쓰기

        val sitemapFile = tempDir.listFiles()
            ?.filter { it.name.startsWith("sitemap") && it.name.endsWith(".xml") }
            ?.maxByOrNull { it.lastModified() }
            ?: throw BusinessException(ErrorCode.SITEMAP_FILE_NOT_FOUND)

        val sitemapXml = Files.readString(sitemapFile.toPath(), StandardCharsets.UTF_8)

        // 캐싱
        return sitemapXml
    }

    private fun setPostUrlToWsg(wsg: WebSitemapGenerator) {
        val postIdList = postRepository.getNotDeletedPostIdList()

        for (postId in postIdList) {
            wsg.addUrl("$frontUrl/post/$postId")
        }
    }

    private fun setPostListUrlToWsg(wsg: WebSitemapGenerator, pageSize: Int = 10) {
        val totalCount = postRepository.countNotDeletedPosts()
        val maxPage = if (totalCount == 0L) 1 else ((totalCount - 1) / pageSize + 1)

        for (page in 1..maxPage) {
            wsg.addUrl("$frontUrl/post/list/$page")
        }
    }
}
