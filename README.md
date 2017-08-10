


## SlideSwitch 切换选项卡 可配合ViewPage使用  支持ListAdapter配置
## BannerSwitch 轮播广告横幅  支持ListAdapter配置
## PowSwitch 切换开关 


Add Gradle dependency:
```gradle
dependencies {
   compile 'com.github.powyin:switch:3.0.0'
}
```

###  UI

|SlideSwitch(多项Item)|SlideSwitch(少量Item)|PowSwtch(开关)|
|---|---|----
|![github](https://github.com/powyin/slide/blob/master/app/src/main/res/raw/slide_m.gif)|![github](https://github.com/powyin/slide/blob/master/app/src/main/res/raw/slide_s.gif)|![github](https://github.com/powyin/slide/blob/master/app/src/main/res/raw/slide_x.gif)|


|BannerSwitch(单条)|BannerSwitch(多条)|BannerSwitch(自由)|
|---|---|----
|![github](https://github.com/powyin/slide/blob/master/app/src/main/res/raw/banner_1.gif)|![github](https://github.com/powyin/slide/blob/master/app/src/main/res/raw/banner_3.gif)|![github](https://github.com/powyin/slide/blob/master/app/src/main/res/raw/banner_n.gif)|


### how to use  SwipeRefresh

     
     配置ListAdapter 或者 直接布局
     <com.powyin.slide.widget.SlideSwitch
        android:id="@+id/test_switch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:pow_show_over_scroll="true"          //是否显示过度拉升效果
        app:pow_checked_bac="@drawable/"         //底下状态栏背景图片
        app:pow_checked_drawable="@drawable/"    //底下状态栏选中者样式
        app:pow_checked_hei="3dp"                //底下状态栏高度
        app:pow_fixed_item="-1"                  //此值控制如何平方宽度到子View； 默认为-1 测量子View大小 不平分空间
        >

        <TextView
            android:background="#6f00ff00"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:minWidth="70dp"
            android:textSize="14sp"
            android:text="音乐"
            android:textColor="#ff656565"
            android:gravity="center" />

        <TextView
            android:background="#7700bbff"
            android:layout_width="wrap_content"
            android:layout_height="40dp"
            android:minWidth="70dp"
            android:textSize="14sp"
            android:text="视频"
            android:textColor="#ff656565"
            android:gravity="center"/>
            
            。。。。。。。。。。。。。。。。
    </com.powyin.slide.widget.SlideSwitch>


### how to use  BannerSwitch

     
       配置ListAdapter 或者 直接布局
       <com.powyin.slide.widget.BannerSwitch
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:pow_switch_auto_ennable="true"            // 是否自动播放
            app:pow_switch_fixed_item="1"                 // 一次播放数量
            app:pow_switch_period="2500"                  // 自动播放间隔
            app:pow_switch_animation_period="350"         // 切换内容动画时间
            android:id="@+id/my_banner" >


            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                >
                <ImageView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:scaleType="centerCrop"
                    android:src="@drawable/a" />
                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:text="0"
                    android:textColor="#00d9fa"
                    android:textSize="15sp" />
            </FrameLayout>

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent">
                <ImageView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:scaleType="centerCrop"
                    android:src="@drawable/b" />
                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:gravity="center"
                    android:text="1"
                    android:textColor="#00d9fa"
                    android:textSize="15sp" />
            </FrameLayout>
            。。。。。。。。。。。。。。
      </com.powyin.slide.widget.BannerSwitch>

### how to use  PowSwitch

     
      <com.powyin.slide.widget.PowSwitch
        android:layout_width="50dp"
        android:layout_height="30dp"
        android:padding="5dp"
        app:pow_switch_bac_off="@drawable/type_1_switch_bac_off"              //关闭状态下背景图
        app:pow_switch_bac_on="@drawable/type_1_switch_bac_on"                //开启状态下背景图
        app:pow_switch_icon_off="@drawable/type_1_switch_icon_off"            //关闭状态下显示图标
        app:pow_switch_icon_on="@drawable/type_1_switch_icon_on"              //开启状态下显示图标
      />
        


### 优点
```

  SlideSwitch切换条自动滚动中央位置
  SlideSwitch切换条可以过度滚动；
  SlideSwitch切换条可以设置固定最大条目数在(平方布局空间)； 默认为-1 自动计算子View大小(不平方布局空间)；
  
  BannerSwitch 实现无限轮播  可以定义一次载入数量
  
  PowSwitch 可以自定义开关样式
  
```

### contact me
```
  QQ 1217881964
```





