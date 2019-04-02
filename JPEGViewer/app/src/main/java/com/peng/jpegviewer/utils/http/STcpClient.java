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

public class STcpClient implements MessageQueue.MessageCallback {
    private static final ALog.Category TAG = ALog.Category.HttpsClient;
    private static final int DefaultDownloadCache = 1024*10; // 10k
    public static final int STATUS_OK = 0;
    public static final int STATUS_CONNECT_FAILED = -2;
    public static final int STATUS_READ_FAILED = -3;

    private final MessageQueue mWorkingQueue;
    private byte[] mCache = new byte[DefaultDownloadCache];
    private final String mUrl;
    private final Listener mListener;
    private OutputStream mOutputStream = null;
    private URLConnection mConnection = null;
    private Status mStatus = Status.Disconnected;

    private enum TaskId {
        Connect, Disconnect, Send, RetriveContent, StopDownload
    }

    public enum Status {
        Disconnected, Connecting, Connected
    }

    public STcpClient(String url, Listener listener) {
        mUrl = url;
        mListener = listener;
        mWorkingQueue = new MessageQueue("STcpClientThread");
    }

    public void connect() {
        Message msg = Message.obtain();
        msg.what = TaskId.Connect.ordinal();
        mWorkingQueue.postMessage(msg, this);
    }

    public void disconnect() {
        Message msg = Message.obtain();
        msg.what = TaskId.Disconnect.ordinal();
        mWorkingQueue.postMessage(msg, this);
    }

    public void send(String content) {
        Message msg = Message.obtain();
        msg.what = TaskId.Send.ordinal();
        msg.obj = content;
        mWorkingQueue.postMessage(msg, this);
    }

    private void postRetriveContent(URLConnection conn) {
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
            case Connect:
                onConnect();
                break;
            case Disconnect:
                onDisconnect();
                break;
            case Send:
                onSend((String) msg.obj);
                break;
            case RetriveContent:
                onRetriveContent((URLConnection) msg.obj);
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

    private URLConnection onConnectSetup() {
        Exception exception = null;
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

    private void onConnect() {
        if (mStatus != Status.Disconnected) return;
        mStatus = Status.Connecting;
        URLConnection conn = onConnectSetup();
        if (conn == null) {
            mWorkingQueue.release();
            if (mListener != null) {
                mListener.onContent(this, null, STATUS_CONNECT_FAILED, "establish connection failed", null);
            }
            mStatus = Status.Disconnected;
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
                mStatus = Status.Disconnected;
                return;
            }
        }
        mStatus = Status.Connected;
    }

    private void onDisconnect() {
        if (mStatus == Status.Disconnected) return;
        if (mConnection instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection) mConnection;
            httpConn.disconnect();
        }
        mStatus = Status.Disconnected;
    }

    private void onSend(String content) {
        if (mStatus != Status.Connected) {
            ALog.e(TAG, "error: status is not connected");
            return;
        }
        try {
            mConnection.getOutputStream().write(content.getBytes());
            postRetriveContent(mConnection);
        } catch (IOException e) {
            e.printStackTrace();
            ALog.e(TAG, "error: write error:" + e);
        }
    }

    private void onRetriveContent(URLConnection conn) {
        if (mStatus != Status.Connected) {
            ALog.e(TAG, "error: status is not connected");
            return;
        }
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
                reads = is.read(mCache);
            }
            if (mListener != null) {
                mListener.onContent(this, mOutputStream, STATUS_OK, null, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onContent(this, mOutputStream, STATUS_READ_FAILED, e.toString(), null);
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
        mWorkingQueue.release();
    }

    public interface Listener {
        void onContent(STcpClient client, OutputStream os, int status, Object param1, Object param2);
    }

}
