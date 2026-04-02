# AndroidUiMode v2.x
让日夜间模式实现起来更简单

#### 最新版本
https://bintray.com/a-liya/maven/android-uimode/_latestVersion

#### 简介
v3.x Migrate to Androidx

为了方便升级过度，v2.x目前完全兼容v1.x的方案；v2.x是结合官方v7包日夜主题实现的(参考AppCompatDelegate.NightMode)。  

src/main/res目录文件夹规则：
```
res
|____color
|____color-night
|____drawable
|____drawable-night
|____layout
|____mipmap-night-xxhdpi
|____mipmap-xxhdpi
|____values
|____values-night
```

### 一、添加依赖

```
dependencies {
    implementation 'com.wogoo.android:uimode:1.0.5'
    implementation 'androidx.appcompat:appcompat:x.x.x'
}
```

### 二、代码配置，具体用法参考示例 [app](app)
* 初始化 Application#onCreate(); 参考示例代码 [AppUiMode.init(context)]([AppUiMode.java](app/src/main/java/com/aliya/uimode/sample/AppUiMode.java))

```java
  UiModeManager.init(sContext, new LayoutInflater.Factory2() {
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        // 此处可自定义拦截创建View
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }
});
        UiModeManager.addFactory2(new BackgroundFactory());
        UiModeManager.setDefaultUiMode(_get().uiMode);
```

* BaseActivity#onCreate(Bundle);
```
protected void onCreate(Bundle savedInstanceState) {
   // LayoutInflaterCompat.setFactory2(getLayoutInflater(), UiModeManager.INSTANCE.obtainInflaterFactory());
    UiModeManager.INSTANCE.setFactory2(getLayoutInflater());
    super.onCreate(savedInstanceState);
}
```
* 实现日夜模式切换的Activity必须是AppCompatActivity的子类
```java
public class BaseActivity extends AppCompatActivity {
}
```
* 定义拓展类型
```
class BackgroundViewWidget : ViewWidget() {


    /**
     * 自定义属性
     * background
     * background_button_drawable
     * background_multi_selector
     * background_multi_selector_text
     * background_press
     * background_selector
     * bl_anim
     * bl_other
     * bl_text
     * text_selector
     *
     */
    override fun onRegisterStyleable() {
        super.onRegisterStyleable()

        registerCustomAttrArray(com.noober.background.R.styleable.background);
        registerCustomAttrArray(com.noober.background.R.styleable.background_press);
        registerCustomAttrArray(com.noober.background.R.styleable.background_selector);
        registerCustomAttrArray(com.noober.background.R.styleable.text_selector);
        registerCustomAttrArray(com.noober.background.R.styleable.background_button_drawable);
        registerCustomAttrArray(com.noober.background.R.styleable.bl_other);
        registerCustomAttrArray(com.noober.background.R.styleable.bl_anim);
        registerCustomAttrArray(com.noober.background.R.styleable.background_multi_selector);
        registerCustomAttrArray(com.noober.background.R.styleable.background_multi_selector_text);
        registerCustomAttrArray(com.noober.background.R.styleable.bl_text);
    }

    override fun onApplyCustom(v: View, typedArrayMap: Map<IntArray, CachedTypedValueArray>) {
        super.onApplyCustom(v, typedArrayMap)
        BackgroundTypedArrayDelegate.setViewBackground(
            v.context,
            typedArrayMap as MutableMap<IntArray, CachedTypedValueArray>,
            v
        )

    }
}
```

* 注册类型
```java
  WidgetRegister.put(View::class.java,BackgroundViewWidget())

```

* AndroidManifest.xml 配置 configChanges="uiMode", 不然会调用Activity#recreate()
```
<activity
    android:name=".xx"
    android:configChanges="uiMode" />
```

### 日夜间切换支持的属性
#### 完整支持com.noober.background的所有属性

在layout文件定义背景库支持的自定义背景后,会根据uiMode切换动态重新创建背景,实现日夜模式切换。

#### 所有类型View都支持的属性
支持日夜间切换
透明度
背景
前景
tint
tintMode
调用invalidate()方法

