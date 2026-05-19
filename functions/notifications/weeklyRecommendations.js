const {onSchedule} = require("firebase-functions/v2/scheduler");
const {admin} = require("../config/config");

// Send notif every monday at 11am
const sendWeeklyRecommendations = onSchedule({
  schedule: "0 11 * * 1",
  timeZone: "Europe/Paris",
  regions: ["europe-west1"],
  maxInstances: 5,
}, async (event) => {
  try {
    const usersSnapshot = await admin.firestore()
        .collection("Users")
        .where("fcmToken", "!=", null)
        .get();

    if (usersSnapshot.empty) {
      console.log("No users with active FCM token.");
      return null;
    }

    const promises = usersSnapshot.docs.map(async (userDoc) => {
      const userData = userDoc.data();
      const userId = userDoc.id;
      const userPreferences = userData.preferences || [];

      if (userPreferences.length === 0) return null;

      const matchingRecipesSnapshot = await admin.firestore()
          .collection("Recipes")
          .where("tags", "array-contains-any", userPreferences.slice(0, 10))
          .get();

      if (matchingRecipesSnapshot.empty) return null;

      // Recommend only recipes from other users
      const validDocs = matchingRecipesSnapshot.docs.filter(
          (doc) => doc.data().authorId !== userId,
      );

      if (validDocs.length === 0) return null;
      const randomIndex = Math.floor(Math.random() * validDocs.length);
      const recommendedRecipe = validDocs[randomIndex].data();

      const message = {
        token: userData.fcmToken,
        data: {
          type: "RECOMMENDED_RECIPE",
          title: `${userData.fullName}, wanna try a new recipe ?`,
          recipeId: `${recommendedRecipe.recipeId}`,
          recipeTitle: recommendedRecipe.title,
          userId: `${userId}`,
          message: `You should love this new recipe : ` +
           `${recommendedRecipe.title}!`,
        },
      };

      try {
        await admin.messaging().send(message);
        console.log("Notification sent to user :", userData.fcmToken);
      } catch (error) {
        console.error(`Error while sending notification to user ${userId} :`
            , error);
      }
    });

    await Promise.all(promises);
    console.log("Weekly recommendations routine terminated.");
  } catch (error) {
    console.error("Error while weekly recommendations routine:", error);
  }
  return null;
});

module.exports = sendWeeklyRecommendations;
