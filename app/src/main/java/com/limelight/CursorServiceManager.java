package com.limelight;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.Gravity;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.limelight.binding.input.touch.RelativeTouchContext;
import com.limelight.binding.input.touch.TouchContext;
import com.limelight.preferences.PreferenceConfiguration;
import com.limelight.ui.CursorView;
import com.limelight.ui.StreamView;

import java.io.DataInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 光标服务管理器
 * 负责光标网络服务（接收服务端光标变化）、动画播放、本地光标渲染器管理。
 * 从 Game 类提取，减少 Game 的职责范围。
 */
public class CursorServiceManager {

    /**
     * UI 回调接口，用于与 Activity 交互
     */
    public interface UiCallback {
        /** 在 UI 线程上执行 */
        void runOnUi(Runnable runnable);
        /** Activity 是否仍然存活 */
        boolean isActivityAlive();
    }

    private static final int CURSOR_PORT = 5005;
    // 特殊控制包 Hash 标识 (0xFFFFFFFF 在 Java 的有符号 int 中即为 -1)
    private static final int MAGIC_CONTROL_HASH = -1;

    private final StreamView streamView;
    private final CursorView cursorOverlay;
    private final PreferenceConfiguration prefConfig;
    private final TouchContext[] relativeTouchContextMap;
    private final UiCallback uiCallback;

    // 网络线程相关
    private Thread cursorNetworkThread;
    private volatile boolean isCursorNetworking = false;
    private Socket cursorSocket;
    private String computerIpAddress;

    // 动画管理
    private Runnable currentAnimationTask = null;
    private final Handler animationHandler = new Handler(Looper.getMainLooper());

    // 光标缓存 (最大缓存 100 张光标)
    private final LruCache<Integer, Bitmap> cursorCache = new LruCache<>(100);

    // 文本光标状态记录 (数据感知与动作执行解耦)
    private float lastRemoteCursorYPercent = -1.0f; // 记录最近的光标高度百分比
    private boolean isLocalKeyboardActive = false; // 本地键盘/虚拟全键盘是否已打开

    public CursorServiceManager(StreamView streamView,
                                CursorView cursorOverlay,
                                PreferenceConfiguration prefConfig,
                                TouchContext[] relativeTouchContextMap,
                                UiCallback uiCallback) {
        this.streamView = streamView;
        this.cursorOverlay = cursorOverlay;
        this.prefConfig = prefConfig;
        this.relativeTouchContextMap = relativeTouchContextMap;
        this.uiCallback = uiCallback;
    }

    // ========== 本地光标渲染器管理 ==========

    /**
     * 初始化本地光标渲染器
     */
    public void initializeLocalCursorRenderers(int width, int height) {
        if (cursorOverlay == null) {
            return;
        }

        for (TouchContext context : relativeTouchContextMap) {
            if (context instanceof RelativeTouchContext) {
                RelativeTouchContext relativeContext = (RelativeTouchContext) context;
                relativeContext.initializeLocalCursorRenderer(cursorOverlay, width, height);
                boolean shouldShow = prefConfig.enableLocalCursorRendering
                        && prefConfig.touchscreenTrackpad
                        && !prefConfig.enableNativeMousePointer;
                relativeContext.setEnableLocalCursorRendering(shouldShow);
            }
        }
    }

    /**
     * 销毁本地光标渲染器
     */
    public void destroyLocalCursorRenderers() {
        for (TouchContext context : relativeTouchContextMap) {
            if (context instanceof RelativeTouchContext) {
                ((RelativeTouchContext) context).destroyLocalCursorRenderer();
            }
        }
    }

    /**
     * 刷新本地光标状态
     */
    public void refreshLocalCursorState(boolean enabled) {
        boolean shouldRender = enabled && !prefConfig.enableNativeMousePointer;

        for (TouchContext context : relativeTouchContextMap) {
            if (context instanceof RelativeTouchContext) {
                ((RelativeTouchContext) context).setEnableLocalCursorRendering(shouldRender);
            }
        }
        updateServiceState(enabled);
    }

