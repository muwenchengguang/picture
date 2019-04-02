/**
 * 
 * @author PengPeng
 * @date 2016-03-15
 * 
 */

package com.peng.jpegviewer.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageQueue {
    private static final ALog.Category TAG = ALog.Category.MessageQueue;
    private Handler mHandler = null;
    private HandlerThread mThread = null;
    private Object mLock = null;
    private static int sIncreaseIndex = 0;
    private static final int MaxIncreaseIndex = 8000;
    private int mThisIndex = 0;
    private final String mName;
    private List<MessageQueueRunnable> mCommands = null;
    private boolean mEnableDebug = true;

    private static MessageQueue sGLobalMessageQueue = null; // this message queue is for global use, and never release

    // CAUTION: don't release global instance
    public static MessageQueue getGlobalInstance() {
        if (sGLobalMessageQueue == null) {
            sGLobalMessageQueue = new MessageQueue("GLobalMessageThread");
        }
        return sGLobalMessageQueue;
    }

    public MessageQueue(String threadName) {
        mName = threadName;
        mLock = new Object();
        mThread = new HandlerThread(threadName);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        initThisIndex();
        mCommands = new ArrayList<MessageQueueRunnable>();
        ALog.i(TAG, "Thread([" + mThisIndex + "]) " + threadName + " constructed");
    }

    public MessageQueue(Handler handler, String handlerName) {
        mHandler = handler;
        mName = handlerName;
        mLock = new Object();
        initThisIndex();
        mCommands = new ArrayList<MessageQueueRunnable>();
        ALog.i(TAG, "Handler([" + mThisIndex + "]) " + mName + " attached");
    }

    public Thread thread() {
        if (mThread != null) return mThread;
        else return mHandler.getLooper().getThread();
    }

    private void initThisIndex() {
        mThisIndex = sIncreaseIndex++;
        if (sIncreaseIndex > MaxIncreaseIndex) {
            sIncreaseIndex = 0;
        }
    }

    public void disableDebug() {
        mEnableDebug = false;
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        release();
        super.finalize();
    }

    public void release() {
        if (mThread != null) {
            cancelAllMessage();
            ALog.i(TAG, "Thread([" + mThisIndex + "]) " + mThread.getName() + " destroyed");
            mThread.getLooper().quit();
        } else if (mHandler != null) {
            cancelAllMessage();
            ALog.i(TAG, "Handler([" + mThisIndex + "]) " + mName + " detached");
        } else {
            return;
        }
        synchronized (mLock) {
            mThread = null;
            mHandler = null;
        }
    }

    public boolean isIdle() {
        boolean processing = false;
        synchronized (mLock) {
            for (MessageQueueRunnable r : mCommands) {
                if (r.processing) {
                    processing = true;
                    break;
                }
            }
        }
        return !processing;
    }

    public int commandCount() {
        synchronized (mLock) {
            return mCommands.size();
        }
    }

    public void postMessage(Message msg, MessageCallback cb) {
        synchronized (mLock) {
            if (mHandler != null) {
                MessageQueueRunnable r = new MessageQueueRunnable(msg, cb);
                mCommands.add(r);
                mHandler.post(r);
            }
        }
    }

    public void postMessageDelayed(Message msg, MessageCallback cb, long delayMillis) {
        synchronized (mLock) {
            if (mHandler != null) {
                MessageQueueRunnable r = new MessageQueueRunnable(msg, cb);
                mCommands.add(r);
                mHandler.postDelayed(r, delayMillis);
            }
        }
    }

    private void cancelAllMessage() {
        synchronized (mLock) {
            if (mHandler != null) {
                for (MessageQueueRunnable r : mCommands) {
                    mHandler.removeCallbacks(r);
                }
                mCommands.clear();
            }
        }
    }

    public void cancelAllMessage(MessageCallback cb) {
        synchronized (mLock) {
            if (mHandler != null) {
                int count = mCommands.size();
                if (count > 0) {
                    for (int i = count - 1; i >= 0; i--) {
                        MessageQueueRunnable r = mCommands.get(i);
                        if (r.cb == cb) {
                            mHandler.removeCallbacks(r);
                            mCommands.remove(i);
                        }
                    }
                }
            }
        }
    }

    public void cancelMessage(int what, MessageCallback cb) {
        synchronized (mLock) {
            if (mHandler != null) {
                int count = mCommands.size();
                if (count > 0) {
                    for (int i = count - 1; i >= 0; i--) {
                        MessageQueueRunnable r = mCommands.get(i);
                        if (r.msg.what == what && r.cb == cb) {
                            mHandler.removeCallbacks(r);
                            mCommands.remove(i);
                        }
                    }
                }
            }
        }
    }

    class MessageQueueRunnable implements Runnable {
        Message msg = null;
        MessageCallback cb = null;
        boolean processing = false;
        MessageQueueRunnable(Message msg, MessageCallback cb) {
            this.msg = msg;
            this.cb = cb;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            processing = true;
            synchronized (mLock) {
                mCommands.remove(this);
                if (mEnableDebug && mCommands.size() > 0) {
                    String str = "Handler([" + mThisIndex + "]) " + mName + " has " + mCommands.size() + " in pending\n";
                    for (MessageQueueRunnable r : mCommands) {
                        str += r.cb.toString() + ":" + r.msg.what + "\n";
                    }
                    ALog.i(TAG, str);
                }
            }
            cb.onMessage(msg);
            processing = false;
        }

    }

    public interface MessageCallback {
        void onMessage(Message msg);
    }
}

