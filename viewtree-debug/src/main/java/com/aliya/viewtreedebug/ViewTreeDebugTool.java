package com.aliya.viewtreedebug;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ViewTreeDebugTool {
    private static final DebugController CONTROLLER = new DebugController();
    private static final Object DETAIL_INFO_PROVIDER_LOCK = new Object();
    private static final List<DetailInfoProvider> DETAIL_INFO_PROVIDERS = new ArrayList<>();

    private ViewTreeDebugTool() {
    }

    public interface DetailInfoProvider {
        @Nullable
        String provide(@NonNull View view);
    }

    public static void init(@NonNull Application application) {
        CONTROLLER.init(application);
    }

    public static boolean ensureOverlayPermission(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (Settings.canDrawOverlays(activity)) {
            return true;
        }
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        activity.startActivity(intent);
        return false;
    }

    public static void enable() {
        CONTROLLER.enable();
    }

    public static void disable() {
        CONTROLLER.disable();
    }

    public static void toggle() {
        if (isEnabled()) {
            disable();
        } else {
            enable();
        }
    }

    public static boolean isEnabled() {
        return CONTROLLER.isEnabled();
    }

    public static void registerDetailInfoProvider(@NonNull DetailInfoProvider provider) {
        synchronized (DETAIL_INFO_PROVIDER_LOCK) {
            if (!DETAIL_INFO_PROVIDERS.contains(provider)) {
                DETAIL_INFO_PROVIDERS.add(provider);
            }
        }
    }

    public static void unregisterDetailInfoProvider(@NonNull DetailInfoProvider provider) {
        synchronized (DETAIL_INFO_PROVIDER_LOCK) {
            DETAIL_INFO_PROVIDERS.remove(provider);
        }
    }

    public static void clearDetailInfoProviders() {
        synchronized (DETAIL_INFO_PROVIDER_LOCK) {
            DETAIL_INFO_PROVIDERS.clear();
        }
    }

    private enum OverlayMode {
        DOT,
        LIST,
        FULL
    }

    private enum WindowKind {
        ACTIVITY(1),
        DIALOG(2),
        POPUP(3);

        final int priority;

        WindowKind(int priority) {
            this.priority = priority;
        }
    }

    private static final class WindowEntry {
        final View root;
        final WindowManager.LayoutParams layoutParams;
        final WindowKind kind;
        final int index;

        WindowEntry(View root, WindowManager.LayoutParams layoutParams, WindowKind kind, int index) {
            this.root = root;
            this.layoutParams = layoutParams;
            this.kind = kind;
            this.index = index;
        }
    }

    private static final class ViewHitInfo {
        final View view;
        final int depth;
        final Rect rect;
        final String title;
        final WindowKind windowKind;
        final long zOrder;

        ViewHitInfo(View view, int depth, Rect rect, String title, WindowKind windowKind, long zOrder) {
            this.view = view;
            this.depth = depth;
            this.rect = rect;
            this.title = title;
            this.windowKind = windowKind;
            this.zOrder = zOrder;
        }
    }

    private static final class HitTreeNode {
        final View view;
        final WindowKind windowKind;
        final int depth;
        final Rect rect;
        final String title;
        final List<HitTreeNode> children = new ArrayList<>();
        boolean expanded;
        boolean childrenLoaded;

        HitTreeNode(@NonNull View view, @NonNull WindowKind windowKind, int depth) {
            this.view = view;
            this.windowKind = windowKind;
            this.depth = depth;
            Rect bounds = new Rect();
            view.getGlobalVisibleRect(bounds);
            this.rect = bounds;
            this.title = ViewInspector.buildTitle(view, depth, bounds, windowKind);
        }
    }

    private static final class TreeNode {
        final View view;
        final int depth;
        final Rect rect;
        final List<TreeNode> children = new ArrayList<>();
        TreeNode parent;
        boolean expanded;
        final String title;

        TreeNode(View view, int depth, Rect rect, String title) {
            this.view = view;
            this.depth = depth;
            this.rect = rect;
            this.title = title;
        }
    }

    private static final class DebugController implements Application.ActivityLifecycleCallbacks {
        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        private final Runnable settleRunnable = new Runnable() {
            @Override
            public void run() {
                showHitListForDotCenter();
            }
        };

        private Application application;
        private WindowManager windowManager;
        private boolean initialized;
        private boolean enabled;
        private Activity topActivity;

        private OverlayMode overlayMode = OverlayMode.DOT;

        private View dotView;
        private WindowManager.LayoutParams dotParams;
        private float dotRawX;
        private float dotRawY;

        private View listView;
        private RecyclerView hitRecyclerView;
        private HitAdapter hitAdapter;
        private WindowManager.LayoutParams listParams;
        private List<HitTreeNode> currentHitRoots = new ArrayList<>();
        private List<HitTreeNode> currentHitVisible = new ArrayList<>();
        private WindowEntry currentHitWindow;
        private HitTreeNode selectedHitNode;

        private View fullView;
        private RecyclerView treeRecyclerView;
        private TreeAdapter treeAdapter;
        private TextView detailText;
        private ImageView detailImage;
        private EditText searchInputView;
        private TextView searchTipView;
        private WindowManager.LayoutParams fullParams;
        private HighlightOverlayView fullHighlightOverlayView;
        private Bitmap detailBitmap;
        private TreeNode selectedNode;
        private TreeNode currentRootNode;
        private String lastSearchQuery = "";
        private List<TreeNode> searchMatches = new ArrayList<>();
        private int searchIndex = 0;
        private boolean glideTagResolved;
        private Integer glideTagId;

        private int dotSizePx;
        private HighlightOverlayView highlightOverlayView;
        private WindowManager.LayoutParams highlightParams;

        synchronized void init(@NonNull Application application) {
            if (initialized) {
                return;
            }
            this.application = application;
            this.windowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
            this.application.registerActivityLifecycleCallbacks(this);
            this.dotSizePx = dp(20f);
            initialized = true;
        }

        boolean isEnabled() {
            return enabled;
        }

        void enable() {
            postToMain(new Runnable() {
                @Override
                public void run() {
                    if (!initialized || enabled || windowManager == null) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(application)) {
                        return;
                    }
                    enabled = true;
                    overlayMode = OverlayMode.DOT;
                    ensureDotOverlay();
                    hideListOverlay();
                    hideFullOverlay();
                }
            });
        }

        void disable() {
            postToMain(new Runnable() {
                @Override
                public void run() {
                    if (!initialized) {
                        return;
                    }
                    enabled = false;
                    mainHandler.removeCallbacks(settleRunnable);
                    hideDotOverlay();
                    hideListOverlay();
                    hideFullOverlay();
                    recycleDetailBitmap();
                }
            });
        }

        private void postToMain(@NonNull Runnable runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                runnable.run();
            } else {
                mainHandler.post(runnable);
            }
        }

        private void ensureDotOverlay() {
            if (dotView == null) {
                dotView = createDotView();
            }
            if (dotParams == null) {
                dotParams = createCommonLayoutParams();
                dotParams.width = dotSizePx;
                dotParams.height = dotSizePx;
                dotParams.gravity = Gravity.START | Gravity.TOP;
                dotParams.x = dp(24f);
                dotParams.y = dp(160f);
                dotRawX = dotParams.x;
                dotRawY = dotParams.y;
            }
            addOrUpdateView(dotView, dotParams);
        }

        private View createDotView() {
            View view = new View(application);
            view.setBackground(createDotBackground());
            view.setOnTouchListener(new View.OnTouchListener() {
                private float downRawX;
                private float downRawY;
                private float startX;
                private float startY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!enabled || dotParams == null) {
                        return false;
                    }
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            downRawX = event.getRawX();
                            downRawY = event.getRawY();
                            startX = dotParams.x;
                            startY = dotParams.y;
                            mainHandler.removeCallbacks(settleRunnable);
                            if (overlayMode == OverlayMode.FULL) {
                                hideFullOverlay();
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            float dx = event.getRawX() - downRawX;
                            float dy = event.getRawY() - downRawY;
                            dotRawX = Math.max(0, startX + dx);
                            dotRawY = Math.max(0, startY + dy);
                            dotParams.x = (int) dotRawX;
                            dotParams.y = (int) dotRawY;
                            updateViewLayoutSafe(dotView, dotParams);
                            hideListOverlay();
                            overlayMode = OverlayMode.DOT;
                            mainHandler.removeCallbacks(settleRunnable);
                            mainHandler.postDelayed(settleRunnable, 1000L);
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mainHandler.removeCallbacks(settleRunnable);
                            mainHandler.postDelayed(settleRunnable, 1000L);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            return view;
        }

        private android.graphics.drawable.Drawable createDotBackground() {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor("#FF3D5AFE"));
            drawable.setStroke(dp(1f), Color.WHITE);
            return drawable;
        }

        private void showHitListForDotCenter() {
            if (!enabled || dotParams == null) {
                return;
            }
            int x;
            int y;
            if (dotView != null && dotView.getParent() != null) {
                int[] location = new int[2];
                dotView.getLocationOnScreen(location);
                x = location[0] + dotView.getWidth() / 2;
                y = location[1] + dotView.getHeight() / 2;
            } else {
                x = dotParams.x + dotSizePx / 2;
                y = dotParams.y + dotSizePx / 2;
            }
            WindowEntry topWindow = WindowInspector.findTopWindowHit(x, y);
            currentHitWindow = topWindow;
            if (topWindow == null) {
                hideListOverlay();
                overlayMode = OverlayMode.DOT;
                return;
            }
            List<ViewHitInfo> hitList = ViewInspector.collectHitViews(topWindow.root, x, y, topWindow.kind);
            if (hitList.isEmpty()) {
                hideListOverlay();
                clearHighlight();
                overlayMode = OverlayMode.DOT;
                return;
            }
            currentHitRoots = buildHitNodesInOrder(hitList, topWindow.kind);
            ensureListOverlay();
            hitAdapter.submitRootNodes(currentHitRoots);
            currentHitVisible = hitAdapter.getVisibleNodesSnapshot();
            overlayMode = OverlayMode.LIST;
            if (!currentHitVisible.isEmpty()) {
                setSelectedHitNode(currentHitVisible.get(0));
            }
            updateListPositionNearDot();
            addOrUpdateView(listView, listParams);
        }

        private void ensureListOverlay() {
            if (listView != null && listParams != null) {
                return;
            }
            FrameLayout root = new FrameLayout(application);
            root.setBackgroundColor(Color.parseColor("#B31E1E1E"));
            int horizontalPadding = dp(8f);
            int verticalPadding = dp(6f);
            root.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            LinearLayout content = new LinearLayout(application);
            content.setOrientation(LinearLayout.VERTICAL);
            root.addView(content, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout actionBar = new LinearLayout(application);
            actionBar.setOrientation(LinearLayout.HORIZONTAL);
            actionBar.setGravity(Gravity.CENTER_VERTICAL);
            TextView back = buildActionButton("←");
            TextView close = buildActionButton("X");
            TextView title = new TextView(application);
            title.setText("坐标命中列表");
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            titleLp.leftMargin = dp(8f);
            titleLp.rightMargin = dp(8f);
            actionBar.addView(back, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            actionBar.addView(title, titleLp);
            actionBar.addView(close, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            content.addView(actionBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            RecyclerView recyclerView = new RecyclerView(application);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(application, RecyclerView.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            hitAdapter = new HitAdapter(new HitAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(HitTreeNode node) {
                    setSelectedHitNode(node);
                    showFullOverlay(node.view);
                }
            });
            recyclerView.setAdapter(hitAdapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    int first = layoutManager.findFirstVisibleItemPosition();
                    List<HitTreeNode> visibleNodes = hitAdapter.getVisibleNodesSnapshot();
                    if (first >= 0 && first < visibleNodes.size()) {
                        currentHitVisible = visibleNodes;
                        setSelectedHitNode(visibleNodes.get(first));
                    }
                }
            });
            LinearLayout.LayoutParams listLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            listLp.topMargin = dp(6f);
            content.addView(recyclerView, listLp);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideListOverlay();
                    overlayMode = OverlayMode.DOT;
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disable();
                }
            });
            listView = root;
            hitRecyclerView = recyclerView;
            listParams = createCommonLayoutParams();
            listParams.gravity = Gravity.START | Gravity.TOP;
            listParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            listParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        private void updateListPositionNearDot() {
            if (listParams == null || dotParams == null || topActivity == null) {
                return;
            }
            int screenW = topActivity.getResources().getDisplayMetrics().widthPixels;
            int screenH = topActivity.getResources().getDisplayMetrics().heightPixels;
            int desiredX = dotParams.x - dp(60f);
            int desiredY = dotParams.y + dotSizePx + dp(8f);
            int maxX = Math.max(0, screenW - dp(220f));
            int maxY = Math.max(0, screenH - dp(80f));
            listParams.x = Math.max(0, Math.min(desiredX, maxX));
            listParams.y = Math.max(0, Math.min(desiredY, maxY));
        }

        private void showFullOverlay(@NonNull View targetView) {
            if (!enabled) {
                return;
            }
            ensureFullOverlay();
            if (currentHitWindow == null) {
                currentHitWindow = WindowInspector.findWindowContainsView(targetView);
            }
            View root = currentHitWindow == null ? targetView.getRootView() : currentHitWindow.root;
            TreeNode rootNode = ViewInspector.buildTree(root, 0);
            currentRootNode = rootNode;
            TreeNode targetNode = ViewInspector.findNodeByView(rootNode, targetView);
            if (targetNode != null) {
                ViewInspector.expandAncestors(targetNode);
                selectedNode = targetNode;
            } else {
                selectedNode = rootNode;
            }
            resetSearchState();
            treeAdapter.submitRoot(rootNode, selectedNode);
            overlayMode = OverlayMode.FULL;
            addOrUpdateView(fullView, fullParams);
            scrollToSelected();
            bindNodeDetail(selectedNode);
            updateTreeNodeHighlight(selectedNode);
        }

        private void ensureFullOverlay() {
            if (fullView != null && fullParams != null) {
                return;
            }
            FrameLayout root = new FrameLayout(application);
            root.setBackgroundColor(Color.parseColor("#99161616"));

            HighlightOverlayView fullHighlightView = new HighlightOverlayView(application);
            root.addView(fullHighlightView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout container = new LinearLayout(application);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setPadding(dp(12f), dp(12f), dp(12f), dp(12f));
            root.addView(container, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            LinearLayout titleBar = new LinearLayout(application);
            titleBar.setOrientation(LinearLayout.HORIZONTAL);
            titleBar.setGravity(Gravity.CENTER_VERTICAL);
            TextView back = buildActionButton("返回列表");
            TextView close = buildActionButton("关闭调试");
            TextView title = new TextView(application);
            title.setText("全屏 View 树");
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            titleLp.leftMargin = dp(12f);
            titleBar.addView(back, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            titleBar.addView(title, titleLp);
            titleBar.addView(close, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(titleBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout searchBar = new LinearLayout(application);
            searchBar.setOrientation(LinearLayout.HORIZONTAL);
            searchBar.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams searchBarLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            searchBarLp.topMargin = dp(10f);
            container.addView(searchBar, searchBarLp);

            EditText searchInput = new EditText(application);
            searchInput.setHint("输入ID或类名");
            searchInput.setHintTextColor(Color.parseColor("#80FFFFFF"));
            searchInput.setTextColor(Color.WHITE);
            searchInput.setSingleLine(true);
            searchInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            searchInput.setBackgroundColor(Color.parseColor("#33333333"));
            searchInput.setPadding(dp(8f), dp(6f), dp(8f), dp(6f));
            LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            searchBar.addView(searchInput, inputLp);

            TextView searchBtn = buildActionButton("搜索");
            LinearLayout.LayoutParams searchBtnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            searchBtnLp.leftMargin = dp(8f);
            searchBar.addView(searchBtn, searchBtnLp);

            TextView clearBtn = buildActionButton("清除");
            LinearLayout.LayoutParams clearBtnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clearBtnLp.leftMargin = dp(8f);
            searchBar.addView(clearBtn, clearBtnLp);

            TextView searchTip = new TextView(application);
            searchTip.setTextColor(Color.parseColor("#CCFFFFFF"));
            searchTip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            LinearLayout.LayoutParams searchTipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            searchTipLp.topMargin = dp(6f);
            container.addView(searchTip, searchTipLp);

            HorizontalScrollView hScroll = new HorizontalScrollView(application);
            hScroll.setFillViewport(false);
            hScroll.setHorizontalScrollBarEnabled(true);
            RecyclerView recyclerView = new RecyclerView(application);
            recyclerView.setLayoutManager(new LinearLayoutManager(application, RecyclerView.VERTICAL, false));
            treeAdapter = new TreeAdapter(new TreeAdapter.OnNodeActionListener() {
                @Override
                public void onNodeClick(TreeNode node) {
                    selectedNode = node;
                    bindNodeDetail(node);
                    updateTreeNodeHighlight(node);
                    treeAdapter.updateSelectedNode(selectedNode);
                }

                @Override
                public void onNodeToggle(TreeNode node) {
                    if (!node.children.isEmpty()) {
                        node.expanded = !node.expanded;
                        treeAdapter.rebuildVisibleList();
                    }
                }
            });
            recyclerView.setAdapter(treeAdapter);
            hScroll.addView(recyclerView, new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            LinearLayout.LayoutParams treeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
            treeLp.topMargin = dp(12f);
            container.addView(hScroll, treeLp);

            ScrollView detailScroll = new ScrollView(application);
            detailScroll.setFillViewport(true);
            detailScroll.setVerticalScrollBarEnabled(true);
            LinearLayout detailContainer = new LinearLayout(application);
            detailContainer.setOrientation(LinearLayout.VERTICAL);
            detailContainer.setPadding(dp(10f), dp(10f), dp(10f), dp(10f));
            detailContainer.setBackgroundColor(Color.parseColor("#332D2D2D"));
            TextView detail = new TextView(application);
            detail.setTextColor(Color.WHITE);
            detail.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            detail.setLineSpacing(dp(2f), 1f);
            ImageView imageView = new ImageView(application);
            imageView.setAdjustViewBounds(true);
            LinearLayout.LayoutParams imageLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            imageLp.topMargin = dp(8f);
            detailContainer.addView(detail, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            detailContainer.addView(imageView, imageLp);
            detailScroll.addView(detailContainer, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams detailLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(240f));
            detailLp.topMargin = dp(12f);
            container.addView(detailScroll, detailLp);

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideFullOverlay();
                    if (enabled) {
                        ensureListOverlay();
                        addOrUpdateView(listView, listParams);
                        overlayMode = OverlayMode.LIST;
                        List<HitTreeNode> visibleNodes = hitAdapter == null ? Collections.<HitTreeNode>emptyList() : hitAdapter.getVisibleNodesSnapshot();
                        if (!visibleNodes.isEmpty()) {
                            HitTreeNode targetNode = selectedHitNode == null ? visibleNodes.get(0) : selectedHitNode;
                            setSelectedHitNode(targetNode);
                        }
                    }
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    disable();
                }
            });
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performSearch(searchInput.getText().toString().trim());
                }
            });
            clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchInput.setText("");
                    clearSearch();
                }
            });

            fullView = root;
            fullHighlightOverlayView = fullHighlightView;
            treeRecyclerView = recyclerView;
            detailText = detail;
            detailImage = imageView;
            searchInputView = searchInput;
            searchTipView = searchTip;
            fullParams = createCommonLayoutParams();
            fullParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            fullParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            fullParams.gravity = Gravity.TOP | Gravity.START;
            fullParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }

        private TextView buildActionButton(String text) {
            TextView tv = new TextView(application);
            tv.setText(text);
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.parseColor("#335A6CFF"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tv.setPadding(dp(10f), dp(6f), dp(10f), dp(6f));
            return tv;
        }

        private void bindNodeDetail(@Nullable TreeNode node) {
            if (detailText == null || detailImage == null || node == null) {
                return;
            }
            View view = node.view;
            Rect rect = node.rect;
            String idText = ViewInspector.getIdName(view);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            Rect visibleRect = new Rect();
            boolean visible = view.getGlobalVisibleRect(visibleRect);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            String marginText = "N/A";
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams margin = (ViewGroup.MarginLayoutParams) params;
                marginText = String.format(Locale.CHINA, "[l=%d,t=%d,r=%d,b=%d]",
                        margin.leftMargin, margin.topMargin, margin.rightMargin, margin.bottomMargin);
            }
            String weightText = "N/A";
            if (params instanceof LinearLayout.LayoutParams) {
                weightText = String.format(Locale.CHINA, "%.2f", ((LinearLayout.LayoutParams) params).weight);
            }
            StringBuilder detailBuilder = new StringBuilder(512);
            detailBuilder.append("【基本信息】")
                    .append('\n').append("id=").append(idText)
                    .append('\n').append("class=").append(view.getClass().getName())
                    .append('\n').append("visibility=").append(visibilityToText(view.getVisibility()))
                    .append('\n').append("alpha=").append(String.format(Locale.CHINA, "%.2f", view.getAlpha()))
                    .append('\n').append("depth=").append(node.depth)
                    .append('\n').append("rect=[").append(rect.left).append(',').append(rect.top).append(',').append(rect.right).append(',').append(rect.bottom).append(']')
                    .append('\n').append("size=").append(view.getWidth()).append('x').append(view.getHeight())
                    .append('\n').append("visible=").append(visible)
                    .append('\n')
                    .append('\n').append("【布局信息】")
                    .append('\n').append("margin=").append(marginText)
                    .append('\n').append("padding=[l=").append(view.getPaddingLeft()).append(",t=").append(view.getPaddingTop())
                    .append(",r=").append(view.getPaddingRight()).append(",b=").append(view.getPaddingBottom()).append(']')
                    .append('\n').append("measured=").append(view.getMeasuredWidth()).append('x').append(view.getMeasuredHeight())
                    .append('\n').append("screenXY=[").append(location[0]).append(',').append(location[1]).append(']')
                    .append('\n').append("weight=").append(weightText)
                    .append('\n').append("scroll=[").append(view.getScrollX()).append(',').append(view.getScrollY()).append(']')
                    .append('\n').append("scale=[").append(String.format(Locale.CHINA, "%.2f", view.getScaleX())).append(',')
                    .append(String.format(Locale.CHINA, "%.2f", view.getScaleY())).append(']')
                    .append('\n').append("rotate=[").append(String.format(Locale.CHINA, "%.2f", view.getRotationX())).append(',')
                    .append(String.format(Locale.CHINA, "%.2f", view.getRotationY())).append(',')
                    .append(String.format(Locale.CHINA, "%.2f", view.getRotation())).append(']')
                    .append('\n').append("translation=[").append(String.format(Locale.CHINA, "%.2f", view.getTranslationX())).append(',')
                    .append(String.format(Locale.CHINA, "%.2f", view.getTranslationY())).append(']')
                    .append('\n').append("left/top/right/bottom=[")
                    .append(view.getLeft()).append(',').append(view.getTop()).append(',')
                    .append(view.getRight()).append(',').append(view.getBottom()).append(']')
                    .append('\n')
                    .append('\n').append("【关键状态】")
                    .append('\n').append("clickable=").append(view.isClickable())
                    .append('\n').append("focused=").append(view.isFocused())
                    .append('\n').append("enabled=").append(view.isEnabled())
                    .append('\n')
                    .append('\n').append("【资源信息】")
                    .append('\n').append("foreground=").append(describeForeground(view))
                    .append('\n').append("background=").append(describeDrawable(view.getBackground()));
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                float textSizeSp = textView.getTextSize() / textView.getResources().getDisplayMetrics().scaledDensity;
                detailBuilder.append('\n')
                        .append('\n').append("【TextView属性】")
                        .append('\n').append("textSizeSp=").append(String.format(Locale.CHINA, "%.2f", textSizeSp))
                        .append('\n').append("textColor=").append(colorToHex(textView.getCurrentTextColor()))
                        .append('\n').append("text=").append(textView.getText());
            }
            if (view instanceof ImageView) {
                detailBuilder.append('\n')
                        .append('\n').append("【ImageView属性】")
                        .append('\n').append("glideTag=").append(describeGlideTag(view));
            }
            appendInjectedDetail(view, detailBuilder);
            detailText.setText(detailBuilder.toString());
            recycleDetailBitmap();
            detailBitmap = ViewInspector.captureBitmap(view, dp(320f));
            detailImage.setImageBitmap(detailBitmap);
        }

        private void appendInjectedDetail(@NonNull View view, @NonNull StringBuilder detailBuilder) {
            List<DetailInfoProvider> providers;
            synchronized (DETAIL_INFO_PROVIDER_LOCK) {
                if (DETAIL_INFO_PROVIDERS.isEmpty()) {
                    return;
                }
                providers = new ArrayList<>(DETAIL_INFO_PROVIDERS);
            }
            StringBuilder extensionBuilder = new StringBuilder();
            for (DetailInfoProvider provider : providers) {
                if (provider == null) {
                    continue;
                }
                try {
                    String value = provider.provide(view);
                    if (!TextUtils.isEmpty(value)) {
                        if (extensionBuilder.length() > 0) {
                            extensionBuilder.append('\n');
                        }
                        extensionBuilder.append(value);
                    }
                } catch (Throwable throwable) {
                    if (extensionBuilder.length() > 0) {
                        extensionBuilder.append('\n');
                    }
                    extensionBuilder.append(provider.getClass().getSimpleName())
                            .append(" error: ")
                            .append(throwable.getClass().getSimpleName());
                }
            }
            if (extensionBuilder.length() == 0) {
                return;
            }
            detailBuilder.append('\n')
                    .append('\n').append("【扩展信息】")
                    .append('\n').append(extensionBuilder);
        }

        @NonNull
        private String visibilityToText(int visibility) {
            if (visibility == View.VISIBLE) {
                return "VISIBLE";
            }
            if (visibility == View.INVISIBLE) {
                return "INVISIBLE";
            }
            if (visibility == View.GONE) {
                return "GONE";
            }
            return String.valueOf(visibility);
        }

        @NonNull
        private String describeForeground(@NonNull View view) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return "UNSUPPORTED";
            }
            return describeDrawable(view.getForeground());
        }

        @NonNull
        private String describeDrawable(@Nullable Drawable drawable) {
            if (drawable == null) {
                return "null";
            }
            return drawable.getClass().getName();
        }

        @NonNull
        private String colorToHex(int color) {
            return String.format(Locale.CHINA, "#%08X", color);
        }

        @NonNull
        private String describeGlideTag(@NonNull View view) {
            StringBuilder builder = new StringBuilder();
            Object defaultTag = view.getTag();
            if (defaultTag != null) {
                builder.append("default=").append(defaultTag.getClass().getName());
            }
            Integer glideTagKey = resolveGlideTagId();
            if (glideTagKey != null) {
                Object glideTagValue = view.getTag(glideTagKey);
                if (glideTagValue != null) {
                    if (builder.length() > 0) {
                        builder.append("; ");
                    }
                    builder.append("glide(").append(glideTagKey).append(")=").append(glideTagValue.getClass().getName());
                }
            }
            return builder.length() == 0 ? "none" : builder.toString();
        }

        @Nullable
        private Integer resolveGlideTagId() {
            if (glideTagResolved) {
                return glideTagId;
            }
            glideTagResolved = true;
            String[] candidateFields = new String[]{
                    "glide_custom_view_target_tag",
                    "view_target_tag"
            };
            for (String fieldName : candidateFields) {
                try {
                    Class<?> glideIdClass = Class.forName("com.bumptech.glide.R$id");
                    Field field = glideIdClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof Integer) {
                        glideTagId = (Integer) value;
                        return glideTagId;
                    }
                } catch (Throwable ignored) {
                }
            }
            return null;
        }

        private void recycleDetailBitmap() {
            if (detailBitmap != null && !detailBitmap.isRecycled()) {
                detailBitmap.recycle();
            }
            detailBitmap = null;
            if (detailImage != null) {
                detailImage.setImageDrawable(null);
            }
        }

        private WindowManager.LayoutParams createCommonLayoutParams() {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.format = PixelFormat.TRANSLUCENT;
            params.gravity = Gravity.TOP | Gravity.START;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            return params;
        }

        private int dp(float dpValue) {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, application.getResources().getDisplayMetrics()));
        }

        private void addOrUpdateView(@NonNull View view, @NonNull WindowManager.LayoutParams params) {
            if (windowManager == null) {
                return;
            }
            try {
                if (view.getParent() == null) {
                    windowManager.addView(view, params);
                } else {
                    windowManager.updateViewLayout(view, params);
                }
            } catch (Throwable ignored) {
            }
        }

        private void updateViewLayoutSafe(@Nullable View view, @Nullable WindowManager.LayoutParams params) {
            if (windowManager == null || view == null || params == null || view.getParent() == null) {
                return;
            }
            try {
                windowManager.updateViewLayout(view, params);
            } catch (Throwable ignored) {
            }
        }

        private void removeViewSafe(@Nullable View view) {
            if (windowManager == null || view == null || view.getParent() == null) {
                return;
            }
            try {
                windowManager.removeView(view);
            } catch (Throwable ignored) {
            }
        }

        private void hideDotOverlay() {
            removeViewSafe(dotView);
        }

        private void hideListOverlay() {
            removeViewSafe(listView);
            selectedHitNode = null;
            currentHitRoots = new ArrayList<>();
            currentHitVisible = new ArrayList<>();
            clearHighlight();
        }

        private void hideFullOverlay() {
            removeViewSafe(fullView);
            if (fullHighlightOverlayView != null) {
                fullHighlightOverlayView.clearHighlightRect();
            }
            recycleDetailBitmap();
        }

        private void performSearch(@NonNull String query) {
            if (currentRootNode == null || treeAdapter == null) {
                return;
            }
            if (TextUtils.isEmpty(query)) {
                clearSearch();
                return;
            }
            if (query.equalsIgnoreCase(lastSearchQuery) && !searchMatches.isEmpty()) {
                searchIndex = (searchIndex + 1) % searchMatches.size();
                applySearchSelection(searchMatches.get(searchIndex));
                updateSearchTip(query, searchMatches.size(), searchIndex + 1);
                return;
            }
            List<TreeNode> matches = ViewInspector.searchNodes(currentRootNode, query);
            lastSearchQuery = query;
            searchMatches = matches;
            searchIndex = 0;
            if (matches.isEmpty()) {
                treeAdapter.applySearchState(query, matches, selectedNode);
                updateSearchTip(query, 0, 0);
                return;
            }
            for (TreeNode node : matches) {
                ViewInspector.expandAncestors(node);
            }
            applySearchSelection(matches.get(0));
            updateSearchTip(query, matches.size(), 1);
        }

        private void applySearchSelection(@NonNull TreeNode node) {
            selectedNode = node;
            treeAdapter.applySearchState(lastSearchQuery, searchMatches, selectedNode);
            bindNodeDetail(selectedNode);
            updateTreeNodeHighlight(selectedNode);
            scrollToSelected();
        }

        private void updateTreeNodeHighlight(@Nullable TreeNode node) {
            if (node == null || fullHighlightOverlayView == null) {
                return;
            }
            fullHighlightOverlayView.setHighlightRect(node.rect);
            clearWindowHighlight();
        }

        private void clearSearch() {
            resetSearchState();
            if (treeAdapter != null) {
                treeAdapter.clearSearchState(selectedNode);
            }
            updateSearchTip("", 0, 0);
            scrollToSelected();
        }

        private void resetSearchState() {
            lastSearchQuery = "";
            searchMatches = new ArrayList<>();
            searchIndex = 0;
        }

        private void updateSearchTip(@NonNull String query, int count, int index) {
            if (searchTipView == null) {
                return;
            }
            if (TextUtils.isEmpty(query)) {
                searchTipView.setText("支持按ID或类名搜索，重复点击搜索可定位下一个结果");
                return;
            }
            if (count <= 0) {
                searchTipView.setText("未命中: " + query);
                return;
            }
            searchTipView.setText(String.format(Locale.CHINA, "搜索 \"%s\" 命中 %d 个，当前第 %d 个", query, count, index));
        }

        private void scrollToSelected() {
            if (treeRecyclerView == null || treeAdapter == null) {
                return;
            }
            Integer pos = treeAdapter.getSelectedPosition();
            if (pos == null) {
                return;
            }
            treeRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    Integer selectedPosition = treeAdapter.getSelectedPosition();
                    if (selectedPosition != null) {
                        treeRecyclerView.scrollToPosition(selectedPosition);
                    }
                }
            });
        }

        private void setSelectedHitNode(@Nullable HitTreeNode node) {
            if (node == null) {
                return;
            }
            selectedHitNode = node;
            if (hitAdapter != null) {
                hitAdapter.setSelectedNode(node);
                currentHitVisible = hitAdapter.getVisibleNodesSnapshot();
            }
            showHighlightFor(node);
        }

        @NonNull
        private List<HitTreeNode> buildHitNodesInOrder(@NonNull List<ViewHitInfo> hitList, @NonNull WindowKind kind) {
            List<HitTreeNode> roots = new ArrayList<>();
            for (ViewHitInfo info : hitList) {
                roots.add(new HitTreeNode(info.view, kind, info.depth));
            }
            return roots;
        }

        private void showHighlightFor(@NonNull HitTreeNode node) {
            if (!enabled) {
                return;
            }
            if (overlayMode == OverlayMode.FULL && fullHighlightOverlayView != null) {
                fullHighlightOverlayView.setHighlightRect(node.rect);
                clearWindowHighlight();
                return;
            }
            ensureHighlightOverlay();
            addOrUpdateView(highlightOverlayView, highlightParams);
            highlightOverlayView.setHighlightRect(node.rect);
        }

        private void ensureHighlightOverlay() {
            if (highlightOverlayView != null && highlightParams != null) {
                return;
            }
            HighlightOverlayView view = new HighlightOverlayView(application);
            WindowManager.LayoutParams params = createCommonLayoutParams();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.TOP | Gravity.START;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            highlightOverlayView = view;
            highlightParams = params;
        }

        private void clearHighlight() {
            if (fullHighlightOverlayView != null) {
                fullHighlightOverlayView.clearHighlightRect();
            }
            clearWindowHighlight();
        }

        private void clearWindowHighlight() {
            if (highlightOverlayView != null) {
                highlightOverlayView.clearHighlightRect();
            }
            removeViewSafe(highlightOverlayView);
        }

        private void restoreDotModeForActivityLifecycle(@NonNull Activity activity) {
            if (!enabled || topActivity != activity) {
                return;
            }
            if (overlayMode == OverlayMode.LIST) {
                hideListOverlay();
                overlayMode = OverlayMode.DOT;
                ensureDotOverlay();
                return;
            }
            if (overlayMode == OverlayMode.FULL) {
                hideFullOverlay();
                hideListOverlay();
                overlayMode = OverlayMode.DOT;
                ensureDotOverlay();
            }
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable android.os.Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            topActivity = activity;
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            topActivity = activity;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            restoreDotModeForActivityLifecycle(activity);
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (topActivity == activity) {
                topActivity = null;
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull android.os.Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            restoreDotModeForActivityLifecycle(activity);
            if (topActivity == activity) {
                topActivity = null;
            }
        }
    }

    private static final class HitAdapter extends RecyclerView.Adapter<HitAdapter.HitViewHolder> {
        interface OnItemClickListener {
            void onItemClick(HitTreeNode node);
        }

        private final List<HitTreeNode> roots = new ArrayList<>();
        private final List<HitTreeNode> visibleNodes = new ArrayList<>();
        private final OnItemClickListener listener;
        private HitTreeNode selectedNode;

        HitAdapter(OnItemClickListener listener) {
            this.listener = listener;
        }

        void submitRootNodes(@Nullable List<HitTreeNode> list) {
            roots.clear();
            if (list != null) {
                roots.addAll(list);
            }
            if (selectedNode == null || !containsNode(roots, selectedNode)) {
                selectedNode = null;
            }
            rebuildVisibleList();
        }

        void setSelectedNode(@Nullable HitTreeNode node) {
            selectedNode = node;
            notifyDataSetChanged();
        }

        void notifyTreeChanged() {
            rebuildVisibleList();
        }

        @NonNull
        List<HitTreeNode> getVisibleNodesSnapshot() {
            return new ArrayList<>(visibleNodes);
        }

        private void rebuildVisibleList() {
            visibleNodes.clear();
            for (HitTreeNode node : roots) {
                appendNode(node);
            }
            notifyDataSetChanged();
        }

        private void appendNode(@NonNull HitTreeNode node) {
            visibleNodes.add(node);
            if (!node.expanded || node.children.isEmpty()) {
                return;
            }
            for (HitTreeNode child : node.children) {
                appendNode(child);
            }
        }

        private boolean containsNode(@NonNull List<HitTreeNode> nodes, @NonNull HitTreeNode target) {
            for (HitTreeNode node : nodes) {
                if (node == target) {
                    return true;
                }
                if (containsNode(node.children, target)) {
                    return true;
                }
            }
            return false;
        }

        @NonNull
        @Override
        public HitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            TextView textView = new TextView(context);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            textView.setSingleLine(true);
            textView.setPadding(dp(context, 10f), dp(context, 6f), dp(context, 10f), dp(context, 6f));
            textView.setBackgroundColor(Color.parseColor("#335A6CFF"));
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(context, 8f);
            textView.setLayoutParams(lp);
            return new HitViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull HitViewHolder holder, int position) {
            final HitTreeNode node = visibleNodes.get(position);
            holder.textView.setText(node.title);
            holder.textView.setBackgroundColor(node == selectedNode
                    ? Color.parseColor("#6B5A6CFF")
                    : Color.parseColor("#335A6CFF"));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = holder.getBindingAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onItemClick(node);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return visibleNodes.size();
        }

        static int dp(Context context, float dpValue) {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics()));
        }

        static final class HitViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;

            HitViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }

    private static final class HighlightOverlayView extends View {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF screenRect = new RectF();
        private final RectF drawRect = new RectF();
        private final int[] locationOnScreen = new int[2];
        private boolean hasRect;

        HighlightOverlayView(Context context) {
            super(context);
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(Color.parseColor("#223D5AFE"));
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dp(context, 2f));
            strokePaint.setColor(Color.parseColor("#FF80A8FF"));
            strokePaint.setPathEffect(new DashPathEffect(new float[]{dp(context, 8f), dp(context, 6f)}, 0f));
        }

        void setHighlightRect(@NonNull Rect highlightRect) {
            screenRect.set(highlightRect);
            hasRect = !screenRect.isEmpty();
            invalidate();
        }

        void clearHighlightRect() {
            hasRect = false;
            screenRect.setEmpty();
            drawRect.setEmpty();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!hasRect) {
                return;
            }
            getLocationOnScreen(locationOnScreen);
            drawRect.set(
                    screenRect.left - locationOnScreen[0],
                    screenRect.top - locationOnScreen[1],
                    screenRect.right - locationOnScreen[0],
                    screenRect.bottom - locationOnScreen[1]
            );
            canvas.drawRect(drawRect, fillPaint);
            canvas.drawRect(drawRect, strokePaint);
        }

        private static int dp(Context context, float dpValue) {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics()));
        }
    }

    private static final class TreeAdapter extends RecyclerView.Adapter<TreeAdapter.TreeViewHolder> {
        interface OnNodeActionListener {
            void onNodeClick(TreeNode node);

            void onNodeToggle(TreeNode node);
        }

        private final OnNodeActionListener listener;
        private final List<TreeNode> visibleNodes = new ArrayList<>();
        private final Set<TreeNode> searchVisibleNodes = new HashSet<>();
        private final Set<TreeNode> matchedNodes = new HashSet<>();
        private TreeNode root;
        private TreeNode selected;
        private boolean searchMode;
        private TreeNode lastClickedNode;
        private long lastClickUptimeMs;

        TreeAdapter(OnNodeActionListener listener) {
            this.listener = listener;
        }

        void submitRoot(TreeNode root, TreeNode selected) {
            this.root = root;
            this.selected = selected;
            searchMode = false;
            searchVisibleNodes.clear();
            matchedNodes.clear();
            lastClickedNode = null;
            lastClickUptimeMs = 0L;
            rebuildVisibleList();
        }

        void updateSelectedNode(TreeNode selected) {
            this.selected = selected;
            notifyDataSetChanged();
        }

        void applySearchState(@Nullable String query, @NonNull List<TreeNode> matches, @Nullable TreeNode selected) {
            this.selected = selected;
            searchMode = !TextUtils.isEmpty(query);
            searchVisibleNodes.clear();
            matchedNodes.clear();
            matchedNodes.addAll(matches);
            if (searchMode) {
                for (TreeNode node : matches) {
                    TreeNode current = node;
                    while (current != null) {
                        searchVisibleNodes.add(current);
                        current = current.parent;
                    }
                }
            }
            rebuildVisibleList();
        }

        void clearSearchState(@Nullable TreeNode selected) {
            this.selected = selected;
            searchMode = false;
            searchVisibleNodes.clear();
            matchedNodes.clear();
            rebuildVisibleList();
        }

        void rebuildVisibleList() {
            visibleNodes.clear();
            if (root != null) {
                appendVisible(root);
            }
            notifyDataSetChanged();
        }

        private void appendVisible(TreeNode node) {
            if (searchMode && !searchVisibleNodes.contains(node)) {
                return;
            }
            visibleNodes.add(node);
            if (searchMode) {
                for (TreeNode child : node.children) {
                    appendVisible(child);
                }
            } else if (node.expanded) {
                for (TreeNode child : node.children) {
                    appendVisible(child);
                }
            }
        }

        @Nullable
        Integer getSelectedPosition() {
            if (selected == null) {
                return null;
            }
            int index = visibleNodes.indexOf(selected);
            return index >= 0 ? index : null;
        }

        @NonNull
        @Override
        public TreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            TextView tv = new TextView(context);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tv.setSingleLine(false);
            tv.setPadding(dp(context, 8f), dp(context, 6f), dp(context, 8f), dp(context, 6f));
            return new TreeViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull TreeViewHolder holder, int position) {
            final TreeNode node = visibleNodes.get(position);
            holder.bind(node, node == selected, matchedNodes.contains(node), listener);
        }

        @Override
        public int getItemCount() {
            return visibleNodes.size();
        }

        static int dp(Context context, float dpValue) {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics()));
        }

        final class TreeViewHolder extends RecyclerView.ViewHolder {
            final TextView tv;

            TreeViewHolder(@NonNull View itemView) {
                super(itemView);
                tv = (TextView) itemView;
            }

            void bind(final TreeNode node, boolean selected, boolean matched, final OnNodeActionListener listener) {
                int indent = dp(tv.getContext(), Math.min(16f * node.depth, 360f));
                tv.setPadding(indent + dp(tv.getContext(), 8f), dp(tv.getContext(), 6f), dp(tv.getContext(), 8f), dp(tv.getContext(), 6f));
                String prefix;
                if (node.children.isEmpty()) {
                    prefix = "·";
                } else if (node.expanded) {
                    prefix = "[-]";
                } else {
                    prefix = "[+]";
                }
                tv.setText((matched ? "★ " : "") + prefix + " " + node.title);
                tv.setBackgroundColor(selected ? Color.parseColor("#993D5AFE") : Color.TRANSPARENT);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onNodeClick(node);
                        long now = SystemClock.uptimeMillis();
                        boolean isDoubleTap = node == lastClickedNode
                                && now - lastClickUptimeMs <= ViewConfiguration.getDoubleTapTimeout();
                        lastClickedNode = node;
                        lastClickUptimeMs = now;
                        if (isDoubleTap) {
                            listener.onNodeToggle(node);
                        }
                    }
                });
                tv.setOnLongClickListener(null);
            }
        }
    }

    private static final class WindowInspector {
        @Nullable
        static WindowEntry findTopWindowHit(int x, int y) {
            List<WindowEntry> windows = collectWindows();
            for (WindowEntry entry : windows) {
                Rect rect = new Rect();
                if (entry.root.getGlobalVisibleRect(rect) && rect.contains(x, y)) {
                    return entry;
                }
            }
            return null;
        }

        @Nullable
        static WindowEntry findWindowContainsView(@NonNull View view) {
            List<WindowEntry> windows = collectWindows();
            View root = view.getRootView();
            for (WindowEntry entry : windows) {
                if (entry.root == root) {
                    return entry;
                }
            }
            return null;
        }

        @NonNull
        static List<WindowEntry> collectWindows() {
            try {
                Class<?> clazz = Class.forName("android.view.WindowManagerGlobal");
                Method getInstance = clazz.getDeclaredMethod("getInstance");
                getInstance.setAccessible(true);
                Object global = getInstance.invoke(null);
                Field viewsField = clazz.getDeclaredField("mViews");
                viewsField.setAccessible(true);
                Field paramsField = clazz.getDeclaredField("mParams");
                paramsField.setAccessible(true);
                List<View> viewList = (List<View>) viewsField.get(global);
                List<WindowManager.LayoutParams> paramList = (List<WindowManager.LayoutParams>) paramsField.get(global);
                if (viewList == null || paramList == null) {
                    return Collections.emptyList();
                }
                List<WindowEntry> windows = new ArrayList<>();
                int count = Math.min(viewList.size(), paramList.size());
                for (int i = 0; i < count; i++) {
                    View root = viewList.get(i);
                    WindowManager.LayoutParams params = paramList.get(i);
                    if (root == null || params == null || root.getVisibility() != View.VISIBLE) {
                        continue;
                    }
                    WindowKind kind = classifyWindow(root, params);
                    windows.add(new WindowEntry(root, params, kind, i));
                }
                Collections.sort(windows, new Comparator<WindowEntry>() {
                    @Override
                    public int compare(WindowEntry o1, WindowEntry o2) {
                        if (o1.kind.priority != o2.kind.priority) {
                            return o2.kind.priority - o1.kind.priority;
                        }
                        return o2.index - o1.index;
                    }
                });
                return windows;
            } catch (Throwable ignored) {
                return Collections.emptyList();
            }
        }

        static WindowKind classifyWindow(@NonNull View root, @NonNull WindowManager.LayoutParams params) {
            int type = params.type;
            String className = root.getClass().getName();
            if (className.contains("PopupDecorView")
                    || type == WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                    || type == WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL) {
                return WindowKind.POPUP;
            }
            if (type == WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG || !containsActivityContext(root.getContext())) {
                return WindowKind.DIALOG;
            }
            return WindowKind.ACTIVITY;
        }

        static boolean containsActivityContext(@Nullable Context context) {
            Context current = context;
            int guard = 0;
            while (current != null && guard < 20) {
                if (current instanceof Activity) {
                    return true;
                }
                if (current instanceof ContextWrapper) {
                    current = ((ContextWrapper) current).getBaseContext();
                } else {
                    break;
                }
                guard++;
            }
            return false;
        }
    }

    private static final class ViewInspector {
        @NonNull
        static List<ViewHitInfo> collectHitViews(@NonNull View root, int x, int y, @NonNull WindowKind kind) {
            List<ViewHitInfo> list = new ArrayList<>();
            long[] orderSeed = new long[]{0L};
            collectHitViewsRecursive(root, x, y, 0, list, kind, orderSeed);
            Collections.sort(list, new Comparator<ViewHitInfo>() {
                @Override
                public int compare(ViewHitInfo o1, ViewHitInfo o2) {
                    if (o1.zOrder != o2.zOrder) {
                        return Long.compare(o2.zOrder, o1.zOrder);
                    }
                    if (o1.depth != o2.depth) {
                        return o2.depth - o1.depth;
                    }
                    long area1 = (long) Math.max(1, o1.rect.width()) * Math.max(1, o1.rect.height());
                    long area2 = (long) Math.max(1, o2.rect.width()) * Math.max(1, o2.rect.height());
                    if (area1 != area2) {
                        return Long.compare(area1, area2);
                    }
                    return o1.title.compareTo(o2.title);
                }
            });
            if (list.size() > 20) {
                return new ArrayList<>(list.subList(0, 20));
            }
            return list;
        }

        @NonNull
        static List<HitTreeNode> buildChildHitNodes(@NonNull HitTreeNode parentNode) {
            if (!(parentNode.view instanceof ViewGroup)) {
                return Collections.emptyList();
            }
            ViewGroup group = (ViewGroup) parentNode.view;
            int childCount = group.getChildCount();
            List<HitTreeNode> children = new ArrayList<>();
            for (int order = childCount - 1; order >= 0; order--) {
                int childIndex = getDrawingOrderIndex(group, childCount, order);
                View child = group.getChildAt(childIndex);
                if (child == null) {
                    continue;
                }
                HitTreeNode node = new HitTreeNode(child, parentNode.windowKind, parentNode.depth + 1);
                children.add(node);
            }
            return children;
        }

        private static void collectHitViewsRecursive(@NonNull View view, int x, int y, int depth, @NonNull List<ViewHitInfo> out, @NonNull WindowKind kind, @NonNull long[] orderSeed) {
            if (view.getVisibility() != View.VISIBLE || view.getAlpha() <= 0f) {
                return;
            }
            Rect rect = new Rect();
            if (!view.getGlobalVisibleRect(rect) || !rect.contains(x, y)) {
                return;
            }
            out.add(new ViewHitInfo(view, depth, rect, buildTitle(view, depth, rect, kind), kind, ++orderSeed[0]));
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                int childCount = group.getChildCount();
                for (int order = childCount - 1; order >= 0; order--) {
                    int childIndex = getDrawingOrderIndex(group, childCount, order);
                    View child = group.getChildAt(childIndex);
                    if (child != null) {
                        collectHitViewsRecursive(child, x, y, depth + 1, out, kind, orderSeed);
                    }
                }
            }
        }

        private static int getDrawingOrderIndex(@NonNull ViewGroup group, int childCount, int order) {
            if (!isChildrenDrawingOrderEnabled(group)) {
                return order;
            }
            try {
                Method method = ViewGroup.class.getDeclaredMethod("getChildDrawingOrder", int.class, int.class);
                method.setAccessible(true);
                Object result = method.invoke(group, childCount, order);
                if (result instanceof Integer) {
                    int index = (Integer) result;
                    if (index >= 0 && index < childCount) {
                        return index;
                    }
                }
            } catch (Throwable ignored) {
            }
            return order;
        }

        private static boolean isChildrenDrawingOrderEnabled(@NonNull ViewGroup group) {
            try {
                Method method = ViewGroup.class.getDeclaredMethod("isChildrenDrawingOrderEnabled");
                method.setAccessible(true);
                Object result = method.invoke(group);
                return result instanceof Boolean && (Boolean) result;
            } catch (Throwable ignored) {
                return false;
            }
        }

        @NonNull
        static TreeNode buildTree(@NonNull View root, int depth) {
            Rect rect = new Rect();
            root.getGlobalVisibleRect(rect);
            TreeNode node = new TreeNode(root, depth, rect, buildNodeTitle(root, rect));
            if (depth < 2) {
                node.expanded = true;
            }
            if (root instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) root;
                for (int i = 0; i < group.getChildCount(); i++) {
                    View child = group.getChildAt(i);
                    TreeNode childNode = buildTree(child, depth + 1);
                    childNode.parent = node;
                    node.children.add(childNode);
                }
            }
            return node;
        }

        @Nullable
        static TreeNode findNodeByView(@Nullable TreeNode node, @NonNull View target) {
            if (node == null) {
                return null;
            }
            if (node.view == target) {
                return node;
            }
            for (TreeNode child : node.children) {
                TreeNode result = findNodeByView(child, target);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        static void expandAncestors(@NonNull TreeNode node) {
            TreeNode current = node;
            while (current != null) {
                current.expanded = true;
                current = current.parent;
            }
        }

        @NonNull
        static List<TreeNode> searchNodes(@NonNull TreeNode root, @NonNull String query) {
            String normalized = query.trim().toLowerCase(Locale.ROOT);
            if (TextUtils.isEmpty(normalized)) {
                return Collections.emptyList();
            }
            List<TreeNode> result = new ArrayList<>();
            searchNodesRecursive(root, normalized, result);
            return result;
        }

        private static void searchNodesRecursive(@NonNull TreeNode node, @NonNull String normalizedQuery, @NonNull List<TreeNode> out) {
            if (isNodeMatched(node, normalizedQuery)) {
                out.add(node);
            }
            for (TreeNode child : node.children) {
                searchNodesRecursive(child, normalizedQuery, out);
            }
        }

        private static boolean isNodeMatched(@NonNull TreeNode node, @NonNull String normalizedQuery) {
            String className = node.view.getClass().getSimpleName();
            String fullClassName = node.view.getClass().getName();
            String idName = getIdName(node.view);
            return className.toLowerCase(Locale.ROOT).contains(normalizedQuery)
                    || fullClassName.toLowerCase(Locale.ROOT).contains(normalizedQuery)
                    || idName.toLowerCase(Locale.ROOT).contains(normalizedQuery);
        }

        @NonNull
        static String buildTitle(@NonNull View view, int depth, @NonNull Rect rect, @NonNull WindowKind kind) {
            String className = view.getClass().getSimpleName();
            if (TextUtils.isEmpty(className)) {
                className = view.getClass().getName();
            }
            return String.format(Locale.CHINA, "%s | %s | d=%d | [%d,%d,%d,%d]",
                    kind.name(), className, depth, rect.left, rect.top, rect.right, rect.bottom);
        }

        @NonNull
        static String buildNodeTitle(@NonNull View view, @NonNull Rect rect) {
            String className = view.getClass().getSimpleName();
            if (TextUtils.isEmpty(className)) {
                className = view.getClass().getName();
            }
            String idName = getIdName(view);
            return String.format(Locale.CHINA, "%s id=%s [%d,%d,%d,%d] vis=%d",
                    className, idName, rect.left, rect.top, rect.right, rect.bottom, view.getVisibility());
        }

        @NonNull
        static String getIdName(@NonNull View view) {
            int id = view.getId();
            if (id == View.NO_ID) {
                return "NO_ID";
            }
            try {
                return view.getResources().getResourceEntryName(id);
            } catch (Throwable ignored) {
                return "0x" + Integer.toHexString(id);
            }
        }

        @Nullable
        static Bitmap captureBitmap(@NonNull View view, int maxDimensionPx) {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width <= 0 || height <= 0) {
                return null;
            }
            float scale = 1f;
            int max = Math.max(width, height);
            if (max > maxDimensionPx) {
                scale = maxDimensionPx * 1f / max;
            }
            int bitmapW = Math.max(1, Math.round(width * scale));
            int bitmapH = Math.max(1, Math.round(height * scale));
            try {
                Bitmap bitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                if (scale != 1f) {
                    canvas.scale(scale, scale);
                }
                view.draw(canvas);
                return bitmap;
            } catch (OutOfMemoryError ignored) {
                return null;
            }
        }
    }
}
