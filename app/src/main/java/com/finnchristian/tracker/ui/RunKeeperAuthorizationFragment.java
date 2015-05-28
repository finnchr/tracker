package com.finnchristian.tracker.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.SettingsUtil;
import com.finnchristian.tracker.api.RunKeeper;
import com.finnchristian.tracker.model.runkeeper.Token;
import com.finnchristian.tracker.service.RunKeeperService;
import com.google.common.base.Strings;

import java.util.UUID;

import retrofit.RetrofitError;


public class RunKeeperAuthorizationFragment extends Fragment {
    private static final String ARG_TRACK_ID = "ARG_TRACK_ID";
    private static final String ARG_POST_ACTIVITY_WHEN_AUTHORIZED = "ARG_POST_ACTIVITY_WHEN_AUTHORIZED";

    private String previousActionBarTitle = null;
    private int trackId;
    private boolean postActivityWhenAuthorized;

    public static RunKeeperAuthorizationFragment newInstance(final int trackId, final boolean postActivityWhenAuthorized) {
        final Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        args.putBoolean(ARG_POST_ACTIVITY_WHEN_AUTHORIZED, postActivityWhenAuthorized);

        RunKeeperAuthorizationFragment fragment = new RunKeeperAuthorizationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public RunKeeperAuthorizationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID, -1);
            postActivityWhenAuthorized = getArguments().getBoolean(ARG_POST_ACTIVITY_WHEN_AUTHORIZED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_run_keeper_authorization, container, false);

        // generate random state value
        final String state = UUID.randomUUID().toString();

        final WebView webView = (WebView) view.findViewById(R.id.runkeeper_web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                final String redirectUri = getString(R.string.uri_runkeeper_redirect);

                if (url.startsWith(redirectUri)) {
                    final Uri uri = Uri.parse(url);
                    final String paramState = uri.getQueryParameter("state");

                    if (state.equals(paramState)) {
                        final String paramCode = uri.getQueryParameter("code");
                        final String paramError = uri.getQueryParameter("error");

                        if (!Strings.isNullOrEmpty(paramCode)) {
                            getAccessToken(paramCode);
                        } else if (!Strings.isNullOrEmpty(paramError) && "access_denied".equalsIgnoreCase(paramError)) {
                            // authorization denied by user
                            getFragmentManager().popBackStack();
                        }
                    }

                    // don't follow url
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.loadUrl(getAuthorizeUrl(state));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ActionBarActivity activity = (ActionBarActivity) getActivity();
        final ActionBar actionBar = activity.getSupportActionBar();
        previousActionBarTitle = actionBar.getTitle().toString();
        actionBar.setTitle(R.string.runkeeper_authorization_actionbar_title);
    }

    @Override
    public void onPause() {
        super.onPause();

        final ActionBarActivity activity = (ActionBarActivity) getActivity();
        final ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle(previousActionBarTitle);
    }

    private String getAuthorizeUrl(final String state) {
        return Uri.parse(getString(R.string.uri_runkeeper_authorize)).buildUpon()
                .appendQueryParameter("client_id", getString(R.string.runkeeper_client_id))
                .appendQueryParameter("state", state)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("redirect_uri", getString(R.string.uri_runkeeper_redirect))
                .build()
                .toString();
    }

    private void getAccessToken(final String code) {
        final String clientId = getString(R.string.runkeeper_client_id);
        final String clientSecret = getString(R.string.runkeeper_client_secret);
        final String grantType = getString(R.string.runkeeper_grant_type);
        final String redirectUri = getString(R.string.uri_runkeeper_redirect);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    final String runKeeperTokenUrl = getString(R.string.uri_runkeeper_token);
                    final RunKeeper.Service runKeeperApi = RunKeeper.createService(runKeeperTokenUrl);
                    final Token token = runKeeperApi.getToken(code, clientId, clientSecret, grantType, redirectUri);

                    new SettingsUtil(getActivity()).setRunKeeperToken(token);

                    return true;
                }
                catch (RetrofitError error) {
                    final String url = error.getResponse().getUrl();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if(success) {
                    if(postActivityWhenAuthorized) {
                        RunKeeperService.startActionPostFitnessActivity(getActivity(), trackId);
                    }

                    getFragmentManager().popBackStack();
                }
                else {
                    showAccessDeniedDialog();
                }
            }
        }.execute();
    }

    private void showAccessDeniedDialog() {
        new AlertDialog.Builder(RunKeeperAuthorizationFragment.this.getActivity())
                .setTitle(R.string.runkeeper_authorization_actionbar_title)
                .setMessage(R.string.runkeeper_authorization_dialog_access_denied)
                .setPositiveButton(R.string.runkeeper_authorization_dialog_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getFragmentManager().popBackStack();
                    }
                })
                .show();
    }
}
