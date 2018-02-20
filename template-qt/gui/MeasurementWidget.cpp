#include "MeasurementWidget.h"
#include "ui_MeasurementWidget.h"
#include "DeviceWidget.h"
#include "bean/Device.h"
#include "TopWindow.h"
#include "util/UiUtil.h"

#include <QDebug>
#include <QVBoxLayout>

MeasurementWidget::MeasurementWidget(QWidget *parent) : QWidget(parent), ui(new Ui::MeasurementWidget) {
    initializeUi();
    handleEvents();

    // TODO: 测试 Ui，增加设备
    QStringList types = QStringList() << "T" << "P" << "H";

    for (int i = 0; i < 100; ++i) {
        QString id = QString("%1 - %2").arg(i).arg(1000000 + qrand() % 100000);
        QString type = types.at(qrand() % 3);
        addDevice(Device(id, type, "", qrand() % 101, qrand() % 2 == 0));
    }
}

MeasurementWidget::~MeasurementWidget() {
    delete ui;
}

// 添加设备
void MeasurementWidget::addDevice(const Device &device) {
    DeviceWidget *sw = new DeviceWidget(device);
    QVBoxLayout *l = qobject_cast<QVBoxLayout *>(devicesWidget->layout());
    l->insertWidget(l->count() - 1, sw);
}

// 获取选中的设备
QList<Device> MeasurementWidget::getSelectedDevices() const {
    QList<Device> devices;
    QList<DeviceWidget *> deviceWidgets = getSelectedDeviceWidgets();

    foreach(DeviceWidget *sw, deviceWidgets) {
        if (sw->isChecked()) {
            devices.append(sw->getDevice());
        }
    }

    return devices;
}

// 获取所有设备的 DeviceWidget
QList<DeviceWidget *> MeasurementWidget::getAllDeviceWidgets() const {
    QList<DeviceWidget *> devices;

    QObjectList children = devicesWidget->children();
    foreach (QObject *child, children) {
        DeviceWidget *sw = qobject_cast<DeviceWidget *>(child);
        if (NULL == sw) { continue; }
        devices.append(sw);
    }

    return devices;
}

// 获取选中设备的 DeviceWidget
QList<DeviceWidget *> MeasurementWidget::getSelectedDeviceWidgets() const {
    QList<DeviceWidget *> devices = getAllDeviceWidgets();
    QList<DeviceWidget *> selectedDevices;

    foreach(DeviceWidget *sw, devices) {
        if (sw->isChecked()) {
            selectedDevices.append(sw);
        }
    }

    return selectedDevices;
}

// 初始化界面
void MeasurementWidget::initializeUi() {
    ui->setupUi(this);
    setAttribute(Qt::WA_StyledBackground);

    QVBoxLayout *l = new QVBoxLayout();
    l->addStretch();

    // ScrollArea 的 content widget
    devicesWidget = new QWidget();
    devicesWidget->setLayout(l);
    devicesWidget->setObjectName("devicesWidget");
    ui->devicesScrollArea->setWidget(devicesWidget);
    UiUtil::setWidgetPaddingAndSpacing(devicesWidget, 0, 1);
}

// 信号槽事件处理
void MeasurementWidget::handleEvents() {
    // 点击刷新按钮
    connect(ui->refreshButton, &QPushButton::clicked, [this] {
        QList<Device> devices = getSelectedDevices();
        foreach (Device device, devices) {
            qDebug() << device.toString();
        }
    });

    connect(ui->helpButton, &QPushButton::clicked, [this] {
        TopWindow::message("<img src=\"image/doc/help-1.jpg\" width=300 height=400>");
    });
}
