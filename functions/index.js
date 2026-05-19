/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {
  onDocumentUpdated,
  onDocumentCreated,
} = require("firebase-functions/v2/firestore");
const {setGlobalOptions} = require("firebase-functions/v2");

const handleNewReview = require("./notifications/onNewReview");
const handleDuplicatedRecipe = require("./notifications/onDuplicatedRecipe");
const sendWeeklyRecommendations = require("./notifications/" +
  "weeklyRecommendations");

setGlobalOptions({region: "europe-west1", maxInstances: 10});
// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
exports.handleNewReview = onDocumentUpdated(
    "Recipes/{recipeId}",
    handleNewReview,
);

exports.handleDuplicatedRecipe = onDocumentCreated(
    "Recipes/{recipeId}",
    handleDuplicatedRecipe,
);

exports.sendWeeklyRecommendations = sendWeeklyRecommendations;

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
