const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_musician_1";
const BOB_UID = "bob_client_2";
const EVE_UID = "eve_outsider_3";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function nowPast() {
  return new Date(Date.now() - 2000);
}

function nowFuture() {
  return new Date(Date.now() + 60000);
}

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("musician_profiles").where("publicListed", "==", true).get());
  await assertFails(unauthDb.collection("posts").where("publicListed", "==", true).get());
  await assertFails(unauthDb.collection("gigs").where("publicListed", "==", true).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      handle: "@alice",
      role: "MUSICIAN",
      avatarUrl: "",
      bio: "",
      location: "Austin, TX",
      verified: false,
      createdAt: nowPast(),
      updatedAt: nowPast(),
    })
  );
});

test("UserAccount enforces strict ownership, role bounds, and verified=false", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = nowPast();

  // Self-verified escalation rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      handle: "@alice",
      role: "MUSICIAN",
      avatarUrl: "",
      bio: "Session guitarist",
      location: "Austin, TX",
      verified: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid user creation succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      handle: "@alice",
      role: "MUSICIAN",
      avatarUrl: "",
      bio: "Session guitarist",
      location: "Austin, TX",
      verified: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Cross-user read & write rejected
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      bio: "Hacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Owner partial update succeeds; verified escalation on update fails
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      bio: "Touring & studio guitarist",
      role: "CLIENT",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      verified: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("SocialConnection subcollection is strictly owner-scoped", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = nowPast();

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("social_connections")
      .doc("instagram")
      .set({
        userId: ALICE_UID,
        provider: "INSTAGRAM",
        externalHandle: "@alice.strings",
        profileUrl: "https://instagram.com/alice.strings",
        followerCount: 14200,
        connected: true,
        sandboxMode: true,
        lastSyncedAt: ts,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("social_connections").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("social_connections").get()
  );
});

test("MusicianProfile and PortfolioItem enforce filtered list queries and owner writes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = nowPast();

  // Owner existence check before profile doc is created MUST succeed; other user's check MUST fail
  await assertSucceeds(aliceDb.collection("musician_profiles").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("musician_profiles").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("musician_profiles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice Keys",
      handle: "@alicekeys",
      avatarUrl: "",
      coverUrl: "",
      bio: "Neo-soul keys & synths",
      location: "Brooklyn, NY",
      remoteAvailable: true,
      verified: false,
      instagramConnected: true,
      instagramHandle: "@alicekeys.ig",
      instagramFollowers: 18500,
      specialties: ["Producer", "Keys"],
      genres: ["Jazz", "Electronic"],
      instruments: ["Prophet-6", "Rhodes Mk1"],
      experienceYears: 7,
      hourlyRate: 95,
      perGigRate: 450,
      perTrackRate: 300,
      currency: "USD",
      availabilityStatus: "AVAILABLE",
      gearList: "Prophet-6, Rhodes Mk1, Apollo x8",
      hasVideoDemos: true,
      publicListed: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Filtered query succeeds, unfiltered query fails
  await assertSucceeds(
    bobDb.collection("musician_profiles").where("publicListed", "==", true).get()
  );
  await assertFails(bobDb.collection("musician_profiles").get());

  // Cross-user tamper fails
  await assertFails(
    bobDb.collection("musician_profiles").doc(ALICE_UID).update({
      hourlyRate: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Portfolio item with future timestamp fails; valid item succeeds
  await assertFails(
    aliceDb.collection("portfolio_items").doc("item_bad_ts").set({
      musicianId: ALICE_UID,
      musicianName: "Alice Keys",
      musicianHandle: "@alicekeys",
      title: "Future Jam",
      mediaType: "NATIVE_VIDEO",
      sourceUrl: "https://storage.googleapis.com/exoplayer-test-media-0/shortform_1.mp4",
      thumbnailUrl: "",
      aspectRatio: "9:16",
      genreTag: "Jazz",
      duration: "0:32",
      viewsCount: 0,
      publicListed: true,
      createdAt: nowFuture(),
      updatedAt: nowFuture(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("portfolio_items").doc("item_1").set({
      musicianId: ALICE_UID,
      musicianName: "Alice Keys",
      musicianHandle: "@alicekeys",
      title: "Rhodes Midnight Groove",
      mediaType: "INSTAGRAM_REEL",
      sourceUrl: "https://storage.googleapis.com/exoplayer-test-media-0/shortform_1.mp4",
      thumbnailUrl: "",
      aspectRatio: "9:16",
      genreTag: "Jazz",
      duration: "0:32",
      viewsCount: 120,
      publicListed: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Booking enforces no self-booking, strict participant isolation, and valid state transitions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();
  const ts = nowPast();

  // Self-booking MUST fail
  await assertFails(
    bobDb.collection("bookings").doc("self_booking").set({
      clientId: BOB_UID,
      clientName: "Bob Venue",
      musicianId: BOB_UID,
      musicianName: "Bob",
      musicianHandle: "@bob",
      musicianAvatarUrl: "",
      gigId: "",
      proposedRate: 400,
      rateType: "PER_GIG",
      eventDate: "2026-12-10",
      scopeNotes: "Self book attempt",
      lastMessagePreview: "Self book",
      status: "PENDING",
      participantUids: [BOB_UID, BOB_UID],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Bob books Alice
  await assertSucceeds(
    bobDb.collection("bookings").doc("booking_1").set({
      clientId: BOB_UID,
      clientName: "Bob Venue",
      musicianId: ALICE_UID,
      musicianName: "Alice Keys",
      musicianHandle: "@alicekeys",
      musicianAvatarUrl: "",
      gigId: "",
      proposedRate: 450,
      rateType: "PER_GIG",
      eventDate: "2026-12-15",
      scopeNotes: "Friday jazz set, 2x45m",
      lastMessagePreview: "Sent booking offer: $450 PER_GIG",
      status: "PENDING",
      participantUids: [BOB_UID, ALICE_UID],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Outsider Eve cannot read or query bookings
  await assertFails(eveDb.collection("bookings").doc("booking_1").get());
  await assertFails(eveDb.collection("bookings").get());

  // Participant filtered query succeeds
  await assertSucceeds(
    aliceDb.collection("bookings").where("participantUids", "array-contains", ALICE_UID).get()
  );

  // Messages in booking thread: participants succeed, outsider fails
  await assertSucceeds(
    bobDb
      .collection("bookings")
      .doc("booking_1")
      .collection("messages")
      .doc("msg_1")
      .set({
        bookingId: "booking_1",
        senderId: BOB_UID,
        senderName: "Bob Venue",
        text: "Looking forward to having you play!",
        messageType: "BOOKING_OFFER",
        participantUids: [BOB_UID, ALICE_UID],
        createdAt: ts,
      })
  );
  await assertFails(
    eveDb
      .collection("bookings")
      .doc("booking_1")
      .collection("messages")
      .doc("msg_1")
      .get()
  );
  await assertFails(
    eveDb
      .collection("bookings")
      .doc("booking_1")
      .collection("messages")
      .get()
  );
  await assertFails(
    eveDb
      .collection("bookings")
      .doc("booking_1")
      .collection("messages")
      .where("participantUids", "array-contains", ALICE_UID)
      .get()
  );

  // Alice accepts booking, then Bob marks completed; further edits fail (terminal state locked)
  await assertSucceeds(
    aliceDb.collection("bookings").doc("booking_1").update({
      status: "ACCEPTED",
      lastMessagePreview: "Booking accepted by Alice Keys",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    bobDb.collection("bookings").doc("booking_1").update({
      status: "COMPLETED",
      lastMessagePreview: "Session completed",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("bookings").doc("booking_1").update({
      status: "DECLINED",
      lastMessagePreview: "Cannot revert completed",
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Gigs and GigApplications enforce ownership and participant isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();
  const ts = nowPast();

  await assertSucceeds(
    bobDb.collection("gigs").doc("gig_1").set({
      clientId: BOB_UID,
      clientName: "Blue Note Lounge",
      title: "Saturday Jazz Quartet",
      description: "Looking for keys + upright bass for 2 sets.",
      requiredSpecialties: ["Session Guitarist", "Keys"],
      genre: "Jazz",
      location: "New York, NY",
      remote: false,
      eventDate: "2026-12-20",
      budgetMin: 350,
      budgetMax: 600,
      status: "OPEN",
      publicListed: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("gig_applications").doc("app_1").set({
      gigId: "gig_1",
      gigTitle: "Saturday Jazz Quartet",
      clientId: BOB_UID,
      musicianId: ALICE_UID,
      musicianName: "Alice Keys",
      musicianHandle: "@alicekeys",
      attachedReelTitle: "Rhodes Midnight Groove",
      attachedReelUrl: "https://storage.googleapis.com/exoplayer-test-media-0/shortform_1.mp4",
      proposedRate: 450,
      rateType: "PER_GIG",
      note: "Available and local to Brooklyn!",
      status: "APPLIED",
      participantUids: [ALICE_UID, BOB_UID],
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    bobDb.collection("gig_applications").where("participantUids", "array-contains", BOB_UID).get()
  );
  await assertFails(eveDb.collection("gig_applications").doc("app_1").get());
});
