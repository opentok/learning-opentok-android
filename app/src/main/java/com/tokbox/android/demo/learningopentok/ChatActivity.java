package com.tokbox.android.demo.learningopentok;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;


public class ChatActivity extends ActionBarActivity implements WebServiceCoordinator.Listener,
        Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener,
        Session.ArchiveListener {

    private static final String LOG_TAG = ChatActivity.class.getSimpleName();

    private WebServiceCoordinator mWebServiceCoordinator;

    private String mApiKey;
    private String mSessionId;
    private String mToken;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private String mCurrentArchiveId;
    private String mPlayableArchiveId;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private ImageView mArchivingIndicatorView;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
        mArchivingIndicatorView = (ImageView)findViewById(R.id.archiving_indicator_view);

        // initialize WebServiceCoordinator and kick off request for necessary data
        mWebServiceCoordinator = new WebServiceCoordinator(this, this);
        mWebServiceCoordinator.fetchSessionConnectionData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_start_archive:
                startArchive();
                return true;
            case R.id.action_stop_archive:
                stopArchive();
                return true;
            case R.id.action_play_archive:
                playArchive();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeSession() {
        mSession = new Session(this, mApiKey, mSessionId);
        mSession.setSessionListener(this);
        mSession.setArchiveListener(this);
        mSession.connect(mToken);
    }

    private void initializePublisher() {
        mPublisher = new Publisher(this);
        mPublisher.setPublisherListener(this);
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView(), 0);
    }

    private void logOpenTokError(OpentokError opentokError) {
        Log.e(LOG_TAG, "Error Domain: " + opentokError.getErrorDomain().name());
        Log.e(LOG_TAG, "Error Code: " + opentokError.getErrorCode().name());
    }

    private void startArchive() {
        mWebServiceCoordinator.startArchive(mSessionId);
        setStartArchiveEnabled(false);

    }

    private void stopArchive() {
        mWebServiceCoordinator.stopArchive(mCurrentArchiveId);
        setStopArchiveEnabled(false);
    }

    private void playArchive() {
        Uri playArchiveUri = mWebServiceCoordinator.archivePlaybackUri(mPlayableArchiveId);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, playArchiveUri);
        startActivity(browserIntent);
    }

    /* Web Service Coordinator delegate methods */

    @Override
    public void onSessionConnectionDataReady(String apiKey, String sessionId, String token) {
        mApiKey = apiKey;
        mSessionId = sessionId;
        mToken = token;

        initializeSession();
        initializePublisher();
    }

    @Override
    public void onWebServiceCoordinatorError(Exception error) {
        Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
    }

    /* Session Listener methods */

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        if (mPublisher != null) {
            mSession.publish(mPublisher);
        }

        setStartArchiveEnabled(true);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");

        setStartArchiveEnabled(false);
        setStopArchiveEnabled(false);
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber(this, stream);
            mSubscriber.setSubscriberListener(this);
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSession.subscribe(mSubscriber);
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    /* Publisher Listener methods */

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher Stream Created");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher Stream Destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    /* Subscriber Listener methods */

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.i(LOG_TAG, "Subscriber Connected");

        mSubscriberViewContainer.addView(mSubscriber.getView());
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        Log.i(LOG_TAG, "Subscriber Disconnected");
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        logOpenTokError(opentokError);
    }

    /* Archive Listener methods */

    @Override
    public void onArchiveStarted(Session session, String archiveId, String archiveName) {
        mCurrentArchiveId = archiveId;
        setStopArchiveEnabled(true);
        mArchivingIndicatorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onArchiveStopped(Session session, String archiveId) {
        mPlayableArchiveId = archiveId;
        mCurrentArchiveId = null;
        setPlayArchiveEnabled(true);
        setStartArchiveEnabled(true);
        mArchivingIndicatorView.setVisibility(View.INVISIBLE);
    }

    /* Options menu helpers */

    private void setStartArchiveEnabled(boolean enabled) {
        mMenu.findItem(R.id.action_start_archive)
                .setEnabled(enabled)
                .setVisible(enabled);
    }

    private void setStopArchiveEnabled(boolean enabled) {
        mMenu.findItem(R.id.action_stop_archive)
                .setEnabled(enabled)
                .setVisible(enabled);
    }

    private void setPlayArchiveEnabled(boolean enabled) {
        mMenu.findItem(R.id.action_play_archive)
                .setEnabled(enabled)
                .setVisible(enabled);
    }
}