    // ========== 光标与视频流同步 ==========

    /**
     * 强制将光标层与视频层 1:1 对齐
     */
    public void syncCursorWithStream() {
        if (streamView == null || cursorOverlay == null) return;

        float x = streamView.getX();
        float y = streamView.getY();
        int w = streamView.getWidth();
        int h = streamView.getHeight();

        if (w == 0 || h == 0) return;

        ViewGroup.LayoutParams params = cursorOverlay.getLayoutParams();

        // 强制清除 Gravity
        if (params instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) params).gravity = Gravity.TOP | Gravity.LEFT;
        }

        boolean needLayout = false;
        if (params.width != w || params.height != h) {
            params.width = w;
            params.height = h;
            needLayout = true;
        }

        if (needLayout) {
            cursorOverlay.setLayoutParams(params);
        }

        cursorOverlay.setX(x);
        cursorOverlay.setY(y);

        // 同步渲染器边界
        initializeLocalCursorRenderers(w, h);

        LimeLog.info("CursorFix: Sync executed: W=" + w + " H=" + h + " X=" + x);
    }

    // ========== 光标网络服务 ==========

    /**
     * 启动光标网络服务
     */
    public void startService() {
        if (isCursorNetworking) return;
        this.isCursorNetworking = true;

        cursorNetworkThread = new Thread(() -> {
            while (isCursorNetworking) {
                try {
                    cursorSocket = new Socket();
                    cursorSocket.connect(new InetSocketAddress(computerIpAddress, CURSOR_PORT), 3000);
                    cursorSocket.setTcpNoDelay(true);
                    DataInputStream dis = new DataInputStream(cursorSocket.getInputStream());

                    // 连接成功时清空缓存，因为服务端重连后状态重置了
                    cursorCache.evictAll();

                    while (isCursorNetworking) {
                        // 1. 读取总长度
                        byte[] lenBytes = new byte[4];
                        dis.readFully(lenBytes);
                        int packetLen = (lenBytes[0] & 0xFF) | ((lenBytes[1] & 0xFF) << 8) |
                                ((lenBytes[2] & 0xFF) << 16) | ((lenBytes[3] & 0xFF) << 24);

                        // 2. 读取包体
                        byte[] bodyData = new byte[packetLen];
                        dis.readFully(bodyData);

                        // 3. 解析协议 (新协议头 20 字节)
                        // [Hash(4)] [HotX(4)] [HotY(4)] [Frames(4)] [Delay(4)] [PNG...]
                        ByteBuffer wrapped = ByteBuffer.wrap(bodyData);
                        wrapped.order(ByteOrder.LITTLE_ENDIAN);

                        int cursorHash = wrapped.getInt();
                        int hotX = wrapped.getInt();
                        int hotY = wrapped.getInt();
                        int frameCount = wrapped.getInt();
                        int frameDelay = wrapped.getInt();

                        // ========================================================
                        // 拦截特殊控制指令包 (文本光标防遮挡逻辑)
                        // ========================================================
                        if (cursorHash == MAGIC_CONTROL_HASH) {
                            int cmdId = hotX;       // 借用 HotX 字段作为 cmdId
                            int cmdValue = hotY;    // 借用 HotY 字段作为 cmdValue

                            if (cmdId == 2) {
                                // 将具体的 UI 动画逻辑抛到主线程执行
                                uiCallback.runOnUi(() -> handleTextCursorState(cmdValue));
                            }
                            // 控制包没有 PNG 数据，直接结束本次循环
                            continue;
                        }

                        // ========================================================
                        // 正常的图像解析逻辑
                        // ========================================================
                        int headerSize = 20;
                        int pngSize = packetLen - headerSize;

                        Bitmap targetBitmap = null;

                        if (pngSize > 0) {
                            targetBitmap = BitmapFactory.decodeByteArray(bodyData, headerSize, pngSize);
                            if (targetBitmap != null) {
                                cursorCache.put(cursorHash, targetBitmap);
                            }
                        } else {
                            targetBitmap = cursorCache.get(cursorHash);
                            if (targetBitmap == null) {
                                LimeLog.warning("CursorNet: 缓存未命中! Hash: " + cursorHash);
                                continue;
                            }
                        }

                        if (targetBitmap != null) {
                            final Bitmap finalBmp = targetBitmap;
                            uiCallback.runOnUi(() -> handleCursorUpdate(finalBmp, hotX, hotY, frameCount, frameDelay));
                        }
                    }
                } catch (Exception e) {
                    LimeLog.warning("CursorNet: Connection disconnected or failed: " + e.getMessage());
                } finally {
                    try { if (cursorSocket != null) cursorSocket.close(); } catch (Exception ignored) {}
                    cursorSocket = null;

                    if (isCursorNetworking) {
                        stopCurrentAnimation();
                        restoreDefaultCursor();

                        LimeLog.info("CursorNet: 2秒后重试连接...");
                        try { Thread.sleep(2000); } catch (InterruptedException e) { break; }
                    }
                }
            }
            LimeLog.info("CursorNet: 服务线程已退出");
        });
        cursorNetworkThread.start();
    }

    /**
     * 停止光标网络服务
     */
    public void stopService() {
        isCursorNetworking = false;

        if (cursorNetworkThread != null) {
            cursorNetworkThread.interrupt();
        }

        if (cursorSocket != null) {
            try {
                cursorSocket.close();
            } catch (Exception ignored) {}
            cursorSocket = null;
        }

        uiCallback.runOnUi(() -> {
            if (!uiCallback.isActivityAlive()) return;
            restoreDefaultCursorOnUiThread();
        });

        LimeLog.info("CursorNet: 服务已停止");
    }

    /**
     * 根据当前配置和运行状态，决定是启动还是停止光标服务
     */
    public void updateServiceState(boolean shouldRun) {
        updateServiceState(shouldRun, null);
    }

    /**
     * 根据当前配置和运行状态，决定是启动还是停止光标服务
     *
     * @param shouldRun     是否应该运行
     * @param hostAddress   主机地址（可为 null，使用上次的地址）
     */
    public void updateServiceState(boolean shouldRun, String hostAddress) {
        if (hostAddress != null) {
            this.computerIpAddress = hostAddress;
        }

        if (shouldRun) {
            if (!isCursorNetworking && this.computerIpAddress != null) {
                LimeLog.info("CursorNet: Enabling cursor service during stream with host: " + this.computerIpAddress);
                startService();
            }
        } else {
            if (isCursorNetworking) {
                LimeLog.info("CursorNet: Disabling cursor service during stream");
                stopService();
            }
        }
    }

    /**
     * 光标网络服务是否正在运行
     */
    public boolean isServiceRunning() {
        return isCursorNetworking;
    }

    // ========== 内部方法 ==========

    /**
     * 核心逻辑：处理服务端发来的文本光标 Y 轴百分比状态（仅记录数据，不执行动作）
     * @param cmdValue -1表示退出输入状态；0~10000 表示所在的万分比高度
     */
    private void handleTextCursorState(int cmdValue) {
        if (cmdValue == -1) {
            lastRemoteCursorYPercent = -1.0f;
            // 如果输入法当前是开着的，可能需要实时恢复视图 (用户点了空白处)
            if (isLocalKeyboardActive) {
                uiCallback.runOnUi(() -> performViewAdjustment(false));
            }
        } else {
            lastRemoteCursorYPercent = cmdValue / 10000.0f;
            // 如果输入法已经是开启状态，实时调整高度 (用户在键盘开启时切换了输入框)
            if (isLocalKeyboardActive) {
                uiCallback.runOnUi(() -> performViewAdjustment(true));
            }
        }
    }

    /**
     * 执行视图调整动作
     * @param shouldLift 是否根据光标位置尝试抬起
     */
    private void performViewAdjustment(boolean shouldLift) {
        if (streamView == null || cursorOverlay == null) return;

        // 检查是否启用了键盘防遮挡功能
        if (!prefConfig.enableKeyboardViewLift) {
            return;
        }

        // 只有在触摸板模式和本地光标开启时才执行防遮挡
        if (!prefConfig.touchscreenTrackpad || !prefConfig.enableLocalCursorRendering) {
            return;
        }

        float targetY = 0; // 默认恢复原位

        // 将阈值从百分比转换为小数（例如 55 -> 0.55）
        float threshold = prefConfig.keyboardHeightThreshold / 100.0f;

        if (shouldLift && lastRemoteCursorYPercent > threshold) {
            // 只有当键盘开启 且 光标确实靠下时，才计算抬起值
            float currentHeight = streamView.getHeight();
            if (currentHeight == 0) return;

            float overlapPercent = lastRemoteCursorYPercent - threshold + 0.05f;
            targetY = -(overlapPercent * currentHeight);

            LimeLog.info("CursorState: 键盘已开启，光标较低 (" + (lastRemoteCursorYPercent * 100) + "%)，抬起视图: " + targetY);
        } else if (!shouldLift) {
            LimeLog.info("CursorState: 键盘关闭，恢复视图原位");
        }

        // 执行动画
        streamView.animate().translationY(targetY).setDuration(200).start();
        cursorOverlay.animate().translationY(targetY).setDuration(200).start();
    }

    /**
     * 当客户端点击"呼出键盘"或输入法弹出/关闭时调用此方法
     * @param active 键盘是否开启
     */
    public void onLocalKeyboardToggle(boolean active) {
        this.isLocalKeyboardActive = active;
        uiCallback.runOnUi(() -> performViewAdjustment(active));
    }

    private void stopCurrentAnimation() {
        animationHandler.removeCallbacksAndMessages(null);
        currentAnimationTask = null;
    }

    /**
     * 处理光标更新逻辑 (运行在 UI 线程)
     */
    private void handleCursorUpdate(Bitmap spriteSheet, int hotX, int hotY, int frameCount, int frameDelay) {
        stopCurrentAnimation();

        if (frameCount <= 1) {
            setSystemOrOverlayCursor(spriteSheet, hotX, hotY);
            return;
        }

        try {
            int singleFrameW = spriteSheet.getWidth();
            int singleFrameH = spriteSheet.getHeight() / frameCount;

            final Bitmap[] frames = new Bitmap[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = Bitmap.createBitmap(spriteSheet, 0, i * singleFrameH, singleFrameW, singleFrameH);
            }

            currentAnimationTask = new Runnable() {
                int index = 0;
                @Override
                public void run() {
                    if (!isCursorNetworking) return;
                    setSystemOrOverlayCursor(frames[index], hotX, hotY);
                    index = (index + 1) % frameCount;
                    animationHandler.postDelayed(this, frameDelay > 0 ? frameDelay : 33);
                }
            };
            currentAnimationTask.run();
        } catch (Exception e) {
            LimeLog.warning("CursorNet: 动画处理失败: " + e.getMessage());
            setSystemOrOverlayCursor(spriteSheet, hotX, hotY);
        }
    }

    private void setSystemOrOverlayCursor(Bitmap bitmap, int hotX, int hotY) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && prefConfig.enableNativeMousePointer) {
            try {
                PointerIcon pointerIcon = PointerIcon.create(bitmap, hotX, hotY);
                streamView.setPointerIcon(pointerIcon);
            } catch (Exception ignored) {}
        } else {
            if (cursorOverlay != null) {
                cursorOverlay.setCursorBitmap(bitmap, hotX, hotY);
            }
        }
    }

    private void restoreDefaultCursor() {
        uiCallback.runOnUi(this::restoreDefaultCursorOnUiThread);
    }

    private void restoreDefaultCursorOnUiThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && prefConfig.enableNativeMousePointer) {
            try {
                streamView.setPointerIcon(PointerIcon.getSystemIcon(
                        streamView.getContext(), PointerIcon.TYPE_ARROW));
            } catch (Exception ignored) {}
        } else {
            if (cursorOverlay != null) {
                cursorOverlay.resetToDefault();
            }
        }
    }
}