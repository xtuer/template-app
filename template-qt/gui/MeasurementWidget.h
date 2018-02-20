#ifndef MEASUREMENTWIDGET_H
#define MEASUREMENTWIDGET_H

#include <QWidget>
#include <QList>

class Device;
class DeviceWidget;

namespace Ui {
class MeasurementWidget;
}

class MeasurementWidget : public QWidget {
    Q_OBJECT

public:
    explicit MeasurementWidget(QWidget *parent = 0);
    ~MeasurementWidget();

    /**
     * 添加设备
     */
    void addDevice(const Device &device);

    /**
     * 获取选中的设备
     * @return  返回 Device 的 list
     */
    QList<Device> getSelectedDevices() const;

    /**
     * 获取所有设备的 DeviceWidget
     * @return 返回 DeviceWidget* 的 list
     */
    QList<DeviceWidget *> getAllDeviceWidgets() const;

    /**
     * 获取选中设备的 DeviceWidget
     * @return 返回 DeviceWidget* 的 list
     */
    QList<DeviceWidget *> getSelectedDeviceWidgets() const;

private:
    void initializeUi(); // 初始化界面
    void handleEvents(); // 信号槽事件处理

    Ui::MeasurementWidget *ui;
    QWidget *devicesWidget; // 设备的 widget，作为 QScrollArea 的 content widget
};

#endif // MEASUREMENTWIDGET_H
