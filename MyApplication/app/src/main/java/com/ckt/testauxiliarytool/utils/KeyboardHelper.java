package com.ckt.testauxiliarytool.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.text.Editable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.tp.adapters.OnKeyboardActionListenerAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class KeyboardHelper {
    private Activity mActivity;
    private KeyboardView mKeyboardView;
    /**
     * 数字键盘
     */
    private Keyboard mNumberKeyboard;
    /**
     * 字母键盘
     */
    private Keyboard mLetterKeyboard;

    /**
     * 　是否数字键盘
     */
    private boolean isNumber = false;
    /**
     * 是否大写
     */
    private boolean isUpperCase = false;
    private EditText mEditText;
    /* 有第二个键按下时*/
    private boolean isKeyPressed = false;
    /* 上次按下的key*/
    private int lastKey = -Integer.MAX_VALUE;

    /* 按下的key的索引*/
    private int mPrimaryKey;
    /* 记录当前按下的key的code值*/
    private int mPrimaryCode;

    public KeyboardHelper(Context context, final Activity activity, final EditText editText) {

        mActivity = activity;
        mEditText = editText;
        editText.setCursorVisible(true);


        // 加载数字与字母键盘布局
        mNumberKeyboard = new Keyboard(context, R.xml.tp_keyboard_numbers);
        mLetterKeyboard = new Keyboard(context, R.xml.tp_keyboard_qwerty);
        mKeyboardView = (KeyboardView) mActivity.findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboard(mNumberKeyboard);  /* 设置要显示的面板 */
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(true); // 是否显示按钮反馈的弹出窗
        mKeyboardView.setOnKeyboardActionListener(actionListener);
        mKeyboardView.setOnTouchListener(new View.OnTouchListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                notifyChange(event);
                multiTapInput(event);

                return false;
            }
        });

    }

    /**
     * 处理多指点击的情况
     *
     * @param event
     */
    private void multiTapInput(final MotionEvent event) {
        // 处理多指触摸时预览
        if (event.getPointerCount() >= 2) {
            int x = (int) event.getX(1);
            int y = (int) event.getY(1);
            final int[] nearestKeys = getNearestKeys(x, y);
            if (nearestKeys.length > 0)
                mPrimaryKey = nearestKeys[0];   // 获取按下的key的索引

            // 判断当前的key是否是和原来是同一个
            if (lastKey == mPrimaryKey || nearestKeys.length <= 0)
                return;

            lastKey = mPrimaryKey;
            if (nearestKeys.length > 0) {  // 通过key的索引进行相关操作
                showKey(nearestKeys[0]);
                final int keyIndex = nearestKeys[0];
                final Keyboard.Key[] keys = list2Array(mKeyboardView.getKeyboard().getKeys());
//                        detectAndSendKey(keyIndex, x, y, event.getDownTime());
                UiUtil.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int code = keys[keyIndex].codes[0];
                        dealWithKeyEvent(code);
                        mKeyboardView.invalidateAllKeys();
                        isKeyPressed = true;
                    }
                }, 30);
            }
        } else {
            lastKey = Integer.MAX_VALUE;
        }
    }

    /**
     * 通知手指数，测距信息的改变
     *
     * @param event
     */
    private void notifyChange(MotionEvent event) {
        /* 触摸监听 */
        if (pointerListener != null || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (event == null) {
                pointerListener.onChange(null, null, 0);
                return;
            }

            int count = event.getPointerCount();
            PointF first = new PointF(event.getX(), event.getY());
            PointF second = null;
            if (event.getPointerCount() >= 2) {
                second = new PointF(event.getX(1), event.getY(1));
            }
            // 处理手指数为0的情况，当有手指抬起，并且此时指数为1时
            if (event.getAction() == MotionEvent.ACTION_UP && event.getPointerCount() == 1) {
                count = 0;
            }

            pointerListener.onChange(first, second, count);
        }
    }

    /**
     * 根据key code 获取 Keyboard.Key 对象
     *
     * @param primaryCode
     * @return
     */
    private Keyboard.Key getKeyByKeyCode(int primaryCode) {
        if (null != mKeyboardView.getKeyboard()) {
            List<Keyboard.Key> keyList = mKeyboardView.getKeyboard().getKeys();
            for (int i = 0, size = keyList.size(); i < size; i++) {
                Keyboard.Key key = keyList.get(i);

                int codes[] = key.codes;

                if (codes[0] == primaryCode) {
                    return key;
                }
            }
        }

        return null;
    }

    public static Keyboard.Key[] list2Array(List<Keyboard.Key> list) {

        Keyboard.Key[] array = new Keyboard.Key[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }


    //    detectAndSendKey 检测并发送key
    public void detectAndSendKey(int index, int x, int y, long eventTime) {
        Class<? extends KeyboardView> clazz = mKeyboardView.getClass();
        try {
            Method method = clazz.getDeclaredMethod("detectAndSendKey", int.class, int.class, int.class, long.class);
            method.setAccessible(true);
            method.invoke(mKeyboardView, index, x, y, eventTime);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

//    mPopupPreviewX;
//    private int mPopupPreviewY;

    public int[] getPopupPreviewCoordinates() {
        Class<? extends KeyboardView> clazz = mKeyboardView.getClass();
        try {
            Field fieldX = clazz.getDeclaredField("mPopupPreviewX");
            fieldX.setAccessible(true);
            Field fieldY = clazz.getDeclaredField("mPopupPreviewY");
            fieldY.setAccessible(true);
            return new int[]{fieldX.getInt(mKeyboardView), fieldY.getInt(mKeyboardView)};
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取最近的Keys
     *
     * @param x
     * @param y
     * @return
     */
    public int[] getNearestKeys(int x, int y) {
        List<Keyboard.Key> keys = mKeyboardView.getKeyboard().getKeys();
        Keyboard.Key[] mKeys = keys.toArray(new Keyboard.Key[keys.size()]);
        int i = 0;
        for (Keyboard.Key key : mKeys) {
            if (key.isInside(x, y))
                return new int[]{i};
            i++;
        }
        return new int[0];
    }


    /**
     * 显示key对应的popupWin
     *
     * @param keyIndex
     */
    private void showKey(int keyIndex) {
        Class<? extends KeyboardView> clazz = mKeyboardView.getClass();
        try {
            Method method = clazz.getDeclaredMethod("showKey", int.class);
            method.setAccessible(true);
            method.invoke(mKeyboardView, keyIndex);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 键盘动作监听器
     */
    private KeyboardView.OnKeyboardActionListener actionListener = new OnKeyboardActionListenerAdapter() {
        @Override
        public void onPress(int primaryCode) {
//            LogUtil.e(KeyboardHelper.class.getName(), "onPress=" + primaryCode);
        }

        @Override
        public void onRelease(int primaryCode) {
//            LogUtil.e(KeyboardHelper.class.getName(), "onRelease=" + primaryCode);
        }


        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            dealWithKeyEvent(primaryCode);
        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {   // 处理左滑动作的事件
            switch (mPrimaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    mEditText.setText("");   // 删除键左滑清空输入
                    break;
                case 57419: // 左移键
                    mEditText.setSelection(0);
                    break;
                case 57421: // 右移键
                    mEditText.setSelection(mEditText.length());
                    break;
            }

        }
    };

    /**
     * 处理按键事件
     *
     * @param primaryCode int
     */
    private void dealWithKeyEvent(int primaryCode) {
        if (isKeyPressed) {
            isKeyPressed = false;
            return;
        }
        mPrimaryCode = primaryCode;
        LogUtil.e(KeyboardHelper.class.getName(), "primaryCode=" + primaryCode);
        Editable editable = mEditText.getText();
        int start = mEditText.getSelectionStart();
        if (primaryCode == Keyboard.KEYCODE_CANCEL) { // cancel
            hideCustomKeyboard();
            notifyChange(null);
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) { // 回删
            if (editable != null && editable.length() > 0) {
                if (start > 0) {
                    editable.delete(start - 1, start);
                }
            }
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) { // 大小写切换
            changeKeyboardLetterCase();
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) { // 数字与字母键盘互换
            if (isNumber) {
                isNumber = false;
                mKeyboardView.setKeyboard(mLetterKeyboard);
            } else {
                isNumber = true;
                mKeyboardView.setKeyboard(mNumberKeyboard);
            }
        } else if (primaryCode == 57419) { // 左移
            if (start > 0) {
                mEditText.setSelection(start - 1);
            }
        } else if (primaryCode == 57421) { // 右移
            if (start < mEditText.length()) {
                mEditText.setSelection(start + 1);
            }
        } else { // 输入键盘值
            editable.insert(start, Character.toString((char) primaryCode));
        }


    }


    //绑定一个EditText
    public void registerEditText(EditText editText) {
        // Make the custom keyboard appear
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(v);
                } else {
                    hideCustomKeyboard();
                }
            }
        });
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(KeyboardHelper.class.getName(), "onClick");
                showCustomKeyboard(v);
            }
        });
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogUtil.d(KeyboardHelper.class.getName(), "onTouch");
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();    // Backup the input type
//                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);        // Call native handler
                edittext.setInputType(inType);       // Restore input type
                edittext.setSelection(edittext.getText().length());
                return true;
            }
        });
    }

    public void unregisterEditText(EditText editText) {
        editText.setOnFocusChangeListener(null);
        editText.setOnTouchListener(null);
        editText.setOnTouchListener(null);
    }


    /**
     * 切换键盘字母大小写
     */
    private void changeKeyboardLetterCase() {
        List<Keyboard.Key> keyList = mLetterKeyboard.getKeys();
        if (isUpperCase) { // 大写切换小写
            isUpperCase = false;
            for (Keyboard.Key key : keyList) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;  /* 转大写*/
                }
            }
        } else { // 小写切换成大写
            isUpperCase = true;
            for (Keyboard.Key key : keyList) {
                /* lable定义在xml文件中，用来显示按键内容*/
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32; /* 转小写*/
                }
            }
        }
        mKeyboardView.setKeyboard(mLetterKeyboard);
    }

    /**
     * 判断是否是字母
     */
    private boolean isLetter(String str) {
        String wordStr = "abcdefghijklmnopqrstuvwxyz";
        return wordStr.contains(str.toLowerCase());
    }

    /**
     * 隐藏键盘
     */
    public void hideCustomKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        if (visibility == View.VISIBLE) {
            mKeyboardView.setVisibility(View.INVISIBLE);
            mKeyboardView.setEnabled(false);
        }
    }

    /**
     * 显示键盘
     */
    public void showCustomKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            mKeyboardView.setVisibility(View.VISIBLE);
            mKeyboardView.setEnabled(true);
        }
    }

    private void showCustomKeyboard(View v) {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (v != null) {
            ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public boolean isCustomKeyboardVisible() {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    /**
     * 手指状态改变监听
     */
    public interface OnPointerStateChangeListener {
        void onChange(PointF first, PointF second, int pointerCount);
    }

    private OnPointerStateChangeListener pointerListener;

    public void setOnPointerStateChangeListener(OnPointerStateChangeListener pointerListener) {
        this.pointerListener = pointerListener;
    }
}
