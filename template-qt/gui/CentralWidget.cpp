#include "CentralWidget.h"
#include "ui_CentralWidget.h"
#include "TopWindow.h"
#include "SettingWidget.h"
#include "WidgetsShower.h"
#include "util/UiUtil.h"

#include <QDebug>
#include <QHash>
#include <QList>
#include <QButtonGroup>
#include <QMessageBox>

/*-----------------------------------------------------------------------------|
 |                            CentralWidgetPrivate                             |
 |----------------------------------------------------------------------------*/
class CentralWidgetPrivate {
public:
    CentralWidgetPrivate(CentralWidget *owner): owner(owner) {
        groupButtons = new QButtonGroup(owner);
        swithButtons = new QButtonGroup(owner);
        swithButtons->setExclusive(true);
    }

    CentralWidget *owner;
    QButtonGroup  *groupButtons; // 侧边栏的分组按钮
    QButtonGroup  *swithButtons; // 侧边栏切换界面的按钮
    QList<QAbstractButton *> itemButtons; // 侧边栏的所有 class 为 GroupItemButton 的按钮
    QHash<QAbstractButton *, QWidget *> buttonWidgetHash; // key 是侧边栏切换界面的按钮的指针，value 是右侧 widget 的指针
};

/*-----------------------------------------------------------------------------|
 |                                CentralWidget                                |
 |----------------------------------------------------------------------------*/
CentralWidget::CentralWidget(QWidget *parent) : QWidget(parent), ui(new Ui::CentralWidget), d(new CentralWidgetPrivate(this)) {
    initializeUi();
    handleEvents();
}

CentralWidget::~CentralWidget() {
    delete ui;
    delete d;
}

/**
 * 初始化界面
 */
void CentralWidget::initializeUi() {
    ui->setupUi(this);
    setAttribute(Qt::WA_StyledBackground);

    // 去掉窗口和侧边栏的 padding 和 margin
    UiUtil::setWidgetPaddingAndSpacing(this, 0, 0);
    UiUtil::setWidgetPaddingAndSpacing(ui->sideBarWidget, 0, 0);

    // 搜集处理侧边栏的按钮
    // 属性 class 为 GroupButton 的按钮放入 d->groupButtons，用来切换隐藏和显示 GroupItemButton
    // 属性 class 为 GroupItemButton 的按钮都放入 d->itemButtons，
    //    如果它的 action 属性不为 popup 则把它添加到一个 QButtonGroup d->swithButtons 中，它们的作用是用来切换界面的
    // 并把 QPushButton 设置为 flat 的效果
    QObjectList children = ui->sideBarWidget->children();
    foreach (QObject *child, children) {
        QAbstractButton *button = qobject_cast<QAbstractButton*>(child); // 可能是 QPushButton，也可能是 QToolButton
        QString className = child->property("class").toString();
        QString action    = child->property("action").toString();

        if (NULL == button) { continue; }

        if ("GroupButton" == className) {
            d->groupButtons->addButton(button);
        } else if ("GroupItemButton" == className) {
            d->itemButtons.append(button);
            button->hide();

            // 切换界面的按钮
            if ("popup" != action) {
                button->setCheckable(true);
                d->swithButtons->addButton(button);
            }
        }

        // 把 QPushButton 设置为 flat 的效果，这样 QSS 的效果更好
        QPushButton *pushButton = qobject_cast<QPushButton *>(button);
        if (NULL != pushButton) {
            pushButton->setFlat(true);
        }
    }
}

/**
 * 信号槽事件处理
 */
void CentralWidget::handleEvents() {
    // 点击侧边栏的分组按钮，隐藏其他分组的按钮，显示当前分组的按钮
    connect(d->groupButtons, QOverload<QAbstractButton *>::of(&QButtonGroup::buttonClicked), [this] (QAbstractButton *button) {
        foreach (QAbstractButton *itemButton, d->itemButtons) {
            QString groupName = button->property("groupName").toString();
            QString itemGroupName = itemButton->property("groupName").toString();

            if (itemGroupName == groupName) {
                itemButton->show();
            } else {
                itemButton->hide();
            }
        }
    });

    // 点击侧边栏切换界面的按钮，切换 widget
    // 1. 如果按钮对应的 widget 不存在则创建它
    // 2. 如果存在则显示出来
    connect(d->swithButtons, QOverload<QAbstractButton *>::of(&QButtonGroup::buttonClicked), [this] (QAbstractButton *button) {
        // [1] 创建
        if (!d->buttonWidgetHash.contains(button)) {
            createWidgetInContentStackedWidget(button);
        }

        // [2] 显示
        QWidget *targetWidget = d->buttonWidgetHash.value(button);
        UiUtil::setCurrentWidgetOfStackedWidget(targetWidget, ui->contentStackedWidget);
    });

    // TODO: 普通按钮的事件处理，这里只是为了演示
    connect(ui->itemButton8, &QPushButton::clicked, [this] {
        QMessageBox::aboutQt(this);
    });

    // TODO: 使用自定义无边框窗口显示弹出对话框，这里只是为了演示
    connect(ui->itemButton7, &QPushButton::clicked, [this] {
        TopWindow::message("<b>公司</b>: 花果山果汁科技信息技术有限公司<br>"
                           "<b>法人</b>: 齐天大圣<br>"
                           "<b>版本</b>: Release 1.1.3<br>"
                           "<center><img src=\":/image/top-window/logo.png\" width=64 height=64></center>");
    });

    // TODO: 显示第二个按钮对应的 widget，这里只是为了演示
    ui->groupButton1->click();
    ui->itemButton1->click();
}

/**
 * 创建需要在内容区 stacked widget 中显示的 widget
 * @param button 侧边栏切换界面的按钮
 */
void CentralWidget::createWidgetInContentStackedWidget(QAbstractButton *button) {
    // TODO: 创建 widget，需要根据实际的 widget 类来创建
    if (button == ui->itemButton1) {
        // [1] 创建 widget
        QWidget *w = new WidgetsShower();
        d->buttonWidgetHash.insert(ui->itemButton1, w);

        // [2] 添加 widget 到窗口中
        UiUtil::addWidgetIntoStackedWidget(w, ui->contentStackedWidget);
    } else if (button == ui->itemButton2) {
        QWidget *w = new QWidget();
        w->setStyleSheet("background: #80848f");
        d->buttonWidgetHash.insert(ui->itemButton2, w);

        UiUtil::addWidgetIntoStackedWidget(w, ui->contentStackedWidget);
    } else if (button == ui->itemButton3) {
        QWidget *w = new QWidget();
        w->setStyleSheet("background: #2d8cf0");
        d->buttonWidgetHash.insert(ui->itemButton3, w);

        UiUtil::addWidgetIntoStackedWidget(w, ui->contentStackedWidget);
    } else if (button == ui->itemButton4) {
        QWidget *w = new SettingWidget();
        d->buttonWidgetHash.insert(ui->itemButton4, w);
        UiUtil::addWidgetIntoStackedWidget(w, ui->contentStackedWidget, false, false, false, false);
    }
}







