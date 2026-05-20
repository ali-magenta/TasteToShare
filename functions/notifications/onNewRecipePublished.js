const {admin} = require("../config/config");

const handleNewRecipePublished = async (event) => {
  const newRecipe = event.data.data();
  const recipeId = event.params.recipeId;

  const recipeTags = newRecipe.tags || [];

  if (recipeTags.length === 0) return null;

  const searchTags = recipeTags.slice(0, 10);

  try {
    const usersSnapshot = await admin.firestore()
        .collection("Users")
        .where("fcmToken", "!=", null)
        .where("preferences", "array-contains-any", searchTags)
        .get();

    if (usersSnapshot.empty) {
      console.log("No users match these recipe tags.");
      return null;
    }

    const promises = usersSnapshot.docs.map(async (userDoc) => {
      const userData = userDoc.data();
      const userId = userDoc.id;

      if (userId === newRecipe.authorId) return null;

      const message = {
        token: userData.fcmToken,
        data: {
          type: "RECOMMENDED_RECIPE",
          title: `New recipe for you, ${userData.fullName}! 🍽️`,
          recipeId: `${recipeId}`,
          recipeTitle: `${newRecipe.title}`,
          userId: `${userId}`,
          message: `A recipe matching your tastes was just published:` +
            `${newRecipe.title}`,
        },
      };

      try {
        await admin.messaging().send(message);
        console.log(`Notification sent to ${userId} for recipe ${recipeId}`);
      } catch (error) {
        console.error(`Failed to send notification to ${userId}:`, error);
      }
    });

    await Promise.all(promises);
    console.log(`Routine finished for recipe ${recipeId}.`);
  } catch (error) {
    console.error("Error while notifying for new recipes:", error);
  }

  return null;
};

module.exports = handleNewRecipePublished;
