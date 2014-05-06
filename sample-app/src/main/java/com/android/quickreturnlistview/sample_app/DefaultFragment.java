/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.quickreturnlistview.sample_app;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.quickreturnlistview.library.QuickReturnListView;

public class DefaultFragment extends ListFragment {
    private QuickReturnListView mListView;
    private View mQuickReturnView;

    public static DefaultFragment newInstance(boolean animated) {
        DefaultFragment fragment = new DefaultFragment();

        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }

        args.putBoolean("animated", animated);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, null);
        mQuickReturnView = view.findViewById(R.id.sticky);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((TextView) mQuickReturnView.findViewById(R.id.stickyTV)).setText("Default");

        mListView = (QuickReturnListView) getListView();
        Bundle args = getArguments();
        if (args != null) {
            boolean animated = args.getBoolean("animated");
            mListView.setAnimatedReturn(animated);
        }

        mListView.setQuickReturnView(mQuickReturnView);

        String[] array = new String[]{"Android 1", "Android 2", "Android 3",
                "Android 4", "Android 5", "Android 6", "Android 7", "Android 8",
                "Android 9", "Android 10", "Android 11", "Android 12", "Android 13",
                "Android 14", "Android 15", "Android 16"};

        setListAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.list_item, R.id.text1, array));
    }
}