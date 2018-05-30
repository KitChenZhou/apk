package com.ckt.testauxiliarytool.webviewtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.InflateException;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ckt.testauxiliarytool.BaseActivity;
import com.ckt.testauxiliarytool.R;
import com.ckt.testauxiliarytool.utils.DateTimeUtils;
import com.ckt.testauxiliarytool.utils.ExcelUtils;
import com.ckt.testauxiliarytool.utils.SystemUtils;
import com.ckt.testauxiliarytool.utils.URLUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jxl.write.WriteException;
public class WebViewTest extends BaseActivity {
    private static final String TAG = "WebViewTest";
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private WebView mWebview;
    private WebSettings mWebSettings;
    private static final int MSG = 110;
    private static final int TIMEUNIT = 1000;
    private static final int POSTDELAY = 5000;
    private Timer mTimer;
    private EditText url, _count, inter, delay, on;
    private Button start, excel;
    private int runcount = 1, succCount, failCount;
    private TextView tv_on, tv_off, tv_conclusion;
    private long begin, end;
    //定义一个简单的下拉列表
    private Spinner mSpinner;
    //选择是http还是https
    private String preHttp;
    //定义一个进度条
    private ProgressDialog mProgressDialog, progressDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000:
                    isDelay = "网络超时";
                    Log.d(TAG, "handleMessage: 开始设置了-------");
                    break;
            }
        }
    };
    private int MAX_PROGRESS;
    //定义输出Excel的字段,由于是Excel故这里全部设置为String类型
    private String id, beginTime, endTime, loadTime, isSuccess, isDelay = "", descrip = "";
    private List<ExcelBean> list;
    //判断是否连接成功
    private boolean isError = false;
    /**
     * 控制开始显示按钮的handler
     */
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    start.setEnabled(false);
                    break;
                case 1:
                    initProgressDialog(WebViewTest.this);
                    start.setEnabled(true);
                    excel.setEnabled(true);
                    String onresult = "打开成功：<font color='green'>%d</font> 次";
                    onresult = String.format(onresult, succCount);
                    tv_on.setText(Html.fromHtml(onresult));
                    String failresult = "失败了：<font color='green'>%d</font> 次";
                    failresult = String.format(failresult, failCount);
                    tv_off.setText(Html.fromHtml(failresult));
                    String conresult = "测试成功率：";
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    // 设置精确到小数点后2位
                    numberFormat.setMaximumFractionDigits(2);
                    String result = conresult + numberFormat.format((float) succCount / (float) (succCount + failCount) * 100);
                    SystemUtils.hideSoftKeyBorad(WebViewTest.this);
                    tv_conclusion.setText(Html.fromHtml(result) + "%");
                    succCount = 0;
                    failCount = 0;
                    runcount = 1;
