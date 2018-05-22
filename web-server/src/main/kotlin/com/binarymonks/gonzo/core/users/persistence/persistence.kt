package com.binarymonks.gonzo.core.users.persistence

import com.binarymonks.gonzo.core.users.api.User
import org.springframework.data.repository.CrudRepository
import javax.persistence.*

@Entity
data class UserEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(nullable = false, unique = true)
        var email: String = "",

        @Column(nullable = false)
        var encryptedPassword: String = "",

        @Column(nullable = true)
        var firstName: String? = null,

        @Column(nullable = true)
        var lastName: String? = null,

        @OneToOne(cascade = [CascadeType.ALL])
        var spice: Spice = Spice()
) {
    fun toUser(): User = User(
            id = id!!,
            email = email,
            firstName = firstName,
            lastName = lastName
    )
}

@Entity
class Spice(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var userID: Long = -1,

        @Column(nullable = false)
        var pepper: String = ""
)

interface UserRepo : CrudRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity
}