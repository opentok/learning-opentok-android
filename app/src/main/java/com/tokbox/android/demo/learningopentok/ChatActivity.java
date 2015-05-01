package com.tokbox.android.demo.learningopentok;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Connection;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;


public class ChatActivity extends ActionBarActivity implements WebServiceCoordinator.Listener,
        Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener,
        View.OnClickListener, Session.SignalListener {

    private static final String LOG_TAG = ChatActivity.class.getSimpleName();
    public static final String SIGNAL_TYPE_CHAT = "chat";

    private WebServiceCoordinator mWebServiceCoordinator;

    private String mApiKey;
    private String mSessionId;
    private String mToken;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ChatMessageAdapter mMessageHistory;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private Button mSendButton;
    private EditText mMessageEditText;
    private ListView mMessageHistoryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
        mSendButton = (Button)findViewById(R.id.send_button);
        mMessageEditText = (EditText)findViewById(R.id.message_edit_text);
        mMessageHistoryListView = (ListView)findViewById(R.id.message_history_list_view);

        // Attach data source to message history
        mMessageHistory = new ChatMessageAdapter(this);
        mMessageHistoryListView.setAdapter(mMessageHistory);

        // Attach handlers to UI
        mSendButton.setOnClickListener(this);

        // initialize WebServiceCoordinator and kick off request for necessary data
        mWebServiceCoordinator = new WebServiceCoordinator(this, this);
        mWebServiceCoordinator.fetchSessionConnectionData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeSession() {
        mSession = new Session(this, mApiKey, mSessionId);
        mSession.setSessionListener(this);
        mSession.setSignalListener(this);
        mSession.connect(mToken);
    }

    private void initializePublisher() {
        mPublisher = new Publisher(this);
        mPublisher.setPublisherListener(this);
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView());
    }

    private void sendMessage() {
        disableMessageViews();

        ChatMessage message = new ChatMessage(mMessageEditText.getText().toString());
        mSession.sendSignal(SIGNAL_TYPE_CHAT, message.toString());

        mMessageEditText.setText("");
        enableMessageViews();
    }

    private void disableMessageViews() {
        mMessageEditText.setEnabled(false);
        mSendButton.setEnabled(false);
    }

    private void enableMessageViews() {
        mMessageEditText.setEnabled(true);
        mSendButton.setEnabled(true);
    }

    private void showMessage(String messageData, boolean remote) {
        ChatMessage message = ChatMessage.fromData(messageData);
        if (message != null) {
            message.setRemote(remote);
            mMessageHistory.add(message);
        }
    }

    private void logOpenTokError(OpentokError opentokError) {
        Log.e(LOG_TAG, "Error Domain: " + opentokError.getErrorDomain().name());
        Log.e(LOG_TAG, "Error Code: " + opentokError.getErrorCode().name());
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

        enableMessageViews();
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");

        disableMessageViews();
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

    /* OnClick Listener methods */

    @Override
    public void onClick(View v) {
        if (v.equals(mSendButton)) {
            sendMessage();
        }
    }

    /* Signal Listener methods */

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        boolean remote = !connection.equals(mSession.getConnection());
        switch (type) {
            case SIGNAL_TYPE_CHAT:
                showMessage(data, remote);
                break;
        }
    }
}
