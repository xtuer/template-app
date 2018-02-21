#include "DeviceWidget.h"
#include "ui_DeviceWidget.h"
#include "TopWindow.h"
#include "util/UiUtil.h"
#include "bean/Device.h"

#include <QDebug>
#include <QMenu>
#include <QMouseEvent>

class DeviceWidgetPrivate {
public:
    Device device;
    bool   checked = false;

    QAction *statusAction    = NULL; // 打开记录器状态界面 Action
    QAction *calibrateAction = NULL; // 打开记录器校准界面 Action
    QAction *versionAction   = NULL; // 打开记录器软件版本界面 Action
};

DeviceWidget::DeviceWidget(const Device &device, QWidget *parent) : QWidget(parent), ui(new Ui::DeviceWidget) {
    d = new DeviceWidgetPrivate();
    d->device = device;
    d->statusAction    = new QAction("记录器状态", this);
    d->calibrateAction = new QAction("记录器校准", this);
    d->versionAction   = new QAction("记录器软件版本", this);

    initializeUi();
    handleEvents();
}

DeviceWidget::~DeviceWidget() {
    delete ui;
}

void DeviceWidget::setId(const QString &id) {
    ui->idLabel->setText(id);
}

void DeviceWidget::setVoltage(int voltage) {
    d->device.voltage = voltage;

    // 设置电量级别，更新 QSS
    if (0 <= voltage && voltage <= 25) {
        ui->voltageLabel->setProperty("voltageLevel", "1");
    } else if (25 < voltage && voltage <= 50) {
        ui->voltageLabel->setProperty("voltageLevel", "2");
    } else if (50 < voltage && voltage <= 75) {
        ui->voltageLabel->setProperty("voltageLevel", "3");
    } else if (75 < voltage && voltage <= 100) {
        ui->voltageLabel->setProperty("voltageLevel", "4");
    }

    UiUtil::updateQss(ui->voltageLabel);
}

void DeviceWidget::setWorking(bool working) {
    d->device.working = working;

    // 更新样式
    ui->statusLabel->setProperty("working", working);
    UiUtil::updateQss(ui->statusLabel);
}

void DeviceWidget::setType(const QString &type) {
    if ("T" != type && "P" != type && "H" != type) {
        qDebug() << QString("设备类型不能为 %1").arg(type);
        return;
    }

    d->device.type = type;

    ui->typeLabel->setProperty("type", type);
    UiUtil::updateQss(ui->typeLabel);
}

void DeviceWidget::setAddress(const QString &address) {
    d->device.address = address;
}

void DeviceWidget::setChecked(bool checked) {
    d->checked = checked;

    this->setProperty("checked", checked);
    UiUtil::updateQss(this);
}

bool DeviceWidget::isChecked() const {
    return d->checked;
}

void DeviceWidget::setDevice(const Device &device) {
    setId(device.id);
    setType(device.type);
    setAddress(device.address);
    setVoltage(device.voltage);
    setWorking(device.working);
}

Device DeviceWidget::getDevice() const {
    return d->device;
}

// 点击鼠标左键选中和取消选中设备
void DeviceWidget::mousePressEvent(QMouseEvent *event) {
    if (event->button() == Qt::LeftButton) {
        setChecked(!isChecked());
    }
}

// 初始化界面
void DeviceWidget::initializeUi() {
    ui->setupUi(this);
    setAttribute(Qt::WA_StyledBackground);
    setContextMenuPolicy(Qt::CustomContextMenu);

    // 删除 label 上的辅助文字
    ui->statusLabel->setText("");
    ui->typeLabel->setText("");
    ui->idLabel->setText("");
    ui->voltageLabel->setText("");

    // 初始化设置对应 widget 的 property，使得样式生效
    setDevice(d->device);
}

void DeviceWidget::handleEvents() {
    // 弹出右键菜单
    connect(this, &QWidget::customContextMenuRequested, [this](const QPoint &pos) {
        QMenu* popMenu = new QMenu(this);
        popMenu->addAction(d->statusAction);
        popMenu->addAction(d->calibrateAction);
        popMenu->addAction(d->versionAction);
        popMenu->exec(mapToGlobal(pos));
    });

    connect(d->statusAction, &QAction::triggered, [this] {
        TopWindow::message("statusAction " + d->device.id);
    });

    connect(d->calibrateAction, &QAction::triggered, [this] {
        TopWindow::message("calibrateAction " + d->device.id);
    });

    connect(d->versionAction, &QAction::triggered, [this] {
        TopWindow::message("versionAction " + d->device.id);
    });
}