//                    isDelay = "";
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.web_test);
        } catch (InflateException e) {
            e.printStackTrace();
            hookWebView();//捕获设置布局时的异常
            setContentView(R.layout.web_test);
        }

        verifyStoragePermissions(this);
        url = (EditText) findViewById(R.id.et_url);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        _count = (EditText) findViewById(R.id.et_count);
        inter = (EditText) findViewById(R.id.et_interval);
        delay = (EditText) findViewById(R.id.et_delay);
        tv_on = (TextView) findViewById(R.id.tv_on);
        tv_off = (TextView) findViewById(R.id.tv_off);
        tv_conclusion = (TextView) findViewById(R.id.tv_conclusion);
        on = (EditText) findViewById(R.id.et_on);
        start = (Button) findViewById(R.id.start);
        excel = (Button) findViewById(R.id.excel);
        // 设置下拉框的监听事件
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] http = getResources().getStringArray(R.array.http);
                preHttp = http[i];//获取选择的位置
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                preHttp = "https://";//默认为https
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SystemUtils.isNetworkAvailable(WebViewTest.this)) {
                    Toast.makeText(WebViewTest.this, R.string.nonet, Toast.LENGTH_SHORT).show();
                } else if (SystemUtils.isFastClick()) {
                    synchronized (this) {
                        {//将变量初始化
                            runcount = 1;
                            succCount = 0;
                            failCount = 0;
                            isSuccess = "";
                            isDelay = "";
                            descrip = "";
                            isError = false;
                        }
                    }
                    list = new ArrayList<>();
                    SystemUtils.hideSoftKeyBorad(WebViewTest.this);
                    String __url = url.getText().toString().trim();
                    final String _url = preHttp + __url;
                    if (!URLUtils.isUrl(_url)) {
                        Toast.makeText(WebViewTest.this, R.string.webwarning, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String __count = _count.getText().toString().trim();
                    String _inter = inter.getText().toString().trim();
                    String _delay = delay.getText().toString().trim();
                    String _on = on.getText().toString().trim();
                    if (__count.equals("") || _inter.equals("") || __url.equals("") || _delay.equals("") || _on.equals("")) {
                        Toast.makeText(WebViewTest.this, R.string.notnull, Toast.LENGTH_SHORT).show();
                    } else {
                        int mcount = Integer.parseInt(__count);
                        int mon = Integer.parseInt(_on);
                        int minter = Integer.parseInt(_inter);
                        int mdelay = Integer.parseInt(_delay);
                        webViewStart(_url, mcount, mon, minter, mdelay);
                        showDialogProgress(WebViewTest.this, mcount);
                    }
                }
            }
        });
        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vew) {
                if (SystemUtils.isFastClick()) {
                    //将list去重复
                    for (int i = 0; i < list.size() - 1; i++) {
                        for (int j = list.size() - 1; j > i; j--) {
                            if (list.get(j).getId().equals(list.get(i).getId())) {
                                list.remove(j);
                            }
                        }
                    }
                    try {
                        if (ExcelUtils.exportExcel(list, WebViewTest.this))
                            excel.setEnabled(false);
                        else
                            Toast.makeText(WebViewTest.this, "Fail to export Excel", Toast.LENGTH_SHORT).show();
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void webViewStart(final String inupturl, final int mcount, final int on, final int inter, final int mdelay) {
        mWebview = new WebView(this);
        //不使用缓存：
        mWebSettings = mWebview.getSettings();
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebSettings.setJavaScriptEnabled(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebview.loadUrl(inupturl);
            }
        });
        //设置不用系统浏览器打开,直接显示在当前Webview
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebview.loadUrl(url);
                return true;
            }
        });
        //设置WebChromeClient类
        mWebview.setWebChromeClient(new WebChromeClient() {
            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            }
        });
        //设置WebViewClient类
        mWebview.setWebViewClient(new WebViewClient() {
            //设置加载前的函数
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                begin = System.currentTimeMillis(); //记录开始时间
                beginTime = DateTimeUtils.detailFormat(new Date());
                isError = false;//置是否错误为假
                super.onPageStarted(view, url, favicon);
                isDelay = "";//置是否延时为空
                mTimer = new Timer();
                final TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mWebview == null) {
                                    mTimer.cancel();
                                    mTimer.purge();
                                } else if ((mWebview != null) && mWebview.getProgress() < 100) {
                                    Message msg = new Message();
                                    msg.what = 1000;
                                    mHandler.sendMessage(msg);
                                    mTimer.cancel();
                                    mTimer.purge();
                                }
                            }
                        });
                    }
                };
                mTimer.schedule(timerTask, mdelay * TIMEUNIT);//这里使用一个定时线程来判断是否超时
            }

            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
                end = (System.currentTimeMillis() - begin);//记录总用时
                NumberFormat numberFormat = NumberFormat.getInstance();
                // 设置精确到小数点后3位
                numberFormat.setMaximumFractionDigits(3);
                loadTime = numberFormat.format((float) end / 1000);
                Log.d(TAG, "onPageFinished: ----《》《》《》》》》" + loadTime);
                Long.parseLong(String.valueOf(mdelay));
                endTime = DateTimeUtils.detailFormat(new Date());
                mTimer.cancel();
                mTimer.purge();
                if (!isError) {
                    synchronized (this) {
                        succCount++;
                        isSuccess = "成功";
                    }
                }
                mProgressDialog.setProgress(runcount);
                try {
                    Thread.sleep(on * TIMEUNIT);
                    if (mWebview != null)
                        mWebview.clearHistory();//清除历史记录
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                id = String.valueOf(runcount);
                ExcelBean mExcelBean = new ExcelBean(id, beginTime, endTime, isSuccess, loadTime, isDelay, descrip);
                if (list != null)
                    list.add(mExcelBean);
                else {
                    Toast.makeText(WebViewTest.this, "Error in APP", Toast.LENGTH_SHORT).show();
                }
                synchronized (this) {//加个同步锁
                    runcount++;
                    isSuccess = "";
                    isDelay = "";
                    descrip = "";
                }
                try {
                    Thread.sleep(inter * TIMEUNIT);
                    Log.d(TAG, "webViewStart: " + inupturl);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mWebview != null)
                                mWebview.loadUrl(inupturl);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (runcount > mcount) {
                    if (mWebview != null) {
                        mWebview.stopLoading();
                        mWebview.clearHistory();
                        mWebview = null;
                    }
                    myHandler.sendEmptyMessage(1);
                    SystemUtils.hideSoftKeyBorad(WebViewTest.this);
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String
                    failingUrl) {
                synchronized (this) {
                    isError = true;
                    isSuccess = "失败";
//                isDelay = "";
                    descrip = description;
                    failCount++;
                }
                Log.d(TAG, "onReceivedError: " + failCount + "--------<<<<<<<" + description);
            }
        });
    }

    /**
     * 显示对话框进度条
     *
     * @param context 上下文
     * @param max     设置最大值
     */

    private void showDialogProgress(final Context context, int max) {
        mProgressDialog = new ProgressDialog(context);
        MAX_PROGRESS = max;
        mProgressDialog.setTitle(R.string.urldialogtitle);
        mProgressDialog.setMessage("Waiting .......");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置进度条对话框样式（水平，旋转）
        //进度最大值
        mProgressDialog.setMax(MAX_PROGRESS);
        final boolean[] flag = {true};
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "停止本次测试",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (SystemUtils.isFastClick() && flag[0]) {
                            // TODO Auto-generated method stub
                            if (mWebview != null) {
                                mWebview.stopLoading();
                                mWebview.clearHistory();
                                mWebview = null;
                            }
                            myHandler.sendEmptyMessage(1);
                            SystemUtils.hideSoftKeyBorad(WebViewTest.this);
                            //删除消息队列
                            DialogHandler.removeMessages(MSG);
                            mProgressDialog.dismiss();
                        }
                        flag[0] = false;
                    }
                });
        //显示
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);//点击返回键对话框不消失
        //必须设置点击空白处不消失
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private Handler DialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG:
                    if (runcount >= MAX_PROGRESS) {
                        //重新设置
                        mProgressDialog.dismiss();//销毁对话框
                    } else {
                        mProgressDialog.incrementProgressBy(runcount);
                        //延迟发送消息
                        DialogHandler.sendEmptyMessageDelayed(MSG, 100);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 30);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume is run ......");
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy is run ......");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        super.onDestroy();
        if (mWebview != null) {
            Log.d(TAG,"-----"+mWebview);
            // mWebview.removeAllViews();
            mWebview.destroy();
        }
    }

    private void initProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在生成报告请等待！");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, POSTDELAY);
    }
    /**
     * 让系统应用也能使用WebView组件
     */
    private void hookWebView() {
        Class<?> factoryClass = null;
        try {
            factoryClass = Class.forName("android.webkit.WebViewFactory");
            Method getProviderClassMethod = null;
            Object sProviderInstance = null;
            if (Build.VERSION.SDK_INT >= 23) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                if (constructor != null) {
                    constructor.setAccessible(true);
                    Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                    constructor2.setAccessible(true);
                    sProviderInstance = constructor.newInstance(constructor2.newInstance());
                }
            } else if (Build.VERSION.SDK_INT == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                if (constructor != null) {
                    constructor.setAccessible(true);
                    Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                    constructor2.setAccessible(true);
                    sProviderInstance = constructor.newInstance(constructor2.newInstance());
                }
            } else if (Build.VERSION.SDK_INT == 21) {//Android 21无WebView安全限制
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                getProviderClassMethod.setAccessible(true);
                Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                sProviderInstance = providerClass.newInstance();
            }
            if (sProviderInstance != null) {
                Field field = factoryClass.getDeclaredField("sProviderInstance");
                field.setAccessible(true);
                field.set("sProviderInstance", sProviderInstance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}