```xml
    <declare-styleable name="UiModeView">
        <attr name="view_invalidate" format="boolean" />
        <!--uiMode忽略的属性，为空忽略所有属性,多个属性时属性名之间用'|'分割-->
        <!--忽略所有属性   例app:uiMode_ignore="" -->
        <!--忽略textColor   例app:uiMode_ignore=“textColor”  -->
        <!--忽略textColor和drawableLeft   例app:uiMode_ignore=“textColor|drawableLeft”  -->
        <attr name="uiMode_ignore" format="string" />

        <attr name="android:foreground" />
        <attr name="android:theme" />
    </declare-styleable>

    <declare-styleable name="ViewBackgroundHelper">
        <attr name="android:background"/>
        
        <attr format="color" name="backgroundTint"/>

        
        <attr name="backgroundTintMode">
            <!-- The tint is drawn on top of the drawable.
                 [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc] -->
            <enum name="src_over" value="3"/>
            <!-- The tint is masked by the alpha channel of the drawable. The drawable’s
                 color channels are thrown out. [Sa * Da, Sc * Da] -->
            <enum name="src_in" value="5"/>
            <!-- The tint is drawn above the drawable, but with the drawable’s alpha
                 channel masking the result. [Da, Sc * Da + (1 - Sa) * Dc] -->
            <enum name="src_atop" value="9"/>
            <!-- Multiplies the color and alpha channels of the drawable with those of
                 the tint. [Sa * Da, Sc * Dc] -->
            <enum name="multiply" value="14"/>
            <!-- [Sa + Da - Sa * Da, Sc + Dc - Sc * Dc] -->
            <enum name="screen" value="15"/>
            <!-- Combines the tint and icon color and alpha channels, clamping the
                 result to valid color values. Saturate(S + D) -->
            <enum name="add" value="16"/>
        </attr>
    </declare-styleable>



    <declare-styleable name="UiModeViewEffectsStyle">
        <!--   background foreground imageDrawable compoundDrawable mutate    -->
        <attr name="mutate_drawable" format="boolean" />
        <!-- 设置颜色过滤器 -->
        <attr name="drawable_colorFilter" format="color|reference" />
        <!-- 颜色过滤器的混合模式，默认值为 src_in -->
        <attr name="drawable_colorFilterMode" format="enum">
            <enum name="clear" value="0" />
            <enum name="src" value="1" />
            <enum name="dst" value="2" />
            <enum name="src_over" value="3" />
            <enum name="dst_over" value="4" />
            <enum name="src_in" value="5" />
            <enum name="dst_in" value="6" />
            <enum name="src_out" value="7" />
            <enum name="dst_out" value="8" />
            <enum name="src_atop" value="9" />
            <enum name="dst_atop" value="10" />
            <enum name="xor" value="11" />
            <enum name="darken" value="12" />
            <enum name="lighten" value="13" />
            <enum name="multiply" value="14" />
            <enum name="screen" value="15" />
            <enum name="add" value="16" />
            <enum name="overlay" value="17" />
        </attr>
        <attr name="view_alpha" format="reference" />
    </declare-styleable>

```

