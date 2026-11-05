package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.ItemPriority
import com.example.data.model.ItemType
import com.example.data.model.WorkspaceItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class WorkspaceRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createItem_authenticatedUser_succeeds(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = WorkspaceRepository(firestore)

    val item = WorkspaceItem(
      id = "item_${UUID.randomUUID()}",
      title = "Test Sync Note",
      content = "Testing local rule execution",
      type = ItemType.NOTE.name,
      priority = ItemPriority.MEDIUM.name,
      tags = listOf("test")
    )

    val result = withTimeout(5000L) {
      repo.createItem(item)
    }
    assertTrue(result.isSuccess)
  }

  @Test
  fun observeItems_authenticatedUser_emitsItems(): Unit = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repo = WorkspaceRepository(firestore)

    val itemId = "item_${UUID.randomUUID()}"
    val item = WorkspaceItem(
      id = itemId,
      title = "Emitted Sync Note",
      content = "Testing flow emission",
      type = ItemType.NOTE.name,
      priority = ItemPriority.HIGH.name,
      tags = emptyList()
    )

    withTimeout(5000L) {
      repo.createItem(item).getOrThrow()
    }

    val emitted = withTimeout(5000L) {
      repo.observeItems().first { list -> list.any { it.id == itemId } }
    }
    assertTrue(emitted.any { it.id == itemId })
  }

  @Test
  fun crossUserIsolation_bobCannotReadAliceItems(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = WorkspaceRepository(firestore)

    val itemId = "secret_${UUID.randomUUID()}"
    val item = WorkspaceItem(
      id = itemId,
      title = "Alice Confidential",
      content = "Top Secret",
      type = ItemType.NOTE.name,
      priority = ItemPriority.HIGH.name
    )

    withTimeout(5000L) {
      aliceRepo.createItem(item).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    var exceptionThrown: FirebaseFirestoreException? = null
    try {
      withTimeout(5000L) {
        firestore.collection("users").document(aliceUid).collection("items").document(itemId).get().await()
      }
    } catch (e: Exception) {
      exceptionThrown = e.unwrapFirestoreException()
    }
    assertNotNull(exceptionThrown)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exceptionThrown?.code)
  }

  @Test
  fun unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    auth.signOut()
    val repo = WorkspaceRepository(firestore)

    var exceptionCaught: Exception? = null
    try {
      withTimeout(3000L) {
        repo.observeItems().first()
      }
    } catch (e: Exception) {
      exceptionCaught = e
    }
    assertNotNull(exceptionCaught)
  }

  companion object {
    private const val ALICE_EMAIL = "alice@example.com"
    private const val BOB_EMAIL = "bob@example.com"
  }
}
