'use strict'

const functions = require('firebase-functions');
const admin=require('firebase-admin')
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

exports.sendNotification = functions.database.ref('/Users/{receiver_user_id}/Notifications/{notification_id}')
.onWrite((data, context) =>
{
	const receiver_user_id = context.params.receiver_user_id;
	const notification_id = context.params.notification_id;
	const messageText = data.getElementsByTagName('message')


	console.log('We have a notification to send to :' , receiver_user_id);


	if (!data.after.val()) 
	{
		console.log('A notification has been deleted :' , notification_id);
		return null;
	}

	const DeviceToken = admin.database().ref(`/Users/${receiver_user_id}/device_token`).once('value');

	return DeviceToken.then(result => 
	{
		const token_id = result.val();

		const payload = 
		{
			notification:
			{
				title: "New Message",
				body: `messageText`,
				icon: "default"
			}
		};

		return admin.messaging().sendToDevice(token_id, payload)
		.then(response => 
			{
				console.log('This was a notification feature.');
			});
	});
});