#### TextView 支持属性
```xml
  <declare-styleable name="TextViewHelper">
        <attr name="android:textCursorDrawable" />
        <attr name="android:drawableLeft" />
        <attr name="android:drawableTop" />
        <attr name="android:drawableRight" />
        <attr name="android:drawableBottom" />
        <attr name="android:drawableStart" />
        <attr name="android:drawableEnd" />
        <attr name="android:textAppearance" />
        <attr name="android:textColor" />
        <attr name="android:textColorHint" />
        <attr name="android:textColorLink" />
        <attr name="android:textColorHighlight" />

        <attr format="reference" name="drawableLeftCompat" />
        <attr format="reference" name="drawableTopCompat" />
        <attr format="reference" name="drawableRightCompat" />
        <attr format="reference" name="drawableBottomCompat" />
        <attr format="reference" name="drawableStartCompat" />
        <attr format="reference" name="drawableEndCompat" />
        <!-- Tint to apply to the compound (left, top, etc.) drawables. -->
        <attr format="color" name="drawableTint" />

    </declare-styleable>
 ```
 #### ImageView 支持属性
 ```xml
  <declare-styleable name="AppCompatImageView">
        <attr name="android:src"/>
        <attr format="reference" name="srcCompat"/>
        <attr format="color" name="tint"/>
        <attr name="tintMode">
            <!-- The tint is drawn on top of the drawable.
                 [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc] -->
            <enum name="src_over" value="3"/>
            <!-- The tint is masked by the alpha channel of the drawable. The drawable’s
                 color channels are thrown out. [Sa * Da, Sc * Da] -->
            <enum name="src_in" value="5"/>
            <!-- The tint is drawn above the drawable, but with the drawable’s alpha
                 channel masking the result. [Da, Sc * Da + (1 - Sa) * Dc] -->
            <enum name="src_atop" value="9"/>
            <!-- Multiplies the color and alpha channels of the drawable with those of
                 the tint. [Sa * Da, Sc * Dc] -->
            <enum name="multiply" value="14"/>
            <!-- [Sa + Da - Sa * Da, Sc + Dc - Sc * Dc] -->
            <enum name="screen" value="15"/>
            <!-- Combines the tint and icon color and alpha channels, clamping the
                 result to valid color values. Saturate(S + D) -->
            <enum name="add" value="16"/>
        </attr>
    </declare-styleable>
 ```  
 #### ProgressBar支持属性
 ```xml
  <declare-styleable name="ProgressBarHelper">
        <attr name="android:progressDrawable" />
   </declare-styleable>      
 ```

 #### SeekBar 支持属性
 ```xml
       <declare-styleable name="SeekBarHelper">
        <attr name="android:thumb" />
    </declare-styleable>
 ```

 #### Toolbar 支持属性
 ```xml
    <declare-styleable name="ToolbarHelper">
        <attr name="android:logo" />
        <attr name="android:navigationIcon" />

        <attr name="android:subtitleTextColor" />

        <attr name="android:titleTextColor" />

    </declare-styleable>
 ```
 #### CompoundButton 支持属性
 ```xml
    <declare-styleable name="ButtonHelper">
        <attr name="android:buttonTint" />
        <attr name="android:buttonTintMode" />
        <attr name="android:button" />
    </declare-styleable>
 ```
#### ListView 和LinearLayout 支持属性
 ```xml
    <declare-styleable name="DividerHelper">
        <attr name="android:divider" />
    </declare-styleable>
 ```


### 三、ImageView夜间模式用法

* 1. `<ImageView/>` 夜间模式有默认值，若想修改分别配置  

> res  
  |____values  
  |&nbsp;&nbsp;&nbsp;|____colors.xml

```xml
<resources>
    <color name="uiMode_maskColor">@android:color/transparent</color>
</resources>
```
      
> res  
  |____values-night  
  |&nbsp;&nbsp;&nbsp;|____colors.xml
```xml
<resources>
    <color name="uiMode_maskColor">#7f000000</color>
</resources>
```

* 2. 通过app:maskColor自定义属性配置遮罩颜色

```
<ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:maskColor="@color/ic_color" />
        
去掉遮罩
app:maskColor="@android:color/transparent"

```

### 四、uiMode_ignore属性

属性声明
```xml
<declare-styleable name="UiMode">
    <!--uiMode忽略的属性，多个属性时属性名之间用'|'分割-->
    <attr name="uiMode_ignore" format="string" />
</declare-styleable>

<!--忽略所有属性   例app:uiMode_ignore="" -->
<ImageView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:uiMode_ignore="" />
<!--忽略textColor   例app:uiMode_ignore=“textColor”  -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Hello World"
    android:textColor="@color/text_color"
    app:uiMode_ignore="textColor" />
<!--忽略background   例app:uiMode_ignore=“background”  -->
<View
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/bg_color"
    app:uiMode_ignore="background" />

<!--忽略textColor和drawableLeft   例app:uiMode_ignore=“textColor|drawableLeft”  -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Hello World"
    android:textColor="@color/text_color"
    android:drawableLeft="@drawable/ic_nav_bar_back_dark"
    app:uiMode_ignore="textColor|drawableLeft" />
```

