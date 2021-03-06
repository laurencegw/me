package com.binarymonks.gonzo.core.articles.service

import com.binarymonks.gonzo.articleEntryNew
import com.binarymonks.gonzo.core.articles.api.Article
import com.binarymonks.gonzo.core.articles.api.ArticleDraft
import com.binarymonks.gonzo.core.articles.api.ArticleDraftNew
import com.binarymonks.gonzo.core.common.NotFound
import com.binarymonks.gonzo.core.test.GonzoTestConfig
import com.binarymonks.gonzo.core.test.harness.TestDataManager
import com.binarymonks.gonzo.core.time.clock
import com.binarymonks.gonzo.core.users.api.User
import com.binarymonks.gonzo.core.users.service.UserService
import com.binarymonks.gonzo.newUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.AnnotationConfigContextLoader
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
        classes = [
            GonzoTestConfig::class
        ],
        loader = AnnotationConfigContextLoader::class
)
class ArticlesServiceTest {

    lateinit var mockClock: Clock

    @Autowired
    lateinit var articleService: ArticlesService
    @Autowired
    lateinit var userService: UserService
    @Autowired
    lateinit var testDataManager: TestDataManager

    lateinit var user: User

    @BeforeEach
    fun setUp() {
        testDataManager.clearData()
        mockClock = Mockito.mock(Clock::class.java)
        clock = mockClock
        user = userService.createUser(newUser())
        itIsNow()
    }

    @Test
    fun createAndGetArticleEntryDraft() {
        val now = itIsNow()

        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val created = articleService.createArticle(newArticleEntry)

        val expectedArticleEntry = ArticleDraft(
                id = created.id,
                title = newArticleEntry.title,
                content = newArticleEntry.content,
                author = user.toPublicHeader(),
                published = false,
                unpublishedChanges = true,
                updated = now,
                created = now
        )

        Assertions.assertEquals(expectedArticleEntry, created)

        val retrieved = articleService.getArticleDraftByID(created.id)

        Assertions.assertEquals(expectedArticleEntry, retrieved)
    }

    @Test
    fun createArticleEntry_GetArticleEntryThatHasNotBeenPublished() {
        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val created = articleService.createArticle(newArticleEntry)

        Assertions.assertThrows(NotFound::class.java) {
            articleService.getArticleById(created.id)
        }
    }

    @Test
    fun updateArticleEntry_Unpublished() {
        val now = itIsNow()

        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val createdDraft = articleService.createArticle(newArticleEntry)

        val later = itIsNow(now.plusDays(1))

        val update = createdDraft.toUpdate().copy(
                title = "changed${createdDraft.title}",
                content = "changed${createdDraft.content}"
        )

        val expected = createdDraft.copy(
                title = update.title,
                content = update.content,
                updated = later
        )

        val updated = articleService.updateArticle(update)

        Assertions.assertEquals(expected, updated)
        Assertions.assertEquals(expected, articleService.getArticleDraftByID(createdDraft.id))
    }

    @Test
    fun publishArticleEntry() {
        val createdTime = itIsNow()

        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val createdDraft = articleService.createArticle(newArticleEntry)

        val publishTime = itIsNow(createdTime.plusDays(1))

        articleService.publishArticle(createdDraft.id)

        val expectedDraft = createdDraft.copy(
                published = true,
                unpublishedChanges = false
        )
        val expectedArticleEntry = Article(
                id = createdDraft.id,
                title = createdDraft.title,
                content = createdDraft.content,
                author = createdDraft.author,
                publishedOn = publishTime,
                lastEdited = publishTime
        )

        Assertions.assertEquals(expectedDraft, articleService.getArticleDraftByID(createdDraft.id))
        Assertions.assertEquals(expectedArticleEntry, articleService.getArticleById(createdDraft.id))

    }

    @Test
    fun updateArticleEntry_realChanges_AlreadyPublished() {
        val createdTime = itIsNow()

        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val createdDraft = articleService.createArticle(newArticleEntry)

        val publishTime = itIsNow(createdTime.plusDays(1))

        articleService.publishArticle(createdDraft.id)

        val updateTime = itIsNow(publishTime.plusDays(1))

        val update = createdDraft.toUpdate().copy(
                title = "changed${createdDraft.title}",
                content = "changed${createdDraft.content}"
        )

        val expectedArticleDraft = createdDraft.copy(
                title = update.title,
                content = update.content,
                updated = updateTime,
                published = true,
                unpublishedChanges = true
        )
        val expectedArticleEntry = Article(
                id = createdDraft.id,
                title = createdDraft.title,
                content = createdDraft.content,
                author = createdDraft.author,
                publishedOn = publishTime,
                lastEdited = publishTime
        )

        val updated = articleService.updateArticle(update)

        Assertions.assertEquals(expectedArticleDraft, updated)
        Assertions.assertEquals(expectedArticleEntry, articleService.getArticleById(createdDraft.id))

    }

    @Test
    fun updateArticleEntry_noChange_AlreadyPublished() {
        val createdTime = itIsNow()

        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val createdDraft = articleService.createArticle(newArticleEntry)

        val publishTime = itIsNow(createdTime.plusDays(1))

        articleService.publishArticle(createdDraft.id)

        itIsNow(publishTime.plusDays(1))

        val update = createdDraft.toUpdate().copy()

        val expectedArticleDraft = createdDraft.copy(
                title = update.title,
                content = update.content,
                published = true,
                unpublishedChanges = false
        )
        val expectedArticleEntry = Article(
                id = createdDraft.id,
                title = createdDraft.title,
                content = createdDraft.content,
                author = createdDraft.author,
                publishedOn = publishTime,
                lastEdited = publishTime
        )

        val updated = articleService.updateArticle(update)

        Assertions.assertEquals(expectedArticleDraft, updated)
        Assertions.assertEquals(expectedArticleEntry, articleService.getArticleById(createdDraft.id))

    }

