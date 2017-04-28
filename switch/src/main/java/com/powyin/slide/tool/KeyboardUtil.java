/*
 * Copyright (C) 2015-2017 Jacksgong(blog.dreamtobe.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.powyin.slide.tool;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;


public class KeyboardUtil {

    private static int MIN_KEYBOARD_HEI = 0;
    private static int LAST_SAVE_KEYBOARD_HEIGHT = 0;
    private static boolean INIT = false;
    private static int STATUS_BAR_HEIGHT = 50;
    private final static String STATUS_BAR_DEF_PACKAGE = "android";
    private final static String STATUS_BAR_DEF_TYPE = "dimen";
    private final static String STATUS_BAR_NAME = "status_bar_height";
    private final static String FILE_NAME = "keyboard.common";
    private final static String KEY_KEYBOARD_HEIGHT = "sp.key.keyboard.height";

    public interface IPanelHeightTarget {
        void refreshHeight(final int panelHeight);

        int getHeight();

        void onKeyboardShowing(boolean showing);
    }


    // 显示键盘
    public static void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) view.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, 0);
    }

    // 隐藏键盘
    public static void hideKeyboard(final View view) {
        InputMethodManager imm =
                (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // 得到键盘高度
    public static int getKeyboardHeight(final Context context) {
        if (LAST_SAVE_KEYBOARD_HEIGHT == 0) {
            LAST_SAVE_KEYBOARD_HEIGHT = get(context, getMinKeyBoardHeight(context));
        }
        return LAST_SAVE_KEYBOARD_HEIGHT;
    }

    // 得到状态栏高度
    public static int getStatusBarHeight( Context context) {
        if (!INIT) {
            int resourceId = context.getResources().
                    getIdentifier(STATUS_BAR_NAME, STATUS_BAR_DEF_TYPE, STATUS_BAR_DEF_PACKAGE);
            if (resourceId > 0) {
                STATUS_BAR_HEIGHT = context.getResources().getDimensionPixelSize(resourceId);
                INIT = true;
            }
        }
        return STATUS_BAR_HEIGHT;
    }

    // 注册键盘高度监听器
    public static ViewTreeObserver.OnGlobalLayoutListener attach(final Activity activity, IPanelHeightTarget target) {
        final ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new KeyboardStatusListener(contentView, target);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        return globalLayoutListener;
    }

    // 移除键盘高度监听器
    public static void detach(Activity activity, ViewTreeObserver.OnGlobalLayoutListener l) {
        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (Build.VERSION.SDK_INT >= 19) {
            contentView.getViewTreeObserver().removeOnGlobalLayoutListener(l);
        } else {
            contentView.getViewTreeObserver().removeGlobalOnLayoutListener(l);
        }
    }

    private static class KeyboardStatusListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private int previousDisplayHeight = 0;
        private final ViewGroup contentView;
        private final IPanelHeightTarget panelHeightTarget;
        private final int statusBarHeight;
        private boolean lastKeyboardShowing;
        private int maxOverlayLayoutHeight;
        private View mTopView = null;
        private Rect r = new Rect();

        KeyboardStatusListener(
                ViewGroup contentView, IPanelHeightTarget panelHeightTarget) {
            this.contentView = contentView;
            this.panelHeightTarget = panelHeightTarget;
            this.statusBarHeight = getStatusBarHeight(contentView.getContext());
            mTopView = (View) contentView.getParent().getParent().getParent();
        }


        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        @Override
        public void onGlobalLayout() {
            contentView.getWindowVisibleDisplayFrame(r);
            int displayHeight = r.top == 0 ? r.bottom - r.top : r.bottom - r.top + statusBarHeight;
            calculateKeyboardHeight(displayHeight);
            calculateKeyboardShowing(displayHeight);
            previousDisplayHeight = displayHeight;
        }

        private void calculateKeyboardHeight(final int displayHeight) {
            // first result.
            if (previousDisplayHeight == 0) {
                previousDisplayHeight = displayHeight;
                panelHeightTarget.refreshHeight(getKeyboardHeight(getContext()));
                return;
            }

            int keyboardHeight = Math.abs(mTopView.getHeight() - displayHeight);

            // no change.
            if (keyboardHeight <= getMinKeyBoardHeight(getContext())) {
                return;
            }

            // save the keyboardHeight
            boolean changed = KeyboardUtil.saveKeyboardHeight(getContext(), keyboardHeight);

            if (changed) {
                int validPanelHeight = getKeyboardHeight(getContext());
                if (this.panelHeightTarget.getHeight() != validPanelHeight) {
                    this.panelHeightTarget.refreshHeight(validPanelHeight);
                }
            }
        }


        private void calculateKeyboardShowing(final int displayHeight) {
            boolean currentKeyboardShow;

            final int overLayoutHeight = mTopView.getHeight();
            if (maxOverlayLayoutHeight == 0) {
                currentKeyboardShow = lastKeyboardShowing;
            } else {
                currentKeyboardShow = displayHeight < maxOverlayLayoutHeight - getMinKeyBoardHeight(getContext());
            }
            maxOverlayLayoutHeight = Math.max(maxOverlayLayoutHeight, overLayoutHeight);
            if (lastKeyboardShowing != currentKeyboardShow) {
                this.panelHeightTarget.onKeyboardShowing(currentKeyboardShow);
            }
            lastKeyboardShowing = currentKeyboardShow;
        }

        private Context getContext() {
            return contentView.getContext();
        }
    }



    // 得到最低键盘高度
    private static int getMinKeyBoardHeight(final Context context) {
        if (MIN_KEYBOARD_HEI == 0) {
            final float scale =  context.getResources().getDisplayMetrics().density;
            MIN_KEYBOARD_HEI= (int) (100 * scale + 0.5f);
        }
        return MIN_KEYBOARD_HEI;
    }

    // 保存键盘高度
    private static boolean saveKeyboardHeight(final Context context, int keyboardHeight) {
        if (LAST_SAVE_KEYBOARD_HEIGHT == keyboardHeight) {
            return false;
        }
        LAST_SAVE_KEYBOARD_HEIGHT = keyboardHeight;
        return save(context, LAST_SAVE_KEYBOARD_HEIGHT);
    }


    // 写入键盘高度 disk
    private static boolean save(final Context context, final int keyboardHeight) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.edit().putInt(KEY_KEYBOARD_HEIGHT, keyboardHeight).commit();
    }

    // 读取键盘高度 disk
    private static int get(final Context context, final int defaultHeight) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.getInt(KEY_KEYBOARD_HEIGHT, defaultHeight);
    }




}