如下配置，当日夜间模式切换时会忽略src属性
```xml
<ImageView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginRight="15dp"
    android:src="@mipmap/ic_nav_bar_back_dark"
    app:uiMode_ignore="src" />
```

### 五、invalidate属性
```xml
<declare-styleable name="UiMode">
    <attr name="invalidate" format="boolean" />
</declare-styleable>
```

`app:invalidate="true"` 表示日、夜间模式切换会调用对应View.invalidate()来刷新

> 场景：RecyclerView的分割线，当日、夜间模式切换时，RecyclerView不刷新分割线的颜色就不会变化

### 六、MaskImageView功能

#### 6.1 圆角相关
* 属性声明
```xml
<declare-styleable name="Round">
    <attr name="radius" format="dimension" />
    <attr name="radius_leftTop" format="dimension" />
    <attr name="radius_leftBottom" format="dimension" />
    <attr name="radius_rightTop" format="dimension" />
    <attr name="radius_rightBottom" format="dimension" />
    <attr name="radius_oval" format="boolean" />
    <attr name="border_width" format="dimension" />
    <attr name="border_color" format="color" />
</declare-styleable>
```

* 实现四个圆角半径均为5dp，xml代码如下
```xml
<ImageView
    android:layout_width="50dp"
    android:layout_height="50dp"
    app:radius="5dp" />
```

* 实现四个圆角分别为5dp、6dp、7dp、8dp，xml代码如下
```xml
<ImageView
    android:layout_width="50dp"
    android:layout_height="50dp"
    app:radius_leftTop="5dp"
    app:radius_rightTop="6dp"
    app:radius_rightBottom="7dp"
    app:radius_leftBottom="8dp" />
```

* 实现裁剪成椭圆，当宽高相等时即为圆
```xml
<ImageView
    android:layout_width="100dp"
    android:layout_height="50dp"
    app:radius_oval="true" />
```

* 实现裁剪成圆，并添加边框
```xml
<ImageView
    android:layout_width="50dp"
    android:layout_height="50dp"
    app:border_color="@color/color_border_color"
    app:border_width="2dp"
    app:radius_oval="true" />
```

#### 6.2 固定宽高比属性

* 属性声明
```xml
<declare-styleable name="RatioLayout">
    <attr name="ratio_w2h" format="string" />
</declare-styleable>
```

* 实现宽高比为1:1，注意：宽、高
```xml
<ImageView
    android:layout_width="100dp"
    android:layout_height="wrap_content"
    app:ratio_w2h="1:1" />
```

#### 6.3 夜间模式遮罩规则
* 属性声明
```xml
<declare-styleable name="MaskImageView">
    <attr name="maskColor" format="color" />
    <!--false:遮罩层与原始图片取并集; true:取交集; 默认值false -->
    <attr name="maskUnion" format="boolean" />
</declare-styleable>
```

* xml配置，默认为false
```xml
<ImageView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:maskUnion="true" />
```

### 六、colorFilter

支持对所有View的background foreground
TextView 所有drawable leftDrawable,topDrawable,rightDrawable,bottomDrawable
ImageView
设置colorFilter



```xml
<View
    android:layout_width="30dp"
    android:layout_height="30dp"
    android:background="@drawable/ic_nav_bar_back_dark"
    app:drawable_colorFilter="@color/_dddddd_343434"
    app:drawable_colorFilterMode="src_in"
/>


<TextView
android:layout_width="30dp"
android:layout_height="30dp"
android:leftDrawable="@drawable/ic_nav_bar_back_dark"
app:drawable_colorFilter="@color/_dddddd_343434"
app:drawable_colorFilterMode="src_in"
/>
```


### 七、自定义View日夜模式切换的实现

* 实现接口UiModeChangeListener，模版如下
```java
public class CustomView extends View implements UiModeChangeListener {
    
     @Override
     public void onUiModeChange() {
        // UiMode切换，在此处刷新属性
     }
    
}

```
* 参考自定义控件[MaskImageView](uimode/src/main/java/com/aliya/uimode/widget/MaskImageView.java)

### 对特定View类型的创建和日夜模式切换的监听和统一处理

