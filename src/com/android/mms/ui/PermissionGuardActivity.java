/*
 * Copyright (c) 2016, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *    * Neither the name of The Linux Foundation nor the names of its
 *      contributors may be used to endorse or promote products derived
 *      from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.mms.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.util.Log;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.util.DraftCache;

public class PermissionGuardActivity extends Activity {

    private TextView mExit;
    private TextView mNext;
    private TextView mSettings;
    private long mRequestTime;
    private static long PERMISSION_DENY_THRESHOLD_MILLLIS = 250;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String PACKAGE_URI_PREFIX = "package:";
    public static final String ORIGINAL_INTENT = "original_intent";
    private Intent mOriginalIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (redirectIfNeeded()){
            return;
        }
        mOriginalIntent = (Intent) getIntent().getExtras().get(ORIGINAL_INTENT);
        setContentView(R.layout.permission_guard_activity);

        mExit = (TextView) findViewById(R.id.exit);
        mExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        mNext = (TextView) findViewById(R.id.next);
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                goRequestPermissions();
            }
        });
        mSettings = (TextView)findViewById(R.id.settings);
        mSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse(PACKAGE_URI_PREFIX + getPackageName()));
                startActivity(intent);
            }
        });
    }

    protected void goRequestPermissions() {
        mRequestTime = SystemClock.elapsedRealtime();
        requestPermissions(MessageUtils.getMissingBasicPermissions(), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[],
            final int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (MessageUtils.hasBasicPermissions()) {
                enter();
            } else {
                final long current = SystemClock.elapsedRealtime();
                if ((current - mRequestTime) < PERMISSION_DENY_THRESHOLD_MILLLIS) {
                    mNext.setVisibility(View.GONE);
                    mSettings.setVisibility(View.VISIBLE);
                    findViewById(R.id.enable_permission_settings).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (redirectIfNeeded()){
            return;
        }
    }

    private boolean redirectIfNeeded() {
        if (!MessageUtils.hasBasicPermissions()){
            return false;
        }
        enter();
        return true;
    }

    private void enter() {
        finish();
        startActivity(mOriginalIntent);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
