你是android技术专家,现在设计一款debug工具,用于在android运行期间查看顶层activity
的window view树.
## 功能支持开启和关闭
### 开启

#### 开启后有三种状态
圆点获取坐标状态和坐标View列表状态和全屏View树状态
#### 圆点获取坐标状态
圆点获取坐标状态使用系统悬浮窗显示小点
手指可以自由拖动小点在屏幕中移动
#### 坐标View列表状态
手指抬起后1秒显示横向列表,列表为所在悬浮点根据坐标获取View树中对应View的信息.
列表会做排序越是内层ChildView越靠前,且childView区域一定要包含在悬浮小点坐标
Z轴堆叠（如 FrameLayout 或自定义 DrawingOrder）的情况，应当优先展示用户肉眼可见的最上层 View。
列表可以左右滑动,列表最多显示20条数据
视觉辅助：元素高亮 (Highlight)
功能： 在“坐标 View 列表”滑动时，屏幕上对应的 View 区域应同步出现半透明虚线边框。
默认选中列表左边第一条数据对应view高亮
##### 状态切换
提供返回按钮返回箭头← 和叉号X关闭按钮
点击返回箭头返回到 圆点获取坐标状态
点击X号直接进入viewtree功能关闭状态,隐藏悬浮窗


#### 全屏View树状态
点击列表中item进入全屏悬浮窗页面,不要使用Activity页面承载,全屏悬浮窗布局宽高和屏幕大小相同,
##### 全屏悬浮窗显示完整View树结构
全屏悬浮窗显示完整View树结构,自动滚动到传递目标View,并展开对应View树,显示对应View信息

全屏View树可以自由双击展开折叠View树
全屏View树可以自由上下滑动和左右滑动
##### View树元素点击
点开某个View可以查看详细的属性,且支持查看view的截图,(使用canvas绘制bitmap,获取bitmap进行显示)
搜索与过滤
功能： 全屏 View 树状态下，支持通过 ID 字符串或 类名关键字 进行搜索，自动定位并展开对应节点。对于包含数百个节点的复杂页面，这能节省大量滚动时间。


基本信息： ID (R.id.tv_title对应的tv_title)、ClassName、visiable/gone/invisiable、Alpha。
depth
rect
size
visible


布局信息： Margin、Padding、MeasuredWidth/Height、X/Y在屏幕坐标、Weight。srollx/scrolly
scaleX/scaleY
Rotatex/y/z
TranslationX/Y
left/top/right/bottom

关键状态： isClickable、isFocused、enable、,

自定义属性： 如果是 TextView，显示 TextSize、TextColor、Text 内容。
如果是ImageView,查看是否含有glide图片加载库对应tag信息
View的foreground,background

##### View树元素点击后屏幕上显示对应view虚线边框
在屏幕中正确位置显示view虚线边框
显示位置在view树背景下层
同时适当增加View树背景透明度


##### 视觉辅助：元素高亮 (Highlight)
功能： 在“全屏 View 树”选中某个节点时，屏幕上对应的 View 区域应同步出现半透明虚线边框。

##### view树支持双击展开和折叠
对于 ViewGroup 元素，如果包含 childView，用户双击该元素可展开并显示所有 child 元素。
对于已展开的 ViewGroup 元素，用户再次双击该元素可折叠并隐藏其所有 childView。


## 对应activity 页面onPause、onDestroy对三种状态影响
- 页面onPause、onDestroy圆点获取坐标状态：不影响
- 坐标View列表状态：页面onPause、onDestroy,返回圆点获取坐标状态
- 全屏View树状态：页面onPause、onDestroy,返回圆点获取坐标状态

## 关闭
隐藏系统悬浮窗
## window view树
支持以下三类 Window：
	•	Activity（DecorView）
	•	Dialog
	•	PopupWindow
Window 层级规则（Z-order）
PopupWindow > Dialog > Activity

从顶层 Window → 向下遍历
命中第一个包含 (x, y) 的 Window 即停止	
- 坐标统一使用屏幕坐标系进行命中计算，避免 Dialog/PopupWindow 的窗口偏移导致误判
- 调试工具自身悬浮窗会从 Window 扫描中过滤，避免命中到调试面板而非业务 Window

## 支持扩展接口供外部开发者注册注入信息
- 新增可扩展接口 DetailInfoProvider ，供外部开发者注册注入：
- 新增多注册能力（去重）、注销、清空注册：
  - registerDetailInfoProvider(...)
  - unregisterDetailInfoProvider(...)
  - clearDetailInfoProviders(...)
- 在 bindNodeDetail 中调用所有已注册接口，收集返回的 String 并展示在“【扩展信息】”区块：
- 支持注册多个 provider，按注册顺序拼接展示。


## 代码库自动初始化

ViewTreeDebugProvider 继承ContentProvider
ContentProvider 中onCreate 方法中进行初始化
AndroidManifest.xml中注册ViewTreeDebugProvider



## 性能
及时回收Bitmap，避免内存泄漏。
