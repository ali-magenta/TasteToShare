const {admin} = require("../config/config");

const handleNewReview = async (event) => {
  const beforeRecipe = event.data.before.data();
  const afterRecipe = event.data.after.data();

  const beforeReviews = beforeRecipe.reviews || [];
  const afterReviews = afterRecipe.reviews || [];

  // No new review, other recipe update
  if (afterReviews.length <= beforeReviews.length) {
    return null;
  }

  // find the new review
  const newReviews = afterReviews.filter(
      (afterReview) =>
        !beforeReviews.some(
            (beforeReview) => beforeReview.reviewId === afterReview.reviewId,
        ),
  );

  if (newReviews.length === 0) {
    return null;
  }

  const newReview = newReviews[0];

  const targetUserId = afterRecipe.authorId;
  const recipeId = event.params.recipeId;
  const reviewerId = newReview.userId;

  // Self review, no notif
  if (targetUserId === reviewerId) {
    return null;
  }


  const userDoc = await admin
      .firestore()
      .collection("Users")
      .doc(targetUserId)
      .get();
  if (!userDoc.exists) {
    console.log("User not found : ", targetUserId);
    return null;
  }

  const fcmToken = userDoc.data().fcmToken;
  if (!fcmToken) {
    console.log(`The user ${targetUserId} has no active token`);
    return null;
  }

  const message = {
    token: fcmToken,
    data: {
      type: "NEW_REVIEW",
      title: "New review on your recipe! ⭐️",
      recipeId: recipeId,
      recipeTitle: afterRecipe.title,
      targetId: newReview.reviewId,
      userId: targetUserId,
      message: `Someone reviewed your recipe: ${afterRecipe.title} ⭐️`,
    },
  };

  try {
    await admin.messaging().send(message);
    console.log("Notification sent to user :", fcmToken);
  } catch (error) {
    console.error(`Error while sending notification to user ${targetUserId} :`
        , error);
  }

  return null;
};

module.exports = handleNewReview;
