package com.peng.jpegviewer.utils.http;

import android.os.Message;

import com.peng.jpegviewer.utils.ALog;
import com.peng.jpegviewer.utils.MessageQueue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by richie on 4/10/18.
 */

public class HttpsClient implements MessageQueue.MessageCallback {
    private static final ALog.Category TAG = ALog.Category.HttpsClient;
    private static final int DefaultDownloadCache = 1024*10; // 10k
    public static final int STATUS_DOWNLOAD_ERROR = -1;
    public static final int STATUS_CONNECT_FAILED = -2;
    public static final int STATUS_DOWNLOAD_INPROGRESS = 1;
    public static final int STATUS_DOWNLOAD_COMPLETE = 2;
    public static final int STATUS_DOWNLOAD_CANCELED = 3;

    private final MessageQueue mWorkingQueue;
    private byte[] mCache = new byte[DefaultDownloadCache];
    private final String mUrl;
    private final Map<String, String> mParams = new HashMap<>();
    private final Map<String, String> mBodyParams = new HashMap<>();
    private final Listener mListener;
    private OutputStream mOutputStream = null;
    private int mDownloaded = 0;
    private int mTotalSize = 0;
    private URLConnection mConnection = null;

    private enum TaskId {
        StartDownload, RetriveContent, StopDownload
    }

    public enum Method {
        Get, Post
    }

    public HttpsClient(String url, Listener listener) {
        mUrl = url;
        mListener = listener;
        mWorkingQueue = new MessageQueue("HttpsClientThread");
    }

    public HttpsClient(String url, Listener listener, OutputStream os) {
        mUrl = url;
        mListener = listener;
        mOutputStream = os;
        mWorkingQueue = new MessageQueue("HttpsClientThread");
    }

    public void setRequestProperty(String key, String value) {
        mParams.put(key, value);
    }

    public void setRequestBodyParam(String key, String value) {
        mBodyParams.put(key, value);
    }

    public void startDownload(Method method) {
        Message msg = Message.obtain();
        msg.what = TaskId.StartDownload.ordinal();
        msg.obj = method;
        mWorkingQueue.postMessage(msg, this);
    }

    private void retriveContent(URLConnection conn) {
        Message msg = Message.obtain();
        msg.what = TaskId.RetriveContent.ordinal();
        msg.obj = conn;
        mWorkingQueue.postMessage(msg, this);
    }

    public void stopDownload() {
        Message msg = Message.obtain();
        msg.what = TaskId.StopDownload.ordinal();
        mWorkingQueue.postMessage(msg, this);
    }

    @Override
    public void onMessage(Message msg) {
        TaskId id = TaskId.values()[msg.what];
        switch (id) {
            case StartDownload:
                onStartDownload((Method) msg.obj);
                break;
            case RetriveContent:
                if (mOutputStream != null) {
                    onRetriveContent((URLConnection) msg.obj);
                } else {
                    onRetriveContentOneTime((URLConnection) msg.obj);
                }
                break;
            case StopDownload:
                onStopDownload();
                break;
        }
    }

