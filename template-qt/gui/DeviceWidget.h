#ifndef DEVICEWIDGET_H
#define DEVICEWIDGET_H

#include <QWidget>

class Device;
class DeviceWidgetPrivate;

namespace Ui {
class DeviceWidget;
}

/**
 * 传感器设备
 */
class DeviceWidget : public QWidget {
    Q_OBJECT

public:
    explicit DeviceWidget(const Device &sensor, QWidget *parent = 0);
    ~DeviceWidget();

    void setId(const QString &id);     // 记录器 ID
    void setType(const QString &type); // 类型: T, H, P
    void setAddress(const QString &address);
    void setVoltage(int voltage);  // 电压
    void setWorking(bool working); // 工作状态: 工作中为 true，否则为 false
    void setChecked(bool checked); // 是否选中

    bool isChecked() const; // 此 widget 选中时返回 true，否则返回 false
    void setDevice(const Device &device); // 设置显示到此 widget 的设备
    Device getDevice() const;             // 获取此 widget 显示的设备

protected:
    // 点击鼠标左键选中和取消选中设备
    void mousePressEvent(QMouseEvent *event) Q_DECL_OVERRIDE;

private:
    void initializeUi(); // 初始化界面
    void handleEvents(); // 信号槽事件处理

    Ui::DeviceWidget *ui;
    DeviceWidgetPrivate *d;
};

#endif // DEVICEWIDGET_H
