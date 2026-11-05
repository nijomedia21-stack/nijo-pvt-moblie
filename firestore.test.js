const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read items", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("items").get());
});

test("Authenticated user: can create and read their own workspace item", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const itemRef = aliceDb.collection("users").doc(ALICE_UID).collection("items").doc("item_1");

  await assertSucceeds(
    itemRef.set({
      userId: ALICE_UID,
      type: "NOTE",
      title: "Sample Sync Note",
      content: "This is a note accessible across devices",
      isCompleted: false,
      priority: "MEDIUM",
      tags: ["work", "sync"],
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );

  await assertSucceeds(itemRef.get());
});

test("Cross-user isolation: Bob cannot read Alice's items", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("items").doc("secret_item").set({
      userId: ALICE_UID,
      type: "NOTE",
      title: "Alice Secret",
      content: "Classified",
      isCompleted: false,
      priority: "HIGH",
      tags: [],
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("items").doc("secret_item").get());
});

test("Cross-user isolation: Bob cannot write to Alice's items", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const itemRef = bobDb.collection("users").doc(ALICE_UID).collection("items").doc("bob_injected");

  await assertFails(
    itemRef.set({
      userId: BOB_UID,
      type: "TASK",
      title: "Malicious Task",
      content: "Hacked",
      isCompleted: false,
      priority: "LOW",
      tags: [],
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Chat messages: authenticated user can add chat message and cannot modify it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const chatRef = aliceDb.collection("users").doc(ALICE_UID).collection("chat_messages").doc("msg_1");

  await assertSucceeds(
    chatRef.set({
      userId: ALICE_UID,
      role: "user",
      text: "How do I optimize cross-screen layout?",
      modelUsed: "gemini-4-argon",
      hasImage: false,
      timestamp: new Date()
    })
  );

  await assertFails(
    chatRef.update({
      text: "Tampered message text"
    })
  );
});
