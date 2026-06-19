const {admin} = require("../config/config");

const handleDuplicatedRecipe = async (event) => {
  const newRecipe = event.data.data();

  const originalRecipeId = newRecipe.originalRecipeId;

  // Not duplicated recipe
  if (originalRecipeId == null) {
    return null;
  }

  const originalRecipeDoc = await admin
      .firestore()
      .collection("Recipes")
      .doc(originalRecipeId)
      .get();

  if (!originalRecipeDoc.exists) {
    console.log("Recipe not found : ", originalRecipeId);
    return null;
  }

  const originalRecipe = originalRecipeDoc.data();
  const originalAuthorId = originalRecipe.authorId;

  const creatorId = newRecipe.authorId;

  if (originalAuthorId === creatorId) {
    console.log("User duplicated their own recipe, no notification needed.");
    return null;
  }

  const userDoc = await admin
      .firestore()
      .collection("Users")
      .doc(originalAuthorId)
      .get();

  if (!userDoc.exists) {
    console.log("User not found : ", originalAuthorId);
    return null;
  }

  const fcmToken = userDoc.data().fcmToken;
  if (!fcmToken) {
    console.log(`The user ${originalAuthorId} has no active token`);
    return null;
  }

  const message = {
    token: fcmToken,
    data: {
      type: "DUPLICATED_RECIPE",
      title: "Your recipe was duplicated! 🍴",
      recipeId: `${newRecipe.recipeId}`,
      recipeTitle: `${newRecipe.title}`,
      userId: `${originalAuthorId}`,
      message: `A chef was inspired by your ${originalRecipe.title}` +
       "and created a new version!",
    },
  };

  try {
    await admin.messaging().send(message);
    console.log("Notification sent to user :", fcmToken);
  } catch (error) {
    console.error(`Error sending notification to user ${originalAuthorId} :`
        , error);
  }

  return null;
};

module.exports = handleDuplicatedRecipe;
