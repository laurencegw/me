package com.binarymonks.gonzo.core.blog.service

import com.binarymonks.gonzo.core.blog.api.*
import com.binarymonks.gonzo.core.blog.persistence.BlogEntryEntity
import com.binarymonks.gonzo.core.blog.persistence.BlogRepo
import com.binarymonks.gonzo.core.time.nowUTC
import com.binarymonks.gonzo.core.users.persistence.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BlogService : Blog {

    @Autowired
    lateinit var blogRepo: BlogRepo

    @Autowired
    lateinit var userRepo: UserRepo


    override fun createBlogEntry(blogEntryNew: BlogEntryNew): BlogEntry {
        return blogRepo.save(BlogEntryEntity(
                title = blogEntryNew.title,
                content = blogEntryNew.content,
                author = userRepo.findById(blogEntryNew.authorID).get(),
                published = blogEntryNew.published,
                firstPublished = if (blogEntryNew.published) nowUTC() else null,
                created = nowUTC(),
                updated = nowUTC()
        )).toBlogEntry()
    }


    override fun updateBlogEntry(update: BlogEntryUpdate): BlogEntry {
        val entity = blogRepo.findById(update.id).get()
        entity.title = update.title
        entity.content = update.content
        if (!entity.published && update.published) {
            entity.firstPublished = nowUTC()
        }
        entity.published = update.published
        entity.updated = nowUTC()
        blogRepo.save(entity)
        return entity.toBlogEntry()
    }

    override fun getBlogEntryHeaders(): List<BlogEntryHeader> = blogRepo.findAll().map { it.toBlogEntry().toHeader() }

    override fun getBlogEntryById(id: Long): BlogEntry = blogRepo.findById(id).get().toBlogEntry()

}