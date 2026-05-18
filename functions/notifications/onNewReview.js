const {admin} = require("../config/firebase");

const handleNewReview = async (change, context) => {
  const beforeRecipe = change.before.data();
  const afterRecipe = change.after.data();

  const beforeReviews = beforeRecipe.reviews || [];
  const afterReviews = afterRecipe.reviews || [];

  if (afterReviews.length <= beforeReviews.length) {
    console.log("No new review, other recipe update.");
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
  const recipeId = context.params.recipeId;
  const reviewerId = newReview.userId;

  if (targetUserId === reviewerId) {
    // Self review, no notif
    return null;
  }

  const userDoc = await admin
      .firestore()
      .collection("users")
      .doc(targetUserId)
      .get();
  if (!userDoc.exists) {
    console.log("User not found");
    return null;
  }

  const fcmToken = userDoc.data().fcmToken;
  if (!fcmToken) {
    console.log("This user has no active token");
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
    console.error("Error while sending notification :", error);
  }

  return null;
};

module.exports = handleNewReview;
