# ⚠️ This repository is no longer maintained. If you are looking for OpenTok Android SDK samples please check the [opentok-android-sdk-samples](https://github.com/opentok/opentok-android-sdk-samples) repository.


## Learning OpenTok Android Sample App

<img src="https://assets.tokbox.com/img/vonage/Vonage_VideoAPI_black.svg" height="48px" alt="Tokbox is now known as Vonage" />

This sample app shows how to accomplish basic tasks using the
[OpenTok Android SDK](https://tokbox.com/opentok/libraries/client/android/).
It connects the user with another client so that they can share an OpenTok audio-video
chat session. The app uses the OpenTok Android SDK to implement the following:

* Connect to an OpenTok session
* Publish an audio-video stream to the session
* Subscribe to another client's audio-video stream
* Record the session, stop the recording, and view the recording
* Implement text chat
* Use a simple custom audio driver for audio input and output
* Use a custom video renderer
* Use a simple custom video capturer
* Use a custom video capturer that uses the device camera
* Publish a screen-sharing stream
* Use video capturer to obtain still screen captures of the camera used by a publisher

For a full description and walk-through of this code, go to
https://tokbox.com/developer/tutorials/android/.

The code for this sample is found the following git branches:

* *basics* -- This branch contains a completed version of the [Basic Video Chat Tutorial](https://tokbox.com/developer/tutorials/android/basic-video-chat/).

* *archiving* -- This branch shows you how to record the session.

* *signaling.step-1* -- This branch shows you how to use the OpenTok signaling API.

* *signaling.step-2* -- This branch shows you how to implement text chat using the OpenTok
signaling API.

* *signaling.step-3* -- This branch adds some UI improvements for the text chat feature.

* *audio-driver.step-1* -- This branch shows you how to implement a custom audio driver that
  uses a simple audio capturer.

* *audio-driver.step-2* -- This branch shows you how to implement a custom audio driver that
  uses a simple audio renderer.

* *basic-renderer* -- This branch shows the basics of implementing a custom video renderer
  for an OpenTok subscriber.

* *basic-capturer* -- This branch shows the basics of implementing a custom video capturer
  for an OpenTok publisher.

* *camera-capturer* -- This branch shows you how to use a custom video capturer using
  the device camera as the video source.

* *screensharing* -- This branch shows you how to use the device's screen (instead of a
  camera) as the video source for a published stream.

## Development and Contributing

Interested in contributing? We :heart: pull requests! See the [Contribution](CONTRIBUTING.md) guidelines.

## Getting Help

We love to hear from you so if you have questions, comments or find a bug in the project, let us know! You can either:

- Open an issue on this repository
- See <https://support.tokbox.com/> for support options
- Tweet at us! We're [@VonageDev](https://twitter.com/VonageDev) on Twitter
- Or [join the Vonage Developer Community Slack](https://developer.nexmo.com/community/slack)

## Further Reading

- Check out the Developer Documentation at <https://tokbox.com/developer/>
