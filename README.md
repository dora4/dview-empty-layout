dview-empty-layout
![Release](https://jitpack.io/v/dora4/dview-empty-layout.svg)
--------------------------------
#### 卡片

![DORA视图 空态布局](https://github.com/user-attachments/assets/5c7cf793-ee58-4c39-912f-3c3dd52cb6bc)

##### 卡名：Dora视图 EmptyLayout
###### 卡片类型：调整怪兽
###### 属性：暗
###### 星级：1
###### 种族：魔法师族
###### 攻击力/防御力：100/1000
###### 效果：此卡不会因为对方卡的效果而破坏，并可使其无效化。此卡攻击里侧守备表示的怪兽时，若攻击力高于其守备力，则给予对方此卡原攻击力的伤害，并抽一张卡。

#### Gradle依赖配置

```groovy
// 添加以下代码到项目根目录下的build.gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
// 添加以下代码到app模块的build.gradle
dependencies {
    implementation 'com.github.dora4:dview-empty-layout:1.12'
}
```
