#include "CentralWidget.h"
#include "ui_CentralWidget.h"
#include "util/UiUtil.h"
#include "MagicWindow/MagicWindow.h"

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
        swithButtons = new QButtonGroup(owner);
        swithButtons->setExclusive(true);
    }

    CentralWidget *owner;
    QButtonGroup  *swithButtons; // 侧边栏切换界面的按钮
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

    // 去掉窗口和侧边栏的 padding 和 margin
    UiUtil::setWidgetPaddingAndSpacing(this, 0, 0);
    UiUtil::setWidgetPaddingAndSpacing(ui->sideBarWidget, 0, 0);

    // 侧边栏中 class 为 GroupItemButton，但 action 属性不为 popup 的按钮加到一个 QButtonGroup 里，这样同时只有一个能够被选中
    QObjectList children = ui->sideBarWidget->children();
    foreach (QObject *child, children) {
        if ("GroupItemButton" == child->property("class").toString() && "popup" != child->property("action").toString()) {
            QAbstractButton *button = qobject_cast<QAbstractButton*>(child);
            button->setCheckable(true);
            d->swithButtons->addButton(button);
        }
    }
}

/**
 * 信号槽事件处理
 */
void CentralWidget::handleEvents() {
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

    // TODO: 普通按钮的事件处理
    connect(ui->itemButton8, &QPushButton::clicked, [this] {
        QMessageBox::aboutQt(this);
    });

    // TODO: 使用自定义无边框窗口显示弹出对话框
    connect(ui->itemButton7, &QPushButton::clicked, [this] {
        QWidget *centralWidget2 = new QWidget();
        centralWidget2->setStyleSheet("background: #AAA;");

        // showModal() 显示为模态对话框，并且使用了自定义边框
        MagicWindow *dialog = new MagicWindow(centralWidget2, QMargins(4,4,4,4), QMargins(8,8,8,8), ":/image/MagicWindow/colorful-border.png", true);
        dialog->setTitle("模态对话框");
        dialog->setResizable(false);
        dialog->setAttribute(Qt::WA_DeleteOnClose);
        dialog->showModal();
    });

    // TODO: 显示第二个按钮对应的 widget
    ui->itemButton2->click();
}

/**
 * 创建需要在内容区 stacked widget 中显示的 widget
 * @param button 侧边栏切换界面的按钮
 */
void CentralWidget::createWidgetInContentStackedWidget(QAbstractButton *button) {
    // TODO: 创建 widget
    if (button == ui->itemButton1) {
        // [1] 创建 widget
        QWidget *w = new QWidget();
        w->setStyleSheet("background: #bbbec4");
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
    }
}