    private SSLSocketFactory buildSSLFactory() {
        SSLContext sc = null;
        Exception exception = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            exception = e;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            exception = e;
        }
        if (exception != null) {
            ALog.e(TAG, "error:" + exception);
        }
        return null;
    }

    private URLConnection onConnectSetup(Method method) {
        Exception exception = null;
        String smethod = (method == Method.Get ? "GET" : "POST");
        try {
            HostnameVerifier verifier = new HostnameVerifier() {

                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    ALog.i(TAG, "verify " + s + ", " + sslSession);
                    return true;
                }
            };

            URL serverUrl = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
                httpsConn.setSSLSocketFactory(buildSSLFactory());
                httpsConn.setHostnameVerifier(verifier);
            }
            conn.setRequestMethod(smethod);

            for (Map.Entry<String, String> item :mParams.entrySet()) {
                conn.setRequestProperty(item.getKey(), item.getValue());
            }
            String bodyParams = "";
            for (Map.Entry<String, String> item :mBodyParams.entrySet()) {
                if (item.getKey() != null && item.getValue() != null) {
                    String bodyParam = item.getKey() + "=" + URLEncoder.encode(item.getValue(), "utf-8") + "&";
                    bodyParams += bodyParam;
                }
            }
            if (bodyParams.length() > 0) {
                bodyParams = bodyParams.substring(0, bodyParams.length() - 1);
            }
            if (bodyParams.length() > 0) {
                ALog.i(TAG, "bodyParams:" + bodyParams);
                conn.getOutputStream().write(bodyParams.getBytes());
                conn.getOutputStream().close();
            }
            conn.connect();
            ALog.i(TAG, "responce code:" + conn.getResponseCode() + ", " + conn.getResponseMessage());
            Map<String,List<String>> map = conn.getHeaderFields();
            for (Map.Entry<String,List<String>> entry : map.entrySet()) {
                String value = "";
                for (String i : entry.getValue()) {
                    value += i + "/";
                }
                ALog.i(TAG, entry.getKey() + "->" + value);
            }
            mTotalSize = conn.getContentLength();
            return conn;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            exception = e;
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;

        }
        if (exception != null) {
            ALog.e(TAG, "error:" + exception);
            if (mListener != null) {
                mListener.onContent(this, null, STATUS_CONNECT_FAILED, exception.toString(), null);
            }
        }
        return null;
    }

    private void onStartDownload(Method method) {
        URLConnection conn = onConnectSetup(method);
        if (conn == null) {
            mWorkingQueue.release();
            return;
        }
        mConnection = conn;

        if (mConnection instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection) mConnection;
            int respCode = 200;
            String errMsg = "";
            try {
                respCode = httpConn.getResponseCode();
                errMsg = "responce code:" + respCode;
            } catch (IOException e) {
                e.printStackTrace();
                errMsg = e.toString();
            }

            if (respCode != 200) {
                if (mListener != null) {
                    mListener.onContent(this, null, STATUS_CONNECT_FAILED, errMsg, null);
                }
                httpConn.disconnect();
                mWorkingQueue.release();
                return;
            }
        }

        retriveContent(conn);
    }

    private void onRetriveContent(URLConnection conn) {
        if (conn == null) {
            mWorkingQueue.release();
            return;
        }

        try {
            InputStream is = conn.getInputStream();
            if (mOutputStream == null) {
                mOutputStream = new ByteArrayOutputStream(DefaultDownloadCache);
            }

            int reads = is.read(mCache);
            if (reads < 0) {
                if (mListener != null) {
                    mListener.onContent(this, mOutputStream, STATUS_DOWNLOAD_COMPLETE, "" + mDownloaded, "" + mTotalSize);
                }
                mWorkingQueue.release();
                return;
            } else if (reads > 0) {
                mOutputStream.write(mCache, 0, reads);
                mDownloaded += reads;
                if (mListener != null) {
                    mListener.onContent(this, mOutputStream, STATUS_DOWNLOAD_INPROGRESS, "" + mDownloaded, "" + mTotalSize);
                }
            }
            retriveContent(conn);
            return;

        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onContent(this, mOutputStream, STATUS_DOWNLOAD_ERROR, e.toString(), null);
            }
        }
        mWorkingQueue.release();
    }

    private void onRetriveContentOneTime(URLConnection conn) {
        if (conn == null) {
            mWorkingQueue.release();
            return;
        }

        try {
            InputStream is = conn.getInputStream();
            if (mOutputStream == null) {
                mOutputStream = new ByteArrayOutputStream(DefaultDownloadCache);
            }

            int reads = is.read(mCache);
            while (reads > 0) {
                mOutputStream.write(mCache, 0, reads);
                mDownloaded += reads;
                reads = is.read(mCache);
            }
            if (mListener != null) {
                mListener.onContent(this, mOutputStream, STATUS_DOWNLOAD_COMPLETE, null, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onContent(this, mOutputStream, STATUS_DOWNLOAD_ERROR, e.toString(), null);
            }
        }
        mWorkingQueue.release();
    }

    private void onStopDownload() {
        if (mConnection == null) {
            mWorkingQueue.release();
            return;
        }
        if (mConnection instanceof HttpsURLConnection) {
            HttpURLConnection connection = (HttpURLConnection) mConnection;
            connection.disconnect();
        }
        if (mListener != null) {
            mListener.onContent(this, mOutputStream, STATUS_DOWNLOAD_CANCELED, "" + mDownloaded, "" + mTotalSize);
        }
        mWorkingQueue.release();
    }

    public interface Listener {
        void onContent(HttpsClient client, OutputStream os, int status, Object param1, Object param2);
    }

}