```java
  UiModeManager.INSTANCE.registerViewCreateUiModeChanged(RichEditText.class, new OnViewCreateUiModeChanged<RichEditText>(
        ) {
            @Override
            public void onCreate(@NonNull RichEditText richEditText) {

            }

            @Override
            public void onChanged(@NonNull RichEditText richEditText) {
                if(richEditText.isResetWhenUiModeChanged()){
                    try {
                        int last = richEditText.getSelectionEnd();
                        int start = richEditText.getSelectionStart();
                        richEditText.setText(richEditText.getText().toString());
                        richEditText.setSelection(last, start);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

```

### 八、其他相关用法

#### 8.1 Activity监听UiMode切换

Activity实现接口UiModeChangeListener
```java
public class MainActivity extends AppCompatActivity implements UiModeChangeListener {
    
     @Override
     public void onUiModeChange() {
        // UiMode切换在此回调
     }
     
}

```

#### 注册UiModeChangeListener

在Fragment中可以调用以下方法注册UiModeChangeListener
 UiModeManager.registerUiModeChangeListener(
        lifecycleOwner: LifecycleOwner,
        listener: UiModeChangeListener
    )
```java
    class CustomFragment : Fragment(), UiModeChangeListener {
   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /**
         * 在fragment,onViewCreated中注册
         * 注册 UiModeChangeListener
         * 根据监听者生命周期，自动取消注册
         */
        UiModeManager.INSTANCE.registerUiModeChangeListener(this.viewLifecycleOwner, this)
    }
    override fun onUiModeChange() {
        // UiMode切换，在此处刷新属性
    }
   }
   
```


### 特别说明
如果在layout文件中配置日夜间颜色和drawable tint等
在运行时由于状态变化需要改变颜色，drawable tint属性的,
为避免日夜切换后被覆盖,使用下面两种方案处理,优先使用方案二
方案一
在layout文件中忽略对应属性,从而避免日夜间切换修改对应属性.

app:uiMode_ignore="textColor"
app:uiMode_ignore="src"
app:uiMode_ignore="srcCompat"
app:uiMode_ignore="background"

此方案丧失了日夜间切换功能
可在onUiModeChange中根据状态修改对应颜色和drawable tint
方案二
创建自定义属性，并调用saveViewValue方法保存属性resourceId,内部会应用属性值,并在下次日夜切换自动更改对应颜色、drawable

```kotlin
    fun saveViewValueTextColor(view: TextView, @ColorRes color: Int) {
        saveViewValue(
            view, R.styleable.TextViewHelper, 
            R.styleable.TextViewHelper_android_textColor,
            color)
    }

    fun saveViewValueTextDrawableTint(view: TextView, @ColorRes color: Int) {
        saveViewValue(
            view,
            R.styleable.TextViewHelper,
            R.styleable.TextViewHelper_drawableTint,
            color
        )
    }

    fun saveViewValueBackground(view: View, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.ViewBackgroundHelper,
            androidx.appcompat.R.styleable.ViewBackgroundHelper_android_background,
            res
        )
    }

    fun saveViewValueBackgroundTint(view: View, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.ViewBackgroundHelper,
            androidx.appcompat.R.styleable.ViewBackgroundHelper_backgroundTint,
            res
        )
    }

    fun saveViewValueImageSrc(view: ImageView, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.AppCompatImageView,
            androidx.appcompat.R.styleable.AppCompatImageView_android_src,
            res
        )
    }

    fun saveViewValueImageTint(view: ImageView, @AnyRes res: Int) {
        saveViewValue(
            view,
            androidx.appcompat.R.styleable.AppCompatImageView,
            androidx.appcompat.R.styleable.AppCompatImageView_tint,
            res
        )
    }
```


### 九、已知问题

#### 9.1 遮罩问题

当 drawable 通过xml定义旋转90度，且原图是长方形时，应用在 : ImageView - android:src，TextView - android:drawableTop 等属性，存在被裁剪的问题。
```
<rotate xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@mipmap/ic_arrow_bottom"
    android:fromDegrees="90"
    android:pivotX="50%"
    android:pivotY="50%"
    android:toDegrees="90" />
```

### 十、UML

![avatar](/doc/AndroidUiMode_UML.png)