    @Test
    fun publishArticleEntry_withChanges_AlreadyPublished() {
        val createdTime = itIsNow()

        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )

        val createdDraft = articleService.createArticle(newArticleEntry)

        val publishTime = itIsNow(createdTime.plusDays(1))

        articleService.publishArticle(createdDraft.id)

        val updateTime = itIsNow(publishTime.plusDays(1))

        val update = createdDraft.toUpdate().copy(
                title = "changed${createdDraft.title}",
                content = "changed${createdDraft.content}"
        )

        val updated = articleService.updateArticle(update)

        val publishedAgainTime = itIsNow(updateTime.plusDays(1))

        articleService.publishArticle(createdDraft.id)

        val expectedArticleDraft = createdDraft.copy(
                title = update.title,
                content = update.content,
                updated = updateTime,
                published = true,
                unpublishedChanges = false
        )
        val expectedArticleEntry = Article(
                id = createdDraft.id,
                title = updated.title,
                content = updated.content,
                author = createdDraft.author,
                publishedOn = publishTime,
                lastEdited = publishedAgainTime
        )

        Assertions.assertEquals(expectedArticleDraft, articleService.getArticleDraftByID(createdDraft.id))
        Assertions.assertEquals(expectedArticleEntry, articleService.getArticleById(createdDraft.id))
    }


    @Test
    fun getArticleEntryHeaders_allUsers_onlyPublished() {
        articleService.createArticle(articleEntryNew().copy(
                title = "Entry1",
                authorID = user.id
        )).toHeader() // Not published, should not see this one

        val articleEntryUser1_Published = articleService.createArticle(articleEntryNew().copy(
                title = "Entry2",
                authorID = user.id
        )).toHeader()
        articleService.publishArticle(articleEntryUser1_Published.id)

        val user2 = userService.createUser(newUser().copy("another@blah.com", "another"))
        val articleEntryUser2_Published = articleService.createArticle(articleEntryNew().copy(
                title = "Entry3",
                authorID = user2.id
        )).toHeader()
        articleService.publishArticle(articleEntryUser2_Published.id)

        val expectedHeaders = listOf(articleEntryUser1_Published, articleEntryUser2_Published)


        val actual = articleService.getArticleHeaders()
        Assertions.assertEquals(actual.size, expectedHeaders.size)
        for (header in expectedHeaders) {
            Assertions.assertTrue(actual.contains(header))
        }
    }

    @Test
    fun getArticleEntryHeadersByAuthor_onlyAuthor_onlyPublished() {
        articleService.createArticle(articleEntryNew().copy(
                title = "Entry1",
                authorID = user.id
        )).toHeader() // Not published, should not see this one

        val draftUser1Published = articleService.createArticle(articleEntryNew().copy(
                title = "User1Published",
                authorID = user.id
        )).toHeader()
        articleService.publishArticle(draftUser1Published.id)
        val user1PublishedHeader = articleService.getArticleById(draftUser1Published.id).toHeader()

        val user2 = userService.createUser(newUser().copy("another@blah.com", "another"))
        val draftUser2Created = articleService.createArticle(articleEntryNew().copy(
                title = "User2Published",
                authorID = user2.id
        )) // Not right author, should not see this one
        articleService.publishArticle(draftUser2Created.id)

        val expectedHeaders = listOf(user1PublishedHeader)

        val actual = articleService.getArticleHeadersByAuthor(user.id)
        Assertions.assertEquals(actual.size, expectedHeaders.size)
        for (header in expectedHeaders) {
            Assertions.assertTrue(actual.contains(header))
        }
    }

    @Test
    fun getArticleEntryDraftHeaders_allAuthorsArticles() {
        val articleEntryUser1_UnPublished = articleService.createArticle(articleEntryNew().copy(
                title = "Entry1",
                authorID = user.id
        )).toHeader()

        val articleEntryUser1_Published = articleService.createArticle(articleEntryNew().copy(
                title = "Entry2",
                authorID = user.id
        )).toHeader()
        articleService.publishArticle(articleEntryUser1_Published.id)

        val user2 = userService.createUser(newUser().copy("another@blah.com", "another"))
        val articleEntryUser2_Published = articleService.createArticle(articleEntryNew().copy(
                title = "Entry3",
                authorID = user2.id
        )).toHeader() // Not right author, should not see this one
        articleService.publishArticle(articleEntryUser2_Published.id)

        val expectedHeaders = listOf(articleEntryUser1_Published, articleEntryUser1_UnPublished)


        val actual = articleService.getArticleDraftHeaders(user.id)
        Assertions.assertEquals(actual.size, expectedHeaders.size)
        for (header in expectedHeaders) {
            Assertions.assertTrue(actual.contains(header))
        }
    }

    @Test
    fun deleteArticleEntry_alreadyPublished(){
        val newArticleEntry = ArticleDraftNew(
                title = "Some Articles Entry",
                content = "A bit of content",
                authorID = user.id
        )
        val createdDraft = articleService.createArticle(newArticleEntry)
        articleService.publishArticle(createdDraft.id)

        articleService.deleteArticle(createdDraft.id)

        Assertions.assertThrows(NoSuchElementException::class.java) {
            articleService.getArticleDraftByID(createdDraft.id)
        }
    }


    /**
     * Helper for setting the mock time.
     */
    private fun itIsNow(now: ZonedDateTime = LocalDateTime.now().atZone(ZoneId.of("UTC"))): ZonedDateTime {
        Mockito.`when`(mockClock.instant()).thenReturn(now.toInstant())
        return now
    }

}